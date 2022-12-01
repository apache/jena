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

import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.base.record.RecordMapper;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.TransactionalComponentLifecycle;
import org.apache.jena.dboe.transaction.txn.TxnId;
import org.apache.jena.query.ReadWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * B-Tree taken from:
 * <pre>
 * Introduction to Algorithms, Second Edition
 * Chapter 18: B-Trees
 * by Thomas H. Cormen, Charles E. Leiserson,
 *    Ronald L. Rivest and Clifford Stein
 * </pre>
 * Includes implementation of removal
 * then the B-Tree code converted to a B+Tree,
 * then made MVCC/transactional.
 * <p>
 * Notes:
 * <ul>
 * <li>
 * Stores "records", which are a key and value (the value may be absent).
 * <li>
 * In this B+Tree implementation, the (key,value) pairs are held in
 * RecordBuffer, which wraps a ByteBuffer that only has records in it.
 * BPTreeRecords provides the B+Tree view of a RecordBuffer. All records
 * are in RecordBuffer - the "tree" part is an index for finding the right
 * page. The tree only holds keys, copies from the (key, value) pairs in
 * the RecordBuffers.
 * <li>
 * The version above splits nodes on the way down when full,
 * not when needed where a split can bubble up from below.
 * It means it only ever walks down the tree on insert.
 * Similarly, the delete code ensures a node is suitable
 * before descending.
 * </ul>
 */

public class BPlusTree extends TransactionalComponentLifecycle<BptTxnState> implements RangeIndex
{
    /*
     * Insertion:
     * There are two styles for handling node splitting.
     *
     * Classically, when a leaf is split, the separating key is inserted into
     * the parent, which may itself be full and so that is split, etc propagting
     * up to the root (splitting the root is the only time the depth of the
     * BTree increases). This involves walking back up the tree.
     *
     * It is more convenient to have a spare slot in a tree node, so that the
     * new key can be inserted, then the keys and child pointers split.
     *
     * Modification: during insertion, splitting is applied to any full node
     * traversed on the way down, resulting in any node passed through having
     * some space for a new key. When splitting starts at a leaf, only the
     * immediate parent is changed because it must have space for the new key.
     * There is no cascade back to the top of the tree (it would have happened on
     * the way down); in other words, splitting is done early. This is insertion
     * in a single downward pass.
     *
     * When compared to the classic approach including the extra slot for
     * convenient inserting, the space useage is approximately the same.
     *
     * Deletion:
     * Deletion always occurs at a leaf; if it's an internal node, swap the key
     * with the right-most left key (predecessor) or left-most right key (successor),
     * and delete in the leaf.
     *
     * The classic way is to propagate node merging back up from the leaf.  The
     * book outlines a way that checks that a nod eis delte-suitable (min+1 in size)
     * on the way down.  This is implemented here; this is one-pass(ish).
     *
     * Variants:
     * http://en.wikipedia.org/wiki/Btree
     *
     * B+Tree: Tree contains keys, and only the leaves have the values. Used for
     * secondary indexes (external pointers) but also for general on-disk usage
     * because more keys are packed into a level. Can chain the leaves for a
     * sorted-order traversal.
     *
     * B*Tree: Nodes are always 2/3 full. When a node is full, keys are shared adjacent
     * nodes and if all they are all full do 2 nodes get split into 3 nodes.
     * Implementation wise, it is more complicated; can cause more I/O.
     *
     * B#Tree: A B+Tree where the operations try to swap nodes between immediate
     * sibling nodes instead of immediately splitting (like delete, only on insert).
     */

    private static Logger log = LoggerFactory.getLogger(BPlusTree.class);

    // Root id across transactions
    // Changes as the tree evolves in write transactions.
    private int rootIdx = -199;
    private BPTStateMgr stateManager;
    private BPTreeNodeMgr nodeManager;
    private BPTreeRecordsMgr recordsMgr;
    private final BPlusTreeParams bpTreeParams;
    private Mode mode = Mode.TRANSACTIONAL;
    private BptTxnState nonTxnState = null;

    // Construction is a two stage process
    //    1/ Create the object, uninitialized
    //      (Setup data structures, without referring to any BPlusTree methods)
    //    2/ initialize
    /*package*/ BPlusTree(ComponentId componentId, BPlusTreeParams bpTreeParams) {
        super(componentId);
        this.rootIdx = -99;
        this.bpTreeParams = bpTreeParams;
        this.nodeManager = null;
        this.recordsMgr = null;
    }

