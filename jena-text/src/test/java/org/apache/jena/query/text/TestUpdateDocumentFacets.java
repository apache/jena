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
 * Tests that updateDocument properly applies facetsConfig.build() so that
 * facet counts remain correct after document updates.
 */
public class TestUpdateDocumentFacets {

    private static final String NS = "http://example.org/";
    private static final Property CATEGORY = ResourceFactory.createProperty(NS + "category");

    private Dataset dataset;
    private TextIndexLucene textIndex;

    @Before
    public void setUp() {
        EntityDefinition entDef = new EntityDefinition("uri", "text");
        entDef.setPrimaryPredicate(RDFS.label);
        entDef.set("category", CATEGORY.asNode());

        TextIndexConfig config = new TextIndexConfig(entDef);
        config.setValueStored(true);
        config.setFacetFields(Arrays.asList("category"));

        Dataset baseDs = DatasetFactory.create();
        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        textIndex = new TextIndexLucene(dir, config);
        dataset = TextDatasetFactory.create(baseDs, textIndex);
    }

    @After
    public void tearDown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    @Test
    public void testUpdatePreservesFacetCounts() {
        // Add initial data
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource doc = ResourceFactory.createResource(NS + "doc1");
            model.add(doc, RDFS.label, "Original Title");
            model.add(doc, CATEGORY, "science");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Verify initial facet counts
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10);
        assertNotNull(facets.get("category"));

        // Update the document - this exercises updateDocument
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource doc = ResourceFactory.createResource(NS + "doc1");
            // Remove old and add new
            model.removeAll(doc, RDFS.label, null);
            model.add(doc, RDFS.label, "Updated Title");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Verify facet counts are still valid after update
        Map<String, List<FacetValue>> updatedFacets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10);
        assertNotNull(updatedFacets.get("category"));
    }

    @Test
    public void testAddMultipleDocumentsWithFacets() {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            for (int i = 0; i < 5; i++) {
                Resource doc = ResourceFactory.createResource(NS + "doc" + i);
                model.add(doc, RDFS.label, "Document " + i);
                model.add(doc, CATEGORY, i < 3 ? "technology" : "science");
            }
            dataset.commit();
        } finally {
            dataset.end();
        }

        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10);

        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);
        assertEquals(2, categoryFacets.size());

        // technology=3, science=2
        boolean foundTech = false;
        boolean foundScience = false;
        for (FacetValue fv : categoryFacets) {
            if ("technology".equals(fv.getValue())) {
                assertEquals(3, fv.getCount());
                foundTech = true;
            } else if ("science".equals(fv.getValue())) {
                assertEquals(2, fv.getCount());
                foundScience = true;
            }
        }
        assertTrue("Should find technology", foundTech);
        assertTrue("Should find science", foundScience);
    }
}
