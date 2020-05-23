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

import static org.apache.jena.graph.Node_Triple.triple;

import java.util.Iterator;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.iterator.RX;
import org.apache.jena.tdb.TDBException;
import org.apache.jena.tdb.store.NodeId;
import org.apache.jena.tdb.store.nodetable.NodeTable;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable;

/**
 * See {@link RX} which is the same algorithm for Triple/Node space.
 */
public class SolverRX {

    // These argument get passe around a lot, makign the argument lists long.
    private static class SolverCxt {
        final NodeTupleTable nodeTupleTable;
        final boolean anyGraphs;
        final Predicate<Tuple<NodeId>> filter;
        final ExecutionContext execCxt;
        SolverCxt(NodeTupleTable nodeTupleTable, boolean anyGraphs, Predicate<Tuple<NodeId>> filter, ExecutionContext execCxt) {
            super();
            this.nodeTupleTable = nodeTupleTable;
            this.anyGraphs = anyGraphs;
            this.filter = filter;
            this.execCxt = execCxt;
        }
    }


    // Call point for SolverLib.execute
    public static Iterator<BindingNodeId> solveRX(NodeTupleTable nodeTupleTable, Tuple<Node> tuple, boolean anyGraph,
                                                  Iterator<BindingNodeId> chain, Predicate<Tuple<NodeId>> filter,
                                                  ExecutionContext execCxt) {
        SolverCxt sCxt = new SolverCxt(nodeTupleTable, anyGraph, filter, execCxt);
        return rdfStarTriple(chain, tuple, sCxt);
    }


    private static Iterator<BindingNodeId> rdfStarTriple(Iterator<BindingNodeId> input, Tuple<Node> pattern, SolverCxt sCxt) {
        if ( ! tripleHasNodeTriple(pattern) )
            return matchData( input, pattern, sCxt);
        return rdfStarTripleSub(input, pattern, sCxt);
    }

    private static Iterator<BindingNodeId> rdfStarTripleSub(Iterator<BindingNodeId> input,
                                                            Tuple<Node> pattern, SolverCxt sCxt) {
        Pair<Iterator<BindingNodeId>, Tuple<Node>> pair = preprocessForTripleTerms(input, pattern, sCxt);
        Iterator<BindingNodeId> chain2 = matchData(pair.getLeft(), pair.getRight(), sCxt);
        return chain2;
    }

    /**
     * Match a triple pattern (which may have nested triple terms in it).
     * Any matched triples are added as triple terms bound to the supplied variable.
     */
    private static Iterator<BindingNodeId> matchTripleStar(Iterator<BindingNodeId> chain, Var var, Tuple<Node> pattern, SolverCxt sCxt) {
        if ( tripleHasNodeTriple(pattern) ) {
            Pair<Iterator<BindingNodeId>, Tuple<Node>> pair =
                preprocessForTripleTerms(chain, pattern, sCxt);
            chain = pair.getLeft();
            pattern = pair.getRight();
        }
        // Match to data and assign to var in each binding, based on the triple pattern grounded by the match.
        Iterator<BindingNodeId> qIter = bindTripleTerm(chain, var, pattern, sCxt);
        return qIter;
    }

    // XXX RX
    private static VarAlloc varAlloc = new VarAlloc("*1*"/*allocTripleTerms*/) ;

    private static Pair<Iterator<BindingNodeId>, Tuple<Node>>
            preprocessForTripleTerms(Iterator<BindingNodeId> chain, Tuple<Node> patternTuple, SolverCxt sCxt) {
        int sIdx = subjectIdx(patternTuple);
        int oIdx = objectIdx(patternTuple);

        Node subject = patternTuple.get(sIdx);
        Node object = patternTuple.get(oIdx);

        if ( subject.isNodeTriple() && ! subject.isConcrete() ) {
            Triple tripleTerm = triple(subject);
            Var var = varAlloc.allocVar();
            patternTuple = createTuple(patternTuple, var, sIdx);
            Tuple<Node> patternTuple2 = tuple(tripleTerm);
            chain = matchTripleStar(chain, var, patternTuple2, sCxt);
        }

        if ( object.isNodeTriple() && ! object.isConcrete() ) {
            Triple tripleTerm = triple(object);
            Var var = varAlloc.allocVar();
            patternTuple = createTuple(patternTuple, var, oIdx);
            Tuple<Node> patternTuple2 = tuple(tripleTerm);
            chain = matchTripleStar(chain, var, patternTuple2, sCxt);
        }

        // XXX Optimize for no change. But we caught that earlier?
        return Pair.create(chain, patternTuple);
    }

