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

package com.hp.hpl.jena.sdb.test.misc;

import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.test.junit.ParamAllStores;
import com.hp.hpl.jena.sparql.sse.SSE;

@RunWith(Parameterized.class)
public class TestConnectionPooled extends ParamAllStores
{
    java.sql.Connection conn ;
    
    // Use "AllStores" so the JDBC connections are already there
    public TestConnectionPooled(String name, Store store)
    {
        super(name, store) ;
    }
    
    @Before public void before()
    {
        store.getTableFormatter().create() ;
        conn = store.getConnection().getSqlConnection() ;
    }

    @After public void after() { }

    
    @Test public void reuseJDBCConection() 
    {
        Triple t1 = SSE.parseTriple("(:x1 :p :z)") ;
        Triple t2 = SSE.parseTriple("(:x2 :p :z)") ;
        boolean explicitTransactions = false ;
        
        // Make store.
        {
            SDBConnection sConn1 = SDBConnectionFactory.create(conn) ;
            Store store1 = StoreFactory.create(sConn1, store.getLayoutType(), store.getDatabaseType()) ;
            
            if ( explicitTransactions )
                store1.getConnection().getTransactionHandler().begin() ;
            
            Graph graph1 = SDBFactory.connectDefaultGraph(store1) ;
            graph1.add(t1) ;
            assertTrue(graph1.contains(t1)) ;
            
            if ( explicitTransactions )
            {
                store1.getConnection().getTransactionHandler().commit() ;
                assertTrue(graph1.contains(t1)) ;
            }
            
            //store1.close() ;
            
        }       
        
        // Mythically return conn to the pool.
        // Get from pool
        // i.e. same connection.  Make a store around it
        
        {
            SDBConnection sConn2 = SDBConnectionFactory.create(conn) ;
            Store store2 = StoreFactory.create(sConn2, store.getLayoutType(), store.getDatabaseType()) ;

            if ( explicitTransactions )
                store2.getConnection().getTransactionHandler().begin() ;
            
            Graph graph2 = SDBFactory.connectDefaultGraph(store2) ;
            assertTrue(graph2.contains(t1)) ;
            
            graph2.add(t2) ;
            assertTrue(graph2.contains(t2)) ;

            if ( explicitTransactions )
                store2.getConnection().getTransactionHandler().commit() ;

            //store2.close() ;
        }
        System.exit(0) ;
    }
}
