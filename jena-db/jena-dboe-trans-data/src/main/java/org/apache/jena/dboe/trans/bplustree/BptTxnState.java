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

/** Transactional state of a {@link BPlusTree} */
final class BptTxnState {
    final int initialroot;
    private int root = -1;

    // Point at which we start adding blocks in a write transaction.
    // The unmodifiable area is [0,boundary) and is different for each block manager.

    final long boundaryBlocksNode;
    final long boundaryBlocksRecord;

    BptTxnState(int initRoot, long boundaryNode, long boundaryRecords) {
        this.initialroot = initRoot;
        this.root = initRoot;
        this.boundaryBlocksNode = boundaryNode;
        this.boundaryBlocksRecord = boundaryRecords;
    }

    boolean modifiableNodeBlock(int id) {
        if ( BPT.forcePromoteModes )
            return ! BPT.promoteDuplicateNodes;
        return id >= boundaryBlocksNode;
    }

    boolean modifiableRecordsBlock(int id) {
        if ( BPT.forcePromoteModes )
            return ! BPT.promoteDuplicateRecords;
        return id >= boundaryBlocksRecord;
    }

    @Override
    public String toString() {
        return "BptTxnState: [root="+root+", n="+boundaryBlocksNode+", r="+boundaryBlocksRecord+"]";
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
    }
}

