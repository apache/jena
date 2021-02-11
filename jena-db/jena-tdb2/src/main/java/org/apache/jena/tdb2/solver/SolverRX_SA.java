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

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.RX_SA;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;

public class SolverRX_SA {

    /**
     * Match a data node against a pattern node, which can include variables and
     * triple terms. Return null for no match.
     */
    public static Binding match(Binding input, Node nData, Node nPattern) {
//        // Deep substitute. This happens anyway as we walk structures.
//        nPattern = Substitute.substitute(nPattern, input);

        nPattern = Var.lookup(input, nPattern);

        // Easy case - nPattern is a variable.
        if ( Var.isVar(nPattern) ) {
            Var var = Var.alloc(nPattern);
            Binding binding = BindingFactory.binding(input, var, nData);
            return binding;
        }

        // nPattern.isConcrete() : either nPattern is an RDF term or is <<>> with no variables.
        if ( nPattern.isConcrete() ) {
            // No nested variables. Is data equal to pattern?
            if ( nPattern.equals(nData) )
                // Match, no additional bindings.
                return input;
            else
                // No match
                return null;
        }

        // nPattern is <<>> with variables.
        if ( ! nData.isNodeTriple() )
            return null;

        // nData is <<>>, nPattern is <<>>
        // Unpack, match components.
        Triple tPattern = Node_Triple.triple(nPattern);
        Node sPattern = tPattern.getSubject();
        Node pPattern = tPattern.getPredicate();
        Node oPattern = tPattern.getObject();

        Triple tData = Node_Triple.triple(nData);
        Node sData = tData.getSubject();
        Node pData = tData.getPredicate();
        Node oData = tData.getObject();

        Binding chain = input;
        chain = match(chain, sData, sPattern);
        if ( chain == null )
            return null;
        chain = match(chain, pData, pPattern);
        if ( chain == null )
            return null;
        chain = match(chain, oData, oPattern);
//        if ( chain == null )
//            return null;
        return chain;
    }

    // -------- TDB2

    // << stored as three NodeIds >> is easier - stay in NodeId space for <<?var>>.
    // Solve in Node space (constants don't need to come from NodeTable!
    // and convert bindings.

    // Converts each triple pattern with:
    /* STARTS
        Iterator<BindingNodeId> chain = Iter.map(input, SolverLib.convFromBinding(nodeTable)) ;
        List<Abortable> killList = new ArrayList<>() ;
        for ( Triple triple : triples )
        {
            Tuple<Node> tuple = null;
            if ( graphNode == null )
                // 3-tuples
                tuple = TupleFactory.tuple(triple.getSubject(), triple.getPredicate(), triple.getObject());
            else
                // 4-tuples.
                tuple = TupleFactory.tuple(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject());
            // Plain RDF
            //chain = solve(nodeTupleTable, tuple, anyGraph, chain, filter, execCxt) ;
            // RDF-star
            chain = SolverRX.solveRX(nodeTupleTable, tuple, anyGraph, chain, filter, execCxt) ;
            chain = makeAbortable(chain, killList);
        }
    */

    // [RDF-star] Pattern tuple and node+triple
    static Iterator<BindingNodeId> stepOne(Iterator<BindingNodeId> chain, Node graphNode, Triple tPattern, NodeTupleTable nodeTupleTable, Tuple<Node> patternTuple, boolean anyGraph, Predicate<Tuple<NodeId>> filter, ExecutionContext execCxt) {
        // [RDF-star] Optional. Run tests with this off.
        if ( ! RX_tripleHasNodeTriple(tPattern) || tPattern.isConcrete() ) {
            // No RDF-star <<>> with variables.
            return StageMatchTuple.access(nodeTupleTable, chain, patternTuple, filter, anyGraph, execCxt);
        }

        // [RDF-star]
        // Filter
        // AnyGraph

        boolean isTriple = (patternTuple.len() == 3);

        NodeTable nodeTable = nodeTupleTable.getNodeTable();

        // The scan.
        Function<BindingNodeId, Iterator<BindingNodeId>> step = bnid -> {
            // [RDF-star] Is tuple in SolverLib necessary?
            Node g =
                    graphNode == null
                    ? null
                    : RX_nodeTopLevel(graphNode);
            Node s = RX_nodeTopLevel(tPattern.getSubject());
            Node p = RX_nodeTopLevel(tPattern.getPredicate());
            Node o = RX_nodeTopLevel(tPattern.getObject());
            // No - this is creating bindings. We want data.
//            Tuple<Node> dataAccessTuple = null;
//            if ( graphNode == null )
//                dataAccessTuple = TupleFactory.create3(s, p, o);
//            else
//                dataAccessTuple = TupleFactory.create4(g, s, p, o);

            // XXX See StageMatchTuple.access. Filter and anyGraph

            // [RDF-star] access to below-variable call.
            // Iterator<BindingNodeId> topLevel = StageMatchTuple.access(nodeTupleTable, bnid, dataAccessTuple, filter, anyGraph, execCxt);

            DatasetGraph dsg = execCxt.getDataset();
            Iterator<Quad> dsgIter = dsg.find(g, s, p, o);
            // [RDF-star] bnid.isEmpty() =>
            // isEmpty ==> Binding input = BindingRoot.create();
            Binding input = new BindingTDB(bnid, nodeTable);
            Iterator<Binding> matched = Iter.iter(dsgIter).map(dQuad->matchQuad(input, dQuad, g, tPattern)).removeNulls();
            return convFromBinding(matched, nodeTable);
        };
        return Iter.flatMap(chain, step);
    }

