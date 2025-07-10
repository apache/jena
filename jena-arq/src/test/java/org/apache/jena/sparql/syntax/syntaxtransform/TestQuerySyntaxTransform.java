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

package org.apache.jena.sparql.syntax.syntaxtransform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/** Test of variable replaced by value */
public class TestQuerySyntaxTransform
{
    @Test public void transformTransformReplace_01() {
        testQuery("SELECT * { }",
                  "SELECT * {}",
                  "o", "1"); }

    @Test public void transformTransformReplace_02() {
        testQuery("SELECT ?x { }",
                  "SELECT ?x {}",
                  "o", "1"); }

    @Test public void transformTransformReplace_03() {
        testQuery("SELECT ?o { }",
                  "SELECT (1 as ?o) {}",
                  "o", "1"); }

    @Test public void transformTransformReplace_04() {
        testQuery("SELECT (?o AS ?z) { }",
                  "SELECT (1 AS ?z) {}",
                  "o", "1"); }

    @Test public void transformTransformReplace_05() {
        testQuery("SELECT (?o+2 AS ?z) { }",
                  "SELECT (1+2 AS ?z) {}",
                  "o", "1");
    }

    @Test public void transformTransformReplace_09() {
        testQuery("SELECT * {?s ?p ?o}",
                  "SELECT * {?s ?p 1}",
                  "o", "1");
    }

    @Test public void transformTransformReplace_10() {
        testQuery("SELECT * { SELECT ?o {} }",
                  "SELECT * { SELECT (1 as ?o) {}}",
                  "o", "1");
    }

    @Test public void transformTransformReplace_11() {
        testQuery("SELECT * { ?s ?p ?o { SELECT ?x { ?x ?p ?o } } }",
                  "SELECT * { ?s ?p 1  { SELECT ?x { ?x ?p 1 } } }",
                  "o", "1"); }

    @Test public void transformTransformReplace_20() {
        testQuery("SELECT * { ?s ?p ?g GRAPH ?g { ?s ?p ?g } }",
                  "SELECT * { ?s ?p <urn:ex:graph> GRAPH <urn:ex:graph> { ?s ?p <urn:ex:graph> } }",
                  "g", "<urn:ex:graph>"); }

    @Test public void transformTransformReplace_21() {
        testQuery("SELECT * { ?s ?p ?srv SERVICE ?srv { ?s ?p ?srv}}",
                  "SELECT * { ?s ?p <urn:ex:service> SERVICE <urn:ex:service> { ?s ?p <urn:ex:service>}}",
                  "srv", "<urn:ex:service>"); }

    @Test public void transformTransformReplace_30() {
        testQuery("SELECT * { ?s ?p ?o } ORDER BY ?s",
                  "SELECT * { <urn:ex:z> ?p ?o } ORDER BY (<urn:ex:z>)",
                "s", "<urn:ex:z>");
    }

    // GH-2650
    @Test public void transformTransformReplace_31() {
        testQuery("PREFIX : <http://example/> SELECT (SUM(?a + ?b) AS ?c) WHERE { ?s :p ?a }",
                  "PREFIX : <http://example/> SELECT (SUM(123 + ?b) AS ?c) WHERE { ?s :p 123 }",
                  "a", "123");
    }

    // GH-2650
    @Test public void transformTransformReplace_32() {
        testQuery("PREFIX : <http://example/> SELECT (SUM(?a + ?b) AS ?c) WHERE { }",
                  "PREFIX : <http://example/> SELECT (SUM(123 + ?b) AS ?c) WHERE { }",
                  "a", "123");
    }

    // GH-2650
    @Test public void transformTransformReplace_33() {
        testQuery("SELECT * WHERE { ?s ?p ?o { SELECT (count(?a) as ?C) WHERE {} } }",
                  "SELECT * WHERE { ?s ?p ?o { SELECT (count(123) as ?C) WHERE {} } }",
                  "a", "123");
    }

