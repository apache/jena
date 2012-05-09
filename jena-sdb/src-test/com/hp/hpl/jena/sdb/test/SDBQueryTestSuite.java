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

package com.hp.hpl.jena.sdb.test;

import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.test.junit.QueryTestSDBFactory;

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

        QueryTestSDBFactory.make(this, SDBTestSetup.storeList, SDBTestSetup.manifestMain) ;
        QueryTestSDBFactory.make(this, SDBTestSetup.storeListSimple, SDBTestSetup.manifestSimple) ;
    }
    
 
}
