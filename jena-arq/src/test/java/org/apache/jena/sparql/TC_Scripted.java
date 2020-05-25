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

package org.apache.jena.sparql;

import junit.framework.TestSuite ;
import org.apache.jena.riot.langsuite.FactoryTestRiot;
import org.apache.jena.sparql.expr.E_Function ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.junit.ScriptTestSuiteFactory ;

public class TC_Scripted extends TestSuite
{
    static public TestSuite suite()
    {
        TestSuite ts = new TC_Scripted() ;
        ts.addTest(ScriptTestSuiteFactory.make(ARQTestSuite.testDirARQ+"/Syntax/manifest-syntax.ttl")) ;
        ts.addTest(ScriptTestSuiteFactory.make(ARQTestSuite.testDirARQ+"/manifest-arq.ttl")) ;
        ts.addTest(ScriptTestSuiteFactory.make(ARQTestSuite.testDirARQ+"/Serialization/manifest.ttl")) ;
        
        String testDirRDFStar = "testing/ARQ/RDF-Star";
        ts.addTest(FactoryTestRiot.make(testDirRDFStar+"/Turtle-Star/manifest.ttl"));
        ts.addTest(ScriptTestSuiteFactory.make(testDirRDFStar+"/SPARQL-Star/manifest.ttl")) ;
        
        return ts ;
    }

    public TC_Scripted()
    {
        super("Scripted") ;
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
    }
}
