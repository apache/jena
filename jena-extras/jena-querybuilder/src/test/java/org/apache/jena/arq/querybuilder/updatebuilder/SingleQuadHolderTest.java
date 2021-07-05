/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.updatebuilder;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

public class SingleQuadHolderTest {

    private SingleQuadHolder holder;

    @Test
    public void getQuads_Quad() {
        Node g = NodeFactory.createURI("g");
        Node s = NodeFactory.createURI("s");
        Node p = NodeFactory.createURI("p");
        Node o = NodeFactory.createURI("o");
        Quad quad = new Quad(g, s, p, o);
        holder = new SingleQuadHolder(quad);
        List<Quad> lst = holder.getQuads().toList();
        assertEquals(1, lst.size());
        assertEquals(quad, lst.get(0));
    }

    @Test
    public void getQuads_Quad_vars() {
        Node g = NodeFactory.createURI("g");
        Node s = NodeFactory.createURI("s");
        Node p = NodeFactory.createVariable("p");
        Node o = NodeFactory.createURI("o");
        Quad quad = new Quad(g, s, p, o);
        holder = new SingleQuadHolder(quad);
        List<Quad> lst = holder.getQuads().toList();
        assertEquals(1, lst.size());
        assertEquals(quad, lst.get(0));

        Map<Var, Node> map = new HashMap<>();
        Node p2 = NodeFactory.createURI("p2");
        map.put(Var.alloc(p), p2);
        holder.setValues(map);
        Quad quad2 = new Quad(g, s, p2, o);
        lst = holder.getQuads().toList();
        assertEquals(1, lst.size());
        assertEquals(quad2, lst.get(0));
    }

    @Test
    public void getQuads_Triple() {

        Node s = NodeFactory.createURI("s");
        Node p = NodeFactory.createURI("p");
        Node o = NodeFactory.createURI("o");
        Triple triple = new Triple(s, p, o);
        Quad quad = new Quad(Quad.defaultGraphNodeGenerated, s, p, o);
        holder = new SingleQuadHolder(triple);
        List<Quad> lst = holder.getQuads().toList();
        assertEquals(1, lst.size());
        assertEquals(quad, lst.get(0));
    }

    @Test
    public void getQuads_Triple_vars() {
        Node s = NodeFactory.createURI("s");
        Node p = NodeFactory.createVariable("p");
        Node o = NodeFactory.createURI("o");
        Triple triple = new Triple(s, p, o);
        Quad quad = new Quad(Quad.defaultGraphNodeGenerated, s, p, o);
        holder = new SingleQuadHolder(triple);
        List<Quad> lst = holder.getQuads().toList();
        assertEquals(1, lst.size());
        assertEquals(quad, lst.get(0));

        Map<Var, Node> map = new HashMap<>();
        Node p2 = NodeFactory.createURI("p2");
        map.put(Var.alloc(p), p2);
        holder.setValues(map);
        Quad quad2 = new Quad(Quad.defaultGraphNodeGenerated, s, p2, o);
        lst = holder.getQuads().toList();
        assertEquals(1, lst.size());
        assertEquals(quad2, lst.get(0));
    }

}
