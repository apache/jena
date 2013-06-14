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

package org.apache.jena.riot.langsuite ;

import junit.framework.Test ;
import junit.framework.TestSuite ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;

import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.util.junit.TestFactoryManifest ;
import com.hp.hpl.jena.util.junit.TestUtils ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class FactoryTestRiot extends TestFactoryManifest
{
    public static String assumedRootURIex = "http://example/base/" ;
    public static String assumedRootURITurtle = "http://www.w3.org/2013/TurtleTests/" ;
    public static String assumedRootURITriG = "http://www.w3.org/2013/TriGTests/" ;
    
    public static EarlReport report = null ;
    
    public static TestSuite make(String manifest, Resource dftTestType, String labelPrefix)
    {
        return new FactoryTestRiot(dftTestType, labelPrefix).process(manifest) ;
    }

    private Resource dftTestType ;
    private String labelPrefix ;

    public FactoryTestRiot(Resource dftTestType, String labelPrefix)
    {
        this.dftTestType = dftTestType ;
        this.labelPrefix = labelPrefix ;
    }
    
    @Override
    public Test makeTest(Resource manifest, Resource item, String testName, Resource action, Resource result)
    {
        try
        {
            Resource r = TestUtils.getResource(item, RDF.type) ;
            if ( r == null )
                r = dftTestType ;
            if ( r == null )
                throw new RiotException("Can't determine the test type") ;
            
            if ( labelPrefix != null )
                testName = labelPrefix+testName ;
            
            // In Turtle tests, the action directly names the file to process.
            Resource input = action ;
            Resource output = result ; 
            
            if ( r.equals(VocabLangRDF.TestPositiveSyntaxTTL) )
                return new UnitTestSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.TURTLE, report) ;
            
            if ( r.equals(VocabLangRDF.TestNegativeSyntaxTTL) )
                return new UnitTestBadSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.TURTLE, report) ;

            if ( r.equals(VocabLangRDF.TestPositiveSyntaxTriG) )
                return new UnitTestSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.TRIG, report) ;
            
            if ( r.equals(VocabLangRDF.TestNegativeSyntaxTriG) )
                return new UnitTestBadSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.TRIG, report) ;

            if ( r.equals(VocabLangRDF.TestPositiveSyntaxNT) )
                return new UnitTestSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.NTRIPLES, report) ;
            
            if ( r.equals(VocabLangRDF.TestNegativeSyntaxNT) )
                return new UnitTestBadSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.NTRIPLES, report) ;

            if ( r.equals(VocabLangRDF.TestPositiveSyntaxNQ) )
                return new UnitTestSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.NQUADS, report) ;
            
            if ( r.equals(VocabLangRDF.TestNegativeSyntaxNQ) )
                return new UnitTestBadSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.NQUADS, report) ;

            if ( r.equals(VocabLangRDF.TestPositiveSyntaxRJ) )
                return new UnitTestSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.RDFJSON, report) ;
            
            if ( r.equals(VocabLangRDF.TestNegativeSyntaxRJ) )
                return new UnitTestBadSyntax(testName, item.getURI(), input.getURI(), RDFLanguages.RDFJSON, report) ;
            
            if ( r.equals(VocabLangRDF.TestSurpressed ))
                return new UnitTestSurpressed(testName, item.getURI(), report) ;

            // Eval.
            
            if ( r.equals(VocabLangRDF.TestEvalTTL) ) {
                String base = rebase(input, assumedRootURITurtle) ;
                return new UnitTestEval(testName, item.getURI(), input.getURI(), result.getURI(), base, RDFLanguages.TURTLE, report) ;
            }

            if ( r.equals(VocabLangRDF.TestEvalTriG) ) {
                String base = rebase(input, assumedRootURITriG) ;
                return new UnitTestEval(testName, item.getURI(), input.getURI(), result.getURI(), base, RDFLanguages.TRIG, report) ;
            }

            if ( r.equals(VocabLangRDF.TestNegativeEvalTTL) )
                return new UnitTestBadEval(testName, item.getURI(), input.getURI(), RDFLanguages.TURTLE, report) ;
            
            if ( r.equals(VocabLangRDF.TestNegativeEvalTriG) )
                return new UnitTestBadEval(testName, item.getURI(), input.getURI(), RDFLanguages.TRIG, report) ;

            if ( r.equals(VocabLangRDF.TestEvalNT) )
                return new UnitTestEval(testName, item.getURI(), input.getURI(), result.getURI(), null, RDFLanguages.NTRIPLES, report) ;

            if ( r.equals(VocabLangRDF.TestNegativeEvalNT) )
                return new UnitTestBadEval(testName, item.getURI(), input.getURI(), RDFLanguages.NTRIPLES, report) ;
            
            if ( r.equals(VocabLangRDF.TestEvalRJ) ) {
                String base = rebase(input, assumedRootURIex) ;
                return new UnitTestEval(testName, item.getURI(), input.getURI(), result.getURI(), base, RDFLanguages.RDFJSON, report) ;
            }

            if ( r.equals(VocabLangRDF.TestNegativeEvalRJ) )
                return new UnitTestBadEval(testName, item.getURI(), input.getURI(), RDFLanguages.RDFJSON, report) ;

            System.err.println("Unrecognized turtle test : ("+r+")" + testName) ;
            return null ;

        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            System.err.println("Failed to grok test : " + testName) ;
            return null ;
        }
    }

    private static String rebase(Resource input, String baseIRI) {
        String x = input.getLocalName() ;
        // Yuk, yuk, yuk.
        baseIRI = baseIRI+x ;
        return baseIRI ;
    }
    
}
