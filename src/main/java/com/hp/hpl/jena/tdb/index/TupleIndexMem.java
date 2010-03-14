/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;


import java.util.Iterator;

import org.openjena.atlas.iterator.NullIterator ;
import org.openjena.atlas.iterator.SingletonIterator ;
import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.InternalErrorException ;
import org.openjena.atlas.lib.Tuple ;



import com.hp.hpl.jena.tdb.index.mem.Index;
import com.hp.hpl.jena.tdb.index.mem.Index3;
import com.hp.hpl.jena.tdb.index.mem.IterFunc;
import com.hp.hpl.jena.tdb.store.NodeId;

/** In-mmeory structures and indexing. */
public class TupleIndexMem extends TupleIndexBase
{
    // Not used.
    // Converty to TupleIndexBaseFind.
    // Simple in-memory structure.
    private Index3<NodeId, NodeId, NodeId, Tuple<NodeId>> index = new Index3<NodeId, NodeId, NodeId, Tuple<NodeId>>() ;
    
    public TupleIndexMem(int N, ColumnMap colMapping)
    {
        super(N, colMapping) ;
        if ( N != 3 )
            throw new UnsupportedOperationException("TupleIndexMem - triples only") ;
    }
    
    @Override
    protected boolean performAdd(Tuple<NodeId> tuple)
    {
        NodeId x1 = colMap.mapSlot(0, tuple) ;
        NodeId x2 = colMap.mapSlot(1, tuple) ;
        NodeId x3 = colMap.mapSlot(2, tuple) ;
        return index.put(x1,x2,x3, tuple) ;  
    }

    @Override
    protected boolean performDelete(Tuple<NodeId> tuple)
    {
        NodeId x1 = colMap.mapSlot(0, tuple) ;
        NodeId x2 = colMap.mapSlot(1, tuple) ;
        NodeId x3 = colMap.mapSlot(2, tuple) ;
        return index.remove(x1,x2,x3 ) ;  
    }

    //@Override
    public Iterator<Tuple<NodeId>> all()
    {
        return index.flatten() ;
    }

    @Override
    protected Iterator<Tuple<NodeId>> performFind(Tuple<NodeId> pattern)
    {
        NodeId x1 = colMap.mapSlot(0, pattern) ;
        if ( NodeId.doesNotExist(x1) )
            return new NullIterator<Tuple<NodeId>>() ;
        if ( undef(x1) )
            x1 = null ;
        
        NodeId x2 = colMap.mapSlot(1, pattern) ;
        if ( NodeId.doesNotExist(x2) )
            return new NullIterator<Tuple<NodeId>>() ;
        if (undef(x2) )
            x2 = null ;

        NodeId x3 = colMap.mapSlot(2, pattern) ;
        if ( NodeId.doesNotExist(x3) )
            return new NullIterator<Tuple<NodeId>>() ;
        if ( undef(x3) )
            x3 = null ;
        
        if ( x1 == null )
            throw new InternalErrorException("TupleIndexMem.find: no first index") ;
        
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

    //@Override
    public void sync()
    { sync(true) ; }
    
    //@Override
    public void sync(boolean force)
    {}

    //@Override
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