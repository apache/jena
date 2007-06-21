/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import static com.hp.hpl.jena.sdb.test.SDBTest.manifest;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.junit.QueryTestSDBFactory;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.Pair;

@RunWith(AllTests.class)
public class SDBQueryTestSuite extends TestSuite
{
    //  static suite() becomes in JUnit 4:... 
    //  @RunWith(Suite.class) and SuiteClasses(TestClass1.class, ...)
    
    // @RunWith(Parameterized.class) and parameters are sdb files or Stores
    // But does not allow for programmatic construction of a test suite.

    // Old style (JUnit3) but it allows programmatic
    // construction of the test suite hierarchy from a script.
    
    static public TestSuite suite() { return new SDBQueryTestSuite() ; }
    
    private SDBQueryTestSuite()
    {
        super("SDB") ;
        
        if ( true )
            // PostgreSQL gets upset with comments in comments??
            ARQ.getContext().setFalse(SDB.annotateGeneratedSQL) ;
        
        for ( Pair<Store, String> p : StoreList.stores() )
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
