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
 * Tests for JSON filter support in text:query property function.
 */
public class TestTextQueryPFFilters {

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
    public void testQueryWithoutFiltersStillWorks() {
        // Standard text:query without filters should work as before
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?s ?score WHERE {\n" +
            "  (?s ?score) text:query (\"learning\" 10)\n" +
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
    public void testQueryWithSingleFilter() {
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?s ?score WHERE {\n" +
            "  (?s ?score) text:query (\"learning\" '{\"category\": [\"technology\"]}' 20)\n" +
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
                // Only technology docs should be returned
                // doc1, doc2, doc3 are technology + have "learning"
                assertTrue("Should find technology docs", subjects.size() > 0);
                for (String s : subjects) {
                    // doc4 (science) and doc5 (science) should NOT be here
                    assertFalse("Should not find doc4 (science)", s.equals(NS + "doc4"));
                }
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testQueryWithMultiValueFilter() {
        // Filter for category = technology OR science
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) text:query (\"learning\" '{\"category\": [\"technology\", \"science\"]}' 20)\n" +
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
                assertTrue("Should find results from both categories", count > 0);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testQueryWithMultiFieldFilter() {
        // Filter for category=technology AND author=Smith
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) text:query (\"learning\" '{\"category\": [\"technology\"], \"author\": [\"Smith\"]}' 20)\n" +
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
                // Only doc1 and doc3 are technology + Smith + "learning"
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
    public void testQueryWithFilterNoMatches() {
        // Filter for a non-existent category
        String sparql = "PREFIX text: <http://jena.apache.org/text#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) text:query (\"learning\" '{\"category\": [\"nonexistent\"]}' 20)\n" +
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
    public void testParseJsonFilters() {
        Map<String, List<String>> filters = TextQueryPF.parseJsonFilters(
            "{\"category\": [\"Technology\", \"Science\"], \"author\": [\"Smith\"]}");

        assertEquals(2, filters.size());
        assertEquals(Arrays.asList("Technology", "Science"), filters.get("category"));
        assertEquals(Arrays.asList("Smith"), filters.get("author"));
    }

    @Test
    public void testParseJsonFiltersSingleValue() {
        Map<String, List<String>> filters = TextQueryPF.parseJsonFilters(
            "{\"category\": [\"Technology\"]}");

        assertEquals(1, filters.size());
        assertEquals(Arrays.asList("Technology"), filters.get("category"));
    }
}
