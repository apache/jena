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

import java.util.*;

import org.apache.jena.dboe.base.record.RecordMapper;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.trans.bplustree.AccessPath.AccessStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Temp - merge back into BPTreeRangeIterator

// What about RecordRangeIterator

/** Iterator that converts to X directly */
class BPTreeRangeIteratorMapper<X> implements Iterator<X> {
    static Logger log = LoggerFactory.getLogger(BPTreeRangeIteratorMapper.class);

    public static <X> Iterator<X> create(BPTreeNode node, Record minRec, Record maxRec, int keyLength, RecordMapper<X> mapper) {
        if ( minRec != null && maxRec != null && Record.keyGE(minRec, maxRec) )
            return Iter.nullIter();
        return new BPTreeRangeIteratorMapper<>(node, minRec, maxRec, keyLength, mapper);
    }

    // Convert path to a stack of iterators
    final private Deque<Iterator<BPTreePage>> stack = new ArrayDeque<>();
    final private Record minRecord;
    final private Record maxRecord;
    final private RecordMapper<X> mapper;
    private Iterator<X> current;
    private X slot = null;
    final private byte[] keySlot;
    private boolean finished = false;

    BPTreeRangeIteratorMapper(BPTreeNode node, Record minRec, Record maxRec, int keyLength, RecordMapper<X> mapper) {
        this.minRecord = minRec;
        this.maxRecord = maxRec;
        this.mapper = mapper;
        this.keySlot = new byte[keyLength];

        BPTreeRecords r = loadStack(node);
        current = getRecordsIterator(r, minRecord, maxRecord, mapper);
    }

    @Override
    public boolean hasNext() {
        if ( finished )
            return false;
        if ( slot != null )
            return true;
        while(current != null && !current.hasNext()) {
            current = moveOnCurrent();
        }
        if ( current == null ) {
            end();
            return false;
        }
        slot = current.next();
        return true;
    }

    // Move across the head of the stack until empty - then move next level.
    private Iterator<X> moveOnCurrent() {
        Iterator<BPTreePage> iter = null;
        while(!stack.isEmpty()) {
            iter = stack.peek();
            if ( iter.hasNext() )
              break;
            stack.pop();
        }

        if ( iter == null || ! iter.hasNext() )
            return null;
        BPTreePage p = iter.next();
        BPTreeRecords r = null;
        if (p instanceof BPTreeNode) {
            r = loadStack((BPTreeNode)p);
        }
        else {
            r = (BPTreeRecords)p;
        }
        return getRecordsIterator(r, minRecord, maxRecord, mapper);
    }

    // ---- Places we touch blocks.

    private static<X>  Iterator<X> getRecordsIterator(BPTreeRecords records, Record minRecord, Record maxRecord, RecordMapper<X> mapper) {
        records.bpTree.startReadBlkMgr();
        Iterator<X> iter = records.getRecordBuffer().iterator(minRecord, maxRecord, mapper);
        records.bpTree.finishReadBlkMgr();
        return iter;
    }

    private BPTreeRecords loadStack(BPTreeNode node) {
        AccessPath path = new AccessPath(null);
        node.bpTree.startReadBlkMgr();

        if ( minRecord == null )
            node.internalMinRecord(path);
        else
            node.internalSearch(path, minRecord);
        List<AccessStep> steps = path.getPath();
        for ( AccessStep step : steps ) {
            BPTreeNode n = step.node;
            Iterator<BPTreePage> it = n.iterator(minRecord, maxRecord);
            if ( it == null || ! it.hasNext() )
                continue;
            BPTreePage p = it.next();
            stack.push(it);
        }
        BPTreePage p = steps.get(steps.size()-1).page;
        if ( ! ( p instanceof BPTreeRecords ) )
            throw new InternalErrorException("Last path step not to a records block");
        node.bpTree.finishReadBlkMgr();
        return (BPTreeRecords)p;
    }

    // ----

    private void end() {
        finished = true;
        current = null;
    }

    // ----

    public void close() {
        if ( ! finished )
            end();
    }

    @Override
    public X next() {
        if ( ! hasNext() )
            throw new NoSuchElementException();
        X r = slot;
        if ( r == null )
            throw new InternalErrorException("Null slot after hasNext is true");
        slot = null;
        return r;
    }
}
