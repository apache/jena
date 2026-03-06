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
import org.apache.jena.query.text.ShaclIndexMapping.FieldDef;
import org.apache.jena.query.text.ShaclIndexMapping.FieldType;
import org.apache.jena.query.text.ShaclIndexMapping.IndexProfile;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that {@link ShaclTextIndexLucene#docFromMapping(Entity, IndexProfile)} produces
 * correct Lucene field types for TEXT, KEYWORD, INT, LONG, DOUBLE fields.
 */
public class TestShaclDocumentBuilding {

    private static final String NS = "http://example.org/";
    private static final Node BOOK_CLASS = NodeFactory.createURI(NS + "Book");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");
    private static final Node YEAR_PRED = NodeFactory.createURI(NS + "year");
    private static final Node PAGES_PRED = NodeFactory.createURI(NS + "pages");
    private static final Node RATING_PRED = NodeFactory.createURI(NS + "rating");

    private ShaclTextIndexLucene textIndex;
    private IndexProfile testProfile;

    @Before
    public void setUp() {
        FieldDef titleField = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));

        FieldDef categoryField = new FieldDef("category", FieldType.KEYWORD, null,
            true, true, true, true, false, false,
            Collections.singleton(CATEGORY_PRED));

        FieldDef yearField = new FieldDef("year", FieldType.INT, null,
            true, true, false, true, false, false,
            Collections.singleton(YEAR_PRED));

        FieldDef pagesField = new FieldDef("pages", FieldType.LONG, null,
            true, true, false, false, false, false,
            Collections.singleton(PAGES_PRED));

        FieldDef ratingField = new FieldDef("rating", FieldType.DOUBLE, null,
            true, true, false, true, false, false,
            Collections.singleton(RATING_PRED));

        testProfile = new IndexProfile(
            NodeFactory.createURI(NS + "BookShape"),
            Collections.singleton(BOOK_CLASS),
            "uri", "docType",
            Arrays.asList(titleField, categoryField, yearField, pagesField, ratingField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(testProfile));

        // Build a minimal EntityDefinition for TextIndexLucene
        EntityDefinition defn = new EntityDefinition("uri", "title");
        defn.set("title", TITLE_PRED);
        defn.set("category", CATEGORY_PRED);
        defn.setLangField("lang");

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setFacetFields(Collections.singletonList("category"));

        textIndex = new ShaclTextIndexLucene(new ByteBuffersDirectory(), config);
    }

    @After
    public void tearDown() {
        if (textIndex != null) {
            textIndex.close();
        }
    }

    @Test
    public void testShaclModeEnabled() {
        assertTrue(textIndex.isShaclMode());
        assertNotNull(textIndex.getShaclMapping());
    }

    @Test
    public void testDocHasEntityUriField() {
        Entity entity = new Entity("http://example.org/book1", null);
        entity.put("title", "Test Book");

        Document doc = textIndex.docFromMapping(entity, testProfile);

        String uri = doc.get("uri");
        assertEquals("http://example.org/book1", uri);
    }

    @Test
    public void testDocHasDiscriminatorField() {
        Entity entity = new Entity("http://example.org/book1", null);
        entity.put("title", "Test Book");

        Document doc = textIndex.docFromMapping(entity, testProfile);

        String docType = doc.get("docType");
        assertEquals("Book", docType);
    }

    @Test
    public void testTextFieldType() {
        Entity entity = new Entity("http://example.org/book1", null);
        entity.put("title", "Test Book Title");

        Document doc = textIndex.docFromMapping(entity, testProfile);

        IndexableField titleField = doc.getField("title");
        assertNotNull("Should have title field", titleField);
        assertEquals("Test Book Title", titleField.stringValue());
    }

    @Test
    public void testKeywordFieldType() {
        Entity entity = new Entity("http://example.org/book1", null);
        entity.put("category", "Science");

        Document doc = textIndex.docFromMapping(entity, testProfile);

        // Should have StringField
        IndexableField catField = doc.getField("category");
        assertNotNull("Should have category field", catField);
        assertEquals("Science", catField.stringValue());

        // Should also have SortedSetDocValuesFacetField (facetable=true)
        // and SortedDocValuesField (sortable=true)
        IndexableField[] allCatFields = doc.getFields("category");
        assertTrue("Should have multiple category fields (string + facet + sort)",
            allCatFields.length >= 2);
    }

    @Test
    public void testIntFieldType() {
        Entity entity = new Entity("http://example.org/book1", null);
        entity.put("year", 2024);

        Document doc = textIndex.docFromMapping(entity, testProfile);

        // IntPoint for indexing
        IndexableField yearField = doc.getField("year");
        assertNotNull("Should have year field", yearField);

        // StoredField for retrieval (stored=true)
        IndexableField[] yearFields = doc.getFields("year");
        assertTrue("Should have IntPoint + StoredField + NumericDocValues (sortable)",
            yearFields.length >= 2);
    }

    @Test
    public void testLongFieldType() {
        Entity entity = new Entity("http://example.org/book1", null);
        entity.put("pages", 350L);

        Document doc = textIndex.docFromMapping(entity, testProfile);

        IndexableField[] pagesFields = doc.getFields("pages");
        assertTrue("Should have LongPoint + StoredField", pagesFields.length >= 2);
    }

    @Test
    public void testDoubleFieldType() {
        Entity entity = new Entity("http://example.org/book1", null);
        entity.put("rating", 4.5);

        Document doc = textIndex.docFromMapping(entity, testProfile);

        IndexableField[] ratingFields = doc.getFields("rating");
        assertTrue("Should have DoublePoint + StoredField + NumericDocValues",
            ratingFields.length >= 2);
    }

    @Test
    public void testMultiValuedField() {
        Entity entity = new Entity("http://example.org/book1", null);
        entity.addValue("title", "First Title");
        entity.addValue("title", "Second Title");

        Document doc = textIndex.docFromMapping(entity, testProfile);

        IndexableField[] titleFields = doc.getFields("title");
        assertTrue("Should have 2 title fields for multi-valued", titleFields.length >= 2);
    }

    @Test
    public void testNullFieldSkipped() {
        Entity entity = new Entity("http://example.org/book1", null);
        // Only set title, leave category/year/pages/rating null

        entity.put("title", "Test");
        Document doc = textIndex.docFromMapping(entity, testProfile);

        // Should not have category, year, pages, or rating fields
        assertNull("category should be null", doc.get("category"));
    }

    @Test
    public void testIntFromString() {
        // Numeric fields should handle String input (from RDF literal lexical form)
        Entity entity = new Entity("http://example.org/book1", null);
        entity.put("year", "2024");

        Document doc = textIndex.docFromMapping(entity, testProfile);

        IndexableField[] yearFields = doc.getFields("year");
        assertTrue("Should parse int from string", yearFields.length >= 2);
    }
}
