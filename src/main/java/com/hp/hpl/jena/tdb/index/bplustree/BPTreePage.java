/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import com.hp.hpl.jena.tdb.base.page.Page;
import com.hp.hpl.jena.tdb.base.record.Record;

/** Abstraction of a B+Tree node - either an branch (BTreeNode) or leaf (BTreeLeaf - records)*/
abstract public interface BPTreePage extends Page
{
    // BPTreePageMgr to be superclass of BPTreeRecordsMgr and BPTreeNodeMgr 
    //  Provides the bpTree and their operations.
    
    // Only "public" for external very low level tools in development to access this class.
    // Assume package access.

//    // Does not use PageBase because BPTreeRecords does not need it.
//    protected final BPlusTree bpTree ;
//    protected final BPlusTreeParams params ;
//    
//    protected BPTreePage(BPlusTree bpTree)
//    {
//        if ( bpTree == null )
//            System.err.println("NULL B+Tree") ;
//        
//        this.bpTree = bpTree ;
//        this.params = bpTree.getParams() ;
//    }
    
    abstract BPlusTree getBPlusTree() ;
    abstract BPlusTreeParams getParams() ;
    
    /** Split in two, return the new (upper) page.  
     *  Split key is highest key of the old (lower) page.
     *  Does NOT put pages back to any underlying block manager
     */ 
    abstract BPTreePage split() ;
    
    /** Move the element from the high end of this to the low end of other,
    *  possible including the splitKey
     * Return the new split point (highest record in left tree for records; moved element for nodes)
     */
    abstract Record shiftRight(BPTreePage other, Record splitKey) ;
    
    /** Move the element from the high end of other to the high end of this, 
     * possible including the splitKey
     * Return the new split point (highest record in left tree; moved element for nodes)
     */
    abstract Record shiftLeft(BPTreePage other, Record splitKey) ;
    
    /** Merge this (left) and the page imemdiately to it's right other into a single block
     * Return the new page (may be left or right)
     */
    abstract BPTreePage merge(BPTreePage right, Record splitKey) ;
    
//    /** Rebalance records/pointers across this page and page other. 
//     *  Can assume other is the same type as 'this'
//     *  Can assume that other is the immediate left or immediate right of this page; 
//     *  which is indicated by the boolean.
//     *  Return BPTreePage if it is the only page now used having released other.  
//     */
//    public abstract BPTreePage rebalance(BPTreePage other, boolean pageIsRight) ; 
    
    /** Test whether this page is full (has no space for a new element) - used in "insert" */
    abstract boolean isFull() ;
    
    /**  Test whether this page is of minimum size (removing a record would violate the packing limits) - used in "delete" */
    abstract boolean isMinSize() ;
    
    abstract int getCount() ;
    
    abstract void setCount(int count) ;
  
    abstract public int getMaxSize() ;
    
    /**  Test whether this page has any keys */
    abstract boolean hasAnyKeys() ;

    /** Find a record; return null if not found */
    abstract Record internalSearch(Record rec) ;
    
    /** Find the page for the record (bottom-most page) */
    abstract BPTreeRecords findPage(Record rec) ;
    
    /** Find the first page (supports iterators) */
    abstract BPTreeRecords findFirstPage() ;

    /** Insert a record - return existing value if any, else null - put back modifed blocks */
    abstract Record internalInsert(Record record) ;
    
    /** Delete a record - return the old value if there was one, else null - put back modifed blocks */
    abstract Record internalDelete(Record record) ;

    /** Least in page */
    abstract Record getLowRecord() ;

    /** Greatest in page */
    abstract Record getHighRecord() ;

    /** Least in subtree */
    abstract Record minRecord() ;

    /** Greatest in subtree */
    abstract Record maxRecord() ;
    
    /** Finished with this block (for now!) */
    abstract void put() ;
    
    /** Discard with this block (for ever) */
    abstract void release() ;
    
//    /** Promote to a write */
//    abstract BPTreePage promote() ;
    
    /** Check - just this level.*/
    abstract void checkNode() ;
    
    /** Check - here and below */
    abstract void checkNodeDeep() ;

    /** Return the split point for this record (need only be a key)*/
    abstract Record getSplitKey() ;
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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