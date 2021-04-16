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

package org.apache.jena.rdfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfs.assembler.VocabRDFS;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

public class TestAssemblerRDFS {

    static { JenaSystem.init(); }

    private static final String DIR = "testing/RDFS";

    private static final String PREFIXES = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
             ,"PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
             ,"PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>"
             ,"PREFIX sh:      <http://www.w3.org/ns/shacl#>"
             ,"PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>"
             ,"");

    @Test public void assemble_dataset() {
        // Reference <assem-data.trig> and <assem-vocab.ttl>
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"/assembler-rdfs.ttl", VocabRDFS.tDatasetRDFS);
        assertSparqlAsk(ds, "{ :z rdf:type :P }");
        assertSparqlCount(ds, "{ :z ?p ?o }", 2);
    }

    @Test public void assemble_model() {
        // Reference <assem-data.ttl> and <assem-vocab.ttl>
        Model model = (Model)AssemblerUtils.build(DIR+"/assembler-rdfs.ttl", VocabRDFS.tGraphRDFS);
        Dataset ds = DatasetFactory.wrap(model);
        assertSparqlAsk(ds, "{ :z rdf:type :P }");
        assertSparqlCount(ds, "{ :z ?p ?o }", 2);
    }

    // --------------------

    // ASK
    private void assertSparqlAsk(Dataset dataset, String pattern) {
        String queryString = "ASK "+pattern;
        String qs = PREFIXES+queryString;
        Query query = QueryFactory.create(qs);
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ) {
            assertTrue(pattern+" -- ", qExec.execAsk());
        }
    }

    // Count rows.
    private void assertSparqlCount(Dataset dataset, String pattern, int expected) {
        String queryString = "SELECT (count(*) AS ?C) "+pattern;
        String qs = PREFIXES+queryString;
        Query query = QueryFactory.create(qs);

        try ( QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ) {
            ResultSet rs = qExec.execSelect();
            assertTrue("Result is zero rows", rs.hasNext());
            Binding binding = rs.nextBinding();
            assertTrue("Result not one row", ! rs.hasNext());
            Node n = binding.get(Var.alloc("C"));
            int actual = NodeValue.makeNode(n).getInteger().intValue();
            assertEquals("count "+pattern+" -- ", expected, actual);
        }
    }
}
