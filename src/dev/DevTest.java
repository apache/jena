/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import arq.cmd.CmdUtils;

import com.hp.hpl.jena.query.junit.TestItem;
import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerBasicPattern;
import com.hp.hpl.jena.sdb.junit.QueryTestSDB;
import com.hp.hpl.jena.sdb.junit.QueryTestSDBFactory;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleHSQL;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleMySQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesHSQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesMySQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.test.SDBTest;
import com.hp.hpl.jena.util.FileManager;

@RunWith(AllTests.class)
public class DevTest extends TestSuite
{
//  Is there a better way to dynamically build a set of tests?
    static public TestSuite suite() {
        CmdUtils.setLog4j() ;
        return new DevTest();
    }

    private Store store ;
    
    private DevTest()
    {
        super("SDB - Dev test") ;
        if ( true )
        {
            SDBConnection.logSQLExceptions = true ;
            SDBConnection.logSQLStatements = false ;
            QueryCompilerBasicPattern.printSQL = false ;
        }
        initMySQL_2() ;
        loadTests() ;
    }
        
    private void loadTests()
    {
        test(store,
             SDBTest.testDirSDB+"General/general-1.rq",
             SDBTest.testDirSDB+"General/data.ttl") ;
        test(store,
             SDBTest.testDirSDB+"BasicPatterns/basic-1.rq",
             SDBTest.testDirSDB+"Data/data.ttl") ;

        
//        loadManifest(SDBTest.testDirSDB+"General/manifest.ttl") ;
//        loadManifest(SDBTest.testDirSDB+"BasicPatterns/manifest.ttl") ; 
//        loadManifest(SDBTest.testDirSDB+"Optionals1/manifest.ttl") ; 
    }

    private void loadManifest(String s)
    {
        TestSuite ts = QueryTestSDBFactory.make(store, s) ; 
        ts.setName(ts.getName()) ;
        addTest(ts) ;
    }
    
    private void initHSQL_1()
    {
        JDBC.loadDriverHSQL() ;
        SDBConnection sdb = new SDBConnection("jdbc:hsqldb:mem:testdb1", "sa", "") ;
        store = new StoreSimpleHSQL(sdb) ;
        // Init.
        store.getTableFormatter().format() ;
    }

    private void initHSQL_2()
    {
        JDBC.loadDriverHSQL() ;
        SDBConnection sdb = new SDBConnection("jdbc:hsqldb:mem:testdb2", "sa", "") ;
        store = new StoreTriplesNodesHSQL(sdb) ;
        // Init.
        store.getTableFormatter().format() ;
    }
    
    private void initMySQL_1()
    {
        JDBC.loadDriverMySQL() ;
        SDBConnection sdb = new SDBConnection("jdbc:mysql://localhost/SDB1", Access.getUser(), Access.getPassword()) ;
        store = new StoreSimpleMySQL(sdb) ;
    }

    private void initMySQL_2()
    {
        JDBC.loadDriverMySQL() ;
        SDBConnection sdb = new SDBConnection("jdbc:mysql://localhost/SDB2", Access.getUser(), Access.getPassword()) ;
        store = new StoreTriplesNodesMySQL(sdb) ;
    }
    
    private void test(Store store, String queryFile, String dataFile) { test(store, null, queryFile, dataFile) ; }
    static int count = 0 ; 
    private void test(Store store, String testName, String queryFile, String dataFile)
    {
        if ( testName == null )
            testName = "Test "+(++count) ;
        
        TestItem testItem = new TestItem(testName, queryFile, dataFile, null) ;
        TestCase tc = new QueryTestSDB(store, testName, FileManager.get() , testItem) ;
        addTest(tc) ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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