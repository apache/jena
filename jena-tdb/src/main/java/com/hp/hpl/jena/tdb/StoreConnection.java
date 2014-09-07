/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.tdb ;

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Map ;
import java.util.Set ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.tdb.base.file.ChannelManager ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.file.LocationLock;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB;
import com.hp.hpl.jena.tdb.transaction.* ;

/** A StoreConnection is the reference to the underlying storage.
 *  There is JVM-wide cache of backing datasets.
 *  The work of transaction coordination is done in TransactionManager.
 */
public class StoreConnection
{
    private final TransactionManager transactionManager ;
    private final DatasetGraphTDB    baseDSG ;
    private boolean                  isValid = true ;
    private volatile boolean         haveUsedInTransaction = false ;

    private StoreConnection(DatasetGraphTDB dsg)
    {
        baseDSG = dsg ;
        transactionManager = new TransactionManager(baseDSG) ;
    }

    private void checkValid()
    {
        if (!isValid) 
            throw new TDBTransactionException("StoreConnection inValid (issued before a StoreConnection.release?") ;
    }
    
    // Ensure that a dataset used non-transactionally has been flushed to disk
    private void checkTransactional()
    {
        // Access to booleans is atomic.
        if ( ! haveUsedInTransaction )
        {
            // See http://en.wikipedia.org/wiki/Double-checked_locking
            // Except we don't have the delayed constructor problem.
            synchronized(this)
            {
                if ( ! haveUsedInTransaction )
                {
                    // Sync the underlying database in case used
                    // non-transactionally by the application.
                    baseDSG.sync() ;
                }
                haveUsedInTransaction = true ;
            }
        }
    }

    public boolean haveUsedInTransaction() { return haveUsedInTransaction ; }
    
    public Location getLocation()
    {
        checkValid() ;
        return baseDSG.getLocation() ;
    }

    /** Return a description of the transaction manager state */
    public SysTxnState getTransMgrState()
    {
        checkValid() ;
        return transactionManager.state() ;
    }

    /**
     * Begin a transaction. Terminate a write transaction with
     * {@link Transaction#commit()} or {@link Transaction#abort()}. Terminate a
     * write transaction with {@link Transaction#close()}.
     */
    public DatasetGraphTxn begin(ReadWrite mode)
    {
        checkValid() ;
        checkTransactional() ;
        haveUsedInTransaction = true ;
        return transactionManager.begin(mode) ;
    }


    /**
     * Begin a transaction, giving it a label. Terminate a write transaction
     * with {@link Transaction#commit()} or {@link Transaction#abort()}.
     * Terminate a write transaction with {@link Transaction#close()}.
     */
    public DatasetGraphTxn begin(ReadWrite mode, String label)
    {
        checkValid() ;
        checkTransactional() ;
        return transactionManager.begin(mode, label) ;
    }

    /**
     * Testing operation - do not use the base dataset without knowing how the
     * transaction system uses it. The base dataset may not reflect the true state
     * if pending commits are queued.
     * @see #flush
     */
    public DatasetGraphTDB getBaseDataset()
    {
        checkValid() ;
        return baseDSG ;
    }
    
    /** Flush the delayed write queue to the base storage.
     *  This can only be done if there are no active transactions.
     *  If there are active transactions, nothing is done but this is safe to call. 
     */ 
    public void flush()
    {
        if ( ! haveUsedInTransaction() )
            return ;
        checkValid() ;
        transactionManager.flush() ;
    }
    
    /** Indicate whether there are any active transactions.
     *  @see #getTransMgrState
     */
    public boolean activeTransactions()
    { 
        checkValid() ;
        return transactionManager.activeTransactions() ; 
    }
    
    /** Flush the journal regardless - use with great case - do not use when transactions may be active. */ 
    public void forceRecoverFromJournal()
    {
        JournalControl.recoverFromJournal(getBaseDataset().getConfig(), transactionManager.getJournal()) ;
    }

    /** Highly risky! */
    public void printJournal()
    {
        JournalControl.print(transactionManager.getJournal()) ;
    }
    
    private static Map<Location, StoreConnection> cache = new HashMap<>() ;

    // ---- statics managing the cache.
    /** Obtain a StoreConenction for a particular location */
    public static StoreConnection make(String location)
    {
        return make(new Location(location)) ;
    }

    /** Stop managing all locations. Use with great care. */
    public static synchronized void reset()
    {
        // Copy to avoid potential CME.
        Set<Location> x = new HashSet<>(cache.keySet()) ;
        for (Location loc : x)
            expel(loc, true) ;
        cache.clear() ;
    }

