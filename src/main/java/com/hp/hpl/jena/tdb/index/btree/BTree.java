/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.btree;

import static com.hp.hpl.jena.tdb.index.btree.BTreeParams.CheckingBTree ;
import static com.hp.hpl.jena.tdb.index.btree.BTreeParams.CheckingNode ;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.sys.Session ;

/** B-Tree
 * 
 * Taken from:
 * Introduction to Algorithms, Second Edition
 * Chapter 18: B-Trees
 * by Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest and Clifford Stein 
 *  
 * Includes implementation of removal.
 * 
 * Stores "records".  The record comparator can either match the whole record
 * (so the record is the key), or part of it (the record is a key and value parts).
 * 
 * This is a B-Tree (not a B+Tree) so if the record in's all key, the storage density
 * isn't as good.  But for complete indexes, the key is the record.
 *  
 * Notes:
 * 
 *   The version above splits nodes on the way down when full,
 *   not when needed where a split can bubble up from below.
 *   It means it only ever walks down the tree on insert.
 *   Similarly, the delete code ensures a node is suitable
 *   before decending. 
 *    
 *  Variations:
 *     + In this impl, splitRoot leaves the root node in place.
 *       The root is always the same block.
 */

public class BTree implements Iterable<Record>, RangeIndex, Session
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
    
    private static Logger log = LoggerFactory.getLogger(BTree.class) ;
    private int rootIdx ;
    BTreeNode root ;
    private BTreePageMgr pageMgr ; 
    private BTreeParams bTreeParams ;
    
    
    public static BTree makeMem(int N, int keyLength, int valueLength)
    { return makeMem(null, N, keyLength, valueLength) ; }
    
    public static BTree makeMem(String name, int N, int keyLength, int valueLength)
    {
        BTreeParams params = new BTreeParams(N, keyLength, valueLength) ;
        BlockMgr mgr = BlockMgrFactory.createMem(name, params.getBlockSize()) ;
        return new BTree(params, mgr) ;
    }
    
//    public BTree(int N, int keyLength, int valueLength, BlockMgr blkMgr)
//    {
//        this(new BTreeParams(N, keyLength, valueLength), blkMgr) ;
//    }
    
    public BTree(BTreeParams bTreeParams, BlockMgr blkMgr)
    {
        this.bTreeParams = bTreeParams ;
        this.pageMgr = new BTreePageMgr(this, blkMgr) ;
        if ( pageMgr.valid(0) )
        {
            startReadBlkMgr() ;
            // Existing BTree
            root = pageMgr.getRoot(rootIdx) ;
            rootIdx = root.id ;
            // Build root node.
            // Per session count only.
            finishReadBlkMgr() ;
        }
        else
        {
            startUpdateBlkMgr() ;
            // Fresh BTree
            root = pageMgr.createRoot() ;
            rootIdx = root.id ;
            if ( CheckingNode )
                root.checkNodeDeep() ;
            pageMgr.put(root) ;
            finishUpdateBlkMgr() ;
        }
    }
    
    BTreeParams getParams()     { return bTreeParams ; } 
    BTreePageMgr getPageMgr()   { return pageMgr ; }
    
    @Override
    public RecordFactory getRecordFactory()
    {
        return bTreeParams.recordFactory ;
    }
    
    @Override
    public Record find(Record record)
    {
        startReadBlkMgr() ;
        BTreeNode root = getRoot() ;
        Record v = root.search(record) ;
        finishReadBlkMgr() ;
        return v ;
    }
    
    private BTreeNode getRoot()
    {
        // Cache it?
        // Across staert/Update
        //BPTreeNode root2 = nodeManager.getRoot(rootIdx) ;
        return root ;
    }
    
    
    private void releaseRoot(BTreeNode root)
    {
        //nodeManager.releaseRoot(rootIdx) ;
        if ( root != this.root )
            log.warn("Root is not root!") ;
    }
    
    @Override
    public boolean contains(Record record)
    {
        startReadBlkMgr() ;
        BTreeNode root = getRoot() ;
        Record r = root.search(record) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return r != null ;
    }

    @Override
    public Record minKey()
    {
        startReadBlkMgr() ;
        BTreeNode root = getRoot() ;
        Record r = root.minRecord();
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return r ;
    }

    @Override
    public Record maxKey()
    {
        startReadBlkMgr() ;
        BTreeNode root = getRoot() ;
        Record r = root.maxRecord();
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return r ;
    }

    @Override
    public boolean add(Record record)
    {
        return addAndReturnOld(record) == null ;
    }
    
    /** Add a record into the BTree */
    public Record addAndReturnOld(Record record)
    {
        startUpdateBlkMgr() ;
        BTreeNode root = getRoot() ;
        Record r = root.insert(record) ;
        if ( CheckingBTree ) root.checkNodeDeep() ;
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
        BTreeNode root = getRoot() ;
        Record r = root.delete(record) ;
        if ( CheckingBTree ) root.checkNodeDeep() ;
        releaseRoot(root) ;
        finishUpdateBlkMgr() ;
        return r ;
    }

    @Override
    public Iterator<Record> iterator()
    {
        startReadBlkMgr() ;
        BTreeNode root = getRoot() ;
        Iterator<Record> iter = root.iterator() ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;    // WRONG!
        return iter ;
    }
    
    @Override
    public Iterator<Record> iterator(Record fromRec, Record toRec)
    {
        startReadBlkMgr() ;
        BTreeNode root = getRoot() ;
        Iterator<Record> iter = root.iterator(fromRec, toRec) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;    // WRONG!
        return iter ;
    }
    
    @Override
    public boolean isEmpty()
    {
        startReadBlkMgr() ;
        BTreeNode root = getRoot() ;
        boolean b = ( root.getCount() == 0 ) ;
        releaseRoot(root) ;
        finishReadBlkMgr() ;
        return b ;
    }

    @Override
    public void clear()
    { throw new UnsupportedOperationException("RangeIndex("+Utils.classShortName(this.getClass())+").clear") ; }
    
    @Override
    public void sync() 
    {
        if ( pageMgr.getBlockMgr() != null )
            pageMgr.getBlockMgr().sync()   ;
    }
    
    @Override
    public void close()
    { 
        if ( pageMgr.getBlockMgr() != null )
            pageMgr.getBlockMgr().close()   ;
    }
    
    @Override
    public void startRead()
    {}

    @Override
    public void finishRead()
    {}

    @Override
    public void startUpdate()
    {}

    @Override
    public void finishUpdate()
    {}

    // Internal calls.
    private void startReadBlkMgr()      { pageMgr.startRead() ; }

    private void finishReadBlkMgr()     { pageMgr.finishRead() ; }

    private void startUpdateBlkMgr()    { pageMgr.startUpdate() ; }
    
    private void finishUpdateBlkMgr()   { pageMgr.finishUpdate() ; }
    
    @Override
    public long size()
    { 
        Iterator<Record> iter = iterator() ;
        return Iter.count(iter) ;
    }

    public long sizeByCounting()
    {
        return root.sizeByCounting() ;
    }

    @Override
    public void check()
    {
        root.checkNodeDeep() ;
    }

    public void dump()
    {
        root.dump() ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
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