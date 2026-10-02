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

package org.apache.jena.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.VarUtils;

public class TestVarUtils {

    private static final Node g = SSE.parseNode(":g");
    private static final Node s = SSE.parseNode(":s");
    private static final Node p = SSE.parseNode(":p");
    private static final Node o = SSE.parseNode(":o");

    private static final Var varG = Var.alloc("g");
    private static final Var varS = Var.alloc("s");
    private static final Var varP = Var.alloc("p");
    private static final Var varO = Var.alloc("o");
    private static final Var varX = Var.alloc("x");

    private static Set<Var> vars(Var...vars) {
        return new LinkedHashSet<>(Arrays.asList(vars));
    }

    // -- getVars(Triple)

    @Test public void getVars_triple_01() {
        Triple triple = SSE.parseTriple("(:s :p :o)");
        assertEquals(vars(), VarUtils.getVars(triple));
    }

    @Test public void getVars_triple_02() {
        Triple triple = SSE.parseTriple("(?s ?p ?o)");
        assertEquals(vars(varS, varP, varO), VarUtils.getVars(triple));
    }

    @Test public void getVars_triple_03() {
        Triple triple = SSE.parseTriple("(?s :p ?o)");
        assertEquals(vars(varS, varO), VarUtils.getVars(triple));
    }

    // Same variable twice - a set, so once.
    @Test public void getVars_triple_04() {
        Triple triple = SSE.parseTriple("(?x :p ?x)");
        assertEquals(vars(varX), VarUtils.getVars(triple));
    }

    // Triple terms (RDF 1.2)
    @Test public void getVars_triple_05() {
        Triple triple = SSE.parseTriple("(?s :p <<( :x :y ?o )>>)");
        assertEquals(vars(varS, varO), VarUtils.getVars(triple));
    }

    @Test public void getVars_triple_06() {
        Triple triple = SSE.parseTriple("(:s :p <<( ?x :y <<( :a :b ?o )>> )>>)");
        assertEquals(vars(varX, varO), VarUtils.getVars(triple));
    }

    // -- addVarsFromTriple

    @Test public void addVarsFromTriple_01() {
        List<Var> acc = new ArrayList<>();
        Triple triple = SSE.parseTriple("(?s :p ?o)");
        VarUtils.addVarsFromTriple(acc, triple);
        assertEquals(List.of(varS, varO), acc);
    }

    // A Collection, not a Set - duplicates and accumulation across calls.
    @Test public void addVarsFromTriple_02() {
        List<Var> acc = new ArrayList<>();
        Triple triple1 = SSE.parseTriple("(?x :p ?x)");
        Triple triple2 = SSE.parseTriple("(?x :p :o)");
        VarUtils.addVarsFromTriple(acc, triple1);
        VarUtils.addVarsFromTriple(acc, triple2);
        assertEquals(List.of(varX, varX, varX), acc);
    }

    @Test public void addVarsFromTriple_03() {
        Set<Var> acc = vars(varG);
        Triple triple = SSE.parseTriple("(?s :p :o)");
        VarUtils.addVarsFromTriple(acc, triple);
        assertEquals(vars(varG, varS), acc);
    }

    // -- addVarsFromQuad

    @Test public void addVarsFromQuad_01() {
        Set<Var> acc = vars();
        Quad quad = SSE.parseQuad("(:g :s :p :o)");
        VarUtils.addVarsFromQuad(acc, quad);
        assertEquals(vars(), acc);
    }

    @Test public void addVarsFromQuad_02() {
        Set<Var> acc = vars();
        Quad quad = SSE.parseQuad("(?g ?s ?p ?o)");
        VarUtils.addVarsFromQuad(acc, quad);
        assertEquals(vars(varG, varS, varP, varO), acc);
    }

    @Test public void addVarsFromQuad_03() {
        Set<Var> acc = vars();
        VarUtils.addVarsFromQuad(acc, Quad.create(varG, s, p, o));
        assertEquals(vars(varG), acc);
    }

    // A quad with no graph - null is skipped, not an error.
    @Test public void addVarsFromQuad_04() {
        Set<Var> acc = vars();
        VarUtils.addVarsFromQuad(acc, Quad.create(null, varS, p, o));
        assertEquals(vars(varS), acc);
    }

    // -- addVarsFromTriplePath

    @Test public void addVarsFromTriplePath_01() {
        Set<Var> acc = vars();
        Path path = SSE.parsePath("(path+ :p)");
        VarUtils.addVarsFromTriplePath(acc, new TriplePath(varS, path, varO));
        assertEquals(vars(varS, varO), acc);
    }

    @Test public void addVarsFromTriplePath_02() {
        Set<Var> acc = vars();
        Path path = SSE.parsePath("(path+ :p)");
        VarUtils.addVarsFromTriplePath(acc, new TriplePath(s, path, o));
        assertEquals(vars(), acc);
    }

    // A TriplePath of a triple: the predicate slot is not a path and is not collected.
    @Test public void addVarsFromTriplePath_03() {
        Set<Var> acc = vars();
        Triple triple = SSE.parseTriple("(?s ?p ?o)");
        TriplePath triplePath = new TriplePath(triple);
        VarUtils.addVarsFromTriplePath(acc, triplePath);
        assertEquals(vars(varS, varP, varO), acc);
    }

    // -- addVar

    @Test public void addVar_01() {
        Set<Var> acc = vars();
        VarUtils.addVar(acc, varX);
        assertEquals(vars(varX), acc);
    }

