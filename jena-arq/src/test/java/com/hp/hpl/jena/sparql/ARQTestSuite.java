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


import junit.framework.JUnit4TestAdapter ;
import junit.framework.TestSuite ;
import org.openjena.atlas.TC_Atlas ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.TC_Riot ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.junit.ScriptTestSuiteFactory ;
import com.hp.hpl.jena.sparql.modify.TS3_Update ;
import com.hp.hpl.jena.sparql.syntax.TS3_Syntax ;

/**
 * All the ARQ tests 
 */

public class ARQTestSuite extends TestSuite
{
    // Log4j for testing.
    public static final String log4jPropertiesResourceName = "log4j-testing.properties" ;
    static { System.getProperty("log4j.configuration", log4jPropertiesResourceName) ; }
    
    public static final String testDirARQ = "testing/ARQ" ;
    
    static public TestSuite suite()
    {
        // See also TC_General.
        
        TestSuite ts = new ARQTestSuite() ;
        
        // No warnings (e.g. bad lexical forms).
        ErrorHandlerFactory.setTestLogging(false) ;
        
        // ARQ dependencies
        ts.addTest(new JUnit4TestAdapter(TC_Atlas.class)) ;
        ts.addTest(new JUnit4TestAdapter(TC_Riot.class)) ;

        // Main ARQ internal test suite.
        ts.addTest(new JUnit4TestAdapter(TC_General.class)) ;
        
        // Fiddle around with the config if necessary
        if ( false )
        {
            QueryEngineMain.unregister() ;
            QueryEngineRef.register() ;
        }

        // JUnit3 style. 
        ts.addTest(TS3_Syntax.suite()) ;
        ts.addTest(TS3_Update.suite()) ;
        ts.addTest(TS3_Syntax.suite()) ;
        
        // Tests should be silent.
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
        
        // Lower level

        // Scripted tests for SPARQL
        ts.addTest(ScriptTestSuiteFactory.make(testDirARQ+"/manifest-arq.ttl")) ;

        // SPARQL 1.1 test suite (when finalized)
        // ts.addTest(TC_SPARQL11.suite()) ;
        
        // Scripted tests for ARQ features outside SPARQL syntax
        // Currently at end of manifest-arq.ttl
//        ts.addTest(QueryTestSuiteFactory.make(testDirARQ+"/manifest-ext.ttl")) ;
        
        // The DAWG official tests (some may be duplicated in ARQ test suite
        // but this should be the untouched versions)
        ts.addTest(TC_DAWG.suite()) ;
        return ts ;
    }

	private ARQTestSuite()
	{
        super("All ARQ tests");
        ARQ.init() ;
        // Tests should be silent.
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
	}
}
