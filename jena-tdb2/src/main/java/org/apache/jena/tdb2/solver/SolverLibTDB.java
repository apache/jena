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

package org.apache.jena.tdb2.solver;

import static org.apache.jena.sparql.engine.main.solver.SolverLib.makeAbortable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.Abortable;
import org.apache.jena.sparql.engine.iterator.QueryIterAbortable;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.tdb2.lib.NodeLib;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities used within the TDB BGP solver : local TDB store */
public class SolverLibTDB
{
    private static Logger log = LoggerFactory.getLogger(SolverLibTDB.class);

    static Iterator<BindingNodeId> convertToIds(Iterator<Binding> iterBindings, NodeTable nodeTable)
    { return Iter.map(iterBindings, convFromBinding(nodeTable)); }

    /** Convert from Iterator<BindingNodeId> to Iterator<Binding>, conversion "on demand"
     * (in convToBinding(BindingNodeId, NodeTable)
     */
    static Iterator<Binding> convertToNodes(Iterator<BindingNodeId> iterBindingIds, NodeTable nodeTable)
    { return Iter.map(iterBindingIds, bindingNodeIds -> convToBinding(bindingNodeIds, nodeTable)); }

    static Binding convToBinding(BindingNodeId bindingNodeIds, NodeTable nodeTable) {
        if ( true )
            return new BindingTDB(bindingNodeIds, nodeTable);
        else {
            // Makes nodes immediately. Causing unnecessary NodeTable accesses
            // (e.g. project)
            BindingBuilder builder = Binding.builder();
            for (Var v : bindingNodeIds) {
                NodeId id = bindingNodeIds.get(v);
                Node n = nodeTable.getNodeForNodeId(id);
                builder.add(v, n);
            }
            return builder.build();
        }
    }

    // Transform : Binding ==> BindingNodeId
    static Iterator<BindingNodeId> convFromBinding(Iterator<Binding> input, NodeTable nodeTable) {
        return Iter.map(input, SolverLibTDB.convFromBinding(nodeTable));
    }

    static Function<Binding, BindingNodeId> convFromBinding(final NodeTable nodeTable) {
        return binding -> SolverLibTDB.convert(binding, nodeTable);
    }

    /** Binding {@literal ->} BindingNodeId, given a NodeTable */
    static BindingNodeId convert(Binding binding, NodeTable nodeTable) {
        if ( binding instanceof BindingTDB )
            return ((BindingTDB)binding).getBindingId();

        BindingNodeId b = new BindingNodeId(binding);
        // and copy over, getting NodeIds.
        Iterator<Var> vars = binding.vars();

        for ( ; vars.hasNext() ; ) {
            Var v = vars.next();
            Node n = binding.get(v);
            if ( n == null )
                // Variable mentioned in the binding but not actually defined.
                // Can occur with BindingProject
                continue;

            // Rely on the node table cache for efficency - we will likely be
            // repeatedly looking up the same node in different bindings.
            NodeId id = nodeTable.getNodeIdForNode(n);
            // Even put in "does not exist" for a node now known not to be in the DB.
            // Optional: whether to put in "known missing"
            // Currently, we do. The rest of the code should work with either choice.

            // if ( ! NodeId.isDoesNotExist(id) )
            b.put(v, id);
        }
        return b;
    }

    /** Find whether a specific graph name is in the quads table. */
    static QueryIterator testForGraphName(DatasetGraphTDB ds, Node graphNode, QueryIterator input,
                                                 Predicate<Tuple<NodeId>> filter, ExecutionContext execCxt) {
        NodeId nid = TDBInternal.getNodeId(ds, graphNode);
        boolean exists = !NodeId.isDoesNotExist(nid);
        if ( exists ) {
            // Node exists but is it used in the quad position?
            NodeTupleTable ntt = ds.getQuadTable().getNodeTupleTable();
            // Don't worry about abortable - this iterator should be fast
            // (with normal indexing - at least one G???).
            // Either it finds a starting point, or it doesn't.  We are only
            // interested in the first .hasNext.
            Iterator<Tuple<NodeId>> iter1 = ntt.find(nid, NodeId.NodeIdAny, NodeId.NodeIdAny, NodeId.NodeIdAny);
            if ( filter != null )
                iter1 = Iter.filter(iter1, filter);
            exists = iter1.hasNext();
        }

        if ( exists )
            return input;
        else {
            input.close();
            return QueryIterNullIterator.create(execCxt);
        }
    }

    private static Tuple<NodeId> TupleANY = TupleFactory.create4(NodeId.NodeIdAny, NodeId.NodeIdAny, NodeId.NodeIdAny, NodeId.NodeIdAny);

    private enum GraphNamesDistinctMode {
        NONE,
        ADJACENT,
        FULL;
    }

