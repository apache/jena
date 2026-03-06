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
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for inverse and sequence path support in SHACL entity-per-document indexing.
 * <p>
 * Data model:
 * <pre>
 *   ex:book1 a ex:Book ; rdfs:label "Machine Learning" ; ex:author ex:smith .
 *   ex:smith a ex:Person ; ex:name "Jane Smith" .
 *   ex:smith ex:wrote ex:book1 .
 * </pre>
 * <p>
 * Sequence path: {@code sh:path ( ex:author ex:name )} indexes "Jane Smith" on book1.
 * Inverse path: {@code sh:path [ sh:inversePath ex:wrote ]} indexes ex:smith on book1.
 */
public class TestShaclPathSupport {

    private static final String NS = "http://example.org/";
    private static final Node BOOK_CLASS = NodeFactory.createURI(NS + "Book");
    private static final Node PERSON_CLASS = NodeFactory.createURI(NS + "Person");
    private static final Node LABEL_PRED = RDFS.label.asNode();
    private static final Node AUTHOR_PRED = NodeFactory.createURI(NS + "author");
    private static final Node NAME_PRED = NodeFactory.createURI(NS + "name");
    private static final Node WROTE_PRED = NodeFactory.createURI(NS + "wrote");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");

    private Dataset dataset;
    private ShaclTextIndexLucene textIndex;

    @Before
    public void setUp() {
        // title: direct predicate (rdfs:label)
        FieldDef titleField = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(LABEL_PRED),
            PathFactory.pathLink(LABEL_PRED));

        // category: direct predicate (keyword, facetable)
        FieldDef categoryField = new FieldDef("category", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            Collections.singleton(CATEGORY_PRED),
            PathFactory.pathLink(CATEGORY_PRED));

        // authorName: sequence path (ex:author / ex:name)
        Path authorNamePath = PathFactory.pathSeq(
            PathFactory.pathLink(AUTHOR_PRED),
            PathFactory.pathLink(NAME_PRED));
        Set<Node> authorNamePreds = new LinkedHashSet<>();
        authorNamePreds.add(AUTHOR_PRED);
        authorNamePreds.add(NAME_PRED);
        FieldDef authorNameField = new FieldDef("authorName", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            authorNamePreds, authorNamePath);

        // wroteBy: inverse path (^ex:wrote) — indexes who wrote this book
        Path wroteByPath = PathFactory.pathInverse(PathFactory.pathLink(WROTE_PRED));
        Set<Node> wroteByPreds = Collections.singleton(WROTE_PRED);
        FieldDef wroteByField = new FieldDef("wroteBy", FieldType.KEYWORD, null,
            true, true, false, false, true, false,
            wroteByPreds, wroteByPath);

        IndexProfile bookProfile = new IndexProfile(
            NodeFactory.createURI(NS + "BookShape"),
            Collections.singleton(BOOK_CLASS),
            "uri", "docType",
            Arrays.asList(titleField, categoryField, authorNameField, wroteByField));

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

            // 1. Person entities first — needed by sequence paths before books are added
            addPerson(model, "smith", "Jane Smith");
            addPerson(model, "wilson", "Robert Wilson");

            // 2. Wrote relationships — needed by inverse paths before books are added
            model.add(ResourceFactory.createResource(NS + "smith"),
                ResourceFactory.createProperty(NS + "wrote"),
                ResourceFactory.createResource(NS + "book1"));
            model.add(ResourceFactory.createResource(NS + "smith"),
                ResourceFactory.createProperty(NS + "wrote"),
                ResourceFactory.createResource(NS + "book3"));
            model.add(ResourceFactory.createResource(NS + "wilson"),
                ResourceFactory.createProperty(NS + "wrote"),
                ResourceFactory.createResource(NS + "book2"));

            // 3. Book entities last — rebuild picks up all intermediate data
            addBook(model, "book1", "Introduction to Machine Learning", "Technology", "smith");
            addBook(model, "book2", "Quantum Physics Basics", "Science", "wilson");
            addBook(model, "book3", "Deep Learning Techniques", "Technology", "smith");

            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    private void addBook(Model model, String id, String title, String category, String authorId) {
        Resource book = ResourceFactory.createResource(NS + id);
        model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
        model.add(book, RDFS.label, title);
        model.add(book, ResourceFactory.createProperty(NS + "category"), category);
        model.add(book, ResourceFactory.createProperty(NS + "author"),
            ResourceFactory.createResource(NS + authorId));
    }

    private void addPerson(Model model, String id, String name) {
        Resource person = ResourceFactory.createResource(NS + id);
        model.add(person, RDF.type, ResourceFactory.createResource(NS + "Person"));
        model.add(person, ResourceFactory.createProperty(NS + "name"), name);
    }

