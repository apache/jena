/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.btree;

import static com.hp.hpl.jena.tdb.base.record.Record.compareByKey ;
import static com.hp.hpl.jena.tdb.base.record.Record.keyGE ;
import static com.hp.hpl.jena.tdb.index.btree.BTreeParams.CheckingNode ;
import static com.hp.hpl.jena.tdb.index.btree.BTreeParams.DumpTree ;
import static com.hp.hpl.jena.tdb.index.btree.BTreeParams.Logging ;
import static java.lang.String.format ;

import java.io.ByteArrayOutputStream ;
import java.io.PrintStream ;
import java.util.Iterator ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.lib.NotImplemented ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.buffer.PtrBuffer ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;
import com.hp.hpl.jena.tdb.base.page.PageBase ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class BTreeNode extends PageBase 
{
    private static Logger log = LoggerFactory.getLogger(BTreeNode.class) ;
 
    /*
     * Packed leaves:
     *        SplitPoint moves as well.
     *      Affected:
     *        Insert:  split, splitRoot, 
     *        Delete:  merge, shiftLeft, shiftRight, shuffleUp shuffleUpAll, shuffleDown   
     *   
     */
    
    // Package visible
    BTree bTree ;
    BTreeParams bTreeParams ;
    BTreePageMgr pageMgr ;
    final int id ;
    
    boolean isLeaf ; 
    int count ;             // # records in use : ==records.size() : ptrs.size() is 1 more
    int parent ;            // Need to consider splitRoot - let the root id change?
    
    RecordBuffer records ;
    PtrBuffer ptrs ;
    
    /*
     * N = 2, Gap = 1 =>
     *  2*N+Gap:  MaxRec = 4, MaxPtr = 5,
     *  Max-1:    HighRec = 3, HighPtr = 4
     *  N-1:      MinRec = 1, MinPtr = 2
     *
     * Non-leaf:
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
     * Leaf: Packed with keys.
     *      
     *      +------------------------+
     *      | | K0 | K1 | ** | ** |--|
     *      +------------------------+

     * Pictures:      
     *      /--\ \--\
     * means a block with free space introduced between records[i] and records[i+1], ptrs[i+1]/ptrs[i+2]
     * Lower half is a valid structure (except for overall size) 
     *       
     *      /--/ /--\
     * means a block with free space introduced between records[i] and records[i+1], ptrs[i]/ptrs[i+1]
     * Upper half is a valid structure (except for overall size) 
     */

    private BTreeNode create(int parent, boolean asLeaf)
    {
        BTreeNode n = pageMgr.create(parent, asLeaf) ;
        return n ;
    }
    
    BTreeNode(BTree bTree, int id, Block block)
    {
        super(block) ;
        if ( bTree == null )
            System.err.println("NULL btree") ;
        this.id = getId() ;
        this.bTree = bTree ;
        this.bTreeParams = bTree.getParams() ;
        this.pageMgr = bTree.getPageMgr() ;
        this.count = -1 ;
    }
    
    private BTreeNode getNode(int idx)
    {
        int ptr = ptrs.get(idx) ;
        return pageMgr.get(ptr, id) ;
    }
    
//    /** Return the nodes id */ 
//    public ByteBuffer getByteBuffer()
//    {
////        if ( CheckingNode )
////            checkNodeDeep() ;
//        return byteBuffer ;
//    }
//    
    // ---------- Public calls.
    // None of these are called recursively.

    /** Find a record, using the active comparator */
    public Record search(Record rec)
    {
        if ( CheckingNode )
            internalCheckNodeDeep() ;
        if ( getId() != 0 )
            throw new BTreeException("Search not starting from the root: "+this) ;
        return _search(rec) ;
    }

    final int getCount() { return count ; }
    
    static final boolean DUP_CHECK = false;
    /** Insert a record - return existing value if any, else null */
    public Record insert(Record record)
    {
        if ( logging() )
        {
            log.debug(format("** insert(%s) / start", record)) ;
            if ( DumpTree ) dump() ;
        }
     
        Record x = null ;
        if ( DUP_CHECK )
            x = search(record) ;
        
        if ( ! isRoot() )
            throw new BTreeException("Insert begins but this is not the root") ;
        
        if ( isFull() )
            // Root full - root split is a special case.
            splitRoot() ;
        
        // Root ready - call insert proper.
        Record result = insertNonFull(record) ;
        
        if ( DUP_CHECK )
        {
            if ( x != null && result == null )
                System.err.println("BUG: result is null but contains was true") ;
            if ( x == null && result != null )
                System.err.println("BUG: result is not null but contains was false") ;
        }
        
        internalCheckNodeDeep() ;
    
        if ( logging() )
        {
            log.debug(format("** insert(%s) / finish", record)) ;
            if ( DumpTree ) dump() ;
        }
        return result ;
    }

    /** Delete a rcord - return the old value if there was one, else null*/
    public Record delete(Record rec)
    { 
        if ( logging() )
        {
            log.debug(format("** delete(%s) / start", rec)) ;
            if ( DumpTree ) dump() ;
        }
        if ( ! isRoot() )
            throw new BTreeException("Delete begins but this is not the root") ;
    
        // Entry: checkNodeDeep() ;
        Record v = _delete(rec) ;
        // Fix root in case it became empty.
        if ( !isLeaf && count == 0 )
            reduceRoot() ;
        internalCheckNodeDeep() ;
        
        if ( logging() )
        {
            log.debug(format("** delete(%s) / finish", rec)) ;
            if ( DumpTree ) dump() ;
        }
        return v ;
    }

    public Record maxRecord()
    {
        if ( logging() )
            log.debug("maxRecord") ;
        BTreeNode n = maxNode() ;
        if ( n == null )
            // Empty tree
            return null ;
        return n.records.get(n.count-1) ;
    }

    public Record minRecord()
    {
        if ( logging() )
            log.debug("minRecord") ;
        BTreeNode n = minNode() ;
        if ( n == null )
            // Empty tree
            return null ;
        return n.records.get(0) ;
    }

    /** size : this is an explicit test which walks the tree.  The BTree object keeps a cache count  */
    
    public long sizeByCounting()
    {
        long x = count ;
        if ( ! isLeaf )
        {
            for ( int i = 0 ; i <= count ; i++ )
            {
                BTreeNode n = getNode(i) ;
                //n.internalCheckNode() ;
                x = x + n.sizeByCounting() ;
            }
        }
        return x ;
    }

    // ============ SEARCH
    /* 
     * Do a (binary) search of the node to find the record.
     *   Returns: 
     *     +ve or 0 => the index of the record 
     *     -ve => The insertion point : the immediate higher record or length as (-i-1)
     *  If +ve return record
     *  If -ve and leaf ==> not found
     *  Get child point
     *    Pointers are staggered so this is (-(i+1))   
     *  Search in child node.
     */
    
    private Record _search(Record rec)
    {
        int x = findSlot(rec) ;          // Find index, or insertion point (immediate higher slot) as (-i-1)
        if ( x >= 0 )
            // Found
            return records.get(x) ;
        // Not found. 
        if ( isLeaf )
            return null ;
        // Index of pointer block (which is staggered from the records list) 
        x = -(x+1) ;
        // Recurse
        BTreeNode n = getNode(x) ;
        // I'm a tail recursion - get me out of here.
        return n._search(rec) ;
    }
    
    private BTreeNode minNode()
    {
        BTreeNode n = this ;
        while( ! n.isLeaf )
            n = n.getNode(0) ;
        // Empty tree
        if ( n.count == 0 )
            return null ;
        return n ;
    }

    private BTreeNode maxNode()
    {
        BTreeNode n = this ;
        while( ! n.isLeaf )
            n = n.getNode(n.count) ;
        // Empty in empty tree
        if ( n.count == 0 )
            return null ;
        return n ;
    }

    // ============ INSERT
    
    /* Insert into a node that is not full.
     *   Full nodes are split while processing the parent.
     * Find lost.
     *   If found, update if necessary and return 
     * If not found:
     * If leaf
     *   insert
     *   WRITE(this)
     *   return
     * Get child pointer.
     * Ensure not full, split if necessary
     *   Split writes the parent and two sub-blocks. 
     *   When split need to check the insertion point has not moved.
     * InsertNonFull on child. 
     */
    
    private Record insertNonFull(Record record)
    {
        if ( CheckingNode ) internalCheckNode() ;
        int i = findSlot(record) ;
        if ( i >= 0 )
            return updateExisting(i, record) ;
        
        // Did not find.
        i = -(i+1) ;
        if ( isLeaf )
        {
            // Shuffle in a space - assumes top is empty (node not full)
            if ( CheckingNode && isFull() ) error("Node is full" ) ;
            records.add(i, record) ;
            count++ ;
            pageMgr.put(this) ;
            return null ;
        }
    
        // Not leaf.
        BTreeNode n = getNode(i) ;
        if ( n.isFull() )
        {
            split(i, n) ;
            // Examine the record we pulled up in the split.
            int cmp = compareByKey(record, records.get(i)) ; 

            // Did we pull up the record we're trying to insert?
            if ( cmp == 0 )
                return updateExisting(i, record) ;
            
            // Did we move the insertion point?
            // gt(record, records.get(i))
            if ( cmp > 0 ) 
            {
                i = i+1 ;
                n = getNode(i) ;
            }
            if ( CheckingNode )
                n.internalCheckNodeDeep() ;
        }
        return n.insertNonFull(record) ;
    }

    private Record updateExisting(int i, Record record)
    {
        Record r = records.get(i) ;
        if ( record.hasSeparateValue() )
        {
            // Potential placement value
            if ( ! r.equals(record) )
            {
                records.set(i,record) ;
                pageMgr.put(this) ;
            }
        }
        // Return old value
        return r ;
    }

    /* Split a non-root node y, held at slot idx.
     * Record median slot
     * Allocate a new node , z(will be come the high end)
     * Copy high end of y to z
     * Insert median and z pointer into parent (this)
     * WRITE(y)
     * WRITE(z)
     * WRITE(this)
     */
    private void split(int idx, BTreeNode y)
    {
        if ( logging() )
            log.debug(format("Split(Parent %d[%d], Node %d)", this.id, idx, y.id)) ;
            
        internalCheckNode() ;
        if ( CheckingNode )
        {
            if( y.id == 0 ) error("Splitting root in non-root split routine") ;
            if ( y.count != maxRecords()) error("Node is not full (by count)") ;
            if ( this.ptrs.get(idx) != y.id ) error("Node to be split isn't in right place") ;
        }        
        // Median record : will go in parent.
        int ix = bTreeParams.SplitIndex ;
        Record split = y.records.get(ix) ; 

        if ( logging())
        {
            log.debug(format("** Split: Parent: %d[%d]",id, idx)) ;
            log.debug(format("** Split: %d=(%s) in node %d", ix, split, y.id)) ;
            log.debug("split >>   "+this) ;
            log.debug("split >>   "+y) ;
        }
        
        // New block.
        BTreeNode z = create(id, y.isLeaf) ;
        
        // Unlike the book, we leave the low end untouched and copy, and clear the high end.
        // z becomes the new upper node, not the lower node.  this is the lower block.
        // y is the full block.
        
        int maxRec = maxRecords() ;
        // Copy from top of y into z. 
        y.records.copy(ix+1, z.records, 0, maxRec-(ix+1)) ;
        y.records.clear(ix, maxRec-ix) ;                    // Clear copied and median slot 
        y.records.setSize(ix) ;                                // Reset size
        
        if ( ! y.isLeaf )
        {
            y.ptrs.copy(ix+1, z.ptrs, 0, bTreeParams.MaxPtr-(ix+1)) ;
            y.ptrs.clear(ix+1, bTreeParams.MaxPtr-(ix+1)) ;
            y.ptrs.setSize(ix+1) ;
        }

        // Set sizes of subnodes
        y.count = ix ;                              // Median is ix
        y.internalCheckNode() ;                     // y finished
        
        z.count = maxRec - (ix+1) ;                 // Number copied into z
        z.internalCheckNode() ;                     // z finished
        
        // Insert new node in parent 
        shuffleUp(idx) ;                            // Leaves ptr[i] (which is y.id) alone. i.e /==\ \==\
        records.set(idx, split) ;                   // Insert new node.
        ptrs.set(idx+1, z.id) ;

        if ( DumpTree  && logging() )
        {
            log.debug("split <<   "+this) ;
            log.debug("split <<   "+y) ;
            log.debug("split <<   "+z) ;
        }
        
        pageMgr.put(y) ;
        pageMgr.put(z) ;
        pageMgr.put(this) ;
    }
    
    /* Split the root and leave the root block as the root.
     * This is the only point the height of the tree increases.
     *
     *  Allocate new blocks.
     *  Copy root low into left
     *  Copy root high into right
     *  Set counts.
     *  Create new root settings (two poniters, one record) 
     *  WRITE(left)
     *  WRITE(right)
     *  WRITE(root)
     */
    private void splitRoot()
    {
        if ( CheckingNode )
        {
            if ( id != 0 ) error("Not root: %d (root is id zero)", id) ;
            //if ( parent != BTreeParams.RootParent ) error("Not root parent: %d (root is id zero)", id) ;
        }
        internalCheckNode() ;
        
        // Median record
        Record rec = records.get(bTreeParams.SplitIndex) ;

        if ( DumpTree && logging() )
        {
            log.debug(format("** Split root %d (%s)", bTreeParams.SplitIndex, rec)) ;
            log.debug("splitRoot >>   "+this) ;
        }

        // New blocks.
        BTreeNode left = create(id, isLeaf) ;
        BTreeNode right = create(id, isLeaf) ;
        
        // New left
        records.copy(0, left.records, 0, bTreeParams.SplitIndex) ;
        if ( ! isLeaf )
            ptrs.copy(0, left.ptrs, 0, bTreeParams.SplitIndex+1) ;
        left.count = bTreeParams.SplitIndex ;

        // New right
        records.copy(bTreeParams.SplitIndex+1, right.records, 0, maxRecords()-(bTreeParams.SplitIndex+1)) ;
        if ( ! isLeaf )
            ptrs.copy(bTreeParams.SplitIndex+1, right.ptrs, 0, bTreeParams.MaxPtr-(bTreeParams.SplitIndex+1)) ;
        right.count = maxRecords()-(bTreeParams.SplitIndex+1) ;
        
        // So left.count+right.count = bTree.NumRec-1
        
        // Clear root by reformatting.  New root not a leaf
        BTreePageMgr.formatForRoot(this, false) ;
        // Make a non-leaf.
        
        // Insert two subnodes, divided by the median record
        count = 1 ;
        records.add(0, rec) ;
        
        ptrs.add(left.id) ;        // slot 0
        ptrs.add(right.id) ;       // slot 1
        
        if ( logging())
        {
            log.debug("splitRoot <<   "+this) ;
            log.debug("splitRoot <<   "+left) ;
            log.debug("splitRoot <<   "+right) ;
        }

        if ( CheckingNode )
        {
            this.internalCheckNode() ;
            left.internalCheckNode() ;
            right.internalCheckNode() ;
        }

        pageMgr.put(left) ;
        pageMgr.put(right) ;
        pageMgr.put(this) ;
    }

    // ============ DELETE
    /* Delete
     * Find record, or insertion point encoded as -i-1
     * If found:
     *   do leaf or internal node
     * If not found:
     *   If left - nothing to do - return
     * Get child node
     * Ensure more than min size so delete is always possible without needing
     *   to move back up the tree.
     *    
     */
    
    private Record _delete(Record rec)
    {
        internalCheckNode() ;
        if ( logging() )
            log.debug(format("_delete(%s) : %s", rec, this)) ;
        
        int x = findSlot(rec) ;
        
        if ( x >= 0 )
        {
            // Record found.
            Record r = records.get(x) ; 
            
            if ( isLeaf )
                deleteFromLeaf(x) ;
            else
                deleteFromInternal(rec, x) ;
            return r ;
        }

        // Not found.
        //if ( x < 0 )

        if ( logging() )
            log.debug("Record not found") ;

        if ( isLeaf )
        {
            if ( logging() )
                log.debug("No such record") ;
            return Record.NO_REC ;
        }
        
        // Not found, internal node.
        // Identify subtree and recurse but fixup sizes first.
        
        x = -(x+1) ;    // First record slot above is highest child below.
        BTreeNode n = getNode(x) ;
        if ( n.count == bTreeParams.MinRec )
        {
            n = rebalance(n, x) ;   // Flushes nodes
            internalCheckNode() ;
            n.internalCheckNode() ;
        }

        return n._delete(rec) ;
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
        
        BTreeNode n = getNode(0) ;
        
        // Got child.  Clear root by reformatting.  New root a leaf IFF n is.
        BTreePageMgr.formatForRoot(this, n.isLeaf) ;
        
        // Pull up child, avoiding copying whole of ByteBuffer.
        n.records.copy(0, this.records, 0, n.count) ;
        
        if ( ! isLeaf )
            n.ptrs.copy(0, ptrs, 0, n.count+1) ;

        count = n.count ;
        
        pageMgr.put(this) ;
        pageMgr.release(n.getBackingBlock()) ;
        
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
     *   WRITE(rght)
     *   WRITE(n)
     *   WRITE(this)
     * else 
     *  merge with left or right sibling
     * Suboperations do all the write-backof nodes.
     */ 
    private BTreeNode rebalance(BTreeNode n, int idx)
    {
        if ( logging() )
        {
            log.debug(format("rebalance(id=%d, %d)", n.id, idx)) ;
            log.debug(format(">> %s", n)) ;
        }
        internalCheckNode() ;
        
        if ( CheckingNode && n.count != bTreeParams.MinRec ) error("Node not minimal size in rebalance") ;
        
        BTreeNode left = null ;
        if ( idx > 0 )
            left = getNode(idx-1) ;
        
        if ( left != null && left.count > bTreeParams.MinRec )
        {
            if ( logging() )
                log.debug("rebalance/shiftRight") ;
            
            shiftRight(left, n, idx-1) ;
            pageMgr.put(left) ;
            pageMgr.put(n) ;
            pageMgr.put(this) ;
            
            if ( logging() )
                log.debug("rebalance<<") ;
            if ( CheckingNode )
            {
                left.internalCheckNode();
                n.internalCheckNode();
                this.internalCheckNode() ;
            }
            return n ;
        }

        BTreeNode right = null ;
        if ( idx < count )
            right = getNode(idx+1) ;
        
        if ( right != null && right.count > bTreeParams.MinRec )
        {
            if ( logging() )
                log.debug("rebalance/shiftLeft") ;

            shiftLeft(n, right, idx) ;
            pageMgr.put(right) ;
            pageMgr.put(n) ;
            pageMgr.put(this) ;

            if ( logging() )
                log.debug("rebalance<<") ;
            if ( CheckingNode )
            {
                right.internalCheckNode();
                n.internalCheckNode();
                this.internalCheckNode() ;
            }
            if ( logging() )
                log.debug("rebalance<<") ;
            return n ;
        }

        if ( CheckingNode && left == null && right == null) error("No siblings") ;

        BTreeNode newNode = null ;
        if ( left != null )
        {
            if ( logging() )
                log.debug(format("rebalance/merge/left: left=%d n=%d [%d]", left.id, n.id, idx-1)) ;
            if ( CheckingNode && left.id == n.id ) error("Left and n the same" ) ;
            return merge(left, n, idx-1) ;
        }
        else
        {
            // right != null
            if ( logging() )
                log.debug(format("rebalance/merge/right: n=%d right=%d [%d]", n.id, right.id, idx)) ;
            if ( CheckingNode && right.id == n.id ) error("N and right the same" ) ;
            return merge(n, right, idx) ;
        }
    }

    /* Delete from leaf - left is not minimal size
     * Shuffle down
     * WRITE(this)
     */
    private void deleteFromLeaf(int x)
    {
        if ( logging() )
        {
            log.debug(format("deleteFromLeaf(%s, %d)", records.get(x), x)) ;
            log.debug(format("deleteFromLeaf >> %s", this)) ;
        }
        internalCheckNode() ;
        
        shuffleDown(x) ;
        pageMgr.put(this) ;

        if ( logging() )
            log.debug(format("deleteFromLeaf << %s", this)) ;
    }
        
    /* Deletes always happen at a leaf in a B-Tree
     * This is done by one of:
     * 1/ swapping the highest record from the left subtree
     *   (records less than the record to delete)
     * 2/ swapping the lowest record from the right subtree
     *   (records greater than the record to delete)
     * 3/ merging two subnodes, which will pull the record into 
     *    the merged node as the mid point, and deleting from that
     */

    private void deleteFromInternal(Record rec, int x)
    {
        if ( logging() )
        {
            log.debug(format("deleteFromInternal(%s, %d)", rec, x)) ;
            log.debug(format("deleteFromInternal >> %s", this)) ;
        }
        internalCheckNode() ;

        // try left
        BTreeNode left = getNode(x) ;
        if ( left.count > bTreeParams.MinRec )
        {
            if ( logging() )
                log.debug("deleteFromInternal/left") ;
            //left.deleteHigh(this, x) ;
            deleteHigh(left, x) ;
            return ;
        }
        
        // try right
        BTreeNode right = getNode(x+1) ;
        if ( right.count > bTreeParams.MinRec )
        {
            if ( logging() )
                log.debug("deleteFromInternal/right") ;
            deleteLow(right, x) ;
            return ;
        }
        
        if ( logging() )
            log.debug(format("deleteFromInternal/merge(%d, %d, [%d])", left.id, right.id, x)) ;
        
        // Both left and right subnodes are minimal sized.
        // Merge to make a larger node, including moving down the record,
        // and delete from the new, merged node. 
        
        BTreeNode sub = merge(left, right, x) ;
        // sub, left and righ thave been written, which is excessive.
        // But simpler - use the dirty block marking BlockMgr to improve.

        // The record will have been moved into the subnode
        // so it is there and sub will be written.
        // This may now be an empty root (no records, one pointer).
        // but delete() will handle that.
        
        sub._delete(rec) ;
        if ( logging() )
            log.debug(format("deleteFromInternal<< %s", this)) ;
    }
    
    /* Swap highest  
     *   Find the highest record in the subtree
     *   Swap with the record at origIdx
     *   Delete from the leaf (writes leaf). 
     *   WRITE(orig)
     */ 
    
    private void deleteHigh(BTreeNode subTree, int origIdx)
    {
        if ( logging() )
            log.debug(format("deleteHigh(,%d) >> %s", origIdx, this)) ;

        BTreeNode n = subTreeLeft(subTree) ;
        // n is now a leaf.
        Record rec = n.records.get(n.count-1) ;
        n.deleteFromLeaf(n.count-1) ;       // Writes back leaf block

        records.set(origIdx, rec) ;
        pageMgr.put(this) ;

        if ( logging() )
            log.debug(format("deleteHigh(,%d) << %s", origIdx, this)) ;

    }
    
    // Helper - find leftmost leaf, rebalancing for delete as we go.
    private static BTreeNode subTreeLeft(BTreeNode subTree)
    {
        BTreeNode n = subTree ;
        while ( ! n.isLeaf )
        {
            BTreeNode n2 = n.getNode(n.count) ;
            if ( n2.count == n2.bTreeParams.MinRec )
            {
                n2 = n.rebalance(n2, n.count) ;
                n.internalCheckNode() ;
                n2.internalCheckNode() ;
            }
            n = n2 ;
        }
        return n ;
    }
   
    /* Swap with lowest
     *   Find the lowest record in the subtree
     *   Swap with the record at origIdx
     *   Delete from the leaf (writes leaf) 
     *   WRITE(orig)
     */
    
  private void deleteLow(BTreeNode subTree, int origIdx)
  {
      if ( logging() )
          log.debug(format("deleteLow(,%d) >> %s", origIdx, this)) ;
      BTreeNode n = subTreeRight(subTree) ;
      Record rec = n.records.get(0) ;
      n.deleteFromLeaf(0) ;             // Writes leaf block

      records.set(origIdx, rec) ;
      pageMgr.put(this) ;
      if ( logging() )
          log.debug(format("deleteLow(,%d) << %s", origIdx, this)) ;
}
    
    // Helper - find rightmost leaf, rebalancing for delete as we go.
    private static BTreeNode subTreeRight(BTreeNode subTree)
    {
        BTreeNode n = subTree ;
        while ( ! n.isLeaf )
        {
            BTreeNode n2 = n.getNode(0) ;
            if ( n2.count == n2.bTreeParams.MinRec )
            {
                n2 = n.rebalance(n2, 0) ;
                n.internalCheckNode() ;
                n2.internalCheckNode() ;
            }
            n = n2 ;
        }
        return n ;
    }

    /* Merge two nodes
     *   Allocate a new node.
     *   Copy in left
     *   Insert median point
     *   Copy in right
     *   
     *   Remove from parent
     *   RELEASE(left)
     *   RELEASE(right)
     *   WRITE(new subnode)
     *   WRITE(this)
     * Return new node
     */
    private BTreeNode merge(BTreeNode left, BTreeNode right, int dividingSlot)
    {
        if ( logging() )
        {
            log.debug(format("merge(,,%d) ", dividingSlot)) ;
            if ( DumpTree )
            {
                log.debug(format("merge >> %s", this)) ;
                log.debug(format("merge >> %s", left)) ;
                log.debug(format("merge >> %s", right)) ;
            }
        }
        
        if ( CheckingNode )
        {
            if ( left.count != bTreeParams.MinRec ) error("Left node is not min sized") ;
            if ( right.count != bTreeParams.MinRec ) error("Right node is not min sized") ;
            if ( ptrs.get(dividingSlot) != left.id ) error("Left node not as expected") ;
            if ( ptrs.get(dividingSlot+1) != right.id) error("Right node not as expected") ;
            if ( left.isLeaf != right.isLeaf ) error("Left and right nodes not of the same leaf-ness") ; 
        }
        
        internalCheckNode() ;
        BTreeNode n = create(id, left.isLeaf) ;      // Parent is "this"
        
        // Copy in left
        left.records.copy(0, n.records, 0 , left.count) ;
        if ( ! n.isLeaf )
            left.ptrs.copy(0, n.ptrs, 0 , left.count+1) ;

        // Median record
        int idx = left.count ;
        Record medianRec = records.get(dividingSlot) ;
        
        // One above what was just copied.
        n.records.add(medianRec) ;
        
        // Copy in right
        right.records.copy(0, n.records, idx+1, right.count) ;
        if ( ! right.isLeaf )
            right.ptrs.copy(0, n.ptrs, idx+1, right.count+1) ;
        
        int size = left.count+right.count+1 ;
        n.count = size ;
        
        // Depending on whether there is a gap or not.
        if ( CheckingNode && size != maxRecords() )
            error("Inconsistent node size: %d", size) ; 

        // Remove from parent (which is "this")
        shuffleDown(dividingSlot) ;            // Assumes /==/ /==\
        ptrs.set(dividingSlot, n.id) ;

        // Release old nodes
        pageMgr.free(left.getBackingBlock()) ;
        pageMgr.free(right.getBackingBlock()) ;
        
        if ( CheckingNode && findSlot(medianRec) >= 0 )
            error("Can still find record in parent of merge blocks") ;
        pageMgr.put(n) ;
        pageMgr.put(this) ;
        
        if ( logging() )
        {
            log.debug(format("merge << %s", this)) ;
            log.debug(format("merge << %s", n)) ;
        }
        internalCheckNode() ;
        n.internalCheckNode();
        return n ;
    }

    // ---- No writing blocks back from here on.
    
    /* Move a record from left to right via the parent
     * Does not write blocks.
     */
    
    private void shiftRight(BTreeNode left, BTreeNode right, int idx)
    {
        if ( logging() )
        {
            log.debug(format("shiftRight(%d, %d)", left.id, right.id)) ;
            log.debug(format("shiftRight >> %s", this)) ;
            log.debug(format("shiftRight >> %s", left)) ;
            log.debug(format("shiftRight >> %s", right)) ;
        }
        if ( CheckingNode )
        {
            if ( left.count <= bTreeParams.MinRec ) error("Left too small to rotate a record out") ;
            if ( right.count >= maxRecords() ) error("Right too large to rotate a record into") ;
            if ( ptrs.get(idx) != left.id ) error("Index is not of the left in shiftRight") ; 
            if ( ptrs.get(idx+1) != right.id) error("Index is not of the right in shiftRight") ;
        }

        // Shift record and child into the right node via the the parent (this)
        // idx is index of left in the parent.

        Record rec1 = records.get(idx) ;
        right.records.add(0, rec1) ;
        if ( ! right.isLeaf )
        {
            int ptr1 = left.ptrs.get(left.count) ;      // Greatest left child
            right.ptrs.add(0, ptr1) ;
        }
        right.count++ ;

        Record rec2 = left.records.get(left.count-1) ;
        left.records.removeTop() ;
        if ( ! left.isLeaf )
            left.ptrs.removeTop() ;
        left.count -- ;
        
        // Put separating record in
        records.set(idx, rec2) ;
        
        if ( DumpTree && logging() )
        {
            log.debug(format("shiftRight << %s", this)) ;
            log.debug(format("shiftRight << %s", left)) ;
            log.debug(format("shiftRight << %s", right)) ;
        }
    }

    /* Move a record from right to left via the parent
     * Does not write blocks.
     */
    private void shiftLeft(BTreeNode left, BTreeNode right, int idx)
    {
        if ( logging() )
        {
            log.debug(format("shiftLeft(%d, %d, [%d])", left.id, right.id, idx)) ;
            log.debug(format("shiftLeft >> %s", this)) ;
            log.debug(format("shiftLeft >> %s", left)) ;
            log.debug(format("shiftLeft >> %s", right)) ;
        }
    
        if ( CheckingNode )
        {
            if ( right.count <= bTreeParams.MinRec )  error("Right too small to rotate a record out") ;
            if ( left.count >= maxRecords() )   error("Left too large to rotate a record into") ;
            if ( ptrs.get(idx) != left.id )     error("Index is not of the left in shiftLeft") ;
            if ( ptrs.get(idx+1) != right.id )  error("Index is not of the right in shiftLeft") ;
        }
        
        // Insert in left
        Record rec1 = records.get(idx) ;
        left.records.add(left.count, rec1) ; 
        if ( ! right.isLeaf )
        {
            int ptr1 = right.ptrs.get(0) ;          // Least right child
            left.ptrs.add(left.count+1, ptr1) ;
        }
        left.count++ ;

        // Remove from right
        Record rec2 = right.records.get(0) ;
        right.shuffleDown(0) ;                  //  alters count
        
        // Set new separating record in parent.
        records.set(idx, rec2) ;

        if ( logging() )
        {
            log.debug(format("shiftLeft << %s", this)) ;
            log.debug(format("shiftLeft << %s", left)) ;
            log.debug(format("shiftLeft << %s", right)) ;
        }
        left.internalCheckNode();
        right.internalCheckNode();
        internalCheckNode() ;
    }

    // Increases count always. /==\ \==\
    // ** Moves children from i+1
    // Does not write nodes
    
    private void shuffleUp(int i)
    {
        if (logging() )
        {
            log.debug(format("shuffleUp: i=%d count=%d MaxRec=%d", i, count, maxRecords())) ;
            log.debug("shuffleUp >> "+this) ;
        }
        internalCheckNode() ;

        if ( i == count )
        {
            if (logging() )
                log.debug("shuffleUp << No op") ;
            // Increment - the top slot is now free to use.
            // Need to increment records.
            records.incSize() ;
            if ( ! isLeaf )
                ptrs.incSize() ;
            count++ ;
            return ;
        }
        
        if ( CheckingNode && i > count ) error("shuffleUp: out of bounds: %d :: %s", i, this) ;
        
        count++ ;
        records.shiftUp(i) ;    // Does not work if i == top+1
        
        if ( ! isLeaf )
        {
            ptrs.shiftUp(i+1) ;
            // Which is .add(i+1, NO_PTR)
            //ptrs.clear(i+1) ;
        }
        if ( logging() )
            log.debug("shuffleUp << "+this) ;
        // not valid - slot has not been reset yet.
        //checkNode() ;
    }

    // Move all up.
    // Does not write node

    private void shuffleAllUp()
    {
        if ( logging() )
            log.debug("shuffleAllUp >> "+this) ;
        count++ ;
        records.shiftUp(0) ;
        if ( ! isLeaf )
            ptrs.shiftUp(0) ;
        if ( logging() )
            log.debug("shuffleAllUp << "+this) ;
        // Not valid - slot not set yet
        //checkNode() ;
    }
    
    // Move down, does not write back.
    // Moves on top of child i  /==/ /==\ ==> /====\ 
    // Does not write node
    
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
            if ( ! isLeaf )
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
        
        // Shuffle down.
        records.shiftDown(x) ;
        if ( ! isLeaf )
            ptrs.shiftDown(x) ;  
        count -- ;
        if ( logging() )
            log.debug("shuffleDown << "+this) ;
        internalCheckNode() ;
    }

    final int findSlot(Record rec)
    {
        return records.find(rec) ;
    }

    private final boolean isRoot()
    {
        return bTree.root == this ;
    }

    private final int maxRecords() { return (isLeaf ? bTreeParams.MaxRecLeaf : bTreeParams.MaxRecNonLeaf ) ; }
    
    private final boolean isFull()
    {
        if ( CheckingNode && count > maxRecords()  )
            error("isFull: Moby block: %s", this) ;
        
        return count == maxRecords() ;
    }

    /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */ 
    public Iterator<Record> iterator(Record fromRec, Record toRec)
    { return BTreeRangeIterator.iterator(this, fromRec, toRec) ; }
    
    public Iterator<Record> iterator()
    { return BTreeRangeIterator.iterator(this) ; }
    
    // ========== Other
    
    private static final boolean logging()
    {
        return Logging && log.isDebugEnabled() ;
    }
    
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder() ;
        if ( isLeaf )
            b.append("LEAF: ") ;
        else
            b.append("NODE: ") ;
        
        
        String parentStr = "??" ;
        if ( parent >= 0 )
            parentStr = Integer.toString(parent) ;
        else if ( parent == BTreeParams.RootParent )
            parentStr = "root" ;
                
        b.append(String.format("%d [%s] (size %d) -- ", id, parentStr, count)) ;
        
        if ( isLeaf )
        {
            for ( int i = 0 ; i < maxRecords() ; i++ )
            {
                if ( i != 0 )
                    b.append(" ") ;
                b.append("(") ;
                b.append(recstr(records, i)) ;
                b.append(")") ;
            }
        }
        else   
        {
            for ( int i = 0 ; i < maxRecords() ; i++ )
            {
                b.append(childStr(i)) ;
                b.append(" (") ;
                b.append(recstr(records, i)) ;
                b.append(") ") ;
            }
            b.append(childStr(bTreeParams.HighPtr)) ;
        }
        return b.toString() ;
    }

    private final String recstr(RecordBuffer records, int idx)
    {
        if ( records.isClear(idx) )
            return "----" ;

        Record r = records._get(idx) ;
        return r.toString() ;
    }
    
    @Override
    public void output(IndentedWriter out)
    {
        throw new NotImplemented() ;
    }

    public void dump()
    {
        dump(System.out, 0) ;
    }

    public String dumpToString()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        PrintStream x = new PrintStream(out) ;
        dump(x, 0) ;
        x.flush();
        return out.toString() ;
    }
    
    
    public void dump(PrintStream out, int level)
    {
        indent(out, level) ;
        System.out.print(toString()) ;
        System.out.println() ;
        level++ ;
        if ( ! isLeaf )
            for ( int i = 0 ; i < count+1 ; i++ )
            {
                int c = ptrs.get(i) ;
                BTreeNode n = pageMgr.get(c, id) ;
                n.dump(out, level) ;
            }
    }

    private void indent(PrintStream out, int x)
    {
        for ( int i = 0 ; i < x ; i++ )
            out.print("  ") ;
    }

    private String childStr(int i)
    {
        if ( i >= ptrs.size() )
            return "*" ;
        int x = ptrs.get(i) ;
        return Integer.toString(x) ; 
    }
    
    // =========== Checking
    
    // Check node does not assume a valid tree - may be in mid-operation. 
    final void internalCheckNode()
    { 
        if ( CheckingNode )
            checkNode(null, null) ;
    }
    
    final private void checkNode(Record min, Record max)
    {
        if ( count != records.size() )
            error("Inconsistent: id=%d, count=%d, records.size()=%d : %s", id, count, records.size(), this) ; 
        
        if ( ! isLeaf && count+1 != ptrs.size() )
            error("Inconsistent: id=%d, count+1=%d, ptrs.size()=%d ; %s", id, count+1, ptrs.size(), this) ; 

        if ( bTree.root != null && !isRoot() && count < bTreeParams.MinRec)
        {
            isRoot() ;
            error("Runt node: %s", this) ;
        }
        
        if ( !isRoot() && count > maxRecords() ) error("Over full node: %s", this) ;
        if ( parent == id ) error("Parent same as id: %s", this) ; 
        Record k = min ;

        // Test records in the allocated area
        for ( int i = 0 ; i < count ; i++ )
        {
            if ( records.get(i) == null ) error("Node: %d : Invalid record @%d :: %s",id, i, this) ;
            if ( k != null && keyGE(k, records.get(i)) ) 
            {
                Record r = records.get(i) ; 
                //ge(k, r) ;
                error("Node: %d: Not sorted (%d) (%s, %s) :: %s ", id, i, k, r, this) ;
            }
            k = records.get(i) ;
        }
        
        if ( k != null && max != null && keyGE(k,max) )
            error("Node: %d - Record is too high (max=%s):: %s", id, max, this) ;
        
        if ( SystemTDB.NullOut )
            // Test records in the free area
            for ( int i = count ; i < maxRecords() ; i++ )
                if ( ! records.isClear(i) )
                    error("Node: %d - not clear (idx=%d) :: %s", id, i, this) ;
        
        // Test children
        
        if ( SystemTDB.NullOut )
        {
            int i = 0 ;
            if ( ! isLeaf )
            {
                for ( ; i < count+1 ; i++ )
                    if ( ptrs.get(i) <= 0 ) 
                        error("Node: %d: Invalid child pointer @%d :: %s", id, i , this) ;
                for ( ; i < bTreeParams.MaxPtr ; i ++ )
                    if ( ! ptrs.isClear(i) )
                    {
                        ptrs.isClear(i) ;
                        error("Node: %d: Unexpected pointer @%d :: %s", id, i, this) ;
                    }
            }
        }
    }

    // Assumes a valid tree
    
    private final void internalCheckNodeDeep()
    {
        if ( ! CheckingNode )
            return ;
        checkNodeDeep() ;
    }
    
    public final void checkNodeDeep()
    {
        if ( isRoot() )
        {
            if ( !isLeaf && count == 0 )
                error("Root is of size zero (one pointer) but not a leaf") ;
//            if ( parent != BTreeParams.RootParent )
//                error("Root parent is wrong") ;
            
//            if ( count == 0 )
//                return ;
        }
        checkNodeDeep(null, null) ;
    }
    
    private void checkNodeDeep(Record min, Record max)
    {
        checkNode(min, max) ;
        if ( isLeaf )
            return ;
        
        // Check pointers.
        int limit = (count == 0) ? 0 : count+1 ; 
        
        for ( int i = 0 ; i < limit ; i++ )
        {
            Record min1 = min ;
            Record max1 = max ;
            if ( ! pageMgr.valid(ptrs.get(i)) )
                error("Node: %d: Dangling ptr in block @%d :: %s", id, i, this) ;
            BTreeNode n = pageMgr.get(ptrs.get(i), id) ;
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
            if ( n.parent != id )
                error("Node: %d [%d]: Parent/child mismatch :: %s", id, n.parent, this) ;
            
            n.checkNodeDeep(min1, max1) ;
        }
    }

    private void error(String msg, Object... args)
    {
        msg = format(msg, args) ;
        System.out.println(msg) ;
        System.out.flush();
        try {
            pageMgr.dump() ;
            bTree.dump() ;
            System.out.flush();
        } catch (Exception ex) {}
        throw new BTreeException(msg) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */