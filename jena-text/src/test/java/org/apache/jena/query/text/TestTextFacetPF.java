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

import java.util.Arrays;
import java.util.Collections;

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
 * Tests for the luc:facet property function.
 */
public class TestTextFacetPF {

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
        TextIndexLucene textIndex = new TextIndexLucene(dir, config);

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
    public void testBasicFacetCounts() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"learning\" '[\"category\"]' 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    assertNotNull(sol.get("f"));
                    assertNotNull(sol.get("v"));
                    assertNotNull(sol.get("c"));
                    count++;
                }
                assertTrue("Should have at least one facet result", count > 0);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetCountsWithMultipleFields() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"learning\" '[\"category\", \"author\"]' 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                boolean foundCategory = false;
                boolean foundAuthor = false;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    String field = sol.getLiteral("f").getString();
                    if ("category".equals(field)) foundCategory = true;
                    if ("author".equals(field)) foundAuthor = true;
                }
                assertTrue("Should have category facets", foundCategory);
                assertTrue("Should have author facets", foundAuthor);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetCountsWithFilters() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"learning\" '[\"author\"]' '{\"category\": [\"technology\"]}' 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    assertEquals("author", sol.getLiteral("f").getString());
                    count++;
                }
                assertTrue("Should have filtered author facets", count > 0);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetCountsWithMaxValues() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"learning\" '[\"author\"]' 1)\n" +
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
                assertEquals("Should have at most 1 author facet value", 1, count);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetCountsWithMinCount() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"learning\" '[\"author\"]' 10 2)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    long cnt = sol.getLiteral("c").getLong();
                    assertTrue("Count should be >= 2", cnt >= 2);
                    count++;
                }
                assertEquals("Only Smith should pass minCount=2", 1, count);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetCountsWithMaxValuesZero() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"learning\" '[\"author\"]' 0)\n" +
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
                assertEquals("maxValues=0 should return all authors", 3, count);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetCountsWithMinCountAndMaxValues() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"learning\" '[\"author\"]' 0 2)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    assertEquals("Smith", sol.getLiteral("v").getString());
                    count++;
                }
                assertEquals("Only Smith should pass combined filter", 1, count);
            }
        } finally {
            dataset.end();
        }
    }
}
