/*
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

package org.seaborne.tdb2.store.tupletable;

import static java.lang.String.format ;
import static org.seaborne.tdb2.sys.SystemTDB.SizeOfNodeId ;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.function.Predicate ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.NullIterator ;
import org.apache.jena.atlas.iterator.SingletonIterator ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.lib.TupleLib ;
import org.seaborne.tdb2.store.NodeId ;
import org.seaborne.tdb2.store.NodeIdFactory;

public class TupleIndexRecord extends TupleIndexBase
{
    private static final boolean Check = false ;
    private RangeIndex index ; 
    private RecordFactory factory ;
    
    public TupleIndexRecord(int N,  TupleMap tupleMapping, String name, RecordFactory factory, RangeIndex index)
    {
        super(N, tupleMapping, name) ;
        this.factory = factory ;
        this.index = index ;
        
        if ( factory.keyLength() != N*SizeOfNodeId)
            throw new TDBException(format("Mismatch: TupleIndex of length %d is not comparative with a factory for key length %d", N, factory.keyLength())) ;
    }
    
    /** Insert a tuple */
    @Override
    protected void performAdd(Tuple<NodeId> tuple) { 
        Record r = TupleLib.record(factory, tuple, tupleMap) ;
        index.insert(r) ;
    }
    
    /** Delete a tuple */
    @Override
    protected void performDelete(Tuple<NodeId> tuple) { 
        Record r = TupleLib.record(factory, tuple, tupleMap) ;
        index.delete(r) ;
    }
    
    /** Insert tuples */
    @Override
    public void addAll(Collection<Tuple<NodeId>> tuples) {
        for ( Tuple<NodeId> t : tuples ) 
            add(t) ;
    }
    
    /** Delete tuples */
    @Override
    public void deleteAll(Collection<Tuple<NodeId>> tuples) {
        for ( Tuple<NodeId> t : tuples ) 
            delete(t) ;
    }
    
    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any.
     *  Input pattern in natural order, not index order.
     */
    
    @Override
    protected Iterator<Tuple<NodeId>> performFind(Tuple<NodeId> pattern) {
        return findOrScan(pattern) ;
    }

    // Package visibility for testing.
    final Iterator<Tuple<NodeId>> findOrScan(Tuple<NodeId> pattern) {
        return findWorker(pattern, true, true) ;
    }
    
    final Iterator<Tuple<NodeId>> findOrPartialScan(Tuple<NodeId> pattern) {
        return findWorker(pattern, true, false) ;
    }

    final Iterator<Tuple<NodeId>> findByIndex(Tuple<NodeId> pattern) {
        return findWorker(pattern, false, false) ;
    }
    
    private Iterator<Tuple<NodeId>> findWorker(Tuple<NodeId> patternNaturalOrder, boolean partialScanAllowed, boolean fullScanAllowed) {
        if ( Check )
        {
            if ( tupleLength != patternNaturalOrder.len() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", patternNaturalOrder.len(), tupleLength)) ;
        } 
        
        // Convert to index order.
        Tuple<NodeId> pattern = tupleMap.map(patternNaturalOrder) ;
        
        // Canonical form.
        int numSlots = 0 ;
        int leadingIdx = -2;    // Index of last leading pattern NodeId.  Start less than numSlots-1
        boolean leading = true ;
        
        // Records.
        Record minRec = factory.createKeyOnly() ;
        Record maxRec = factory.createKeyOnly() ;
        
        // Set the prefixes.
        for ( int i = 0 ; i < pattern.len() ; i++ ) {
            NodeId X = pattern.get(i) ;
            if ( NodeId.isAny(X) ) {
                X = null ;
                // No longer seting leading key slots.
                leading = false ;
                continue ;
            }
//            if ( NodeId.isDoesNotExist(X) )
//                return Iter.nullIterator();

            numSlots++ ;
            if ( leading ) {
                leadingIdx = i ;
                NodeIdFactory.set(X, minRec.getKey(), i*SizeOfNodeId) ;
                NodeIdFactory.set(X, maxRec.getKey(), i*SizeOfNodeId) ;
            }
        }
        
        // Is it a simple existence test?
        if ( numSlots == pattern.len() ) {
            if ( index.contains(minRec) )
                return new SingletonIterator<>(pattern) ;
            else
                return new NullIterator<>() ;
        }
        
        Iterator<Record> iter = null ;
        
        if ( leadingIdx < 0 ) {
            if ( ! fullScanAllowed )
                return null ;
            //System.out.println("Full scan") ;
            // Full scan necessary
            iter = index.iterator() ;
        } else {
            // Adjust the maxRec.
            NodeId X = pattern.get(leadingIdx) ;
            // Set the max Record to the leading NodeIds, +1.
            // Example, SP? inclusive to S(P+1)? exclusive where ? is zero. 
            NodeIdFactory.setNext(X, maxRec.getKey(), leadingIdx*SizeOfNodeId) ;
            iter = index.iterator(minRec, maxRec) ;
        }
        
        Iterator<Tuple<NodeId>> tuples = Iter.map(iter, item -> TupleLib.tuple(item, tupleMap)) ;
        
        if ( leadingIdx < numSlots-1 ) {
            if ( ! partialScanAllowed )
                return null ;
            // Didn't match all defined slots in request.  
            // Partial or full scan needed.
            //pattern.unmap(colMap) ;
            tuples = scan(tuples, patternNaturalOrder) ;
        }
        
        return tuples ;
    }
    
    @Override
    public Iterator<Tuple<NodeId>> all()
    {
        Iterator<Record> iter = index.iterator() ;
        return Iter.map(iter, item -> TupleLib.tuple(item, tupleMap)) ;
    }
    
    private Iterator<Tuple<NodeId>> scan(Iterator<Tuple<NodeId>> iter, Tuple<NodeId> pattern) {
        Predicate<Tuple<NodeId>> filter = (item) -> {
            // Check on pattern and item (both in natural order)
            for ( int i = 0 ; i < tupleLength ; i++ ) {
                NodeId n = pattern.get(i) ;
                // The pattern must be null/Any or match the tuple being tested.
                if ( ! NodeId.isAny(n) )
                    if ( ! item.get(i).equals(n) ) 
                        return false ;
            }
            return true ;
        } ;
        
        return Iter.filter(iter, filter) ;
    }
    
    @Override
    public void close() {
        index.close() ;
    }

    @Override
    public void sync() {
        index.sync() ;
    }

    public final RangeIndex getRangeIndex() {
        return index ;
    }

    //protected final RecordFactory getRecordFactory()        { return factory ; }
    
    @Override
    public boolean isEmpty() {
        return index.isEmpty() ;
    }

    @Override
    public void clear() {
        index.clear() ;
    }

    @Override
    public long size() {
        return index.size() ;
    }
}
