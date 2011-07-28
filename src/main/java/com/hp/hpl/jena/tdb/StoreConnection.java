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
import com.hp.hpl.jena.tdb.transaction.JournalControl ;
import com.hp.hpl.jena.tdb.transaction.SysTxnState ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

/** Interface to the TDB transaction mechanism. */ 
public class StoreConnection
{
    // Contains the cache.
    // Most of the work is done in the TransactionManager
    
    private final TransactionManager transactionManager ;
    private final DatasetGraphTDB baseDSG ;

    private StoreConnection(Location location)
    {
        baseDSG = DatasetBuilderStd.build(location) ;
        transactionManager = new TransactionManager(baseDSG) ;
    }
    
    public Location getLocation() { return baseDSG.getLocation() ; }
    
    public SysTxnState getTransMgrState() { return transactionManager.state() ; }
    
    public DatasetGraphTxn begin(ReadWrite mode)
    {
        return transactionManager.begin(mode) ;
    }
    
    public DatasetGraphTxn begin(ReadWrite mode, String label)
    {
        return transactionManager.begin(mode, label) ;
    }
    
    // ---- statics managing the cache.
    
    public static StoreConnection make(String location)
    {
        return make(new Location(location)) ; 
    }

    private static Map<Location, StoreConnection> cache = new HashMap<Location, StoreConnection>() ;
    
    public static synchronized void reset() 
    {
        for ( Map.Entry<Location, StoreConnection> e : cache.entrySet() )
            e.getValue().baseDSG.close() ;
        
        cache.clear() ;
    }
    
    public static synchronized StoreConnection make(Location location)
    {
        TDBMaker.releaseLocation(location) ;
        StoreConnection sConn = cache.get(location) ;
        if ( sConn == null )
        {
            sConn = new StoreConnection(location) ;
            JournalControl.recovery(sConn.baseDSG) ;
            cache.put(location, sConn) ;
        }
        return sConn ; 
    }
}

