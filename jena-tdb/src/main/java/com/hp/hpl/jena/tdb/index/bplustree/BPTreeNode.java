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

import static com.hp.hpl.jena.tdb.base.record.Record.keyGT ;
import static com.hp.hpl.jena.tdb.base.record.Record.keyLT ;
import static com.hp.hpl.jena.tdb.base.record.Record.keyNE ;
import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.CheckingNode ;
import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.CheckingTree ;
import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.DumpTree ;
import static java.lang.String.format ;
import static org.apache.jena.atlas.lib.Alg.decodeIndex ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.buffer.PtrBuffer ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public final class BPTreeNode extends BPTreePage
{
    private static final short READ = 1 ;
    private static final short WRITE = 2 ;

    // Only "public" for external very low level tools in development to access this class.
    // Assume package access.

    private static Logger log = LoggerFactory.getLogger(BPTreeNode.class) ;
    
    private Block block ;
    private int id ;
    private short blockState = READ ;  
    
    int parent ;
    int count ;             // Number of records.  Number of pointers is +1
    
    // "Leaf" of the BPTree is the lowest level of ptr/key splits, not the data blocks.
    // We need to know this to know which block manager the block pointers refer to.
    boolean isLeaf ;        
    private RecordBuffer records ;
    void setRecordBuffer(RecordBuffer r) { records = r ; }
    PtrBuffer ptrs ;

    /* B+Tree
     * 
     * Two block managers : 
     *   one for Nodes (BPlusTreePages => BPlusTreeNode)
     *   one for Leaves (RecordBufferPages)
     * The split key is the held in the highest in the block  
     * 
     * A "leaf" node is a leaf of the B+Tree part, and points to 
     * highest record in a RecordBuffer 
     *
     * The Gap is usually zero.
     * N = 2, Gap = 1 =>
     *  2*N+Gap:  MaxRec = 4, MaxPtr = 5,
     *  Max-1:    HighRec = 3, HighPtr = 4
     *  N-1:      MinRec = 1, MinPtr = 2
     *
     * BPTreeNode:
     * 
     *      +------------------------+
     *      |-| K0 | K1 | K2 | K3 |--|
     *      +------------------------+
     *      | P0 | P1 | P2 | P3 | P4 |
     *      +------------------------+
     *
     *      +------------------------+
     *      | | K0 | K1 | ** | ** |--|
     *      +------------------------+
     *      | P0 | P1 | P2 | ** | ** |
     *      +------------------------+
     *      
     * BPTreeRecords -> RecordBuffer:
     *      
     *      +------------------------+
     *      | K0 | K1 | K2 | ** | ** |
     *      +------------------------+
     *      
     * The size of records blocks and size of tree nodes don't have to be the same.
     * They use different page managers, and are in different files.  
     *
     * The minimal tree is one, leaf, root BPTreeNode and one BPTreeRecords page.
     * 
     * Pictures:      
     *      /--\ \--\
     * means a block with free space introduced between records[i] and records[i+1], ptrs[i+1]/ptrs[i+2]
     * Lower half is a valid structure (except for overall size) 
     *       
     *      /--/ /--\
     * means a block with free space introduced between records[i] and records[i+1], ptrs[i]/ptrs[i+1]
     * Upper half is a valid structure (except for overall size) 
     */

    // Branch nodes only need create branch nodes (splitting sideways)
    // Leaf nodes only create leaf nodes.
    // The root is an exception.
    
    private BPTreeNode create(int parent, boolean isLeaf)
    {
        return create(bpTree, parent, isLeaf) ;
    }
    
    private static BPTreeNode create(BPlusTree bpTree, int parent, boolean isLeaf)
    {
        BPTreeNode n = bpTree.getNodeManager().createNode(parent) ;
        n.isLeaf = isLeaf ;
        return n ;
    }
    
    /*package*/ BPTreeNode(BPlusTree bpTree, Block block)
    {
        super(bpTree) ;
        this.block = block ;
        this.id = block.getId().intValue() ;
    }

    @Override
    public void reset(Block block) 
    { 
        this.block = block ;
        // reformat block (sets record and pointer buffers)
        BPTreeNodeMgr.formatBPTreeNode(this, bpTree, block, isLeaf, parent, count) ;
    }
    
    // [TxTDB:PATCH-UP] REMOVE
    //private BPTreePage get(int idx) { return get(idx, WRITE) ; }
    
    /** Get the page at slot idx - switch between B+Tree and records files */ 
    private BPTreePage get(int idx, short state)
    {
        int subId = ptrs.get(idx) ;
        if ( state == READ )
            return getMgrRead(subId) ;
        if ( state == WRITE )
            return getMgrWrite(subId) ;
        log.error("Unknown state: "+state) ;
        return null ;   
    }
    
    private BPTreePage getMgrRead(int subId)
    {
        if ( isLeaf )
            return bpTree.getRecordsMgr().getRead(subId) ;
        else
            return bpTree.getNodeManager().getRead(subId, this.id) ;
    }
    
    private BPTreePage getMgrWrite(int subId)
    {
        // [TxTDB:PATCH-UP]
        if ( isLeaf )
            return bpTree.getRecordsMgr().getWrite(subId) ;
        else
            return bpTree.getNodeManager().getWrite(subId, this.id) ;
    }

    // ---------- Public calls.
    // None of these are called recursively.
    
    /** Find a record, using the active comparator */
    public static Record search(BPTreeNode root, Record rec)
    {
        root.internalCheckNodeDeep() ;
        if ( root.id != 0 )
            throw new BPTreeException("Search not starting from the root: "+root) ;
        Record r = root.internalSearch(rec) ;
        return r ;
    }

    /** Insert a record - return existing value if any, else null */
    public static Record insert(BPTreeNode root, Record record)
    {
        // [TxTDB:PATCH-UP] - put in BPlusTree.
        if ( logging() )
        {
            log.debug(format("** insert(%s) / start", record)) ;
            if ( DumpTree ) root.dump() ;
        }
     
        if ( ! root.isRoot() )
            throw new BPTreeException("Insert begins but this is not the root") ;
        
        if ( root.isFull() )
        {
            // Root full - root split is a special case.
            splitRoot(root) ;
            if ( DumpTree ) root.dump() ;
        }
        
        // Root ready - call insert proper.
        Record result = root.internalInsert(record) ;
        
        root.internalCheckNodeDeep() ;
    
        if ( logging() )
        {
            log.debug(format("** insert(%s) / finish", record)) ;
            if ( DumpTree ) root.dump() ;
        }
        return result ;
    }

    /** Delete a record - return the old value if there was one, else null*/
    public static Record delete(BPTreeNode root, Record rec)
    { 
        if ( logging() )
        {
            log.debug(format("** delete(%s) / start", rec)) ;
            if ( DumpTree ) root.dump() ;
        }
        if ( ! root.isRoot() )
            throw new BPTreeException("Delete begins but this is not the root") ;
    
        if ( root.isLeaf && root.count == 0 )
        {
            // Special case.  Just a records block.  Allow that to go too small.
            BPTreePage page = root.get(0, WRITE) ;
            if ( CheckingNode && ! ( page instanceof BPTreeRecords ) )
                root.error("Zero size leaf root but not pointing a records block") ;
            Record r = page.internalDelete(rec) ;
            page.release() ;
            return r ;
        }
        
        // Entry: checkNodeDeep() ;
        Record v = root.internalDelete(rec) ;

        // Fix root in case it became empty in deletion process.
        if ( ! root.isLeaf && root.count == 0 )
        {
            root.reduceRoot() ;
            root.internalCheckNodeDeep() ;
        }
        
        if ( logging() )
        {
            log.debug(format("** delete(%s) / finish", rec)) ;
            if ( DumpTree ) root.dump() ;
        }
        return v ;
    }
    
    /** Returns the id of the records buffer page for this record.  Records Buffer Page NOT read; record may not exist */ 
    static int recordsPageId(BPTreeNode node, Record fromRec)
    {
        // Walk down the B+tree part of the structure ...
        while ( !node.isLeaf() )
        {
            BPTreePage page = (fromRec == null ) ? node.get(0, READ) : node.findHere(fromRec) ;
            // Not a leaf so we can cast safely.
            BPTreeNode n = (BPTreeNode)page ;
            // Release if not root.
            if ( ! node.isRoot() )
                node.release() ;
            node = n ;
        }
        // ... then find the id of the next step down, but do not touch the records buffer page. 
        int id ;
        if ( fromRec == null )
        {
            // Just get the lowest starting place.
            id = node.getPtrBuffer().getLow() ;
        }
        else
        {
            // Get the right id based on starting record.
            int idx = node.findSlot(fromRec) ; 
            idx = convert(idx) ;
            id = node.getPtrBuffer().get(idx) ;
        }
        if ( ! node.isRoot() )
            node.release() ;
        return id ;
    }



    @Override
    protected Record maxRecord()
    {
        BPTreePage page = get(count, READ) ;
        Record r = page.maxRecord() ;
        page.release() ;
        return r ;
    }

    @Override
    protected Record minRecord()
    {
        BPTreePage page = get(0, READ) ;
        Record r = page.minRecord() ;
        page.release() ;
        return r ;
    }

//    @Override
//    protected BPTreeRecords findPage(Record rec)
//    {
//        if ( CheckingNode ) internalCheckNode() ;
//        
//        BPTreePage page = findHere(rec) ;
//        if ( page == null )
//            return null ;
//        BPTreeRecords bpr = page.findPage(rec) ;
//        page.release() ;
//        return bpr ;
//    }
//    
//    // Find first page.
//    @Override
//    BPTreeRecords findFirstPage()
//    {
//        BPTreePage page = get(0, READ) ;
//        BPTreeRecords records = page.findFirstPage() ;
//        page.release() ;
//        // Err - records is released!
//        return records ;
//    }

    @Override final
    Record getLowRecord()
    {
        return records.getLow() ;
    }

    @Override final
    Record getHighRecord()
    {
        return records.getHigh() ; 
    }
    
    // count is the number of pointers.
    
    @Override
    final int getMaxSize()           { return params.getOrder() ; }
    
    @Override
    final int getCount()             { return count ; }
 
    @Override
    final void setCount(int count)   { this.count = count ; }
    
    @Override
//    public ByteBuffer getBackingByteBuffer()       { return byteBuffer ; }
    public Block getBackingBlock()       { return block ; }
    
    /** Do not use without great care */
    RecordBuffer getRecordBuffer()   { return records ; }
    /** Do not use without great care */
    PtrBuffer getPtrBuffer()         { return ptrs ; }
    
    void setIsLeaf(boolean isLeaf)   { this.isLeaf = isLeaf ; }

    boolean isLeaf()                 { return this.isLeaf ; }
    
    @Override
    public final int getId()                { return id ; }

    @Override
    final void write()          { bpTree.getNodeManager().write(this) ; } 
    
    @Override
    final void promote()        { bpTree.getNodeManager().promote(this) ; }

    @Override
    final void release()        { bpTree.getNodeManager().release(this) ; } 

    @Override
    final void free()           { bpTree.getNodeManager().free(this) ; } 
    
    
    // ============ SEARCH
    
    /* 
     * Do a (binary) search of the node to find the record.
     *   Returns: 
     *     +ve or 0 => the index of the record 
     *     -ve => The insertion point : the immediate higher record or length as (-i-1)
     *  Convert to +ve and decend to find the RecordBuffer with the record in it. 
     */
    
    @Override final
    Record internalSearch(Record rec)
    {
        if ( CheckingNode ) internalCheckNode() ;
        BPTreePage page = findHere(rec) ;
        Record r = page.internalSearch(rec) ;
        page.release() ;
        return r ;
    }

    /** Find the next page to look at as we walk down the tree */
    private final BPTreePage findHere(Record rec)
    {
        int idx = findSlot(rec) ; 
        idx = convert(idx) ;
        // Find index, or insertion point (immediate higher slot) as (-i-1)
        // A key is the highest element of the records up to this point
        // so we search down at slot idx (between something smaller and something
        // larger.
        BPTreePage page = get(idx, READ) ;
        return page ;
    }
    
    // ============ INSERT
    
    /* Traverse this page, ensuring the node below is not full before
     * decending.  Therefore there is always space to do the actual insert.
     */
    
    @Override final
    Record internalInsert(Record record)
    {
        if ( logging() )
            log.debug(format("internalInsert: %s [%s]", record, this)) ;
        
        internalCheckNode() ;
        
        int idx = findSlot(record) ;

        if ( logging() )
            log.debug(format("internalInsert: idx=%d=>%d", idx, convert(idx))) ;
        
        idx = convert(idx) ;
        
        BPTreePage page = get(idx, READ) ;
        
        if ( logging() )
            log.debug(format("internalInsert: next: %s",page));
        
        if ( page.isFull() )
        {
            // Need to split the page before descending.
            split(idx, page) ;
            // Did it shift the insert index?
            // Examine the record we pulled up in the split.
            if ( Record.keyGT(record, records.get(idx)) )
            {
                page.release() ;
                // Yes.  Get the new (upper) page
                idx = idx+1 ;
                page = get(idx, READ) ;
            }
            internalCheckNode() ;
        }

        Record r = page.internalInsert(record) ;
        page.release() ;
        return r ;
    }

    private static int convert(int idx)
    {
        if ( idx >= 0 ) return idx ;
        return decodeIndex(idx) ;
    }

    // **** Old documentation
    /* Split a non-root node y, held at slot idx.
     * Do this by splitting the node in two (call to BPTree.split)
     * and insertting the new key/pointer pair.
     * WRITE(y)
     * WRITE(z)
     * WRITE(this)
     */
    private void split(int idx, BPTreePage y)
    {
        boolean logging = logging() ; 
        //logging = true ;
        if ( logging )
        {
            log.debug(format("split >> y.id=%d  this.id=%d idx=%d", y.getId(), this.id, idx)) ;
            log.debug("split --   "+y) ;
        }
            
        internalCheckNode() ;
        if ( CheckingNode )
        {
            if ( ! y.isFull() ) error("Node is not full") ;
            if ( this.ptrs.get(idx) != y.getId() )
            {
                int a = this.ptrs.get(idx) ;
                int b = y.getId();
                error("Node to be split isn't in right place [%d/%d]", a, b) ;
            }
        }
        internalCheckNodeDeep() ;
        
        promote() ;
        y.promote() ;
        
        Record splitKey = y.getSplitKey() ;
        splitKey = keyRecord(splitKey) ;
        
        if ( logging )
            log.debug(format("Split key: %s", splitKey)) ;

        BPTreePage z = y.split();
        if ( logging )
        {
            log.debug(format("Split: %s", y)) ;
            log.debug(format("Split: %s", z)) ;
        }
        
        // Key only.
        if ( splitKey.hasSeparateValue() )
        {
            // [Issue: FREC]
            // This creates a empty (null-byte-initialized) value array.
            splitKey = params.getKeyFactory().create(splitKey.getKey()) ;

            // Better: but an on-disk change. This is key only.
            // splitKey = params.getKeyFactory().createKeyOnly(splitKey) ;
        }        
        
        // Insert new node. "add" shuffle's up as well.
        records.add(idx, splitKey) ;
        ptrs.add(idx+1, z.getId()) ;
        count++ ;
        
        if ( logging )
        {
            log.debug("split <<   "+this) ;
            log.debug("split <<   "+y) ;
            log.debug("split <<   "+z) ;
        }
        
        y.write();
        z.write();
        z.release() ;
        // y.release() ; y release management done by caller.
        this.write();
        if ( CheckingTree )
        {
            if ( Record.keyNE(splitKey, y.maxRecord()) )
                error("Split key %d but max subtree %s", splitKey, y.maxRecord()) ;
            internalCheckNodeDeep() ;
        }
    }
    
    @Override final
    Record getSplitKey()
    {
        int ix = params.SplitIndex ;
        Record split = records.get(ix) ; 
        return split ;
    }
    
    /** Split this block - return the split record (key only needed) */
    @Override final
    BPTreePage split()
    {
        // Median record : will go in parent.
        int ix = params.SplitIndex ;

        // New block.
        BPTreeNode z = create(this.parent, isLeaf) ;
        
        // Leave the low end untouched and copy, and clear the high end.
        // z becomes the new upper node, not the lower node.
        // 'this' is the lower block.
        
        int maxRec = maxRecords() ;
        // Copy from top of y into z. 
        records.copy(ix+1, z.records, 0, maxRec-(ix+1)) ;
        records.clear(ix, maxRec-ix) ;                    // Clear copied and median slot 
        records.setSize(ix) ;                             // Reset size
        
        ptrs.copy(ix+1, z.ptrs, 0, params.MaxPtr-(ix+1)) ;
        ptrs.clear(ix+1, params.MaxPtr-(ix+1)) ;
        ptrs.setSize(ix+1) ;

        // Set sizes of subnodes
        setCount(ix) ;                          // Median is ix
        internalCheckNode() ;                   // y finished
        
        z.isLeaf = isLeaf ; 
        z.setCount(maxRec - (ix+1)) ;           // Number copied into z

        // Caller puts the blocks in split(int, BTreePage)
        z.internalCheckNode() ;
        return z ;
    }
    
    /* Split the root and leave the root block as the root.
     * This is the only point the height of the tree increases.
     *
     *  Allocate new blocks.
     *  Copy root low into left
     *  Copy root high into right
     *  Set counts.
     *  Create new root settings (two pointers, one key record) 
     *  WRITE(left)
     *  WRITE(right)
     *  WRITE(root)
     */
    private static void splitRoot(BPTreeNode root)
    {
        BPlusTree bpTree = root.bpTree ;
        
        if ( CheckingNode )
            if ( root.id != 0 ) root.error("Not root: %d (root is id zero)", root.id) ;
        root.internalCheckNode() ;
        root.promote() ;
        
        // Median record
        int splitIdx = root.params.SplitIndex ;
        Record rec = root.records.get(splitIdx) ;
        
        if ( logging() )
        {
            log.debug(format("** Split root %d (%s)", splitIdx, rec)) ;
            log.debug("splitRoot >>   "+root) ;
        }

        // New blocks.
        BPTreeNode left = create(bpTree, root.id, root.isLeaf) ;
        BPTreeNode right = create(bpTree, root.id, root.isLeaf) ;
        
        //int maxRecords = maxRecords() ;
        
        // New left
        root.records.copy(0, left.records, 0, splitIdx) ;
        root.ptrs.copy(0, left.ptrs, 0, splitIdx+1) ;
        left.count = splitIdx ;

        // New right
        root.records.copy(splitIdx+1, right.records, 0, root.maxRecords()-(splitIdx+1)) ;
        root.ptrs.copy(splitIdx+1, right.ptrs, 0, root.params.MaxPtr-(splitIdx+1)) ;
        right.count = root.maxRecords()-(splitIdx+1) ;
        
        if ( logging() )
        {
            log.debug("splitRoot -- left:   "+left) ;
            log.debug("splitRoot -- right:  "+right) ;
        }
        
        // So left.count+right.count = bTree.NumRec-1
        
        // Clear root by reformatting.  New root not a leaf.  Has count of 1 after formatting.
        BPTreeNodeMgr.formatForRoot(root, false) ;
        // Make a non-leaf.
        
        // Insert two subnodes, divided by the median record
        root.count = 1 ;
        
        root.records.add(0, rec) ;
        root.ptrs.setSize(2) ;
        root.ptrs.set(0, left.id) ;        // slot 0
        root.ptrs.set(1, right.id) ;       // slot 1
        
        if ( logging())
        {
            log.debug("splitRoot <<   "+root) ;
            log.debug("splitRoot <<   "+left) ;
            log.debug("splitRoot <<   "+right) ;
        }

        left.write() ;
        right.write() ;
        left.release() ;
        right.release() ;
        root.write() ;

        if ( CheckingTree )
            root.checkNodeDeep() ;
        else
            if ( CheckingNode )
            {
                root.internalCheckNode() ;
                left.internalCheckNode() ;
                right.internalCheckNode() ;
            }
    }

    // ============ DELETE

    /* Delete
     * Descend, making sure that the node is not minimum size at each descend.
     * If it is, rebalenace.
     */
    
    @Override final
    Record internalDelete(Record rec)
    {
        internalCheckNode() ;
        if ( logging() )
            log.debug(format("internalDelete(%s) : %s", rec, this)) ;
        
        int x = findSlot(rec) ;

        // If x is >= 0, may need to adjust this 
        int y = convert(x) ;
        BPTreePage page = get(y, READ) ;
        
        boolean thisWriteNeeded = false ;
        if ( page.isMinSize() )             // Can't be root - we decended in the get(). 
        {
            promote() ;
            page = rebalance(page, y) ;
            thisWriteNeeded = true ;
            // May have moved/removed at x.  Find again. YUK.
            x = findSlot(rec) ;
            if ( CheckingNode )
            {
                internalCheckNode() ;
                page.checkNode() ;
            }
            this.write() ;
        }
        
        // Go to bottom
        // Need to return the new key.
        Record r2 =  page.internalDelete(rec) ;
        if ( x >= 0 )
        {
            promote() ;
            // YUK
            records.set(x, keyRecord(page.maxRecord())) ;
            this.write() ;
        }

        page.release() ;
        return r2 ;
    }

    /* Reduce the root when it has only one pointer and no records.
     * Keep the root as id 0 so this is just a copy-up of the one child node.
     * WRITE(root)
     * RELEASE(old child)
     * This is the only point the height of the tree decreases.
     */ 
    
    private void reduceRoot()
    {
        if ( logging() )
            log.debug(format("reduceRoot >> %s", this)) ;
        
        if ( CheckingNode && ( ! isRoot() || count != 0 ) ) error("Not an empty root") ;
        
        if ( isLeaf )
        {
            if ( logging() )
                log.debug(format("reduceRoot << leaf root")) ;
            // Now empty leaf root.
            return ;
        }
        
        BPTreePage sub = get(0, WRITE) ;
        BPTreeNode n = cast(sub) ;
        // Can pull up into the root.
        // Leave root node in same block (rather than swap to new root).
        BPTreeNodeMgr.formatForRoot(this, n.isLeaf) ;
        n.records.copy(0, this.records, 0, n.count) ;
        n.ptrs.copy(0, ptrs, 0, n.count+1) ;
        isLeaf = n.isLeaf ;
        count = n.count ;
        this.write();
        // Free up.
        n.free() ;
        internalCheckNodeDeep() ;
        
        if ( logging() )
            log.debug(format("reduceRoot << %s", this)) ;
    }

    /* Rebalance node n at slot idx in parent (this)
     * The node will then be greater than the minimum size
     * and one-pass delete is then possible. 
     * 
     * try to shift right, from the left sibling (if exists)
     *   WRITE(left)
     *   WRITE(n)
     *   WRITE(this)
     * try to shift left, from the right sibling (if exists)
     *   WRITE(right)
     *   WRITE(n)
     *   WRITE(this)
     * else 
     *  merge with left or right sibling
     * Suboperations do all the write-back of nodes.
     */ 
    private BPTreePage rebalance(final BPTreePage node, int idx)
    {
        if ( logging() )
        {
            log.debug(format("rebalance(id=%d, idx=%d)", node.getId(), idx)) ;
            log.debug(format(">> this: %s", this)) ;
            log.debug(format(">> node: %s", node)) ;
        }
        internalCheckNode() ;
        promote() ;
        node.promote() ;
        
        BPTreePage left = null ;
        if ( idx > 0 )
            // [TxTDB:PATCH-UP] 
            // release on left
            left = get(idx-1, WRITE) ;
        
        // *** SHIFTING : need to change the marker record in the parent.
        // *** getHighRecord of lower block.
        
        if ( left != null && ! left.isMinSize() )
        {
            if ( logging() )
                log.debug("rebalance/shiftRight") ;
            
            // Move elements around.
            // Has not done "this.put()" yet.
            shiftRight(left, node, idx-1) ;
            
            if ( logging() )
                log.debug("<< rebalance: "+this) ;
            if ( CheckingNode )
            {
                left.checkNode() ;
                node.checkNode();
                this.internalCheckNode() ;
            }
            left.release() ;
            return node ;
        }

        BPTreePage right = null ;
        if ( idx < count )
            right = get(idx+1, WRITE) ;
        
        if ( right != null && ! right.isMinSize() )
        {
            if ( logging() )
                log.debug("rebalance/shiftLeft") ;

            shiftLeft(node, right, idx) ;

            if ( logging() )
                log.debug("<< rebalance: "+this) ;
            if ( CheckingNode )
            {
                right.checkNode();
                node.checkNode();
                this.internalCheckNode() ;
            }
            if ( left != null ) left.release() ;
            right.release() ;
            return node ;
        }

        // Couldn't shift.  Collapse two pages.  
        if ( CheckingNode && left == null && right == null) error("No siblings") ;

        if ( left != null )
        {
            if ( logging() )
                log.debug(format("rebalance/merge/left: left=%d n=%d [%d]", left.getId(), node.getId(), idx-1)) ;
            if ( CheckingNode && left.getId() == node.getId() ) 
                error("Left and n the same: %s", left) ;
            BPTreePage page = merge(left, node, idx-1) ;
            if ( right != null )
                // HACK : We didn't use it.
                right.release() ;
            return page ;
        }
        else
        {
            // left == null
            // rigth != null
            if ( logging() )
                log.debug(format("rebalance/merge/right: n=%d right=%d [%d]", node.getId(), right.getId(), idx)) ;
            if ( CheckingNode && right.getId() == node.getId() )
                error("N and right the same: %s",right ) ;
            BPTreePage page = merge(node, right, idx) ;
            return page ;
        }
    }
    
    /** Merge left with right ; fills left, frees right */
    private BPTreePage merge(BPTreePage left, BPTreePage right, int dividingSlot)
    {
        if ( logging() )
        {
            log.debug(format(">> merge(@%d): %s", dividingSlot, this)) ;
            log.debug(">> left:  "+left) ;
            log.debug(">> right: "+right) ;
        }
        
        // /==\ + key + /==\   ==>   /====\ 
        Record splitKey = records.get(dividingSlot) ;
        BPTreePage page = left.merge(right, splitKey) ;
        // Must release right (not done in merge)
        if ( logging() )
            log.debug("-- merge: "+page) ;

        left.write();
        right.free() ;
        
        if ( page == right )
            error("Returned page is not the left") ;
            
        // Depending on whether there is a gap or not.
        if ( CheckingNode )
        {
            if ( isLeaf )
            {
                // If two data blocks, then the split key is not inlcuded (it's alread ythere, with it value)
                // Size is N+N and max could be odd so N+N and N+N+1 are possible. 
                if ( left.getCount()+1 != left.getMaxSize() && left.getCount() != left.getMaxSize() )
                    error("Inconsistent data node size: %d/%d", left.getCount(), left.getMaxSize()) ;
            }
            else if ( ! left.isFull() )
            {
                // If not two data blocks, the left side should now be full (N+N+split) 
                error("Inconsistent node size: %d/%d", left.getCount(), left.getMaxSize()) ;
            }
        }

        // Remove from parent (which is "this")
        shuffleDown(dividingSlot) ;
        this.write();
        internalCheckNodeDeep() ;
        if ( logging() )
        {
            log.debug("<< merge: "+this) ;
            log.debug("<< left:  "+left) ;
        }
        return left ;
        
//         }
//        else if ( page == right )
//        {
//            // Never happnes?
//            // Depending on whether there is a gap or not.
//            if ( CheckingNode && ! right.isFull() )
//                error("Inconsistent node size: %d", right.getCount()) ; 
//            // Remove from parent (which is "this")
//            shuffleDown(dividingSlot) ;
//            right.put() ;
//            left.release() ;
//            this.put() ;
//            internalCheckNodeDeep() ;
//            if ( logging() )
//            {
//                log.debug("<< merge: "+this) ;
//                log.debug("<< right:  "+right) ;
//            }
//            return right ;
//        }
//        else
//        {
//            error("merge: returned page is neither left nor right") ;
//            return null ; 
//        }
    }

    @Override
    BPTreePage merge(BPTreePage right, Record splitKey)
    {
        return merge(this, splitKey, cast(right)) ;
    }

    private static BPTreeNode merge(BPTreeNode left, Record splitKey, BPTreeNode right)
    {
        // Merge blocks - does not adjust the parent.
        // Copy right to top of left.
        // Caller releases 'right' (needed for testing code).

        left.records.add(splitKey) ;
        
        // Copy over right to top of left.
        right.records.copyToTop(left.records) ;
        right.ptrs.copyToTop(left.ptrs) ;
        
        // Update count
        left.count = left.count + right.count + 1  ;
        left.internalCheckNode() ;
        
        right.records.clear();
        right.ptrs.clear();
        return left ;
    }

    private void shiftRight(BPTreePage left, BPTreePage right, int i)
    {
        if ( logging() )
        {
            log.debug(">> shiftRight: this:  "+this) ;
            log.debug(">> shiftRight: left:  "+left) ;
            log.debug(">> shiftRight: right: "+right) ;
        }
        Record r1 = records.get(i) ;
        Record r2 = left.shiftRight(right, r1) ;
        r2 = keyRecord(r2) ;
        this.records.set(i, r2) ;
        
        left.write() ;
        right.write() ;
        // Do later -- this.put();
        if ( logging() )
        {
            log.debug("<< shiftRight: this:  "+this) ;
            log.debug("<< shiftRight: left:  "+left) ;
            log.debug("<< shiftRight: right: "+right) ;
        }
    }

    private void shiftLeft(BPTreePage left, BPTreePage right, int i)
    {
        if ( logging() )
        {
            log.debug(">> shiftLeft: this:  "+this) ;
            log.debug(">> shiftLeft: left:  "+left) ;
            log.debug(">> shiftLeft: right: "+right) ;
        }
        Record r1 = records.get(i) ;
        Record r2 = left.shiftLeft(right, r1) ;
        r2 = keyRecord(r2) ;
        this.records.set(i, r2) ;
        
        left.write() ;
        right.write() ;
        // Do this later - this.put();
        if ( logging() )
        {
            log.debug("<< shiftLeft: this:  "+this) ;
            log.debug("<< shiftLeft: left:  "+left) ;
            log.debug("<< shiftLeft: right: "+right) ;
        }
    }

    @Override
    Record shiftRight(BPTreePage other, Record splitKey)
    {
        BPTreeNode node = cast(other) ;
        if ( CheckingNode )
        {
            if (count == 0 ) error("Node is empty - can't shift a slot out") ;
            if ( node.isFull() ) error("Destination node is full") ;
        }
        // Records: promote moving element, replace with splitKey
        Record r = this.records.getHigh() ;
        this.records.removeTop() ;
        node.records.add(0, splitKey) ;
        
        // Pointers just shift
        this.ptrs.shiftRight(node.ptrs) ; 
        
        this.count -- ;
        node.count ++ ;
        this.internalCheckNode() ;
        node.internalCheckNode() ;
        return r ;
    }

    @Override
    Record shiftLeft(BPTreePage other, Record splitKey)
    {
        BPTreeNode node = cast(other) ;
        if ( CheckingNode )
        {
            if ( count == 0 ) error("Node is empty - can't shift a slot out") ;
            if ( isFull() ) error("Destination node is full") ;
        }
        Record r = node.records.getLow() ;
        // Records: promote moving element, replace with splitKey
        this.records.add(splitKey) ;
        node.records.shiftDown(0) ;
        
        // Pointers just shift
        this.ptrs.shiftLeft(node.ptrs) ;
        
        this.count ++ ;
        node.count -- ;
        return r ;
    }

    private void shuffleDown(int x)
    {
        // x is the index in the parent and may be on eover the end. 
        if ( logging() )
        {
            log.debug(format("ShuffleDown: i=%d count=%d MaxRec=%d", x, count, maxRecords())) ;
            log.debug("shuffleDown >> "+this) ;
        }

        if ( CheckingNode && x >= count ) error("shuffleDown out of bounds") ;

        // Just the top to clear

        if ( x == count-1 )
        {
            records.removeTop() ;
            ptrs.removeTop() ;

            count-- ;
            if ( logging() )
            {
                log.debug("shuffleDown << Clear top") ;
                log.debug("shuffleDown << "+this) ;
            }
            internalCheckNode() ;
            return ;
        }

        // Shuffle down. Removes key and pointer just above key.
        
        records.shiftDown(x) ;
        ptrs.shiftDown(x+1) ;  
        count -- ;
        if ( logging() )
            log.debug("shuffleDown << "+this) ;
        internalCheckNode() ;
    }

    // ---- Utilities

    private final BPTreeNode cast(BPTreePage other)
    {
        try { return (BPTreeNode)other  ; }
        catch (ClassCastException ex) { error("Wrong type: "+other) ; return null ; }
    }

    final int findSlot(Record rec)
    {
        int x = records.find(rec) ;
        return x ;
    }
    
    final boolean isRoot()
    {
        // No BPT remembered root node currently 
        //if ( bpTree.root == this ) return true ;
        return this.id == BPlusTreeParams.RootId ;
    }

    private Record keyRecord(Record record)
    {
        return bpTree.getRecordFactory().createKeyOnly(record) ;
    }

    // Fixup/remove? 
    private final int maxRecords() { return params.MaxRec ; }
    
    @Override
    final boolean isFull()
    {
        if ( CheckingNode && count > maxRecords()  )
            error("isFull: Moby block: %s", this) ;
        
        // Count is of records.  
        return count >= maxRecords() ;
    }
    
    /** Return true if there are no keys here or below this node */
    @Override
    final boolean hasAnyKeys()
    {
        if ( this.count > 0 ) 
            return true ;
        if ( ! isRoot() )
            return false ;
        
        // The root can be zero size and point to a single data block.
        int id = this.getPtrBuffer().getLow() ;
        BPTreePage page = get(id, READ) ;
        boolean b = page.hasAnyKeys() ;  
        page.release() ;
        return b ;
    }


    
    @Override
    final boolean isMinSize()
    {
        int min = params.getMinRec() ;
        if ( CheckingNode && count < min  )
            error("isMinSize: Dwarf block: %s", this) ;
        
        return count <= min ;
    }
    
    // ========== Other
    
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder() ;
        if ( isLeaf )
            b.append("LEAF: ") ;
        else
            b.append("NODE: ") ;
        String labelStr = "??" ;
        if ( parent >= 0 )
            labelStr = Integer.toString(parent) ;
        else if ( parent == BPlusTreeParams.RootParent )
            labelStr = "root" ;
        if ( isLeaf )
            labelStr = labelStr+"/leaf" ;
        
        b.append(String.format("%d [%s] (size %d) -- ", id, labelStr, count)) ;
        for ( int i = 0 ; i < maxRecords() ; i++ )
        {
            b.append(childStr(i)) ;
            b.append(" (") ;
            b.append(recstr(records, i)) ;
            b.append(") ") ;
        }
        b.append(childStr(params.HighPtr)) ;
        return b.toString() ;
    }

    private final String recstr(RecordBuffer records, int idx)
    {
        if ( records.isClear(idx) )
            return "----" ;

        Record r = records._get(idx) ;
        return r.toString() ;
    }
    
    public void dump()
    {
        dump(IndentedWriter.stdout) ;
    }

    public void dump(IndentedWriter out)
    {
        output(out) ;
        out.ensureStartOfLine() ;
        out.flush();
    }
    
    public String dumpToString()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        output(buff) ;
        return buff.asString() ;
    }
    
    
    @Override
    public void output(IndentedWriter out)
    {
        out.print(toString()) ;
        out.incIndent() ;
        for ( int i = 0 ; i < count+1 ; i++ )
        {
            out.println();
            BPTreePage page = get(i, READ) ;
            page.output(out) ;
            page.release() ;
            
        }
        out.decIndent() ;
    }

