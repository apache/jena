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

import static org.apache.jena.sparql.engine.main.solver.SolverLib.nodeTopLevel;
import static org.apache.jena.sparql.engine.main.solver.SolverLib.sameTermAs;
import static org.apache.jena.sparql.engine.main.solver.SolverLib.tripleHasEmbTripleWithVars;

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.util.iterator.ExtendedIterator;

/** RDF-star - triple form. Solve patterns when there is an embedded triple with variables. */
public class SolverRX3 {

    /**
     * This constant is not public API. It is exposed only so integration testing can
     * check the value for a release build.
     */
    public static final boolean DATAPATH = true;

    public static Iterator<Binding> rdfStarTriple(Iterator<Binding> chain, Triple tPattern, ExecutionContext execCxt) {
        // Should all work without this trap for plain RDF.
        if ( DATAPATH ) {
            // No variables inside <<>>
            if ( ! tripleHasEmbTripleWithVars(tPattern) )
                // No RDF-star : direct to data.
                return matchData(chain, tPattern, execCxt);
        }
        return rdfStarTripleSub(chain, tPattern, execCxt);
    }

    private static Iterator<Binding> rdfStarTripleSub(Iterator<Binding> input, Triple tPattern, ExecutionContext execCxt) {
        Iterator<Binding> matches = Iter.flatMap(input, binding->rdfStarTripleSub(binding, tPattern, execCxt));
        return matches;
    }

    private static Iterator<Binding> rdfStarTripleSub(Binding input, Triple xPattern, ExecutionContext execCxt) {
        Triple tPattern = Substitute.substitute(xPattern, input);
        Node s = nodeTopLevel(tPattern.getSubject());
        Node p = nodeTopLevel(tPattern.getPredicate());
        Node o = nodeTopLevel(tPattern.getObject());
        Graph graph = execCxt.getActiveGraph();
        ExtendedIterator<Triple> graphIter = graph.find(s, p, o);
        ExtendedIterator<Binding> matched = graphIter.mapWith(tData->matchTriple(input, tData, tPattern)).filterDrop(Objects::isNull);
        return matched;
    }

    /**
     * Direct.
     * Match the graph with a triple pattern.
     * This is the accessor to the graph.
     * It assumes any triple terms have been dealt with.
     */
    private static Iterator<Binding> matchData(Iterator<Binding> chain, Triple pattern, ExecutionContext execCxt) {
        Graph g = execCxt.getActiveGraph();
        Iterator<Binding> iter = StageMatchTriple.accessTriple(chain, g, pattern, null, execCxt);
        return iter;
        // 4.0.0 and before.
        //return new QueryIterTriplePattern(chain, pattern, execCxt);
    }

    public static Binding matchTriple(Binding input, Triple tData, Triple tPattern) {
        BindingBuilder bb = Binding.builder(input);
        boolean r = bindTriple(bb, tData, tPattern);
        return r ? bb.build() : null;
    }

    // Used in matching a triple in an embedded triple term.
    /*package*/ static boolean bindTriple(BindingBuilder bb, Triple tData, Triple tPattern) {
        Node sPattern = tPattern.getSubject();
        Node pPattern = tPattern.getPredicate();
        Node oPattern = tPattern.getObject();

        Node sData = tData.getSubject();
        Node pData = tData.getPredicate();
        Node oData = tData.getObject();

        if ( ! match(bb, sData, sPattern) )
            return false;
        if ( ! match(bb, pData, pPattern) )
            return false;
        if ( ! match(bb, oData, oPattern) )
            return false;
        return true;
    }
    // RDF term matcher - may recurse and call matchTriple is a triple term is in the pattern.
    private static boolean match(BindingBuilder bb, Node nData, Node nPattern) {
        if ( nPattern == null )
            return true;
        if ( nData == Node.ANY )
            return true;

        // Deep substitute. This happens anyway as we walk structures.
        //   nPattern = Substitute.substitute(nPattern, input);
        // Shallow substitute
        nPattern = Var.lookup(v->bb.get(v), nPattern);

        // nPattern.isConcrete() : either nPattern is an RDF term or is <<>> with no variables.
        if ( nPattern.isConcrete() ) {
            // No nested variables. Is data equal to pattern?
            // Term comparison.
            return sameTermAs(nData, nPattern);
        }

        // Easy case - nPattern is a variable.
        if ( Var.isVar(nPattern) ) {
            Var var = Var.alloc(nPattern);
            bb.add(var, nData);
            return true;
        }

        // nPattern is <<>> with variables. Is the data a <<>>?
        if ( !nData.isNodeTriple() )
            return false;

        // nData is <<>>, nPattern is <<>>
        // Unpack, match components.
        Triple tPattern = nPattern.getTriple();
        Triple tData = nData.getTriple();
        return bindTriple(bb, tData, tPattern);
    }
}
