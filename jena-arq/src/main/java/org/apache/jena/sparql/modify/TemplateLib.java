/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.modify;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream ;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.lang.LabelToNodeMap;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.sparql.util.NodeUtils;

public class TemplateLib {
    /**
     * Take a template, as a list of quad patterns, a default graph, and an
     * iterator of bindings, and produce an iterator of quads that results from
     * applying the template to the bindings.
     */
    public static Iterator<Quad> template(List<Quad> quads, final Node dftGraph, Iterator<Binding> bindings) {
        if ( quads == null || quads.isEmpty() )
            return null;
        quads = remapDefaultGraph(quads, dftGraph);
        return calcQuads(quads, bindings);
    }

    /**
     * Map quads to a different graph if they are in the default graph.
     */
    public static List<Quad> remapDefaultGraph(List<Quad> quads, final Node dftGraph) {
        // The fast path is "no change"
        if ( quads == null || quads.isEmpty() )
            return quads ;
        if ( dftGraph == null || Quad.isDefaultGraph(dftGraph) )
            return quads ;
        Stream<Quad> remappedStream = quads.stream().map(q->
            !q.isDefaultGraph() ? q : Quad.create(dftGraph, q.getSubject(), q.getPredicate(), q.getObject())
        );
        return remappedStream.toList();
    }

    /** Substitute into triple patterns */
    public static Iterator<Triple> calcTriples(final List<Triple> triples, Iterator<Binding> bindings) {
        Function<Binding, Iterator<Triple>> mapper = new Function<>() {
            Map<Node, Node> bNodeMap = new HashMap<>();

            @Override
            public Iterator<Triple> apply(final Binding b) {
                // Iteration is a new mapping of bnodes.
                bNodeMap.clear();

                List<Triple> tripleList = new ArrayList<>(triples.size());
                for ( Triple triple : triples ) {
                    Triple q = subst(triple, b, bNodeMap);
                    if ( !q.isConcrete() || ! NodeUtils.isValidAsRDF(q.getSubject(), q.getPredicate(), q.getObject()) ) {
                        // Log.warn(TemplateLib.class, "Unbound quad:
                        // "+FmtUtils.stringForQuad(quad)) ;
                        continue;
                    }
                    tripleList.add(q);
                }
                return tripleList.iterator();
            }
        };
        return Iter.flatMap(bindings, mapper);
    }

    /** Substitute into quad patterns */
    public static Iterator<Quad> calcQuads(final List<Quad> quads, Iterator<Binding> bindings) {
        Function<Binding, Iterator<Quad>> mapper = new Function<>() {
            Map<Node, Node> bNodeMap = new HashMap<>();

            @Override
            public Iterator<Quad> apply(final Binding b) {
                // Iteration is a new mapping of bnodes.
                bNodeMap.clear();

                List<Quad> quadList = new ArrayList<>(quads.size());
                for ( Quad quad : quads ) {
                    Quad q = subst(quad, b, bNodeMap);
                    if ( !q.isConcrete() ) {
                        // Log.warn(TemplateLib.class, "Unbound quad:"+FmtUtils.stringForQuad(quad)) ;
                        continue;
                    }
                    quadList.add(q);
                }
                return quadList.iterator();
            }
        };
        return Iter.flatMap(bindings, mapper);
    }

    /** Substitute into a quad, with rewriting of bNodes */
    public static Quad subst(Quad quad, Binding b, Map<Node, Node> bNodeMap) {
        Node g = quad.getGraph();
        Node s = quad.getSubject();
        Node p = quad.getPredicate();
        Node o = quad.getObject();

        Node g1 = g;
        Node s1 = s;
        Node p1 = p;
        Node o1 = o;

        // replace blank nodes.
        if ( g1.isBlank() || Var.isBlankNodeVar(g1) )
            g1 = newBlank(g1, bNodeMap);

        if ( s1.isBlank() || Var.isBlankNodeVar(s1) )
            s1 = newBlank(s1, bNodeMap);

        if ( p1.isBlank() || Var.isBlankNodeVar(p1) )
            p1 = newBlank(p1, bNodeMap);

        if ( o1.isBlank() || Var.isBlankNodeVar(o1) )
            o1 = newBlank(o1, bNodeMap);

        Quad q = quad;
        if ( s1 != s || p1 != p || o1 != o || g1 != g )
            q = Quad.create(g1, s1, p1, o1);
        Quad q2 = Substitute.substitute(q, b);
        return q2;
    }

    /** Substitute into a triple, with rewriting of bNodes */
    public static Triple subst(Triple triple, Binding b, Map<Node, Node> bNodeMap) {
        Node s = triple.getSubject();
        Node p = triple.getPredicate();
        Node o = triple.getObject();

        Node s1 = s;
        Node p1 = p;
        Node o1 = o;

        if ( s1.isBlank() || Var.isBlankNodeVar(s1) )
            s1 = newBlank(s1, bNodeMap);

        if ( p1.isBlank() || Var.isBlankNodeVar(p1) )
            p1 = newBlank(p1, bNodeMap);

        if ( o1.isBlank() || Var.isBlankNodeVar(o1) )
            o1 = newBlank(o1, bNodeMap);

        Triple t = triple;
        if ( s1 != s || p1 != p || o1 != o )
            t = Triple.create(s1, p1, o1);
        Triple t2 = Substitute.substitute(t, b);
        return t2;
    }

    /** generate a blank node consistently */
    private static Node newBlank(Node n, Map<Node, Node> bNodeMap) {
        if ( !bNodeMap.containsKey(n) )
            bNodeMap.put(n, NodeFactory.createBlankNode());
        return bNodeMap.get(n);
    }

    // ---- Template to query pattern
    // Used by CONSTRUCTWHERE to convert a template into a query pattern of the same shape.

    public static ElementGroup templateToQueryPattern(Template template){
        ElementGroup elg = new ElementGroup();
        Map<Node, BasicPattern> graphs = template.getGraphPattern();
        for(Node gn: graphs.keySet()){
            BasicPattern bgp = graphs.get(gn);
            BasicPattern bgp2 = templateToQuery(bgp);
            Element el = new ElementPathBlock(bgp2);
            if(! Quad.defaultGraphNodeGenerated.equals(gn) ){
                ElementGroup e = new ElementGroup();
                e.addElement(el);
                el = new ElementNamedGraph(gn, e);
            }
            elg.addElement(el);
        }
        return elg;
    }

    private static BasicPattern templateToQuery(BasicPattern bgp) {
        BasicPattern bgp2 = new BasicPattern();
        // Create anon vars.
        LabelToNodeMap mapper = LabelToNodeMap.createVarMap();
        Map<Node, Node> blankNodeReplacements = new HashMap<>();
        for ( Triple t : bgp.getList() ) {
            Node s = t.getSubject();
            Node s2 = templateNodeToQueryPatternNode(s, mapper);
            Node p = t.getPredicate();
            Node p2 = templateNodeToQueryPatternNode(p, mapper);
            Node o = t.getObject();
            Node o2 = templateNodeToQueryPatternNode(o, mapper);
            if ( s == s2 && p == p2 && o == o2 ) {
                bgp2.add(t);
                continue;
            }
            Triple t2 = Triple.create(s2, p2, o2);
            bgp2.add(t2);
        }
        return bgp2;
    }

    // Rename blank nodes as anon variables for a query pattern.
    private static Node templateNodeToQueryPatternNode(Node n, LabelToNodeMap map) {
        if ( ! n.isBlank() )
            return n;
        Node n2 = map.asNode(n.getBlankNodeLabel());
        return n2;
    }
}
