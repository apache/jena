/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import static com.hp.hpl.jena.tdb.Const.SizeOfNodeId;
import iterator.Iter;
import iterator.Transform;

import java.util.Iterator;

import lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.NodeTable;

public class TupleLib
{
    public static Iterator<Tuple<NodeId>> tuples(Iterator<Record> iter)
    {
        Transform<Record, Tuple<NodeId>> transformToSPO = new Transform<Record, Tuple<NodeId>>() {
            @Override
            public Tuple<NodeId> convert(Record item)
            {
                return TupleLib.tuple(item) ;
            }} ; 
        return Iter.map(iter, transformToSPO) ;
    }
    
    
    public static Tuple<NodeId> tuple(Record e)
    {
        // In index native order
        long x = NodeLib.getId(e, 0) ;
        long y = NodeLib.getId(e, SizeOfNodeId) ;
        long z = NodeLib.getId(e, 2*SizeOfNodeId) ;
        return new Tuple<NodeId>(NodeId.create(x), 
                                 NodeId.create(y), 
                                 NodeId.create(z)) ;
    }

    
    public static  Iterator<Triple> convertToTriples(final NodeTable nodeTable, Iterator<Tuple<NodeId>> iter)
    {
        Transform<Tuple<NodeId>, Triple> action =  new Transform<Tuple<NodeId>, Triple>(){

            @Override
            public Triple convert(Tuple<NodeId> item)
            {
                return triple(nodeTable, item) ;
            }} ;
        return Iter.map(iter, action) ;
    }
    
    public static Triple triple(NodeTable nodeTable, NodeId s, NodeId p, NodeId o) 
    {
        Node sNode = nodeTable.retrieveNode(s) ;
        Node pNode = nodeTable.retrieveNode(p) ;
        Node oNode = nodeTable.retrieveNode(o) ;
        return new Triple(sNode, pNode, oNode) ;
    }
    
    public static Triple triple(NodeTable nodeTable, Tuple<NodeId> tuple) 
    {
        return triple(nodeTable, tuple.get(0), tuple.get(1), tuple.get(2)) ;
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