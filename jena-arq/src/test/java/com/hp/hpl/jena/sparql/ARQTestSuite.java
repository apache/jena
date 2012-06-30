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
import com.hp.hpl.jena.query.TS_ParamString ;
import com.hp.hpl.jena.sparql.algebra.TC_Algebra ;
import com.hp.hpl.jena.sparql.api.TS_API ;
import com.hp.hpl.jena.sparql.engine.TS_Engine ;
import com.hp.hpl.jena.sparql.engine.binding.TestBindingStreams ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.TS_Expr ;
import com.hp.hpl.jena.sparql.graph.TS_Graph ;
import com.hp.hpl.jena.sparql.junit.ScriptTestSuiteFactory ;
import com.hp.hpl.jena.sparql.lang.TS_Lang ;
import com.hp.hpl.jena.sparql.modify.TS_Update ;
import com.hp.hpl.jena.sparql.path.TS_Path ;
import com.hp.hpl.jena.sparql.resultset.TS_ResultSet ;
import com.hp.hpl.jena.sparql.solver.TS_Solver ;
import com.hp.hpl.jena.sparql.syntax.TS_SSE ;
import com.hp.hpl.jena.sparql.syntax.TS_Serialization ;
import com.hp.hpl.jena.sparql.syntax.TS_Syntax ;
import com.hp.hpl.jena.sparql.util.TS_Util ;

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
        // See also TS_General.
        
        TestSuite ts = new ARQTestSuite() ;
        
        // No warnings (e.g. bad lexical forms).
        ErrorHandlerFactory.setTestLogging(false) ;
        
        // ARQ dependencies
        ts.addTest(new JUnit4TestAdapter(TC_Atlas.class)) ;
        ts.addTest(new JUnit4TestAdapter(TC_Riot.class)) ;
        
        // Fiddle around with the config if necessary
        if ( false )
        {
            QueryEngineMain.unregister() ;
            QueryEngineRef.register() ;
        }

        // Tests should be silent.
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
        
        // Lower level
        ts.addTest(TS_General.suite() );
        ts.addTest(TS_Expr.suite()) ;
        ts.addTest(TS_Util.suite()) ;
        ts.addTest(new JUnit4TestAdapter(TS_Lang.class)) ;
        ts.addTest(new JUnit4TestAdapter(TS_ResultSet.class)) ;
        
        // Syntax
        ts.addTest(TS_Syntax.suite()) ;
        // Serialization
        ts.addTest(TS_Serialization.suite()) ;

        // Binding I/O
        ts.addTest(TestBindingStreams.suite()) ;
        
        // Algebra
        ts.addTest(new JUnit4TestAdapter(TC_Algebra.class)) ;

        // Property paths 
        ts.addTest(new JUnit4TestAdapter(TS_Path.class)) ;
        
        // Scripted tests for SPARQL
        ts.addTest(ScriptTestSuiteFactory.make(testDirARQ+"/manifest-arq.ttl")) ;
      
        // Scripted tests for ARQ features outside SPARQL syntax
        // Currently at end of manifest-arq.ttl
//        ts.addTest(QueryTestSuiteFactory.make(testDirARQ+"/manifest-ext.ttl")) ;
        
        // The DAWG official tests (some may be duplicated in ARQ test suite
        // but this should be the untouched versions)
        ts.addTest(TS_DAWG.suite()) ;
      
        // API
        ts.addTest(new JUnit4TestAdapter(TS_API.class)) ;
        
        // SPARQL/Update
        ts.addTest(TS_Update.suite()) ;
        
        ts.addTest(TS_SSE.suite()) ;

        ts.addTest(new JUnit4TestAdapter(TS_Graph.class)) ;
        ts.addTest(new JUnit4TestAdapter(TS_Solver.class)) ;
        ts.addTest(new JUnit4TestAdapter(TS_Engine.class)) ; 

        // API
        ts.addTest(new JUnit4TestAdapter(TS_ParamString.class)) ;
        
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