    // Same except use the Model API.
    @Test public void transformTransformReplace_model_2() {
        testQueryModel("SELECT * { ?s ?p ?o } ORDER BY ?s",
                       "SELECT * { <urn:ex:z> ?p ?o } ORDER BY (<urn:ex:z>)",
                       "s", "<urn:ex:z>");
    }

    @Test public void transformTransformReplace_40() {
        testQueryModel("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }",
                       "CONSTRUCT { <urn:ex:z> ?p ?o } WHERE { <urn:ex:z> ?p ?o }",
                       "s", "<urn:ex:z>");
    }

    @Test public void transformTransformReplace_41() {
        testQueryModel("CONSTRUCTWHERE { ?s ?p ?o }",
                       "CONSTRUCT { <urn:ex:z> ?p ?o } WHERE { <urn:ex:z> ?p ?o }",
                       "s", "<urn:ex:z>");
    }

    @Test public void transformTransformReplace_42() {
        testQueryModel("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }",
                       "CONSTRUCT { <urn:ex:z> ?p 57 } WHERE { <urn:ex:z> ?p 57 }",
                       "s", "<urn:ex:z>", "o", "57");
    }

    @Test public void transformTransformReplace_43() {
        testQueryModel("CONSTRUCT { GRAPH ?g {?s ?p ?o } } WHERE { GRAPH ?g {?s ?p ?o } }",
                       "CONSTRUCT { GRAPH <urn:ex:g> { <urn:ex:z> ?p ?o } } WHERE { GRAPH <urn:ex:g> { <urn:ex:z> ?p ?o } }",
                       "s", "<urn:ex:z>", "g", "<urn:ex:g>");
    }

    @Test public void transformTransformReplace_44() {
        testQueryModel("CONSTRUCTWHERE { GRAPH ?g {?s ?p ?o } }",
                       "CONSTRUCT { GRAPH <urn:ex:g> { <urn:ex:z> ?p ?o } } WHERE { GRAPH <urn:ex:g> { <urn:ex:z> ?p ?o } }",
                       "s", "<urn:ex:z>", "g", "<urn:ex:g>");
    }

    @Test public void transformSubstituteupdate_01() {
        testUpdate("DELETE { ?s <urn:ex:p> ?x } WHERE {}",
                   "DELETE { ?s <urn:ex:p> <urn:ex:z> } WHERE {}",
                   "x", "<urn:ex:z>");
    }

    @Test public void transformSubstituteupdate_02() {
        testUpdate("DELETE { ?s <urn:ex:p> ?x } WHERE { ?s <urn:ex:p> ?x }",
                   "DELETE { ?s <urn:ex:p> <urn:ex:z> } WHERE { ?s <urn:ex:p> <urn:ex:z> }",
                   "x", "<urn:ex:z>");
    }

    @Test public void transformSubstituteupdate_03() {
        testUpdate("DELETE { ?s <urn:ex:p> ?x } INSERT { ?s <urn:ex:p> ?x } WHERE { ?s <urn:ex:p> ?x }",
                   "DELETE { ?s <urn:ex:p> <urn:ex:z> } INSERT { ?s <urn:ex:p> <urn:ex:z> } WHERE { ?s <urn:ex:p> <urn:ex:z> }",
                   "x", "<urn:ex:z>");
    }

    @Test public void transformSubstituteupdate_09() {
        testUpdate("DELETE WHERE { ?s <urn:ex:p> ?x }",
                   "DELETE WHERE { ?s <urn:ex:p> <urn:ex:z> }",
                   "x", "<urn:ex:z>");
    }

    @Test public void transformSubstituteupdate_10() {
        testUpdateModel("DELETE WHERE { ?s <urn:ex:p> ?x }",
                        "DELETE WHERE { ?s <urn:ex:p> <urn:ex:z> }",
                           "x", "<urn:ex:z>");
    }

    @Test public void transformTransformJsonReplace_01() {
        testQuery("JSON { 'key': ?x } {  }",
                  "JSON { 'key': 123 } { }",
                  "x", "123");
    }

    @Test public void transformTransformJsonReplace_02() {
        testQuery("PREFIX : <http://example/> JSON { \"o\": ?o } { ?s :p ?o }",
                  "PREFIX : <http://example/> JSON { \"o\": 123 } { ?s :p 123}",
                  "o", "123");
    }

