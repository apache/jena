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

import com.hp.hpl.jena.tdb.base.page.Page ;
import com.hp.hpl.jena.tdb.base.record.Record ;

/** Abstraction of a B+Tree node - either an branch (BTreeNode) or leaf (BTreeLeaf - records)*/
abstract public class BPTreePage implements Page
{
    // Does not use PageBase because BPTreeRecords does not need it.
    protected final BPlusTree bpTree ;
    protected final BPlusTreeParams params ;
    
    protected BPTreePage(BPlusTree bpTree)
    {
        if ( bpTree == null )
            System.err.println("NULL B+Tree") ;
        
        this.bpTree = bpTree ;
        this.params = bpTree.getParams() ;
    }
    
    public final BPlusTree getBPlusTree()       { return bpTree ; } 
    public final BPlusTreeParams getParams()    { return params ; }
    
//    /** Return the page number */
//    abstract int getId() ;
    
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
     */
    abstract BPTreePage merge(BPTreePage right, Record splitKey) ;
    //* Return the new page (may be left or right)
    
    /** Test whether this page is full (has no space for a new element) - used in "insert" */
    abstract boolean isFull() ;
    
    /**  Test whether this page is of minimum size (removing a record would violate the packing limits) - used in "delete" */
    abstract boolean isMinSize() ;
    
    abstract int getCount() ;
    
    abstract void setCount(int count) ;
  
    abstract int getMaxSize() ;
    
    /**  Test whether this page has any keys */
    abstract boolean hasAnyKeys() ;

    /** Find a record; return null if not found */
    abstract Record internalSearch(Record rec) ;
    
//    /** Find the page for the record (bottom-most page) */
//    abstract BPTreeRecords findPage(Record rec) ;
//    
//    /** Find the first page (supports iterators) */
//    abstract BPTreeRecords findFirstPage() ;

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
    
    /** Write, or at least ensure wil be written */
    abstract void write() ; 
    
    /** Turn a read page into a write page */
    abstract void promote() ;

    /** Mark as no longer needed */
    abstract void release() ;
    
    /** Discard with this block (for ever) */
    abstract void free() ;

    /** Check - just this level.*/
    abstract void checkNode() ;
    
    /** Check - here and below */
    abstract void checkNodeDeep() ;

    /** Return the split point for this record (need only be a key)*/
    abstract Record getSplitKey() ;
}