    // A Node_Variable, not already a Var.
    @Test public void addVar_02() {
        Set<Var> acc = vars();
        VarUtils.addVar(acc, NodeFactory.createVariable("x"));
        assertEquals(vars(varX), acc);
    }

    @Test public void addVar_03() {
        Set<Var> acc = vars();
        VarUtils.addVar(acc, s);
        assertEquals(vars(), acc);
    }

    @Test public void addVar_null() {
        Set<Var> acc = vars();
        VarUtils.addVar(acc, null);
        assertTrue(acc.isEmpty());
    }

    // Node.ANY is not a variable.
    @Test public void addVar_any() {
        Set<Var> acc = vars();
        VarUtils.addVar(acc, Node.ANY);
        assertTrue(acc.isEmpty());
    }

    @Test public void addVar_tripleTerm_01() {
        Set<Var> acc = vars();
        Node tripleTerm = SSE.parseNode("<<( ?s :p ?o )>>");
        VarUtils.addVar(acc, tripleTerm);
        assertEquals(vars(varS, varO), acc);
    }

    @Test public void addVar_tripleTerm_02() {
        Set<Var> acc = vars();
        Node tripleTerm = SSE.parseNode("<<( :s :p :o )>>");
        VarUtils.addVar(acc, tripleTerm);
        assertEquals(vars(), acc);
    }

    // -- addVarNodes

    @Test public void addVarNodes_01() {
        Set<Var> acc = vars();
        VarUtils.addVarNodes(acc, List.of(varS, s, varO, o));
        assertEquals(vars(varS, varO), acc);
    }

    @Test public void addVarNodes_02() {
        Set<Var> acc = vars();
        VarUtils.addVarNodes(acc, List.of());
        assertEquals(vars(), acc);
    }

    @Test public void addVarNodes_null() {
        Set<Var> acc = vars();
        VarUtils.addVarNodes(acc, null);
        assertTrue(acc.isEmpty());
    }

    // -- addVarsTriples

    @Test public void addVarsTriples_01() {
        Set<Var> acc = vars();
        List<Triple> triples = List.of(SSE.parseTriple("(?s :p :o)"), SSE.parseTriple("(:s ?p ?o)"));
        VarUtils.addVarsTriples(acc, triples);
        assertEquals(vars(varS, varP, varO), acc);
    }

    @Test public void addVarsTriples_02() {
        Set<Var> acc = vars();
        VarUtils.addVarsTriples(acc, List.of());
        assertEquals(vars(), acc);
    }

    // -- addVars(acc, BasicPattern)

    @Test public void addVars_bgp_01() {
        Set<Var> acc = vars();
        BasicPattern bgp = SSE.parseBGP("(bgp (?s :p :o) (:s ?p ?o))");
        VarUtils.addVars(acc, bgp);
        assertEquals(vars(varS, varP, varO), acc);
    }

    @Test public void addVars_bgp_02() {
        Set<Var> acc = vars();
        VarUtils.addVars(acc, new BasicPattern());
        assertEquals(vars(), acc);
    }

    // -- addVars(acc, graphNode, BasicPattern)

    @Test public void addVars_graph_bgp_01() {
        Set<Var> acc = vars();
        BasicPattern bgp = SSE.parseBGP("(bgp (?s :p :o))");
        VarUtils.addVars(acc, varG, bgp);
        assertEquals(vars(varG, varS), acc);
    }

    @Test public void addVars_graph_bgp_02() {
        Set<Var> acc = vars();
        BasicPattern bgp = SSE.parseBGP("(bgp (?s :p :o))");
        VarUtils.addVars(acc, g, bgp);
        assertEquals(vars(varS), acc);
    }

    @Test public void addVars_graph_bgp_03() {
        Set<Var> acc = vars();
        BasicPattern bgp = SSE.parseBGP("(bgp (?s :p :o))");
        VarUtils.addVars(acc, null, bgp);
        assertEquals(vars(varS), acc);
    }

    // -- addVars(acc, QuadPattern)

    @Test public void addVars_quadPattern_01() {
        Set<Var> acc = vars();
        QuadPattern quads = new QuadPattern();
        quads.add(Quad.create(varG, s, p, o));
        quads.add(Quad.create(g, varS, p, varO));
        VarUtils.addVars(acc, quads);
        assertEquals(vars(varG, varS, varO), acc);
    }

    @Test public void addVars_quadPattern_02() {
        Set<Var> acc = vars();
        VarUtils.addVars(acc, new QuadPattern());
        assertEquals(vars(), acc);
    }

    // -- addVars(acc, PropFuncArg)

    @Test public void addVars_propFuncArg_node_01() {
        Set<Var> acc = vars();
        VarUtils.addVars(acc, new PropFuncArg(varX));
        assertEquals(vars(varX), acc);
    }

    @Test public void addVars_propFuncArg_node_02() {
        Set<Var> acc = vars();
        VarUtils.addVars(acc, new PropFuncArg(s));
        assertEquals(vars(), acc);
    }

    @Test public void addVars_propFuncArg_list_01() {
        Set<Var> acc = vars();
        VarUtils.addVars(acc, new PropFuncArg(List.of(varS, o, varX)));
        assertEquals(vars(varS, varX), acc);
    }

    @Test public void addVars_propFuncArg_list_02() {
        Set<Var> acc = vars();
        VarUtils.addVars(acc, new PropFuncArg(List.<Node>of()));
        assertEquals(vars(), acc);
    }
}
