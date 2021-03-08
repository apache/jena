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

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.util.iterator.ExtendedIterator;

/** RDF-star - when there is an embedded triple with variables. */
public class RX {
    // Note: running with DATAPATH = false

    // GraphMem is sameValueAs (historical)
    // and tests use GraphPlain, which is langtag case insensitive (There is one DAWG test that needs this).

    // If running in development with "DATAPATH = false" two failures ("lang-3", run twice).

    /**
     * This constant is not public API. It is exposed only so integration testing can
     * check the value for a release build.
     */
    public static final boolean DATAPATH = true;

    public static QueryIterator rdfStarTriple(QueryIterator chain, Triple pattern, ExecutionContext execCxt) {
        // Should all work without this trap for plain RDF.
        if ( DATAPATH ) {
            if ( ! tripleHasNodeTriple(pattern) ) {
                // No RDF-star : direct to data.
                return matchData(chain, pattern, execCxt);
            }
        }
        return rdfStarTripleSub(chain, pattern, execCxt);
    }

    /**
     * Match the graph with a triple pattern.
     * This is the accessor to the graph.
     * It assumes any triple terms have been dealt with.
     */
    private static QueryIterator matchData(QueryIterator chain, Triple pattern, ExecutionContext execCxt) {
        return new QueryIterTriplePattern(chain, pattern, execCxt);
    }

    /**
     * Test whether a triple has an triple term as one of its components.
     */
    private static boolean tripleHasNodeTriple(Triple triple) {
        return triple.getSubject().isNodeTriple()
               /*|| triple.getPredicate().isNodeTriple()*/
               || triple.getObject().isNodeTriple();
    }

    private static QueryIterator rdfStarTripleSub(QueryIterator input, Triple pattern, ExecutionContext execCxt) {
        Iterator<Binding> matches = Iter.flatMap(input, binding->rdfStarTripleSub(binding, pattern, execCxt));
        return new QueryIterPlainWrapper(matches, execCxt);
    }

    private static Iterator<Binding> rdfStarTripleSub(Binding input, Triple tPattern, ExecutionContext execCxt) {
        Node s = nodeTopLevel(tPattern.getSubject());
        Node p = nodeTopLevel(tPattern.getPredicate());
        Node o = nodeTopLevel(tPattern.getObject());
        Graph graph = execCxt.getActiveGraph();
        ExtendedIterator<Triple> graphIter = graph.find(s, p, o);
        ExtendedIterator<Binding> matched = graphIter.mapWith(tData->matchTriple(input, tData, tPattern)).filterDrop(Objects::isNull);
        return matched;
    }

    /** Convert a pattern node into ANY, or leave as a constant term. */
    public static Node nodeTopLevel(Node node) {
        // Public so TDB code can use it.
        if ( Var.isVar(node) )
            return Node.ANY;
        if ( node.isNodeTriple() ) { //|| node.isNodeGraph() )
            if ( ! node.getTriple().isConcrete() )
                // Nested variables.
                return Node.ANY;
        }
        return node;
    }

    public static Binding matchTriple(Binding input, Triple tData, Triple tPattern) {
        BindingBuilder bb = Binding.builder(input);
        boolean r = matchTriple(bb, tData, tPattern);
        return r ? bb.build() : null;
    }

    private static boolean matchTriple(BindingBuilder bb, Triple tData, Triple tPattern) {
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
            return nPattern.equals(nData);
        }

        // Easy case - nPattern is a variable.
        if ( Var.isVar(nPattern) ) {
            Var var = Var.alloc(nPattern);
            bb.add(var,  nData);
            return true;
        }

        // nPattern is <<>> with variables. Is the data a <<>>?
        if ( ! nData.isNodeTriple() )
            return false;

        // nData is <<>>, nPattern is <<>>
        // Unpack, match components.
        Triple tPattern = nPattern.getTriple();
        Triple tData = nData.getTriple();
        return matchTriple(bb, tData, tPattern);
    }
}
