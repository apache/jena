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
package org.apache.jena.permissions.graph;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.jena.graph.*;
import org.apache.jena.permissions.EqualityTester;
import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class MemGraphTest {
    private SecuredGraph securedGraph;
    private final MockSecurityEvaluator securityEvaluator;
    private Node s;
    private Node p;
    private Node o;
    private Triple t;

    private Graph baseGraph;

    public MemGraphTest(final MockSecurityEvaluator securityEvaluator) {
        this.securityEvaluator = securityEvaluator;
    }

    protected Graph createGraph() throws Exception {
        return GraphFactory.createDefaultGraph();
    }

    private boolean shouldRead() {
        return securityEvaluator.evaluate(Action.Read) || !securityEvaluator.isHardReadError();
    }

    @Before
    public void setUp() {
        try {
            baseGraph = createGraph();
        } catch (Exception e) {
            throw new RuntimeException("Setup error", e);
        }
        baseGraph.remove(Node.ANY, Node.ANY, Node.ANY);
        baseGraph.getPrefixMapping().setNsPrefix("foo", "http://example.com/foo/");
        securedGraph = org.apache.jena.permissions.Factory.getInstance(securityEvaluator,
                "http://example.com/securedGraph", baseGraph);
        s = NodeFactory.createURI("http://example.com/securedGraph/s");
        p = NodeFactory.createURI("http://example.com/securedGraph/p");
        o = NodeFactory.createURI("http://example.com/securedGraph/o");
        t = new Triple(s, p, o);
        baseGraph.add(t);
    }

    @Test
    public void testContainsNodes() throws Exception {
        try {
            boolean result = securedGraph.contains(s, p, o);
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.assertTrue(result);
            } else {
                Assert.assertFalse(result);
            }
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testContainsTriple() throws Exception {
        try {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.assertTrue(securedGraph.contains(t));
            } else {
                Assert.assertFalse(securedGraph.contains(t));
            }

            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

    @Test
    public void testDelete() throws Exception {
        final Set<Action> UD = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });
        try {
            securedGraph.delete(t);

            if (!securityEvaluator.evaluate(UD)) {
                Assert.fail("Should have thrown AccessDenied Exception");
            }
            Assert.assertEquals(0, baseGraph.size());

        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(UD)) {
                Assert.fail(String.format("Should not have thrown AccessDenied Exception: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testDependsOn() throws Exception {
        try {
            Assert.assertFalse(securedGraph.dependsOn(GraphFactory.createDefaultGraph()));
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
        try {
            boolean result = securedGraph.dependsOn(baseGraph);
            if (securedGraph.canRead()) {
                Assert.assertTrue(result);
            } else {
                Assert.assertFalse(result);
            }
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testFindNodes() throws Exception {
        try {
            List<Triple> result = securedGraph.find(Node.ANY, Node.ANY, Node.ANY).toList();
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.assertFalse(result.isEmpty());
            } else {
                Assert.assertTrue(result.isEmpty());
            }

            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testFindTriple() throws Exception {
        try {

            List<Triple> lst = securedGraph.find(t).toList();
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.assertFalse(lst.isEmpty());
            } else {
                Assert.assertTrue(lst.isEmpty());
            }
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testGetPrefixMapping() throws Exception {
        SecuredPrefixMappingTest.runTests(securityEvaluator, new Supplier<PrefixMapping>() {
            @Override
            public PrefixMapping get() {
                setUp();
                return securedGraph.getPrefixMapping();
            }
        }, baseGraph.getPrefixMapping().getNsPrefixMap());
    }

    @Test
    public void testInequality() {
        EqualityTester.testInequality("proxy and base", securedGraph, baseGraph);
        final Graph g2 = org.apache.jena.permissions.graph.impl.Factory.getInstance(securityEvaluator,
                "http://example.com/securedGraph", baseGraph);
        EqualityTester.testEquality("proxy and proxy2", securedGraph, g2);
        EqualityTester.testInequality("base and proxy2", baseGraph, g2);
    }

    @Test
    public void testIsIsomorphicWith_Not() throws Exception {

        try {
            boolean actual = securedGraph.isIsomorphicWith(GraphFactory.createDefaultGraph());
            if (securityEvaluator.evaluate(Action.Read)) {
                assertFalse(actual);
            } else {
                assertTrue(actual);
            }
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

    @Test
    public void testIsIsomorphicWith_Is() throws Exception {
        try {
            boolean actual = securedGraph.isIsomorphicWith(baseGraph);
            if (securityEvaluator.evaluate(Action.Read)) {
                assertTrue(actual);
            } else {
                assertFalse(actual);
            }
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testSize() throws Exception {
        int expected = securityEvaluator.evaluate(Action.Read) ? 1 : 0;
        try {
            Assert.assertEquals(expected, securedGraph.size());
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }
}