    /**
     * Add a binding to each row with triple grounded by the current row.
     * If the triple isn't concrete, then just return the row as-is.
     */
    private static Iterator<BindingNodeId> bindTripleTerm(Iterator<BindingNodeId> chain, Var var, Tuple<Node> pattern, SolverCxt sCxt) {
        NodeTable nodeTable = sCxt.nodeTupleTable.getNodeTable();
        chain = matchData(chain, pattern, sCxt);
        // Add (var, triple term), filter no matches.
        chain = Iter.iter(chain).map(b->bindVarTripleTerm(var, pattern, b, nodeTable)).removeNulls();
        return chain;
    }

    // We need to reconstruct the reason the pattern matched
    // to find the NodeId for the Node_Triple.
    // This involves creating a Node_Triple and looking it up.
    // This isn't ideal but without triple ids in the database,
    // there isn't much we can do.
    private static BindingNodeId bindVarTripleTerm(Var var, Tuple<Node> pattern, BindingNodeId binding, NodeTable nodeTable) {
        // Get triple out of tuple of length 3 or 4.
        int idx = (pattern.len()==4) ? 1 : 0;

        // Access to Nodes.
        Node s = pattern.get(idx);
        Node s1 = substitute(s, binding, nodeTable);
        if ( s1 == null || ! s1.isConcrete() )
            return null;

        Node p = pattern.get(idx+1);
        Node p1 = substitute(p, binding, nodeTable);
        if ( p1 == null || ! p1.isConcrete() )
            return null;

        Node o = pattern.get(idx+2);
        Node o1 = substitute(o, binding, nodeTable);
        if ( o1 == null || ! o1.isConcrete() )
            return null;

        // Does it exist?
        Node t = NodeFactory.createTripleNode(s1,p1,o1);
        NodeId tid = nodeTable.getNodeIdForNode(t);
        // Should not happen.
        if ( NodeId.isDoesNotExist(tid) )
            return null;
        BindingNodeId b2 = new BindingNodeId(binding);
        b2.put(var, tid);
        return b2;
    }

    private static Node substitute(Node node, BindingNodeId binding, NodeTable nodeTable) {
        if ( ! Var.isVar(node) )
            return node;
        Var var = Var.alloc(node);
        try {
            NodeId id = binding.get(var) ;
            if ( id == null )
                return null ;
            if ( NodeId.isDoesNotExist(id) )
                return null;
            Node n = nodeTable.getNodeForNodeId(id) ;
            if ( n == null )
                // But there was to put it in the BindingNodeId.
                throw new TDBException("No node in NodeTable for NodeId "+id);
            return n ;
        } catch (Exception ex)
        {
            FmtLog.error(SolverRX.class, ex, "SolverRX: substitute(%s) %s", node, binding) ;
            return null ;
        }
    }

    private static Iterator<BindingNodeId> matchData(Iterator<BindingNodeId> chain, Tuple<Node> pattern, SolverCxt sCxt) {
        return SolverLib.solve(sCxt.nodeTupleTable, pattern, sCxt.anyGraphs, chain, sCxt.filter, sCxt.execCxt);
    }

    private static Tuple<Node> createTuple(Tuple<Node> tuple, Var var, int idx) {
        switch(idx) {
            case 0: return TupleFactory.create3(var, tuple.get(1), tuple.get(2));
            case 1: return TupleFactory.create4(tuple.get(0), var, tuple.get(2), tuple.get(3));
            case 2: return TupleFactory.create3(tuple.get(0), tuple.get(1), var);
            case 3: return TupleFactory.create4(tuple.get(0), tuple.get(1), tuple.get(2), var);
            default:
                throw new ARQException("Index is not recognized: "+idx);
        }
    }

    private static int subjectIdx(Tuple<Node> pattern) {
        switch(pattern.len()) {
            case 3: return 0;
            case 4: return 1;
            default: throw new ARQException("Tuple not of length 3 or 4");
        }
    }

    private static int objectIdx(Tuple<Node> pattern) {
        switch(pattern.len()) {
            case 3: return 2;
            case 4: return 3;
            default: throw new ARQException("Tuple not of length 3 or 4");
        }
    }

    // Get NodeId for constants
    private static NodeId idFor(NodeTable nodeTable, Node node) {
        if ( Var.isVar(node) )
            return null;
        return nodeTable.getNodeIdForNode(node);
    }

    private static boolean tripleHasNodeTriple(Tuple<Node> pattern) {
        int sIdx = subjectIdx(pattern);
        if ( pattern.get(sIdx).isNodeTriple() )
            return true;
        int oIdx = subjectIdx(pattern);
        if ( pattern.get(oIdx).isNodeTriple() )
            return true;
        return false;
    }

    // XXX Somewhere
    private static Tuple<Node> tuple(Triple triple) {
        return TupleFactory.create3(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    private static Tuple<Node> tuple(Quad quad) {
        return TupleFactory.create4(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }
}

