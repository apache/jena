/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DataFormat;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.junit.TestFactoryManifest;
import com.hp.hpl.jena.util.junit.TestUtils;

public class QueryTestSuiteFactory extends TestFactoryManifest
{
    private FileManager fileManager = FileManager.get() ;
    // Set (and retrieve) externally.
    public static EarlReport results = null ;

    /** Make a test suite from a manifest file */
    static public TestSuite make(String filename) 
    {
        QueryTestSuiteFactory tFact = new QueryTestSuiteFactory() ;
        return tFact.process(filename) ;
    }

    /** Make a single test */
    static public TestSuite make(String query, String data, String result)
    {
        TestItem item = new TestItem(query, query, data, result) ;
        QueryTest t = new QueryTest(item.getName(), null, FileManager.get(), item) ;
        TestSuite ts = new TestSuite() ;
        ts.setName(TestUtils.safeName(query)) ;
        ts.addTest(t) ;
        return ts ;
    }
    
    public Test makeTest(Resource manifest, Resource entry, String testName, Resource action, Resource result)
    {
        // Defaults.
        Syntax querySyntax = TestQueryUtils.getQuerySyntax(manifest)  ;
        
        if ( querySyntax != null )
        {
            if ( ! querySyntax.equals(Syntax.syntaxRDQL) &&
            ! querySyntax.equals(Syntax.syntaxARQ) &&
            ! querySyntax.equals(Syntax.syntaxSPARQL) )
                throw new QueryTestException("Unknown syntax: "+querySyntax) ;
        }
        
        // May be null
        Resource defaultTestType = TestUtils.getResource(manifest, TestManifestX.defaultTestType) ;
        // test name
        // test type
        // action -> query specific query[+data]
        // results
        
        TestItem item = new TestItem(entry, defaultTestType, querySyntax, DataFormat.langXML) ;
        
        TestCase test = null ;
        
        if ( item.getTestType() != null )
        {
            // Good syntax
            if ( item.getTestType().equals(TestManifest.PositiveSyntaxTest)
                 || item.getTestType().equals(TestManifestX.TestSyntax) )
                test = new SyntaxTest(testName, results, item) ;
            
            // Bad syntax
            if ( item.getTestType().equals(TestManifest.NegativeSyntaxTest) 
                || item.getTestType().equals(TestManifestX.TestBadSyntax) )
                test = new SyntaxTest(testName, results, item, false) ;
            
            if ( item.getTestType().equals(TestManifestX.TestSerialization) )
                test = new SerializerTest(testName, results, item) ;
            
            if ( item.getTestType().equals(TestManifest.QueryEvaluationTest)
                || item.getTestType().equals(TestManifestX.TestQuery) )
                test = new QueryTest(testName, results, fileManager, item) ;
            
            if ( item.getTestType().equals(TestManifestX.TestSurpressed) )
                test = new SurpressedTest(testName, results, item) ;
            
            if ( test == null )
                System.err.println("Test type '"+item.getTestType()+"' not recognized") ;
        }
        // Default 
        if ( test == null )
            test = new QueryTest(testName, results, fileManager, item) ;
        return test ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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