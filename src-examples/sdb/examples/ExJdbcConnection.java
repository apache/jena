/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import arq.cmd.CmdUtils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.*;

/** Managed JDBC connections : creat */ 

public class ExJdbcConnection
{
    static { CmdUtils.setLog4j() ; }
    
    public static void main(String...argv)
    {
        String jdbcURL = String.format("jdbc:derby:%s", "DB/test2-hash") ;
        JDBC.loadDriverDerby() ;
        
        // Setup - make the JDBC connection and read the store description once.
        Connection jdbc = makeConnection(jdbcURL) ;
        //StoreDesc storeDesc = StoreDesc.read("sdb-store.ttl") ;
        
        // Make a store description without any connection information. 
        StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash,
                                            DatabaseType.Derby) ;
        
        // Make some calls to the store, using the same JDBC connection and store description.
        System.out.println("Subjects: ") ;
        query("SELECT DISTINCT ?s { ?s ?p ?o }", storeDesc, jdbc) ;
        System.out.println("Predicates: ") ;
        query("SELECT DISTINCT ?p { ?s ?p ?o }", storeDesc, jdbc) ;
        System.out.println("Objects: ") ;
        query("SELECT DISTINCT ?o { ?s ?p ?o }", storeDesc, jdbc) ;
    }
    
    public static void query(String queryString, StoreDesc storeDesc, Connection jdbcConnection)
    {
        Query query = QueryFactory.create(queryString) ;

        SDBConnection conn = new SDBConnection(jdbcConnection) ;
        
        Store store = StoreFactory.create(conn, storeDesc) ;
        
        Dataset ds = DatasetStore.create(store) ;
        QueryExecution qe = QueryExecutionFactory.create(query, ds) ;
        try {
            ResultSet rs = qe.execSelect() ;
            ResultSetFormatter.out(rs) ;
        } finally { qe.close() ; }
        // Does not close the JDBC connection.
        // Do not call : store.getConnection().close() , which does close the underlying connection.
        store.close() ;
    }
    
    public static Connection makeConnection(String jdbcURL)
    { 
        try {
            return DriverManager.getConnection(jdbcURL,
                                               Access.getUser(),
                                               Access.getPassword()) ;
        } catch (SQLException ex)
        {
            throw new SDBException("SQL Exception while connecting to database: "+jdbcURL+" : "+ex.getMessage()) ;
        }
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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