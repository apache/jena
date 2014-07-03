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

package com.hp.hpl.jena.tdb.index.bplustree;

import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.CheckingNode ;
import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.CheckingTree ;

import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrTracker ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageMgr ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordRangeIterator ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;

/** B-Tree converted to B+Tree
 * 
 * B-Tree taken from:
 * Introduction to Algorithms, Second Edition
 * Chapter 18: B-Trees
 * by Thomas H. Cormen, Charles E. Leiserson, 
 *    Ronald L. Rivest and Clifford Stein 
 *  
 * Includes implementation of removal.
 * 
 * Notes:
 * Stores "records", which are a key and value (the value may be null).
 * 
 * In this B+Tree implementation, the (key,value) pairs are held in
 * RecordBuffer, which wrap a ByteBuffer that only has records in it.  
 * BPTreeRecords provides the B+Tree view of a RecordBuffer. All records
 * are in RecordBuffer - the "tree" part is an index for finding the right
 * page. The tree only holds keys, copies from the (key, value) pairs in
 * the RecordBuffers. 
 *
 * Notes:
 * 
 * The version above splits nodes on the way down when full,
 * not when needed where a split can bubble up from below.
 * It means it only ever walks down the tree on insert.
 * Similarly, the delete code ensures a node is suitable
 * before decending. 
 *    
 * Variations:
 * In this impl, splitRoot leaves the root node in place.
 * The root is always the same block.
 */

