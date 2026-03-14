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

import org.apache.jena.query.text.cql.CqlExpression;
import org.junit.Test;

/**
 * Tests for SearchExecution key generation and caching logic.
 */
public class TestSearchExecution {

    @Test
    public void testBuildKeyConsistentOrder() {
        List<String> fields1 = Arrays.asList("description", "title");
        List<String> fields2 = Arrays.asList("title", "description");

        String key1 = SearchExecution.buildKey(fields1, "test query", null, null);
        String key2 = SearchExecution.buildKey(fields2, "test query", null, null);

        assertEquals("Key should be the same regardless of field order", key1, key2);
    }

    @Test
    public void testBuildKeyWithCqlFilter() {
        CqlExpression filter1 = new CqlExpression.CqlComparison("=", "state", "WA");
        CqlExpression filter2 = new CqlExpression.CqlComparison("=", "state", "WA");

        String key1 = SearchExecution.buildKey(null, "test", filter1, null);
        String key2 = SearchExecution.buildKey(null, "test", filter2, null);

        assertEquals("Same CQL filter should produce same key", key1, key2);
    }

    @Test
    public void testBuildKeyDifferentQueries() {
        String key1 = SearchExecution.buildKey(null, "query1", null, null);
        String key2 = SearchExecution.buildKey(null, "query2", null, null);

        assertNotEquals("Different queries should produce different keys", key1, key2);
    }

    @Test
    public void testBuildKeyDifferentFields() {
        String key1 = SearchExecution.buildKey(List.of("title"), "test", null, null);
        String key2 = SearchExecution.buildKey(List.of("description"), "test", null, null);

        assertNotEquals("Different fields should produce different keys", key1, key2);
    }

    @Test
    public void testBuildKeyWithSortSpecs() {
        List<SortSpec> sort1 = List.of(new SortSpec("year", true));
        List<SortSpec> sort2 = List.of(new SortSpec("year", false));

        String key1 = SearchExecution.buildKey(null, "test", null, sort1);
        String key2 = SearchExecution.buildKey(null, "test", null, sort2);

        assertNotEquals("Different sort specs should produce different keys", key1, key2);
    }

    @Test
    public void testBuildKeyNullFields() {
        String key1 = SearchExecution.buildKey(null, "test", null, null);
        String key2 = SearchExecution.buildKey(new ArrayList<>(), "test", null, null);

        assertEquals("Null and empty fields should produce the same key", key1, key2);
    }

    @Test
    public void testBuildKeyNullQuery() {
        String key = SearchExecution.buildKey(null, null, null, null);
        assertNotNull(key);
        assertTrue(key.contains("|qs="));
    }

    @Test
    public void testBuildKeyCqlAndOrder() {
        // CQL AND with different orderings should produce same canonical key
        CqlExpression a = new CqlExpression.CqlComparison("=", "state", "WA");
        CqlExpression b = new CqlExpression.CqlComparison("=", "type", "report");

        CqlExpression and1 = new CqlExpression.CqlAnd(List.of(a, b));
        CqlExpression and2 = new CqlExpression.CqlAnd(List.of(b, a));

        String key1 = SearchExecution.buildKey(null, "test", and1, null);
        String key2 = SearchExecution.buildKey(null, "test", and2, null);

        assertEquals("AND with different arg order should produce same key", key1, key2);
    }
}
