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

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

/**
 * Tests for SearchExecution key generation and caching logic.
 */
public class TestSearchExecution {

    @Test
    public void testBuildKeyConsistentOrder() {
        List<Resource> props1 = Arrays.asList(
            ResourceFactory.createResource("http://example.org/b"),
            ResourceFactory.createResource("http://example.org/a")
        );
        List<Resource> props2 = Arrays.asList(
            ResourceFactory.createResource("http://example.org/a"),
            ResourceFactory.createResource("http://example.org/b")
        );

        String key1 = SearchExecution.buildKey(props1, "test query", null);
        String key2 = SearchExecution.buildKey(props2, "test query", null);

        assertEquals("Key should be the same regardless of property order", key1, key2);
    }

    @Test
    public void testBuildKeyWithFilters() {
        Map<String, List<String>> filters1 = new LinkedHashMap<>();
        filters1.put("category", Arrays.asList("B", "A"));
        filters1.put("author", Arrays.asList("Smith"));

        Map<String, List<String>> filters2 = new LinkedHashMap<>();
        filters2.put("author", Arrays.asList("Smith"));
        filters2.put("category", Arrays.asList("A", "B"));

        String key1 = SearchExecution.buildKey(null, "test", filters1);
        String key2 = SearchExecution.buildKey(null, "test", filters2);

        assertEquals("Key should be the same regardless of filter order", key1, key2);
    }

    @Test
    public void testBuildKeyDifferentQueries() {
        String key1 = SearchExecution.buildKey(null, "query1", null);
        String key2 = SearchExecution.buildKey(null, "query2", null);

        assertNotEquals("Different queries should produce different keys", key1, key2);
    }

    @Test
    public void testBuildKeyDifferentFilters() {
        Map<String, List<String>> filters1 = new LinkedHashMap<>();
        filters1.put("category", Arrays.asList("Technology"));

        Map<String, List<String>> filters2 = new LinkedHashMap<>();
        filters2.put("category", Arrays.asList("Science"));

        String key1 = SearchExecution.buildKey(null, "test", filters1);
        String key2 = SearchExecution.buildKey(null, "test", filters2);

        assertNotEquals("Different filters should produce different keys", key1, key2);
    }

    @Test
    public void testBuildKeyNullProps() {
        String key1 = SearchExecution.buildKey(null, "test", null);
        String key2 = SearchExecution.buildKey(new ArrayList<>(), "test", null);

        assertEquals("Null and empty props should produce the same key", key1, key2);
    }

    @Test
    public void testBuildKeyNullQuery() {
        String key = SearchExecution.buildKey(null, null, null);
        assertNotNull(key);
        assertTrue(key.contains("|qs="));
    }
}