    @Test public void transformTransformJsonReplace_03() {
        String queryString = """
                JSON { "s" : ?s ,
                       "x" : ?x ,
                       "unused": ?unused ,
                       "fixed" : 'fixed' } WHERE {}
              """;
        String expectedString = """
                JSON { "s" : ?s ,
                       "x" : "abc" ,
                       "unused": ?unused ,
                       "fixed" : 'fixed' } WHERE {}
                """;
        testQuery(queryString, expectedString, "x", "'abc'");
    }

    //static final String PREFIX = "PREFIX : <http://example/>\n";
    static final String PREFIX = "";

    private void testQuery(String input, String output, String varStr, String valStr) {
        Query q1 = QueryFactory.create(PREFIX+input);
        Query qExpected = QueryFactory.create(PREFIX+output);

        Map<Var, Node> map = new HashMap<>();
        map.put(Var.alloc(varStr), SSE.parseNode(valStr));

        Query qTrans = QueryTransformOps.replaceVars(q1, map);

        if ( ! qExpected.equals(qTrans) ) {
            System.out.println(qExpected.getProject());
            System.out.print(qExpected);
            System.out.println(qTrans.getProject());
            System.out.print(qTrans);
        }

        assertEquals(qExpected, qTrans);
    }

    private void testQueryModel(String input, String output, String varStr, String valStr) {
        Query q1 = QueryFactory.create(PREFIX+input);
        Query qExpected = QueryFactory.create(PREFIX+output);

        Map<String, RDFNode> map = Map.of(varStr, fromString(valStr));
        Query qTrans = QueryTransformOps.queryReplaceVars(q1, map);
        assertEquals(qExpected, qTrans);
    }

    private void testQueryModel(String input, String output, String varStr1, String valStr1, String varStr2, String valStr2) {
        Query q1 = QueryFactory.create(PREFIX+input);
        Query qExpected = QueryFactory.create(PREFIX+output);

        Map<String, RDFNode> map = Map.of(varStr1, fromString(valStr1), varStr2, fromString(valStr2));

        Query qTrans = QueryTransformOps.queryReplaceVars(q1, map);
        assertEquals(qExpected, qTrans);

    }

    private RDFNode fromString(String valStr) {
        Node n = SSE.parseNode(valStr);
        RDFNode x = ModelUtils.convertGraphNodeToRDFNode(n);
        return x;
    }

    private void testUpdate(String input, String output, String varStr, String valStr) {
        UpdateRequest req1 = UpdateFactory.create(PREFIX+input);
        UpdateRequest reqExpected = UpdateFactory.create(PREFIX+output);

        Map<Var, Node> map = new HashMap<>();
        map.put(Var.alloc(varStr), SSE.parseNode(valStr));

        UpdateRequest reqTrans = UpdateTransformOps.transform(req1, map);

        // Crude.
        String x1 = reqExpected.toString().replaceAll("[ \n\t]", "");
        String x2 = reqTrans.toString().replaceAll("[ \n\t]", "");
        //assertEquals(reqExpected, reqTrans);
        assertEquals(x1, x2);
    }

    private void testUpdateModel(String input, String output, String varStr, String valStr) {
        UpdateRequest req1 = UpdateFactory.create(PREFIX+input);
        UpdateRequest reqExpected = UpdateFactory.create(PREFIX+output);

        Map<String, RDFNode> map = new HashMap<>();
        Node n = SSE.parseNode(valStr);
        RDFNode x = ModelUtils.convertGraphNodeToRDFNode(n);
        map.put(varStr, x);

        UpdateRequest reqTrans = UpdateTransformOps.transformUpdate(req1, map);

        // Crude.
        String x1 = reqExpected.toString().replaceAll("[ \n\t]", "");
        String x2 = reqTrans.toString().replaceAll("[ \n\t]", "");
        //assertEquals(reqExpected, reqTrans);
        assertEquals(x1, x2);
    }
}