public class BPlusTree implements Iterable<Record>, RangeIndex
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
    
    private static Logger log = LoggerFactory.getLogger(BPlusTree.class) ;
    
    private int rootIdx = BPlusTreeParams.RootId ;
    ///*package*/ BPTreeNode root ;
    private BPTreeNodeMgr nodeManager ; 
    private BPTreeRecordsMgr recordsMgr; 
    private BPlusTreeParams bpTreeParams ;
    
    /** Create the in-memory structures to correspond to
     * the supplied block managers for the persistent storage.
     * Initialize the persistent storage to the empty B+Tree if it does not exist.
     * This is the normal way to create a B+Tree.
     */
    public static BPlusTree create(BPlusTreeParams params, BlockMgr blkMgrNodes, BlockMgr blkMgrLeaves)
    { 
        BPlusTree bpt = attach(params, blkMgrNodes, blkMgrLeaves) ;
        bpt.createIfAbsent() ;
        return bpt ;
    }
    
    /** Create the in-memory structures to correspond to
     *  the supplied block managers for the persistent storage.
     *  Does not inityalize the B+Tree - it assumes the block managers
     *  correspond to an existing B+Tree.
     */
    public static BPlusTree attach(BPlusTreeParams params, BlockMgr blkMgrNodes, BlockMgr blkMgrRecords)
    { 
        return new BPlusTree(params, blkMgrNodes, blkMgrRecords) ;
    }

    /** (Testing mainly) Make an in-memory B+Tree, with copy-in, copy-out block managers */
    public static BPlusTree makeMem(int order, int minRecords, int keyLength, int valueLength)
    { return makeMem(null, order, minRecords, keyLength, valueLength) ; }
    
    /** (Testing mainly) Make an in-memory B+Tree, with copy-in, copy-out block managers */
    public static BPlusTree makeMem(String name, int order, int minRecords, int keyLength, int valueLength)
    {
        BPlusTreeParams params = new BPlusTreeParams(order, keyLength, valueLength) ;
        
        int blkSize ;
        if ( minRecords > 0 )
        {
            int maxRecords = 2*minRecords ;
            //int rSize = RecordBufferPage.HEADER+(maxRecords*params.getRecordLength()) ;
            blkSize = RecordBufferPage.calcBlockSize(params.getRecordFactory(), maxRecords) ;
        }
        else
            blkSize = params.getCalcBlockSize() ;
        
        BlockMgr mgr1 = BlockMgrFactory.createMem(name+"(nodes)", params.getCalcBlockSize()) ;
        BlockMgr mgr2 = BlockMgrFactory.createMem(name+"(records)", blkSize) ;
        
        BPlusTree bpTree = BPlusTree.create(params, mgr1, mgr2) ;
        return bpTree ;
    }

    /** Debugging */
    public static BPlusTree addTracking(BPlusTree bpTree)
    {
        BlockMgr mgr1 = bpTree.getNodeManager().getBlockMgr() ;
        BlockMgr mgr2 = bpTree.getRecordsMgr().getBlockMgr() ;
//        mgr1 = BlockMgrTracker.track("BPT/Nodes", mgr1) ;
//        mgr2 = BlockMgrTracker.track("BPT/Records", mgr2) ;
        mgr1 = BlockMgrTracker.track(mgr1) ;
        mgr2 = BlockMgrTracker.track(mgr2) ;

        return BPlusTree.attach(bpTree.getParams(), mgr1, mgr2) ;
    }

    private BPlusTree(BPlusTreeParams params, BlockMgr blkMgrNodes, BlockMgr blkMgrRecords)
    {
        // Consistency checks.
        this.bpTreeParams = params ;
        this.nodeManager = new BPTreeNodeMgr(this, blkMgrNodes) ;
        RecordBufferPageMgr recordPageMgr = new RecordBufferPageMgr(params.getRecordFactory(), blkMgrRecords) ;
        recordsMgr = new BPTreeRecordsMgr(this, recordPageMgr) ;
    }

    /** Create if does not exist */
    private void createIfAbsent()
    {
        // This fixes the root to being block 0
        if ( ! nodeManager.valid(BPlusTreeParams.RootId) )
        //if ( ! nodeManager.getBlockMgr().isEmpty() )
        {
            // Create as does not exist.
            // [TxTDB:PATCH-UP]
            // ** Better: seperate "does it exist? - create statics used in factory"
            startUpdateBlkMgr() ;
            // Fresh BPlusTree
            rootIdx = nodeManager.createEmptyBPT() ;
            if ( rootIdx != 0 )
                throw new InternalError() ;
            
            if ( CheckingNode )
            {            
                BPTreeNode root = nodeManager.getRead(rootIdx, BPlusTreeParams.RootParent) ;
                root.checkNodeDeep() ;
                root.release() ;
            }
            
            // Sync created blocks to disk - any caches are now clean. 
            nodeManager.getBlockMgr().sync() ;
            recordsMgr.getBlockMgr().sync() ;
            
            // Cache : not currently done - root is null
            //setRoot(root) ;
            finishUpdateBlkMgr() ;
        }
    }

    private BPTreeNode getRoot()
    {
        // No caching here.
        BPTreeNode root = nodeManager.getRoot(rootIdx) ;
        //this.root = root ;
        return root ;
    }

    private void releaseRoot(BPTreeNode rootNode)
    {
//        // [TxTDB:PATCH-UP]
//        if ( root != null ) 
//        {
//            root.release() ;
//            //nodeManager.release(rootNode) ;
//        }
//        if ( root != null && rootNode != root )
//            log.warn("Root is not root!") ;
        
        rootNode.release() ;
    }

    private void setRoot(BPTreeNode node)
    {
        //root = node ;
    }

    /** Get the parameters describing this B+Tree */
    public BPlusTreeParams getParams()     { return bpTreeParams ; } 

    /** Only use for careful manipulation of structures */
    public BPTreeNodeMgr getNodeManager()          { return nodeManager ; }
    /** Only use for careful manipulation of structures */
    public BPTreeRecordsMgr getRecordsMgr()     { return recordsMgr ; }
    
    @Override
    public RecordFactory getRecordFactory()
    {
        return bpTreeParams.recordFactory ;
    }
    
    @Override
    public Record find(Record record)
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record v = BPTreeNode.search(root, record) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return v ;
    }
    
    @Override
    public boolean contains(Record record)
    {
        Record r = find(record) ;
        return r != null ;
    }

    @Override
    public Record minKey()
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record r = root.minRecord();
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return r ;
    }

    @Override
    public Record maxKey()
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record r = root.maxRecord() ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return r ;
    }

    @Override
    public boolean add(Record record)
    {
        return addAndReturnOld(record) == null ;
    }
    
    /** Add a record into the B+Tree */
    public Record addAndReturnOld(Record record)
    {
        startUpdateBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record r = BPTreeNode.insert(root, record) ;
        if ( CheckingTree ) root.checkNodeDeep() ;
        releaseRoot(root) ;
        finishUpdateBlkMgr() ;
        return r ;
    }
    
    @Override
    public boolean delete(Record record)
    { return deleteAndReturnOld(record) != null ; }
    
    public Record deleteAndReturnOld(Record record)
    {
        startUpdateBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record r = BPTreeNode.delete(root, record) ;
        if ( CheckingTree ) root.checkNodeDeep() ;
        releaseRoot(root) ;
        finishUpdateBlkMgr() ;
        return r ;
    }

    @Override
    public Iterator<Record> iterator()
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Iterator<Record> iter = iterator(root) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return iter ;
    }
    
    @Override
    public Iterator<Record> iterator(Record fromRec, Record toRec)
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Iterator<Record> iter = iterator(root, fromRec, toRec) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        // Note that this end the read-part (find the start), not the iteration.
        // Iterator read blocks still get handled.
        return iter ;
    }
    
    /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */ 
    private static Iterator<Record> iterator(BPTreeNode node, Record fromRec, Record toRec)
    { 
        // Look for starting RecordsBufferPage id.
        int id = BPTreeNode.recordsPageId(node, fromRec) ; 
        if ( id < 0 )
            return Iter.nullIter() ;
        RecordBufferPageMgr pageMgr = node.getBPlusTree().getRecordsMgr().getRecordBufferPageMgr() ;
        // No pages are active at this point.
        return RecordRangeIterator.iterator(id, fromRec, toRec, pageMgr) ;
    }
    
    private static Iterator<Record> iterator(BPTreeNode node)
    { 
        return iterator(node, null, null) ; 
    }
    
    // Internal calls.
    private void startReadBlkMgr()
    {
        nodeManager.startRead() ;
        recordsMgr.startRead() ;
    }

    private void finishReadBlkMgr()
    {
        nodeManager.finishRead() ;
        recordsMgr.finishRead() ;
    }

    private void startUpdateBlkMgr()
    {
        nodeManager.startUpdate() ;
        recordsMgr.startUpdate() ;
    }
    
    private void finishUpdateBlkMgr()
    {
        nodeManager.finishUpdate() ;
        recordsMgr.finishUpdate() ;
    }
    
    @Override
    public boolean isEmpty()
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        boolean b = ! root.hasAnyKeys() ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return b ;
    }
    
    private static int SLICE = 10000 ;
    @Override
    public void clear() {
        Record[] records = new Record[SLICE] ;
        while(true) {
            Iterator<Record> iter = iterator() ;
            int i = 0 ; 
            for ( i = 0 ; i < SLICE ; i++ ) {
                if ( ! iter.hasNext() )
                    break ;
                Record r = iter.next() ;
                records[i] = r ;
            }
            if ( i == 0 )
                break ;
            for ( int j = 0 ; j < i ; j++ ) {
                delete(records[j]) ;
                records[j] = null ;
            }
        }
    }
    
    @Override
    public void sync() 
    { 
        if ( nodeManager.getBlockMgr() != null )
            nodeManager.getBlockMgr().sync() ;
        if ( recordsMgr.getBlockMgr() != null )
            recordsMgr.getBlockMgr().sync() ;
    }
    
    @Override
    public void close()
    { 
        if ( nodeManager.getBlockMgr() != null )
            nodeManager.getBlockMgr().close()   ;
        if ( recordsMgr.getBlockMgr() != null )
            recordsMgr.getBlockMgr().close() ;
    }
    
//    public void closeIterator(Iterator<Record> iter)
//    {
//    }

    @Override
    public long size()
    {
        Iterator<Record> iter = iterator() ;
        return Iter.count(iter) ;
    }
    
    @Override
    public void check()
    {
        getRoot().checkNodeDeep() ;
    }

    public void dump()
    {
        getRoot().dump() ;
    }
    
    public void dump(IndentedWriter out)
    {
        getRoot().dump(out) ;
    }
}
