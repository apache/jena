/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import java.util.Iterator;

import lib.Tuple;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

/** Triple wrapper around an index */

public class TripleIndex2
{
    private final TupleIndex index ;

    public TripleIndex2(Location location, String indexType, RangeIndex rangeIndex)
    {
        if ( indexType.length() != 3 )
            throw new TDBException("Index description is not of length 3: "+indexType) ;
        
        ColumnMap columnMap = new ColumnMap("SPO", indexType) ;
        RecordFactory factory = rangeIndex.getRecordFactory() ;
        //RangeIndex rangeIndex = IndexBuilder.createRangeIndex(location, indexType, factory) ;
        this.index = new TupleIndex(3, columnMap, GraphTDB.indexRecordFactory, rangeIndex) ;  
    }
    
    public boolean add(NodeId s, NodeId p, NodeId o)
    { 
        Tuple<NodeId> t = tuple3(s,p,o) ; 
        return index.add(t) ;
    }

    public boolean delete(NodeId s, NodeId p, NodeId o)
    {
        Tuple<NodeId> t = tuple3(s,p,o) ; 
        return index.delete(t) ;
    }

    public int weight(NodeId s, NodeId p, NodeId o)
    {
        Tuple<NodeId> t = tuple3(s,p,o) ; 
        return index.weight(t) ;
    }
    
    private Tuple<NodeId> tuple3(NodeId x, NodeId y, NodeId z)
    {
        return new Tuple<NodeId>(x, y, z) ;
    }

    public Iterator<Tuple<NodeId>> find(NodeId s, NodeId p, NodeId o)
    {
        Tuple<NodeId> t = tuple3(s,p,o) ; 
        return index.find(t) ;
    }
 
//    public void copyInto(TripleIndex index2)
//    {
//        Iterator<Tuple<NodeId>> iter = all() ;
//        for ( int i = 0 ; iter.hasNext() ; i++ )
//        {
//            Tuple<NodeId> tuple = iter.next();
//            index2.add(tuple.get(0), tuple.get(1), tuple.get(2)) ;
//        } 
//    }
    
    @Override
    public String toString()            { return "TripleIndex: "+index.getDesc().getLabel() ; }  
    
    public void sync(boolean force)     { index.sync(force); }
    public void close()                 { index.close(); }
    
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