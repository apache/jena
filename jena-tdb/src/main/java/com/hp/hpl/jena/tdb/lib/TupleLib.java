/*
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

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Tuple ;




import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.store.NodeId;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

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
        if ( ! NodeId.isConcrete(s) )
            throw new InternalErrorException("Invalid id for subject: "+fmt(s,p,o)) ;
        if ( ! NodeId.isConcrete(p) )
            throw new InternalErrorException("Invalid id for predicate: "+fmt(s,p,o)) ;
        if ( ! NodeId.isConcrete(o) )
            throw new InternalErrorException("Invalid id for object: "+fmt(s,p,o)) ;
        
        Node sNode = nodeTable.getNodeForNodeId(s) ;
        if ( sNode == null )
            throw new InternalErrorException("Invalid id node for subject (null node): "+fmt(s,p,o)) ;

        Node pNode = nodeTable.getNodeForNodeId(p) ;
        if ( pNode == null )
        {
            nodeTable.getNodeForNodeId(p) ;
            throw new InternalErrorException("Invalid id node for predicate (null node): "+fmt(s,p,o)) ;
        }
        
        Node oNode = nodeTable.getNodeForNodeId(o) ;
        if ( oNode == null )
            throw new InternalErrorException("Invalid id node for object (null node): "+fmt(s,p,o)) ;
        
        return new Triple(sNode, pNode, oNode) ;
    }
    
    private static String fmt(NodeId s, NodeId p, NodeId o)
    {
        return "("+s+", "+p+", "+o+")" ;
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

    // ---- Tuples and Records
    public static Tuple<NodeId> tuple(Record r, ColumnMap cMap)
    {
        int N = r.getKey().length/SizeOfLong ;
        NodeId[] nodeIds = new NodeId[N] ;
        for ( int i = 0 ; i < N ; i++ )
        {
            NodeId id = NodeId.create(r.getKey(), i*SizeOfLong) ;
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