    /*package*/ void init(BPTStateMgr stateManager, BPTreeNodeMgr  nodeManager, BPTreeRecordsMgr recordsMgr) {
        // Second part of creating.
        // Some of these point to the BPlusTree object so we create the BPlusTree as
        // basic structure then initialize fully here.
        this.rootIdx = stateManager.getRoot();
        this.stateManager = stateManager;
        this.nodeManager = nodeManager;
        this.recordsMgr = recordsMgr;
    }

    private BPTreeNode getRootRead() {
        if ( isTransactional() ) {
            super.checkTxn();
            int rootId = super.getDataState().getRoot();
            return nodeManager.getRead(rootId, BPlusTreeParams.RootParent);
        }
        return nodeManager.getRead(rootIdx, BPlusTreeParams.RootParent);
    }

    private BPTreeNode getRootWrite() {
        if ( isTransactional() ) {
            super.requireWriteTxn();
            int rootId = super.getDataState().getRoot();
            return nodeManager.getRead(rootId, BPlusTreeParams.RootParent);
        }
        return nodeManager.getRead(rootIdx, BPlusTreeParams.RootParent);
    }

    private boolean isTransactional() {
        return mode == Mode.TRANSACTIONAL;
    }

    private void releaseRootRead(BPTreeNode rootNode) {
        rootNode.release();
    }

    private void releaseRootWrite(BPTreeNode rootNode) {
        rootNode.release();
    }

    private void setRoot(BPTreeNode node) {
        throw new InternalErrorException("BPlusTree.setRoot");
    }

    public void newRoot(BPTreeNode newRoot) {
        if ( isTransactional() )
            getDataState().setRoot(newRoot.getId());
        else
            rootIdx = newRoot.getId();
    }

    public int getRootId() {
        if ( super.isActiveTxn() )
            return super.getDataState().getRoot();
        else
            return rootIdx;
    }

    /*package*/ BptTxnState state() {
        if ( mode == Mode.TRANSACTIONAL ) {
            if ( super.isActiveTxn() )
                return super.getDataState();
            return null;
        }
        return nonTxnState;
    }

    /** Get the parameters describing this B+Tree */
    public BPlusTreeParams getParams()          { return bpTreeParams; }

    /** Only use for careful manipulation of structures */
    public BPTStateMgr getStateManager()          { return stateManager; }

    /** Only use for careful manipulation of structures */
    public BPTreeNodeMgr getNodeManager()       { return nodeManager; }

    /** Only use for careful manipulation of structures */
    public BPTreeRecordsMgr getRecordsMgr()     { return recordsMgr; }

    @Override
    public RecordFactory getRecordFactory() {
        return bpTreeParams.recordFactory;
    }

    @Override
    public Record find(Record record) {
        startReadBlkMgr();
        BPTreeNode root = getRootRead();
        Record v = BPTreeNode.search(root, record);
        releaseRootRead(root);
        finishReadBlkMgr();
        return v;
    }

    @Override
    public boolean contains(Record record) {
        Record r = find(record);
        return r != null;
    }

    @Override
    public Record minKey() {
        startReadBlkMgr();
        BPTreeNode root = getRootRead();
        Record r = BPTreeNode.minRecord(root);
        releaseRootRead(root);
        finishReadBlkMgr();
        return r;
    }

    @Override
    public Record maxKey() {
        startReadBlkMgr();
        BPTreeNode root = getRootRead();
        Record r = BPTreeNode.maxRecord(root);
        releaseRootRead(root);
        finishReadBlkMgr();
        return r;
    }

    @Override
    public boolean insert(Record record) {
        return insertAndReturnOld(record) == null;
    }

    /** Add a record into the B+Tree */
    public Record insertAndReturnOld(Record record) {
        startUpdateBlkMgr();
        BPTreeNode root = getRootWrite();
        //System.out.println("INSERT: "+record);
        Record r = BPTreeNode.insert(root, record);
        releaseRootWrite(root);
        finishUpdateBlkMgr();
        if ( false ) {
            // check, and if an error found, dump tree
            try { check(); }
            catch (BPTreeException ex) {
                ex.printStackTrace();
                dump();
                throw ex;
            }
        }
        return r;
    }

