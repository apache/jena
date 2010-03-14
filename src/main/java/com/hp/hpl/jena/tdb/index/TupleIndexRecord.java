/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;
import static java.lang.String.format;

import java.util.Iterator;

import org.openjena.atlas.iterator.* ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Tuple ;



import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.lib.TupleLib;
import com.hp.hpl.jena.tdb.store.NodeId;

public class TupleIndexRecord extends TupleIndexBase
{
    private static final boolean Check = false ;
    private RangeIndex index ; 
    private RecordFactory factory ;
    
    public TupleIndexRecord(int N,  ColumnMap colMapping, RecordFactory factory, RangeIndex index)
    {
        super(N, colMapping) ;
        this.factory = factory ;
        this.index = index ;
        
        if ( factory.keyLength() != N*SizeOfNodeId)
            throw new TDBException(format("Mismatch: TupleIndex of length %d is not comparative with a factory for key length %d", N, factory.keyLength())) ;
    }
    
    /** Insert a tuple - return true if it was really added, false if it was a duplicate */
    @Override
    protected boolean performAdd(Tuple<NodeId> tuple) 
    { 
        Record r = TupleLib.record(factory, tuple, colMap) ;
        return index.add(r) ;
    }
    
    /** Delete a tuple - return true if it was deleted, false if it didn't exist */
    @Override
    protected boolean performDelete(Tuple<NodeId> tuple) 
    { 
        Record r = TupleLib.record(factory, tuple, colMap) ;
        return index.delete(r) ;
    }
    
    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any.
     *  Input pattern in natural order, not index order.
     */
    
    @Override
    protected Iterator<Tuple<NodeId>> performFind(Tuple<NodeId> pattern)
    {
        return findOrScan(pattern) ;
    }

    // Package visibility for testing.
    final Iterator<Tuple<NodeId>> findOrScan(Tuple<NodeId> pattern)
    {
        return findWorker(pattern, true, true) ;
    }
    
    final Iterator<Tuple<NodeId>> findOrPartialScan(Tuple<NodeId> pattern)
    {
        return findWorker(pattern, true, false) ;
    }

    final Iterator<Tuple<NodeId>> findByIndex(Tuple<NodeId> pattern)
    {
        return findWorker(pattern, false, false) ;
    }
    
    private Iterator<Tuple<NodeId>> findWorker(Tuple<NodeId> pattern, boolean partialScanAllowed, boolean fullScanAllowed)
    {
        if ( Check )
        {
            if ( tupleLength != pattern.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", pattern.size(), tupleLength)) ;
        } 
        
        // Convert to index order.
        pattern = colMap.map(pattern) ;
        
        // Canonical form.
        int numSlots = 0 ;
        int leadingIdx = -2;    // Index of last leading pattern NodeId.  Start less than numSlots-1
        boolean leading = true ;
        
        // Records.
        Record minRec = factory.createKeyOnly() ;
        Record maxRec = factory.createKeyOnly() ;
        
        for ( int i = 0 ; i < pattern.size() ; i++ )
        {
            NodeId X = pattern.get(i) ;
            if ( NodeId.isAny(X) )
                X = null ;
            
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
        
        // Is it a simple existence test?
        if ( numSlots == pattern.size() )
        {
            if ( index.contains(minRec) )
                return new SingletonIterator<Tuple<NodeId>>(pattern) ;  
            else
                return new NullIterator<Tuple<NodeId>>() ;
        }
        
        Iterator<Record> iter = null ;
        
        if ( leadingIdx < 0 )
        {
            if ( ! fullScanAllowed )
                return null ;
            //System.out.println("Full scan") ;
            // Full scan necessary
            iter = index.iterator() ;
        }
        else 
        {
            // Adjust the maxRec.
            NodeId X = pattern.get(leadingIdx) ;
            // Set the max Record to the leading NodeIds, +1.
            // Example, SP? inclusive to S(P+1)? exclusive where ? is zero. 
            Bytes.setLong(X.getId()+1, maxRec.getKey(), leadingIdx*SizeOfNodeId) ;
            iter = index.iterator(minRec, maxRec) ;
        }
        
        Iterator<Tuple<NodeId>> tuples = Iter.map(iter, transformToTuple) ;
        
        if ( leadingIdx < numSlots-1 )
        {
            if ( ! partialScanAllowed )
                return null ;
            
            //System.out.println("Partial scan") ;
            // Didn't match all defined slots in request.  
            // Partial or full scan needed.
            tuples = scan(tuples, pattern) ;
        }
        
        return tuples ;
    }
    
    //@Override
    public Iterator<Tuple<NodeId>> all()
    {
        Iterator<Record> iter = index.iterator() ;
        return Iter.map(iter, transformToTuple) ;
    }
    
    private Transform<Record, Tuple<NodeId>> transformToTuple = new Transform<Record, Tuple<NodeId>>()
    {
        //@Override
        public Tuple<NodeId> convert(Record item)
        {
            return TupleLib.tuple(item, colMap) ;
        }
    } ; 
    
    private Iterator<Tuple<NodeId>> scan(Iterator<Tuple<NodeId>> iter,
                                         final Tuple<NodeId> pattern)
    {
        Filter<Tuple<NodeId>> filter = new Filter<Tuple<NodeId>>()
        {
            //@Override
            public boolean accept(Tuple<NodeId> item)
            {
                // Check on pattern
                for ( int i = 0 ; i < tupleLength ; i++ )
                {
                    NodeId n = pattern.get(i) ;
                    // The pattern must be null/Any or match the tuple being tested.
                    if ( ! NodeId.isAny(n) )
                        if ( ! item.get(i).equals(pattern.get(i)) ) 
                            return false ;
                }
                return true ;
            }
        } ;
        
        return Iter.filter(iter, filter) ;
    }
    
    //@Override
    public void close()
    {
        index.close();
    }
    
    //@Override
    public void sync()
    { sync(true) ; }
    
    //@Override
    public void sync(boolean force)
    {
        index.sync(force) ;
    }

    public final RangeIndex getRangeIndex()                 { return index ; } 

    //protected final RecordFactory getRecordFactory()        { return factory ; }
    
    //@Override
    public boolean isEmpty()
    {
        return index.isEmpty() ;
    }
    
    //@Override
    public long size()
    {
        return index.size() ;
    }
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