/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.btree;


import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openjena.atlas.iterator.Iter ;


import com.hp.hpl.jena.tdb.base.record.Record;



class BTreeRangeIterator implements Iterator<Record>
{
    // Could use parent pointers to do the walk but 
    // (1) still need to find a node in a pointer list each time
    // (2) to avoid that, need a stack of idx.
    
    /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */ 
    public static Iterator<Record> iterator(BTreeNode node, Record fromRec, Record toRec)
    {
        if ( toRec != null && fromRec != null && Record.keyLE(toRec, fromRec) )
            return Iter.nullIter() ;
        
        // Find the starting node.
        int idxMin = -1 ; 
            
        if ( fromRec != null )
            idxMin = node.findSlot(fromRec) ;

        int idxMax = -node.count-1 ;
        
        if ( toRec != null )
            idxMax = node.findSlot(toRec) ;
        
        if ( idxMin < 0 && idxMin == idxMax && !node.isLeaf )
        {
            // Fully contained in a subtree - the child behind the records insertion point.
            int x = -(idxMin+1) ;
            int subId = node.ptrs.get(x) ;
            BTreeNode n = node.pageMgr.get(subId, node.id) ; 
            return n.iterator(fromRec, toRec) ;
        }
        
        // Subtree.  Start here.
        return new BTreeRangeIterator(node, fromRec, idxMin, toRec, idxMax) ;
    }
    
    public static Iterator<Record> iterator(BTreeNode node)
    {
        return new BTreeRangeIterator(node) ;
        //return new RangeIterator(node, null, null) ;
    }

    private BTreeNode node ;

    // Index of record in this node to next yield.
    // idx == -1 => doing left subtree.
    // idx points to the next record of this node to yield (after sub-iteraror, if any, exhaused)
    
    private int idx ; 

    // Iterator over subtree
    private BTreeRangeIterator sub ;

    // Next thing to yield.
    private Record slot ;

    // Just above the last thing to yield.
    private Record upperLimitRec = null ;

    private BTreeRangeIterator(BTreeNode node, Record fromRec, int idxMin, Record toRec, int idxMax)
    {
        init(node, fromRec, idxMin, toRec, idxMax) ;
    }

    private BTreeRangeIterator(BTreeNode node, Record fromRec, Record toRec)
    {
        // Find the starting node.
        int idxMin = -1 ; 

        if ( fromRec != null )
            idxMin = node.findSlot(fromRec) ;

        int idxMax = -node.count-1 ;

        if ( toRec != null )
            idxMax = node.findSlot(toRec) ;
        init(node, fromRec, idxMin, toRec, idxMax) ;
    }

    private BTreeRangeIterator(BTreeNode node)
    {
        this.node = node ;
        idx = 0 ;
        slot = null ;
        if ( ! node.isLeaf )
            sub = makeSub(idx) ;
        upperLimitRec = null ;
    }

    private void init(BTreeNode node, Record fromRec, int idxMin, Record toRec, int idxMax)
    {
        this.node = node ;

        if ( idxMin >= 0 )
        {
            slot = null ;
            idx = idxMin ;
        }
        else
        {
            slot = null ;
            idx = -(idxMin+1) ;             // Insertion point - which is the next record in this node to yield.

            if ( ! node.isLeaf)
            {
                // Start at subtree to the immediate left.
                // Due to child ptr staggering, this is idx.
                // Not "makeSub(idx)" - need to pass down fromRec
                BTreeNode sn = node.pageMgr.get(node.ptrs.get(idx), node.id) ;
                sub = new BTreeRangeIterator(sn, fromRec, toRec) ;
            }
        }

        upperLimitRec = toRec ;
    }

    //@Override
    public boolean hasNext()
    {
        // Leaf?
        if ( slot != null )
            // Something waiting.
            return true ;

        // sub iterator active.
        if ( sub != null )
        {
            advanceSub() ;
            if ( slot != null )
                return true ; 
            // sub ended
            sub = null ;
        }

        // slot == null and sub == null

        if ( node.isLeaf )
        {
            if ( idx < node.count )
            {                 
                Record r = node.records.get(idx) ;
                setSlot(r) ;
            }
            idx++ ; 
            return (slot!=null) ;
        }

        // sub iterator ended.  Internal node.
        // Do one separator by setting slot and then move to the next one sub tree above. 

        if ( idx < node.count )
        {
            // Load the separator so this gets yielded next.
            // idx maybe -1 meaning do the subtree with no preceeding record from this node.
            if ( idx >= 0 )
            {
                Record r = node.records.get(idx) ; 
                setSlot(r) ;
                // Continue to set up sub
            }

            idx++ ;
            // Move the sub iterator to the next subtree.
            sub = makeSub(idx) ;
            
            // If the slot was not set, then set it from the subIterator.
            
            if ( slot != null )
                return true ;
            advanceSub() ;
            return ( slot != null ) ; 
        }

        // idx >= node.count
        return false ;
    }

    private void advanceSub()
    {
        if  ( sub.hasNext() )
            setSlot(sub.next()) ;
        else
            sub = null ;
    }
    
    private BTreeRangeIterator makeSub(int i)
    {
        BTreeNode sn = node.pageMgr.get(node.ptrs.get(i), node.id) ;
        return new BTreeRangeIterator(sn, null, upperLimitRec) ;
    }
    
    private void setSlot(Record rec)
    {
        if ( upperLimitRec != null && Record.keyGE(rec, upperLimitRec ) )
        {
            slot = null ;
            return ;
        }
        slot = rec ;
    }

    //@Override
    public Record next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        Record x = slot ;
        slot = null ;
        return x ;
    }

    //@Override
    public void remove()
    { throw new UnsupportedOperationException("remove") ; }

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