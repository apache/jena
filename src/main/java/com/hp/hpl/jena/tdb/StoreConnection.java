/**
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

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxnTDB ;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

public class StoreConnection
{
    private static TransactionManager transactionManager = new TransactionManager() ;
    
    private DatasetGraphTDB baseDSG ;
    private static int transactionCounter = 0 ;
    private int transactionId = 0 ;         // Per dataset count (unliek trasnaction ids which are global)
    private int readers = 0 ; 
    private int writers = 0 ;       // 0 or 1 
    private int committed = 0 ;     // Committed but not replyed yet.
    

    private StoreConnection(Location location)
    {
        baseDSG = DatasetBuilderStd.build(location.getDirectoryPath()) ;
        transactionCounter++ ;
        transactionId = transactionCounter ;
    }
    
    public Location getLocation() { return baseDSG.getLocation() ; }
    
    synchronized
    public DatasetGraphTxnTDB begin(ReadWrite mode)
    {
        switch (mode)
        {
            case READ :
                readers++ ;
                DatasetGraphTxnTDB dsg = null ;
                dsg.setReadOnly(true) ;
                // Make a new read-only dataset.
                //new DatasetGraphTxnRead(baseDSG) ;
                break ;
            case WRITE :
                if ( writers > 0 )
                    throw new TDBTransactionException("Existing active transaction") ;
                // Check only active transaction.
                // Make from the last commited transation or base.
                
                // Attach connection to DatasetGraphTxnTDB
                DatasetGraphTxnTDB dsg2 = transactionManager.begin(baseDSG) ;
                dsg2.getTransaction() ;
                return dsg2 ;
        }
        System.err.println("StoreConnection.begin: Not implemented fully") ;
        return null ;
    }
    
    // ---- statics
    
    public static StoreConnection make(String location)
    {
        return make(new Location(location)) ; 
    }

    private static Map<Location, StoreConnection> cache = new HashMap<Location, StoreConnection>() ;
    
    public static StoreConnection make(Location location)
    {
        TDBMaker.releaseLocation(location) ;
        StoreConnection sConn = cache.get(location) ;
        if ( sConn == null )
        {
            sConn = new StoreConnection(location) ;
            cache.put(location, sConn) ;
        }
        return sConn ; 
    }
    
    
    
}

