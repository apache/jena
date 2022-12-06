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
        return new BPTreeDistinctKeyPrefixIterator(node, keyPrefixLength);
    }

    // Convert path to a stack of iterators
    private final Deque<Iterator<BPTreePage>> stack = new ArrayDeque<>();
    private Iterator<Record> current;
    private Record slot = null, minRecord, maxRecord;
    private byte[] lastPrefix = null;
    private boolean finished = false;

    private final int keyPrefixLength;

    BPTreeDistinctKeyPrefixIterator(BPTreeNode node, int keyPrefixLength) {
        if (keyPrefixLength < 1) {
            throw new IllegalArgumentException("keyPrefixLength must be >= 1");
        } else if (keyPrefixLength > node.params.recordFactory.keyLength()) {
            throw new IllegalArgumentException(
                    "Maximum keyPrefixLength for this B+Tree is " + node.params.recordFactory.keyLength());
        }

        this.keyPrefixLength = keyPrefixLength;
        this.minRecord = node.minRecord();
        this.maxRecord = node.maxRecord();

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
        if (p instanceof BPTreeNode) {
            BPTreeNode n = ((BPTreeNode) p);
            // Check whether this entire subtree has the same key prefix, if so we can just return a singleton
            // iterator for this entire subtree and skip further recursion
            Record subtreeMin = n.minRecord();
            Record subtreeMax = n.maxRecord();
            if (haveSamePrefix(subtreeMin, subtreeMax)) {
                return Iter.singleton(subtreeMin);
            }
            r = loadStack(n);
        } else {
            r = (BPTreeRecords) p;
        }
        return getRecordsIterator(r);
    }

    protected final boolean haveSamePrefix(Record a, Record b) {
        return Arrays.compare(a.getKey(), 0, this.keyPrefixLength, b.getKey(), 0, this.keyPrefixLength) == 0;
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
            if (haveSamePrefix(lowRecord, records.getHighRecord())) {
                // If the low and high keys for this page have the same prefix then just return a singleton iterator
                iter = Iter.singleton(lowRecord);
            } else {
                // Otherwise need to scan the whole page
                iter = records.getRecordBuffer().iterator();
            }
        }
        records.bpTree.finishReadBlkMgr();
        return iter;
    }

    private BPTreeRecords loadStack(BPTreeNode node) {
        AccessPath path = new AccessPath(null);
        node.bpTree.startReadBlkMgr();

        node.internalMinRecord(path);
        List<AccessStep> steps = path.getPath();
        for (AccessStep step : steps) {
            BPTreeNode n = step.node;
            Iterator<BPTreePage> it = n.iterator(this.minRecord, this.maxRecord);
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
