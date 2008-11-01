/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import iterator.Filter;
import iterator.Iter;
import iterator.Transform;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.core.Closeable;

import lib.Bytes;
import lib.Tuple;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Descriptor;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;

import static java.lang.String.format ;

// Tuple<NodeID> <=> Record
// Extract from tripleTable.

// NEED TO GENERALISE Descriptor.
// See XXX below

public class TupleIndex implements Sync, Closeable
{
    // Usually done further out.
    static final boolean Check = false ;
    RangeIndex index ; 
    final int tupleLength ;
    RecordFactory factory ;
    private Descriptor descriptor ;
    
    public TupleIndex(int N,  Descriptor desc, RecordFactory factory)
    {
        this.tupleLength = N ;
        this.factory = factory ;
        this.descriptor = desc ;
        if ( factory.keyLength() != N*SizeOfNodeId)
            throw new TDBException(format("Mismatch: TupleIndex of length %d is not comparative with a factory for key length %d", N, factory.keyLength())) ;
    }
    
    /** Insert a tuple - return true if it was really added, false if it was a duplicate */
    public boolean add( Tuple<NodeId> tuple) 
    { 
        if ( Check )
        {
            if ( tupleLength != tuple.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", tuple.size(), tupleLength)) ;
        }

        Record r = descriptor.record(tuple) ;
        return index.add(r) ;
    }
    /** Delete a tuple - return true if it was deleted, false if it didn't exist */
    public boolean delete( Tuple<NodeId> tuple ) 
    { 
        if ( Check )
        {
            if ( tupleLength != tuple.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", tuple.size(), tupleLength)) ;
        }

        Record r = descriptor.record(tuple) ;
        return index.delete(r) ;
    }
    
    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any.
     */
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern)
    {
        if ( Check )
        {
            if ( tupleLength != pattern.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", pattern.size(), tupleLength)) ;
        } 
        
        NodeId[] pattern2 = new NodeId[pattern.size()] ;
        
        // Canonical form.
        int numSlots = 0 ;
        int leadingIdx = 0 ;    // Index of last leading pattern NodeId.
        boolean leading = true ;
        // Records.
        Record minRec = factory.createKeyOnly() ;
        Record maxRec = factory.createKeyOnly() ;

        
        for ( int i = 0 ; i < pattern.size() ; i++ )
        {
            pattern2[i] = pattern.get(i) ;
            if ( pattern2[i] == NodeId.NodeIdAny )
                pattern2[i] = null ;
            
            NodeId X = pattern2[i] ;
            if ( X != null )
            {
                numSlots++ ;
                if ( leading )
                {
                    leadingIdx = i ;
                    Bytes.setLong(X.getId(), minRec.getKey(), i*SizeOfNodeId) ;
                    Bytes.setLong(X.getId(), maxRec.getKey(), i*SizeOfNodeId) ;
                }
            }
            else
                // Not leading key slots.
                leading = false ;
        }

        
        // Adjust the 
        
        Iterator<Record> iter = null ;
        
        if ( numSlots == 0 )
            // No index at all.  Scan.
            iter = index.iterator() ;
        else 
        {
            if ( true )
                throw new ARQNotImplemented("Unfinished: TupleIndex.find") ;
            // Adjust the maxRec.
            NodeId X = pattern2[leadingIdx] ;
            Bytes.setLong(X.getId()+1, maxRec.getKey(), leadingIdx*SizeOfNodeId) ;
            iter = index.iterator(minRec, maxRec) ;
        }
        
        Iterator<Tuple<NodeId>> tuples = Iter.map(iter, transformToTuple) ;
        
        if ( leadingIdx < numSlots-1 )
            // Didn't match all defined slots in request.  
            // Partial or full scan needed.
            tuples = scan(tuples, pattern) ;
        
        return tuples ;
    }
    
    private Transform<Record, Tuple<NodeId>> transformToTuple = new Transform<Record, Tuple<NodeId>>()
    {
        @Override
        public Tuple<NodeId> convert(Record item)
        {
            return descriptor.tuple(item) ;
        }
    } ; 
    
    private Iterator<Tuple<NodeId>> scan(Iterator<Tuple<NodeId>> iter,
                                         final Tuple<NodeId> pattern)
    {
        Filter<Tuple<NodeId>> filter = new Filter<Tuple<NodeId>>()
        {
            @Override
            public boolean accept(Tuple<NodeId> item)
            {
                // Check on pattern
                for ( int i = 0 ; i < tupleLength ; i++ )
                {
                    // The pattern must be null or match the tuple being tested.
                    if ( pattern.get(i) != null && item.get(i) != pattern.get(i) ) 
                        return false ;
                }
                return true ;
            }
        } ;
        
        return Iter.filter(iter, filter) ;
    }
    
    public int weight(Tuple<NodeId> pattern)
    {
        if ( Check )
        {
            if ( tupleLength != pattern.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", pattern.size(), tupleLength)) ;
        } 
        
        for ( int i = 0 ; i < tupleLength ; i++ )
        {
            if ( true )
                throw new ARQNotImplemented("Unfinished: TupleIndex.find") ;

            //NodeId X = desc.getSlot(i, pattern) ;
            NodeId X = null ;
            if ( X == null ) return i ;
        }
        return 0 ;
    }

    
    @Override
    public void close()
    {
        index.close();
    }
    
    @Override
    public void sync(boolean force)
    {
        index.sync(force) ;
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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