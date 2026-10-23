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

package org.apache.jena.dboe.trans.bplustree;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.trans.bplustree.AccessPath.AccessStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Iterator over records that only provides records which are distinct with respect to a prefix of their key
 */
class BPTreeDistinctKeyPrefixIterator implements Iterator<Record> {
    static Logger log = LoggerFactory.getLogger(BPTreeDistinctKeyPrefixIterator.class);

    public static Iterator<Record> create(BPTreeNode node, int keyPrefixLength) {
        return create(node, keyPrefixLength, null, null);
    }

    /**
     * Iterate distinct key prefixes in the half-open range [min, max).
     * Only the first keyPrefixLength bytes of either bound are significant; each
     * bound must contain at least that many bytes. Null denotes an unbounded end.
     * A zero-length prefix represents a single distinct value on a non-empty tree.
     */
    public static Iterator<Record> create(BPTreeNode node, int keyPrefixLength, Record minRecordInclusive, Record maxRecordExclusive) {
        int keyLength = node.params.recordFactory.keyLength();
        if (keyPrefixLength < 0 || keyPrefixLength > keyLength) {
            throw new IllegalArgumentException("keyPrefixLength must be between 0 and " + keyLength);
        }

        // Tree traversal and record-buffer scans compare whole keys. Map prefix
        // bounds to the first full key of each prefix, including the exclusive max.
        Record min = normalizeBound(minRecordInclusive, keyPrefixLength, keyLength);
        Record max = normalizeBound(maxRecordExclusive, keyPrefixLength, keyLength);
        if (min != null && max != null && Record.keyGE(min, max)) {
            return Iter.nullIter();
        }

        Record nodeMinRecord = node.minRecord();
        Record nodeMaxRecord = node.maxRecord();
        if (nodeMinRecord == null) {
            return Iter.nullIter();
        }

        if (haveSamePrefix(nodeMinRecord, nodeMaxRecord, keyPrefixLength)) {
            return isPrefixInRequestRange(nodeMinRecord, keyPrefixLength, min, max)
                    ? Iter.singletonIterator(nodeMinRecord) : Iter.nullIter();
        }

        // The downstream search requires a non-null lower bound.
        return new BPTreeDistinctKeyPrefixIterator(node, keyPrefixLength,
                min == null ? nodeMinRecord : min, max);
    }

    private static Record normalizeBound(Record bound, int keyPrefixLength, int keyLength) {
        if (bound == null) {
            return null;
        }
        if (bound.getKey().length < keyPrefixLength) {
            throw new IllegalArgumentException("Range bound is shorter than keyPrefixLength");
        }
        byte[] key = new byte[keyLength];
        System.arraycopy(bound.getKey(), 0, key, 0, keyPrefixLength);
        return new Record(key, null);
    }

    // Convert path to a stack of iterators
    private final Deque<Iterator<BPTreePage>> stack = new ArrayDeque<>();
    private Iterator<Record> current;
    private Record slot = null;
    private final Record minRecordInclusive, maxRecordExclusive;
    private byte[] lastPrefix = null;
    private boolean finished = false;

    private final int keyPrefixLength;

    BPTreeDistinctKeyPrefixIterator(BPTreeNode node, int keyPrefixLength, Record minRecordInclusive, Record maxRecordExclusive) {
        this.keyPrefixLength = keyPrefixLength;
        this.minRecordInclusive = Objects.requireNonNull(minRecordInclusive);
        this.maxRecordExclusive = maxRecordExclusive;

        BPTreeRecords r = loadStack(node);
        current = getRecordsIterator(r);
    }

    @Override
    public boolean hasNext() {
        if (finished) {
            return false;
        }
        if (slot != null) {
            return true;
        }
        while (true) {
            if (current == null) {
                end();
                return false;
            }

            if (current.hasNext()) {
                Record next = current.next();
                if (lastPrefix == null) {
                    return populateSlot(next);
                } else if (Arrays.compare(this.lastPrefix, 0, this.keyPrefixLength, next.getKey(), 0,
                                          this.keyPrefixLength) == 0) {
                    // Same key prefix as the last record we yielded so continue scanning
                    continue;
                } else {
                    return populateSlot(next);
                }
            } else {
                current = moveOnCurrent();
            }
        }
    }

    private boolean populateSlot(Record next) {
        slot = next;
        lastPrefix = next.getKey();
        return true;
    }

