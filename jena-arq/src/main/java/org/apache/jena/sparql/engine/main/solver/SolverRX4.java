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
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

/** RDF-star - quad form. Solve patterns when there is an embedded triple with variables. */
public class SolverRX4 {

    /**
     * This constant is not public API. It is exposed only so integration testing can
     * check the value for a release build.
     */
    public static final boolean DATAPATH = true;

    public static Iterator<Binding> rdfStarQuad(Iterator<Binding> chain, Quad pattern, ExecutionContext execCxt) {
        return rdfStarQuad(chain, pattern.getGraph(), pattern.asTriple(), execCxt) ;
    }

    public static Iterator<Binding> rdfStarQuad(Iterator<Binding> chain, Node graphName, Triple pattern, ExecutionContext execCxt) {
        // Should all work without this trap for plain RDF.
        if ( DATAPATH ) {
            if ( ! tripleHasEmbTripleWithVars(pattern) &&
                    (graphName == null || ! graphName.isNodeTriple() ) )
                // No variables inside <<>>
                return matchDataQuad(chain, graphName, pattern, execCxt);
        }
        return rdfStarQuadSub(chain, graphName, pattern, execCxt);
    }

    private static Iterator<Binding> rdfStarQuadSub(Iterator<Binding> chain, Node graphName, Triple pattern, ExecutionContext execCxt) {
        Iterator<Binding> matches = Iter.flatMap(chain, binding->rdfStarQuadSub(binding, graphName, pattern, execCxt));
        return matches;
    }

    private static Iterator<Binding> rdfStarQuadSub(Binding binding, Node xGraphName, Triple xPattern, ExecutionContext execCxt) {
        Triple tPattern = Substitute.substitute(xPattern, binding);
        Node graphName = Substitute.substitute(xGraphName, binding);
        Node g = (graphName == null) ? Quad.defaultGraphIRI : nodeTopLevel(graphName);
        Node s = nodeTopLevel(tPattern.getSubject());
        Node p = nodeTopLevel(tPattern.getPredicate());
        Node o = nodeTopLevel(tPattern.getObject());
        DatasetGraph dsg = execCxt.getDataset();
        Iterator<Quad> dataIter = dsg.find(g, s, p, o);
        Iterator<Binding> matched = Iter.map(dataIter, qData->matchQuad(binding, qData, g, tPattern));
        return Iter.removeNulls(matched);
    }

    private static final Predicate<Quad> NoFilter = null;
    private static final boolean NoAnyGraph = false;

    private static Iterator<Binding> matchDataQuad(Iterator<Binding> chain, Node graphName, Triple pattern, ExecutionContext execCxt) {
        Iterator<Binding> matches = StageMatchData.accessQuad(chain, graphName, pattern, NoFilter, NoAnyGraph, execCxt);
        return matches;
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
    }

    /** Match data "qData" against "tGraphNode, tPattern", including RDF-star terms. */
    public static Binding matchQuad(Binding input, Quad qData, Node tGraphNode, Triple tPattern) {
        BindingBuilder bb = Binding.builder(input);
        boolean r = matchQuad(bb, qData, tGraphNode, tPattern);
        return r ? bb.build() : null;
    }

    private static boolean matchQuad(BindingBuilder bb, Quad qData, Node tGraphNode, Triple tPattern) {
        Node sPattern = tPattern.getSubject();
        Node pPattern = tPattern.getPredicate();
        Node oPattern = tPattern.getObject();

        Node gData = qData.getGraph();
        Node sData = qData.getSubject();
        Node pData = qData.getPredicate();
        Node oData = qData.getObject();

        if ( ! match(bb, gData, tGraphNode) )
            return false;
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
        return SolverRX3.bindTriple(bb, tData, tPattern);
    }
}
