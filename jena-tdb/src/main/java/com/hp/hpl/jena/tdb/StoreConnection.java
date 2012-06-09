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
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;
import com.hp.hpl.jena.tdb.transaction.* ;

/** Interface to the TDB transaction mechanism. */
public class StoreConnection
{
    // A StoreConnection is the reference to the underlying storage.
    // There is cache of backing datasets, managed by statics.
    // The work of transaction coordination is done in TransactionManager.

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
    
    // Ensure that a dadaset used non-trasnactionally has been flushed to disk
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
                    // Sync the underlying databse in case used
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

//    /**
//     * Return the associated transaction manager - do NOT use to manipulate
//     * transactions
//     */
//    public TransactionManager getTransMgr()
//    {
//        checkValid() ;
//        return transactionManager ;
//    }

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
     * testing operation - do not use the base dataset without knowing how the
     * transaction system uses it
     */
    public DatasetGraphTDB getBaseDataset()
    {
        checkValid() ;
        return baseDSG ;
    }

    private static Map<Location, StoreConnection> cache = new HashMap<Location, StoreConnection>() ;

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
        Set<Location> x = new HashSet<Location>(cache.keySet()) ;
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
    }

    /**
     * Return a StoreConnection for a particular connection. This is used to
     * create transactions for the database at the location.
     */
    public static synchronized StoreConnection make(Location location)
    {
        StoreConnection sConn = cache.get(location) ;
        if (sConn != null) return sConn ;

        DatasetGraphTDB dsg = DatasetBuilderStd.build(location) ;
        sConn = _makeAndCache(dsg) ;
        return sConn ;
    }

//    /**
//     * Return a StoreConnection for a particular connection. This is used to
//     * create transactions for the database at the location.
//     */
//    public static synchronized StoreConnection make(DatasetGraphTDB dsg)
//    {
//        if (dsg instanceof DatasetGraphTxn)
//        {
//            // ((DatasetGraphTxn)dsg).getTransaction().getBaseDataset() ;
//            throw new TDBTransactionException(
//                                              "Can't make a StoreConnection from a transaction instance - need the base storage DatasetGraphTDB") ;
//        }
//        Location location = dsg.getLocation() ;
//
//        StoreConnection sConn = cache.get(location) ;
//        if (sConn == null) sConn = _makeAndCache(dsg) ;
//        return sConn ;
//    }

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
        // Just in case ... also poke the old-style cache.
        TDBMaker.releaseLocation(dsg.getLocation()) ;
        StoreConnection sConn = cache.get(location) ;
        if (sConn == null)
        {
            sConn = new StoreConnection(dsg) ;
            JournalControl.recoverFromJournal(dsg, sConn.transactionManager.getJournal()) ;
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
        DatasetGraphTDB dsg = DatasetBuilderStd.build(Location.mem()) ;
        return new StoreConnection(dsg) ;
    }

}