//    private void indent(PrintStream out, int x)
//    {
//        for ( int i = 0 ; i < x ; i++ )
//            out.print("  ") ;
//    }

    private String childStr(int i)
    {
        if ( i >= ptrs.size() )
            return "*" ;
        int x = ptrs.get(i) ;
        return Integer.toString(x) ; 
    }
    
    // =========== Checking
    // internal checks - only if checking
    
    // Check node does not assume a valid tree - may be in mid-operation. 
    private final void internalCheckNode()
    { 
        if ( CheckingNode )
            checkNode(null, null) ;
    }
    
    private final void internalCheckNodeDeep()
    {
        if ( ! CheckingTree )
            return ;
        checkNodeDeep() ;
    }

    @Override
    final void checkNode()
    {
        checkNode(null, null) ;
    }

    @Override
    final void checkNodeDeep()
    {
        if ( isRoot() )
        {
//            if ( !isLeaf && count == 0 )
//                error("Root is of size zero (one pointer) but not a leaf") ;
            if ( parent != BPlusTreeParams.RootParent )
                error("Root parent is wrong") ;
            //            if ( count == 0 )
            //                return ;
        }
        checkNodeDeep(null, null) ;
    }

    // Checks of a single node - no looking at children
    // min - inclusive; max - inclusive (allows for duplicates) 
    final private void checkNode(Record min, Record max)
    {
        if ( count != records.size() )
            error("Inconsistent: id=%d, count=%d, records.size()=%d : %s", id, count, records.size(), this) ; 
        
        if ( ! isLeaf && count+1 != ptrs.size() )
            error("Inconsistent: id=%d, count+1=%d, ptrs.size()=%d ; %s", id, count+1, ptrs.size(), this) ; 
    
        // No BPT remembered root node currently 
        //if ( bpTree.root != null && !isRoot() && count < params.MinRec)
        if ( !isRoot() && count < params.MinRec)
        {
            //warning("Runt node: %s", this) ;
            error("Runt node: %s", this) ;
        }
        if ( !isRoot() && count > maxRecords() ) error("Over full node: %s", this) ;
        if ( ! isLeaf && parent == id ) error("Parent same as id: %s", this) ;  
        Record k = min ;
    
        // Test records in the allocated area
        for ( int i = 0 ; i < count ; i++ )
        {
            if ( records.get(i) == null ) error("Node: %d : Invalid record @%d :: %s",id, i, this) ;
            if ( k != null && keyGT(k, records.get(i)) ) 
            {
                Record r = records.get(i) ; 
                //keyGT(k, r) ;
                error("Node: %d: Not sorted (%d) (%s, %s) :: %s ", id, i, k, r, this) ;
            }
            k = records.get(i) ;
        }
        
        if ( k != null && max != null && keyGT(k,max) )
            error("Node: %d - Record is too high (max=%s):: %s", id, max, this) ;
        
        if ( SystemTDB.NullOut )
        {
            // Test records in the free area
            for ( int i = count ; i < maxRecords() ; i++ )
            {       
                if ( ! records.isClear(i) )
                    error("Node: %d - not clear (idx=%d) :: %s", id, i, this) ;
            }
        }
        
        // Pointer checks.
        int i = 0 ;
        // Check not empty at bottom. 
        for ( ; i < count+1 ; i++ )
        {
            if ( ptrs.get(i) < 0 ) 
                error("Node: %d: Invalid child pointer @%d :: %s", id, i , this) ;

            // This does BlockIO so distrubs tracking. 
            if ( CheckingTree && isLeaf )
            {
                int ptr = ptrs.get(i) ;
                BPTreeRecords records = bpTree.getRecordsMgr().getRead(ptr) ;
                int id = records.getId() ;
                if ( id != ptrs.get(i) )
                    error("Records: Block @%d has a different id: %d :: %s", id, i, this) ;
                int link = records.getLink() ;
                // Don't check if +1 does not exist.
                if ( i != count )
                {
                    BPTreeRecords page = bpTree.getRecordsMgr().getRead(ptrs.get(i)) ;
                    int id2 = page.getLink() ;
                    if ( link != id2 )
                        error("Records: Link not to next block @%d/@%d has a different id: %d :: %s", id, id2, i, records) ;
                    bpTree.getRecordsMgr().release(page) ;
                }
                records.release() ;
            }
            
        }
            
        // Check empty is empty
        if ( SystemTDB.NullOut )
        {
            int x = params.MaxPtr ;
            for ( ; i < x ; i ++ )
            {
                if ( ! ptrs.isClear(i) )
                    error("Node: %d: Unexpected pointer @%d :: %s", id, i, this) ;
            }
        }
    }

    private void checkNodeDeep(Record min, Record max)
    {
        checkNode(min, max) ;
    
        // Check pointers.
        int limit = (count == 0) ? 0 : count+1 ; 
        
        for ( int i = 0 ; i < limit ; i++ )
        {
            Record min1 = min ;
            Record max1 = max ;
            BPTreePage n = get(i, READ) ;
            
            if ( i != count )
            {
                Record keySubTree = n.getHighRecord() ;     // high key in immediate child 
                Record keyHere = records.get(i) ;           // key in this
                
                if ( keySubTree == null )
                    error("Node: %d: Can't get high record from %d", id, n.getId()) ;
                
                if ( keySubTree.getKey() == null )
                    error("Node: %d: Can't get high record is missing it's key from %d", id, n.getId()) ;
                    
                if ( keyHere == null )
                    error("Node: %d: record is null", id) ;
                
                if ( keyHere.getKey() == null )
                    error("Node: %d: Record key is null", id) ;
                
                if ( keyGT(keySubTree, keyHere) )
                    error("Node: %d: Child key %s is greater than this key %s", id, keySubTree, keyHere) ;
                
                Record keyMax = n.maxRecord() ;             // max key in subTree
                Record keyMin = n.minRecord() ;
                
                if ( keyNE(keyHere, keyMax) )
                    error("Node: %d: Key %s is not the max [%s] of the sub-tree idx=%d", id, keyHere, keyMax, i) ;
                
                if ( min != null && keyGT(min, keyMin) )
                    error("Node: %d: Minimun for this node should be %s but it's %s", id, min, keyMin) ;
                if ( max != null && keyLT(max, keyMax) )
                    error("Node: %d: Maximum for this node should be %s but it's %s", id, max, keyMax) ;
                if ( min != null && keyGT(min, keyHere) )
                    error("Node: %d: Key too small: %s - min should be %s", id, keyHere, min) ;
                // keyHere == keyMax ??
                if ( max != null && keyLT(max, keyHere) )
                    error("Node: %d: Key too large: %s - max should be %s", id, keyHere, max) ;
            }

            // Look deeper.
            if ( ! ( n instanceof BPTreeNode ) )
            {
                // Records.
                n.checkNodeDeep() ;
                n.release() ;
                continue ;
            }
            
            // Valid pointer?
            if ( isLeaf )
            {
                if ( ! bpTree.getRecordsMgr().getBlockMgr().valid(ptrs.get(i)) )
                    error("Node: %d: Dangling ptr (records) in block @%d :: %s", id, i, this) ;
            }
            else
            {
                if ( ! bpTree.getNodeManager().valid(ptrs.get(i)) )
                    error("Node: %d: Dangling ptr in block @%d :: %s", id, i, this) ;
            }

            // Calc new min/max.
            if ( i == 0 ) 
                max1 = records.get(0) ;
            else if ( i == count )
            {
                min1 = records.get(count-1) ;
                max1 = null ;
            }
            else
            { 
                min1 = records.get(i-1) ;
                max1 = records.get(i) ;
            }
//            if ( n.parent != id )
//                error("Node: %d [%d]: Parent/child mismatch :: %s", id, n.parent, this) ;
            
            ((BPTreeNode)n).checkNodeDeep(min1, max1) ;
            n.release() ;
        }
    }

    private static boolean logging()
    {
        return BPlusTreeParams.logging(log) ;
    }
    
    private void warning(String msg, Object... args)
    {
        msg = format(msg, args) ;
        System.out.println("Warning: "+msg) ;
        System.out.flush();
    }
    
    private void error(String msg, Object... args)
    {
        msg = format(msg, args) ;
        System.out.println() ;
        System.out.println(msg) ;
        System.out.flush();
        try { dumpBlocks() ; } catch (Exception ex) {}
        throw new BPTreeException(msg) ;
    }
    
    private void dumpBlocks()
    {
        System.out.println("---Nodes") ;
        bpTree.getNodeManager().dump() ;
        System.out.println("---Records") ;
        bpTree.getRecordsMgr().dump() ;
        System.out.println("---") ;
        System.out.flush();
    }
}
