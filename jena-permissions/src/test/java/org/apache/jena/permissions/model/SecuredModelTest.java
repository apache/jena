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
package org.apache.jena.permissions.model;

import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.*;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.graph.SecuredGraph;
import org.apache.jena.permissions.graph.SecuredPrefixMappingTest;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.*;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredModelTest {
    protected final MockSecurityEvaluator securityEvaluator;
    protected SecuredModel securedModel;
    protected Model baseModel;
    protected Resource s;
    protected Resource s2;
    protected Property p;
    protected Property p2;
    protected Resource o;
    protected Resource o2;

    public SecuredModelTest(final MockSecurityEvaluator securityEvaluator) {
        this.securityEvaluator = securityEvaluator;
    }

    /**
     * create an unsecured securedModel.
     *
     * @return
     */
    protected Model createModel() {
        return ModelFactory.createDefaultModel();
    }

    protected Model createSecondModel() {
        Model secondModel = ModelFactory.createDefaultModel();
        secondModel.add(s2, p2, o2);
        return secondModel;
    }

    private boolean shouldRead() {
        return !securityEvaluator.isHardReadError() || securityEvaluator.evaluate(Action.Read);
    }

    @Before
    public void setup() {
        baseModel = createModel();
        baseModel.removeAll();
        baseModel.setNsPrefix("foo", "http://example.com/foo/");
        securedModel = Factory.getInstance(securityEvaluator, "http://example.com/securedGraph", baseModel);
        s = ResourceFactory.createResource("http://example.com/graph/s");
        p = ResourceFactory.createProperty("http://example.com/graph/p");
        o = ResourceFactory.createResource("http://example.com/graph/o");
        baseModel.add(s, p, o);
        p2 = ResourceFactory.createProperty("http://example.com/graph/p2");
        baseModel.add(s, p2, "yeehaw");
        baseModel.add(s, p2, "yeehaw yall", "us");
        baseModel.add(s, p2, "whohoo", "uk");
        s2 = ResourceFactory.createResource("http://example.com/graph/s2");
        o2 = ResourceFactory.createResource("http://example.com/graph/o2");
    }

    private void __testAdd(Supplier<Model> addFunc) {
        final Set<Action> createAndUpdate = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            assertNotNull(addFunc.get());
            if (!securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

    @Test
    public void testAdd() throws Exception {
        final List<Statement> stmt = baseModel.listStatements().toList();
        __testAdd(() -> securedModel.add(stmt));
        __testAdd(() -> securedModel.add(baseModel));
        __testAdd(() -> securedModel.add(stmt.get(0)));
        __testAdd(() -> securedModel.add(stmt.toArray(new Statement[stmt.size()])));
        __testAdd(() -> securedModel.add(baseModel.listStatements()));
        __testAdd(() -> securedModel.add(baseModel));
        __testAdd(() -> securedModel.add(s, p, o));
        __testAdd(() -> securedModel.add(s, p, "foo"));
        __testAdd(() -> securedModel.add(s, p, "foo", XSDDatatype.XSDstring));
        __testAdd(() -> securedModel.add(s, p, "foo", "en"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAddLiteral() throws Exception {
        __testAdd(() -> securedModel.addLiteral(s, p, true));
        __testAdd(() -> securedModel.addLiteral(s, p, 'c'));
        __testAdd(() -> securedModel.addLiteral(s, p, 2.0d));
        __testAdd(() -> securedModel.addLiteral(s, p, 2.0f));
        __testAdd(() -> securedModel.addLiteral(s, p, 5));
        __testAdd(() -> securedModel.addLiteral(s, p, 5L));
        __testAdd(() -> securedModel.addLiteral(s, p, baseModel.createLiteral("Literal")));
    }

    @Test
    public void testAnonymousInModel() {
        // test anonymous
        final RDFNode rdfNode = ResourceFactory.createResource();
        final RDFNode rdfNode2 = rdfNode.inModel(securedModel);
        Assert.assertEquals("Should have placed RDFNode in secured securedModel", securedModel, rdfNode2.getModel());
    }

    @Test
    public void testAsRDFNode() {
        RDFNode node = securedModel.asRDFNode(NodeFactory.createURI("http://example.com/rdfNode"));
        assertTrue(node.isResource());
        assertEquals(securedModel, node.getModel());
        if (securedModel.canRead()) {
            assertEquals("http://example.com/rdfNode", ((Resource) node).getURI());
        }
    }

    @Test
    public void testAsStatement_Exists() {
        Triple t = Triple.create(s.asNode(), p.asNode(), o.asNode());
        try {
            Statement stmt = securedModel.asStatement(t);
            assertEquals(securedModel, stmt.getModel());
            if (securedModel.canRead(t)) {
                assertEquals(s, stmt.getSubject());
                assertEquals(p, stmt.getPredicate());
                assertEquals(o, stmt.getObject());
            }
            if (!securityEvaluator.evaluate(Action.Read) && !securityEvaluator.evaluate(Action.Update)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }

        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read) && securityEvaluator.evaluate(Action.Update)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testAsStatement_NotExists() {
        // check a triple that does not exist -- must have update to add it.
        Triple t = Triple.create(s.asNode(), p2.asNode(), o.asNode());
        try {
            Statement stmt = securedModel.asStatement(t);
            assertEquals(securedModel, stmt.getModel());
            if (securedModel.canRead(t)) {
                assertEquals(s, stmt.getSubject());
                assertEquals(p2, stmt.getPredicate());
                assertEquals(o, stmt.getObject());
            }
            if (!securityEvaluator.evaluate(Action.Read) && !securityEvaluator.evaluate(Action.Update)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    private void __testContains_true(Supplier<Boolean> supplier) {
        try {
            boolean result = supplier.get();
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

    private void __testContains_false(Supplier<Boolean> supplier) {
        try {
            boolean result = supplier.get();
            Assert.assertFalse(result);

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
    public void testContains() throws Exception {
        final Statement stmt = baseModel.listStatements().next();
        /*
         * baseModel.add(s, p2, "yeehaw"); baseModel.add(s, p2, "yeehaw yall", "us");
         * baseModel.add(s, p2, "whohoo", "uk");
         */
        __testContains_true(() -> securedModel.contains(stmt));
        __testContains_true(() -> securedModel.contains(s, p));
        __testContains_true(() -> securedModel.contains(s, p, o));
        __testContains_true(() -> securedModel.contains(s, p2, "yeehaw"));
        __testContains_true(() -> securedModel.contains(s, p2, "whohoo", "uk"));

        final Statement stmt2 = createSecondModel().listStatements().next();
        __testContains_false(() -> securedModel.contains(stmt2));
        __testContains_false(() -> securedModel.contains(s2, p));
        __testContains_false(() -> securedModel.contains(s, p, o2));
        __testContains_false(() -> securedModel.contains(s, p2, "foo"));
        __testContains_false(() -> securedModel.contains(s, p, "whohoo", "fr"));
    }

    @Test
    public void testContainsAll_Model() throws Exception {
        try {
            boolean result = securedModel.containsAll(baseModel);
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

        try {
            Model secondModel = createSecondModel();
            secondModel.add(s, p, o);
            boolean result = securedModel.containsAll(secondModel);
            Assert.assertFalse(result);

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
    public void testContainsAll_Statements() throws Exception {
        try {
            boolean result = securedModel.containsAll(baseModel.listStatements());
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

        try {
            Model secondModel = createSecondModel();
            secondModel.add(s, p2, o);
            boolean result = securedModel.containsAll(secondModel.listStatements());
            Assert.assertFalse(result);

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
    public void testCreateAlt() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });
        try {
            securedModel.createAlt();
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
        try {
            securedModel.createAlt("foo");
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testCreateBag() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });
        try {
            securedModel.createBag();
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
        try {
            securedModel.createBag("foo");
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testCreateList() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        final List<RDFNode> nodeList = new ArrayList<>();
        try {
            securedModel.createList();
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
        baseModel.removeAll();

        try {
            securedModel.createList(nodeList.iterator());
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
        baseModel.removeAll();

        try {
            final RDFNode[] list = new RDFNode[] { ResourceFactory.createResource(), ResourceFactory.createResource(),
                    ResourceFactory.createResource(), ResourceFactory.createResource(), };

            securedModel.createList(list);
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
        baseModel.removeAll();

    }

    @Test
    public void testCreateLiteral() throws Exception {
        securedModel.createLiteral("foo");
        securedModel.createLiteral("foo", "");
    }

    @Test
    public void testCreateLiteralBoolean() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });

        try {
            securedModel.createLiteralStatement(s, p, true);
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testCreateLiteralChar() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });
        try {
            securedModel.createLiteralStatement(s, p, 'a');
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testCreateLiteralDouble() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });

        try {
            securedModel.createLiteralStatement(s, p, 1.0d);
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testCreateLiteralFloat() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });

        try {
            securedModel.createLiteralStatement(s, p, 1.0f);
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testCreateLiteralInt() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });

        try {
            securedModel.createLiteralStatement(s, p, 1);
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

    @Test
    public void testCreateLiteralLong() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });

        try {
            securedModel.createLiteralStatement(s, p, 1L);
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testCreateLiteralObject() throws Exception {
        final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Update });

        try {
            securedModel.createLiteralStatement(s, p, new URL("http://example.com/testing/URIType"));
            if (!securityEvaluator.evaluate(CU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(CU)) {
                e.printStackTrace();
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testDifference() throws Exception {
        try {
            Model m = securedModel.difference(baseModel);
            assertTrue(m.isEmpty());
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        Model secondModel = createSecondModel();
        try {
            Model m = securedModel.difference(secondModel);
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(4, m.size());
                assertTrue(m.contains(s, p, o));
                assertEquals("yeehaw", m.getProperty(s, p2, "").getString());
                assertEquals("yeehaw yall", m.getProperty(s, p2, "us").getString());
                assertEquals("whohoo", m.getProperty(s, p2, "uk").getString());
            } else {
                assertTrue(m.isEmpty());
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
    public void testEquals() throws Exception {
        securedModel.equals(baseModel);
        baseModel.equals(securedModel);
    }

    @Test
    public void testGetAlt_Existing() throws Exception {
        Resource r = baseModel.createAlt("http://example.com/securedModel/alt");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Bag.asNode());

        try {
            Alt alt = securedModel.getAlt(r);
            assertEquals(securedModel, alt.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/alt", alt.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }

        try {
            Alt alt = securedModel.getAlt("http://example.com/securedModel/alt");
            assertEquals(securedModel, alt.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/alt", alt.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }

    }

    @Test
    public void testGetAlt_ResourceNotExisting() throws Exception {

        Resource r = ResourceFactory.createResource("http://example.com/securedModel/alt");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Alt.asNode());
        try {
            Alt alt = securedModel.getAlt(r);
            assertEquals(securedModel, alt.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/alt", alt.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }
    }

    @Test
    public void testGetAlt_StringNotExisting() throws Exception {

        Resource r = ResourceFactory.createResource("http://example.com/securedModel/alt");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Alt.asNode());
        try {
            Alt alt = securedModel.getAlt("http://example.com/securedModel/alt");
            assertEquals(securedModel, alt.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/alt", alt.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }
    }

    @Test
    public void testGetAnyReifiedStmt_none() {
        // first with create.
        try {
            Resource r = securedModel.getAnyReifiedStatement(baseModel.listStatements().next());
            Assert.assertNotNull(r);
            if (!securityEvaluator.evaluate(Action.Update)) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
            if (!securityEvaluator.evaluate(Action.Create)) {
                Assert.fail("Should have thrown AddDeniedException Exception");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(Action.Update)) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } catch (final AddDeniedException e) {
            if (securityEvaluator.evaluate(Action.Create)) {
                Assert.fail(String.format("Should not have thrown AddDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testGetAnyReifiedStmt_one() {
        final Statement st = baseModel.listStatements().next();
        ReifiedStatement s = baseModel.createReifiedStatement(st);
        // there is a reified statement so only read is required.

        try {
            Resource r = securedModel.getAnyReifiedStatement(st);

            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.assertEquals(s.getURI(), r.getURI());
            }
            if (!securityEvaluator.evaluate(Action.Update) && !securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
            if (!securityEvaluator.evaluate(Action.Create) && !securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown AddDeniedException Exception");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(Action.Update)) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } catch (final AddDeniedException e) {
            if (securityEvaluator.evaluate(Action.Create)) {
                Assert.fail(String.format("Should not have thrown AddDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }

        }
    }

    @Test
    public void testGetBag_Existing() {
        final Resource r = baseModel.createBag("http://example.com/securedModel/bag");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Bag.asNode());

        try {
            Bag bag = securedModel.getBag(r);
            assertEquals(securedModel, bag.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/bag", bag.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }

        try {
            Bag bag = securedModel.getBag("http://example.com/securedModel/bag");
            assertEquals(securedModel, bag.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/bag", bag.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }
    }

    @Test
    public void testGetBag_ResourceNotExisting() throws Exception {

        Resource r = ResourceFactory.createResource("http://example.com/securedModel/bag");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Bag.asNode());
        try {
            Bag bag = securedModel.getBag(r);
            assertEquals(securedModel, bag.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/bag", bag.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }
    }

    @Test
    public void testGetBag_StringNotExisting() throws Exception {

        Resource r = ResourceFactory.createResource("http://example.com/securedModel/bag");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Bag.asNode());
        try {
            Bag bag = securedModel.getBag("http://example.com/securedModel/bag");
            assertEquals(securedModel, bag.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/bag", bag.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }
    }

    @Test
    public void testGetGraph() throws Exception {
        final Graph g = securedModel.getGraph();
        Assert.assertTrue(g instanceof SecuredGraph);
        EqualityTester.testInequality("getGraph test", g, baseModel.getGraph());
    }

    @Test
    public void testGetLock() {
        securedModel.getLock();
    }

    private void __testGetProperty_exists(Supplier<Property> supplier) {
        try {
            Property prop = supplier.get();
            assertEquals(p.getURI(), prop.getURI());
            if (!securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    private void __testGetProperty_notExists(Supplier<Property> supplier) {
        try {
            Property actual = supplier.get();
            Assert.assertEquals("http://example.com/graph/p3", actual.getURI());
            if (!securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testGetProperty_String() {
        __testGetProperty_exists(() -> securedModel.getProperty("http://example.com/graph/p"));
    }

    @Test
    public void testGetProperty_String_String() {
        __testGetProperty_exists(() -> securedModel.getProperty("http://example.com/graph/", "p"));
    }

    @Test
    public void testGetProperty_String_notExist() {
        __testGetProperty_notExists(() -> securedModel.getProperty("http://example.com/graph/p3"));
    }

    @Test
    public void testGetProperty_String_String_notExist() {
        __testGetProperty_notExists(() -> securedModel.getProperty("http://example.com/graph/", "p3"));
    }

    @Test
    public void testGetProperty_SP_exists() {
        Statement expected = baseModel.getProperty(s, p);
        try {
            Statement actual = securedModel.getProperty(s, p);
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(expected, actual);
            } else {
                Assert.assertNull(actual);
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
    public void testGetProperty_SP_notExists() {
        try {
            Statement actual = securedModel.getProperty(s2, p);
            Assert.assertNull(actual);
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

    private void __testGetProperty_lang(Supplier<Statement> supplier, String expected) {
        try {
            Statement stmt = supplier.get();
            if (securityEvaluator.evaluate(Action.Read) && expected != null) {
                assertNotNull(stmt);
                assertEquals(expected, stmt.getObject().asLiteral().getString());
            } else {
                assertNull(stmt);
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
    public void testGetPropertyWithLang() {
        __testGetProperty_lang(() -> securedModel.getProperty(s, p2, ""), "yeehaw");
        __testGetProperty_lang(() -> securedModel.getProperty(s, p2, "us"), "yeehaw yall");
        __testGetProperty_lang(() -> securedModel.getProperty(s, p2, "uk"), "whohoo");
        __testGetProperty_lang(() -> securedModel.getProperty(s, p2, "fr"), null);
        __testGetProperty_lang(() -> securedModel.getProperty(s, p, ""), null);
        __testGetProperty_lang(() -> securedModel.getProperty(s, p, "us"), null);
        __testGetProperty_lang(() -> securedModel.getProperty(s, p, "uk"), null);
    }

    @Test
    public void testGetPrefixMapping() throws Exception {
        SecuredPrefixMappingTest.runTests(securityEvaluator, new Supplier<PrefixMapping>() {
            @Override
            public PrefixMapping get() {
                setup();
                return securedModel;
            }
        }, baseModel.getNsPrefixMap());
    }

//	@Test
//	public void testGetQNameFor() throws Exception {
//		try {
//			securedModel.qnameFor("foo");
//			if (!securityEvaluator.evaluate(Action.Read)) {
//				Assert.fail("Should have thrown ReadDeniedException Exception");
//			}
//		} catch (final ReadDeniedException e) {
//			if (securityEvaluator.evaluate(Action.Read)) {
//				Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
//						e.getTriple()));
//			}
//		}
//	}

    @Test
    public void testGetRDFNode() {
        RDFNode node = securedModel.getRDFNode(NodeFactory.createURI("http://example.com/rdfNode"));
        assertTrue(node.isResource());
        assertEquals(securedModel, node.getModel());
        if (securedModel.canRead()) {
            assertEquals("http://example.com/rdfNode", ((Resource) node).getURI());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetReader() {
        securedModel.getReader(null);
        securedModel.getReader("TURTLE");
    }

    @Test
    public void testGetResource() {
        securedModel.getResource("foo");
    }

    @Test
    public void testGetSeq_Existing() {
        final Resource r = baseModel.createSeq("http://example.com/securedModel/seq");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Bag.asNode());

        try {
            Seq seq = securedModel.getSeq(r);
            assertEquals(securedModel, seq.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/seq", seq.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }

        try {
            Seq seq = securedModel.getSeq("http://example.com/securedModel/seq");
            assertEquals(securedModel, seq.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/seq", seq.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }
    }

    @Test
    public void testGetSeq_ResourceNotExisting() throws Exception {

        Resource r = ResourceFactory.createResource("http://example.com/securedModel/seq");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Seq.asNode());
        try {
            Seq seq = securedModel.getSeq(r);
            assertEquals(securedModel, seq.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/seq", seq.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }
    }

    @Test
    public void testGetSeq_StringNotExisting() throws Exception {

        Resource r = ResourceFactory.createResource("http://example.com/securedModel/seq");
        Triple t = Triple.create(r.asNode(), RDF.type.asNode(), RDF.Seq.asNode());
        try {
            Seq seq = securedModel.getSeq("http://example.com/securedModel/seq");
            assertEquals(securedModel, seq.getModel());
            if (securedModel.canRead()) {
                assertEquals("http://example.com/securedModel/seq", seq.getURI());
            } else if (!securedModel.canUpdate() || !securedModel.canCreate(t)) {
                fail("Should have thrown AddDeniedException ");
            }
        } catch (AddDeniedException e) {
            if (securedModel.canUpdate() && securedModel.canCreate(t)) {
                fail(String.format("Should not have thrown AddDeniedException ", e));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetWriter() {
        securedModel.getWriter(null);
        securedModel.getWriter("TURTLE");
    }

    @Test
    public void testIndependent() throws Exception {
        Assert.assertFalse(securedModel.independent());
    }

    @Test
    public void testIntersection() throws Exception {
        try {
            Model m = securedModel.intersection(baseModel);
            if (securityEvaluator.evaluate(Action.Read)) {
                assertTrue(m.isIsomorphicWith(baseModel));
            } else {
                assertTrue(m.isEmpty());
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

        Model secondModel = createSecondModel();
        try {
            Model m = securedModel.intersection(secondModel);
            assertTrue(m.isEmpty());
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        secondModel.add(s, p, o);
        try {
            Model m = securedModel.intersection(secondModel);
            if (securityEvaluator.evaluate(Action.Read)) {
                assertTrue(m.contains(s, p, o));
                assertEquals(1, m.size());
            } else {
                assertTrue(m.isEmpty());
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
    public void testIsClosed() throws Exception {
        securedModel.isClosed();
    }

    @Test
    public void testIsEmpty() throws Exception {
        try {
            boolean actual = securedModel.isEmpty();
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.assertFalse(actual);
            } else {
                Assert.assertTrue(actual);
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

        baseModel.removeAll();
        try {
            boolean actual = securedModel.isEmpty();
            Assert.assertTrue(actual);
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
    public void testIsIsomorphicWith() {
        try {
            boolean result = securedModel.isIsomorphicWith(baseModel);
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

        try {
            boolean result = baseModel.isIsomorphicWith(securedModel);
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
    public void testIsReified() {
        Statement stmt = baseModel.listStatements().next();
        try {
            boolean actual = securedModel.isReified(stmt);
            assertFalse(actual);
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        baseModel.createReifiedStatement(stmt);
        try {
            boolean actual = securedModel.isReified(stmt);
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

    private void __testListLiteralStatements(Supplier<StmtIterator> supplier) {
        try {
            StmtIterator iter = supplier.get();
            assertNotNull(iter);
            if (securityEvaluator.evaluate(Action.Read)) {
                assertTrue(iter.hasNext());
            } else {
                assertFalse(iter.hasNext());
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
    public void testListLiteralStatements() throws Exception {

        baseModel.addLiteral(s, p, true);
        baseModel.addLiteral(s, p, '0');
        baseModel.addLiteral(s, p, 2d);
        baseModel.addLiteral(s, p, 2f);
        baseModel.addLiteral(s, p, 1);

        __testListLiteralStatements(() -> securedModel.listLiteralStatements(s, p, true));
        __testListLiteralStatements(() -> securedModel.listLiteralStatements(s, p, '0'));
        __testListLiteralStatements(() -> securedModel.listLiteralStatements(s, p, 2.0d));
        __testListLiteralStatements(() -> securedModel.listLiteralStatements(s, p, 2.0f));
        __testListLiteralStatements(() -> securedModel.listLiteralStatements(s, p, 1));
    }

    @Test
    public void testLock() throws Exception {
        try {
            securedModel.lock();
            if (!securityEvaluator.evaluate(Action.Update)) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(Action.Update)) {
                Assert.fail(String.format("Should not have thrown UpdateDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testPrefixMapping() throws Exception {
        SecuredPrefixMappingTest.runTests(securityEvaluator, new Supplier<PrefixMapping>() {
            @Override
            public PrefixMapping get() {
                setup();
                return securedModel;
            }
        }, baseModel.getNsPrefixMap());
    }

    @Test
    public void testQuery() throws Exception {
        final Selector s = new SimpleSelector();
        try {
            Model model = securedModel.query(s);
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.assertTrue(model.isIsomorphicWith(baseModel));
            } else {
                Assert.assertTrue(model.isEmpty());
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
    public void testRDFNodeInModel() {
        // test uri
        final RDFNode rdfNode = ResourceFactory.createResource("http://exmple.com/testInModel");
        final RDFNode rdfNode2 = rdfNode.inModel(securedModel);
        Assert.assertEquals("Should have placed RDFNode in secured securedModel", securedModel, rdfNode2.getModel());
    }

    @Test
    public void testReadEmpty() throws Exception {
        final Set<Action> createAndUpdate = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        final String XML_INPUT = "<rdf:RDF" + "   xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' "
                + "   xmlns:rt='http://example.com/readTest#' " + "   xmlns:j.0='http://example.com/readTest#3' > "
                + "  <rdf:Description rdf:about='http://example.com/readTest#1'> "
                + "    <rdf:type rdf:resource='http://example.com/readTest#3'/>" + "  </rdf:Description>"
                + "</rdf:RDF>";
        final String TTL_INPUT = "@prefix rt: <http://example.com/readTest#> . rt:1 a rt:3 .";
        final String base = "http://example.com/test";
        final String lang = "TURTLE";
        try {
            final URL url = SecuredModelTest.class.getResource("./test.xml");
            securedModel.read(url.toString());
            if (!securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } finally {
            baseModel.removeAll();
        }

        try {
            final InputStream in = new ByteArrayInputStream(XML_INPUT.getBytes());
            securedModel.read(in, base);
            if (!securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } finally {
            baseModel.removeAll();
        }

        try {
            final Reader reader = new StringReader(XML_INPUT);
            securedModel.read(reader, base);
            if (!securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } finally {
            baseModel.removeAll();
        }

        try {
            final URL url = SecuredModelTest.class.getResource("./test.ttl");
            securedModel.read(url.toString(), lang);
            if (!securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } finally {
            baseModel.removeAll();
        }

        try {
            final InputStream in = new ByteArrayInputStream(TTL_INPUT.getBytes());
            securedModel.read(in, base, lang);
            if (!securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } finally {
            baseModel.removeAll();
        }

        try {
            final Reader reader = new StringReader(TTL_INPUT);
            securedModel.read(reader, base, lang);
            if (!securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } finally {
            baseModel.removeAll();
        }

        try {
            final URL url = SecuredModelTest.class.getResource("./test.ttl");
            securedModel.read(url.toString(), base, lang);
            if (!securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(createAndUpdate)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } finally {
            baseModel.removeAll();
        }

    }

    @Test
    public void testRemove() throws Exception {
        final Set<Action> DU = SecurityEvaluator.Util.asSet(new Action[] { Action.Delete, Action.Update });

        final List<Statement> stmt = baseModel.listStatements().toList();
        try {
            securedModel.remove(baseModel.listStatements().toList());
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            securedModel.remove(baseModel);
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
        try {
            securedModel.remove(stmt.get(0));
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            securedModel.remove(stmt.toArray(new Statement[stmt.size()]));
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            securedModel.remove(baseModel.listStatements());
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            securedModel.remove(baseModel);
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            securedModel.remove(s, p, o);
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

    @Test
    public void testRemoveAll() throws Exception {
        final Set<Action> DU = SecurityEvaluator.Util.asSet(new Action[] { Action.Delete, Action.Update });

        try {
            securedModel.removeAll();
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        // put some data back
        baseModel.add(s, p, o);
        try {
            securedModel.removeAll(s, p, o);
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testRemoveAllReifications() {
        final Set<Action> DU = SecurityEvaluator.Util.asSet(new Action[] { Action.Delete, Action.Update });

        final List<Statement> stmt = baseModel.listStatements().toList();
        baseModel.createReifiedStatement(stmt.get(0));

        try {
            securedModel.removeAllReifications(stmt.get(0));
            if (!securityEvaluator.evaluate(DU)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(DU)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testGetRequiredProperty() {

        try {
            securedModel.getRequiredProperty(s, p);
            if (!securityEvaluator.evaluate(Action.Read)) {
                if (securityEvaluator.isHardReadError()) {
                    fail("Should have thrown ReadDeniedException Exception");
                }
                fail("Should have thrown PropertyNotFoundException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } catch (PropertyNotFoundException e) {
            if (securityEvaluator.isHardReadError() || securedModel.canRead()) {
                fail("Should not have thrown PropertyNotFoundException");
            }
        }
    }

    private void _testRequiredPropertyWithLang(Supplier<Statement> supplier, String expected) {
        try {
            Statement stmt = supplier.get();
            assertNotNull(stmt);
            if (securedModel.canRead()) {
                assertEquals(expected, stmt.getObject().asLiteral().getString());
            } else {
                if (securityEvaluator.isHardReadError()) {
                    fail("Should have thrown ReadDeniedException Exception");
                }
                fail("Should have thrown PropertyNotFoundException Exception");
            }
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } catch (PropertyNotFoundException e) {
            if (securityEvaluator.isHardReadError() || securedModel.canRead()) {
                fail("Should not have thrown PropertyNotFoundException");
            }
        }

    }

    @Test
    public void testGetRequiredPropertyWithLang() {
        _testRequiredPropertyWithLang(() -> securedModel.getRequiredProperty(s, p2, ""), "yeehaw");
        _testRequiredPropertyWithLang(() -> securedModel.getRequiredProperty(s, p2, "us"), "yeehaw yall");
        _testRequiredPropertyWithLang(() -> securedModel.getRequiredProperty(s, p2, "uk"), "whohoo");
    }

    @Test
    public void testSize() throws Exception {
        try {
            long size = securedModel.size();
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.assertEquals(4, size);
            } else {
                Assert.assertEquals(0, size);
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
    public void testUnion_onLeft() throws Exception {
        Model secondModel = createSecondModel();
        try {
            Model uModel = securedModel.union(secondModel);
            assertTrue(uModel.contains(s2, p2, o2));
            if (securityEvaluator.evaluate(Action.Read)) {
                assertTrue(uModel.contains(s, p, o));
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
    public void testUnion_onRight() throws Exception {
        Model secondModel = createSecondModel();
        try {
            Model uModel = secondModel.union(securedModel);
            assertTrue(uModel.contains(s2, p2, o2));
            if (securityEvaluator.evaluate(Action.Read)) {
                assertTrue(uModel.contains(s, p, o));
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
    public void testVariableInModel() {
        final RDFNode rdfNode = ResourceFactory.createTypedLiteral("yeehaw2");
        final RDFNode rdfNode2 = rdfNode.inModel(securedModel);
        Assert.assertEquals("Should have placed RDFNode in secured securedModel", securedModel, rdfNode2.getModel());
    }

    @Test
    public void testWrapAsResource() throws Exception {
        securedModel.wrapAsResource(NodeFactory.createURI("http://example.com/rdfNode"));
    }

    /**
     * this is a cheat. The supplier executes one of the model write methods and we
     * just check that the outcome (exceptions and all) are as expected.
     */
    private void __testWrite(Supplier<Model> supplier) {
        try {
            supplier.get();
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
    public void testWrite_stream() throws Exception {
        __testWrite(() -> securedModel.write(new ByteArrayOutputStream()));
    }

    @Test
    public void testWrite_stream_lang() throws Exception {
        __testWrite(() -> securedModel.write(new ByteArrayOutputStream(), "TURTLE"));
    }

    @Test
    public void testWrite_stream_lang_base() throws Exception {
        __testWrite(() -> securedModel.write(new ByteArrayOutputStream(), "TURTLE", "http://example.com/securedGraph"));
    }

    @Test
    public void testWrite_writer() throws Exception {
        __testWrite(() -> securedModel.write(new CharArrayWriter(), "RDF/XML"));
    }

    @Test
    public void testWrite_writer_lang() throws Exception {
        __testWrite(() -> securedModel.write(new CharArrayWriter(), "TURTLE"));
    }

    @Test
    public void testWrite_writer_lang_base() throws Exception {
        __testWrite(() -> securedModel.write(new CharArrayWriter(), "TURTLE", "http://example.com/securedGraph"));
    }
}