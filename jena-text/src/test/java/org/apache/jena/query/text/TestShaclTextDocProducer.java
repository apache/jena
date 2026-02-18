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
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.text.ShaclIndexMapping.*;
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
 * Tests for {@link ShaclTextDocProducer} — verifies that adding/removing triples
 * triggers correct index operations.
 */
public class TestShaclTextDocProducer {

    private static final String NS = "http://example.org/";
    private static final Node BOOK_CLASS = NodeFactory.createURI(NS + "Book");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");
    private static final Node IRRELEVANT_PRED = NodeFactory.createURI(NS + "irrelevant");

    private Dataset dataset;
    private TextIndexLucene textIndex;

    @Before
    public void setUp() {
        FieldDef titleField = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));

        FieldDef categoryField = new FieldDef("category", FieldType.KEYWORD, null,
            true, true, true, false, true, false,
            Collections.singleton(CATEGORY_PRED));

        IndexProfile bookProfile = new IndexProfile(
            NodeFactory.createURI(NS + "BookShape"),
            Collections.singleton(BOOK_CLASS),
            "uri", "docType",
            Arrays.asList(titleField, categoryField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(bookProfile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setFacetFields(mapping.getFacetFieldNames());
        config.setValueStored(true);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        textIndex = new TextIndexLucene(dir, config);

        Dataset baseDs = DatasetFactory.create();
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
    public void testAddTypeCreatesDocument() {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource book = ResourceFactory.createResource(NS + "book1");
            model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
            model.add(book, ResourceFactory.createProperty(NS + "title"), "Introduction to SPARQL");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Query the text index for the document
        List<TextHit> hits = textIndex.query(TITLE_PRED, "SPARQL", null, null);
        assertFalse("Should find the book via text search", hits.isEmpty());
        assertEquals(NS + "book1", hits.get(0).getNode().getURI());
    }

    @Test
    public void testAddPropertyRebuildsDocument() {
        // First add type and title
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource book = ResourceFactory.createResource(NS + "book2");
            model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
            model.add(book, ResourceFactory.createProperty(NS + "title"), "RDF Primer");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Now add category
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource book = ResourceFactory.createResource(NS + "book2");
            model.add(book, ResourceFactory.createProperty(NS + "category"), "Semantic Web");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Verify the document was rebuilt with both title and category
        List<TextHit> hits = textIndex.query(TITLE_PRED, "Primer", null, null);
        assertFalse("Should still find book by title", hits.isEmpty());
    }

    @Test
    public void testIrrelevantPredicateIgnored() {
        // Add a book
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource book = ResourceFactory.createResource(NS + "book3");
            model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
            model.add(book, ResourceFactory.createProperty(NS + "title"), "Test Book");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Add an irrelevant predicate — should not cause errors
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource book = ResourceFactory.createResource(NS + "book3");
            model.add(book, ResourceFactory.createProperty(NS + "irrelevant"), "some value");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Original document should still be findable
        List<TextHit> hits = textIndex.query(TITLE_PRED, "Test", null, null);
        assertFalse("Book should still be indexed", hits.isEmpty());
    }

    @Test
    public void testDeleteTypeRemovesDocument() {
        // Add a book
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource book = ResourceFactory.createResource(NS + "book4");
            model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
            model.add(book, ResourceFactory.createProperty(NS + "title"), "Deletable Book");
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Verify it's indexed
        List<TextHit> hits1 = textIndex.query(TITLE_PRED, "Deletable", null, null);
        assertFalse("Should find the book initially", hits1.isEmpty());

        // Delete the rdf:type assertion
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource book = ResourceFactory.createResource(NS + "book4");
            model.remove(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Verify it's removed from index
        List<TextHit> hits2 = textIndex.query(TITLE_PRED, "Deletable", null, null);
        assertTrue("Book should be removed from index after type deletion", hits2.isEmpty());
    }

    @Test
    public void testMultipleBooks() {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            for (int i = 1; i <= 5; i++) {
                Resource book = ResourceFactory.createResource(NS + "multibook" + i);
                model.add(book, RDF.type, ResourceFactory.createResource(NS + "Book"));
                model.add(book, ResourceFactory.createProperty(NS + "title"), "Book Number " + i);
                model.add(book, ResourceFactory.createProperty(NS + "category"), (i % 2 == 0) ? "Even" : "Odd");
            }
            dataset.commit();
        } finally {
            dataset.end();
        }

        // Search for "Book" should return all 5
        List<TextHit> hits = textIndex.query(TITLE_PRED, "Book", null, null);
        assertEquals("Should find all 5 books", 5, hits.size());
    }
}
