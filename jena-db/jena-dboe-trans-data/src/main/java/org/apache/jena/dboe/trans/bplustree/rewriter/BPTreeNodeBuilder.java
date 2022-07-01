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

package org.apache.jena.dboe.trans.bplustree.rewriter;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.buffer.PtrBuffer;
import org.apache.jena.dboe.base.buffer.RecordBuffer;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.trans.bplustree.BPTreeNode;
import org.apache.jena.dboe.trans.bplustree.BPTreeNodeMgr;

/** Take a stream of (block id,split record) pairs and generate B+Tree Nodes */
class BPTreeNodeBuilder implements Iterator<Pair<Integer, Record>> {
    private Pair<Integer, Record>           slot;
    private Iterator<Pair<Integer, Record>> iter;
    private final BPTreeNodeMgr             mgr;

    private final boolean                   leafLayer;
    private final RecordFactory             recordFactory;

    BPTreeNodeBuilder(Iterator<Pair<Integer, Record>> iter, BPTreeNodeMgr mgr, boolean leafLayer, RecordFactory recordFactory) {
        this.iter = iter;
        this.mgr = mgr;
        this.leafLayer = leafLayer;
        this.recordFactory = recordFactory;
    }

    @Override
    public boolean hasNext() {
        if ( slot != null )
            return true;

        if ( iter == null )
            return false;

        if ( !iter.hasNext() ) {
            // Start of next block, no more input.
            iter = null;
            return false;
        }

        // At least one element to put in a new node.
        // Unknown parent. Does not matter (parent is only in-memory)

        BPTreeNode bptNode = mgr.createNode(-1);
        bptNode.setIsLeaf(leafLayer);

        RecordBuffer recBuff = bptNode.getRecordBuffer();
        PtrBuffer ptrBuff = bptNode.getPtrBuffer();
        recBuff.setSize(0);
        ptrBuff.setSize(0); // Creation leaves this junk.

        final boolean debug = false;
        int rMax = recBuff.maxSize();
        int pMax = ptrBuff.maxSize();
        if ( debug )
            System.out.printf("Max: (%d, %d)\n", rMax, pMax);

        for (; iter.hasNext() ; ) {

            int X = bptNode.getCount();
            int X2 = bptNode.getMaxSize();
            int P = ptrBuff.size();
            int P2 = ptrBuff.maxSize();
            int R = recBuff.size();
            int R2 = recBuff.maxSize();

            // bptNode.getMaxSize() is drivel
            // System.out.printf("N: %d/%d : P %d/%d : R %d/%d\n", X, X2, P, P2,
            // R, R2);

            Pair<Integer, Record> pair = iter.next();
            if ( debug )
                System.out.println("** Item: " + pair);
            Record r = pair.cdr();

            // The record is key-only (which is correct) but until FREC fixed,
            // we need key,value
            r = recordFactory.create(r.getKey());

            // Always add - so ptrBuff is one ahead when we finish.
            // There is always one more ptr than record in a B+Tree node.
            if ( ptrBuff.isFull() )
                Log.error(this, "PtrBuffer is full");

            // Add pointer.
            ptrBuff.add(pair.car());

            if ( ptrBuff.isFull() ) {
                // End of this block.

                // Does not add to ptrBuff so the one extra slot is done.
                // Instead, the high point goes to the next level up.

                // Internal consistency check.
                if ( !ptrBuff.isFull() )
                    Log.error(this, "PtrBuffer is not full");

                // The split point for the next level up.
                slot = new Pair<>(bptNode.getId(), pair.cdr());

                if ( debug )
                    System.out.printf("Write(1): %d\n", bptNode.getId());
                if ( debug )
                    System.out.println(bptNode);
                if ( debug )
                    System.out.println("Slot = " + slot);
                mgr.put(bptNode);
                // No count increment needed.
                return true;
            }

            recBuff.add(r);
            bptNode.setCount(bptNode.getCount() + 1);
        }

        // If we get here, the input stream ran out before we finished a
        // complete block.
        // Fix up block (remove the last record)
        Record r = recBuff.getHigh();
        recBuff.removeTop();
        bptNode.setCount(bptNode.getCount() - 1);
        slot = new Pair<>(bptNode.getId(), r);

        if ( debug )
            System.out.printf("Write(2): %d\n", bptNode.getId());
        if ( debug )
            System.out.println(bptNode);
        if ( debug )
            System.out.println("Slot = " + slot);
        mgr.put(bptNode);
        return true;
    }

    @Override
    public Pair<Integer, Record> next() {
        if ( !hasNext() )
            throw new NoSuchElementException();
        Pair<Integer, Record> x = slot;
        slot = null;
        return x;
    }
}
