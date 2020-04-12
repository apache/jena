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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;


/**
 * Like {@link QueryIterBlockTriples} except it process triple term patterns (RDF*)
 * as well.
 */
public class QueryIterBlockTriplesStar extends QueryIter1 {

    public static QueryIterator create(QueryIterator input, BasicPattern pattern, ExecutionContext execContext) {
        return new QueryIterBlockTriplesStar(input, pattern, execContext);
    }

    private final BasicPattern pattern;
    private QueryIterator output;

    private QueryIterBlockTriplesStar(QueryIterator input, BasicPattern pattern, ExecutionContext execContext) {
        super(input, execContext);
        this.pattern = pattern;
        QueryIterator chain = getInput();
        for (Triple triple : pattern) {
            chain = rdfStarTriple(chain, triple);
        }
        output = chain;
    }

    @Override
    protected boolean hasNextBinding() {
        return output.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        return output.nextBinding();
    }

    @Override
    protected void closeSubIterator() {
        if ( output != null )
            output.close();
        output = null;
    }

    @Override
    protected void requestSubCancel() {
        if ( output != null )
            output.cancel();
    }

    static String allocTripleTerms = "*";
    static VarAlloc varAlloc = new VarAlloc(allocTripleTerms) ;

    /** Top level function */
    private QueryIterator rdfStarTriple(QueryIterator chain, Triple triple) {
        // Should all work without this trap for plain RDF but for now,
        // fast track the non-RDF* case.
        if ( ! tripleHasNodeTriple(triple) )
            // No RDF* : direct to data. Behaviour the same as QueryIterBlockTriples.
            return matchData(chain, triple, getExecContext());
        return rdfStarTripleSub(chain, triple, getExecContext());
    }

    /**
     * Insert the stages necessary for a triple with triple pattern term inside it.
     * If the triple pattern has am triple term, possibly with variables, introduce
     * an iterator to solve for that, assign the matching triple term to a hidden
     * variable, and put allocated variable in to main triple pattern. Do for subject
     * and object positions, and also any nested triple pattern terms.
     */
    private static QueryIterator rdfStarTripleSub(QueryIterator chain, Triple triple, ExecutionContext execContext) {
        Pair<QueryIterator, Triple> pair = preprocess(chain, triple, execContext);
        QueryIterator chain2 = matchData(pair.getLeft(), pair.getRight(), execContext);
        return chain2;
    }

    // If we assume the data is correct (in PG mode), no need to test for the triple
    // of a concrete Node_Triple because we able to // test for it in the triple
    // pattern itself.
    // In SA: not an issue.
    // This should be "false".
    private static final boolean TEST_FOR_CONCRETE_TRIPLE_TERM = false;

    private static Pair<QueryIterator, Triple> preprocess(QueryIterator chain, Triple triple, ExecutionContext execContext) {
        Triple triple2 = triple;
        Node s = triple.getSubject();
        Node p = triple.getPredicate();
        Node o = triple.getObject();
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
            return Pair.create(chain, triple);

        // Change. Replace original.
        if ( s1 == null )
            s1 = s ;
        if ( o1 == null )
            o1 = o ;
        Triple triple1 = Triple.create(s1, p, o1);
        return Pair.create(chain, triple1);
    }

    /** Match the graph with a triple pattern, after any triple terms have been dealt with. */
    private static QueryIterator matchData(QueryIterator chain, Triple triple, ExecutionContext execContext) {
        QueryIterator qIter = new QueryIterTriplePattern(chain, triple, execContext);
        return qIter;
    }

    /** Match a triple term pattern; add matches using the supplied variable. */
    private static QueryIterator matchTripleStar(QueryIterator chain, Var var, Triple triple, ExecutionContext execContext) {
        if ( tripleHasNodeTriple(triple) ) {
            Pair<QueryIterator, Triple> pair = preprocess(chain, triple, execContext);
            chain = pair.getLeft();
            triple = pair.getRight();
        }
        // Assign to var in each binding, based on the triple pattern grounded by the match.
        QueryIterator qIter = new QueryIterAddTripleTerm(chain, var, triple, execContext);
        return qIter;
    }

    private static boolean tripleHasNodeTriple(Triple triple) {
        return triple.getSubject().isNodeTriple()
               /*|| triple.getPredicate().isNodeTriple()*/
               || triple.getObject().isNodeTriple();
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt) {
        out.print(this.getClass().getSimpleName()+": " + pattern);
    }
}