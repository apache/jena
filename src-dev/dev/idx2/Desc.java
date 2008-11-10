/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

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
        return NodeLib.record(factory, tuple, colMap) ;
    }
    
    public final Tuple<NodeId> tuple(Record e)
    {
        return NodeLib.tuple(e, colMap) ;
//        // In index native order
//        NodeId[] n = new NodeId[3] ; 
//        
//        n[0] = NodeLib.getNodeId(e, 0) ;
//        n[1] = NodeLib.getNodeId(e, SizeOfNodeId) ;
//        n[2] = NodeLib.getNodeId(e, 2*SizeOfNodeId) ;
//
//        NodeId sId = colMap.mapSlot(0, n) ;
//        NodeId pId = colMap.mapSlot(1, n) ;
//        NodeId oId = colMap.mapSlot(2, n) ;
//        
//        return new Tuple<NodeId>(sId, pId, oId) ; 
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