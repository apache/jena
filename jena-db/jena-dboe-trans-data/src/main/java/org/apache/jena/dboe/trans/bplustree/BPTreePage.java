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

import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.page.Page;
import org.apache.jena.dboe.base.record.Record;
import org.slf4j.Logger ;

/** Abstraction of a B+Tree node - either an branch (BTreeNode) or records block (BTreeRecords) */
abstract public class BPTreePage implements Page
{
    protected BPTreePage(BPlusTree bpTree) {
        this.bpTree = bpTree ;
        // bpTree can be null in testing.
        this.params = ( bpTree == null ) ? null : bpTree.getParams() ;
    }
    
    protected final BPlusTree bpTree ; 
    protected final BPlusTreeParams params ;

    /** Short form for logging */
    protected String label() {
        return String.format("%d[%s]", getId(), typeMark()) ;
    }
    
    protected abstract String typeMark() ;
    
    protected abstract Logger getLogger() ;
    
    abstract BlockMgr getBlockMgr() ;

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
    
    /** Merge this (left) and the page immediately to it's right other into a single block
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
    abstract Record internalSearch(AccessPath path, Record rec) ;
    
    /** Insert a record - return existing value if any, else null - put back modified blocks */
    abstract Record internalInsert(AccessPath path, Record record) ;
    
    /** Delete a record - return the old value if there was one, else null - put back modified blocks */
    abstract Record internalDelete(AccessPath path, Record record) ;

    /** Least in page */
    abstract Record getLowRecord() ;

    /** Greatest in page */
    abstract Record getHighRecord() ;

    /** Least in subtree */
    abstract Record internalMinRecord(AccessPath path) ;

    /** Greatest in subtree */
    abstract Record internalMaxRecord(AccessPath path) ;

    // For checking ...
    
    /** Least in subtree */
    Record minRecord()      { return internalMinRecord(null) ; } 

    /** Greatest in subtree */
    Record maxRecord()      { return internalMaxRecord(null) ; }
    
    /** Write, or at least ensure will be written */
    abstract void write() ; 
    
    /** Turn a read page into a write page. Return true if any changes were made. */
    abstract boolean promote() ;

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
