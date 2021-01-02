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
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.iterator.RX;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb.TDBException;
import org.apache.jena.tdb.store.NodeId;
import org.apache.jena.tdb.store.nodetable.NodeTable;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable;

/**
 * See {@link RX} which is the same algorithm for Triple/Node space.
 */
public class SolverRX {

    // These argument get passed around a lot, making the argument lists long.
    private static class Args {
        final NodeTupleTable nodeTupleTable;
        final boolean anyGraph;
        final Predicate<Tuple<NodeId>> filter;
        final ExecutionContext execCxt;
        final VarAlloc varAlloc;
        Args(NodeTupleTable nodeTupleTable, boolean anyGraph, Predicate<Tuple<NodeId>> filter, ExecutionContext execCxt) {
            super();
            this.nodeTupleTable = nodeTupleTable;
            this.anyGraph = anyGraph;
            this.filter = filter;
            this.execCxt = execCxt;
            this.varAlloc = varAlloc(execCxt);
        }
    }

    private static VarAlloc varAlloc(ExecutionContext execCxt) {
        Context context = execCxt.getContext();
        VarAlloc varAlloc = VarAlloc.get(context, ARQConstants.sysVarAllocRDFStar);
        if ( varAlloc == null ) {
            varAlloc = new VarAlloc(ARQConstants.allocVarTripleTerm);
            context.set(ARQConstants.sysVarAllocRDFStar, varAlloc);
        }
        return varAlloc;
    }

    // Call point for SolverLib.execute
    public static Iterator<BindingNodeId> solveRX(NodeTupleTable nodeTupleTable, Tuple<Node> pattern, boolean anyGraph,
                                                  Iterator<BindingNodeId> chain, Predicate<Tuple<NodeId>> filter,
                                                  ExecutionContext execCxt) {
        if ( ! tripleHasNodeTriple(pattern) )
            SolverLib.solve(nodeTupleTable, pattern, anyGraph, chain, filter, execCxt);

        Args args = new Args(nodeTupleTable, anyGraph, filter, execCxt);
        return rdfStarTriple(chain, pattern, args);
    }

    /**
     * Match a single triple pattern that may involve RDF-star terms. This is the top
     * level function for matching triples. The function {@link #matchTripleStar}
     * matches a triple term and assigns the triple matched to a variable. It is used
     * within {@link #rdfStarTriple} for nested triple term and a temporary allocated
     * variable as well can for {@code FIND(<<...>> AS ?t)}.
     *
     * @implNote
     * Without RDF-star, this would be a plain call of {@link #matchData} which
     * is simply a call to {@link SolverLib#solve}.
     */
    private static Iterator<BindingNodeId> rdfStarTriple(Iterator<BindingNodeId> input, Tuple<Node> pattern, Args args) {
        // Should all work without this trap for plain RDF.
        if ( ! tripleHasNodeTriple(pattern) )
            return matchData( input, pattern, args);
        return rdfStarTripleSub(input, pattern, args);
    }

    /**
     * Insert the stages necessary for a triple with triple pattern term inside it.
     * If the triple pattern has a triple term, possibly with variables, introduce
     * an iterator to solve for that, assign the matching triple term to a hidden
     * variable, and put allocated variable in to main triple pattern. Do for subject
     * and object positions, and also any nested triple pattern terms.
     */
    private static Iterator<BindingNodeId> rdfStarTripleSub(Iterator<BindingNodeId> input,
                                                            Tuple<Node> pattern, Args args) {
        Pair<Iterator<BindingNodeId>, Tuple<Node>> pair = preprocessForTripleTerms(input, pattern, args);
        Iterator<BindingNodeId> chain2 = matchData(pair.getLeft(), pair.getRight(), args);
        return chain2;
    }

    /**
     * Match a triple pattern (which may have nested triple terms in it).
     * Any matched triples are added as triple terms bound to the supplied variable.
     */
    private static Iterator<BindingNodeId> matchTripleStar(Iterator<BindingNodeId> chain, Var var, Tuple<Node> pattern, Args args) {
        if ( tripleHasNodeTriple(pattern) ) {
            Pair<Iterator<BindingNodeId>, Tuple<Node>> pair =
                preprocessForTripleTerms(chain, pattern, args);
            chain = pair.getLeft();
            pattern = pair.getRight();
        }
        // Match to data and assign to var in each binding, based on the triple pattern grounded by the match.
        Iterator<BindingNodeId> qIter = bindTripleTerm(chain, var, pattern, args);
        return qIter;
    }

