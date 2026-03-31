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

package org.apache.jena.query.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.text.ShaclIndexMapping.FieldDef;
import org.apache.jena.query.text.ShaclIndexMapping.FieldType;
import org.apache.jena.query.text.ShaclIndexMapping.IndexProfile;
import org.apache.jena.query.text.assembler.ShaclIndexAssembler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestShaclLucQueryRawValueOnMultiValuedField {

    private static final String NS = "http://example.org/";
    private static final String FIELD_IRI_PREFIX = "urn:jena:lucene:field#";
    private static final Node RECORD_CLASS = NodeFactory.createURI(NS + "Record");
    private static final Node IDENTIFIER_PRED = NodeFactory.createURI(NS + "identifier");

    private Dataset dataset;

    @Before
    public void setUp() {
        TextQuery.init();

        FieldDef identifierField = new FieldDef("identifier", FieldType.KEYWORD, null,
            true, true, false, false, true, false,
            Collections.singleton(IDENTIFIER_PRED));

        IndexProfile recordProfile = new IndexProfile(
            NodeFactory.createURI(NS + "RecordShape"),
            Collections.singleton(RECORD_CLASS),
            "uri", "docType",
            Arrays.asList(identifierField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(recordProfile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setValueStored(true);

        ShaclTextIndexLucene textIndex = new ShaclTextIndexLucene(new ByteBuffersDirectory(), config);
        Dataset baseDs = DatasetFactory.create();
        ShaclTextDocProducer producer = new ShaclTextDocProducer(
            baseDs.asDatasetGraph(), textIndex, mapping);
        dataset = TextDatasetFactory.create(baseDs, textIndex, true, producer);

        loadTestData();
    }

    private void loadTestData() {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource record = ResourceFactory.createResource(NS + "record1");
            model.add(record, RDF.type, ResourceFactory.createResource(NS + "Record"));
            model.add(record, ResourceFactory.createProperty(NS + "identifier"), "");
            model.add(record, ResourceFactory.createProperty(NS + "identifier"), "94130");
            model.add(record, ResourceFactory.createProperty(NS + "identifier"), "DAG2011/00113216");
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    @After
    public void tearDown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    @Test
    public void testLucQueryReturnsMatchedRawValueForMultiValuedField() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s ?matchRaw WHERE {\n" +
            "  (?s ?score ?matchRaw) luc:query (\"" + FIELD_IRI_PREFIX + "identifier\" \"94130\" 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
            ResultSet rs = qe.execSelect();
            assertTrue("Should return a result for identifier 94130", rs.hasNext());

            QuerySolution sol = rs.next();
            assertEquals(NS + "record1", sol.getResource("s").getURI());
            assertEquals("94130", sol.getLiteral("matchRaw").getString());
            assertTrue("Should return a single matching entity", !rs.hasNext());
        } finally {
            dataset.end();
        }
    }
}
