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
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
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
 * Tests for native Lucene facet counts using SortedSetDocValues.
 */
public class TestNativeFacetCounts {

    private static final String NS = "http://example.org/";
    private static final Property CATEGORY = ResourceFactory.createProperty(NS + "category");
    private static final Property AUTHOR = ResourceFactory.createProperty(NS + "author");

    private Dataset dataset;
    private TextIndexLucene textIndex;

    @Before
    public void setUp() {
        // Create entity definition with facet-enabled fields
        EntityDefinition entDef = new EntityDefinition("uri", "text");
        entDef.setPrimaryPredicate(RDFS.label);
        entDef.set("category", CATEGORY.asNode());
        entDef.set("author", AUTHOR.asNode());

        // Create config with facet fields
        TextIndexConfig config = new TextIndexConfig(entDef);
        config.setValueStored(true);
        config.setFacetFields(Arrays.asList("category", "author"));

        // Create dataset with text index
        Dataset baseDs = DatasetFactory.create();
        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        textIndex = new TextIndexLucene(dir, config);
        dataset = TextDatasetFactory.create(baseDs, textIndex);

        // Load test data
        loadTestData();
    }

    private void loadTestData() {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();

            // Technology documents
            createDoc(model, "doc1", "Introduction to Machine Learning", "technology", "Smith");
            createDoc(model, "doc2", "Deep Learning Neural Networks", "technology", "Jones");
            createDoc(model, "doc3", "Machine Learning for Beginners", "technology", "Smith");
            createDoc(model, "doc4", "Advanced Machine Learning", "technology", "Brown");

            // Science documents
            createDoc(model, "doc5", "Learning About Quantum Physics", "science", "Wilson");
            createDoc(model, "doc6", "Machine Learning in Biology", "science", "Smith");

            // Cooking documents
            createDoc(model, "doc7", "Learning to Cook Italian", "cooking", "Garcia");
            createDoc(model, "doc8", "Learning Baking Fundamentals", "cooking", "Taylor");

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
    public void testFacetingIsEnabled() {
        assertTrue("Faceting should be enabled", textIndex.isFacetingEnabled());
        List<String> fields = textIndex.getFacetFields();
        assertEquals(2, fields.size());
        assertTrue(fields.contains("category"));
        assertTrue(fields.contains("author"));
    }

    @Test
    public void testOpenFacets() {
        // Get facet counts without a search query (open facets)
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10
        );

        assertNotNull(facets);
        assertTrue(facets.containsKey("category"));

        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);

        // Should have 3 categories: technology (4), science (2), cooking (2)
        assertEquals(3, categoryFacets.size());

        // Verify counts - should be sorted by count descending
        FacetValue first = categoryFacets.get(0);
        assertEquals("technology", first.getValue());
        assertEquals(4, first.getCount());
    }

    @Test
    public void testOpenFacetsMultipleFields() {
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category", "author"), 10
        );

        assertNotNull(facets);
        assertEquals(2, facets.keySet().size());
        assertTrue(facets.containsKey("category"));
        assertTrue(facets.containsKey("author"));

        // Check author facets
        List<FacetValue> authorFacets = facets.get("author");
        assertNotNull(authorFacets);
        assertTrue(authorFacets.size() > 0);

        // Smith appears 3 times (doc1, doc3, doc6)
        boolean foundSmith = false;
        for (FacetValue fv : authorFacets) {
            if ("Smith".equals(fv.getValue())) {
                assertEquals(3, fv.getCount());
                foundSmith = true;
                break;
            }
        }
        assertTrue("Should find Smith in author facets", foundSmith);
    }

    @Test
    public void testFilteredFacets() {
        // Get facet counts filtered by "machine learning"
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "machine learning",
            Arrays.asList("category"),
            10
        );

        assertNotNull(facets);
        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);

        // "machine learning" appears in:
        // doc1 (technology), doc3 (technology), doc4 (technology), doc6 (science)
        // So: technology=3, science=1
        long techCount = 0;
        long scienceCount = 0;
        for (FacetValue fv : categoryFacets) {
            if ("technology".equals(fv.getValue())) {
                techCount = fv.getCount();
            } else if ("science".equals(fv.getValue())) {
                scienceCount = fv.getCount();
            }
        }

        assertTrue("Technology should have more machine learning docs", techCount >= scienceCount);
    }

    @Test
    public void testMaxFacetValues() {
        // Request only 2 facet values
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 2
        );

        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);
        assertTrue("Should limit to max 2 values", categoryFacets.size() <= 2);
    }

    @Test
    public void testEmptyFacetFields() {
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList(), 10
        );
        assertTrue("Empty field list should return empty map", facets.isEmpty());
    }

    @Test
    public void testNonExistentField() {
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("nonexistent"), 10
        );

        // Should return empty list for non-existent field
        List<FacetValue> nonexistentFacets = facets.get("nonexistent");
        assertNotNull(nonexistentFacets);
        assertTrue(nonexistentFacets.isEmpty());
    }

    @Test
    public void testNoResultsQuery() {
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "xyznonexistentquery123",
            Arrays.asList("category"),
            10
        );

        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);
        // Should be empty since no documents match
        assertTrue(categoryFacets.isEmpty());
    }

    @Test
    public void testGetAllChildrenWhenMaxValuesZero() {
        // maxValues=0 should use getAllChildren and return all values
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "learning",
            Arrays.asList("author"),
            0
        );

        List<FacetValue> authorFacets = facets.get("author");
        assertNotNull(authorFacets);
        // Should return all 6 authors: Smith(3), Jones(1), Brown(1), Wilson(1), Garcia(1), Taylor(1)
        assertEquals("maxValues=0 should return all authors", 6, authorFacets.size());
    }

    @Test
    public void testMinCountFiltering() {
        // minCount=2 should exclude authors with count < 2
        // Smith appears 3 times; all others appear once
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "learning",
            Arrays.asList("author"),
            10,
            2
        );

        List<FacetValue> authorFacets = facets.get("author");
        assertNotNull(authorFacets);
        assertEquals("Only Smith should pass minCount=2", 1, authorFacets.size());
        assertEquals("Smith", authorFacets.get(0).getValue());
        assertEquals(3, authorFacets.get(0).getCount());
    }
}
