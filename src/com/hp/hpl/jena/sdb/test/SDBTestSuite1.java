/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.junit.QueryTestSDBFactory;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleHSQL;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleMySQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;

import junit.framework.TestSuite;


public class SDBTestSuite1 extends TestSuite
{
    static final String testDirSDB = "testing/" ;
    
    static public TestSuite suite() {
        return new SDBTestSuite1();
    }

    private SDBTestSuite1()
    {
        super("SDB - Schema 1") ;
        if ( true ) SDBConnection.logSQLExceptions = true ;
        {
            JDBC.loadDriverMySQL() ;
            SDBConnection sdb = new SDBConnection("jdbc:mysql://localhost/SDB1", Access.getUser(), Access.getPassword()) ;
            addTest(QueryTestSDBFactory.make(new StoreSimpleMySQL(sdb),
                                             testDirSDB+"/manifest-sdb.ttl")) ;
        }
        
        JDBC.loadDriverHSQL() ;
        {
            SDBConnection sdb = new SDBConnection("jdbc:hsqldb:mem:testdb1", "sa", "") ;
            Store store = new StoreSimpleHSQL(sdb) ;
            store.getTableFormatter().format() ;
            TestSuite ts = QueryTestSDBFactory.make(store, testDirSDB+"/manifest-sdb.ttl") ; 
            ts.setName(ts.getName()+"/HSQL-mem") ;
            addTest(ts) ;
        }
        
        if ( false )
        {
            SDBConnection sdb = new SDBConnection("jdbc:hsqldb:file:tmp/testdb1", "sa", "") ;
            Store store = new StoreSimpleHSQL(sdb) ;
            store.getTableFormatter().format() ;
            TestSuite ts = QueryTestSDBFactory.make(new StoreSimpleHSQL(sdb),
                                                   testDirSDB+"/manifest-sdb.ttl") ; 
            ts.setName(ts.getName()+"/HSQL-file") ;
            addTest(ts) ;
        }
        
        
    }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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