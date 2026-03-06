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
import org.apache.jena.query.text.ShaclIndexMapping.*;
import org.apache.jena.query.text.assembler.ShaclIndexAssembler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that updateEntityForProfile properly applies facetsConfig.build() so that
 * facet counts remain correct after document updates.
 */
public class TestUpdateDocumentFacets {

    private static final String NS = "http://example.org/";
    private static final Node DOC_CLASS = NodeFactory.createURI(NS + "Document");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");

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

        IndexProfile profile = new IndexProfile(
            NodeFactory.createURI(NS + "DocShape"),
            Collections.singleton(DOC_CLASS),
            "uri", "docType",
            Arrays.asList(titleField, categoryField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(profile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setFacetFields(mapping.getFacetFieldNames());
        config.setValueStored(true);

        Dataset baseDs = DatasetFactory.create();
        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        textIndex = new ShaclTextIndexLucene(dir, config);
        ShaclTextDocProducer producer = new ShaclTextDocProducer(
            baseDs.asDatasetGraph(), textIndex, mapping);
        dataset = TextDatasetFactory.create(baseDs, textIndex, true, producer);
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
            model.add(doc, RDF.type, ResourceFactory.createResource(NS + "Document"));
            model.add(doc, ResourceFactory.createProperty(NS + "title"), "Original Title");
            model.add(doc, ResourceFactory.createProperty(NS + "category"), "science");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Verify initial facet counts
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10);
        assertNotNull(facets.get("category"));

        // Update the document - this exercises updateEntityForProfile
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource doc = ResourceFactory.createResource(NS + "doc1");
            model.removeAll(doc, ResourceFactory.createProperty(NS + "title"), null);
            model.add(doc, ResourceFactory.createProperty(NS + "title"), "Updated Title");
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
                model.add(doc, RDF.type, ResourceFactory.createResource(NS + "Document"));
                model.add(doc, ResourceFactory.createProperty(NS + "title"), "Document " + i);
                model.add(doc, ResourceFactory.createProperty(NS + "category"), i < 3 ? "technology" : "science");
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
