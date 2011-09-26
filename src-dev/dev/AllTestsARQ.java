/**
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

package dev;

import junit.framework.TestSuite ;
import arq.examples.test.TestLARQExamples ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQTestSuite ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.junit.ScriptTestSuiteFactory ;

/** All tests - the main test suite and also the examples tests */
public class AllTestsARQ extends TestSuite
{
    /*====================
     * Eclipse: Some ARQ tests are also DAWG tests (and so have same name)
     * This confused JUnit/Eclipse but it seems to be safe and they are all run.
     *==================== 
     */
    
    static public TestSuite suite()
    {
        // Fiddle around with the config if necessary
        if ( false )
        {
            QueryEngineMain.unregister() ;
            QueryEngineRef.register() ;
        }
        
        TestSuite ts = new AllTestsARQ() ;

        // Main test suite
        // This is the test suite run by maven.
        ts.addTest(ARQTestSuite.suite()) ;
        
        /*
         * The test count here and maven differ 
         * Example are not part of the mvn testing process 
         */
        
        // Scripted tests for ARQ examples.
        ts.addTest(ScriptTestSuiteFactory.make(ARQTestSuite.testDirARQ+"/Examples/manifest.ttl")) ;
        ts.addTest(TestLARQExamples.suite()) ;
        return ts ;
    }
    
    private AllTestsARQ()
    {
        super("ARQ");
        ARQ.init() ;
        // Tests should be silent.
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
    }

}
