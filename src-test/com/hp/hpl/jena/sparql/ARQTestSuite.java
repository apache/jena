/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql;


import junit.framework.JUnit4TestAdapter ;
import junit.framework.TestSuite ;
import org.openjena.atlas.TC_Atlas ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.TC_Riot ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.algebra.TC_Algebra ;
import com.hp.hpl.jena.sparql.api.TS_API ;
import com.hp.hpl.jena.sparql.core.bio.TestBindingStreams ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.TS_Expr ;
import com.hp.hpl.jena.sparql.graph.TS_Graph ;
import com.hp.hpl.jena.sparql.junit.ScriptTestSuiteFactory ;
import com.hp.hpl.jena.sparql.lang.TS_Lang ;
import com.hp.hpl.jena.sparql.larq.TS_LARQ ;
import com.hp.hpl.jena.sparql.modify.TS_Update ;
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

        // Binding I/O
        ts.addTest(TestBindingStreams.suite()) ;

        
        // Algebra
        ts.addTest(new JUnit4TestAdapter(TC_Algebra.class)) ;

        // Syntax
        ts.addTest(TS_Syntax.suite()) ;
        // Serialization
        ts.addTest(TS_Serialization.suite()) ;
        
        // Scripted tests for SPARQL
        ts.addTest(ScriptTestSuiteFactory.make(testDirARQ+"/manifest-arq.ttl")) ;
      
        // ARQ + Lucene
        ts.addTest(TS_LARQ.suite()) ;
      
        // Scripted tests for ARQ features outside SPARQL syntax
        // Currently at end of manifest-arq.ttl
//        ts.addTest(QueryTestSuiteFactory.make(testDirARQ+"/manifest-ext.ttl")) ;
        
        // The DAWG official tests (some may be duplicated in ARQ test suite
        // but this should be the untouched versions)
        ts.addTest(TS_DAWG.suite()) ;
      
        // The RDQL engine ported to ARQ
        // Includes TS_ExprRDQL so TS_* runs twice. 166 tests
        ts.addTest(TS_RDQL.suite()) ;
      
        // API
        ts.addTest(new JUnit4TestAdapter(TS_API.class)) ;
        
        // SPARQL/Update
        ts.addTest(TS_Update.suite()) ;
        
        ts.addTest(TS_SSE.suite()) ;
        

        ts.addTest(new JUnit4TestAdapter(TS_Graph.class)) ;
        ts.addTest(new JUnit4TestAdapter(TS_Lang.class)) ;
        ts.addTest(new JUnit4TestAdapter(TS_Solver.class)) ;
        
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

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