    /** Find all the graph names in the quads table. */
    static QueryIterator graphNames(DatasetGraphTDB ds, Node graphNode, QueryIterator input,
                                    Predicate<Tuple<NodeId>> filter, ExecutionContext execCxt) {
        List<Abortable> killList = new ArrayList<>();
        NodeTupleTable ntt = ds.getQuadTable().getNodeTupleTable();

        // Select a graph based index for use, if a suitable one is available
        // We select twice here because if there is an index of a specific type we can do additional optimisation, but
        // a graph based index allows for some optimisation regardless of the underlying index type
        TupleIndexRecord idxRecord = ntt.getTupleTable().selectIndex("G", TupleIndexRecord.class);
        TupleIndex idx = ntt.getTupleTable().selectIndex("G");

        GraphNamesDistinctMode distinctMode;
        Iterator<Tuple<NodeId>> iter1;

        if (idxRecord != null && idxRecord.getRangeIndex() instanceof BPlusTree) {
            // Have a B+Tree based graph index so can use our distinct by key prefix iterator to optimise evaluation
            // This iterator already guarantees the graph names returned are distinct so no further distinct calculation
            // is needed
            // Need to pad the yielded tuple with placeholders to create a tuple of the right length so the subsequent
            // filtering (if any) applies correctly
            BPlusTree bpt = (BPlusTree) (idxRecord).getRangeIndex();
            iter1 = Iter.iter(bpt.distinctByKeyPrefix(NodeId.SIZE))
                        .map(r -> TupleFactory.create4(NodeIdFactory.get(r.getKey(), 0), NodeId.NodeIdAny, NodeId.NodeIdAny, NodeId.NodeIdAny));
            distinctMode =  GraphNamesDistinctMode.NONE;
        } else if (idx != null) {
            // Have a generic Graph based index, can at least optimise slightly by only needing distinct adjacent as we
            // know the same Graph Names will be stored in a sequence within the index. This also keeps memory footprint
            // for the distinct constant to a single Graph Name
            iter1 = idx.find(TupleANY);
            distinctMode = GraphNamesDistinctMode.ADJACENT;
        } else {
            // Fall back to a find all which will use the most appropriate index available
            // Need full distinct calculation as no guarantee that the index is ordered with respect to the graph name
            // Memory usage will scale with number of distinct graph names though this is likely to be relatively
            // modest so generally not a problem
            iter1 = ntt.findAll();
            distinctMode = GraphNamesDistinctMode.FULL;
        }

        if ( filter != null )
            iter1 = Iter.filter(iter1, filter);

        Iterator<NodeId> iter2 = Iter.map(iter1, t -> t.get(0));
        // Project is cheap - don't brother wrapping iter1
        iter2 = makeAbortable(iter2, killList);

        // Apply the necessary distinct calculation (if any)
        Iterator<NodeId> iter3;
        switch (distinctMode) {
            case FULL:
                iter3 = Iter.distinct(iter2);
                break;
            case ADJACENT:
                iter3 = Iter.distinctAdjacent(iter2);
                break;
            case NONE:
            default:
                iter3 = iter2;
                break;
        }
        iter3 = makeAbortable(iter3, killList);

        Iterator<Node> iter4 = NodeLib.nodes(ds.getQuadTable().getNodeTupleTable().getNodeTable(), iter3);

        final Var var = Var.alloc(graphNode);
        Iterator<Binding> iterBinding = Iter.map(iter4, node -> BindingFactory.binding(var, node));
        return new QueryIterAbortable(iterBinding, killList, input, execCxt);
    }

    static Set<NodeId> convertToNodeIds(Collection<Node> nodes, DatasetGraphTDB dataset)
    {
        Set<NodeId> graphIds = new HashSet<>();
        NodeTable nt = dataset.getQuadTable().getNodeTupleTable().getNodeTable();
        for ( Node n : nodes )
            graphIds.add(nt.getNodeIdForNode(n));
        return graphIds;
    }

    public static Iterator<Tuple<NodeId>> unionGraph(NodeTupleTable ntt)
    {
        Iterator<Tuple<NodeId>> iter = ntt.find((NodeId)null, null, null, null);
        iter = Iter.map(iter, quadsToAnyTriples);
        //iterMatches = Iter.distinct(iterMatches);

        // This depends on the way indexes are choose and
        // the indexing pattern. It assumes that the index
        // chosen ends in G so same triples are adjacent
        // in a union query.
        /// See TupleTable.scanAllIndex that ensures this.
        iter = Iter.distinctAdjacent(iter);
        return iter;
    }

    private static Function<Tuple<NodeId>, Tuple<NodeId>> quadsToAnyTriples = item -> {
        return TupleFactory.create4(NodeId.NodeIdAny, item.get(1), item.get(2), item.get(3) );
    };
}
