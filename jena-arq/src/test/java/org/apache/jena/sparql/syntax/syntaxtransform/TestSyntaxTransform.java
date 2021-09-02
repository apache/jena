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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Test;

/** Test of variable replaced by value */
public class TestSyntaxTransform
{
    @Test public void subst_query_01() {
        testQuery("SELECT * { }",
                  "SELECT * {}",
                  "o", "1"); }

    @Test public void subst_query_02() {
        testQuery("SELECT ?x { }",
                  "SELECT ?x {}",
                  "o", "1"); }

    @Test public void subst_query_03() {
        testQuery("SELECT ?o { }",
                  "SELECT (1 as ?o) {}",
                  "o", "1"); }

    @Test public void subst_query_04() {
        testQuery("SELECT (?o AS ?z) { }",
                  "SELECT (1 AS ?z) {}",
                  "o", "1"); }

    @Test public void subst_query_05() {
        testQuery("SELECT (?o+2 AS ?z) { }",
                  "SELECT (1+2 AS ?z) {}",
                  "o", "1");
    }

    @Test public void subst_query_09() {
        testQuery("SELECT * {?s ?p ?o}",
                  "SELECT * {?s ?p 1}",
                  "o", "1");
    }

    @Test public void subst_query_10() {
        testQuery("SELECT * { SELECT ?o {} }",
                  "SELECT * { SELECT (1 as ?o) {}}",
                  "o", "1");
    }

    @Test public void subst_query_11() {
        testQuery("SELECT * { ?s ?p ?o { SELECT ?x { ?x ?p ?o } } }",
                  "SELECT * { ?s ?p 1  { SELECT ?x { ?x ?p 1 } } }",
                  "o", "1"); }

    @Test public void subst_query_20() {
        testQuery("SELECT * { ?s ?p ?g GRAPH ?g { ?s ?p ?g } }",
                  "SELECT * { ?s ?p <urn:ex:graph> GRAPH <urn:ex:graph> { ?s ?p <urn:ex:graph> } }",
                  "g", "<urn:ex:graph>"); }

    @Test public void subst_query_21() {
        testQuery("SELECT * { ?s ?p ?srv SERVICE ?srv { ?s ?p ?srv}}",
                  "SELECT * { ?s ?p <urn:ex:service> SERVICE <urn:ex:service> { ?s ?p <urn:ex:service>}}",
                  "srv", "<urn:ex:service>"); }

    @Test public void subst_query_30() {
        testQuery("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }",
                  "CONSTRUCT { ?s ?p 1 } WHERE { ?s ?p 1 } ",
                  "o", "1"); }

    @Test public void subst_query_31() {
        testQuery("CONSTRUCT { GRAPH ?g { ?s ?p ?g } } WHERE { ?s ?p ?g }",
                  "CONSTRUCT { GRAPH <urn:x:g> { ?s ?p <urn:x:g>} } WHERE { ?s ?p <urn:x:g> }",
                  "g", "<urn:x:g>"); }

    @Test public void subst_query_40() {
        testQuery("DESCRIBE ?o ?x",
                  "DESCRIBE ?x <urn:x:obj>",
                  "o", "<urn:x:obj>"); }

    @Test public void subst_query_41() {
        testQuery("DESCRIBE ?o ?x WHERE { ?s ?p ?o } ",
                  "DESCRIBE ?x <urn:x:obj> WHERE { ?s ?p <urn:x:obj> } ",
                  "o", "<urn:x:obj>"); }

    @Test public void subst_query_50() {
        testQuery("ASK { ?s ?p ?o } ",
                  "ASK { ?s ?p <urn:x:obj> } ",
                  "o", "<urn:x:obj>"); }

    @Test public void subst_query_model_1() {
        testQuery("SELECT * { ?s ?p ?o } ORDER BY ?s",
                  "SELECT * { <urn:ex:z> ?p ?o } ORDER BY (<urn:ex:z>)",
                  "s", "<urn:ex:z>");
    }

    // Same except use the Model API.
    @Test public void subst_query_model_2() {
        testQueryModel("SELECT * { ?s ?p ?o } ORDER BY ?s",
                       "SELECT * { <urn:ex:z> ?p ?o } ORDER BY (<urn:ex:z>)",
                       "s", "<urn:ex:z>");
    }

    @Test public void subst_update_01() {
        testUpdate("DELETE { ?s <urn:ex:p> ?x } WHERE {}",
                   "DELETE { ?s <urn:ex:p> <urn:ex:z> } WHERE {}",
                   "x", "<urn:ex:z>");
    }

    @Test public void subst_update_02() {
        testUpdate("DELETE { ?s <urn:ex:p> ?x } WHERE { ?s <urn:ex:p> ?x }",
                   "DELETE { ?s <urn:ex:p> <urn:ex:z> } WHERE { ?s <urn:ex:p> <urn:ex:z> }",
                   "x", "<urn:ex:z>");
    }

    @Test public void subst_update_03() {
        testUpdate("DELETE { ?s <urn:ex:p> ?x } INSERT { ?s <urn:ex:p> ?x } WHERE { ?s <urn:ex:p> ?x }",
                   "DELETE { ?s <urn:ex:p> <urn:ex:z> } INSERT { ?s <urn:ex:p> <urn:ex:z> } WHERE { ?s <urn:ex:p> <urn:ex:z> }",
                   "x", "<urn:ex:z>");
    }

    @Test public void subst_update_09() {
        testUpdate("DELETE WHERE { ?s <urn:ex:p> ?x }",
                   "DELETE WHERE { ?s <urn:ex:p> <urn:ex:z> }",
                   "x", "<urn:ex:z>");
    }

    @Test public void subst_update_10() {
        testUpdateModel("DELETE WHERE { ?s <urn:ex:p> ?x }",
                        "DELETE WHERE { ?s <urn:ex:p> <urn:ex:z> }",
                           "x", "<urn:ex:z>");
    }

    //static final String PREFIX = "PREFIX : <http://example/>\n";
    static final String PREFIX = "";

    private void testQuery(String input, String output, String varStr, String valStr)
    {
        Query q1 = QueryFactory.create(PREFIX+input);
        Query qExpected = QueryFactory.create(PREFIX+output);

        Map<Var, Node> map = new HashMap<>();
        map.put(Var.alloc(varStr), SSE.parseNode(valStr));

        Query qTrans = QueryTransformOps.transform(q1, map);

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

        Map<String, RDFNode> map = new HashMap<>();
        Node n = SSE.parseNode(valStr);
        RDFNode x = ModelUtils.convertGraphNodeToRDFNode(n);
        map.put(varStr, x);

        Query qTrans = QueryTransformOps.transformQuery(q1, map);
        assertEquals(qExpected, qTrans);
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

