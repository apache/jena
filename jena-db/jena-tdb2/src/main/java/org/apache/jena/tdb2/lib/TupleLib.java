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

package org.apache.jena.tdb2.lib;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

public class TupleLib {
    public static Iterator<Tuple<Node>> convertToNodes(final NodeTable nodeTable, Iterator<Tuple<NodeId>> iter) {
        return Iter.map(iter, item -> tupleNodes(nodeTable, item));
    }

    public static Iterator<Tuple<NodeId>> convertToNodeId(final NodeTable nodeTable, Iterator<Tuple<Node>> iter) {
        return Iter.map(iter, item -> tupleNodeIds(nodeTable, item));
    }

    // Leave - bypasses extract step in Tuple<NodeId> -> Tuple<Node> -> Triple
    public static Iterator<Triple> convertToTriples(final NodeTable nodeTable, Iterator<Tuple<NodeId>> iter) {
        return Iter.map(iter, item -> triple(nodeTable, item));
    }

    public static Iterator<Quad> convertToQuads(final NodeTable nodeTable, Iterator<Tuple<NodeId>> iter) {
        return Iter.map(iter, item -> quad(nodeTable, item));
    }

    public static Tuple<Node> tupleNodes(NodeTable nodeTable, Tuple<NodeId> ids) {
        int N = ids.len();
        Node[] n = new Node[N];
        for ( int i = 0 ; i < N ; i++ )
            n[i] = nodeTable.getNodeForNodeId(ids.get(i));
        return TupleFactory.create(n);
    }

    public static Tuple<NodeId> tupleNodeIds(NodeTable nodeTable, Tuple<Node> nodes) {
        int N = nodes.len();
        NodeId[] n = new NodeId[N];
        for ( int i = 0 ; i < N ; i++ )
            n[i] = nodeTable.getNodeIdForNode(nodes.get(i));
        return TupleFactory.create(n);
    }

    private static Triple triple(NodeTable nodeTable, Tuple<NodeId> tuple) {
        if ( tuple.len() != 3 )
            throw new TDBException("Tuple is not of length 3: " + tuple);
        return triple(nodeTable, tuple.get(0), tuple.get(1), tuple.get(2));
    }

    private static Triple triple(NodeTable nodeTable, NodeId s, NodeId p, NodeId o) {
        if ( !NodeId.isConcrete(s) )
            throw new InternalErrorException("Invalid id for subject: " + fmt(s, p, o));
        if ( !NodeId.isConcrete(p) )
            throw new InternalErrorException("Invalid id for predicate: " + fmt(s, p, o));
        if ( !NodeId.isConcrete(o) )
            throw new InternalErrorException("Invalid id for object: " + fmt(s, p, o));

        Node sNode = nodeTable.getNodeForNodeId(s);
        if ( sNode == null )
            throw new InternalErrorException("Invalid id node for subject (null node): " + fmt(s, p, o));

        Node pNode = nodeTable.getNodeForNodeId(p);
        if ( pNode == null )
            throw new InternalErrorException("Invalid id node for predicate (null node): " + fmt(s, p, o));

        Node oNode = nodeTable.getNodeForNodeId(o);
        if ( oNode == null )
            throw new InternalErrorException("Invalid id node for object (null node): " + fmt(s, p, o));

        return new Triple(sNode, pNode, oNode);
    }

    private static String fmt(NodeId s, NodeId p, NodeId o) {
        return "(" + s + ", " + p + ", " + o + ")";
    }

    private static Quad quad(NodeTable nodeTable, Tuple<NodeId> tuple) {
        if ( tuple.len() != 4 )
            throw new TDBException("Tuple is not of length 4: " + tuple);
        return quad(nodeTable, tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3));
    }

    private static Quad quad(NodeTable nodeTable, NodeId g, NodeId s, NodeId p, NodeId o) {
        Node gNode = nodeTable.getNodeForNodeId(g);
        Node sNode = nodeTable.getNodeForNodeId(s);
        Node pNode = nodeTable.getNodeForNodeId(p);
        Node oNode = nodeTable.getNodeForNodeId(o);
        return new Quad(gNode, sNode, pNode, oNode);
    }

    // ---- Tuples and Records
    public static Tuple<NodeId> tuple(Record r, TupleMap tMap) {
        // Unmapping.
        int N = r.getKey().length / NodeId.SIZE;
        NodeId[] nodeIds = new NodeId[N];
        for ( int i = 0 ; i < N ; i++ ) {
            int j = i;
            if ( tMap != null )
                j = tMap.unmapIdx(i);
            NodeId id = NodeIdFactory.get(r.getKey(), j * NodeId.SIZE);
            nodeIds[i] = id;
        }
        return TupleFactory.create(nodeIds);
    }

    public static Record record(RecordFactory factory, Tuple<NodeId> tuple, TupleMap tMap) {
        // Mapping.
        byte[] b = new byte[tuple.len() * NodeId.SIZE];
        for ( int i = 0 ; i < tuple.len() ; i++ ) {
            int j = tMap.getSlotIdx(i);
            // i'th Nodeid goes to j'th bytes slot.
            NodeIdFactory.set(tuple.get(j), b, i * NodeId.SIZE);
        }
        return factory.create(b);
    }
}
