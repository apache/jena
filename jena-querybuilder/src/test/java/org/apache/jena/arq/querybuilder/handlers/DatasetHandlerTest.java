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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;

public class DatasetHandlerTest extends AbstractHandlerTest {
    private Query query;
    private DatasetHandler handler;

    @BeforeEach
    public void setup() {
        query = new Query();
        handler = new DatasetHandler(query);
    }

    @Test
    public void testAddAll() {
        DatasetHandler handler2 = new DatasetHandler(new Query());
        handler2.from("foo");
        handler2.fromNamed("bar");
        handler.addAll(handler2);
        String s = query.toString();
        assertTrue(s.contains("FROM <foo>"));
        assertTrue(s.contains("FROM NAMED <bar>"));
    }

    @Test
    public void testFromNamedString() {
        handler.fromNamed("foo");
        assertTrue(query.toString().contains("FROM NAMED <foo>"));
    }

    @Test
    public void fromNamedCollection() {
        String[] names = { "foo", "bar" };
        handler.fromNamed(Arrays.asList(names));
        String s = query.toString();
        assertTrue(s.contains("FROM NAMED <foo>"));
        assertTrue(s.contains("FROM NAMED <bar>"));
    }

    @Test
    public void fromString() {
        handler.from("foo");
        assertTrue(query.toString().contains("FROM <foo>"));
    }

    @Test
    public void asGraphNameTest() throws URISyntaxException {
        assertEquals("urn:example.com:uri", handler.asGraphName(new URI("urn:example.com:uri")));
        assertEquals("urn:example.com:node", handler.asGraphName(NodeFactory.createURI("urn:example.com:node")));
        assertEquals("five", handler.asGraphName(ResourceFactory.createPlainLiteral("five")));
        assertEquals("6", handler.asGraphName(ResourceFactory.createTypedLiteral(6)));
        Node n = NodeFactory.createBlankNode();
        assertEquals(n.toString(), handler.asGraphName(n));
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), handler.asGraphName(uuid));
    }

    @Test
    public void fromStringCollection() {
        String[] names = { "foo", "bar" };
        handler.from(Arrays.asList(names));
        assertTrue(query.toString().contains("FROM <foo>"));
        assertTrue(query.toString().contains("FROM <bar>"));
    }

    @Test
    public void setVarsFromNamed() {
        Map<Var, Node> values = new HashMap<>();
        handler.fromNamed("?foo");
        handler.from("?bar");
        values.put(Var.alloc("foo"), NodeFactory.createURI("http://example.com/foo"));
        handler.setVars(values);
        String s = query.toString();
        assertTrue(s.contains("FROM NAMED <http://example.com/foo>"));
        assertTrue(s.contains("FROM <?bar>"));
    }

    @Test
    public void setVarsFrom() {
        Map<Var, Node> values = new HashMap<>();
        handler.fromNamed("?foo");
        handler.from("?bar");
        values.put(Var.alloc("bar"), NodeFactory.createURI("http://example.com/bar"));
        handler.setVars(values);
        String s = query.toString();
        assertTrue(s.contains("FROM NAMED <?foo>"));
        assertTrue(s.contains("FROM <http://example.com/bar>"));
    }

    @Test
    public void setVarsBoth() {
        Map<Var, Node> values = new HashMap<>();
        handler.fromNamed("?foo");
        handler.from("?bar");
        values.put(Var.alloc("bar"), NodeFactory.createURI("http://example.com/bar"));
        values.put(Var.alloc("foo"), NodeFactory.createURI("http://example.com/foo"));
        handler.setVars(values);
        String s = query.toString();
        assertTrue(s.contains("FROM NAMED <http://example.com/foo>"));
        assertTrue(s.contains("FROM <http://example.com/bar>"));
    }

}
