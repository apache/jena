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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

/**
 * Try SPARQL with RDFS.
 */
public class TestInfSPARQL {

    private static String PREFIXES = "PREFIX : <http://example/>\n";
    private static Graph vocabDomainSC = SSE.parseGraph("(graph (:p rdfs:domain :R) (:R rdfs:subClassOf :T) )");
    private static Graph dataDomainSC = SSE.parseGraph("(graph (:x :p 123))");

    private static Graph vocabRangeSC = SSE.parseGraph("(graph (:q rdfs:domain :R) (:p rdfs:domain :R0) (:R rdfs:subClassOf :T) )");
//    private static Graph dataRangeSC = SSE.parseGraph("(graph (:x :p 123) (:y :q :z))");

    private static Node node(String str) { return SSE.parseNode(str);}
    private static Quad quad(String str) { return SSE.parseQuad(str);}

    @Test public void sparql1() {
        Graph vocab = vocabDomainSC;
        DatasetGraph dsg0 = DatasetGraphFactory.createTxnMem();
        DatasetGraph dsg = RDFSFactory.datasetRDFS(dsg0, vocab);
        dsg.executeWrite(()->dsg.add(quad("(_ :x :p 123)")));

        String qs = PREFIXES+"SELECT (count(*) AS ?C) { ?s ?p ?o }";
        Query query = QueryFactory.create(qs);

        try ( QueryExecution qExec = QueryExecutionFactory.create(query, dsg) ) {
            ResultSet rs = qExec.execSelect();
            int c = rs.next().getLiteral("C").getInt();
            assertEquals(3, c);
        }
    }

    @Test public void sparql2() {
        Graph vocab = vocabDomainSC;
        DatasetGraph dsg0 = DatasetGraphFactory.createTxnMem();
        DatasetGraph dsg = RDFSFactory.datasetRDFS(dsg0, vocab);
        dsg.executeWrite(()->dsg.add(quad("(:g :x :p 123)")));

        // Default graph is empty.
        {
            String qs1 = PREFIXES+"SELECT (count(*) AS ?C) { ?s ?p ?o }";
            Query query1 = QueryFactory.create(qs1);

            try ( QueryExecution qExec = QueryExecutionFactory.create(query1, dsg) ) {
                ResultSet rs = qExec.execSelect();
                int c = rs.next().getLiteral("C").getInt();
                assertEquals(0, c);
            }
        }
        // Named graph
        {
            String qs2 = PREFIXES+"SELECT (count(*) AS ?C) { GRAPH ?g { ?s ?p ?o } }";
            Query query2 = QueryFactory.create(qs2);

            try ( QueryExecution qExec = QueryExecutionFactory.create(query2, dsg) ) {
                ResultSet rs = qExec.execSelect();
                int c = rs.next().getLiteral("C").getInt();
                assertEquals(3, c);
            }
        }
    }

    @Test public void sparql3() {
        Graph vocab = vocabDomainSC;
        DatasetGraph dsg0 = DatasetGraphFactory.createTxnMem();

        DatasetGraph dsg = RDFSFactory.datasetRDFS(dsg0, vocab);
        dsg.executeWrite(()->{
            dsg.add(quad("(:g1 :x :p 123)"));
            dsg.add(quad("(:g2 :x :p 123)"));
            dsg.add(quad("(:g2 :x :q 'noDR')"));
        });

        // Named graphs, duplicates.
        {
            String qs2 = PREFIXES+"SELECT (count(*) AS ?C) { GRAPH <"+Quad.unionGraph.getURI()+"> { ?s ?p ?o } }";
            Query query2 = QueryFactory.create(qs2);

            try ( QueryExecution qExec = QueryExecutionFactory.create(query2, dsg) ) {
                ResultSet rs = qExec.execSelect();
                int c = rs.next().getLiteral("C").getInt();
                assertEquals(4, c);
            }
        }
    }


}
