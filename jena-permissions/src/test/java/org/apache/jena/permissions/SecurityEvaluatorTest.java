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
package org.apache.jena.permissions;

import static org.junit.Assert.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.junit.Test;

import java.util.EnumSet;

public class SecurityEvaluatorTest {
    private static final Object PRINCIPAL = null;
    private static final Node GRAPH = null;
    private static final Triple TRIPLE = null;
    private static final EnumSet<Action> ALLOWED_ACTIONS = EnumSet.of(Action.Create, Action.Delete);
    private static final EnumSet<Action> DISALLOWED_ACTIONS = EnumSet.complementOf(ALLOWED_ACTIONS);
    public static final EnumSet<Action> ALL_ACTIONS = EnumSet.noneOf(Action.class);


    private final SecurityEvaluator evaluator = new SecurityEvaluator() {

        @Override
        public boolean evaluate(Object principal, Action action, Node graphIRI) throws AuthenticationRequiredException {
            return ALLOWED_ACTIONS.contains(action);
        }

        @Override
        public boolean evaluate(Object principal, Action action, Node graphIRI, Triple triple) throws AuthenticationRequiredException {
            return ALLOWED_ACTIONS.contains(action);
        }

        @Override
        public Object getPrincipal() {
            return null;
        }

        @Override
        public boolean isPrincipalAuthenticated(Object principal) {
            return false;
        }
    };

    @Test
    public void testAllLogicForGraphOperations() {
        assertTrue(evaluator.evaluate(PRINCIPAL, ALL_ACTIONS, GRAPH));
        assertTrue(evaluator.evaluate(PRINCIPAL, ALLOWED_ACTIONS, GRAPH));
        assertFalse(evaluator.evaluate(PRINCIPAL, DISALLOWED_ACTIONS, GRAPH));
        assertFalse(evaluator.evaluate(PRINCIPAL, EnumSet.allOf(Action.class), GRAPH));
    }


    @Test
    public void testAnyLogicForGraphOperations() {
        assertFalse(evaluator.evaluateAny(PRINCIPAL, ALL_ACTIONS, GRAPH));
        assertTrue(evaluator.evaluateAny(PRINCIPAL, ALLOWED_ACTIONS, GRAPH));
        assertFalse(evaluator.evaluateAny(PRINCIPAL, DISALLOWED_ACTIONS, GRAPH));
        assertTrue(evaluator.evaluateAny(PRINCIPAL, EnumSet.allOf(Action.class), GRAPH));
    }

    public void testAllLogicForTripleOperations() {
        assertTrue(evaluator.evaluate(PRINCIPAL, ALL_ACTIONS, GRAPH, TRIPLE));
        assertTrue(evaluator.evaluate(PRINCIPAL, ALLOWED_ACTIONS, GRAPH, TRIPLE));
        assertFalse(evaluator.evaluate(PRINCIPAL, DISALLOWED_ACTIONS, GRAPH, TRIPLE));
        assertFalse(evaluator.evaluate(PRINCIPAL, EnumSet.allOf(Action.class), GRAPH, TRIPLE));
    }


    @Test
    public void testAnyLogicForTripleOperations() {
        assertFalse(evaluator.evaluateAny(PRINCIPAL, ALL_ACTIONS, GRAPH, TRIPLE));
        assertTrue(evaluator.evaluateAny(PRINCIPAL, ALLOWED_ACTIONS, GRAPH, TRIPLE));
        assertFalse(evaluator.evaluateAny(PRINCIPAL, DISALLOWED_ACTIONS, GRAPH, TRIPLE));
        assertTrue(evaluator.evaluateAny(PRINCIPAL, EnumSet.allOf(Action.class), GRAPH, TRIPLE));
    }
}
