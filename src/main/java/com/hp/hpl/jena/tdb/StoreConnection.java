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

package com.hp.hpl.jena.tdb;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;
import com.hp.hpl.jena.tdb.transaction.JournalControl ;
import com.hp.hpl.jena.tdb.transaction.SysTxnState ;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException ;
import com.hp.hpl.jena.tdb.transaction.Transaction ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;
import com.hp.hpl.jena.tdb.transaction.TransactionInfo ;


/** Interface to the TDB transaction mechanism. */ 
public class StoreConnection
{
    // A StoreConnection is the reference to the underlying storage.
    // There is cache of backing datasets, managed by statics in StoreConnection.
    // The work of transaction coordination is done in TransactionManager.
    
    private final TransactionManager transactionManager ;
    private final DatasetGraphTDB baseDSG ;

    private StoreConnection(Location location)
    {
        baseDSG = DatasetBuilderStd.build(location) ;
        transactionManager = new TransactionManager(baseDSG) ;
    }
    
    public Location getLocation() { return baseDSG.getLocation() ; }
    /** Return the associated transaction manager - do NOT use to manipulate transactions */  
    public TransactionManager getTransMgr() { return transactionManager ; }
    
    /** Return a description of the transaction manager state */  
    public SysTxnState getTransMgrState() { return transactionManager.state() ; }
    
    /** Begin a transaction.  
     * Terminate a write transaction with {@link Transaction#commit()} or {@link Transaction#abort()}.
     * Terminate a write transaction with {@link Transaction#close()}.
     */    
    public DatasetGraphTxn begin(ReadWrite mode)
    {
        return transactionManager.begin(mode) ;
    }
    
    /** Begin a transaction, giving it a label.  
     * Terminate a write transaction with {@link Transaction#commit()} or {@link Transaction#abort()}.
     * Terminate a write transaction with {@link Transaction#close()}.
     */    
    public DatasetGraphTxn begin(ReadWrite mode, String label)
    {
        return transactionManager.begin(mode, label) ;
    }
    
    // ---- statics managing the cache.
    /** Obtain a StoreConenction for a particular location */  
    public static StoreConnection make(String location)
    {
        return make(new Location(location)) ; 
    }

    /** testing operation - do not use the base dataset without knowing how the transaction system uses it */
    public DatasetGraphTDB getBaseDataset() { return baseDSG ; }
    
    private static Map<Location, StoreConnection> cache = new HashMap<Location, StoreConnection>() ;
    
    
    /** Stop managing all locations. */  
    public static synchronized void reset() 
    {
        for ( Location loc : cache.keySet() )
            expel(loc, true) ;
        cache.clear() ;
    }
    
    /** Stop managing a location. */  
    public static synchronized void release(Location location)    { expel(location, false) ; }
        
    /** Stop managing a location. */  
    private static synchronized void expel(Location location, boolean force)
    {
        StoreConnection sConn = cache.get(location) ;
        if ( sConn == null )
            return ;
        if ( ! force && sConn.transactionManager.activeTransactions() )
            throw new TDBTransactionException("Can't expel: Active transactions for location: "+location) ;
        
        // No transactions at this point (or we don't care and are clearing up forcefully.)
        sConn.transactionManager.closedown() ;
        sConn.baseDSG.close() ;
        cache.remove(location) ;
    }
    
    /** Return a StoreConnection for a particular connection.  
     * This is used to create transactions for the database at the location.
     */ 
    public static synchronized StoreConnection make(Location location)
    {
        TDBMaker.releaseLocation(location) ;
        StoreConnection sConn = cache.get(location) ;
        if ( sConn == null )
        {
            sConn = new StoreConnection(location) ;
            JournalControl.recovery(sConn.baseDSG) ;
            cache.put(location, sConn) ;

            String NS = TDB.PATH ;
            TransactionInfo txInfo = new TransactionInfo(sConn.getTransMgr()) ;
            ARQMgt.register(NS+".system:type=Transactions", txInfo) ;
        }
        return sConn ; 
    }
    
    /** Return a StoreConnection backed by in-memory datastructures (for testing).
     */ 
    public static StoreConnection createMemUncached()
    {
        return new StoreConnection(Location.mem()) ;
    }
    
}
