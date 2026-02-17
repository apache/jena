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

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the text:facet property function.
 */
public class TestTextFacetPF {

    private static final String NS = "http://example.org/";
    private static final Property CATEGORY = ResourceFactory.createProperty(NS + "category");
    private static final Property AUTHOR = ResourceFactory.createProperty(NS + "author");

    private Dataset dataset;

    @Before
    public void setUp() {
        TextQuery.init();

        EntityDefinition entDef = new EntityDefinition("uri", "text");
        entDef.setPrimaryPredicate(RDFS.label);
        entDef.set("category", CATEGORY.asNode());
        entDef.set("author", AUTHOR.asNode());

        TextIndexConfig config = new TextIndexConfig(entDef);
        config.setValueStored(true);
        config.setFacetFields(Arrays.asList("category", "author"));

        Dataset baseDs = DatasetFactory.create();
        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        TextIndexLucene textIndex = new TextIndexLucene(dir, config);
        dataset = TextDatasetFactory.create(baseDs, textIndex);

        loadTestData();
    }

    private void loadTestData() {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            createDoc(model, "doc1", "Introduction to Machine Learning", "technology", "Smith");
            createDoc(model, "doc2", "Deep Learning Neural Networks", "technology", "Jones");
            createDoc(model, "doc3", "Machine Learning for Beginners", "technology", "Smith");
            createDoc(model, "doc4", "Learning About Quantum Physics", "science", "Wilson");
            createDoc(model, "doc5", "Machine Learning in Biology", "science", "Smith");
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    private void createDoc(Model model, String id, String label, String category, String author) {
        Resource doc = ResourceFactory.createResource(NS + id);
        model.add(doc, RDFS.label, label);
        model.add(doc, CATEGORY, category);
        model.add(doc, AUTHOR, author);
    }

    @After
    public void tearDown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    @Test
    public void testBasicFacetCounts() {
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) text:facet (\"learning\" '[\"category\"]' 10)\n" +
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
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) text:facet (\"learning\" '[\"category\", \"author\"]' 10)\n" +
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
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) text:facet (\"learning\" '[\"author\"]' '{\"category\": [\"technology\"]}' 10)\n" +
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
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) text:facet (\"learning\" '[\"author\"]' 1)\n" +
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
    public void testFacetCountsWithProperty() {
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) text:facet (rdfs:label \"learning\" '[\"category\"]' 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                assertTrue("Should have facet results with property", rs.hasNext());
            }
        } finally {
            dataset.end();
        }
    }
}
