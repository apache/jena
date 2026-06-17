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

package org.apache.jena.query.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.query.text.TextIndexLucene;
import org.apache.jena.query.text.TextQuery;
import org.apache.jena.query.vector.VectorDatasetFactory;
import org.apache.jena.query.vector.VectorIndex;
import org.apache.jena.query.vector.VectorIndexLucene;
import org.apache.jena.query.vector.VectorQuery;
import org.apache.jena.query.vector.VectorSimilarity;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestHybridSearchEndToEnd {
    private Dataset dataset;

    @BeforeEach
    public void before() {
        TextQuery.init();
        VectorQuery.init();
        HybridQuery.init();

        EntityDefinition entityDefinition = new EntityDefinition("uri", "label");
        entityDefinition.set("label", ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label").asNode());
        TextIndexConfig textConfig = new TextIndexConfig(entityDefinition);
        TextIndex textIndex = new TextIndexLucene(new ByteBuffersDirectory(), textConfig);

        VectorIndex vectorIndex = new VectorIndexLucene(new ByteBuffersDirectory(), new TestEmbeddingProvider(),
                NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"), 3, VectorSimilarity.COSINE);

        Dataset base = DatasetFactory.createTxnMem();
        Dataset textDataset = TextDatasetFactory.create(base, textIndex, true);
        dataset = VectorDatasetFactory.create(textDataset, vectorIndex, true);
    }

    @AfterEach
    public void after() {
        if (dataset != null)
            dataset.close();
    }

    @Test
    public void hybridQueryReturnsRrfRankedResults() {
        dataset.begin(ReadWrite.WRITE);
        dataset.getDefaultModel().read(new StringReader(StrUtils.strjoinNL(
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX ex: <http://example/>",
                "ex:item1 rdfs:label \"feline semantic animal\" .",
                "ex:item2 rdfs:label \"feline car vehicle\" .",
                "ex:item3 rdfs:label \"database sparql rdf\" ."
        )), "", "TURTLE");
        dataset.commit();
        dataset.end();

        String query = StrUtils.strjoinNL(
                "PREFIX hybrid: <http://jena.apache.org/hybrid#>",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "SELECT ?s ?score ?tr ?vr ?ts ?vs {",
                "  (?s ?score ?tr ?vr ?ts ?vs) hybrid:query (rdfs:label \"feline animal\" 3 10 60 1.0 1.0) .",
                "}"
        );

        dataset.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecutionFactory.create(query, dataset)) {
            List<QuerySolution> rows = ResultSetFormatter.toList(qExec.execSelect());
            assertEquals("http://example/item1", rows.get(0).getResource("s").getURI());
            assertTrue(rows.get(0).getLiteral("score").getFloat() > rows.get(1).getLiteral("score").getFloat());
            assertTrue(rows.get(0).contains("tr"));
            assertTrue(rows.get(0).contains("vr"));
        } finally {
            dataset.end();
        }
    }
}
