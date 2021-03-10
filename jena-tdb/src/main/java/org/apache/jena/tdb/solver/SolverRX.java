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

package org.apache.jena.tdb.solver;

import static org.apache.jena.tdb.solver.SolverLib.convFromBinding;
import static org.apache.jena.tdb.solver.SolverLib.tripleHasNodeTriple;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.RX;
import org.apache.jena.tdb.lib.TupleLib;
import org.apache.jena.tdb.store.NodeId;
import org.apache.jena.tdb.store.nodetable.NodeTable;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable;

public class SolverRX {
    /**
     * This constant is not public API. It is exposed only so integration testing can
     * check the value for a release build.
     */
    public static final boolean DATAPATH = true;

    // Entry point from SolverLib.
    /*package*/
    static Iterator<BindingNodeId> matchQuadPattern(Iterator<BindingNodeId> chain, Node graphNode, Triple tPattern,
                                                    NodeTupleTable nodeTupleTable, Tuple<Node> patternTuple,
                                                    boolean anyGraph, Predicate<Tuple<NodeId>> filter, ExecutionContext execCxt) {
        if ( DATAPATH ) {
            if ( ! tripleHasNodeTriple(tPattern) || tPattern.isConcrete() ) {
                // No RDF-star <<>> with variables.
                return StageMatchTuple.access(nodeTupleTable, chain, patternTuple, filter, anyGraph, execCxt);
            }
        }

        // RDF-star <<>> with variables.
        // This path should work regardless.

        boolean isTriple = (patternTuple.len() == 3);
        NodeTable nodeTable = nodeTupleTable.getNodeTable();

        Function<BindingNodeId, Iterator<BindingNodeId>> step =
                bnid -> find(bnid, nodeTupleTable, graphNode, tPattern, anyGraph, filter, execCxt);
        return Iter.flatMap(chain, step);
    }

    private static Iterator<BindingNodeId> find(BindingNodeId bnid, NodeTupleTable nodeTupleTable,
                                                Node graphNode, Triple tPattern,
                                                boolean anyGraph, Predicate<Tuple<NodeId>> filter,
                                                ExecutionContext execCxt) {
        Node tGraphNode = anyGraph ? Quad.unionGraph : graphNode ;
        // graphNode is ANY for union graph and null for default graph.
        // Var to ANY, Triple Term to ANY.

        Node g = ( graphNode == null ) ? null : RX.nodeTopLevel(graphNode);
        Node s = RX.nodeTopLevel(tPattern.getSubject());
        Node p = RX.nodeTopLevel(tPattern.getPredicate());
        Node o = RX.nodeTopLevel(tPattern.getObject());
        NodeTable nodeTable = nodeTupleTable.getNodeTable();
        Tuple<Node> patternTuple = ( g == null )
                ? TupleFactory.create3(s,p,o)
                : TupleFactory.create4(g,s,p,o);

        Iterator<Quad> dsgIter = accessData(patternTuple, nodeTupleTable, anyGraph, filter, execCxt);

        Binding input = bnid.isEmpty() ? BindingFactory.empty() : new BindingTDB(bnid, nodeTable);
        Iterator<Binding> matched = Iter.iter(dsgIter).map(dQuad->RX.matchQuad(input, dQuad, tGraphNode, tPattern)).removeNulls();
        return convFromBinding(matched, nodeTable);
    }

    static Iterator<Quad> accessData(Tuple<Node> patternTuple, NodeTupleTable nodeTupleTable,
                                     boolean anyGraph, Predicate<Tuple<NodeId>> filter,
                                     ExecutionContext execCxt) {
        NodeTable nodeTable = nodeTupleTable.getNodeTable();
        Function<Tuple<NodeId>, Quad> asQuad = asQuad(nodeTable, nodeTupleTable.getTupleLen(), anyGraph);
        Tuple<NodeId> patternTupleId = TupleLib.tupleNodeIds(nodeTable, patternTuple);
        if ( patternTupleId.contains(NodeId.NodeDoesNotExist) )
            // Can not match.
            return Iter.nullIterator();

        // -- DRY/StageMatchTuple ??
        Iterator<Tuple<NodeId>> iterMatches = nodeTupleTable.find(patternTupleId);
        // Add filter
        if ( filter != null )
            iterMatches = Iter.filter(iterMatches, filter);
        // Add anyGraph
        if ( anyGraph ) {
            // See StageMatchTuple for discussion.
            iterMatches = Iter.map(iterMatches, quadsToAnyTriples);
            iterMatches = Iter.distinctAdjacent(iterMatches);
        }
        // -- DRY/StageMatchTuple
        //Iterator<Quad> qIter = TupleLib.convertToQuads(nodeTable, iterMatches) ;
        Iterator<Quad> qIter = Iter.map(iterMatches, asQuad);
        return qIter;
    }

    private static  Function<Tuple<NodeId>, Quad> asQuad(NodeTable nodeTable, int tupleLen, boolean anyGraph) {
        switch (tupleLen) {
            case 3:
                return (Tuple<NodeId> t) -> {
                    Node gx = Quad.defaultGraphIRI;
                    Node sx = toNode(t.get(0), nodeTable);
                    Node px = toNode(t.get(1), nodeTable);
                    Node ox = toNode(t.get(2), nodeTable);
                    return Quad.create(gx, sx, px, ox);
                };
            case 4:
                return (Tuple<NodeId> t) -> {
                    Node gx = (anyGraph)? Quad.unionGraph : toNode(t.get(0), nodeTable);
                    Node sx = toNode(t.get(1), nodeTable);
                    Node px = toNode(t.get(2), nodeTable);
                    Node ox = toNode(t.get(3), nodeTable);
                    return Quad.create(gx, sx, px, ox);
                };
            default:
                throw new InternalErrorException("Tuple of unknown length");
        }
    }

    private static Node toNode(NodeId nodeId, NodeTable nodeTable) {
        return nodeTable.getNodeForNodeId(nodeId);
    }

    private static Function<Tuple<NodeId>, Tuple<NodeId>> quadsToAnyTriples = item -> {
        return TupleFactory.create4(NodeId.NodeIdAny, item.get(1), item.get(2), item.get(3));
    };
}
