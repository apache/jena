/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Trie} class
 *
 */
public class TestTrie {

    /**
     * Add a single key value
     */
    @Test
    public void trie_add_01() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        assertTrue(trie.contains("test"));
        assertEquals((Integer) 123, trie.get("test"));
    }

    /**
     * Add two key values
     */
    @Test
    public void trie_add_02() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("other", 456);
        assertTrue(trie.contains("test"));
        assertTrue(trie.contains("other"));
        assertEquals((Integer) 123, trie.get("test"));
        assertEquals((Integer) 456, trie.get("other"));
    }

    /**
     * Replace an existing key value
     */
    @Test
    public void trie_add_03() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("test", 456);
        assertTrue(trie.contains("test"));
        assertEquals((Integer) 456, trie.get("test"));
    }

    /**
     * Adding a null value is ignored
     */
    @Test
    public void trie_add_04() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", null);
        assertFalse(trie.contains("test"));
    }

    /**
     * Adding a null key is permitted - provides access to root of Trie
     */
    @Test
    public void trie_add_05() {
        Trie<Integer> trie = new Trie<>();
        trie.add(null, 123);
        assertTrue(trie.contains(null));
        assertEquals((Integer)123, trie.get(null));
    }

    /**
     * Adding an empty key is permitted - provides access to root of Trie
     */
    @Test
    public void trie_add_06() {
        Trie<Integer> trie = new Trie<>();
        trie.add("", 123);
        assertTrue(trie.contains(""));
        assertEquals((Integer)123, trie.get(""));
    }

    /**
     * Test for non-existent key
     */
    @Test
    public void trie_contains_01() {
        Trie<Integer> trie = new Trie<>();
        assertFalse(trie.contains("test"));
    }

    /**
     * Test for keys with and without values required
     */
    @Test
    public void trie_contains_02() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        assertTrue(trie.contains("test"));
        assertTrue(trie.contains("test", true));
        assertTrue(trie.contains("test", 123));

        // Any prefix of an added key exists if we don't require it to have a
        // value
        assertFalse(trie.contains("t"));
        assertTrue(trie.contains("t", false));
    }

    /**
     * Test for non-existent null key
     */
    @Test
    public void trie_contains_03() {
        Trie<Integer> trie = new Trie<>();
        assertFalse(trie.contains(null));
        assertTrue(trie.contains(null, false));
    }

    /**
     * Test for empty key
     */
    @Test
    public void trie_contains_04() {
        Trie<Integer> trie = new Trie<>();
        assertFalse(trie.contains(""));
        assertTrue(trie.contains("", false));
    }

    /**
     * Removing a key value
     */
    @Test
    public void trie_remove_01() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        assertTrue(trie.contains("test"));
        assertEquals((Integer) 123, trie.get("test"));

        // Removing does not fully remove the key it merely nulls the value
        trie.remove("test");
        assertFalse(trie.contains("test"));
        assertTrue(trie.contains("test", false));
        assertNull(trie.get("test"));
    }

    /**
     * Removing a key value, removing a key which is a prefix of another leaves
     * the other intact
     */
    @Test
    public void trie_remove_02() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);
        assertTrue(trie.contains("test"));
        assertEquals((Integer) 123, trie.get("test"));
        assertTrue(trie.contains("testing"));
        assertEquals((Integer) 456, trie.get("testing"));

        // Removing does not fully remove the key it merely nulls the value
        trie.remove("test");
        assertFalse(trie.contains("test"));
        assertTrue(trie.contains("test", false));
        assertNull(trie.get("test"));

        // It also does not remove any keys who had the removed key as a prefix
        assertTrue(trie.contains("testing"));
        assertEquals((Integer) 456, trie.get("testing"));
    }

    /**
     * Test for removing null key - provides access to trie root
     */
    @Test
    public void trie_remove_03() {
        Trie<Integer> trie = new Trie<>();
        trie.add(null, 123);
        assertTrue(trie.contains(null));
        assertEquals((Integer)123, trie.get(null));

        trie.remove(null);
        assertFalse(trie.contains(null));
    }

    /**
     * Test for removing null key - provides access to trie root
     */
    @Test
    public void trie_remove_04() {
        Trie<Integer> trie = new Trie<>();
        trie.add("", 123);
        assertTrue(trie.contains(""));
        assertEquals((Integer)123, trie.get(""));

        trie.remove("");
        assertFalse(trie.contains(""));
    }

    @Test
    public void trie_clear_01() {
        Trie<Integer> trie = new Trie<>();
        trie.clear() ;
        assertTrue(trie.isEmpty()) ;
    }

    @Test
    public void trie_clear_02() {
        Trie<Integer> trie = new Trie<>();
        trie.add("", 123);
        trie.clear() ;
        assertFalse(trie.contains(""));
        assertTrue(trie.isEmpty()) ;
    }

    @Test
    public void trie_isEmpty_01() {
        Trie<Integer> trie = new Trie<>();
        assertTrue(trie.isEmpty()) ;
    }

    @Test
    public void trie_isEmpty_02() {
        Trie<Integer> trie = new Trie<>();
        trie.add("", 123);
        assertFalse(trie.isEmpty()) ;
    }

    @Test
    public void trie_isEmpty_03() {
        Trie<Integer> trie = new Trie<>();
        trie.add("x", 123);
        assertFalse(trie.isEmpty()) ;
    }

    @Test
    public void trie_isEmpty_04() {
        Trie<Integer> trie = new Trie<>();
        trie.add("xy", 123);
        assertFalse(trie.isEmpty()) ;
    }


    /**
     * Test prefix search
     */
    @Test
    public void trie_prefix_search_01() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);

        //Prefix search on "test" should return two values
        List<Integer> matches = trie.prefixSearch("test");
        assertEquals(2, matches.size());

        //Prefix search on "testi" should return one value
        matches = trie.prefixSearch("testi");
        assertEquals(1, matches.size());

        //Prefix search on "testingly" should return no values
        matches = trie.prefixSearch("testingly");
        assertEquals(0, matches.size());

        //Prefix search on null key should give two values
        matches = trie.prefixSearch(null);
        assertEquals(2, matches.size());
    }

    /**
     * Test partial search
     */
    @Test
    public void trie_partial_search_01() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);

        //Partial search on "test" should return one values
        List<Integer> matches = trie.partialSearch("test");
        assertEquals(1, matches.size());

        //Prefix search on "testi" should return one values
        matches = trie.partialSearch("testi");
        assertEquals(1, matches.size());

        //Prefix search on "testingly" should return two values
        matches = trie.partialSearch("testingly");
        assertEquals(2, matches.size());

        //Prefix search on null key should give no values
        matches = trie.partialSearch(null);
        assertEquals(0, matches.size());
    }

    /**
     * Test longest match
     */
    @Test
    public void trie_longest_match_01() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);
        assertTrue(trie.contains("test"));
        assertTrue(trie.contains("testing"));

        assertEquals((Integer)456, trie.longestMatch("testing"));
    }

    /**
     * Test longest match
     */
    @Test
    public void trie_longest_match_02() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);

        assertEquals((Integer)456, trie.longestMatch("testingly"));
    }

    /**
     * Test longest match
     */
    @Test
    public void trie_longest_match_03() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);
        trie.remove("testing");

        assertEquals((Integer)123, trie.longestMatch("testing"));
    }

    /**
     * Test shortest match
     */
    @Test
    public void trie_shortest_match_01() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);
        assertTrue(trie.contains("test"));
        assertTrue(trie.contains("testing"));

        assertEquals((Integer)123, trie.shortestMatch("testing"));
    }

    /**
     * Test shortest match
     */
    @Test
    public void trie_shortest_match_02() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);

        assertEquals((Integer)123, trie.shortestMatch("testingly"));
    }

    /**
     * Test shortest match
     */
    @Test
    public void trie_shortest_match_03() {
        Trie<Integer> trie = new Trie<>();
        trie.add("test", 123);
        trie.add("testing", 456);
        trie.remove("test");

        assertEquals((Integer)456, trie.shortestMatch("testing"));
    }
}
