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
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ShaclBulkIndexer}.
 * <p>
 * Loads data directly into a base dataset (bypassing the text change listener),
 * then runs the bulk indexer and verifies the Lucene index is correctly built.
 */
public class TestShaclBulkIndexer {

    private static final String NS = "http://example.org/";
    private static final Node BOOK_CLASS = NodeFactory.createURI(NS + "Book");
    private static final Node ARTICLE_CLASS = NodeFactory.createURI(NS + "Article");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");
    private static final Node AUTHOR_PRED = NodeFactory.createURI(NS + "author");
    private static final Node TOPIC_PRED = NodeFactory.createURI(NS + "topic");

    private Dataset baseDataset;
    private TextIndexLucene textIndex;
    private ShaclIndexMapping mapping;

    @Before
    public void setUp() {
        // Book profile
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

        // Article profile (different shape, shared title field)
        FieldDef articleTitleField = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));

        FieldDef topicField = new FieldDef("topic", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            Collections.singleton(TOPIC_PRED));

        IndexProfile articleProfile = new IndexProfile(
            NodeFactory.createURI(NS + "ArticleShape"),
            Collections.singleton(ARTICLE_CLASS),
            "uri", "docType",
            Arrays.asList(articleTitleField, topicField));

        mapping = new ShaclIndexMapping(Arrays.asList(bookProfile, articleProfile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setFacetFields(mapping.getFacetFieldNames());
        config.setValueStored(true);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        textIndex = new TextIndexLucene(dir, config);

        // Create a plain dataset — NO text wrapper, simulating bulk load
        baseDataset = DatasetFactory.create();
    }

    @After
    public void tearDown() {
        if (textIndex != null) textIndex.close();
        if (baseDataset != null) baseDataset.close();
    }

    private void addBook(Model model, String id, String title, String category, String author) {
        Resource book = ResourceFactory.createResource(NS + id);
        model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
        model.add(book, ResourceFactory.createProperty(NS + "title"), title);
        model.add(book, ResourceFactory.createProperty(NS + "category"), category);
        model.add(book, ResourceFactory.createProperty(NS + "author"), author);
    }

    private void addArticle(Model model, String id, String title, String topic) {
        Resource article = ResourceFactory.createResource(NS + id);
        model.add(article, RDF.type, ResourceFactory.createResource(NS + "Article"));
        model.add(article, ResourceFactory.createProperty(NS + "title"), title);
        model.add(article, ResourceFactory.createProperty(NS + "topic"), topic);
    }

    @Test
    public void testBulkIndexBasic() {
        // Load data directly (bypassing text listener)
        Model model = baseDataset.getDefaultModel();
        addBook(model, "book1", "Introduction to Machine Learning", "Technology", "Smith");
        addBook(model, "book2", "Deep Learning Neural Networks", "Technology", "Jones");
        addBook(model, "book3", "Quantum Physics Basics", "Science", "Wilson");

        // Verify nothing in index yet
        List<TextHit> hits = textIndex.query(TITLE_PRED, "machine", null, null);
        assertTrue("Index should be empty before bulk indexing", hits.isEmpty());

        // Run bulk indexer
        DatasetGraph dsg = baseDataset.asDatasetGraph();
        ShaclBulkIndexer indexer = new ShaclBulkIndexer(dsg, textIndex, mapping);
        indexer.index();

        assertEquals("Should have indexed 3 entities", 3, indexer.getEntityCount());

        // Now verify index has data
        hits = textIndex.query(TITLE_PRED, "machine", null, null);
        assertFalse("Should find 'machine' after bulk indexing", hits.isEmpty());

        Set<String> uris = new HashSet<>();
        for (TextHit hit : hits) uris.add(hit.getNode().getURI());
        assertTrue("Should find book1", uris.contains(NS + "book1"));
    }

    @Test
    public void testBulkIndexFacets() {
        Model model = baseDataset.getDefaultModel();
        addBook(model, "book1", "Intro to ML", "Technology", "Smith");
        addBook(model, "book2", "Deep Learning", "Technology", "Jones");
        addBook(model, "book3", "Quantum Physics", "Science", "Wilson");
        addBook(model, "book4", "Biology 101", "Science", "Brown");

        DatasetGraph dsg = baseDataset.asDatasetGraph();
        ShaclBulkIndexer indexer = new ShaclBulkIndexer(dsg, textIndex, mapping);
        indexer.index();

        // Check facets
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10);

        assertNotNull(facets);
        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);

        Map<String, Long> countMap = new HashMap<>();
        for (FacetValue fv : categoryFacets) countMap.put(fv.getValue(), fv.getCount());

        assertEquals("Technology should have 2", Long.valueOf(2), countMap.get("Technology"));
        assertEquals("Science should have 2", Long.valueOf(2), countMap.get("Science"));
    }

    @Test
    public void testBulkIndexMultipleProfiles() {
        Model model = baseDataset.getDefaultModel();
        addBook(model, "book1", "Machine Learning Guide", "Technology", "Smith");
        addArticle(model, "art1", "Machine Learning in Industry", "AI");
        addArticle(model, "art2", "Quantum Computing Review", "Physics");

        DatasetGraph dsg = baseDataset.asDatasetGraph();
        ShaclBulkIndexer indexer = new ShaclBulkIndexer(dsg, textIndex, mapping);
        indexer.index();

        assertEquals("Should have indexed 3 entities (1 book + 2 articles)", 3, indexer.getEntityCount());

        // Search across both profiles
        List<TextHit> hits = textIndex.query(TITLE_PRED, "machine", null, null);
        Set<String> uris = new HashSet<>();
        for (TextHit hit : hits) uris.add(hit.getNode().getURI());

        assertTrue("Should find book1", uris.contains(NS + "book1"));
        assertTrue("Should find art1", uris.contains(NS + "art1"));
        assertFalse("Should NOT find art2", uris.contains(NS + "art2"));

        // Check topic facets (article only)
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("topic"), 10);
        List<FacetValue> topicFacets = facets.get("topic");
        assertNotNull(topicFacets);
        assertEquals("Should have 2 topic values", 2, topicFacets.size());
    }

    @Test
    public void testBulkIndexEmptyDataset() {
        DatasetGraph dsg = baseDataset.asDatasetGraph();
        ShaclBulkIndexer indexer = new ShaclBulkIndexer(dsg, textIndex, mapping);
        indexer.index();

        assertEquals("Should have indexed 0 entities", 0, indexer.getEntityCount());
    }

    @Test
    public void testBulkIndexIgnoresIrrelevantTypes() {
        Model model = baseDataset.getDefaultModel();
        // Add an entity with a type that doesn't match any profile
        Resource other = ResourceFactory.createResource(NS + "other1");
        model.add(other, RDF.type, ResourceFactory.createResource(NS + "OtherType"));
        model.add(other, ResourceFactory.createProperty(NS + "title"), "Some Other Thing");

        // Also add a matching book
        addBook(model, "book1", "Machine Learning Guide", "Technology", "Smith");

        DatasetGraph dsg = baseDataset.asDatasetGraph();
        ShaclBulkIndexer indexer = new ShaclBulkIndexer(dsg, textIndex, mapping);
        indexer.index();

        assertEquals("Should have indexed only 1 entity (the book)", 1, indexer.getEntityCount());
    }

    @Test
    public void testBulkIndexIdempotent() {
        Model model = baseDataset.getDefaultModel();
        addBook(model, "book1", "Machine Learning Guide", "Technology", "Smith");

        DatasetGraph dsg = baseDataset.asDatasetGraph();

        // Index twice
        ShaclBulkIndexer indexer1 = new ShaclBulkIndexer(dsg, textIndex, mapping);
        indexer1.index();

        ShaclBulkIndexer indexer2 = new ShaclBulkIndexer(dsg, textIndex, mapping);
        indexer2.index();

        // Should still find exactly 1 hit (not duplicated)
        List<TextHit> hits = textIndex.query(TITLE_PRED, "machine", null, null);
        assertEquals("Should find exactly 1 hit (no duplicates)", 1, hits.size());
    }

    @Test
    public void testBulkIndexMultiValuedField() {
        Model model = baseDataset.getDefaultModel();
        Resource book = ResourceFactory.createResource(NS + "book1");
        model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
        model.add(book, ResourceFactory.createProperty(NS + "title"), "Multi Category Book");
        model.add(book, ResourceFactory.createProperty(NS + "category"), "Technology");
        model.add(book, ResourceFactory.createProperty(NS + "category"), "Science");
        model.add(book, ResourceFactory.createProperty(NS + "author"), "Smith");

        DatasetGraph dsg = baseDataset.asDatasetGraph();
        ShaclBulkIndexer indexer = new ShaclBulkIndexer(dsg, textIndex, mapping);
        indexer.index();

        // Both category values should be faceted
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10);
        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);

        Set<String> values = new HashSet<>();
        for (FacetValue fv : categoryFacets) values.add(fv.getValue());
        assertTrue("Should have Technology facet", values.contains("Technology"));
        assertTrue("Should have Science facet", values.contains("Science"));
    }
}
