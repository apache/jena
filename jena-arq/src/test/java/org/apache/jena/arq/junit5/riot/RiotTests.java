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

package org.apache.jena.arq.junit5.riot;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.arq.junit5.SkipTest;
import org.apache.jena.arq.junit5.SurpressedTest;
import org.apache.jena.arq.junit5.manifest.ManifestEntry;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.TestManifest;

public class RiotTests
{
    public static String assumedRootURIex = "http://example/base/";

//    // Depends on origin of the tests.
//    // Now done by entry.getManifest().getTestBase();
//    public static String x_assumedRootURITurtle = "https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-turtle/";
//    public static String x_assumedRootURITriG = "https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-trig/";

    /** Create a RIOT language test - or return null for "unrecognized" */
    public static Runnable makeRIOTTest(ManifestEntry entry) {
        //Resource manifest = entry.getManifest();
        Node item = entry.getEntry();
        String testName = entry.getName();
        Node action = entry.getAction();
        Node result = entry.getResult();
        Graph graph = entry.getManifest().getGraph();

        String labelPrefix = "[RIOT]";

        try {
            Node testType = entry.getTestType();
            if ( testType == null )
                return null;

            if ( labelPrefix != null )
                testName = labelPrefix+testName;

            // In Turtle tests, the action directly names the file to process.
            Node input = action;
            Node output = result;

            // Some tests assume a certain base URI.

            // == Syntax tests.

            String assumedBase = entry.getManifest().getTestBase();

            // TTL
            if ( equalsType(testType, VocabLangRDF.TestPositiveSyntaxTTL) ) {
                String base = rebase(input, assumedBase);
                return new RiotSyntaxTest(entry, base, RDFLanguages.TURTLE, true);
            }
            if ( equalsType(testType, VocabLangRDF.TestNegativeSyntaxTTL) )
                return new RiotSyntaxTest(entry, RDFLanguages.TURTLE, false);

            // TRIG
            if ( equalsType(testType, VocabLangRDF.TestPositiveSyntaxTriG) ) {
                String base = rebase(input, assumedBase);
                return new RiotSyntaxTest(entry, base, RDFLanguages.TRIG, true);
            }
            if ( equalsType(testType, VocabLangRDF.TestNegativeSyntaxTriG) )
                return new RiotSyntaxTest(entry, RDFLanguages.TRIG, false);

            // NT
            if ( equalsType(testType, VocabLangRDF.TestPositiveSyntaxNT) )
                return new RiotSyntaxTest(entry, RDFLanguages.NTRIPLES, true);
            if ( equalsType(testType, VocabLangRDF.TestNegativeSyntaxNT) )
                return new RiotSyntaxTest(entry, RDFLanguages.NTRIPLES, false);

            // NQ
            if ( equalsType(testType, VocabLangRDF.TestPositiveSyntaxNQ) )
                return new RiotSyntaxTest(entry, RDFLanguages.NQUADS, true);
            if ( equalsType(testType, VocabLangRDF.TestNegativeSyntaxNQ) )
                return new RiotSyntaxTest(entry, RDFLanguages.NQUADS, false);

            // RDF/XML - W3C test suite
            // This suite has eval tests (positive and warning - they have "warn" in the filename) and negative syntax tests.
            if ( equalsType(testType, VocabLangRDF.TestPositiveRDFXML) ) {
                String fn = entry.getAction().getURI();
                // Assumes the tests are stored in "rdf-xml" or "rdf11-xml"
                String base = fn.replaceAll("^.*/rdf(\\d\\d)?-xml/", "https://w3c.github.io/rdf-tests/rdf/rdf11/rdf-xml/");
                return new RiotEvalTest(entry, base, RDFLanguages.RDFXML, true);
            }
            if ( equalsType(testType, VocabLangRDF.TestNegativeRDFXML) )
                return new RiotSyntaxTest(entry, RDFLanguages.RDFXML, false);

            // Other
            if ( equalsType(testType, VocabLangRDF.TestPositiveSyntaxRJ) )
                return new RiotSyntaxTest(entry, RDFLanguages.RDFJSON, true);
            if ( equalsType(testType, VocabLangRDF.TestNegativeSyntaxRJ) )
                return new RiotSyntaxTest(entry, RDFLanguages.RDFJSON, false);

            if ( equalsType(testType, VocabLangRDF.TestSurpressed ))
                return new SurpressedTest(entry);

            // == Eval tests

            if ( equalsType(testType, VocabLangRDF.TestEvalTTL) ) {
                String base = rebase(input, assumedBase);
                return new RiotEvalTest(entry, base, RDFLanguages.TURTLE, true);
            }
            if ( equalsType(testType, VocabLangRDF.TestNegativeEvalTTL) ) {
                String base = rebase(input, assumedBase);
                return new RiotEvalTest(entry, base, RDFLanguages.TURTLE, false);
            }

            if ( equalsType(testType, VocabLangRDF.TestEvalTriG) ) {
                String base = rebase(input, assumedBase);
                return new RiotEvalTest(entry, base, RDFLanguages.TRIG, true);
            }
            if ( equalsType(testType, VocabLangRDF.TestNegativeEvalTriG) ) {
                String base = rebase(input, assumedBase);
                return new RiotEvalTest(entry, base, RDFLanguages.TRIG, false);
            }

            if ( equalsType(testType, VocabLangRDF.TestEvalNT) ) {
                String base = entry.getAction().getURI();//rebase(input, assumedRootURI);
                return new RiotEvalTest(entry, base, RDFLanguages.NTRIPLES, true);
            }
            if ( equalsType(testType, VocabLangRDF.TestNegativeEvalNT) ) {
                String base = entry.getAction().getURI();//rebase(input, assumedRootURI);
                return new RiotEvalTest(entry, base, RDFLanguages.NTRIPLES, false);
            }

            if ( equalsType(testType, VocabLangRDF.TestEvalRJ) ) {
                String base = rebase(input, assumedRootURIex);
                return new RiotEvalTest(entry, base, RDFLanguages.RDFJSON, true);
            }
//            if ( equalsType(testType, VocabLangRDF.TestNegativeEvalRJ) ) {
//                String base = rebase(input, assumedRootURIex);
//                return new RiotEvalTest(entry, base, RDFLanguages.RDFJSON, false);
//            }

            // == Not supported : Entailment tests.

            String NSX = TestManifest.NS;
            if ( Objects.equals(testType.getURI(), NSX+"PositiveEntailmentTest") )
                return new SkipTest(entry);
            if ( Objects.equals(testType.getURI(), NSX+"NegativeEntailmentTest") )
                return new SkipTest(entry);
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static boolean equalsType(Node typeNode, Resource typeResource) {
        return typeNode.equals(typeResource.asNode());
    }

    private static String rebase(Node input, String baseIRI) {
        if ( input.isBlank() )
            return baseIRI;
        String inputURI = input.getURI();
        if ( baseIRI == null )
            return inputURI;
        int splitPoint = SplitIRI.splitpoint(input.getURI());
        if ( splitPoint < 0 )
            return inputURI;

        String x = SplitIRI.localname(inputURI) ;
        baseIRI = baseIRI+x;
        return baseIRI;
    }

    static Set<String> allowWarningSet = new HashSet<>();
    static {
        // example:
        //allowWarningSet.add("#turtle-eval-bad-01");
    }

    /**
     * Tune tests for warnings. Normally, tests runs are warning sensitive.
     */
    // Some tests have U+FFFD which, in Jena, generates a helpful warning.
    // Some tests have <http:g> which RIOT warns about but passes.

    /*package*/ static boolean allowWarnings(ManifestEntry testEntry) {
        if ( equalsType(testEntry.getTestType(), VocabLangRDF.TestPositiveRDFXML) ) {
            // RDF/XML
            // Various warnings in eval tests.

            String name = testEntry.getName();

            if ( name.equals("datatypes-test002") )
                return true;

            if ( name.equals("rdfms-empty-property-elements-test016") )
                // Processing instruction warning.
                return true;

            if ( name.equals("rdfms-rdf-names-use-test-015") )
                //rdf:_1 is being used on a typed node.
                return true;

            if ( name.startsWith("rdfms-rdf-names-use-warn-") )
                // "is not a recognized RDF property or type."
                // "is not a recognized RDF property."
                return true;

            if ( name.startsWith("unrecognised-xml-attributes-test00") )
                // XML attribute: xml:foo is not known
                return true;

            return false;
        }

        String fragment = fragment(testEntry.getURI());
        if ( fragment == null )
            return false;

        // rdf-tests-cg/sparql11-query/syntax-query/
        // rdf-tests-cg/ntriples/manifest.ttl
        // rdf-tests-cg/nquads/manifest.ttl
        // rdf-tests-cg/turtle/manifest.ttl
        // rdf-tests-cg/trig/manifest.ttl
        // jena-shex
        if ( fragment.endsWith("UTF8_boundaries") || fragment.endsWith("character_boundaries") )
            // Boundaries of the Unicode allowed character blocks.
            return true;
        // rdf-tests Turtle and Trig
        if ( fragment.contains("IRI-resolution") )
            return true;
        return false;
    }

    /*package*/ static String fragment(String uri) {
        if ( uri == null )
            return null;
        int j = uri.lastIndexOf('#');
        String frag = (j >= 0) ? uri.substring(j) : uri;
        return frag;
    }

}
