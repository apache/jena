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
 * prefix search then you should typically use a standard {@link Map} instead.
 * <p>
 * The empty or null string may be used as a special key to refer to the root
 * node of the trie.
 * </p>
 * <p>
 * A Trie cannot store null values since null is used as an internal marker to
 * indicate that there is no value associated with a key. This is necessary
 * because the nature of the data structure means that adding a key potentially
 * adds multiple keys many of which will not be associated with a value.
 * </p>
 * 
 * @param <T>
 *            Type of the value stored.
 */
public final class Trie<T> {

    private TrieNode<T> root = new TrieNode<>(null);

    /**
     * Adds a value to the trie overwriting any existing value
     * <p>
     * Note that a null value is treated as if the key does not actually exist
     * in the tree so trying to add a null is a no-op. If you want to remove a
     * key use the {@link #remove(String)} method instead.
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
        TrieNode<T> n = this.moveToNode(key);
        n.setValue(value);
    }

    /**
     * Move to a node creating new nodes if necessary
     * 
     * @param key
     *            Key
     * @return Node
     */
    private TrieNode<T> moveToNode(String key) {
        TrieNode<T> current = this.root;
        if (key == null)
            return current;
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
    private TrieNode<T> find(String key) {
        TrieNode<T> current = this.root;
        if (key == null)
            return current;
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
        TrieNode<T> n = this.find(key);
        if (n != null) {
            n.setValue(null);
        }
    }

    /**
     * Gets whether a key exists in the trie and has a non-null value mapped to
     * it
     * 
     * @param key
     *            Key
     * @return True if the key exists and has a non-null value mapped to it
     */
    public boolean contains(String key) {
        return this.contains(key, true);
    }

    /**
     * Gets whether a key exists in the trie and meets the given value criteria
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
        TrieNode<T> n = this.find(key);
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
     * 
     * @param key
     *            Key
     * @param value
     *            Value
     * @return True if the key value pair exists in the trie, false otherwise
     */
    public boolean contains(String key, T value) {
        TrieNode<T> n = this.find(key);
        if (n == null)
            return false;
        if (value == null && !n.hasValue())
            return true;
        return value.equals(n.getValue());
    }

    /**
     * Gets the value associated with a key
     * 
     * @param key
     *            Key
     * @return Value
     */
    public T get(String key) {
        TrieNode<T> n = this.find(key);
        if (n == null)
            return null;
        return n.getValue();
    }

    /**
     * Performs a prefix search and returns all values mapped under the given
     * prefix. The entirety of the prefix must be matches, if you only want part
     * of the prefix to be matched use the {@link #partialSearch(String)} method
     * instead.
     * 
     * @param prefix
     *            Prefix
     * @return List of values associated with the given key
     */
    public List<T> prefixSearch(String prefix) {
        TrieNode<T> n = this.find(prefix);
        if (n == null)
            return new ArrayList<>();
        return Collections.unmodifiableList(n.getValues());
    }

    /**
     * Performs a search and returns any value associated with any partial or
     * whole prefix of the key
     * 
     * @param key
     *            Key
     * @return List of values associated with any partial prefix of the key
     */
    public List<T> partialSearch(String key) {
        List<T> values = new ArrayList<>();
        TrieNode<T> current = this.root;
        if (key == null) {
            if (current.hasValue())
                values.add(current.getValue());
        } else {
            for (int i = 0; i < key.length(); i++) {
                if (current.hasValue())
                    values.add(current.getValue());
                current = current.getChild(key.charAt(i));
                if (current == null)
                    return Collections.unmodifiableList(values);
            }
            
            // If we reach here current is the complete key match
            // so make sure to include it in the values list
            if (current.hasValue()) {
                values.add(current.getValue());
            }
            
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Finds the shortest match for a given key i.e. returns the value
     * associated with the shortest prefix of the key that has a value
     * 
     * @param key
     *            Key
     * @return Shortest Match or null if no possible matches
     */
    public T shortestMatch(String key) {
        TrieNode<T> current = this.root;
        if (key == null)
            return current.getValue();
        for (int i = 0; i < key.length(); i++) {
            if (current.hasValue())
                break;
            current = current.getChild(key.charAt(i));
            if (current == null)
                return null;
        }
        return current.getValue();
    }

    /**
     * Finds the longest match for a given key i.e. returns the value associated
     * with the longest prefix of the key that has a value
     * 
     * @param key
     *            Key
     * @return Longest Match or null if no possible matches
     */
    public T longestMatch(String key) {
        T value = null;
        TrieNode<T> current = this.root;
        if (key == null) {
            return current.getValue();
        } else {
            for (int i = 0; i < key.length(); i++) {
                if (current.hasValue())
                    value = current.getValue();
                current = current.getChild(key.charAt(i));
                if (current == null)
                    return value;
            }
            // If we reach here current is the complete key match
            // so return its value if it has one
            if (current.hasValue()) {
                return current.getValue();
            }
        }
        return value;
    }

    /**
     * Represents a node in the Trie
     * <p>
     * The implementation is designed to be sparse such that we delay creation
     * of things at both leafs and interior nodes until they are actually needed
     * </p>
     * 
     */
    private static class TrieNode<T> {
        private Map<Character, TrieNode<T>> children = null;
        private Character singletonChildChar = null;
        private TrieNode<T> singletonChild = null;
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
        public TrieNode<T> getChild(Character c) {
            if (this.children != null) {
                return this.children.get(c);
            } else if (c.equals(this.singletonChildChar)) {
                return this.singletonChild;
            } else {
                return null;
            }
        }

        /**
         * Moves to a child (creating a new node if necessary)
         * 
         * @param c
         *            Character to move to
         * @return Child
         */
        public TrieNode<T> moveToChild(Character c) {
            TrieNode<T> n = this.getChild(c);
            if (n == null) {
                n = new TrieNode<>(null);
                if (this.children != null) {
                    // Add to existing map
                    this.children.put(c, n);
                } else if (this.singletonChildChar != null) {
                    // Need to lazily create map
                    this.children = new HashMap<>();
                    this.children.put(this.singletonChildChar, this.singletonChild);
                    this.children.put(c, n);
                } else {
                    // Singleton child
                    this.singletonChildChar = c;
                    this.singletonChild = n;
                }
            }
            return n;
        }

        /**
         * Gets all values from a given node and its descendants
         * 
         * @return Values
         */
        public List<T> getValues() {
            List<T> values = new ArrayList<>();
            if (this.hasValue()) {
                values.add(this.value);
            }
            if (this.children != null) {
                for (TrieNode<T> child : this.children.values()) {
                    values.addAll(child.getValues());
                }
            } else if (this.singletonChild != null) {
                values.addAll(this.singletonChild.getValues());
            }
            return values;
        }
    }
}