    // Quads as node+triple.
    public static Binding matchQuad(Binding input, Quad qData,  Node qGraphNode, Triple tPattern) {
        return RX_SA.match(input, qData, qGraphNode, tPattern);
    }

    private static Node RX_nodeTopLevel(Node node) {
        if ( Var.isVar(node) )
            return Node.ANY;
        if ( node.isNodeTriple() ) { //|| node.isNodeGraph() )
            if ( ! Node_Triple.triple(node).isConcrete() )
                // Nested variables.
                return Node.ANY;
        }
        return node ;
    }

    /**
     * Test whether a triple has an triple term as one of its components.
     */
    private static boolean RX_tripleHasNodeTriple(Triple triple) {
        return triple.getSubject().isNodeTriple()
               /*|| triple.getPredicate().isNodeTriple()*/
               || triple.getObject().isNodeTriple();
    }

    // -- QueryIterator to Iterator<BindingNodeId>
    private static Iterator<BindingNodeId> convFromBinding(Iterator<Binding> input, NodeTable nodeTable) {
        return Iter.map(input, convFromBinding(nodeTable));
    }

    private static Function<Binding, BindingNodeId> convFromBinding(final NodeTable nodeTable) {
        //return SolverLib.convFromBinding(nodeTable);
        return binding -> SolverLib.convert(binding, nodeTable);
    }


    // ---- Other

//
//
//    // If triple has one or more <<?>>
//
////    public static QueryIterator rdfStarTriple(QueryIterator chain, Triple triple, ExecutionContext execCxt) {
////        if ( MODE_DEV )
////            return rdfStarTriple_SA(chain, triple, execCxt);
////        else
////            return rdfStarTriple_PG(chain, triple, execCxt);
////    }
//
//    private static Iterator<BindingNodeId> /*SolverRX.*/solveRX_PG(NodeTupleTable nodeTupleTable, Tuple<Node> pattern, boolean anyGraph,
//                                                                   Iterator<BindingNodeId> chain, Predicate<Tuple<NodeId>> filter,
//                                                                   ExecutionContext execCxt) {
//        return null;
//    }
//
//
//
//    // called from SolverLib.
//    // which is bridge to/from NodeIds.
//
//    private static void match_tdb(NodeTupleTable nodeTupleTable, Binding binding) {
//
//        NodeTable nodeTable = nodeTupleTable.getNodeTable();
//        // Node -> NodeId
//        Function<Binding, BindingNodeId> conv = /*SolverLib.*/convFromBinding(nodeTable);
//        // chain = SolverRX.solveRX(nodeTupleTable, tuple, anyGraph, chain, filter, execCxt) ;
//
//        //---
//
//        // NodeId -> Node
//        Iterator<BindingNodeId> chain = null;
//        Iterator<Binding> iterBinding = convertToNodes(chain, nodeTable) ;
//    }
//
//    // -- QueryIterator to Iterator<BindingNodeId>
//    // XXX Close iterator.
//    private static Iterator<BindingNodeId> convFromBinding(Iterator<Binding> input, NodeTable nodeTable) {
//        return Iter.map(input, convFromBinding(nodeTable));
//    }
//
//    private static Function<Binding, BindingNodeId> convFromBinding(final NodeTable nodeTable) {
//        //return SolverLib.convFromBinding(nodeTable);
//        return binding -> SolverLib.convert(binding, nodeTable);
//    }
//
//    // -- Iterator<BindingNodeId> to Iterator<Binding> to QueryIterator
//
//    private static Iterator<Binding> convertToNodes(Iterator<BindingNodeId> iterBindingIds, NodeTable nodeTable)
//    { return Iter.map(iterBindingIds, bindingNodeIds -> convToBinding(bindingNodeIds, nodeTable)); }
//
//    private static Binding convToBinding(BindingNodeId bindingNodeIds, NodeTable nodeTable) {
//        if ( true )
//            return new BindingTDB(bindingNodeIds, nodeTable) ;
//        else {
//            // Makes nodes immediately. Causing unnecessary NodeTable accesses
//            // (e.g. project)
//            BindingMap b = BindingFactory.create() ;
//            for (Var v : bindingNodeIds) {
//                NodeId id = bindingNodeIds.get(v) ;
//                Node n = nodeTable.getNodeForNodeId(id) ;
//                b.add(v, n) ;
//            }
//            return b ;
//        }
//    }

}
