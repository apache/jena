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

package org.apache.jena.ontapi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.jena.ontapi.common.OntPersonalities;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.ReasonerVocabulary;

/**
 * The acceptance reasoner tests.
 * Modified copy-paste from jena-core-tests (org.apache.jena.reasoner.test.ManualExample)
 */
public class JenaReasonerTest {

    /**
     * Illustrate different ways of finding a reasoner
     */
    @Test
    public void testFinding() {
        String NS = "urn:example:";

        // Build a trivial example data set
        OntModel example = OntModelFactory.createModel();
        OntDataProperty p = example.createDataProperty(NS + "p");
        OntDataProperty q = example.createDataProperty(NS + "q");
        p.addSuperProperty(q);
        example.createIndividual(NS + "a").addProperty(p, "foo");
        example.setNsPrefixes(OntModelFactory.STANDARD);

        Resource config = OntModelFactory.createDefaultModel()
                .createResource()
                .addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "simple");
        Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(config);

        InfModel inf = OntModelFactory.createModel(
                example.getBaseGraph(),
                OntPersonalities.OWL2_ONT_PERSONALITY().build(), reasoner
        ).asInferenceModel();

        Resource a = inf.getResource(NS + "a");
        Statement s = a.getProperty(q);
        Assertions.assertNotNull(s, "Null statement");
    }

    @Test
    public void testValidation1() {
        validationTest("/dttest1.nt", false);
    }

    @Test
    public void testValidation2() {
        validationTest("/dttest2.nt", false);
    }

    @Test
    public void testValidation3() {
        validationTest("/dttest3.nt", true);
    }

    private void validationTest(String resource, boolean result) {
        OntModel data = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel(resource, Lang.NTRIPLES).getGraph(),
                OntPersonalities.OWL2_ONT_PERSONALITY().build(), ReasonerRegistry.getRDFSReasoner()
        );
        InfModel inf = data.asInferenceModel();
        ValidityReport validity = inf.validate();
        List<ValidityReport.Report> reports = new ArrayList<>();
        if (!validity.isValid()) {
            for (Iterator<ValidityReport.Report> i = validity.getReports(); i.hasNext(); ) {
                ValidityReport.Report report = i.next();
                reports.add(report);
            }
        }
        Assertions.assertEquals(result, validity.isValid(), "Conflicts: " + reports);
    }

    /**
     * Illustrate generic rules and derivation tracing
     */
    @Test
    public void testDerivation() {
        // Test data
        String egNS = PrintUtil.egNS;
        Model rawData = OntModelFactory.createDefaultModel();
        Property p = rawData.createProperty(egNS, "p");
        Resource A = rawData.createResource(egNS + "A");
        Resource B = rawData.createResource(egNS + "B");
        Resource C = rawData.createResource(egNS + "C");
        Resource D = rawData.createResource(egNS + "D");
        A.addProperty(p, B);
        B.addProperty(p, C);
        C.addProperty(p, D);

        // Rule example
        String rules = "[rule1: (?a eg:p ?b) (?b eg:p ?c) -> (?a eg:p ?c)]";
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        InfModel inf = OntModelFactory.createModel(
                rawData.getGraph(),
                OntPersonalities.OWL2_ONT_PERSONALITY().build(), reasoner
        ).asInferenceModel();

        List<Statement> statements = inf.listStatements(A, p, D).toList();
        Assertions.assertEquals(1, statements.size());
        StringWriter res = new StringWriter();
        PrintWriter out = new PrintWriter(res, true);
        Iterator<Derivation> id = inf.getDerivation(statements.get(0));
        while (id.hasNext()) {
            id.next().printTrace(out, true);
        }
        String expected = """
                Rule rule1 concluded (eg:A eg:p eg:D) <-
                    Rule rule1 concluded (eg:A eg:p eg:C) <-
                        Fact (eg:A eg:p eg:B)
                        Fact (eg:B eg:p eg:C)
                    Fact (eg:C eg:p eg:D)
                """;
        Assertions.assertEquals(expected, res.toString().replace("\r", ""));
    }

    /**
     * Another generic rules illustration
     */
    @Test
    public void testGenericRules() {
        // Test data
        String egNS = PrintUtil.egNS;
        Model rawData = OntModelFactory.createDefaultModel();
        Property first = rawData.createProperty(egNS, "concatFirst");
        Property second = rawData.createProperty(egNS, "concatSecond");
        Property p = rawData.createProperty(egNS, "p");
        Property q = rawData.createProperty(egNS, "q");
        Property r = rawData.createProperty(egNS, "r");
        Resource A = rawData.createResource(egNS + "A");
        Resource B = rawData.createResource(egNS + "B");
        Resource C = rawData.createResource(egNS + "C");
        A.addProperty(p, B);
        B.addProperty(q, C);
        r.addProperty(first, p);
        r.addProperty(second, q);

        String data = RDFIOTestUtils.asString(rawData, Lang.TURTLE);

        // Rule example for
        String rules = "[r1: (?c eg:concatFirst ?p), (?c eg:concatSecond ?q) -> [r1b: (?x ?c ?y) <- (?x ?p ?z) (?z ?q ?y)]]";
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        InfModel inf = OntModelFactory.createModel(
                rawData.getGraph(),
                OntPersonalities.OWL2_ONT_PERSONALITY().build(), reasoner
        ).asInferenceModel();
        Assertions.assertTrue(inf.contains(A, p, B));
        Assertions.assertTrue(inf.contains(A, r, C));

        Assertions.assertEquals(data, RDFIOTestUtils.asString(rawData, Lang.TURTLE), "Data has been changed");
    }

}
