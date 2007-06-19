/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.junit.QueryTestSDBFactory;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.sdb.util.StoreUtils;

import static com.hp.hpl.jena.sdb.test.SDBTest.manifest;
import static com.hp.hpl.jena.sdb.test.SDBTest.storeDescBase;
//import static com.hp.hpl.jena.sdb.test.SDBTest.testDirSDB;

@RunWith(AllTests.class)
public class SDBQueryTestSuite extends TestSuite
{
    // 1 - call "QueryTestSuite"
    // 2 - read an RDF file.
    
    //  static suite() becomes in JUnit 4:... 
    //  @RunWith(Suite.class) and SuiteClasses(TestClass1.class, ...)
    
    // @RunWith(Parameterized.class) and parameters are sdb files or Stores
    // But does not allow for programmatic construction of a test suite.

    static boolean includeHash      = false ;
    static boolean includeIndex     = true ;
    
    static boolean includeDerby     = true ;
    static boolean includeMySQL     = false ;
    static boolean includePGSQL     = false ;
    static boolean includeHSQL      = false ;
    static boolean includeSQLServer = false ;
    
    // Old style (JUnit3) but it allows programmatic
    // construction of the test suite hierarchy from a script.
    
    static public TestSuite suite() { return new SDBQueryTestSuite() ; }
    
    private SDBQueryTestSuite()
    {
        super("SDB") ;
        for ( Pair<Store, String> p : stores() )
        {
            TestSuite ts2 = makeSuite(p.getLeft(), p.getRight()) ;
            //ts2.setName(ts2.getName()+" - "+p.getRight()) ;
            ts2.setName(p.getRight()) ;
            addTest(ts2) ;
        }
    }
    
    private static TestSuite makeSuite(Store store, String label)
    {
        TestSuite ts = QueryTestSDBFactory.make(store, manifest, label+" - ") ;
        return ts ;
    }

    
    private static List<Pair<Store, String>> stores() 
    {
        // Move this to an "sdb manifest" file.
        
        List<Pair<Store, String>> stores = new ArrayList<Pair<Store, String>>() ;

        if ( true )
            // PostgreSQL gets upset with comments in comments.
            ARQ.getContext().setFalse(SDB.annotateGeneratedSQL) ;
        
        if ( includeDerby )
        {
            if ( includeHash )  worker(stores, "Derby/Hash",  storeDescBase+"derby-hash.ttl") ;
            if ( includeIndex ) worker(stores, "Derby/Index", storeDescBase+"derby-index.ttl") ;
        }
        
        if ( includeMySQL )
        {
            if ( includeHash )  worker(stores, "MySQL/Hash",  storeDescBase+"mysql-hash.ttl") ;
            if ( includeIndex ) worker(stores, "MySQL/Index", storeDescBase+"mysql-index.ttl") ;
        }

        if ( includePGSQL )
        {
            if ( includeHash )  worker(stores, "PGSQL/Hash",  storeDescBase+"pgsql-hash.ttl") ;
            if ( includeIndex ) worker(stores, "PGSQL/Index", storeDescBase+"pgsql-index.ttl") ;
        }

        if ( includeSQLServer )
        {
            if ( includeHash )  worker(stores, "MS-SQL-e/Hash",  storeDescBase+"mssql-e-hash.ttl") ;
            if ( includeIndex ) worker(stores, "MS-SQL-e/Index", storeDescBase+"mssql-e-index.ttl") ;
        }
        
        if ( includeHSQL )
        {
            if ( includeHash )  worker(stores, "HSQLDB/Hash",  storeDescBase+"hsqldb-hash.ttl") ;
            if ( includeIndex ) worker(stores, "HSQLDB/Index", storeDescBase+"hsqldb-index.ttl") ;
        }
        
        return stores ;
    }

    private static void worker(List<Pair<Store, String>> data, String label, String storeDescFile)
    {
        Store store = StoreFactory.create(storeDescFile) ;
        if ( StoreUtils.isHSQL(store) )
            // HSQL (in memory) need formatting.
            store.getTableFormatter().format() ;
        Pair<Store, String> e = new Pair<Store, String>(store, label) ;
        data.add(e) ;
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
