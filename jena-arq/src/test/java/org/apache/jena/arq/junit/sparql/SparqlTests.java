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

package org.apache.jena.arq.junit.sparql;

import static org.apache.jena.arq.junit.Scripts.entryContainsSubstring;

import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.arq.junit.SurpressedTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.manifest.TestSetupException;
import org.apache.jena.arq.junit.sparql.tests.*;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.TestManifestUpdate_11;
import org.apache.jena.sparql.vocabulary.TestManifestX;
import org.apache.jena.sparql.vocabulary.TestManifest_11;
import org.apache.jena.sparql.vocabulary.TestManifest_12;
import org.apache.jena.vocabulary.TestManifest;

public class SparqlTests {

    // test suite default setting.

    // Command "rdftests" sets this and does not rely on the default.
    // See also "rdftests --arq"
    public static Syntax defaultForSyntaxTests = Syntax.syntaxSPARQL_12;

    /** Create a SPARQL test (syntax or valuation) test - or return null for "unrecognized" */
    public static Runnable makeSPARQLTest(ManifestEntry entry) {
        if ( entry.getAction() == null ) {
            System.out.println("Null action: " + entry);
            return null;
        }

        Syntax querySyntax = defaultForSyntaxTests;

        // Syntax to use for tests where the file extension .rq/.ru applies.
        Syntax querySyntax11 = querySyntax;
        Syntax updateSyntax11 = querySyntax;
        Syntax querySyntax12 = querySyntax;

        if ( querySyntax != null ) {
            if ( ! querySyntax.equals(Syntax.syntaxARQ) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_10) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_11) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_12) )
                throw new TestSetupException("Unknown syntax: "+querySyntax);
        }

        Node testType = entry.getTestType();
        if ( testType == null )
            testType = TestManifest.QueryEvaluationTest.asNode();

        // ---- Query syntax tests
        if ( equalsType(testType, TestManifest.PositiveSyntaxTest) ) {
            if ( entryContainsSubstring(entry, "codepoint-escapes#codepoint-esc-07") )
                // \U escape outside a string or URI.
                return null;
            return new QuerySyntaxTest(entry, querySyntax, true);
        }
        if ( equalsType(testType, TestManifest_11.PositiveSyntaxTest11) )
            return new QuerySyntaxTest(entry, querySyntax11, true);
        if ( equalsType(testType, TestManifest_12.PositiveSyntaxTest12) )
            return new QuerySyntaxTest(entry, querySyntax12, true);
        if ( equalsType(testType, TestManifestX.PositiveSyntaxTestARQ) ) {
            return new QuerySyntaxTest(entry, Syntax.syntaxARQ, true);
        }

        //"codepoint-escapes#codepoint-esc-bad-07"

        // == Bad
        if ( equalsType(testType, TestManifest.NegativeSyntaxTest) ) {
            if ( entryContainsSubstring(entry, "codepoint-escapes#codepoint-esc-bad-03") )
                // \U escape outside a string or URI.
                return null;
            return new QuerySyntaxTest(entry, querySyntax, false);
        }
        if ( equalsType(testType, TestManifest_11.NegativeSyntaxTest11) ) {
            // Special override
            Syntax syn = querySyntax11;
            // Some of these are things that ARQ deals with but aren't SPARQL 1.1 so force SPARQL 1.1
            if ( Scripts.entryContainsSubstring(entry, "/Syntax-SPARQL_11/syn-bad-") )
                syn = Syntax.syntaxSPARQL_11;
            return new QuerySyntaxTest(entry, syn, false);
        }

        if ( equalsType(testType, TestManifestX.NegativeSyntaxTestARQ) )
            return new QuerySyntaxTest(entry, Syntax.syntaxARQ, false);

        // ---- Query Evaluation Tests
        if ( equalsType(testType, TestManifest.QueryEvaluationTest) ) {

            // ??
            if ( entryContainsSubstring(entry, "aggregates/manifest#agg-groupconcat-04") ) {
                return null;
            }

            // ??
            if ( entryContainsSubstring(entry, "functions/manifest#bnode01") ) {
                return null;
            }
            // ??
            if ( entryContainsSubstring(entry, "property-path/manifest#values_and_path") ) {
                return null;
            }
            return new QueryEvalTest(entry);
        }
        if ( equalsType(testType, TestManifestX.TestQuery) )
            return new QueryEvalTest(entry);

        // ---- Update syntax tests
        if ( equalsType(testType, TestManifest_11.PositiveUpdateSyntaxTest11) )
            return new UpdateSyntaxTest(entry, updateSyntax11, true);
        if ( equalsType(testType, TestManifestX.PositiveUpdateSyntaxTestARQ) )
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, true);

        if ( equalsType(testType, TestManifest_11.NegativeUpdateSyntaxTest11) )
            return new UpdateSyntaxTest(entry, querySyntax11, false);
        if ( equalsType(testType, TestManifestX.NegativeUpdateSyntaxTestARQ) )
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, false);

        if ( equalsType(testType, TestManifest_12.PositiveUpdateSyntaxTest) )
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, true);
        if ( equalsType(testType, TestManifest_12.NegativeUpdateSyntaxTest) )
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, false);

        // ---- Update evaluation tests
        if ( equalsType(testType, TestManifestUpdate_11.UpdateEvaluationTest) )
            return new UpdateEvalTest(entry);
        if ( equalsType(testType, TestManifest_11.UpdateEvaluationTest) )
            return new UpdateEvalTest(entry);

        // ---- Other

        if ( equalsType(testType, TestManifestX.TestSerialization) )
            return new SerializationTest(entry);

        // Reduced is funny.
        if ( equalsType(testType, TestManifest.ReducedCardinalityTest) )
            return new QueryEvalTest(entry);

        if ( equalsType(testType, TestManifestX.TestSurpressed) )
            return new SurpressedTest(entry);

        if ( equalsType(testType, TestManifest_11.CSVResultFormatTest) ) {
            Log.warn("Tests", "Skip CSV test: "+entry.getName());
            return null;
        }

        return null;
    }

    private static boolean equalsType(Node typeNode, Resource typeResource) {
        return typeNode.equals(typeResource.asNode());
    }

    /** Make tests, execution only */
    static public Runnable makeSPARQLTestExecOnly(ManifestEntry entry, Creator<Dataset> maker) {
        Node testType = entry.getTestType();
        if ( testType == null )
            testType = TestManifest.QueryEvaluationTest.asNode();

        if ( testType != null ) {
            // -- Query Evaluation Tests
            if ( equalsType(testType, TestManifest.QueryEvaluationTest) )
                return new QueryEvalTest(entry, maker);
            if ( equalsType(testType, TestManifestX.TestQuery) )
                return new QueryEvalTest(entry, maker);

//            // -- Update Evaluation tests
//            if ( equalsType(testType, TestManifestUpdate_11.UpdateEvaluationTest) )
//                return new UpdateExecTest(entry, maker);
//            if ( equalsType(testType, TestManifest_11.UpdateEvaluationTest) )
//                return new UpdateExecTest(entry, maker);
//            if ( equalsType(testType, TestManifestX.TestSurpressed) )
//                return new SurpressedTest(entry);
        }
        return null;
    }
}
