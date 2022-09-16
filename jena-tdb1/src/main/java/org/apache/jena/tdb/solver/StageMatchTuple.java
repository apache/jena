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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.tdb.store.NodeId;
import org.apache.jena.tdb.store.nodetable.NodeTable;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable;

class StageMatchTuple {

    /* Entry point */
    static Iterator<BindingNodeId> access(NodeTupleTable nodeTupleTable, Iterator<BindingNodeId> input, Tuple<Node> patternTuple,
                                                 Predicate<Tuple<NodeId>> filter, boolean anyGraph, ExecutionContext execCxt) {
        return Iter.flatMap(input, bnid -> {
            return StageMatchTuple.access(nodeTupleTable, bnid, patternTuple, filter, anyGraph, execCxt);
        });
    }

    private static Iterator<BindingNodeId> access(NodeTupleTable nodeTupleTable, BindingNodeId input, Tuple<Node> patternTuple,
                                                  Predicate<Tuple<NodeId>> filter, boolean anyGraph, ExecutionContext execCxt) {
        // ---- Convert to NodeIds
        NodeId ids[] = new NodeId[patternTuple.len()];
        // Variables for this tuple after substitution
        final Var[] vars = new Var[patternTuple.len()];

        boolean b = prepare(nodeTupleTable.getNodeTable(), patternTuple, input, ids, vars);
        if ( !b )
            // Short cut - known unknown NodeId
            return Iter.nullIterator();

        Iterator<Tuple<NodeId>> iterMatches = nodeTupleTable.find(TupleFactory.create(ids));
        if ( false ) {
            List<Tuple<NodeId>> x = Iter.toList(iterMatches);
            System.out.println(x);
            iterMatches = x.iterator();
        }

        // ** Allow a triple or quad filter here.
        if ( filter != null )
            iterMatches = Iter.filter(iterMatches, filter);

        // If we want to reduce to RDF semantics over quads,
        // we need to reduce the quads to unique triples.
        // We do that by having the graph slot as "any", then running
        // through a distinct-ifier.
        // Assumes quads are GSPO in the matching tuple - zaps the first slot.
        if ( anyGraph ) {
            iterMatches = Iter.map(iterMatches, quadsToAnyTriples);
            // Guaranteed
            // iterMatches = Iter.distinct(iterMatches);

            // This depends on the way indexes are chosen and
            // the indexing pattern. It assumes that the index
            // chosen ends in G so same triples are adjacent
            // in a union query.
            //
            // If any slot is defined, then the index will be X??G.
            // If no slot is defined, then the index will be ???G.
            // But the TupleTable
            // See TupleTable.scanAllIndex that ensures the latter.
            // No G part way through.
            iterMatches = Iter.distinctAdjacent(iterMatches);
        }

        Function<Tuple<NodeId>, BindingNodeId> binder = tuple -> tupleToBinding(input, tuple, vars);
        return Iter.iter(iterMatches).map(binder).removeNulls();
    }

    private static BindingNodeId tupleToBinding(BindingNodeId input, Tuple<NodeId> tuple, Var[] var) {
        // Reuseable BindingNodeId builder?
        BindingNodeId output = new BindingNodeId(input);
        for ( int i = 0 ; i < var.length ; i++ ) {
            Var v = var[i];
            if ( v == null )
                continue;
            NodeId id = tuple.get(i);
            if ( ! compatible(output, v, id) )
                return null;
            output.put(v, id);
        }
        return output;
    }

    /**
     * Prepare a pattern (tuple of nodes), and an existing binding of NodeId, into
     * NodeIds and Variables. A variable in the pattern is replaced by its binding or
     * null in the NodeIds. A variable that is not bound by the binding is placed in
     * the var array. Return false if preparation detects the pattern can not match.
     */
    private static boolean prepare(NodeTable nodeTable, Tuple<Node> patternTuple, BindingNodeId input, NodeId ids[], Var[] var) {
        // Process the Node to NodeId conversion ourselves because
        // we wish to abort if an unknown node is seen.
        for ( int i = 0 ; i < patternTuple.len() ; i++ ) {
            Node n = patternTuple.get(i);
            // Substitution and turning into NodeIds
            // Variables unsubstituted are null NodeIds
            NodeId nId = idFor(nodeTable, input, n);
            if ( NodeId.isDoesNotExist(nId) )
                return false;
            ids[i] = nId;
            if ( nId == null )
                var[i] = asVar(n);
        }
        return true;
    }

    private static Iterator<Tuple<NodeId>> print(Iterator<Tuple<NodeId>> iter) {
        if ( !iter.hasNext() )
            System.err.println("<empty>");
        else {
            List<Tuple<NodeId>> r = Iter.toList(iter);
            String str = StrUtils.strjoin(r, "\n");
            System.err.println(str);
            // Reset iter
            iter = Iter.iter(r);
        }
        return iter;
    }

    private static boolean compatible(BindingNodeId output, Var var, NodeId value) {
        if ( !output.containsKey(var) )
            return true;
        // sameTermAs for language tags?
        if ( output.get(var).equals(value) )
            return true;
        return false;
    }

    private static Var asVar(Node node) {
        if ( Var.isVar(node) )
            return Var.alloc(node);
        return null;
    }

    /** Return null for variables, and for nodes, the node id or NodeDoesNotExist */
    private static NodeId idFor(NodeTable nodeTable, BindingNodeId input, Node node) {
        if ( Var.isVar(node) ) {
            NodeId n = input.get((Var.alloc(node)));
            // Bound to NodeId or null.
            return n;
        }
        // May return NodeId.NodeDoesNotExist which must not be null.
        return nodeTable.getNodeIdForNode(node);
    }

    private static Function<Tuple<NodeId>, Tuple<NodeId>> quadsToAnyTriples = item -> {
        return TupleFactory.create4(NodeId.NodeIdAny, item.get(1), item.get(2), item.get(3));
    };
}