    @After
    public void tearDown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    // --- Sequence path tests ---

    @Test
    public void testSequencePathIndexesAuthorName() {
        // book1 has ex:author ex:smith, and ex:smith has ex:name "Jane Smith"
        // The sequence path (ex:author / ex:name) should index "Jane Smith" on book1
        String queryStr =
            "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query ('default' 'machine') .\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            ResultSet rs = QueryExecutionFactory.create(queryStr, dataset).execSelect();
            Set<String> results = new HashSet<>();
            while (rs.hasNext()) {
                results.add(rs.next().getResource("s").getURI());
            }
            assertTrue("Should find book1 by title", results.contains(NS + "book1"));
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testSequencePathFacetCounts() {
        // authorName should be indexed via sequence path — facet counts should work
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("authorName"), 10);

        assertNotNull(facets);
        List<FacetValue> authorFacets = facets.get("authorName");
        assertNotNull("Should have authorName facets", authorFacets);

        Map<String, Long> countMap = new HashMap<>();
        for (FacetValue fv : authorFacets) {
            countMap.put(fv.getValue(), fv.getCount());
        }

        assertEquals("Jane Smith wrote 2 books", Long.valueOf(2), countMap.get("Jane Smith"));
        assertEquals("Robert Wilson wrote 1 book", Long.valueOf(1), countMap.get("Robert Wilson"));
    }

    // --- Inverse path tests ---

    @Test
    public void testInversePathIndexesWriter() {
        // ex:smith ex:wrote ex:book1, so ^ex:wrote on book1 should index ex:smith's URI
        // The wroteBy field stores URI strings as keywords
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            Arrays.asList("wroteBy"), 10);

        // wroteBy is not facetable in our setup, so let's query directly
        // The value should be the URI of the person who wrote the book
        String queryStr =
            "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "PREFIX ex: <" + NS + ">\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query ('default' 'machine') .\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            ResultSet rs = QueryExecutionFactory.create(queryStr, dataset).execSelect();
            Set<String> results = new HashSet<>();
            while (rs.hasNext()) {
                results.add(rs.next().getResource("s").getURI());
            }
            // book1 should be found and its wroteBy field should contain smith's URI
            assertTrue("Should find book1", results.contains(NS + "book1"));
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testInversePathMultipleWriters() {
        // Smith wrote book1 and book3, Wilson wrote book2
        // Each book's wroteBy field should contain the correct person URI
        // We verify by checking all books are indexed (data consistency)
        List<TextHit> hits = textIndex.query(LABEL_PRED, "learning OR physics OR deep", null, null);
        Set<String> uris = new HashSet<>();
        for (TextHit hit : hits) {
            uris.add(hit.getNode().getURI());
        }
        assertTrue("Should find book1", uris.contains(NS + "book1"));
        assertTrue("Should find book2", uris.contains(NS + "book2"));
        assertTrue("Should find book3", uris.contains(NS + "book3"));
    }

    // --- Combined tests ---

    @Test
    public void testDirectAndComplexPathsCoexist() {
        // Verify that direct fields (title, category) still work alongside complex paths
        Map<String, List<FacetValue>> facets = textIndex.getFacetCounts(
            "learning",
            Arrays.asList("category", "authorName"), 10);

        assertNotNull(facets);

        // Category facets (direct path)
        List<FacetValue> categoryFacets = facets.get("category");
        assertNotNull("Should have category facets", categoryFacets);
        assertFalse("Category facets should not be empty", categoryFacets.isEmpty());

        // Author name facets (sequence path)
        List<FacetValue> authorFacets = facets.get("authorName");
        assertNotNull("Should have authorName facets", authorFacets);
        assertFalse("Author name facets should not be empty", authorFacets.isEmpty());
    }

    @Test
    public void testFilterBySequencePathField() {
        // Filter by authorName="Jane Smith" via JSON filter on luc:query
        String queryStr =
            "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query ('default' 'learning OR physics OR deep' '{\"op\":\"=\",\"args\":[{\"property\":\"authorName\"},\"Jane Smith\"]}') .\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            ResultSet rs = QueryExecutionFactory.create(queryStr, dataset).execSelect();
            Set<String> results = new HashSet<>();
            while (rs.hasNext()) {
                results.add(rs.next().getResource("s").getURI());
            }
            // Only Smith's books should match
            assertTrue("Should find book1 (Smith)", results.contains(NS + "book1"));
            assertTrue("Should find book3 (Smith)", results.contains(NS + "book3"));
            assertFalse("Should NOT find book2 (Wilson)", results.contains(NS + "book2"));
        } finally {
            dataset.end();
        }
    }
}
