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

package com.hp.hpl.jena.sdb.layout2.hash;

import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQLDerby;
import com.hp.hpl.jena.sdb.layout2.LoaderTuplesNodes;
import com.hp.hpl.jena.sdb.layout2.SQLBridgeFactory2;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.StoreLoader;
import com.hp.hpl.jena.sdb.store.TupleGraphLoader;
import com.hp.hpl.jena.sdb.store.TupleLoader;

public class StoreTriplesNodesHashDB2 extends StoreBaseHash
{

    public StoreTriplesNodesHashDB2(SDBConnection connection, StoreDesc desc)
    {
        // One tuple at a time, default graph only, loader.
        super(connection, desc, 
              new FmtLayout2HashDB2(connection) ,
              //loaderSimple(connection),
              new LoaderTuplesNodes(connection, TupleLoaderHashDB2.class),
              new QueryCompilerFactoryHash(), 
              new SQLBridgeFactory2(),
              new GenerateSQLDerby()) ;
        
        // Not for simple loading.
        ((LoaderTuplesNodes) this.getLoader()).setStore(this);
    }
    
    static StoreLoader loaderSimple(SDBConnection connection)
    {
        // Temporary - simple loader for development. 
        //new LoaderTuplesNodes(connection, TupleLoaderHashDerby.class),
        TupleLoader tLoader = new TupleLoaderOneHash(connection, new TableDescTriples()) ;
        StoreLoader sLoader = new TupleGraphLoader(tLoader) ;
        return sLoader ;
    }
    
//    static StoreLoader loaderFull(SDBConnection connection)
//    {
//        StoreLoader sLoader = new LoaderTuplesNodes(connection, TupleLoaderHashDB2.class) ;
//        ((LoaderTuplesNodes) this.getLoader()).setStore(this);
//        return sLoader ;
//    }
    
}
