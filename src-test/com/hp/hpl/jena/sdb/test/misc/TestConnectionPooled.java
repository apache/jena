/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test.misc;

import static org.junit.Assert.assertTrue;
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
    public TestConnectionPooled(String name, Store store)
    {
        super(name, store) ;
    }
    
    @Test public void reuseJDBCConection() 
    {
        Triple t1 = SSE.parseTriple("(:x1 :p :z)") ;
        Triple t2 = SSE.parseTriple("(:x2 :p :z)") ;
        boolean explicitTransactions = false ;
        java.sql.Connection conn = store.getConnection().getSqlConnection() ;
        store.getTableFormatter().create() ;
        
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

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */