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
package org.apache.jena.permissions.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.sparql.core.Var;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SecuredItemImplTest {
    private SecurityEvaluator evaluator = mock(SecurityEvaluator.class);
    private ItemHolder<?, ?> holder = mock(ItemHolder.class);
    private ArgumentCaptor<Object> principal;
    private ArgumentCaptor<Action> action;
    private ArgumentCaptor<Node> modelNode;
    private ArgumentCaptor<Triple> triple;
    private SecuredItemImpl securedItemImpl = new SecuredItemImpl(evaluator, "urn:name", holder) {
    };
    private static String PRINCIPAL = "principal";

    @Before
    public void setup() {
        principal = ArgumentCaptor.forClass(Object.class);
        action = ArgumentCaptor.forClass(Action.class);
        modelNode = ArgumentCaptor.forClass(Node.class);
        triple = ArgumentCaptor.forClass(Triple.class);
    }

    @Test
    public void canRead() {
        // Triple.ANY
        when(evaluator.getPrincipal()).thenReturn(PRINCIPAL);
        when(evaluator.evaluate(any(), any(SecurityEvaluator.Action.class), any(Node.class), any(Triple.class)))
                .thenReturn(Boolean.TRUE);
        assertTrue(securedItemImpl.canRead(Triple.ANY));
        verify(evaluator).evaluate(principal.capture(), action.capture(), modelNode.capture(), triple.capture());

        Triple t = triple.getValue();
        assertEquals(Node.ANY, t.getSubject());
        assertEquals(Node.ANY, t.getPredicate());
        assertEquals(Node.ANY, t.getObject());

        Node n = modelNode.getValue();
        assertEquals(NodeFactory.createURI("urn:name"), n);

        Object p = principal.getValue();
        assertEquals(PRINCIPAL, p);

        Action a = action.getValue();
        assertEquals(Action.Read, a);

        reset(evaluator);

        // FUTURE ANY Variable
        when(evaluator.getPrincipal()).thenReturn(PRINCIPAL);
        when(evaluator.evaluate(any(), any(SecurityEvaluator.Action.class), any(Node.class), any(Triple.class)))
                .thenReturn(Boolean.TRUE);
        Triple target = Triple.create(SecurityEvaluator.FUTURE, Node.ANY, Var.alloc("var"));
        assertTrue(securedItemImpl.canRead(target));
        verify(evaluator).evaluate(principal.capture(), action.capture(), modelNode.capture(), triple.capture());

        t = triple.getValue();
        assertEquals(SecurityEvaluator.FUTURE, t.getSubject());
        assertEquals(Node.ANY, t.getPredicate());
        assertEquals(SecurityEvaluator.VARIABLE, t.getObject());

        n = modelNode.getValue();
        assertEquals(NodeFactory.createURI("urn:name"), n);

        p = principal.getValue();
        assertEquals(PRINCIPAL, p);

        a = action.getValue();
        assertEquals(Action.Read, a);
    }

    @Test
    public void canCreate() {
        // Triple.ANY
        when(evaluator.getPrincipal()).thenReturn(PRINCIPAL);
        when(evaluator.evaluate(any(), any(SecurityEvaluator.Action.class), any(Node.class), any(Triple.class)))
                .thenReturn(Boolean.TRUE);
        assertTrue(securedItemImpl.canCreate(Triple.ANY));
        verify(evaluator).evaluate(principal.capture(), action.capture(), modelNode.capture(), triple.capture());

        Triple t = triple.getValue();
        assertEquals(Node.ANY, t.getSubject());
        assertEquals(Node.ANY, t.getPredicate());
        assertEquals(Node.ANY, t.getObject());

        Node n = modelNode.getValue();
        assertEquals(NodeFactory.createURI("urn:name"), n);

        Object p = principal.getValue();
        assertEquals(PRINCIPAL, p);

        Action a = action.getValue();
        assertEquals(Action.Create, a);

        reset(evaluator);

        // FUTURE ANY Variable
        when(evaluator.getPrincipal()).thenReturn(PRINCIPAL);
        when(evaluator.evaluate(any(), any(SecurityEvaluator.Action.class), any(Node.class), any(Triple.class)))
                .thenReturn(Boolean.TRUE);
        Triple target = Triple.create(SecurityEvaluator.FUTURE, Node.ANY, Var.alloc("var"));
        assertTrue(securedItemImpl.canCreate(target));
        verify(evaluator).evaluate(principal.capture(), action.capture(), modelNode.capture(), triple.capture());

        t = triple.getValue();
        assertEquals(SecurityEvaluator.FUTURE, t.getSubject());
        assertEquals(Node.ANY, t.getPredicate());
        assertEquals(SecurityEvaluator.VARIABLE, t.getObject());

        n = modelNode.getValue();
        assertEquals(NodeFactory.createURI("urn:name"), n);

        p = principal.getValue();
        assertEquals(PRINCIPAL, p);

        a = action.getValue();
        assertEquals(Action.Create, a);
    }

    @Test
    public void canUpdate() {
        // Triple.ANY
        when(evaluator.getPrincipal()).thenReturn(PRINCIPAL);
        when(evaluator.evaluateUpdate(any(), any(Node.class), any(Triple.class), any(Triple.class)))
                .thenReturn(Boolean.TRUE);
        assertTrue(securedItemImpl.canUpdate(Triple.ANY, Triple.ANY));
        verify(evaluator).evaluateUpdate(principal.capture(), modelNode.capture(), triple.capture(), any());

        Triple t = triple.getValue();
        assertEquals(Node.ANY, t.getSubject());
        assertEquals(Node.ANY, t.getPredicate());
        assertEquals(Node.ANY, t.getObject());

        Node n = modelNode.getValue();
        assertEquals(NodeFactory.createURI("urn:name"), n);

        Object p = principal.getValue();
        assertEquals(PRINCIPAL, p);

        reset(evaluator);

        // FUTURE ANY Variable
        when(evaluator.getPrincipal()).thenReturn(PRINCIPAL);
        when(evaluator.evaluateUpdate(any(), any(Node.class), any(Triple.class), any(Triple.class)))
                .thenReturn(Boolean.TRUE);
        Triple target = Triple.create(SecurityEvaluator.FUTURE, Node.ANY, Var.alloc("var"));
        assertTrue(securedItemImpl.canUpdate(target, Triple.ANY));
        verify(evaluator).evaluateUpdate(principal.capture(), modelNode.capture(), triple.capture(), any());

        t = triple.getValue();
        assertEquals(SecurityEvaluator.FUTURE, t.getSubject());
        assertEquals(Node.ANY, t.getPredicate());
        assertEquals(SecurityEvaluator.VARIABLE, t.getObject());

        n = modelNode.getValue();
        assertEquals(NodeFactory.createURI("urn:name"), n);

        p = principal.getValue();
        assertEquals(PRINCIPAL, p);
    }

    @Test
    public void canDelete() {
        // Triple.ANY
        when(evaluator.getPrincipal()).thenReturn(PRINCIPAL);
        when(evaluator.evaluate(any(), any(SecurityEvaluator.Action.class), any(Node.class), any(Triple.class)))
                .thenReturn(Boolean.TRUE);
        assertTrue(securedItemImpl.canDelete(Triple.ANY));
        verify(evaluator).evaluate(principal.capture(), action.capture(), modelNode.capture(), triple.capture());

        Triple t = triple.getValue();
        assertEquals(Node.ANY, t.getSubject());
        assertEquals(Node.ANY, t.getPredicate());
        assertEquals(Node.ANY, t.getObject());

        Node n = modelNode.getValue();
        assertEquals(NodeFactory.createURI("urn:name"), n);

        Object p = principal.getValue();
        assertEquals(PRINCIPAL, p);

        Action a = action.getValue();
        assertEquals(Action.Delete, a);

        reset(evaluator);

        // FUTURE ANY Variable
        when(evaluator.getPrincipal()).thenReturn(PRINCIPAL);
        when(evaluator.evaluate(any(), any(SecurityEvaluator.Action.class), any(Node.class), any(Triple.class)))
                .thenReturn(Boolean.TRUE);
        Triple target = Triple.create(SecurityEvaluator.FUTURE, Node.ANY, Var.alloc("var"));
        assertTrue(securedItemImpl.canDelete(target));
        verify(evaluator).evaluate(principal.capture(), action.capture(), modelNode.capture(), triple.capture());

        t = triple.getValue();
        assertEquals(SecurityEvaluator.FUTURE, t.getSubject());
        assertEquals(Node.ANY, t.getPredicate());
        assertEquals(SecurityEvaluator.VARIABLE, t.getObject());

        n = modelNode.getValue();
        assertEquals(NodeFactory.createURI("urn:name"), n);

        p = principal.getValue();
        assertEquals(PRINCIPAL, p);

        a = action.getValue();
        assertEquals(Action.Delete, a);
    }
}
