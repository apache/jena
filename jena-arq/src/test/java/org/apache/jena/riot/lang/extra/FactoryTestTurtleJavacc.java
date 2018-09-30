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

package org.apache.jena.riot.lang.extra ;

import junit.framework.Test ;
import junit.framework.TestSuite ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.langsuite.*;
import org.apache.jena.sparql.junit.EarlReport ;
import org.apache.jena.util.junit.TestFactoryManifest ;
import org.apache.jena.util.junit.TestUtils ;
import org.apache.jena.vocabulary.RDF ;

public class FactoryTestTurtleJavacc extends TestFactoryManifest
{
    public static String assumedRootURIex = "http://example/base/" ;
    public static String assumedRootURITurtle = "http://www.w3.org/2013/TurtleTests/" ;
    public static String assumedRootURITriG = "http://www.w3.org/2013/TriGTests/" ;
    
    public static EarlReport report = null ;
    
    public static TestSuite make(String manifest, Resource dftTestType, String labelPrefix)
    {
        return new FactoryTestTurtleJavacc(dftTestType, labelPrefix).process(manifest) ;
    }

    private Resource dftTestType ;
    private String labelPrefix ;

    public FactoryTestTurtleJavacc(Resource dftTestType, String labelPrefix)
    {
        this.dftTestType = dftTestType ;
        this.labelPrefix = labelPrefix ;
    }
    
    @Override
    public Test makeTest(Resource manifest, Resource item, String testName, Resource action, Resource result)
    {
        Lang lang = TurtleJavaccReaderRIOT.lang; 
        
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
                return new UnitTestSyntax(testName, item.getURI(), input.getURI(), lang, report) ;
            
            if ( r.equals(VocabLangRDF.TestNegativeSyntaxTTL) )
                return new UnitTestBadSyntax(testName, item.getURI(), input.getURI(), lang, report) ;

            // Eval.
            
            if ( r.equals(VocabLangRDF.TestEvalTTL) ) {
                String base = rebase(input, assumedRootURITurtle) ;
                return new UnitTestEval(testName, item.getURI(), input.getURI(), result.getURI(), base, lang, report) ;
            }
            if ( r.equals(VocabLangRDF.TestNegativeEvalTTL) )
                return new UnitTestBadEval(testName, item.getURI(), input.getURI(), lang, report) ;

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
        baseIRI = baseIRI+x ;
        return baseIRI ;
    }
}
