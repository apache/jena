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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Assert;
import org.apache.jena.graph.Graph;
import org.apache.jena.permissions.Factory;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredPrefixMappingTest {
    /**
     * Run tests from other classes that have instances of prefix mapping
     * 
     * @param securityEvaluator the SecurityEvaluator for the tests
     * @param supplier          The supplier for the prefixMapping to test.
     * @param aBaseMap          The map of prefix to URL that should be in the
     *                          supplier map.
     * @throws Exception on error
     */
    public static void runTests(final SecurityEvaluator securityEvaluator, final Supplier<PrefixMapping> supplier,
            final Map<String, String> aBaseMap) throws Exception {

        final SecuredPrefixMappingTest pmTest = new SecuredPrefixMappingTest(securityEvaluator) {
            private Map<String, String> theBaseMap = aBaseMap;
            private Supplier<PrefixMapping> theSupplier = supplier;

            @Override
            public void setup() {
                PrefixMapping pm = supplier.get();
                Assert.assertNotNull("PrefixMapping may not be null", pm);
                Assert.assertTrue("PrefixMapping should be secured", pm instanceof SecuredPrefixMapping);

                this.securedMapping = (SecuredPrefixMapping) pm;
                this.baseMap.clear();
                this.baseMap.putAll(theBaseMap);
            }
        };
        Method lockTest = null;
        for (final Method m : pmTest.getClass().getMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                // lock test must come last
                if (m.getName().equals("testLock")) {
                    lockTest = m;
                } else {
                    pmTest.setup();
                    try {
                        m.invoke(pmTest);
                    } catch (InvocationTargetException e) {
                        e.getCause().printStackTrace();
                        throw e;
                    }
                }

            }
        }
        Assert.assertNotNull("Did not find 'testLock' method", lockTest);
        pmTest.setup();
        lockTest.invoke(pmTest);

    }

    private final SecurityEvaluator securityEvaluator;
    private final Object principal;

    protected final Map<String, String> baseMap;
    protected SecuredPrefixMapping securedMapping;

    public SecuredPrefixMappingTest(final SecurityEvaluator securityEvaluator) {
        this.securityEvaluator = securityEvaluator;
        this.principal = securityEvaluator.getPrincipal();
        this.baseMap = new HashMap<String, String>();
    }

    private boolean shouldRead() {
        return !securityEvaluator.isHardReadError() || shouldReadMapping();
    }

    private boolean shouldReadMapping() {
        return securityEvaluator.evaluate(principal, Action.Read, securedMapping.getModelNode());
    }

    @Before
    public void setup() {
        baseMap.clear();
        final Graph g = GraphFactory.createDefaultGraph();
        g.getPrefixMapping().setNsPrefix("foo", "http://example.com/foo/");
        baseMap.putAll(g.getPrefixMapping().getNsPrefixMap());
        final SecuredGraph sg = Factory.getInstance(securityEvaluator, "http://example.com/testGraph", g);

        this.securedMapping = (SecuredPrefixMapping) sg.getPrefixMapping();
    }

    @Test
    public void testExpandPrefix() {
        try {
            String result = securedMapping.expandPrefix("foo:");
            if (shouldReadMapping()) {
                Assert.assertEquals("http://example.com/foo/", result);
            } else {
                Assert.assertEquals("foo:", result);
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
    public void testGetNsPrefixMap() {
        try {
            Map<String, String> map = securedMapping.getNsPrefixMap();
            if (shouldReadMapping()) {
                Assert.assertEquals("http://example.com/foo/", map.get("foo"));
            } else {
                Assert.assertTrue(map.isEmpty());
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
    public void testGetNsPrefixURI() {
        try {
            String result = securedMapping.getNsPrefixURI("foo");
            if (shouldReadMapping()) {
                Assert.assertEquals("http://example.com/foo/", result);
            } else {
                Assert.assertNull(result);
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
    public void testGetNsURIPrefix() {
        try {
            String result = securedMapping.getNsURIPrefix("http://example.com/foo/");
            if (shouldReadMapping()) {
                Assert.assertEquals("foo", result);
            } else {
                Assert.assertNull(result);
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
    public void testLock() {
        try {
            securedMapping.lock();
            if (!securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

    @Test
    public void testQnameFor() {
        try {
            // "http://example.com/foo/bar"
            String result = securedMapping.qnameFor("http://example.com/foo/bar");
            if (shouldReadMapping()) {
                Assert.assertEquals("foo:bar", result);
            } else {
                Assert.assertNull(result);
            }
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException : %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testRemoveNsPrefix() {
        try {
            securedMapping.removeNsPrefix("foo");
            if (!securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail("Should have thrown UpdateDeniedException");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

    @Test
    public void testSamePrefixMappingAs() {
        try {
            PrefixMapping pmap = GraphFactory.createDefaultGraph().getPrefixMapping();
            pmap.setNsPrefixes(baseMap);
            boolean result = securedMapping.samePrefixMappingAs(pmap);
            if (shouldReadMapping()) {
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
    public void testSetNsPrefix() {
        try {
            securedMapping.setNsPrefix("foo2", "http://example.com/foo2");
            if (!securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {

                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            securedMapping.setNsPrefixes(GraphFactory.createDefaultGraph().getPrefixMapping());
            if (!securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            securedMapping.setNsPrefixes(new HashMap<String, String>());
            if (!securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testShortForm() {
        try {
            String result = securedMapping.shortForm("http://example.com/foo/bar");
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException");
            }
            if (shouldReadMapping()) {
                Assert.assertEquals("foo:bar", result);
            } else {
                Assert.assertEquals("http://example.com/foo/bar", result);
            }

        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testWithDefaultMappings() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("example", "http://example.com");
        try {
            // make sure that it must update
            securedMapping.withDefaultMappings(pm);
            if (!securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail("Should have thrown UpdateDeniedException");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testWithDefaultMappingsNoAdd() {
        PrefixMapping pm = new PrefixMappingImpl();
        try {
            // make sure that it must update
            securedMapping.withDefaultMappings(pm);
            // if (!securityEvaluator.evaluate(Action.Update,
            // securedMapping.getModelNode()))
            // {
            // Assert.fail("Should have thrown UpdateDeniedException Exception");
            // }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(principal, Action.Update, securedMapping.getModelNode())) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }
}