    /** Stop managing a location. There should be no transactions running. */
    public static synchronized void release(Location location)
    {
        expel(location, false) ;
    }

    /** Stop managing a location. Use with great care (testing only). */
    public static synchronized void expel(Location location, boolean force)
    {
        StoreConnection sConn = cache.get(location) ;
        if (sConn == null) return ;
        
        if (!force && sConn.transactionManager.activeTransactions()) 
            throw new TDBTransactionException("Can't expel: Active transactions for location: " + location) ;

        // No transactions at this point (or we don't care and are clearing up
        // forcefully.)
        sConn.transactionManager.closedown() ;
        sConn.baseDSG.close() ;
        sConn.isValid = false ;
        cache.remove(location) ;
        ChannelManager.release(sConn.transactionManager.getJournal().getFilename()) ;
        
        // Release the lock
        if (SystemTDB.DiskLocationMultiJvmUsagePrevention) {
            if (location.getLock().isOwned()) {
                location.getLock().release();
            } else if (location.getLock().canLock()) {
                SystemTDB.errlog.warn("Location " + location.getDirectoryPath() + " was not locked, if another JVM accessed this location simultaneously data corruption may have occurred");
            }
        }
    }

    /**
     * Return a StoreConnection for a particular connection. This is used to
     * create transactions for the database at the location.
     */
    public static synchronized StoreConnection make(Location location)
    {
        StoreConnection sConn = cache.get(location) ;
        if (sConn != null) 
            return sConn ;
        DatasetGraphTDB dsg = DatasetBuilderStd.create(location) ;
        sConn = _makeAndCache(dsg) ;
        return sConn ;
    }

    /**
     * Return the StoreConnection if one already exists for this location, else
     * return null
     */
    public static synchronized StoreConnection getExisting(Location location)
    {
        return cache.get(location) ;
    }

    private static StoreConnection _makeAndCache(DatasetGraphTDB dsg)
    {
        Location location = dsg.getLocation() ;
        StoreConnection sConn = cache.get(location) ;
        if (sConn == null)
        {
            sConn = new StoreConnection(dsg) ;
            
            if (SystemTDB.DiskLocationMultiJvmUsagePrevention)
            {
                // Obtain the lock ASAP
                LocationLock lock = location.getLock();
                if (lock.canLock()) {
                    if (!lock.canObtain()) 
                        throw new TDBException("Can't open database at location " + location.getDirectoryPath() + " as it is already locked by the process with PID " + lock.getOwner() + ".  TDB databases do not permit concurrent usage across JVMs so in order to prevent possible data corruption you cannot open this location from the JVM that does not own the lock for the dataset");

                    lock.obtain();
                    // There's an interesting race condition here that two JVMs might write out the lock file one after another without
                    // colliding and causing an IO error in either.  The best way to check for this is simply to check we now own the lock
                    // and if not error
                    if (!lock.isOwned()) {
                        throw new TDBException("Can't open database at location " + location.getDirectoryPath() + " as it is alread locked by the process with PID " + lock.getOwner() + ".  TDB databases do not permit concurrent usage across JVMs so in order to prevent possible data corruption you cannot open this location from the JVM that does not own the lock for the dataset");
                    }
                }
            }
            
            sConn.forceRecoverFromJournal() ;
//            boolean actionTaken = JournalControl.recoverFromJournal(dsg.getConfig(), sConn.transactionManager.getJournal()) ;
//            if ( false && actionTaken )
//            {
//                // This should be unnecessary because we wrote the journal replay
//                // via the DSG storage configuration.  
//                sConn.transactionManager.closedown() ;
//                sConn.baseDSG.close() ;
//                dsg = DatasetBuilderStd.build(location) ;
//                sConn = new StoreConnection(dsg) ;
//            }
            
            if (!location.isMemUnique())
                // Don't cache use-once in-memory datasets.
                cache.put(location, sConn) ;
            String NS = TDB.PATH ;
            TransactionInfo txInfo = new TransactionInfo(sConn.transactionManager) ;
            ARQMgt.register(NS + ".system:type=Transactions", txInfo) ;
        }
        return sConn ;
    }

    /**
     * Return a StoreConnection backed by in-memory datastructures (for
     * testing).
     */
    public static StoreConnection createMemUncached()
    {
        DatasetGraphTDB dsg = DatasetBuilderStd.create(Location.mem()) ;
        return new StoreConnection(dsg) ;
    }

}