    /**
     * Process a triple for triple terms.
     * <p>
     * This creates additional matchers for triple terms in the pattern triple recursively.
     */
    private static Pair<Iterator<BindingNodeId>, Tuple<Node>>
            preprocessForTripleTerms(Iterator<BindingNodeId> chain, Tuple<Node> patternTuple, Args args) {
        int sIdx = subjectIdx(patternTuple);
        int oIdx = objectIdx(patternTuple);

        Node subject = patternTuple.get(sIdx);
        Node object = patternTuple.get(oIdx);
        Node subject1 = null;
        Node object1 = null;

        if ( subject.isNodeTriple() ) {
            Triple tripleTerm = triple(subject);
            Var var = args.varAlloc.allocVar();
            patternTuple = createTuple(patternTuple, var, sIdx);
            Tuple<Node> patternTuple2 = tuple(patternTuple, tripleTerm);
            chain = matchTripleStar(chain, var, patternTuple2, args);
            subject1 = var;
        }

        if ( object.isNodeTriple() ) {
            Triple tripleTerm = triple(object);
            Var var = args.varAlloc.allocVar();
            patternTuple = createTuple(patternTuple, var, oIdx);
            Tuple<Node> patternTuple2 = tuple(patternTuple, tripleTerm);
            chain = matchTripleStar(chain, var, patternTuple2, args);
            object1 = var;
        }

        if ( subject1 == null && object1 == null )
            return Pair.create(chain, patternTuple);
        return Pair.create(chain, patternTuple);
    }

    /**
     * Add a binding to each row with triple grounded by the current row.
     * If the triple isn't concrete, then just return the row as-is.
     */
    private static Iterator<BindingNodeId> bindTripleTerm(Iterator<BindingNodeId> chain, Var var, Tuple<Node> pattern, Args args) {
        NodeTable nodeTable = args.nodeTupleTable.getNodeTable();
        chain = matchData(chain, pattern, args);
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
        // Already bound (FIND)?
        if ( binding.containsKey(var) ) {
            NodeId tid2 = binding.get(var);
            if ( tid.equals(tid2) )
                return binding;
            return null;
        }

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

    /**
     * Match the NodeTupleTable with a tuple pattern.
     * This is the accessor to the data.
     * It assumes any triple terms have been dealt with.
     */

    private static Iterator<BindingNodeId> matchData(Iterator<BindingNodeId> chain, Tuple<Node> pattern, Args args) {
        return SolverLib.solve(args.nodeTupleTable, pattern, args.anyGraph, chain, args.filter, args.execCxt);
    }

    private static Tuple<Node> createTuple(Tuple<Node> tuple, Var var, int idx) {
        switch(idx) {
            case 0: return TupleFactory.create3(var, tuple.get(1), tuple.get(2));
            case 1: return TupleFactory.create4(tuple.get(0), var, tuple.get(2), tuple.get(3));
            case 2: return TupleFactory.create3(tuple.get(0), tuple.get(1), var);
            case 3: return TupleFactory.create4(tuple.get(0), tuple.get(1), tuple.get(2), var);
            default:
                throw new TDBException("Index is not recognized: "+idx);
        }
    }

    private static int subjectIdx(Tuple<Node> pattern) {
        switch(pattern.len()) {
            case 3: return 0;
            case 4: return 1;
            default: throw new TDBException("Tuple not of length 3 or 4");
        }
    }

    private static int objectIdx(Tuple<Node> pattern) {
        switch(pattern.len()) {
            case 3: return 2;
            case 4: return 3;
            default: throw new TDBException("Tuple not of length 3 or 4");
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

    private static Tuple<Node> tuple(Tuple<Node> base, Triple triple) {
        switch(base.len()){
            case 3:
                return TupleFactory.create3(triple.getSubject(), triple.getPredicate(), triple.getObject());
            case 4:
                return TupleFactory.create4(base.get(0), triple.getSubject(), triple.getPredicate(), triple.getObject());
            default:
        }       throw new TDBException("Tuple not of length 3 or 4");
    }
}
