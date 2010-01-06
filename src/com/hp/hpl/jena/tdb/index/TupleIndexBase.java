/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import atlas.lib.ColumnMap ;
import atlas.lib.Tuple ;

import com.hp.hpl.jena.tdb.store.NodeId ;

public abstract class TupleIndexBase implements TupleIndex
{
    protected final ColumnMap colMap ;
    protected final int tupleLength ;
    
    protected TupleIndexBase(int N,  ColumnMap colMapping)
    {
        this.tupleLength = N ;
        this.colMap = colMapping ;
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