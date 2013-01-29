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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
        Trie<Integer> trie = new Trie<Integer>();
        trie.add("test", 123);
        Assert.assertTrue(trie.contains("test"));
        Assert.assertEquals((Integer) 123, trie.get("test"));
    }

    /**
     * Add two key values
     */
    @Test
    public void trie_add_02() {
        Trie<Integer> trie = new Trie<Integer>();
        trie.add("test", 123);
        trie.add("other", 456);
        Assert.assertTrue(trie.contains("test"));
        Assert.assertTrue(trie.contains("other"));
        Assert.assertEquals((Integer) 123, trie.get("test"));
        Assert.assertEquals((Integer) 456, trie.get("other"));
    }

    /**
     * Replace an existing key value
     */
    @Test
    public void trie_add_03() {
        Trie<Integer> trie = new Trie<Integer>();
        trie.add("test", 123);
        trie.add("test", 456);
        Assert.assertTrue(trie.contains("test"));
        Assert.assertEquals((Integer) 456, trie.get("test"));
    }

    /**
     * Adding a null value is ignored
     */
    @Test
    public void trie_add_04() {
        Trie<Integer> trie = new Trie<Integer>();
        trie.add("test", null);
        Assert.assertFalse(trie.contains("test"));
    }
    
    /**
     * Test for non-existent key
     */
    public void trie_contains_01() {
        Trie<Integer> trie = new Trie<Integer>();
        Assert.assertFalse(trie.contains("test"));
    }

    /**
     * Test for keys with and without values required
     */
    @Test
    public void trie_contains_02() {
        Trie<Integer> trie = new Trie<Integer>();
        trie.add("test", 123);
        Assert.assertTrue(trie.contains("test"));
        Assert.assertTrue(trie.contains("test", true));
        Assert.assertTrue(trie.contains("test", 123));

        // Any prefix of an added key exists if we don't require it to have a
        // value
        Assert.assertFalse(trie.contains("t"));
        Assert.assertTrue(trie.contains("t", false));
    }

    /**
     * Removing a key value
     */
    @Test
    public void trie_remove_01() {
        Trie<Integer> trie = new Trie<Integer>();
        trie.add("test", 123);
        Assert.assertTrue(trie.contains("test"));
        Assert.assertEquals((Integer) 123, trie.get("test"));

        // Removing does not fully remove the key it merely nulls the value
        trie.remove("test");
        Assert.assertFalse(trie.contains("test"));
        Assert.assertTrue(trie.contains("test", false));
        Assert.assertNull(trie.get("test"));
    }

    /**
     * Removing a key value, removing a key which is a prefix of another leaves
     * the other intact
     */
    @Test
    public void trie_remove_02() {
        Trie<Integer> trie = new Trie<Integer>();
        trie.add("test", 123);
        trie.add("testing", 456);
        Assert.assertTrue(trie.contains("test"));
        Assert.assertEquals((Integer) 123, trie.get("test"));
        Assert.assertTrue(trie.contains("testing"));
        Assert.assertEquals((Integer) 456, trie.get("testing"));

        // Removing does not fully remove the key it merely nulls the value
        trie.remove("test");
        Assert.assertFalse(trie.contains("test"));
        Assert.assertTrue(trie.contains("test", false));
        Assert.assertNull(trie.get("test"));

        // It also does not remove any keys who had the removed key as a prefix
        Assert.assertTrue(trie.contains("testing"));
        Assert.assertEquals((Integer) 456, trie.get("testing"));
    }
    
    /**
     * Test prefix search
     */
    @Test
    public void trie_prefix_search_01() {
        Trie<Integer> trie = new Trie<Integer>();
        trie.add("test", 123);
        trie.add("testing", 456);
        
        //Prefix search on "test" should return two values
        List<Integer> matches = trie.prefixSearch("test");
        Assert.assertEquals(2, matches.size());
        
        //Prefix search on "testi" should return one value
        matches = trie.prefixSearch("testi");
        Assert.assertEquals(1, matches.size());
        
        //Prefix search on "testingly" should return no values
        matches = trie.prefixSearch("testingly");
        Assert.assertEquals(0, matches.size());
    }
}
