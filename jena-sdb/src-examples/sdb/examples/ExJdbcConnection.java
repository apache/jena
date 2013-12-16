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

package sdb.examples;

import java.sql.Connection ;
import java.sql.DriverManager ;
import java.sql.SQLException ;

import org.apache.jena.atlas.logging.LogCtl ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sdb.SDBException ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.StoreDesc ;
import com.hp.hpl.jena.sdb.shared.Access ;
import com.hp.hpl.jena.sdb.sql.JDBC ;
import com.hp.hpl.jena.sdb.sql.SDBConnection ;
import com.hp.hpl.jena.sdb.store.DatabaseType ;
import com.hp.hpl.jena.sdb.store.DatasetStore ;
import com.hp.hpl.jena.sdb.store.LayoutType ;
import com.hp.hpl.jena.sdb.store.StoreFactory ;

/** Managed JDBC connections : creat */ 

public class ExJdbcConnection
{
    static { LogCtl.setLog4j() ; }
    
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
        
        Store store = StoreFactory.create(storeDesc, conn) ;
        
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