    // Move across the head of the stack until empty - then move next level.
    private Iterator<Record> moveOnCurrent() {
        Iterator<BPTreePage> iter = null;
        while (!stack.isEmpty()) {
            iter = stack.peek();
            if (iter.hasNext()) {
                break;
            }
            stack.pop();
        }

        if (iter == null || !iter.hasNext()) {
            return null;
        }
        BPTreePage p = iter.next();
        BPTreeRecords r;
        if (p instanceof BPTreeNode n) {
            // Check whether this entire subtree has the same key prefix, if so we can just return a singleton
            // iterator for this entire subtree and skip further recursion
            Record subtreeMin = n.minRecord();
            Record subtreeMax = n.maxRecord();
            if (haveSamePrefix(subtreeMin, subtreeMax, keyPrefixLength)) {
                Iterator<Record> recordIter;
                // Check that the prefix is in range
                if (isPrefixInRequestRange(subtreeMin, keyPrefixLength, minRecordInclusive, maxRecordExclusive)) {
                    // If the low and high keys for this page have the same prefix then just return a singleton iterator
                    recordIter = Iter.singletonIterator(subtreeMin);
                } else {
                    recordIter = Iter.nullIterator();
                }
                return recordIter;
            }
            r = loadStack(n);
        } else {
            r = (BPTreeRecords) p;
        }
        return getRecordsIterator(r);
    }

    private static int comparePrefix(Record a, Record b, int keyPrefixLength) {
        // Need to compare unsigned in general:
        // E.g. [0, 5] is less than [0, -1] because the latter must be treated as [0, 255].
        return Arrays.compareUnsigned(a.getKey(), 0, keyPrefixLength, b.getKey(), 0, keyPrefixLength);
    }

    private static boolean haveSamePrefix(Record a, Record b, int keyPrefixLength) {
        return comparePrefix(a, b, keyPrefixLength) == 0;
    }

    protected static boolean isPrefixInRequestRange(Record record, int keyPrefixLength, Record minRecordInclusive, Record maxRecordExclusive) {
        return
            (minRecordInclusive == null || comparePrefix(record, minRecordInclusive, keyPrefixLength) >= 0) &&
            (maxRecordExclusive == null || comparePrefix(record, maxRecordExclusive, keyPrefixLength) < 0);
    }

    // ---- Places we touch blocks.

    protected Iterator<Record> getRecordsIterator(BPTreeRecords records) {
        records.bpTree.startReadBlkMgr();
        Iterator<Record> iter;
        if (!records.hasAnyKeys()) {
            iter = Iter.nullIterator();
        } else {
            // Check whether we need to scan the whole page or can process it by skipping or singleton yield
            Record lowRecord = records.getLowRecord();
            Record highRecord = records.getHighRecord();
            if (haveSamePrefix(lowRecord, highRecord, keyPrefixLength)) {
                // Check that the prefix is in range
                if (isPrefixInRequestRange(lowRecord, keyPrefixLength, minRecordInclusive, maxRecordExclusive)) {
                    // If the low and high keys for this page have the same prefix then just return a singleton iterator
                    iter = Iter.singletonIterator(lowRecord);
                } else {
                    iter = Iter.nullIterator();
                }
            } else {
                // Otherwise need to scan the whole page
                // Note: Adjacent records with the same prefix are filtered out in this class's hasNext() method
                iter = records.getRecordBuffer().iterator(minRecordInclusive, maxRecordExclusive);
            }
        }
        records.bpTree.finishReadBlkMgr();
        return iter;
    }

    private BPTreeRecords loadStack(BPTreeNode node) {
        AccessPath path = new AccessPath(null);
        node.bpTree.startReadBlkMgr();

        node.internalSearch(path, minRecordInclusive);
        List<AccessStep> steps = path.getPath();
        for (AccessStep step : steps) {
            BPTreeNode n = step.node;
            Iterator<BPTreePage> it = n.iterator(minRecordInclusive, maxRecordExclusive);
            if (it == null || !it.hasNext()) {
                continue;
            }
            it.next();
            stack.push(it);
        }
        BPTreePage p = steps.get(steps.size() - 1).page;
        if (!(p instanceof BPTreeRecords)) {
            throw new InternalErrorException("Last path step not to a records block");
        }
        node.bpTree.finishReadBlkMgr();
        return (BPTreeRecords) p;
    }

    // ----

    private void end() {
        finished = true;
        current = null;
    }

    // ----

    public void close() {
        if (!finished) {
            end();
        }
    }

    @Override
    public Record next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Record r = slot;
        if (r == null) {
            throw new InternalErrorException("Null slot after hasNext is true");
        }
        slot = null;
        return r;
    }
}
