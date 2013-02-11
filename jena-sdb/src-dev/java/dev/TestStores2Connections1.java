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

package dev;

import java.sql.SQLException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.sql.TableUtils;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sparql.sse.SSE;

/** 
 * Imitate connection pooling. 
 * Create an SQL/JDBC connection, create a store around it, do something
 * then remove the store (forget it, close it).
 * Create another store on the JDBC conenction.
 */
public class TestStores2Connections1
{
    public static void main(String ... argv) throws SQLException
    {
        Triple t1 = SSE.parseTriple("(:x1 :p :z)") ;
        Triple t2 = SSE.parseTriple("(:x2 :p :z)") ;

        StoreDesc desc = StoreDesc.read("sdb.ttl") ;
        java.sql.Connection conn = SDBFactory.createSqlConnection("sdb.ttl") ;
        
        
        boolean explicitTransactions = false ;
        {
            SDBConnection sConn1 = SDBConnectionFactory.create(conn) ;
            Store store1 = StoreFactory.create(desc, sConn1) ;
            
            if ( explicitTransactions )
                store1.getConnection().getTransactionHandler().begin() ;
            
            Graph graph1 = SDBFactory.connectDefaultGraph(store1) ;
            graph1.clear() ;
            SSE.write(graph1) ; System.out.println();
            graph1.add(t1) ;
            SSE.write(graph1) ; System.out.println();
            
            // Look for temporary tables.
            TableUtils.hasTable(sConn1.getSqlConnection(), "#NNodeTriples", new String[0]) ;

            if ( explicitTransactions )
                store1.getConnection().getTransactionHandler().commit() ;
            
            //store1.close() ;
            
        }       
        
        // Mythically return conn to the pool.
        // Get from pool
        // i.e. same connection.  Make a store around it
        
        {
            SDBConnection sConn2 = SDBConnectionFactory.create(conn) ;
            Store store2 = StoreFactory.create(desc, sConn2) ;

            if ( explicitTransactions )
                store2.getConnection().getTransactionHandler().begin() ;
            
            Graph graph2 = SDBFactory.connectDefaultGraph(store2) ;
            graph2.add(t2) ;
            SSE.write(graph2) ; System.out.println();
            
            if ( explicitTransactions )
                store2.getConnection().getTransactionHandler().commit() ;

            store2.close() ;
        }
        System.exit(0) ;
    }
}
