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

package org.apache.jena.sparql.engine.iterator;

import static org.apache.jena.graph.Node_Triple.triple;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;

/**
 * Solver library for RDF*.
 * <p>
 * There are two entry points.
 * <p>
 * Function {@link #rdfStarTriple} for matching a single triple pattern in a basic
 * graph pattern that may involve RDF* terms.
 * <p>
 * Function {@link #matchTripleStar} for matches a triple term and assigning the
 * triple matched to a variable. It is used within {@link #rdfStarTriple} for nested
 * triple term and a temporary allocated variable as well can for
 * {@code FIND(<<...>> AS ?t)}.
 */
public class RX {
    // From context?
    static String allocTripleTerms = "*";
    static VarAlloc varAlloc = new VarAlloc(allocTripleTerms) ;

    // QueryIterator - acceptable as API as universal.
    //    Internal TDB is a delayed binding iterator.
    // <Triple, Node> vs <Tuple, NodeId>
    // TDB: Iterator<BindingNodeId>

    // TDB: StageMatchTuple ~~ QueryIterTriplePattern

    // TDB: SolverLib.solve
    //   The two solver libs are identical except for imports.
    /*
    public static Iterator<BindingNodeId> solve(NodeTupleTable nodeTupleTable,
                                                Tuple<Node> tuple,
                                                boolean anyGraph,
                                                Iterator<BindingNodeId> chain, Predicate<Tuple<NodeId>> filter,
                                                ExecutionContext execCxt)
    {
        return new StageMatchTuple(nodeTupleTable, chain, tuple, anyGraph, filter, execCxt) ;
    }
     */


    /**
     * Match a single triple pattern that may involve RDF* terms.
     * This is the top level function for matching triples.
     *
     * The function {@link #matchTripleStar} matches a triple term and assigns the triple matched to a variable.
     * It is used within {@link #rdfStarTriple} for nested triple term and a temporary allocated variable
     * as well can for {@code FIND(<<...>> AS ?t)}.
     *
     * @implNote
     * Without RDF*, this would be a plain call of {@link #matchData} which is simply:
     * <pre>
     * new QueryIterTriplePattern(chain, triple, execContext)}
     * </pre>
     *
     */
    public static QueryIterator rdfStarTriple(QueryIterator chain, Triple triple, ExecutionContext execCxt) {
        // Should all work without this trap for plain RDF but for now,
        // fast track the non-RDF* case.
        if ( ! tripleHasNodeTriple(triple) )
            // No RDF* : direct to data.
            return matchData(chain, triple, execCxt);
        return rdfStarTripleSub(chain, triple, execCxt);
    }

    /**
     * Insert the stages necessary for a triple with triple pattern term inside it.
     * If the triple pattern has a triple term, possibly with variables, introduce
     * an iterator to solve for that, assign the matching triple term to a hidden
     * variable, and put allocated variable in to main triple pattern. Do for subject
     * and object positions, and also any nested triple pattern terms.
     */
    private static QueryIterator rdfStarTripleSub(QueryIterator chain, Triple triple, ExecutionContext execContext) {
        Pair<QueryIterator, Triple> pair = preprocess(chain, triple, execContext);
        QueryIterator chain2 = matchData(pair.getLeft(), pair.getRight(), execContext);
        return chain2;
    }

    /**
     * Match a triple pattern (which may have nested triple terms in it).
     * Any matched triples are added as triple terms bound to the supplied variable.
     */
    public static QueryIterator matchTripleStar(QueryIterator chain, Var var, Triple triple, ExecutionContext execContext) {
        if ( tripleHasNodeTriple(triple) ) {
            Pair<QueryIterator, Triple> pair = preprocess(chain, triple, execContext);
            chain = pair.getLeft();
            triple = pair.getRight();
        }
        // Assign to var in each binding, based on the triple pattern grounded by the match.
        QueryIterator qIter = bindTripleTerm(chain, var, triple, execContext);
        return qIter;
    }

    // If we assume the data is correct (in PG mode), no need to test for the triple
    // of a concrete Node_Triple because we are able to test for it in the triple
    // pattern itself. This should be "false".
    // XXX To be removed.
    private static final boolean TEST_FOR_CONCRETE_TRIPLE_TERM = false;

    /**
     * Process a triple for triple terms.
     * <p>
     * This creates additional matchers for triple terms in the pattern triple recursively.
     */
    private static Pair<QueryIterator, Triple> preprocess(QueryIterator chain, Triple patternTriple, ExecutionContext execContext) {
        Triple triple2 = patternTriple;
        Node s = patternTriple.getSubject();
        Node p = patternTriple.getPredicate();
        Node o = patternTriple.getObject();
        Node s1 = null;
        Node o1 = null;

        // Recurse.
        if ( s.isNodeTriple() ) {
            if ( TEST_FOR_CONCRETE_TRIPLE_TERM || ! s.isConcrete() ) {
                Triple t2 = triple(s);
                Var var = varAlloc.allocVar();
                Triple tripleTerm = Triple.create(t2.getSubject(), t2.getPredicate(), t2.getObject());
                chain = matchTripleStar(chain, var, tripleTerm, execContext);
                s1 = var;
            }
        }
        if ( o.isNodeTriple() ) {
            if ( TEST_FOR_CONCRETE_TRIPLE_TERM || ! o.isConcrete() ) {
                Triple t2 = triple(o);
                Var var = varAlloc.allocVar();
                Triple tripleTerm = Triple.create(t2.getSubject(), t2.getPredicate(), t2.getObject());
                chain = matchTripleStar(chain, var, tripleTerm, execContext);
                o1 = var;
            }
        }

        // No triple term in this triple.
        if ( s1 == null && o1 == null )
            return Pair.create(chain, patternTriple);

        // Change. Replace original.
        if ( s1 == null )
            s1 = s ;
        if ( o1 == null )
            o1 = o ;
        Triple triple1 = Triple.create(s1, p, o1);
        return Pair.create(chain, triple1);
    }

    /**
     * Match the graph with a triple pattern.
     * This is the accessor the graph.
     * It assumes any triple terms have been dealt with.
     */
    private static QueryIterator matchData(QueryIterator chain, Triple triple, ExecutionContext execContext) {
        QueryIterator qIter = new QueryIterTriplePattern(chain, triple, execContext);
        return qIter;
    }

    /**
     * Add a binding to each row with triple grounded by the current row.
     * If the triple isn't concrete, then just return the row as-is.
     */
    private static QueryIterator bindTripleTerm(QueryIterator chain, Var var, Triple triple, ExecutionContext execContext) {
        return new QueryIterAddTripleTerm(chain, var, triple, execContext);
    }

    /**
     * Test whether a triple has an triple term as one of its components.
     */
    private static boolean tripleHasNodeTriple(Triple triple) {
        return triple.getSubject().isNodeTriple()
               /*|| triple.getPredicate().isNodeTriple()*/
               || triple.getObject().isNodeTriple();
    }
}

