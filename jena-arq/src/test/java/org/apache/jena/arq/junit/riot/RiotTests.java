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

package org.apache.jena.arq.junit.riot ;

import org.apache.jena.arq.junit.LibTestSetup;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.vocabulary.RDF ;

public class RiotTests
{
    public static String assumedRootURIex = "http://example/base/" ;
    public static String assumedRootURITurtle = "http://www.w3.org/2013/TurtleTests/" ;
    public static String assumedRootURITriG = "http://www.w3.org/2013/TriGTests/" ;

    public static Runnable makeRIOTTest(ManifestEntry entry) {

        //Resource manifest = entry.getManifest();
        Resource item = entry.getEntry();
        String testName = entry.getName();
        Resource action = entry.getAction();
        Resource result = entry.getResult();

        String labelPrefix = "[RIOT]";
        Resource dftTestType = null;

        try
        {
            Resource testType = LibTestSetup.getResource(item, RDF.type) ;
            if ( testType == null )
                testType = dftTestType ;
            if ( testType == null )
                throw new RiotException("Can't determine the test type") ;

            if ( labelPrefix != null )
                testName = labelPrefix+testName ;

            // In Turtle tests, the action directly names the file to process.
            Resource input = action ;
            Resource output = result ;

            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxTTL) )
                return new RiotSyntaxTest(entry, RDFLanguages.TURTLE, true) ;
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxTTL) )
                return new RiotSyntaxTest(entry, RDFLanguages.TURTLE, false) ;

            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxTriG) )
                return new RiotSyntaxTest(entry, RDFLanguages.TRIG, true) ;
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxTriG) )
                return new RiotSyntaxTest(entry, RDFLanguages.TRIG, false) ;

            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxNT) )
                return new RiotSyntaxTest(entry, RDFLanguages.NTRIPLES, true) ;
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxNT) )
                return new RiotSyntaxTest(entry, RDFLanguages.NTRIPLES, false) ;

            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxNQ) )
                return new RiotSyntaxTest(entry, RDFLanguages.NQUADS, true) ;
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxNQ) )
                return new RiotSyntaxTest(entry, RDFLanguages.NQUADS, false) ;

            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxRJ) )
                return new RiotSyntaxTest(entry, RDFLanguages.RDFJSON, true) ;
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxRJ) )
                return new RiotSyntaxTest(entry, RDFLanguages.RDFJSON, false) ;

            if ( testType.equals(VocabLangRDF.TestSurpressed ))
                return new SurpressedTest() ;

            // Eval.

            if ( testType.equals(VocabLangRDF.TestEvalTTL) ) {
                String base = rebase(input, assumedRootURITurtle);
                return new RiotEvalTest(entry, base, RDFLanguages.TURTLE, true);
            }
            if ( testType.equals(VocabLangRDF.TestNegativeEvalTTL) ) {
                String base = rebase(input, assumedRootURITurtle) ;
                return new RiotEvalTest(entry, base, RDFLanguages.TURTLE, false) ;
            }

            if ( testType.equals(VocabLangRDF.TestEvalTriG) ) {
                String base = rebase(input, assumedRootURITriG) ;
                return new RiotEvalTest(entry, base, RDFLanguages.TRIG, true) ;
            }
            if ( testType.equals(VocabLangRDF.TestNegativeEvalTriG) ) {
                String base = rebase(input, assumedRootURITriG) ;
                return new RiotEvalTest(entry, base, RDFLanguages.TRIG, false) ;
            }

            if ( testType.equals(VocabLangRDF.TestEvalNT) ) {
                String base = entry.getAction().getURI();//rebase(input, assumedRootURI) ;
                return new RiotEvalTest(entry, base, RDFLanguages.NTRIPLES, true) ;
            }
            if ( testType.equals(VocabLangRDF.TestNegativeEvalNT) ) {
                String base = entry.getAction().getURI();//rebase(input, assumedRootURI) ;
                return new RiotEvalTest(entry, base, RDFLanguages.NTRIPLES, false) ;
            }

            if ( testType.equals(VocabLangRDF.TestEvalRJ) ) {
                String base = rebase(input, assumedRootURIex) ;
                return new RiotEvalTest(entry, base, RDFLanguages.RDFJSON, true);
            }
//            if ( testType.equals(VocabLangRDF.TestNegativeEvalRJ) ) {
//                String base = rebase(input, assumedRootURIex) ;
//                return new EvalTest(entry, base, RDFLanguages.RDFJSON, false);
//            }

            System.err.println("Unrecognized test : ("+testType+")" + testName) ;
            return new SurpressedTest() ;

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