    @Override
    public boolean delete(Record record) {
        return deleteAndReturnOld(record) != null;
    }

    public Record deleteAndReturnOld(Record record) {
        startUpdateBlkMgr();
        BPTreeNode root = getRootWrite();
        Record r = BPTreeNode.delete(root, record);
        releaseRootWrite(root);
        finishUpdateBlkMgr();
        return r;
    }

    private static Record noMin = null;
    private static Record noMax = null;

    @Override
    public Iterator<Record> iterator() {
        return iterator(noMin, noMax);
    }

    @Override
    public Iterator<Record> iterator(Record fromRec, Record toRec) {
        startReadBlkMgr();
        BPTreeNode root = getRootRead();
        releaseRootRead(root);
        finishReadBlkMgr();
        return BPTreeRangeIterator.create(root, fromRec, toRec);
        //return iterator(fromRec, toRec, RecordFactory.mapperRecord);
    }

    public Iterator<Record> distinctByKeyPrefix(int keyPrefixLength) {
        startReadBlkMgr();
        BPTreeNode root = getRootRead();
        releaseRootRead(root);
        finishReadBlkMgr();
        return BPTreeDistinctKeyPrefixIterator.create(root, keyPrefixLength);
    }

    /*
    @Override
    public <X> Iterator<X> iterator(Record minRec, Record maxRec, RecordMapper<X> mapper) {
        startReadBlkMgr();
        BPTreeNode root = getRoot();
        Iterator<X> iter = iterator(root, minRec, maxRec, mapper);
        releaseRoot(root);
        finishReadBlkMgr();
        // Note that this end the read-part (find the start), not the iteration.
        // Iterator read blocks still get handled.
        return iter;
    }

    private static <X> Iterator<X> iterator(BPTreeNode node, Record fromRec, Record toRec, RecordMapper<X> mapper) {
        // Look for starting RecordsBufferPage id.
        int id = BPTreeNode.recordsPageId(node, fromRec);
        if ( id < 0 )
            return Iter.nullIter();
        RecordBufferPageMgr pageMgr = node.getBPlusTree().getRecordsMgr().getRecordBufferPageMgr();
        // No pages are active at this point.
        return RecordRangeIterator.iterator(id, fromRec, toRec, pageMgr, mapper);
    }
    */

    @Override
    public <X> Iterator<X> iterator(Record minRec, Record maxRec, RecordMapper<X> mapper) {
        startReadBlkMgr();
        BPTreeNode root = getRootRead();
        releaseRootRead(root);
        finishReadBlkMgr();
        return iterator(root, minRec, maxRec, mapper);
    }

    private <X> Iterator<X> iterator(BPTreeNode node, Record minRec, Record maxRec, RecordMapper<X> mapper) {
        int keyLen = recordsMgr.getRecordBufferPageMgr().getRecordFactory().keyLength();
        return BPTreeRangeIteratorMapper.create(node, minRec, maxRec, keyLen, mapper);
    }

    // Internal calls.
    void startReadBlkMgr() {
        nodeManager.startRead();
        recordsMgr.startRead();
    }

    void finishReadBlkMgr() {
        nodeManager.finishRead();
        recordsMgr.finishRead();
    }

    private void startUpdateBlkMgr() {
        nodeManager.startUpdate();
        recordsMgr.startUpdate();
    }

    private void finishUpdateBlkMgr() {
        nodeManager.finishUpdate();
        recordsMgr.finishUpdate();
    }

    @Override
    public boolean isEmpty() {
        startReadBlkMgr();
        BPTreeNode root = getRootRead();
        boolean b = !root.hasAnyKeys();
        releaseRootRead(root);
        finishReadBlkMgr();
        return b;
    }

    private static int SLICE = 10000;
    @Override
    public void clear() {
        Record[] records = new Record[SLICE];
        while(true) {
            Iterator<Record> iter = iterator();
            int i = 0;
            for ( i = 0; i < SLICE ; i++ ) {
                if ( ! iter.hasNext() )
                    break;
                Record r = iter.next();
                records[i] = r;
            }
            if ( i == 0 )
                break;
            for ( int j = 0; j < i ; j++ ) {
                delete(records[j]);
                records[j] = null;
            }
        }
    }

