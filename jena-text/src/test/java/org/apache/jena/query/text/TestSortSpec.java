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

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.text.ShaclIndexMapping.FieldDef;
import org.apache.jena.query.text.ShaclIndexMapping.FieldType;
import org.apache.jena.query.text.ShaclIndexMapping.IndexProfile;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.Test;

/**
 * Tests for sort specification parsing and Lucene Sort construction.
 */
public class TestSortSpec {

    @Test
    public void testParseSingleAsc() {
        List<SortSpec> specs = SortSpecParser.parse("{\"field\":\"year\"}");
        assertEquals(1, specs.size());
        assertEquals("year", specs.get(0).field());
        assertFalse(specs.get(0).descending());
    }

    @Test
    public void testParseSingleDesc() {
        List<SortSpec> specs = SortSpecParser.parse("{\"field\":\"year\",\"order\":\"desc\"}");
        assertEquals(1, specs.size());
        assertEquals("year", specs.get(0).field());
        assertTrue(specs.get(0).descending());
    }

    @Test
    public void testParseMulti() {
        List<SortSpec> specs = SortSpecParser.parse(
            "[{\"field\":\"year\",\"order\":\"desc\"},{\"field\":\"title\"}]");
        assertEquals(2, specs.size());
        assertEquals("year", specs.get(0).field());
        assertTrue(specs.get(0).descending());
        assertEquals("title", specs.get(1).field());
        assertFalse(specs.get(1).descending());
    }

    @Test
    public void testToCanonical() {
        SortSpec asc = new SortSpec("year", false);
        assertEquals("year:asc", asc.toCanonical());

        SortSpec desc = new SortSpec("year", true);
        assertEquals("year:desc", desc.toCanonical());
    }

    @Test
    public void testIsSortSpec() {
        assertTrue(SortSpecParser.isSortSpec("{\"field\":\"year\"}"));
        assertFalse(SortSpecParser.isSortSpec("{\"op\":\"=\"}"));
    }

    @Test
    public void testBuildLuceneSort() {
        FieldDef yearField = new FieldDef("year", FieldType.INT, null,
            true, true, false, true, false, false, Collections.emptySet());
        FieldDef stateField = new FieldDef("state", FieldType.KEYWORD, null,
            true, true, true, true, false, false, Collections.emptySet());

        IndexProfile profile = new IndexProfile(
            NodeFactory.createURI("http://example.org/Shape"),
            Collections.singleton(NodeFactory.createURI("http://example.org/Thing")),
            "uri", "docType",
            Arrays.asList(yearField, stateField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(profile));
        EntityDefinition defn = org.apache.jena.query.text.assembler.ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        TextIndexLucene textIndex = new TextIndexLucene(dir, config);

        List<SortSpec> specs = List.of(
            new SortSpec("year", true),
            new SortSpec("state", false)
        );

        Sort sort = textIndex.buildLuceneSort(specs);
        assertNotNull(sort);
        SortField[] fields = sort.getSort();
        assertEquals(2, fields.length);
        assertEquals("year", fields[0].getField());
        assertEquals(SortField.Type.INT, fields[0].getType());
        assertTrue(fields[0].getReverse());
        assertEquals("state", fields[1].getField());
        assertEquals(SortField.Type.STRING, fields[1].getType());
        assertFalse(fields[1].getReverse());

        textIndex.close();
    }

    @Test
    public void testBuildLuceneSortNull() {
        FieldDef yearField = new FieldDef("year", FieldType.INT, null,
            true, true, false, true, false, false, Collections.emptySet());

        IndexProfile profile = new IndexProfile(
            NodeFactory.createURI("http://example.org/Shape"),
            Collections.singleton(NodeFactory.createURI("http://example.org/Thing")),
            "uri", "docType",
            Collections.singletonList(yearField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(profile));
        EntityDefinition defn = org.apache.jena.query.text.assembler.ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        TextIndexLucene textIndex = new TextIndexLucene(dir, config);

        assertNull(textIndex.buildLuceneSort(null));
        assertNull(textIndex.buildLuceneSort(Collections.emptyList()));

        textIndex.close();
    }

    @Test(expected = TextIndexException.class)
    public void testBuildLuceneSortTextFieldThrows() {
        FieldDef titleField = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true, Collections.emptySet());

        IndexProfile profile = new IndexProfile(
            NodeFactory.createURI("http://example.org/Shape"),
            Collections.singleton(NodeFactory.createURI("http://example.org/Thing")),
            "uri", "docType",
            Collections.singletonList(titleField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(profile));
        EntityDefinition defn = org.apache.jena.query.text.assembler.ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        TextIndexLucene textIndex = new TextIndexLucene(dir, config);

        try {
            textIndex.buildLuceneSort(List.of(new SortSpec("title", false)));
        } finally {
            textIndex.close();
        }
    }
}
