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

package org.seaborne.dboe.trans.bplustree;

import java.util.* ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.trans.bplustree.AccessPath.AccessStep ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

class BPTreeRangeIterator implements Iterator<Record> {
    static Logger log = LoggerFactory.getLogger(BPTreeRangeIterator.class) ;
    
    public static Iterator<Record> create(BPTreeNode node, Record minRec, Record maxRec) {
        if ( minRec != null && maxRec != null && Record.keyGE(minRec, maxRec) )
            return Iter.nullIter();
        return new BPTreeRangeIterator(node, minRec, maxRec) ;
    }
    
    // Convert path to a stack of iterators
    private final Deque<Iterator<BPTreePage>> stack = new ArrayDeque<>();
    private final Deque<BPTreePage> trackStack = new ArrayDeque<>();
    final private Record minRecord ;
    final private Record maxRecord ;
    private Iterator<Record> current ;
    private Record slot = null ;
    private boolean finished = false ;
    
    BPTreeRangeIterator(BPTreeNode node, Record minRec, Record maxRec ) {
        this.minRecord = minRec ;
        this.maxRecord = maxRec ;
        BPTreeRecords r = loadStack(node) ;
        current = r.getRecordBuffer().iterator(minRecord, maxRecord) ;
    }

    @Override
    public boolean hasNext() {
        if ( BPT.logging(log) )
            BPT.log(log, "hasNext") ;
        if ( finished ) 
            return false ;
        if ( slot != null )
            return true ;
        if ( current != null && current.hasNext() ) {
            slot = current.next() ;
            return true ;
        }
        if ( BPT.logging(log) )
            BPT.log(log, "** End of current") ;
        do {
            current = moveOnCurrent() ;
            if ( current == null ) {
                end() ;
                return false ;
            }
        } while(!current.hasNext()) ;
        slot = current.next() ;
        return true ;
        
    }
    
    private Iterator<Record> moveOnCurrent() {
        Iterator<BPTreePage> iter = null ;
        while(!stack.isEmpty()) { 
            iter = stack.pop() ;
            BPTreePage stackPage = trackStack.pop(); 
            if ( BPT.logging(log) )
                BPT.log(log, "hasNext: pop : "+stackPage.label()) ;
            if ( iter.hasNext() )
                break ;
            if ( BPT.logging(log) )
                BPT.log(log, "hasNext: popped empty") ;
        } 
        
        if ( iter == null || ! iter.hasNext() )
            return null ;
        BPTreePage p = iter.next() ;
        // Do this iterator.
        
        BPTreeRecords r = null ;
        if (p instanceof BPTreeNode) {
            r = loadStack((BPTreeNode)p) ;
            if ( BPT.logging(log) ) {
                BPT.log(log, "Node: %s", p.label()) ;
                BPT.log(log, "    r="+r.label()) ;
            }
        }
        else {
            if ( BPT.logging(log) )
                BPT.log(log, "Records: "+p.label()) ;
            r = (BPTreeRecords)p ;
        }
        return r.getRecordBuffer().iterator(minRecord, maxRecord) ;
    }
    
    private BPTreeRecords loadStack(BPTreeNode node) {
        AccessPath path = new AccessPath(null) ;
        if ( minRecord == null )
            node.internalMinRecord(path) ;
        else
            node.internalSearch(path, minRecord) ;
        if ( BPT.logging(log) )
            BPT.log(log, "loadStack: node: %s", node.label()) ;
        
        List<AccessStep> steps = path.getPath() ;
        if ( BPT.logging(log) )
            BPT.log(log, "loadStack: path = "+path) ;
        for ( AccessStep step : steps ) {
            if ( BPT.logging(log) )
                BPT.log(log, "           step = "+step) ;
            BPTreeNode n = step.node ; 
            Iterator<BPTreePage> it = n.iterator(minRecord, maxRecord) ;
            if ( it == null || ! it.hasNext() )
                continue ;
//            // Drop the first  
//            it.next() ;
            stack.push(it) ;
            
            if ( BPT.logging(log) )
                BPT.log(log, "loadStack: push : "+n.label()) ;
            trackStack.push(n) ;
        }
        
        if ( BPT.logging(log) ) {
            BPT.log(log, "loadStack: stack = %d", stack.size()) ;
            StringBuilder sb = new StringBuilder() ;
            sb.append("loadStack: ") ;
            trackStack.forEach(z -> sb.append(" "+z.label())) ;
            BPT.log(log, sb.toString()) ;
        }
        
        BPTreePage p = steps.get(steps.size()-1).page ;
        if ( ! ( p instanceof BPTreeRecords ) )
            throw new InternalErrorException("Last path step not to a records block") ;
        return (BPTreeRecords)p ;
    }

    private void end() {
        if ( BPT.logging(log) )
            BPT.log(log, "end") ;
        finished = true ;
        current = null ;
    }
    
    @Override
    public Record next() {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        Record r = slot ;
        if ( r == null )
            throw new InternalErrorException("Null slot after hashnext is true") ;
        slot = null ;
        return r ;
    }
}
