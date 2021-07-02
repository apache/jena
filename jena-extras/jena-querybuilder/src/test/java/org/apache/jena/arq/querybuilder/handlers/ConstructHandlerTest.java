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
package org.apache.jena.arq.querybuilder.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Template;
import org.junit.Before;
import org.junit.Test;

public class ConstructHandlerTest extends AbstractHandlerTest {
    private Query query;
    private ConstructHandler handler;

    @Before
    public void setup() {
        query = new Query();
        handler = new ConstructHandler(query);
    }

    @Test
    public void testAddAll() {
        Triple t = new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        ConstructHandler handler2 = new ConstructHandler(new Query());
        handler2.addConstruct(t);
        handler.addAll(handler2);
        Template template = query.getConstructTemplate();
        assertNotNull(template);
        List<Triple> lst = template.getTriples();
        assertEquals(1, lst.size());
        assertEquals(t, lst.get(0));
    }

    @Test
    public void testAddConstruct() {
        Triple t = new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"),
                NodeFactory.createURI("three"));
        handler.addConstruct(t);
        Template template = query.getConstructTemplate();
        assertNotNull(template);
        List<Triple> lst = template.getTriples();
        assertEquals(1, lst.size());
        assertEquals(t, lst.get(0));
    }

    @Test
    public void testSetVars() {
        Var v = Var.alloc("v");
        Triple t = new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), v);
        handler.addConstruct(t);
        Template template = query.getConstructTemplate();
        assertNotNull(template);
        List<Triple> lst = template.getTriples();
        assertEquals(1, lst.size());
        assertEquals(t, lst.get(0));

        Map<Var, Node> values = new HashMap<>();
        values.put(v, NodeFactory.createURI("three"));
        handler.setVars(values);

        template = query.getConstructTemplate();
        assertNotNull(template);
        lst = template.getTriples();
        assertEquals(1, lst.size());
        t = new Triple(NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createURI("three"));
        assertEquals(t, lst.get(0));
    }

}
