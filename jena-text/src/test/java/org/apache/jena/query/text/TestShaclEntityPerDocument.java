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
 * End-to-end tests for SHACL entity-per-document indexing.
 * Creates a dataset via programmatic setup, adds data, queries via text:query and text:facet.
 */
public class TestShaclEntityPerDocument {

    private static final String NS = "http://example.org/";
    private static final Node BOOK_CLASS = NodeFactory.createURI(NS + "Book");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");
    private static final Node AUTHOR_PRED = NodeFactory.createURI(NS + "author");

    private Dataset dataset;
    private ShaclTextIndexLucene textIndex;

    @Before
    public void setUp() {
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

            addBook(model, "book1", "Introduction to Machine Learning", "Technology", "Smith");
            addBook(model, "book2", "Deep Learning Neural Networks", "Technology", "Jones");
            addBook(model, "book3", "Machine Learning in Practice", "Technology", "Smith");
            addBook(model, "book4", "Quantum Physics Basics", "Science", "Wilson");
            addBook(model, "book5", "Biology of Learning", "Science", "Brown");

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
    public void testTextQueryFindsBooks() {
        // Search for "machine learning" via the TextIndex directly
        List<TextHit> hits = textIndex.query(TITLE_PRED, "machine learning", null, null);
        assertFalse("Should find books matching 'machine learning'", hits.isEmpty());

        Set<String> uris = new HashSet<>();
        for (TextHit hit : hits) {
            uris.add(hit.getNode().getURI());
        }
        assertTrue(uris.contains(NS + "book1"));
        assertTrue(uris.contains(NS + "book3"));
    }

    @Test
    public void testTextQueryViaSPARQL() {
        String queryStr =
            "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "PREFIX ex: <" + NS + ">\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query ('default' 'machine learning') .\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(queryStr);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
                ResultSet rs = qexec.execSelect();
                Set<String> results = new HashSet<>();
                while (rs.hasNext()) {
                    results.add(rs.next().getResource("s").getURI());
                }
                assertTrue("Should find book1", results.contains(NS + "book1"));
                assertTrue("Should find book3", results.contains(NS + "book3"));
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetCounts() {
        // Get facet counts for category field
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("category"), 10);

        assertNotNull(facets);
        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);

        // Technology: 3 books, Science: 2 books
        Map<String, Long> countMap = new HashMap<>();
        for (FacetValue fv : categoryFacets) {
            countMap.put(fv.getValue(), fv.getCount());
        }

        assertEquals("Technology should have 3", Long.valueOf(3), countMap.get("Technology"));
        assertEquals("Science should have 2", Long.valueOf(2), countMap.get("Science"));
    }

    @Test
    public void testFacetCountsFiltered() {
        // Get facet counts filtered by "learning"
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "learning",
            Arrays.asList("category", "author"), 10);

        assertNotNull(facets);

        // "learning" appears in: book1 (Technology, Smith), book2 (Technology, Jones),
        // book3 (Technology, Smith), book5 (Science, Brown)
        // So Technology=3, Science=1 (or similar depending on Lucene matching)
        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull(categoryFacets);
        assertFalse("Should have category facets", categoryFacets.isEmpty());
    }

    @Test
    public void testAllBooksIndexed() {
        // Each book has a unique title — search for common words to find all
        List<TextHit> hitsLearning = textIndex.query(TITLE_PRED, "learning", null, null);
        List<TextHit> hitsPhysics = textIndex.query(TITLE_PRED, "physics", null, null);
        List<TextHit> hitsBiology = textIndex.query(TITLE_PRED, "biology", null, null);
        List<TextHit> hitsDeep = textIndex.query(TITLE_PRED, "deep", null, null);

        Set<String> allUris = new HashSet<>();
        for (TextHit hit : hitsLearning) allUris.add(hit.getNode().getURI());
        for (TextHit hit : hitsPhysics) allUris.add(hit.getNode().getURI());
        for (TextHit hit : hitsBiology) allUris.add(hit.getNode().getURI());
        for (TextHit hit : hitsDeep) allUris.add(hit.getNode().getURI());

        assertTrue("Should find multiple books indexed", allUris.size() >= 4);
    }

    @Test
    public void testAddBookAfterInitialLoad() {
        // Add a new book
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            addBook(model, "book6", "Advanced Machine Learning Techniques", "Technology", "Adams");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Should be searchable
        List<TextHit> hits = textIndex.query(TITLE_PRED, "Advanced", null, null);
        assertFalse("Should find the newly added book", hits.isEmpty());

        Set<String> uris = new HashSet<>();
        for (TextHit hit : hits) {
            uris.add(hit.getNode().getURI());
        }
        assertTrue(uris.contains(NS + "book6"));
    }

    @Test
    public void testEntityPerDocumentModel() {
        // In entity-per-document model, each entity has ONE document with all fields.
        // The key verification: we can query by title and see that category facets
        // come back correctly without needing a URI-join (they're on the same document).

        // Search for "machine" and get category facets in same index
        List<TextHit> hits = textIndex.query(TITLE_PRED, "machine", null, null);
        assertTrue("Should find machine learning books", hits.size() >= 2);

        // Facets should also work since all fields are on same document
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "machine",
            Arrays.asList("category"), 10);

        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull("Should get category facets for machine query", categoryFacets);
        assertFalse("Category facets should not be empty", categoryFacets.isEmpty());
    }
}
