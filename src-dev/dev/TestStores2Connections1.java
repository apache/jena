/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
            graph1.getBulkUpdateHandler().removeAll() ;
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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