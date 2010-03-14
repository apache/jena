/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import java.util.Iterator ;

import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Tuple ;


import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public abstract class TupleIndexBase implements TupleIndex
{
    private static final boolean Check = true ;

    protected final ColumnMap colMap ;
    protected final int tupleLength ;
    
    protected TupleIndexBase(int N,  ColumnMap colMapping)
    {
        this.tupleLength = N ;
        this.colMap = colMapping ;
    }
    
    /** Add tuple worker: Tuple passed in unmaped (untouched) order */
    protected abstract boolean performAdd(Tuple<NodeId> tuple) ;
    
    /** Delete tuple worker: Tuple passed in unmaped (untouched) order */
    protected abstract boolean performDelete(Tuple<NodeId> tuple) ;
    
    /** Find tuples worker: Tuple passed in unmaped (untouched) order */
    protected abstract Iterator<Tuple<NodeId>> performFind(Tuple<NodeId> tuple) ;

    /** Insert a tuple - return true if it was really added, false if it was a duplicate */
    //@Override
    public final boolean add(Tuple<NodeId> tuple) 
    { 
        if ( Check )
        {
            if ( tupleLength != tuple.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", tuple.size(), tupleLength)) ;
        }
        return performAdd(tuple) ;
    }
    /** Delete a tuple - return true if it was deleted, false if it didn't exist */
    //@Override
    public final boolean delete(Tuple<NodeId> tuple) 
    { 
        if ( Check )
        {
            if ( tupleLength != tuple.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", tuple.size(), tupleLength)) ;
        }

        return performDelete(tuple) ;
    }

    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any.
     *  Input pattern in natural order, not index order.
     */
    //@Override
    public final Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern)
    {
        if ( Check )
        {
            if ( tupleLength != pattern.size() )
            throw new TDBException(String.format("Mismatch: tuple length %d / index for length %d", pattern.size(), tupleLength)) ;
        } 
        // null to NodeId.NodIdAny ??
        return performFind(pattern) ;
    }
    
    //@Override
    public final int weight(Tuple<NodeId> pattern)
    {
        for ( int i = 0 ; i < tupleLength ; i++ )
        {
            NodeId X = colMap.fetchSlot(i, pattern) ;
            if ( undef(X) )
                // End of fixed terms
                return i ;
        }
        return tupleLength ;
    }
    

    //@Override
    public final String getLabel()
    {
        return colMap.getLabel() ;
    }

    //@Override
    public final int getTupleLength()
    {
        return tupleLength ;
    }

    public final ColumnMap getColumnMap() { return colMap ;  }
    
    protected final boolean undef(NodeId x)
    { return NodeId.isAny(x) ; }
    
    @Override
    public String toString() { return "index:"+getLabel() ; }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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