    @Override
    public void sync() {
        if ( nodeManager.getBlockMgr() != null )
            nodeManager.getBlockMgr().sync();
        if ( recordsMgr.getBlockMgr() != null )
            recordsMgr.getBlockMgr().sync();
    }

    @Override
    public void close() {
        nodeManager.close();
        recordsMgr.close();
        stateManager.close();
    }

    @Override
    public long size() {
        Iterator<Record> iter = iterator();
        return Iter.count(iter);
    }

    @Override
    public void check() {
        BPTreeNode root = getRootRead();
        try { root.checkNodeDeep(); }
        finally { releaseRootRead(root); }
    }

    public void dump() {
        // Caution - nesting via startReadBlkMgr
        startReadBlkMgr();
        BPTreeNode root = getRootRead();
        boolean b = BPT.Logging;
        BPT.Logging = false;
        try { root.dump(); }
        finally {
            releaseRootRead(root);
            BPT.Logging = b;
            }
        finishReadBlkMgr();
    }

    public void dump(IndentedWriter out) {
        BPTreeNode root = getRootRead();
        try { root.dump(out); }
        finally { releaseRootRead(root); }
    }

    public void nonTransactional() {
        setMode(Mode.MUTABLE);
    }

    private void setMode(Mode newMode) {

        mode = newMode;

        switch(mode) {
            case TRANSACTIONAL :
                nonTxnState = null;
                break;
            case MUTABLE :
                nonTxnState = new BptTxnState(BPlusTreeParams.RootId, 0, 0);
                break;
            case IMMUTABLE :
                nonTxnState = new BptTxnState(BPlusTreeParams.RootId,
                                              nodeManager.allocLimit(),
                                              recordsMgr.allocLimit());
                break;
            case IMMUTABLE_ALL :
                nonTxnState = new BptTxnState(BPlusTreeParams.RootId,
                                              Long.MAX_VALUE,
                                              Long.MAX_VALUE);
                break;
        }
    }

    @Override
    public void startRecovery() {}

    @Override
    public void recover(ByteBuffer ref) {
        stateManager.setState(ref);
        rootIdx = stateManager.getRoot();
        nodeManager.resetAlloc(stateManager.getNodeBlocksLimit());
        recordsMgr.resetAlloc(stateManager.getRecordsBlocksLimit());
    }

    @Override
    public void finishRecovery() {
        stateManager.sync();
    }

    @Override
    public void cleanStart() { }

    @Override
    protected BptTxnState _begin(ReadWrite readWrite, TxnId txnId) {
        return createState();
    }

    private BptTxnState createState() {
        return new BptTxnState(rootIdx,
                               nodeManager.allocLimit(),
                               recordsMgr.allocLimit());
    }

    /* The persistent transactional state of a B+Tree is new root and the
     * allocation limits of both block managers.
     */
    @Override
    protected BptTxnState _promote(TxnId txnId, BptTxnState oldState) {
        BptTxnState newState = createState();
        return newState;
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, BptTxnState state) {
        nodeManager.getBlockMgr().sync();
        recordsMgr.getBlockMgr().sync();

        long nodeLimit = nodeManager.allocLimit();
        long recordsLimit = recordsMgr.allocLimit();
        // But don't write it yet.
        stateManager.setState(state.getRoot(), nodeLimit, recordsLimit);
        return stateManager.getState();
    }

    @Override
    protected void _commit(TxnId txnId, BptTxnState state) {
        if ( isWriteTxn() ) {
            rootIdx = state.getRoot();
            stateManager.sync();
        }
    }

    @Override
    protected void _commitEnd(TxnId txnId, BptTxnState state) {}

    @Override
    protected void _abort(TxnId txnId, BptTxnState state) {
        if ( isWriteTxn() ) {
            rootIdx = state.initialroot;
            // Truncate - logically in block manager space.
            nodeManager.resetAlloc(state.boundaryBlocksNode);
            recordsMgr.resetAlloc(state.boundaryBlocksRecord);
            stateManager.setState(state.initialroot, state.boundaryBlocksNode, state.boundaryBlocksRecord);
            stateManager.sync();
        }
    }

    @Override
    protected void _complete(TxnId txnId, BptTxnState state) {}

    @Override
    protected void _shutdown() {}
}
