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


import static org.openjena.riot.ErrorHandlerFactory.errorHandlerNoLogging ;
import static org.openjena.riot.ErrorHandlerFactory.getDefaultErrorHandler ;
import static org.openjena.riot.ErrorHandlerFactory.setDefaultErrorHandler ;
import junit.framework.JUnit4TestAdapter ;
import junit.framework.TestSuite ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.openjena.atlas.TC_Atlas ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.TC_Riot ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

/**
 * All the ARQ tests 
 */

public class ARQTestSuite extends TestSuite
{
    public static final String testDirARQ = "testing/ARQ" ;
    public static final String testDirUpdate = "testing/Update" ;

    // Log4j for testing.
    public static final String log4jPropertiesResourceName = "log4j-testing.properties" ;
    static { System.getProperty("log4j.configuration", log4jPropertiesResourceName) ; }
    
    static public TestSuite suite()
    {
        TestSuite ts = new ARQTestSuite() ;
        
        // No warnings (e.g. bad lexical forms).
        BaseTest.setTestLogging() ;
        
        // ARQ dependencies
        ts.addTest(new JUnit4TestAdapter(TC_Atlas.class)) ;
        ts.addTest(new JUnit4TestAdapter(TC_Riot.class)) ;

        // Main ARQ internal test suite.
        ts.addTest(new JUnit4TestAdapter(TC_General.class)) ;
        
        ts.addTest(TC_Scripted.suite()) ;
        ts.addTest(TC_DAWG.suite()) ;
        //ts.addTest(TC_SPARQL11.suite()) ;
        
        // Fiddle around with the config if necessary
        if ( false )
        {
            QueryEngineMain.unregister() ;
            QueryEngineRef.register() ;
        }
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
