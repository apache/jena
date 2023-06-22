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
package org.apache.jena.mem2.collection;

/**
 * Implementation of {@link JenaSet} based on {@link HashCommonBase}.
 */
public abstract class HashCommonSet<K> extends HashCommonBase<K> implements JenaSet<K> {

    /**
     * Initialise this hashed thingy to have <code>initialCapacity</code> as its
     * capacity and the corresponding threshold. All the key elements start out
     * null.
     */
    protected HashCommonSet(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public boolean tryAdd(K key) {
        final var slot = findSlot(key);
        if (slot < 0) return false;
        keys[slot] = key;
        if (++size > threshold) grow();
        return true;
    }

    @Override
    public void addUnchecked(K key) {
        final var slot = findSlot(key);
        if (slot < 0) return;
        keys[slot] = key;
        if (++size > threshold) grow();
    }

    @Override
    protected void grow() {
        final K[] oldContents = keys;
        keys = newKeysArray(calcGrownCapacityAndSetThreshold());
        for (final K key : oldContents) {
            if (key != null) {
                final int slot = findSlot(key);
                keys[slot] = key;
            }
        }
    }

    /**
     * Remove the triple at element <code>i</code> of <code>contents</code>.
     * This is an implementation of Knuth's Algorithm R from tAoCP vol3, p 527,
     * with exchanging of the roles of i and j so that they can be usefully renamed
     * to <i>here</i> and <i>scan</i>.
     * <p>
     * It relies on linear probing but doesn't require a distinguished REMOVED
     * value. Since we resize the table when it gets fullish, we don't worry [much]
     * about the overhead of the linear probing.
     * <p>
     * Iterators running over the keys may miss elements that are moved from the
     * bottom of the table to the top because of Iterator::remove. removeFrom
     * returns such a moved key as its result, and null otherwise.
     */
    @Override
    protected void removeFrom(int here) {
        size -= 1;
        while (true) {
            keys[here] = null;
            int scan = here;
            while (true) {
                if (--scan < 0) scan += keys.length;
                if (keys[scan] == null) return;
                final int r = initialIndexFor(keys[scan].hashCode());
                if ((scan > r || r >= here) && (r >= here || here >= scan) && (here >= scan || scan > r)) {
                    keys[here] = keys[scan];
                    here = scan;
                    break;
                }
            }
        }
    }
}
