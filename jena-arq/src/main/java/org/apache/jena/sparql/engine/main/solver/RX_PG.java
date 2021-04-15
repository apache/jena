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

package org.apache.jena.sparql.engine.main.solver;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterAddTripleTerm;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.util.Context;

/**
 * For reference only: this code uses the triple index for matching and it is PG mode.
 */
/*
 * Solver library for RDF-star.
 * <p>
 * There are two entry points.
 * <p>
 * Function {@link #rdfStarTriple} for matching a single triple pattern in a basic
 * graph pattern that may involve RDF-star terms.
 * <p>
 * Function {@link #matchTripleStar} for matches a triple term and assigning the
 * triple matched to a variable. It is used within {@link #rdfStarTriple} for nested
 * triple term and a temporary allocated variable as well can for
 * {@code FIND(<<...>> AS ?t)}.
 */
/*ARCHIVE*/ class RX_PG {

    // ---- PG mode ----
    // This code implements RDF-star in "PG mode" (the embedded triple also exists as
    // an asserted triple in the same graph). With this assumption, pattern matching
    // of <<?s ?p ?o>> can use an index.

    /**
     * Match a single triple pattern that may involve RDF-star terms.
     * This is the top level function for matching triples in PG mode.
     */
    /*public*//*package*/
    /*ARCHIVE*/ private static QueryIterator rdfStarTriple_PG(QueryIterator chain, Triple triple, ExecutionContext execCxt) {
        // Should all work without this trap for plain RDF.
        if ( ! tripleHasNodeTriple(triple) )
            // No RDF-star : direct to data.
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
    private static QueryIterator rdfStarTripleSub(QueryIterator chain, Triple triple, ExecutionContext execCxt) {
        Pair<QueryIterator, Triple> pair = preprocessForTripleTerms(chain, triple, execCxt);
        QueryIterator chain2 = matchData(pair.getLeft(), pair.getRight(), execCxt);
        return chain2;
    }

    /**
     * Match a triple pattern (which may have nested triple terms in it).
     * Any matched triples are added as triple terms bound to the supplied variable.
     */

    /*public*/private static QueryIterator findTripleStar(QueryIterator chain, Var var, Triple triple, ExecutionContext execCxt) {
        // Called from OpFind/apf:find = TripleTermFind which are to be removed.
        return matchTripleStar(chain, var, triple, execCxt);
    }

    private static QueryIterator matchTripleStar(QueryIterator chain, Var var, Triple triple, ExecutionContext execCxt) {
        if ( tripleHasNodeTriple(triple) ) {
            Pair<QueryIterator, Triple> pair = preprocessForTripleTerms(chain, triple, execCxt);
            chain = pair.getLeft();
            triple = pair.getRight();
        }
        // Match to data and assign to var in each binding, based on the triple pattern grounded by the match.
        QueryIterator qIter = bindTripleTerm(chain, var, triple, execCxt);
        return qIter;
    }

    /**
     * Process a triple for triple terms.
     * <p>
     * This creates additional matchers for triple terms in the pattern triple recursively.
     */
    private static Pair<QueryIterator, Triple> preprocessForTripleTerms(QueryIterator chain, Triple patternTriple, ExecutionContext execCxt) {
        // PG mode
        Node s = patternTriple.getSubject();
        Node p = patternTriple.getPredicate();
        Node o = patternTriple.getObject();
        Node s1 = null;
        Node o1 = null;

        // Recurse.
        if ( s.isNodeTriple() ) {
            Triple t2 = s.getTriple();
            Var var = varAlloc(execCxt).allocVar();
            Triple tripleTerm = Triple.create(t2.getSubject(), t2.getPredicate(), t2.getObject());
            chain = matchTripleStar(chain, var, tripleTerm, execCxt);
            s1 = var;
        }
        if ( o.isNodeTriple() ) {
            Triple t2 = o.getTriple();
            Var var = varAlloc(execCxt).allocVar();
            Triple tripleTerm = Triple.create(t2.getSubject(), t2.getPredicate(), t2.getObject());
            chain = matchTripleStar(chain, var, tripleTerm, execCxt);
            o1 = var;
        }

        // Because of the test in rdfStarTriple,
        // This code only happens when there is a a triple term.

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
     * Add a binding to each row with triple grounded by the current row.
     */
    private static QueryIterator bindTripleTerm(QueryIterator chain, Var var, Triple pattern, ExecutionContext execCxt) {
        QueryIterator qIter = matchData(chain, pattern, execCxt);
        QueryIterator qIter2 = new QueryIterAddTripleTerm(qIter, var, pattern, execCxt);
        return qIter2;
    }

    /**
     * Match the graph with a triple pattern.
     * This is the accessor to the graph.
     * It assumes any triple terms have been dealt with.
     */
    private static QueryIterator matchData(QueryIterator chain, Triple pattern, ExecutionContext execCxt) {
        return QC.execute(chain, pattern, execCxt);
    }

    /**
     * Test whether a triple has an triple term as one of its components.
     */
    private static boolean tripleHasNodeTriple(Triple triple) {
        return triple.getSubject().isNodeTriple()
               /*|| triple.getPredicate().isNodeTriple()*/
               || triple.getObject().isNodeTriple();
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
}

