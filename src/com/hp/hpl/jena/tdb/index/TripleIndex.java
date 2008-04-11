/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import static com.hp.hpl.jena.tdb.Const.SizeOfNodeId;
import iterator.Iter;
import iterator.Transform;

import java.util.Iterator;

import lib.Bytes;
import lib.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.PGraphException;

// Stores NodeId triples in a single order. 

public class TripleIndex
{
    // Replacement for Index3.
    
    private static Logger log = LoggerFactory.getLogger(TripleIndex.class) ;
    Descriptor desc ;
    RangeIndex index ;
    
    public TripleIndex(String description, RangeIndex index)
    {
        this.desc = new Descriptor(description, index.getRecordFactory()) ;
        this.index = index ;
    }
    
    public boolean add(NodeId s, NodeId p, NodeId o)
    { 
        Record r = desc.record(s, p, o) ;
        return index.add(r) ;
    }

    public boolean delete(NodeId s, NodeId p, NodeId o)
    {
        Record r = desc.record(s, p, o) ;
        return index.delete(r) ;
    }
    
    private Transform<Record, Tuple<NodeId>> transformToSPO = new Transform<Record, Tuple<NodeId>>()
    {
        @Override
        public Tuple<NodeId> convert(Record item)
        {
            return desc.tuple(item) ;
        }
    } ; 
    
    private final Iterator<Tuple<NodeId>> asTuples(Iterator<Record> iter)
    { 
        return Iter.map(iter, transformToSPO) ;
    }
        
    public RangeIndex getIndex() { return index ; }
    public String getName() { return desc.getLabel() ; }
    
    public Iterator<Tuple<NodeId>> all()
    {
        Iterator<Record> iter = index.iterator() ;
        return asTuples(iter) ;
    }
    
    /** Weight the pattern - the number of slots used */
    public int weight(NodeId s, NodeId p, NodeId o)
    {
        int numSlots = 0 ;

        NodeId X = desc.getSlot1(s, p, o) ;
        if ( X == null ) return numSlots ;
        numSlots ++ ;
        
        NodeId Y = desc.getSlot2(s, p, o) ;
        if ( Y == null ) return numSlots ;
        numSlots ++ ;

        NodeId Z = desc.getSlot3(s, p, o) ;
        if ( Z == null ) return numSlots ;
        numSlots ++ ;
        return numSlots ;
    }
    
    public Iterator<Tuple<NodeId>> find(NodeId s, NodeId p, NodeId o)  
    {
        NodeId X = desc.getSlot1(s, p, o) ;
        NodeId Y = desc.getSlot2(s, p, o) ;
        NodeId Z = desc.getSlot3(s, p, o) ;
        
        if ( X == null )
            throw new PGraphException("No first key") ;
        
//        if ( Y != null && Z != null )
//        {
//            log.warn("All keys set ("+s+", "+p+", "+o+")") ;
//            // throw new PGraphException("All keys set") ;
//        }

//        if ( Y == null && Z != null )
//            throw new PGraphException("find("+s+","+p+","+o+") : Index is X?Z on "+desc.getLabel()) ;
        
        long min1 = X.getId() ;
        long max1 = min1 ;
        
        long min2, max2 ;
        long min3, max3 ;
        
        if ( Y == null )
        {
            min2 = 0 ;
            max2 = 0 ;
            max1 ++ ;
            min3 = 0 ;
            max3 = 0 ;
        }
        else
        {
            // X != null , Y != null 
            min2 = Y.getId() ;
            max2 = min2 ;
            if ( Z == null )    // It should be null but write for generality (and testing)
            {
                max2 ++ ;
                min3 = 0 ;
                max3 = 0 ;
            }
            else
            {
             // X != null , Y != null, Z != null 
                min3 = Z.getId() ;
                max3 = min3+1 ;     // Still +1 because index high is exclusive. 
            }
        }

        Record r1 = NodeLib.record(index.getRecordFactory(), min1, min2, min3) ;
        Record r2 = NodeLib.record(index.getRecordFactory(), max1, max2, max3) ;
        Iterator<Record> iter = index.iterator(r1, r2) ;
        return asTuples(iter) ;
    }
    
    // Debugging.
    public Iterator<Tuple<NodeId>> tuplesNativeOrder()
    {
        Transform<Record, Tuple<NodeId>> transform0 = new Transform<Record, Tuple<NodeId>>() {
            @Override
            public Tuple<NodeId> convert(Record e)
            {
                long x = getId(e, 0) ;
                long y = getId(e, SizeOfNodeId) ;
                long z = getId(e, 2*SizeOfNodeId) ;
                return new Tuple<NodeId>(NodeId.create(x), NodeId.create(y), NodeId.create(z)) ;
            }   
            private final long getId(Record r, int idx)
            {
                return Bytes.getLong(r.getKey(), idx) ;
            }
        } ; 
        return Iter.map(index.iterator(), transform0) ;
    }
    
    /** SameAs in native order */ 
    public boolean sameAs(TripleIndex other)
    {
        Iterator<Tuple<NodeId>> iter1 = tuplesNativeOrder() ;
        Iterator<Tuple<NodeId>> iter2 = other.tuplesNativeOrder() ;
        
        for ( ;; )
        {
            if ( ! iter1.hasNext() ) return ! iter2.hasNext() ;
            if ( ! iter2.hasNext() ) return false ;
            Tuple<NodeId> tuple1 = iter1.next() ;
            Tuple<NodeId> tuple2 = iter2.next() ;
            if ( ! tuple1.equals(tuple2) ) return false ;
        }
    }

    public void copyInto(TripleIndex index2)
    {
        Iterator<Tuple<NodeId>> iter = all() ;
        for ( int i = 0 ; iter.hasNext() ; i++ )
        {
            Tuple<NodeId> tuple = iter.next();
            index2.add(tuple.get(0), tuple.get(1), tuple.get(2)) ;
        } 
    }
    
    @Override
    public String toString()    { return "TripleIndex: "+desc.getLabel() ; }  
    
    public void sync(boolean force) { index.sync(force); }

    public void close() { index.close(); }
    
    public void dump()
    {
        System.out.println("Index: "+desc.getLabel()) ;
        IndexLib.print(getIndex()) ;
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