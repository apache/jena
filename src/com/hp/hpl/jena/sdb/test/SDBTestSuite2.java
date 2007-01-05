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
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesDerby;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesHSQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesMySQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;

@RunWith(AllTests.class)
public class SDBTestSuite2 extends TestSuite
{
    static boolean includeDerby = true ;
    static boolean includeMySQL = false ;
    static boolean includeHSQL = false ;
    
    static public TestSuite suite() {
        return new SDBTestSuite2();
    }

    private SDBTestSuite2()
    {
        super("SDB - Schema 2") ;
        
        if ( true ) SDBConnection.logSQLExceptions = true ;
        if ( false ) QueryTestSDB.VERBOSE = true ;
        
        if ( includeDerby )
        {
            JDBC.loadDriverDerby() ;
            String url = JDBC.makeURL("derby", "localhost", "DB.test2") ;
            SDBConnection sdb = new SDBConnection(url, null, null) ;
            addTest(QueryTestSDBFactory.make(new StoreTriplesNodesDerby(sdb),
                                             SDBTest.testDirSDB+"manifest-sdb.ttl",
                                             "Schema 2 : ")) ;
        }
        
        if ( includeMySQL )
        {
            JDBC.loadDriverMySQL() ;
            SDBConnection sdb = new SDBConnection("jdbc:mysql://localhost/SDB2",  Access.getUser(), Access.getPassword()) ;
            addTest(QueryTestSDBFactory.make(new StoreTriplesNodesMySQL(sdb),
                                             SDBTest.testDirSDB+"manifest-sdb.ttl",
                                             "Schema 2 : ")) ;
        }
        
        
        if ( includeHSQL )
        {
            JDBC.loadDriverHSQL() ;
            SDBConnection sdb = new SDBConnection("jdbc:hsqldb:mem:testdb2", "sa", "") ;
            Store store = new StoreTriplesNodesHSQL(sdb) ;
            store.getTableFormatter().format() ;
            TestSuite ts = QueryTestSDBFactory.make(store,
                                                    SDBTest.testDirSDB+"/manifest-sdb.ttl",
                                                    "Schema 2 : ") ; 
            ts.setName(ts.getName()+"/HSQL-mem") ;
            addTest(ts) ;
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