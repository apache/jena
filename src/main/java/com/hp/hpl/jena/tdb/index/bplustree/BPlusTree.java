/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.CheckingNode;
import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.CheckingTree;
import static java.lang.String.format;


import java.util.Iterator;


import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.iterator.Iter ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.sys.Session ;

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
 * Stores "records", which are a key and value (the valu emay be null).
 * 
 * In this B+Tree implementation, the (key,value) pairs are held in
 * RecordBuffer, which wrap a ByteBuffer that only has records in it.  
 * BPTreeRecords provides the B+Tree view of a RecordBuffer. All records
 * are in RecordBufefr - the "tree" part is an index for finding the right
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

public class BPlusTree implements Iterable<Record>, RangeIndex, Session
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
    
    private long sessionCounter = 0 ;              // Session counter
    private int rootIdx ;
    BPTreeNode root ;
    private BPTreeNodeMgr nodeManager ; 
    private BPTreeRecordsMgr recordsMgr; 
    private BPlusTreeParams bpTreeParams ;
    
    /** Create the in-memory structures to correspnond to
     * the supplied block managers for the persistent storage.
     * This is the normal way to create a B+Tree.
     */
    public static BPlusTree attach(BPlusTreeParams params, BlockMgr blkMgrNodes, BlockMgr blkMgrLeaves)
    { 
        BPlusTree bpt = new BPlusTree(params, blkMgrNodes, blkMgrLeaves) ;
        bpt.attach() ;
        return bpt ;
    }
    
    /** (Testing mainly) Make an in-memory B+Tree, with copy-in, copy-out block managers */
    public static BPlusTree makeMem(int order, int minRecords, int keyLength, int valueLength)
    { return makeMem(null, order, minRecords, keyLength, valueLength) ; }
    
    /** (Testing mainly) Make an in-memory B+Tree, with copy-in, copy-out block managers */
    public static BPlusTree makeMem(String name, int order, int minRecords, int keyLength, int valueLength)
    {
        BPlusTreeParams params = new BPlusTreeParams(order, keyLength, valueLength) ;
        
        int maxRecords = 2*minRecords ;
        //int rSize = RecordBufferPage.HEADER+(maxRecords*params.getRecordLength()) ;
        
        int blkSize = RecordBufferPage.calcBlockSize(params.getRecordFactory(), maxRecords) ;

        BlockMgr mgr1 = BlockMgrFactory.createMem(name+"(nodes)", params.getBlockSize()) ;
        BlockMgr mgr2 = BlockMgrFactory.createMem(name+"(records)", blkSize) ;
        
        BPlusTree bpTree = BPlusTree.attach(params, mgr1, mgr2) ;
        return bpTree ;
    }

    /** Create the in-memory structures to correspond to
     *  the supplied block managers for the persistent storage.
     *  Do not initialize root - only for testing.
     */
    public static BPlusTree dummy(BPlusTreeParams params, BlockMgr blkMgrNodes, BlockMgr blkMgrLeaves)
    { 
        return new BPlusTree(params, blkMgrNodes, blkMgrLeaves) ;
    }
    
    private BPlusTree(BPlusTreeParams params) { this.bpTreeParams = params ; }

    BPlusTree(int N, int recordLength, BlockMgr blkMgrNodes, BlockMgr blkMgrLeaves)
    {
        this(new BPlusTreeParams(N, recordLength, 0), blkMgrNodes, blkMgrLeaves) ;
    }

    BPlusTree(BPlusTreeParams params, BlockMgr blkMgrNodes, BlockMgr blkMgrLeaves)
    {
        // Consistency checks.
        this.bpTreeParams = params ;
        this.nodeManager = new BPTreeNodeMgr(this, blkMgrNodes) ;
        RecordBufferPageMgr recordPageMgr = new RecordBufferPageMgr(params.getRecordFactory(), blkMgrLeaves) ;
        recordsMgr = new BPTreeRecordsMgr(this, recordPageMgr) ;
    }

    /** Set up according to the attached block storage for the B+Tree */
    void attach()
    {
        // This fixes the root to being block 0
        if ( nodeManager.valid(0) )
        {
            nodeManager.startRead() ;       // Just the nodeManager
            // Existing BTree
            root = nodeManager.getRoot(rootIdx) ;
            rootIdx = root.getId() ;
            // Build root node.
            // Per session count only.
            sessionCounter = 0 ;
            nodeManager.finishRead() ;
        }
        else
        {
            startUpdateBlkMgr() ;
            // Fresh BPlusTree
            root = nodeManager.createEmptyBPT() ;
            rootIdx = root.getId() ;
            if ( rootIdx != 0 )
                throw new InternalError() ;
            sessionCounter = 0 ;
            if ( CheckingNode )
                root.checkNodeDeep() ;
            finishUpdateBlkMgr() ;
        }
    }

    /** Get the parameters describing this B+Tree */
    public BPlusTreeParams getParams()     { return bpTreeParams ; } 

    /** Only use for careful manipulation of structures */
    public BPTreeNodeMgr getNodeManager()          { return nodeManager ; }
    /** Only use for careful manipulation of structures */
    public BPTreeRecordsMgr getRecordsMgr()     { return recordsMgr ; }
    
    public RecordFactory getRecordFactory()
    {
        return bpTreeParams.recordFactory ;
    }
    
    public Record find(Record record)
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record v = root.search(record) ;
        if ( logging() )
            log.debug(format("find(%s) ==> %s", record, v)) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return v ;
    }
    
    private BPTreeNode getRoot()
    {
        // Cache it?
        // Across staert/Update
        //BPTreeNode root2 = nodeManager.getRoot(rootIdx) ;
        return root ;
    }
    
    private void releaseRoot(BPTreeNode root)
    {
        //nodeManager.releaseRoot(rootIdx) ;
        if ( root != this.root )
            log.warn("Root is not root!") ;
    }

    public boolean contains(Record record)
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        if ( logging() )
            log.debug(format("contains(%s)", record)) ;
        Record r = root.search(record) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return r != null ;
    }

    public Record minKey()
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record r = root.minRecord();
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return r ;
    }

    public Record maxKey()
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record r = root.maxRecord() ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return r ;
    }

    //@Override
    public boolean add(Record record)
    {
        return addAndReturnOld(record) == null ;
    }
    
    /** Add a record into the B+Tree */
    public Record addAndReturnOld(Record record)
    {
        if ( logging() )
            log.debug(format("add(%s)", record)) ;
        startUpdateBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record r = root.insert(record) ;
        if ( r == null )
            sessionCounter++ ;
        if ( CheckingTree ) root.checkNodeDeep() ;
        releaseRoot(root) ;
        finishUpdateBlkMgr() ;
        return r ;
    }
    
    public boolean delete(Record record)
    { return deleteAndReturnOld(record) != null ; }
    
    public Record deleteAndReturnOld(Record record)
    {
        if ( logging() )
            log.debug(format("delete(%s)", record)) ;
        startUpdateBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Record r =  root.delete(record) ;
        if ( r != null )
            sessionCounter -- ;
        if ( CheckingTree ) root.checkNodeDeep() ;
        releaseRoot(root) ;
        finishUpdateBlkMgr() ;
        return r ;
    }

    //@Override
    public Iterator<Record> iterator()
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Iterator<Record> iter = root.iterator() ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;    // WRONG!
        return iter ;
    }
    
    public Iterator<Record> iterator(Record fromRec, Record toRec)
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        Iterator<Record> iter = root.iterator(fromRec, toRec) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;    // WRONG!
        return iter ;
    }
    
    //@Override
    public void startRead()
    { }

    //@Override
    public void finishRead()
    { }

    //@Override
    public void startUpdate()
    { }
    
    //@Override
    public void finishUpdate()
    { }

    // Internal calls.
    //@Override
    private void startReadBlkMgr()
    {
        nodeManager.startRead() ;
        recordsMgr.startRead() ;
    }

    //@Override
    private void finishReadBlkMgr()
    {
        nodeManager.finishRead() ;
        recordsMgr.finishRead() ;
    }

    //@Override
    private void startUpdateBlkMgr()
    {
        nodeManager.startUpdate() ;
        recordsMgr.startUpdate() ;
    }
    
    //@Override
    private void finishUpdateBlkMgr()
    {
        nodeManager.finishUpdate() ;
        recordsMgr.finishUpdate() ;
    }

    //@Override
    public boolean isEmpty()
    {
        startReadBlkMgr() ;
        BPTreeNode root = getRoot() ;
        boolean b = ! root.hasAnyKeys() ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return b ;
    }
    
    //@Override
    public void clear()
    { throw new UnsupportedOperationException("RangeIndex("+Utils.classShortName(this.getClass())+").clear") ; }
    
    //@Override
    public void sync() 
    { 
        if ( nodeManager.getBlockMgr() != null )
            nodeManager.getBlockMgr().sync() ;
        if ( recordsMgr.getBlockMgr() != null )
            recordsMgr.getBlockMgr().sync() ;
    }
    
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

    public long sessionTripleCount()
    {
        return sessionCounter ;
    }

    public long size()
    {
        Iterator<Record> iter = iterator() ;
        return Iter.count(iter) ;
    }
    
    long sizeByCounting()
    {
        return root.size() ;
    }

    public void check()
    {
        root.checkNodeDeep() ;
    }

    public void dump()
    {
        root.dump() ;
    }
    
    public void dump(IndentedWriter out)
    {
        root.dump(out) ;
    }

    private static final boolean logging()
    {
        return BPlusTreeParams.logging(log) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */