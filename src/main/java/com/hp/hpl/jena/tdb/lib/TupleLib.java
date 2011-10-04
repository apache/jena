/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.tdb.lib;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfLong;

import java.util.Iterator;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Tuple ;



import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.core.Quad;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.store.NodeId;

public class TupleLib
{
    public static  Iterator<Tuple<Node>> convertToNodes(final NodeTable nodeTable, Iterator<Tuple<NodeId>> iter)
    {
        Transform<Tuple<NodeId>, Tuple<Node>> action =  new Transform<Tuple<NodeId>, Tuple<Node>>(){
            @Override
            public Tuple<Node> convert(Tuple<NodeId> item)
            {
                return tupleNodes(nodeTable, item) ;
            }} ;
        return Iter.map(iter, action) ;
    }
    
    public static Iterator<Tuple<NodeId>> convertToNodeId(final NodeTable nodeTable, Iterator<Tuple<Node>> iter)
    {
        Transform<Tuple<Node>, Tuple<NodeId>> action =  new Transform<Tuple<Node>, Tuple<NodeId>>(){
            @Override
            public Tuple<NodeId> convert(Tuple<Node> item)
            {
                return tupleNodeIds(nodeTable, item) ;
            }} ;
        return Iter.map(iter, action) ;
    }
    
    //@Deprecated
    //Leave - bypasses extract step in Tuple<NodeId> -> Tuple<Node> -> Triple
    public static Iterator<Triple> convertToTriples(final NodeTable nodeTable, Iterator<Tuple<NodeId>> iter)
    {
        Transform<Tuple<NodeId>, Triple> action =  new Transform<Tuple<NodeId>, Triple>(){
            @Override
            public Triple convert(Tuple<NodeId> item)
            {
                return triple(nodeTable, item) ;
            }} ;
        return Iter.map(iter, action) ;
    }
    
    //@Deprecated
    public static Iterator<Quad> convertToQuads(final NodeTable nodeTable, Iterator<Tuple<NodeId>> iter)
    {
        Transform<Tuple<NodeId>, Quad> action =  new Transform<Tuple<NodeId>, Quad>(){
            @Override
            public Quad convert(Tuple<NodeId> item)
            {
                return quad(nodeTable, item) ;
            }} ;
        return Iter.map(iter, action) ;
    }
    
    public static Tuple<Node> tupleNodes(NodeTable nodeTable, Tuple<NodeId> ids) 
    {
        int N = ids.size() ;
        Node[] n = new Node[N] ;
        for ( int i = 0 ; i < N ; i++ )
            n[i] = nodeTable.getNodeForNodeId(ids.get(i)) ;
        return Tuple.create(n) ;
    }
    
    public static Tuple<NodeId> tupleNodeIds(NodeTable nodeTable, Tuple<Node> nodes) 
    {
        int N = nodes.size() ;
        NodeId[] n = new NodeId[N] ;
        for ( int i = 0 ; i < N ; i++ )
            n[i] = nodeTable.getNodeIdForNode(nodes.get(i)) ;
        return Tuple.create(n) ;
    }
    
    private static Triple triple(NodeTable nodeTable, Tuple<NodeId> tuple) 
    {
        if ( tuple.size() != 3 )
            throw new TDBException("Tuple is not of length 3: "+tuple) ;
        return triple(nodeTable, tuple.get(0), tuple.get(1), tuple.get(2)) ;
    }

    private static Triple triple(NodeTable nodeTable, NodeId s, NodeId p, NodeId o) 
    {
        Node sNode = nodeTable.getNodeForNodeId(s) ;
        Node pNode = nodeTable.getNodeForNodeId(p) ;
        Node oNode = nodeTable.getNodeForNodeId(o) ;
        return new Triple(sNode, pNode, oNode) ;
    }
    
    private static Quad quad(NodeTable nodeTable, Tuple<NodeId> tuple) 
    {
        if ( tuple.size() != 4 )
            throw new TDBException("Tuple is not of length 4: "+tuple) ;
        return quad(nodeTable, tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3)) ;
    }
    
    private static Quad quad(NodeTable nodeTable, NodeId g, NodeId s, NodeId p, NodeId o) 
    {
        Node gNode = nodeTable.getNodeForNodeId(g) ;
        Node sNode = nodeTable.getNodeForNodeId(s) ;
        Node pNode = nodeTable.getNodeForNodeId(p) ;
        Node oNode = nodeTable.getNodeForNodeId(o) ;
        return new Quad(gNode, sNode, pNode, oNode) ;
    }

    // ---- Tuples, Triples and Quads

//    /** Triple to Tuple, not remapped by a ColumnMap. */
//    public static Tuple<NodeId> tuple(Triple t, NodeTable nodeTable)
//    {
//        Node s = t.getSubject() ;
//        Node p = t.getPredicate() ;
//        Node o = t.getObject() ;
//
//        NodeId x = nodeTable.storeNode(s) ;
//        NodeId y = nodeTable.storeNode(p) ;
//        NodeId z = nodeTable.storeNode(o) ;
//        return Tuple.create(x, y, z) ;  
//    }
//
//    /** Quad to Tuple, not remapped by a ColumnMap. */
//    public static Tuple<NodeId> tuple(Quad quad, NodeTable nodeTable)
//    {
//        return tuple(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject(), nodeTable) ;
//    }
//    
//    /** Quad (as graph node and triple) to Tuple, not remapped by a ColumnMap. */
//    public static Tuple<NodeId> tuple(Node g, Triple t, NodeTable nodeTable)
//    {
//        return tuple(g, t.getSubject(), t.getPredicate(), t.getObject(), nodeTable) ;
//    }
//    
//    public static Tuple<NodeId> tuple(Node g, Node s, Node p, Node o, NodeTable nodeTable)
//    {
//        NodeId gId = nodeTable.storeNode(g) ;
//        NodeId sId = nodeTable.storeNode(s) ;
//        NodeId pId = nodeTable.storeNode(p) ;
//        NodeId oId = nodeTable.storeNode(o) ;
//        
//        return Tuple.create(gId, sId, pId, oId) ;  
//    }
    
    // ---- Tuples and Records
    public static Tuple<NodeId> tuple(Record r, ColumnMap cMap)
    {
        int N = r.getKey().length/SizeOfLong ;
        NodeId[] nodeIds = new NodeId[N] ;
        for ( int i = 0 ; i < N ; i++ )
        {
            long x = Bytes.getLong(r.getKey(), i*SizeOfLong) ;
            NodeId id = NodeId.create(x) ;
            int j = i ;
            if ( cMap != null )
                j = cMap.fetchSlotIdx(i) ;
            nodeIds[j] = id ;
        }
        return Tuple.create(nodeIds) ;
    }


    public static Record record(RecordFactory factory, Tuple<NodeId> tuple, ColumnMap cMap) 
    {
        byte[] b = new byte[tuple.size()*NodeId.SIZE] ;
        for ( int i = 0 ; i < tuple.size() ; i++ )
        {
            int j = cMap.mapSlotIdx(i) ;
            // i'th Nodeid goes to j'th bytes slot.
            Bytes.setLong(tuple.get(i).getId(), b,j*SizeOfLong) ;
        }
            
        return factory.create(b) ;
    }



}
