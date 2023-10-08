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

package org.apache.jena.tdb1.lib;

import static org.apache.jena.tdb1.sys.SystemTDB.SizeOfLong;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb1.TDB1Exception;
import org.apache.jena.tdb1.base.record.Record;
import org.apache.jena.tdb1.base.record.RecordFactory;
import org.apache.jena.tdb1.store.NodeId;
import org.apache.jena.tdb1.store.nodetable.NodeTable;

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
        return ids.map(nid -> nodeTable.getNodeForNodeId(nid));
    }

    public static Tuple<NodeId> tupleNodeIds(NodeTable nodeTable, Tuple<Node> nodes) {
        return nodes.map(n -> nodeTable.getNodeIdForNode(n));
    }

    private static Triple triple(NodeTable nodeTable, Tuple<NodeId> tuple) {
        if ( tuple.len() != 3 )
            throw new TDB1Exception("Tuple is not of length 3: " + tuple);
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

        return Triple.create(sNode, pNode, oNode);
    }

    private static String fmt(NodeId s, NodeId p, NodeId o) {
        return "(" + s + ", " + p + ", " + o + ")";
    }

    private static String fmt(NodeId g, NodeId s, NodeId p, NodeId o) {
        return "(" + g + "," + s + ", " + p + ", " + o + ")";
    }

    private static Quad quad(NodeTable nodeTable, Tuple<NodeId> tuple) {
        if ( tuple.len() != 4 )
            throw new TDB1Exception("Tuple is not of length 4: " + tuple);
        return quad(nodeTable, tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3));
    }

    private static Quad quad(NodeTable nodeTable, NodeId g, NodeId s, NodeId p, NodeId o) {
        Node gNode = nodeTable.getNodeForNodeId(g);
        Node sNode = nodeTable.getNodeForNodeId(s);
        Node pNode = nodeTable.getNodeForNodeId(p);
        Node oNode = nodeTable.getNodeForNodeId(o);
        if ( gNode == null )
            throw new InternalErrorException("Invalid id node for graph (null node): " + fmt(g, s, p, o));
        if ( sNode == null )
            throw new InternalErrorException("Invalid id node for subject (null node): " + fmt(g, s, p, o));
        if ( pNode == null )
            throw new InternalErrorException("Invalid id node for predicate (null node): " + fmt(g, s, p, o));
        if ( oNode == null )
            throw new InternalErrorException("Invalid id node for object (null node): " + fmt(g, s, p, o));
        return Quad.create(gNode, sNode, pNode, oNode);
    }

    // ---- Tuples and Records
    public static Tuple<NodeId> tuple(Record r, ColumnMap cMap) {
        int N = r.getKey().length / SizeOfLong;
        NodeId[] nodeIds = new NodeId[N];
        for ( int i = 0 ; i < N ; i++ ) {
            NodeId id = NodeId.create(r.getKey(), i * SizeOfLong);
            int j = i;
            if ( cMap != null )
                j = cMap.fetchSlotIdx(i);
            nodeIds[j] = id;
        }
        return TupleFactory.asTuple(nodeIds);
    }

    public static Record record(RecordFactory factory, Tuple<NodeId> tuple, ColumnMap cMap) {
        byte[] b = new byte[tuple.len() * NodeId.SIZE];
        for ( int i = 0 ; i < tuple.len() ; i++ ) {
            int j = cMap.mapSlotIdx(i);
            // i'th Nodeid goes to j'th bytes slot.
            Bytes.setLong(tuple.get(i).getId(), b, j * SizeOfLong);
        }

        return factory.create(b);
    }
}
