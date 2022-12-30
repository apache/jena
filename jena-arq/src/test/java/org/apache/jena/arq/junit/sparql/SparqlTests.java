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

import org.apache.jena.arq.junit.SurpressedTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.sparql.tests.*;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.junit.QueryTestException;
import org.apache.jena.sparql.vocabulary.TestManifest;
import org.apache.jena.sparql.vocabulary.TestManifestUpdate_11;
import org.apache.jena.sparql.vocabulary.TestManifestX;
import org.apache.jena.sparql.vocabulary.TestManifest_11;

public class SparqlTests {

    // test suite default setting.
    // See also "rdftests --arq"
    public static Syntax defaultSyntaxForTests = Syntax.syntaxARQ;
    public static Syntax defaultSyntaxForSyntaxTests = Syntax.syntaxSPARQL_11;

    /** Create a SPARQL test (syntax or valuation) test - or return null for "unrecognized" */
    public static Runnable makeSPARQLTest(ManifestEntry entry) {
        if ( entry.getAction() == null ) {
            System.out.println("Null action: " + entry);
            return null;
        }

        Syntax querySyntax = defaultSyntaxForSyntaxTests;

        // Syntax to use for tests where the file extension .rq/.ru applies.
        // For SPARQL*/RDF*, use ARQ syntax so we can run the RDF-star community tests.
        Syntax querySyntax11 = defaultSyntaxForTests;
        Syntax updateSyntax11 = defaultSyntaxForTests;

        if ( querySyntax != null ) {
            if ( ! querySyntax.equals(Syntax.syntaxARQ) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_10) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_11) )
                throw new QueryTestException("Unknown syntax: "+querySyntax);
        }

        Resource testType = entry.getTestType();
        if ( testType == null )
            testType = TestManifest.QueryEvaluationTest;

        // == Good syntax
        if ( testType.equals(TestManifest.PositiveSyntaxTest) )
            return new QuerySyntaxTest(entry, querySyntax, true);
        if ( testType.equals(TestManifest_11.PositiveSyntaxTest11) )
            return new QuerySyntaxTest(entry, querySyntax11, true);
        if ( testType.equals(TestManifestX.PositiveSyntaxTestARQ) )
            return new QuerySyntaxTest(entry, Syntax.syntaxARQ, true);

        // == Bad
        if ( testType.equals(TestManifest.NegativeSyntaxTest) )
            return new QuerySyntaxTest(entry, querySyntax, false);
        if ( testType.equals(TestManifest_11.NegativeSyntaxTest11) ) {
            // Special override
            Syntax syn = querySyntax11;
            // Some of these are things that ARQ deals with but aren't SPARQL 1.1 so force SPARQL 1.1
            if ( entry.getAction().getURI().contains("/Syntax-SPARQL_11/syn-bad-") )
                syn = Syntax.syntaxSPARQL_11;
            return new QuerySyntaxTest(entry, syn, false);
        }

        if ( testType.equals(TestManifestX.NegativeSyntaxTestARQ) )
            return new QuerySyntaxTest(entry, Syntax.syntaxARQ, false);

        // ---- Update tests
        if ( testType.equals(TestManifest_11.PositiveUpdateSyntaxTest11) )
            return new UpdateSyntaxTest(entry, updateSyntax11, true);
        if ( testType.equals(TestManifestX.PositiveUpdateSyntaxTestARQ) )
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, true);

        if ( testType.equals(TestManifest_11.NegativeUpdateSyntaxTest11) )
            return new UpdateSyntaxTest(entry, querySyntax11, false);
        if ( testType.equals(TestManifestX.NegativeUpdateSyntaxTestARQ) )
            return new UpdateSyntaxTest(entry, Syntax.syntaxARQ, false);

        //---- Query Evaluation Tests
        if ( testType.equals(TestManifest.QueryEvaluationTest) )
            return new QueryExecTest(entry);
        if ( testType.equals(TestManifestX.TestQuery) )
            return new QueryExecTest(entry);

        // ---- Update Evaluation tests
        if ( testType.equals(TestManifestUpdate_11.UpdateEvaluationTest) )
            return new UpdateExecTest(entry);
        if ( testType.equals(TestManifest_11.UpdateEvaluationTest) )
            return new UpdateExecTest(entry);

        // ---- Other

        if ( testType.equals(TestManifestX.TestSerialization) )
            return new SerializationTest(entry);

        // Reduced is funny.
        if ( testType.equals(TestManifest.ReducedCardinalityTest) )
            return new QueryExecTest(entry);

        if ( testType.equals(TestManifestX.TestSurpressed) )
            return new SurpressedTest(entry);

        if ( testType.equals(TestManifest_11.CSVResultFormatTest) )
        {
            Log.warn("Tests", "Skip CSV test: "+entry.getName());
            return null;
        }

        return null;
    }

    /** Make tests, execution only */
    static public Runnable makeSPARQLTestExecOnly(ManifestEntry entry, Creator<Dataset> maker) {
        Resource testType = entry.getTestType();
        if ( testType == null )
            testType = TestManifest.QueryEvaluationTest;

        if ( testType != null ) {
            // -- Query Evaluation Tests
            if ( testType.equals(TestManifest.QueryEvaluationTest) )
                return new QueryExecTest(entry, maker);
            if ( testType.equals(TestManifestX.TestQuery) )
                return new QueryExecTest(entry, maker);

//            // -- Update Evaluation tests
//            if ( testType.equals(TestManifestUpdate_11.UpdateEvaluationTest) )
//                return new UpdateExecTest(entry, maker);
//            if ( testType.equals(TestManifest_11.UpdateEvaluationTest) )
//                return new UpdateExecTest(entry, maker);
//            if ( testType.equals(TestManifestX.TestSurpressed) )
//                return new SurpressedTest(entry);
        }
        return null;
    }
}
