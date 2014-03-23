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

package com.hp.hpl.jena.sparql.junit;

import junit.framework.Test ;
import junit.framework.TestCase ;
import junit.framework.TestSuite ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestUpdate_11 ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest_11 ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.junit.TestFactoryManifest ;
import com.hp.hpl.jena.util.junit.TestUtils ;


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
        QueryTest t = new QueryTest(item.getName(), null, item) ;
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
        
        TestItem item = TestItem.create(entry, TestManifest.QueryEvaluationTest) ;
        Resource testType = item.getTestType() ;
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
                return new QueryTest(testName, results, item) ;
            
            // Reduced is funny.
            if ( testType.equals(TestManifest.ReducedCardinalityTest) )
                return new QueryTest(testName, results, item) ;
            
            if ( testType.equals(TestManifestX.TestSurpressed) )
                return new SurpressedTest(testName, results, item) ;
            
            if ( testType.equals(TestManifest_11.CSVResultFormatTest) )
            {
                Log.warn("Tests", "Skip CSV test: "+testName) ;
                return null ;
            }
            
            System.err.println("Test type '"+testType+"' not recognized") ;
        }
        // Default 
        test = new QueryTest(testName, results, item) ;
        return test ;
    }
}
