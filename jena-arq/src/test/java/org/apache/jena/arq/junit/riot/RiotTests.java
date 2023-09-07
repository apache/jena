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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.arq.junit.LibTestSetup;
import org.apache.jena.arq.junit.SkipTest;
import org.apache.jena.arq.junit.SurpressedTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.TestManifest;

public class RiotTests
{
    public static String assumedRootURIex = "http://example/base/" ;

    // Depends on origin of the tests.
    public static String xassumedRootURITurtle = "https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-turtle/";
    public static String xassumedRootURITriG = "https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-trig/";

//    public static String assumedRootURITurtle = "http://www.w3.org/2013/TurtleTests/" ;
//    public static String assumedRootURITriG = "http://www.w3.org/2013/TriGTests/" ;

    /** Create a RIOT language test - or return null for "unrecognized" */
    public static Runnable makeRIOTTest(ManifestEntry entry) {
        //Resource manifest = entry.getManifest();
        Resource item = entry.getEntry();
        String testName = entry.getName();
        Resource action = entry.getAction();
        Resource result = entry.getResult();

        String labelPrefix = "[RIOT]";

        try
        {
            Resource testType = LibTestSetup.getResource(item, RDF.type) ;
            if ( testType == null )
                throw new RiotException("Can't determine the test type") ;

            if ( labelPrefix != null )
                testName = labelPrefix+testName ;

            // In Turtle tests, the action directly names the file to process.
            Resource input = action ;
            Resource output = result ;

            // Some tests assume a certain base URI.

            // == Syntax tests.

            String assumedBase = entry.getManifest().getTestBase();

            // TTL
            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxTTL) ) {
                String base = rebase(input, assumedBase);
                return new RiotSyntaxTest(entry, base, RDFLanguages.TURTLE, true) ;
            }
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxTTL) )
                return new RiotSyntaxTest(entry, RDFLanguages.TURTLE, false) ;

            // TRIG
            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxTriG) ) {
                    String base = rebase(input, assumedBase);
                    return new RiotSyntaxTest(entry, base, RDFLanguages.TRIG, true) ;
                }
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxTriG) )
                return new RiotSyntaxTest(entry, RDFLanguages.TRIG, false) ;

            // NT
            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxNT) )
                return new RiotSyntaxTest(entry, RDFLanguages.NTRIPLES, true) ;
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxNT) )
                return new RiotSyntaxTest(entry, RDFLanguages.NTRIPLES, false) ;

            // NQ
            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxNQ) )
                return new RiotSyntaxTest(entry, RDFLanguages.NQUADS, true) ;
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxNQ) )
                return new RiotSyntaxTest(entry, RDFLanguages.NQUADS, false) ;

            // RDF/XML - W3C test suite
            // This suite has eval tests (positive and warning - they have "warn" in the filename) and negative syntax tests.
            if ( testType.equals(VocabLangRDF.TestPositiveRDFXML) ) {
                String fn = entry.getAction().getURI();
                // Assumes the tests are stored in "rdf-xml" or "rdf11-xml"
                String base = fn.replaceAll("^.*/rdf(\\d\\d)?-xml/", "https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-xml/");
                return new RiotEvalTest(entry, base, RDFLanguages.RDFXML, true) ;
            }
            if ( testType.equals(VocabLangRDF.TestNegativeRDFXML) )
                return new RiotSyntaxTest(entry, RDFLanguages.RDFXML, false) ;

            // Other
            if ( testType.equals(VocabLangRDF.TestPositiveSyntaxRJ) )
                return new RiotSyntaxTest(entry, RDFLanguages.RDFJSON, true) ;
            if ( testType.equals(VocabLangRDF.TestNegativeSyntaxRJ) )
                return new RiotSyntaxTest(entry, RDFLanguages.RDFJSON, false) ;

            if ( testType.equals(VocabLangRDF.TestSurpressed ))
                return new SurpressedTest(entry) ;

            // == Eval tests

            if ( testType.equals(VocabLangRDF.TestEvalTTL) ) {
                String base = rebase(input, assumedBase);
                return new RiotEvalTest(entry, base, RDFLanguages.TURTLE, true);
            }
            if ( testType.equals(VocabLangRDF.TestNegativeEvalTTL) ) {
                String base = rebase(input, assumedBase) ;
                return new RiotEvalTest(entry, base, RDFLanguages.TURTLE, false) ;
            }

            if ( testType.equals(VocabLangRDF.TestEvalTriG) ) {
                String base = rebase(input, assumedBase) ;
                return new RiotEvalTest(entry, base, RDFLanguages.TRIG, true) ;
            }
            if ( testType.equals(VocabLangRDF.TestNegativeEvalTriG) ) {
                String base = rebase(input, assumedBase) ;
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
//                return new RiotEvalTest(entry, base, RDFLanguages.RDFJSON, false);
//            }

            // == Not supported : Entailment tests.

            String NSX = TestManifest.NS;
            if ( Objects.equals(testType.getURI(), NSX+"PositiveEntailmentTest") )
                return new SkipTest(entry);
            if ( Objects.equals(testType.getURI(), NSX+"NegativeEntailmentTest") )
                return new SkipTest(entry);
            return null;
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

    static Set<String> allowWarningSet = new HashSet<>();
    static {
        // example:
        //allowWarningSet.add("#turtle-eval-bad-01");
    }

    /** Tune tests for warnings. */
    // Some tests have U+FFFD which, in Jena, generates a helpful warning.
    // Some tests have <http:g> which RIOT warns about but passes.
    /*package*/ static boolean allowWarnings(ManifestEntry testEntry) {
        String fragment = fragment(testEntry.getURI());
        if ( fragment == null )
            return false;
        if ( fragment.endsWith("UTF8_boundaries") || fragment.endsWith("character_boundaries") )
            // Boundaries of the Unicode allowed character blocks.
            return true;
        if ( fragment.contains("IRI-resolution") )
            return true;
        return false;
    }

    /*package*/ static String fragment(String uri) {
        if ( uri == null )
            return null;
        int j = uri.lastIndexOf('#') ;
        String frag = (j >= 0) ? uri.substring(j) : uri ;
        return frag ;
    }

}
