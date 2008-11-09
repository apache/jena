/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;
import lib.Tuple;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

public class Desc
{
    private final ColumnMap colMap ;
    private final RecordFactory factory ;

    public Desc(ColumnMap colMap, RecordFactory factory)
    {
        this.colMap = colMap ;
        this.factory = factory ;
    }
    
    public final Record record(Tuple<NodeId> tuple)
    {
        NodeId id1 = tuple.get(colMap.unmapOrder(0)) ;  // NAMING!!!
        NodeId id2 = tuple.get(colMap.unmapOrder(1)) ;
        NodeId id3 = tuple.get(colMap.unmapOrder(2)) ;
        // Convert to [] form.
        return NodeLib.record(factory, id1, id2, id3) ;
    }
    
    public final Tuple<NodeId> tuple(Record e)
    {
        // In index native order
        long x = NodeLib.getId(e, 0) ;
        long y = NodeLib.getId(e, SizeOfNodeId) ;
        long z = NodeLib.getId(e, 2*SizeOfNodeId) ;
        // In SPO order
        return tuple(NodeId.create(x), 
                     NodeId.create(y), 
                     NodeId.create(z)) ;
    }
    
    // To SPO order
    public final Tuple<NodeId> tuple(NodeId x, NodeId y, NodeId z)
    {
        NodeId sId = extract(0, x,y,z) ;
        NodeId pId = extract(1, x,y,z) ;
        NodeId oId = extract(2, x,y,z) ;
        return new Tuple<NodeId>(sId, pId, oId) ; 
    }

    NodeId extract(int i, NodeId...array)
    {
        return array[colMap.mapOrder(i)] ;  /****/
    }

    public String getLabel()
    {
        return colMap.getLabel() ;
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