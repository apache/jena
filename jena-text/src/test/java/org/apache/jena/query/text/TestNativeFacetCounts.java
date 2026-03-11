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
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
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
 * Tests for native Lucene facet counts using SortedSetDocValues.
 */
public class TestNativeFacetCounts {

    private static final String NS = "http://example.org/";
    private static final Node DOC_CLASS = NodeFactory.createURI(NS + "Document");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");
    private static final Node AUTHOR_PRED = NodeFactory.createURI(NS + "author");

    private Dataset dataset;
    private ShaclTextIndexLucene textIndex;

    @Before
    public void setUp() {
        FieldDef titleField = new FieldDef("text", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));

        FieldDef categoryField = new FieldDef("category", FieldType.KEYWORD, null,
            true, true, true, false, true, false,
            Collections.singleton(CATEGORY_PRED));

        FieldDef authorField = new FieldDef("author", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            Collections.singleton(AUTHOR_PRED));

        IndexProfile docProfile = new IndexProfile(
            NodeFactory.createURI(NS + "DocShape"),
            Collections.singleton(DOC_CLASS),
            "uri", "docType",
            Arrays.asList(titleField, categoryField, authorField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(docProfile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setFacetFields(mapping.getFacetFieldNames());
        config.setValueStored(true);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        textIndex = new ShaclTextIndexLucene(dir, config);

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

            addDoc(model, "doc1", "Introduction to Machine Learning", "technology", "Smith");
            addDoc(model, "doc2", "Deep Learning Neural Networks", "technology", "Jones");
            addDoc(model, "doc3", "Machine Learning for Beginners", "technology", "Smith");
            addDoc(model, "doc4", "Advanced Machine Learning", "technology", "Brown");
            addDoc(model, "doc5", "Learning About Quantum Physics", "science", "Wilson");
            addDoc(model, "doc6", "Machine Learning in Biology", "science", "Smith");
            addDoc(model, "doc7", "Learning to Cook Italian", "cooking", "Garcia");
            addDoc(model, "doc8", "Learning Baking Fundamentals", "cooking", "Taylor");

            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    private void addDoc(Model model, String id, String title, String category, String author) {
        Resource doc = ResourceFactory.createResource(NS + id);
        model.add(doc, RDF.type, ResourceFactory.createResource(NS + "Document"));
        model.add(doc, ResourceFactory.createProperty(NS + "title"), title);
        model.add(doc, ResourceFactory.createProperty(NS + "category"), category);
        model.add(doc, ResourceFactory.createProperty(NS + "author"), author);
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
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10
        );

        assertNotNull(facets);
        assertTrue(facets.containsKey("category"));

        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);

        assertEquals(3, categoryFacets.size());

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

        List<FacetValue> authorFacets = facets.get("author");
        assertNotNull(authorFacets);
        assertTrue(authorFacets.size() > 0);

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
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "machine learning",
            Arrays.asList("category"),
            10
        );

        assertNotNull(facets);
        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);

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
        assertTrue(categoryFacets.isEmpty());
    }

    @Test
    public void testGetAllChildrenWhenMaxValuesZero() {
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "learning",
            Arrays.asList("author"),
            0
        );

        List<FacetValue> authorFacets = facets.get("author");
        assertNotNull(authorFacets);
        assertEquals("maxValues=0 should return all authors", 6, authorFacets.size());
    }

    @Test
    public void testMinCountFiltering() {
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
