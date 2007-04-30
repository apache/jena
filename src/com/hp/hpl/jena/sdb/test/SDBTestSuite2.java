/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.junit.QueryTestSDB;
import com.hp.hpl.jena.sdb.junit.QueryTestSDBFactory;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashMySQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDerby;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexHSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexMySQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexSQLServer;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashSQLServer;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;

@RunWith(AllTests.class)
public class SDBTestSuite2 extends TestSuite
{
    static boolean includeHash = true ;
    static boolean includeIndex = true ;
    
    static boolean includeDerby = true ;
    static boolean includeMySQL = false ;
    static boolean includeHSQL = false ;
    static boolean includeSQLServer = false ;
    
    static public TestSuite suite() {
        return new SDBTestSuite2();
    }

    private SDBTestSuite2()
    {
        super("SDB - Schema 2") ;
        
        if ( true )     SDBConnection.logSQLExceptions = true ;
        if ( false )    QueryTestSDB.VERBOSE = true ;
        
        //Note: make sure all tests have unuque names or else they may not run (Eclipse).  
        if ( includeDerby )
        {
            JDBC.loadDriverDerby() ;
            if ( includeHash )
            {
                String url = JDBC.makeURL("derby", "localhost", "DB/test2-hash") ;
                SDBConnection sdb = new SDBConnection(url, null, null) ;
                TestSuite ts = QueryTestSDBFactory.make(new StoreTriplesNodesHashDerby(sdb),
                                                        SDBTest.testDirSDB+"manifest-sdb.ttl",
                                                        "Derby/Hash - ") ;
                ts.setName(ts.getName()+" (Derby/hash)") ;
                addTest(ts) ;
            }
            if ( includeIndex )
            {
                String url = JDBC.makeURL("derby", "localhost", "DB/test2-index") ;
                SDBConnection sdb = new SDBConnection(url, null, null) ;
                TestSuite ts = QueryTestSDBFactory.make(new StoreTriplesNodesIndexDerby(sdb),
                                                        SDBTest.testDirSDB+"manifest-sdb.ttl",
                                                        "Derby/Index - ") ;
                ts.setName(ts.getName()+" (Derby/index)") ;
                addTest(ts) ;
            }
        }
        
        if ( includeMySQL )
        {
            JDBC.loadDriverMySQL() ;
            if ( includeHash )
            {
                SDBConnection sdb = new SDBConnection("jdbc:mysql://localhost/test2-hash", Access.getUser(), Access.getPassword()) ;
                TestSuite ts = QueryTestSDBFactory.make(new StoreTriplesNodesHashMySQL(sdb),
                                                 SDBTest.testDirSDB+"manifest-sdb.ttl", "MySQL/Hash - ") ;
                ts.setName(ts.getName()+" (MySQL/hash)") ;
                addTest(ts) ;
            }
            if ( includeIndex )
            {
                SDBConnection sdb = new SDBConnection("jdbc:mysql://localhost/test2-index", Access.getUser(), Access.getPassword()) ;
                TestSuite ts = QueryTestSDBFactory.make(new StoreTriplesNodesIndexMySQL(sdb),
                                         SDBTest.testDirSDB+"manifest-sdb.ttl", "MySQL/Index - ") ;
                ts.setName(ts.getName()+" (MySQL/index)") ;
                addTest(ts) ;
            }
        }

        if ( includeSQLServer )
        {
            JDBC.loadDriverSQLServer() ;
            String expressStr= "\\SQLEXPRESS" ;
            
            if ( includeHash )
            {
                String jdbc = String.format("jdbc:sqlserver://localhost%s;databaseName=test2-hash", expressStr) ;
                SDBConnection sdb = new SDBConnection(jdbc, Access.getUser(), Access.getPassword()) ;
                TestSuite ts = QueryTestSDBFactory.make(new StoreTriplesNodesHashSQLServer(sdb),
                                                        SDBTest.testDirSDB+"manifest-sdb.ttl",
                                                        "MS SQL/Hash - ") ;
                ts.setName(ts.getName()+" (MS SQL/hash)") ; 
                addTest(ts) ;
            }
            if ( includeIndex )
            {
                String jdbc = String.format("jdbc:sqlserver://localhost%s;databaseName=test2-index", expressStr) ;
                SDBConnection sdb = new SDBConnection(jdbc, Access.getUser(), Access.getPassword()) ;
                TestSuite ts = QueryTestSDBFactory.make(new StoreTriplesNodesIndexSQLServer(sdb),
                                                        SDBTest.testDirSDB+"manifest-sdb.ttl",
                                                        "MS SQL/Index - ") ;
                ts.setName(ts.getName()+" (MS SQL/index)") ; 
                addTest(ts) ;
            }
        }
        
        if ( includeHSQL )
        {
            JDBC.loadDriverHSQL() ;
            if ( includeHash )
            {
                SDBConnection sdb = new SDBConnection("jdbc:hsqldb:mem:testdb2", "sa", "") ;
                Store store = new StoreTriplesNodesIndexHSQL(sdb) ;
                store.getTableFormatter().format() ;
                TestSuite ts = QueryTestSDBFactory.make(store, 
                                                        SDBTest.testDirSDB+"/manifest-sdb.ttl","HSQL/Hash - ") ;
                ts.setName(ts.getName()+" (HSQL-mem-hash)") ;
                addTest(ts) ;
            }
            
            if ( includeIndex )
            {
                SDBConnection sdb = new SDBConnection("jdbc:hsqldb:mem:testdb2", "sa", "") ;
                Store store = new StoreTriplesNodesIndexHSQL(sdb) ;
                store.getTableFormatter().format() ;
                TestSuite ts = QueryTestSDBFactory.make(store,
                                                        SDBTest.testDirSDB+"/manifest-sdb.ttl", "HSQL/Index - ") ;
                ts.setName(ts.getName()+" (HSQL-mem-index)") ;
                addTest(ts) ;
            }
        }
        
    }

}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
