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

package com.hp.hpl.jena.sparql;

import junit.framework.TestSuite ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;

import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.junit.ScriptTestSuiteFactory ;

/** The test suite for all DAWG (the first SPARQL working group) approved tests. 
 *  Many are the same as or overlap with ARQ tests (because the ARQ ones were 
 *  contributed to DAWG or developed in response the feature design within DAWG)
 *  but we keep this set here as a reference.  
 * 
 *  */
public class TC_DAWG extends TestSuite
{
    static final String testSetNameDAWG        = "DAWG - Misc" ;

    static final public String testDirDAWG         = "testing/DAWG" ;
    static final public String testDirWGApproved   = "testing/DAWG-Final" ;
//    static final public String testDirWGPending    = "testing/DAWG-Pending" ;

    private static boolean bVerboseWarnings ;
    private static boolean bWarnOnUnknownFunction ;
    
    @BeforeClass public static void beforeClass()
    {
        bVerboseWarnings = NodeValue.VerboseWarnings ;
        bWarnOnUnknownFunction = E_Function.WarnOnUnknownFunction ;
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
    }
    
    @AfterClass public static void afterClass()
    {
        NodeValue.VerboseWarnings = bVerboseWarnings ;
        E_Function.WarnOnUnknownFunction = bWarnOnUnknownFunction ;
    }

    // Above does not work yet (Junit3/Junit4) ism.
    static 
    {
        // Switch warnings off for thigns that do occur in the scripted test suites
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ; 
    }
    
    static public TestSuite suite() { return new TC_DAWG(); }

    public TC_DAWG()
    {
        super(TC_DAWG.class.getName()) ;

        // One test, dawg-optional-filter-005-simplified or dawg-optional-filter-005-not-simplified
        // must fail because it's the same query and data with different interpretations of the
        // spec.  ARQ implements dawg-optional-filter-005-not-simplified.

        TestSuite ts1 = new TestSuite("Approved") ;
        ts1.addTest(ScriptTestSuiteFactory.make(testDirWGApproved+"/manifest-evaluation.ttl")) ;

        // These merely duplicate ARQ's syntax tests because Andy wrote the DAWG syntax tests,
        // but they are quick so include the snapshot
        // Eclipse can get confused and may mark them as not run (but they have).
        ts1.addTest(ScriptTestSuiteFactory.make(testDirWGApproved+"/manifest-syntax.ttl")) ;
        addTest(ts1) ;

        TestSuite ts3 = new TestSuite("Misc") ;
        // Others in DAWG-Final::
        ts3.addTest(ScriptTestSuiteFactory.make(testDirDAWG+"/Misc/manifest.n3")) ;
        ts3.addTest(ScriptTestSuiteFactory.make(testDirDAWG+"/Syntax/manifest.n3")) ;
        ts3.addTest(ScriptTestSuiteFactory.make(testDirDAWG+"/regex/manifest.n3")) ;
        ts3.addTest(ScriptTestSuiteFactory.make(testDirDAWG+"/examples/manifest.n3")) ;  // Value testing examples
        //In DAWG-Final:: ts3.addTest(QueryTestSuiteFactory.make(testDirDAWG+"/i18n/manifest.ttl")) ;
        addTest(ts3) ;
    }
}
