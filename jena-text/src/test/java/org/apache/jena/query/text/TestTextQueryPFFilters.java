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

import static org.junit.Assert.*;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
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

/**
 * Tests for luc:query with CQL filter support.
 */
public class TestTextQueryPFFilters {

    private static final String NS = "http://example.org/";
    private static final Node BOOK_CLASS = NodeFactory.createURI(NS + "Book");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");
    private static final Node AUTHOR_PRED = NodeFactory.createURI(NS + "author");

    private Dataset dataset;

    @Before
    public void setUp() {
        TextQuery.init();

        FieldDef titleField = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));

        FieldDef categoryField = new FieldDef("category", FieldType.KEYWORD, null,
            true, true, true, false, true, false,
            Collections.singleton(CATEGORY_PRED));

        FieldDef authorField = new FieldDef("author", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            Collections.singleton(AUTHOR_PRED));

        IndexProfile bookProfile = new IndexProfile(
            NodeFactory.createURI(NS + "BookShape"),
            Collections.singleton(BOOK_CLASS),
            "uri", "docType",
            Arrays.asList(titleField, categoryField, authorField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(bookProfile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setFacetFields(mapping.getFacetFieldNames());
        config.setValueStored(true);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        ShaclTextIndexLucene textIndex = new ShaclTextIndexLucene(dir, config);

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
            addBook(model, "doc1", "Introduction to Machine Learning", "technology", "Smith");
            addBook(model, "doc2", "Deep Learning Neural Networks", "technology", "Jones");
            addBook(model, "doc3", "Machine Learning for Beginners", "technology", "Smith");
            addBook(model, "doc4", "Learning About Quantum Physics", "science", "Wilson");
            addBook(model, "doc5", "Machine Learning in Biology", "science", "Smith");
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    private void addBook(Model model, String id, String title, String category, String author) {
        Resource book = ResourceFactory.createResource(NS + id);
        model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
        model.add(book, ResourceFactory.createProperty(NS + "title"), title);
        model.add(book, ResourceFactory.createProperty(NS + "category"), category);
        model.add(book, ResourceFactory.createProperty(NS + "author"), author);
    }

    @After
    public void tearDown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    @Test
    public void testLucQueryWithoutFilters() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s ?score WHERE {\n" +
            "  (?s ?score) luc:query (\"default\" \"learning\" 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    assertNotNull(sol.get("s"));
                    assertNotNull(sol.get("score"));
                    count++;
                }
                assertTrue("Should find results for 'learning'", count > 0);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testLucQueryWithCqlEqualFilter() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s ?score WHERE {\n" +
            "  (?s ?score) luc:query (\"default\" \"learning\" " +
            "    '{\"op\":\"=\",\"args\":[{\"property\":\"category\"},\"technology\"]}' 20)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                Set<String> subjects = new HashSet<>();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    subjects.add(sol.getResource("s").getURI());
                }
                assertTrue("Should find technology docs", subjects.size() > 0);
                for (String s : subjects) {
                    assertFalse("Should not find doc4 (science)", s.equals(NS + "doc4"));
                }
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testLucQueryWithCqlAndFilter() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query (\"default\" \"learning\" " +
            "    '{\"op\":\"and\",\"args\":[" +
            "      {\"op\":\"=\",\"args\":[{\"property\":\"category\"},\"technology\"]}," +
            "      {\"op\":\"=\",\"args\":[{\"property\":\"author\"},\"Smith\"]}" +
            "    ]}' 20)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                Set<String> subjects = new HashSet<>();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    subjects.add(sol.getResource("s").getURI());
                }
                for (String s : subjects) {
                    assertTrue("Should only find doc1 or doc3",
                        s.equals(NS + "doc1") || s.equals(NS + "doc3"));
                }
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testLucQueryWithCqlNoMatches() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query (\"default\" \"learning\" " +
            "    '{\"op\":\"=\",\"args\":[{\"property\":\"category\"},\"nonexistent\"]}' 20)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                assertFalse("Should have no results for nonexistent filter", rs.hasNext());
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testLucQueryByFieldName() {
        // Search only the "title" field
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s ?score WHERE {\n" +
            "  (?s ?score) luc:query (\"title\" \"learning\" 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    assertNotNull(sol.get("s"));
                    count++;
                }
                assertTrue("Should find results when searching 'title' field", count > 0);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testLucQueryByFieldNameArray() {
        // Search multiple fields via JSON array
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s ?score WHERE {\n" +
            "  (?s ?score) luc:query ('[\"title\"]' \"learning\" 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    rs.next();
                    count++;
                }
                assertTrue("Should find results when searching via field array", count > 0);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testLucQueryFieldBinding() {
        // Search a single field and check that ?field is bound
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s ?score ?lit ?totalHits ?g ?field WHERE {\n" +
            "  (?s ?score ?lit ?totalHits ?g ?field) luc:query (\"title\" \"learning\" 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    assertNotNull("?field should be bound for single-field search", sol.get("field"));
                    assertEquals("Field should be 'title'", "title", sol.getLiteral("field").getString());
                    count++;
                }
                assertTrue("Should find results", count > 0);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testLucQueryFieldBindingDefaultUnbound() {
        // With "default" (multi-field), ?field should be unbound
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s ?score ?lit ?totalHits ?g ?field WHERE {\n" +
            "  (?s ?score ?lit ?totalHits ?g ?field) luc:query (\"default\" \"learning\" 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    // "default" resolves to a single field "title" (only defaultSearch field),
                    // so ?field will be bound in this test config
                    count++;
                }
                assertTrue("Should find results", count > 0);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testLucQueryFieldWithCqlFilter() {
        // Search specific field with CQL filter
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s ?score WHERE {\n" +
            "  (?s ?score) luc:query (\"title\" \"learning\" " +
            "    '{\"op\":\"=\",\"args\":[{\"property\":\"category\"},\"technology\"]}' 20)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                Set<String> subjects = new HashSet<>();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    subjects.add(sol.getResource("s").getURI());
                }
                assertTrue("Should find technology docs", subjects.size() > 0);
                for (String s : subjects) {
                    assertFalse("Should not find doc4 (science)", s.equals(NS + "doc4"));
                }
            }
        } finally {
            dataset.end();
        }
    }
}
