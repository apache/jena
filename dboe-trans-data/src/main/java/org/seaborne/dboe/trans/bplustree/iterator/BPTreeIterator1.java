/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.trans.bplustree.iterator;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.trans.bplustree.BPTreeNode ;

/** Iterator for one node */
public class BPTreeIterator1 /*extends IteratorSlotted<Record>*/ implements Iterator<Record>  {

    private final BPTreeNode node ;
    private Record slot = null ;
    private int idx = -1 ; 
    private final int idxMin ;
    private final int idxMax ;
    private boolean finished = false ;
    private Iterator<Record> current ;
    
    
    public BPTreeIterator1(BPTreeNode node, int idxMin, int idxMax, Record minRec, Record maxRec) {
        
        idx = idxMin ;
        this.node = node ;
        this.idxMin = idxMin ;
        this.idxMax = idxMax ;
        
    }


    @Override
    public boolean hasNext() {
        if ( finished )
            return false ;
        if ( slot != null )
            return true ; 
        if ( current.hasNext() ) {
            slot = current.next() ;
            return true ;
        }
        current = null ;
        // "or equals" ??
        if ( idx >= idxMax ) {
            finished = true ;
            return false ;
        }
            
        //get(idx) ;
        idx++ ;
        
        
        
        return false ;
    }


    @Override
    public Record next() {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        Record r = slot ;
        if ( r == null )
            throw new InternalErrorException("Slot is null") ;
        slot = null ;
        return r ;
    }
    
    

}

