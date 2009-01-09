/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import iterator.NullIterator;
import iterator.SingletonIterator;

import java.util.Iterator;

import lib.ColumnMap;
import lib.InternalError;
import lib.Tuple;

import com.hp.hpl.jena.tdb.index.mem.Index;
import com.hp.hpl.jena.tdb.index.mem.Index3;
import com.hp.hpl.jena.tdb.index.mem.IterFunc;
import com.hp.hpl.jena.tdb.store.NodeId;

public class TupleIndexMem implements TupleIndex
{

    // Index by first element only.
    //private Map<NodeId, List<Tuple<NodeId>>> index = new HashMap<NodeId, List<Tuple<NodeId>>>();
    private Index3<NodeId, NodeId, NodeId, Tuple<NodeId>> index = new Index3<NodeId, NodeId, NodeId, Tuple<NodeId>>() ;
    private ColumnMap colMap ;
    private int tupleLength ;
    
    public TupleIndexMem(int N, ColumnMap colMapping)
    {
        this.tupleLength = N ;
        this.colMap = colMapping ;
    }
    
    @Override
    public boolean add(Tuple<NodeId> tuple)
    {
        NodeId x1 = colMap.mapSlot(0, tuple) ;
        NodeId x2 = colMap.mapSlot(1, tuple) ;
        NodeId x3 = colMap.mapSlot(2, tuple) ;
        return index.put(x1,x2,x3, tuple) ;  
    }

    @Override
    public Iterator<Tuple<NodeId>> all()
    {
        return index.flatten() ;
    }

    @Override
    public boolean delete(Tuple<NodeId> tuple)
    {
        NodeId x1 = colMap.mapSlot(0, tuple) ;
        NodeId x2 = colMap.mapSlot(1, tuple) ;
        NodeId x3 = colMap.mapSlot(2, tuple) ;
        return index.remove(x1,x2,x3 ) ;  
    }

    @Override
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern)
    {
        NodeId x1 = colMap.mapSlot(0, pattern) ;
        if ( x1 == NodeId.NodeDoesNotExist )
            return new NullIterator<Tuple<NodeId>>() ;
        if ( x1 == NodeId.NodeIdAny )
            x1 = null ;
        
        NodeId x2 = colMap.mapSlot(1, pattern) ;
        if ( x2 == NodeId.NodeDoesNotExist )
            return new NullIterator<Tuple<NodeId>>() ;
        if ( x2 == NodeId.NodeIdAny )
            x2 = null ;

        NodeId x3 = colMap.mapSlot(2, pattern) ;
        if ( x3 == NodeId.NodeDoesNotExist )
            return new NullIterator<Tuple<NodeId>>() ;
        if ( x3 == NodeId.NodeIdAny )
            x3 = null ;
        
        if ( x1 == null )
            throw new InternalError("TupleIndexMem.find: no first index") ;
        
        if ( x2 == null )
        {
            Index<NodeId, Index<NodeId, Tuple<NodeId>>> idx = index.get(x1) ;
            return IterFunc.flattenII(idx) ;
        }
        if ( x3 == null )
        {
            Index<NodeId, Tuple<NodeId>> idx = index.get(x1, x2) ;
            return idx.values().iterator() ;
        }
        
        Tuple<NodeId> t = index.get(x1, x2, x3) ;
        if ( t == null )
            return new NullIterator<Tuple<NodeId>>() ;
        return new SingletonIterator<Tuple<NodeId>>(t) ;
    }

    @Override
    public String getLabel()
    {
        return colMap.getLabel() ;
    }

    @Override
    public int getTupleLength()
    {
        return tupleLength ;
    }

    @Override
    public int weight(Tuple<NodeId> pattern)
    {
        if ( undef(pattern.get(0)) ) return 0 ;
        if ( undef(pattern.get(1)) ) return 1 ;
        if ( undef(pattern.get(2)) ) return 2 ;
        return 3 ;
    }

    private boolean undef(NodeId x)
    { return x == null || x == NodeId.NodeIdAny ; }
    
    @Override
    public boolean isEmpty()
    {
        return index.isEmpty() ;
    }

    @Override
    public long size()
    {
        return index.size() ;
    }
    
    @Override
    public void sync(boolean force)
    {}

    @Override
    public void close()
    {}

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