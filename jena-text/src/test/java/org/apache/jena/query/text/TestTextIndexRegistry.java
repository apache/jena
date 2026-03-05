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

import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for TextIndexRegistry multi-index support.
 */
public class TestTextIndexRegistry {

    private TextIndexLucene index1;
    private TextIndexLucene index2;

    @Before
    public void setUp() {
        EntityDefinition defn1 = new EntityDefinition("uri", "text");
        TextIndexConfig config1 = new TextIndexConfig(defn1);
        index1 = new TextIndexLucene(new ByteBuffersDirectory(), config1);

        EntityDefinition defn2 = new EntityDefinition("uri", "text");
        TextIndexConfig config2 = new TextIndexConfig(defn2);
        index2 = new TextIndexLucene(new ByteBuffersDirectory(), config2);
    }

    @After
    public void tearDown() {
        index1.close();
        index2.close();
    }

    @Test
    public void testSingleRegistry() {
        TextIndexRegistry reg = TextIndexRegistry.single(index1);
        assertEquals(1, reg.size());
        assertSame(index1, reg.getDefault());
        assertSame(index1, reg.get("default"));
    }

    @Test
    public void testMultiRegistry() {
        TextIndexRegistry reg = new TextIndexRegistry();
        reg.register("reports", index1);
        reg.register("ocr", index2);

        assertEquals(2, reg.size());
        assertSame(index1, reg.get("reports"));
        assertSame(index2, reg.get("ocr"));
        // First registered is default
        assertSame(index1, reg.getDefault());
        assertEquals("reports", reg.getDefaultId());
    }

    @Test(expected = TextIndexException.class)
    public void testGetNonexistent() {
        TextIndexRegistry reg = TextIndexRegistry.single(index1);
        reg.get("nonexistent");
    }

    @Test(expected = TextIndexException.class)
    public void testGetDefaultEmpty() {
        TextIndexRegistry reg = new TextIndexRegistry();
        reg.getDefault();
    }

    @Test
    public void testAllWithIds() {
        TextIndexRegistry reg = new TextIndexRegistry();
        reg.register("a", index1);
        reg.register("b", index2);

        var map = reg.allWithIds();
        assertEquals(2, map.size());
        assertSame(index1, map.get("a"));
        assertSame(index2, map.get("b"));
    }
}
