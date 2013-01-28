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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of a classic Trie, this is a mapping from strings to some
 * value type optimized for fast prefix search and match. If you do not need
 * prefix search then you should typically use a standard {@link Map} instead
 * 
 * @param <T>
 *            Value type
 */
public class Trie<T> {

    private TrieNode root = new TrieNode(null);

    /**
     * Adds a value to the trie
     * <p>
     * Note that a null value is treated as if the key does not actually exist
     * in the tree so trying to add a null is a no-op
     * </p>
     * 
     * @param key
     *            Key
     * @param value
     *            Value
     */
    public void add(String key, T value) {
        if (value == null)
            return;
        TrieNode n = this.moveToNode(key);
        n.setValue(value);
    }

    /**
     * Move to a node creating new nodes if necessary
     * 
     * @param key
     *            Key
     * @return Node
     */
    private TrieNode moveToNode(String key) {
        TrieNode current = this.root;
        for (int i = 0; i < key.length(); i++) {
            current = current.moveToChild(key.charAt(i));
        }
        return current;
    }

    /**
     * Try to find a node in the trie
     * 
     * @param key
     *            Key
     * @return Node or null if not found
     */
    private TrieNode find(String key) {
        TrieNode current = this.root;
        for (int i = 0; i < key.length(); i++) {
            current = current.getChild(key.charAt(i));
            if (current == null)
                break;
        }
        return current;
    }

    /**
     * Removes a value from the trie
     * <p>
     * This doesn't actually remove the key per-se rather sets the value
     * associated with the key to null.
     * </p>
     * 
     * @param key
     *            Key
     */
    public void remove(String key) {
        TrieNode n = this.find(key);
        if (n != null) {
            n.setValue(null);
        }
    }

    /**
     * Gets whether a key exists in the trie and has a non-null value mapped
     * to it
     * 
     * @param key
     *            Key
     * @return True if the key exists and has a non-null value mapped to it
     */
    public boolean contains(String key) {
        return this.contains(key, true);
    }

    /**
     * Gets whether a key exists in the trie and meets the given value
     * criteria
     * 
     * @param key
     *            Key
     * @param requireValue
     *            If true a key must have a non-null value associated with it to
     *            be considered to be contained in the tree, if false then the
     *            key must merely map to a node in the trie
     * @return True if the key exists and the value criteria is met, false
     *         otherwise
     */
    public boolean contains(String key, boolean requireValue) {
        TrieNode n = this.find(key);
        if (n == null)
            return false;
        if (requireValue) {
            return n.hasValue();
        } else {
            return true;
        }
    }
    
    /**
     * Gets whether a key value pair are present in the trie
     * @param key Key
     * @param value Value
     * @return True if the key value pair exists in the trie, false otherwise
     */
    public boolean contains(String key, T value) {
        TrieNode n = this.find(key);
        if (n == null) return false;
        if (value == null && !n.hasValue()) return true;
        return value.equals(n.getValue());
    }
    
    /**
     * Gets the value associated with a key
     * @param key Key
     * @return Value
     */
    public T get(String key) {
        TrieNode n = this.find(key);
        if (n == null) return null;
        return n.getValue();
    }
    
    /**
     * Performs a prefix search and returns all values mapped under the given prefix
     * @param prefix Prefix
     * @return List of values associated with the given key
     */
    public List<T> prefixSearch(String prefix) {
        TrieNode n = this.find(prefix);
        if (n == null) return new ArrayList<T>();
        return Collections.unmodifiableList(n.getValues());
    }

    /**
     * Represents a node in the Trie
     * 
     */
     private class TrieNode {
        private Map<Character, TrieNode> children = new HashMap<Character, TrieNode>();
        private T value;

        /**
         * Creates a Trie Node
         * 
         * @param value
         *            Value
         */
        public TrieNode(T value) {
            this.value = value;
        }

        /**
         * Gets the value
         * 
         * @return Value
         */
        public T getValue() {
            return this.value;
        }

        /**
         * Sets the value
         * 
         * @param value
         *            Value
         */
        public void setValue(T value) {
            this.value = value;
        }

        /**
         * Returns whether a non-null value is associated with this node
         * 
         * @return True if there is a non-null value, false otherwise
         */
        public boolean hasValue() {
            return this.value != null;
        }

        /**
         * Gets the child (if it exists)
         * 
         * @param c
         *            Character to move to
         * @return Child
         */
        public TrieNode getChild(Character c) {
            return this.children.get(c);
        }

        /**
         * Moves to a child (creating a new node if necessary)
         * 
         * @param c
         *            Character to move to
         * @return Child
         */
        public TrieNode moveToChild(Character c) {
            TrieNode n = this.children.get(c);
            if (n == null) {
                n = new TrieNode(null);
                this.children.put(c, n);
            }
            return n;
        }
        
        /**
         * Gets all values from a given node and its descendants
         * @return Values
         */
        public List<T> getValues() {
            List<T> values = new ArrayList<T>();
            if (this.hasValue()) {
                values.add(this.value);
            }
            for (TrieNode child : this.children.values()) {
                values.addAll(child.getValues());
            }
            return values;
        }
    }
}
