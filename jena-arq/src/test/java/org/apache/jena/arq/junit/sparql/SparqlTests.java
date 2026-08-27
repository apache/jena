/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.arq.junit.sparql;

import static org.apache.jena.arq.junit.Scripts.entryContainsSubstring;

import org.apache.jena.arq.junit.OmittedTest;
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

        Syntax sparqlSyntax = defaultForSyntaxTests;

        if ( sparqlSyntax != null ) {
            if ( ! sparqlSyntax.equals(Syntax.syntaxARQ) &&
                 ! sparqlSyntax.equals(Syntax.syntaxSPARQL_10) &&
                 ! sparqlSyntax.equals(Syntax.syntaxSPARQL_11) &&
                 ! sparqlSyntax.equals(Syntax.syntaxSPARQL_12) )
                throw new TestSetupException("Unknown syntax: "+sparqlSyntax);
        }

        Node testType = entry.getTestType();
        SparqlSpecVersion specVersion = SparqlSpecVersion.get(entry.getSpecVersion());

        if ( testType == null ) {
            System.err.println("No type: "+entry.getManifest().getFileName()+ (entry.getName()==null?"": " : "+entry.getName()) );

            testType = TestManifest.QueryEvaluationTest.asNode();
            if ( specVersion == null ) {
                specVersion = SparqlSpecVersion.ARQ;
            }
        }

        return adaptDispatch(entry, testType, specVersion, sparqlSyntax);
    }

    // Allows us to isolate the places where deprecation warnings occur.
    @SuppressWarnings("removal")
    private static Runnable adaptDispatch(ManifestEntry entry, Node testType , SparqlSpecVersion specVersion, Syntax sparqlSyntax) {

        // Legacy test type conversion.
        if ( equalsType(testType, TestManifest_11.PositiveSyntaxTest11) ) {
            testType = TestManifest.PositiveSyntaxTest.asNode();
            specVersion = SparqlSpecVersion.SPARQL_11;
        }
        else if ( equalsType(testType, TestManifest_11.NegativeSyntaxTest11) ) {
            testType = TestManifest.NegativeSyntaxTest.asNode();
            specVersion = SparqlSpecVersion.SPARQL_11;
        }
        else if ( equalsType(testType, TestManifest_12.PositiveSyntaxTest12) ) {
            testType = TestManifest.PositiveSyntaxTest.asNode();
            specVersion = SparqlSpecVersion.SPARQL_12;
        }
        else if ( equalsType(testType, TestManifest_12.NegativeSyntaxTest12) ) {
            testType = TestManifest.NegativeSyntaxTest.asNode();
            specVersion = SparqlSpecVersion.SPARQL_12;
        }
        else if ( equalsType(testType, TestManifest_11.PositiveUpdateSyntaxTest11) ) {
            testType = TestManifest.PositiveUpdateSyntaxTest.asNode();
            specVersion = SparqlSpecVersion.SPARQL_11;
        }
        else if ( equalsType(testType, TestManifest_11.NegativeUpdateSyntaxTest11) ) {
            testType = TestManifest.NegativeUpdateSyntaxTest.asNode();
            specVersion = SparqlSpecVersion.SPARQL_11;
        }

        // ARQ specific tests.
        else if ( equalsType(testType, TestManifestX.PositiveSyntaxTestARQ) ) {
            // Just Do It
            return new QuerySyntaxTest(entry, Syntax.syntaxARQ, true);
//            testType = TestManifest.PositiveSyntaxTest.asNode();
//            specVersion = SparqlSpecVersion.ARQ;
//            sparqlSyntax = Syntax.syntaxARQ;

        }
        else if ( equalsType(testType, TestManifestX.NegativeSyntaxTestARQ) ) {
            return new QuerySyntaxTest(entry, Syntax.syntaxARQ, false);
//            testType = TestManifest.NegativeSyntaxTest.asNode();
//            specVersion = SparqlSpecVersion.ARQ;
//            sparqlSyntax = Syntax.syntaxARQ;
        }
        else if ( equalsType(testType, TestManifestX.PositiveUpdateSyntaxTestARQ) ) {
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, true);
//            testType = TestManifest.PositiveUpdateSyntaxTest.asNode();
//            specVersion = SparqlSpecVersion.ARQ;
//            sparqlSyntax = Syntax.syntaxARQ;
        }
        else if ( equalsType(testType, TestManifestX.NegativeUpdateSyntaxTestARQ) ) {
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, false);
//            testType = TestManifest.NegativeUpdateSyntaxTest.asNode();
//            specVersion = SparqlSpecVersion.ARQ;
//            sparqlSyntax = Syntax.syntaxARQ;
        }

        if ( specVersion == null ) {
            if ( Scripts.entryContainsSubstring(entry, "testing/ARQ") )
                specVersion = SparqlSpecVersion.ARQ;
            else if ( Scripts.entryContainsSubstring(entry, "testing/SPARQL-CDTs") )
                specVersion = SparqlSpecVersion.ARQ;
        }

        if ( specVersion != null ) { //&& sparqlSyntax == null ) {
            switch (specVersion) {
                case SPARQL_10 -> sparqlSyntax = Syntax.syntaxSPARQL_10;
                case SPARQL_11 -> sparqlSyntax = Syntax.syntaxSPARQL_11;
                case SPARQL_12 -> sparqlSyntax = Syntax.syntaxSPARQL_12;
                case ARQ -> sparqlSyntax = Syntax.syntaxARQ;
            }
        }

        // --

        // Now dispatch on testType, with setting by syntax.
        // Split so the adaptions can be isolated.

        return dispatch(entry, testType, sparqlSyntax);
    }

    private static Runnable dispatch(ManifestEntry entry, Node testType , Syntax sparqlSyntax) {
        // ---- Query syntax tests
        if ( equalsType(testType, TestManifest.PositiveSyntaxTest) )
            return new QuerySyntaxTest(entry, sparqlSyntax, true);

        // == Bad
        if ( equalsType(testType, TestManifest.NegativeSyntaxTest) ) {
            return new QuerySyntaxTest(entry, sparqlSyntax, false);
        }
        if ( equalsType(testType, TestManifestX.NegativeSyntaxTestARQ) )
            return new QuerySyntaxTest(entry, Syntax.syntaxARQ, false);

        // ---- Query Evaluation Tests
        if ( equalsType(testType, TestManifest.QueryEvaluationTest) ) {
            // Locally not supported.
            // Omitted tests.
            // Two BNODE in the SELECT
            if ( entryContainsSubstring(entry, "functions/manifest#bnode01") )
                return new OmittedTest(entry);
            // Jena issue?
            if ( entryContainsSubstring(entry, "property-path/manifest#values_and_path") )
                return new OmittedTest(entry);

            // Query evaluation tests.
            return new QueryEvalTest(entry, sparqlSyntax);
        }
        if ( equalsType(testType, TestManifestX.TestQuery) )
            return new QueryEvalTest(entry, sparqlSyntax);

        // ---- Update syntax tests
        if ( equalsType(testType, TestManifestX.PositiveUpdateSyntaxTestARQ) )
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, true);

        if ( equalsType(testType, TestManifestX.NegativeUpdateSyntaxTestARQ) )
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, false);

        if ( equalsType(testType, TestManifest.PositiveUpdateSyntaxTest) )
            return new UpdateSyntaxTest(entry, sparqlSyntax, true);

        if ( equalsType(testType, TestManifest.NegativeUpdateSyntaxTest) )
            return new UpdateSyntaxTest(entry, sparqlSyntax, false);

        // ---- Update evaluation tests
        if ( equalsType(testType, TestManifest.UpdateEvaluationTest) )
            return new UpdateEvalTest(entry, sparqlSyntax);

        // ---- Other

        if ( equalsType(testType, TestManifestX.TestSerialization) )
            return new SerializationTest(entry);
        // Reduced is funny.
        if ( equalsType(testType, TestManifest.ReducedCardinalityTest) )
            return new QueryEvalTest(entry, sparqlSyntax);
        if ( equalsType(testType, TestManifestX.TestSurpressed) )
            return new SurpressedTest(entry);
        if ( equalsType(testType, TestManifestX.CSVResultFormatTest) ) {
            Log.warn("Tests", "Skip CSV query for test: "+entry.getName());
            return new OmittedTest(entry);
        }
        return null;
    }

    private static boolean equalsType(Node typeNode, Resource typeResource) {
        return typeNode.equals(typeResource.asNode());
    }

    /** Make tests, execution only, any syntax version */
    static public Runnable makeSPARQLTestExecOnly(ManifestEntry entry, Creator<Dataset> maker) {
        Node testType = entry.getTestType();
        if ( testType == null )
            testType = TestManifest.QueryEvaluationTest.asNode();

        if ( testType != null ) {
            // -- Query Evaluation Tests
            if ( equalsType(testType, TestManifest.QueryEvaluationTest) )
                return new QueryEvalTest(entry, null, maker);
            if ( equalsType(testType, TestManifestX.TestQuery) )
                return new QueryEvalTest(entry, null, maker);
            if ( equalsType(testType, TestManifestX.TestSurpressed) )
                return new SurpressedTest(entry);
        }
        return null;
    }
}
