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
import org.apache.jena.query.text.ShaclIndexMapping.*;
import org.junit.Test;

/**
 * Unit tests for {@link ShaclIndexMapping} — predicate/class lookups, field types.
 */
public class TestShaclIndexMapping {

    private static final String NS = "http://example.org/";
    private static final Node BOOK_CLASS = NodeFactory.createURI(NS + "Book");
    private static final Node ARTICLE_CLASS = NodeFactory.createURI(NS + "Article");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node LABEL_PRED = NodeFactory.createURI(NS + "label");
    private static final Node CATEGORY_PRED = NodeFactory.createURI(NS + "category");
    private static final Node YEAR_PRED = NodeFactory.createURI(NS + "year");
    private static final Node IRRELEVANT_PRED = NodeFactory.createURI(NS + "irrelevant");

    private ShaclIndexMapping createTestMapping() {
        FieldDef titleField = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            new LinkedHashSet<>(Arrays.asList(TITLE_PRED, LABEL_PRED)));

        FieldDef categoryField = new FieldDef("category", FieldType.KEYWORD, null,
            true, true, true, false, true, false,
            Collections.singleton(CATEGORY_PRED));

        FieldDef yearField = new FieldDef("year", FieldType.INT, null,
            true, true, false, true, false, false,
            Collections.singleton(YEAR_PRED));

        IndexProfile bookProfile = new IndexProfile(
            NodeFactory.createURI(NS + "BookShape"),
            Collections.singleton(BOOK_CLASS),
            "uri", "docType",
            Arrays.asList(titleField, categoryField, yearField));

        // Second profile — Article has title only
        FieldDef articleTitle = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));

        IndexProfile articleProfile = new IndexProfile(
            NodeFactory.createURI(NS + "ArticleShape"),
            Collections.singleton(ARTICLE_CLASS),
            "uri", "docType",
            Collections.singletonList(articleTitle));

        return new ShaclIndexMapping(Arrays.asList(bookProfile, articleProfile));
    }

    @Test
    public void testPredicateLookup() {
        ShaclIndexMapping mapping = createTestMapping();

        assertTrue(mapping.isRelevantPredicate(TITLE_PRED));
        assertTrue(mapping.isRelevantPredicate(LABEL_PRED));
        assertTrue(mapping.isRelevantPredicate(CATEGORY_PRED));
        assertTrue(mapping.isRelevantPredicate(YEAR_PRED));
        assertFalse(mapping.isRelevantPredicate(IRRELEVANT_PRED));
    }

    @Test
    public void testPredicateReturnsCorrectProfiles() {
        ShaclIndexMapping mapping = createTestMapping();

        // TITLE_PRED should match both Book and Article profiles
        List<ProfileField> titleMatches = mapping.getProfilesForPredicate(TITLE_PRED);
        assertEquals(2, titleMatches.size());

        // LABEL_PRED should match only Book profile (via alternativePath)
        List<ProfileField> labelMatches = mapping.getProfilesForPredicate(LABEL_PRED);
        assertEquals(1, labelMatches.size());
        assertEquals("title", labelMatches.get(0).getField().getFieldName());

        // CATEGORY_PRED should match only Book
        List<ProfileField> catMatches = mapping.getProfilesForPredicate(CATEGORY_PRED);
        assertEquals(1, catMatches.size());
        assertEquals("category", catMatches.get(0).getField().getFieldName());
    }

    @Test
    public void testClassLookup() {
        ShaclIndexMapping mapping = createTestMapping();

        List<IndexProfile> bookProfiles = mapping.getProfilesForClass(BOOK_CLASS);
        assertEquals(1, bookProfiles.size());

        List<IndexProfile> articleProfiles = mapping.getProfilesForClass(ARTICLE_CLASS);
        assertEquals(1, articleProfiles.size());

        List<IndexProfile> unknownProfiles = mapping.getProfilesForClass(IRRELEVANT_PRED);
        assertTrue(unknownProfiles.isEmpty());
    }

    @Test
    public void testIrrelevantPredicateReturnsEmpty() {
        ShaclIndexMapping mapping = createTestMapping();

        List<ProfileField> results = mapping.getProfilesForPredicate(IRRELEVANT_PRED);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetFacetFieldNames() {
        ShaclIndexMapping mapping = createTestMapping();

        List<String> facetFields = mapping.getFacetFieldNames();
        assertEquals(1, facetFields.size());
        assertTrue(facetFields.contains("category"));
    }

    @Test
    public void testFieldDefDefaults() {
        // Test default field type
        FieldDef field = new FieldDef("test", null, null,
            true, true, false, false, false, false,
            Collections.singleton(TITLE_PRED));
        assertEquals(FieldType.TEXT, field.getFieldType());
    }

    @Test
    public void testProfileDefaults() {
        FieldDef field = new FieldDef("test", FieldType.TEXT, null,
            true, true, false, false, false, false,
            Collections.singleton(TITLE_PRED));
        IndexProfile profile = new IndexProfile(
            NodeFactory.createURI(NS + "TestShape"),
            Collections.singleton(BOOK_CLASS),
            null, null,
            Collections.singletonList(field));

        assertEquals("uri", profile.getDocIdField());
        assertEquals("docType", profile.getDiscriminatorField());
    }

    @Test
    public void testProfileCount() {
        ShaclIndexMapping mapping = createTestMapping();
        assertEquals(2, mapping.getProfiles().size());
    }

    @Test
    public void testGetDefaultSearchFieldNames() {
        ShaclIndexMapping mapping = createTestMapping();
        List<String> defaults = mapping.getDefaultSearchFieldNames();
        assertTrue("title should be a default search field", defaults.contains("title"));
    }

    @Test
    public void testFieldIRIAutoGenerated() {
        ShaclIndexMapping mapping = createTestMapping();
        FieldDef titleField = mapping.findField("urn:jena:lucene:field#title");
        assertNotNull(titleField.getFieldIRI());
        assertTrue(titleField.getFieldIRI().isURI());
        assertEquals("urn:jena:lucene:field#title", titleField.getFieldIRI().getURI());
    }

    @Test
    public void testFieldIRIExplicit() {
        Node customIRI = NodeFactory.createURI(NS + "myCustomField");
        FieldDef field = new FieldDef("test", FieldType.TEXT, null,
            true, true, false, false, false, false,
            Collections.singleton(TITLE_PRED), null, customIRI);
        assertEquals(customIRI, field.getFieldIRI());
    }

    @Test
    public void testGetAllFieldNames() {
        ShaclIndexMapping mapping = createTestMapping();
        Set<String> all = mapping.getAllFieldNames();
        assertTrue(all.contains("title"));
        assertTrue(all.contains("category"));
        assertTrue(all.contains("year"));
    }

    @Test(expected = TextIndexException.class)
    public void testConflictingFieldTypes() {
        // Same field name with different types should throw
        FieldDef textTitle = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));
        IndexProfile p1 = new IndexProfile(
            NodeFactory.createURI(NS + "Shape1"),
            Collections.singleton(BOOK_CLASS),
            "uri", "docType",
            Collections.singletonList(textTitle));

        FieldDef keywordTitle = new FieldDef("title", FieldType.KEYWORD, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));
        IndexProfile p2 = new IndexProfile(
            NodeFactory.createURI(NS + "Shape2"),
            Collections.singleton(ARTICLE_CLASS),
            "uri", "docType",
            Collections.singletonList(keywordTitle));

        new ShaclIndexMapping(Arrays.asList(p1, p2));
    }
}
