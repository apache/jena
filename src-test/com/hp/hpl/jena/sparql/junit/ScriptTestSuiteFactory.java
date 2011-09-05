/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import junit.framework.Test ;
import junit.framework.TestCase ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.core.DataFormat ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestUpdate_11 ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest_11 ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.junit.TestFactoryManifest ;
import com.hp.hpl.jena.util.junit.TestUtils ;
import com.hp.hpl.jena.vocabulary.RDF ;


public class ScriptTestSuiteFactory extends TestFactoryManifest
{
    private FileManager fileManager = FileManager.get() ;
    // Set (and retrieve) externally.
    public static EarlReport results = null ;

    /** Make a test suite from a manifest file */
    static public TestSuite make(String filename) 
    {
        ScriptTestSuiteFactory tFact = new ScriptTestSuiteFactory() ;
        return tFact.process(filename) ;
    }

    /** Make a single test */
    static public TestSuite make(String query, String data, String result)
    {
        TestItem item = TestItem.create(query, query, data, result) ;
        QueryTest t = new QueryTest(item.getName(), null, FileManager.get(), item) ;
        TestSuite ts = new TestSuite() ;
        ts.setName(TestUtils.safeName(query)) ;
        ts.addTest(t) ;
        return ts ;
    }
    
    @Override
    public Test makeTest(Resource manifest, Resource entry, String testName, Resource action, Resource result)
    {
        if ( action == null )
        {
            System.out.println("Null action: "+entry) ;
            return null ;
        }
        
        // Defaults.
        Syntax querySyntax = TestQueryUtils.getQuerySyntax(manifest)  ;
        
        if ( querySyntax != null )
        {
            if ( ! querySyntax.equals(Syntax.syntaxARQ) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_10) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_11) )
                throw new QueryTestException("Unknown syntax: "+querySyntax) ;
        }
        
        // May be null
        Resource defaultTestType = TestUtils.getResource(manifest, TestManifestX.defaultTestType) ;
        // test name
        // test type
        // action -> query specific query[+data]
        // results
        
        //TestItem only works for query - bodged for Update.

        
        Resource testType = defaultTestType ;
        if ( entry.hasProperty(RDF.type) )
            testType = entry.getProperty(RDF.type).getResource() ;
        
        TestItem item = null ;
        if ( testType == null 
            || // Not an update evaluation test.  
                ! ( testType.equals(TestManifestUpdate_11.UpdateEvaluationTest) || testType.equals(TestManifest_11.UpdateEvaluationTest) )
           )
        {
            // Bodge.  Update results can be a Bnode.
            //  TestItem.create assumes it is a a URI or string.
            // Clean up.
            item = TestItem.create(entry, defaultTestType, querySyntax, DataFormat.langXML) ;
        }
        
        TestCase test = null ;

        // Frankly this all needs rewriting.
        // Drop the idea of testItem.  pass entry/action/result to subclass.
        // Library for parsing entries.
        
        if ( testType != null )
        {
            // == Good syntax
            if ( testType.equals(TestManifest.PositiveSyntaxTest) )
                return new SyntaxTest(testName, results, item) ;
            if ( testType.equals(TestManifest_11.PositiveSyntaxTest11) )
                return new SyntaxTest(testName, results, item) ;
            if ( testType.equals(TestManifestX.PositiveSyntaxTestARQ) )
                return new SyntaxTest(testName, results, item) ;

            // == Bad
            if ( testType.equals(TestManifest.NegativeSyntaxTest) )
                return new SyntaxTest(testName, results, item, false) ;
            if ( testType.equals(TestManifest_11.NegativeSyntaxTest11) )
                return new SyntaxTest(testName, results, item, false) ;
            if ( testType.equals(TestManifestX.NegativeSyntaxTestARQ) )
                return new SyntaxTest(testName, results, item, false) ;
            
            // ---- Update tests
            if ( testType.equals(TestManifest_11.PositiveUpdateSyntaxTest11) )
                return new SyntaxUpdateTest(testName, results, item, true) ;
            if ( testType.equals(TestManifest_11.NegativeUpdateSyntaxTest11) )
                return new SyntaxUpdateTest(testName, results, item, false) ;

            // Two names for same thing.
            // Note item is not passed down.
            if ( testType.equals(TestManifestUpdate_11.UpdateEvaluationTest) )
                return UpdateTest.create(testName, results, entry, action, result) ;
            if ( testType.equals(TestManifest_11.UpdateEvaluationTest) )
                return UpdateTest.create(testName, results, entry, action, result) ;

            // ----
            
            if ( testType.equals(TestManifestX.TestSerialization) )
                return new TestSerialization(testName, results, item) ;
            
            if ( testType.equals(TestManifest.QueryEvaluationTest)
                || testType.equals(TestManifestX.TestQuery)
                )
                return new QueryTest(testName, results, fileManager, item) ;
            
            // Reduced is funny.
            if ( testType.equals(TestManifest.ReducedCardinalityTest) )
                return new QueryTest(testName, results, fileManager, item) ;
            
            if ( testType.equals(TestManifestX.TestSurpressed) )
                return new SurpressedTest(testName, results, item) ;
            
            System.err.println("Test type '"+testType+"' not recognized") ;
        }
        // Default 
        test = new QueryTest(testName, results, fileManager, item) ;
        return test ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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