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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.function.Supplier;

import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.model.impl.SecuredResourceImpl;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.shared.ReadDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredResourceTest extends SecuredRDFNodeTest {

    public SecuredResourceTest(final MockSecurityEvaluator securityEvaluator) {
        super(securityEvaluator);
    }

    private SecuredResource getSecuredResource() {
        return (SecuredResource) getSecuredRDFNode();
    }

    private Resource getBaseResource() {
        return (Resource) getBaseRDFNode();
    }

    @Override
    @Before
    public void setup() {
        super.setup();
        setSecuredRDFNode(SecuredResourceImpl.getInstance(securedModel, SecuredRDFNodeTest.s), s);
    }

    /**
     * True if the P property should exist
     * 
     * @return
     */
    protected boolean hasP() {
        return true;
    }

    protected boolean hasP2() {
        return true;
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     */
    @Test
    public void testAddLiteralBoolean() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, true);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    public void testAddLiteralChar() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, 'c');
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    public void testAddLiteralDouble() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, 3.14D);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    public void testAddLiteralFloat() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, 3.14F);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    public void testAddLiteral() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, ResourceFactory.createTypedLiteral("Yee haw"));
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    public void testAddLiteralLong() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, 1L);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    public void testAddLiteralObject() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        final Object o = Integer.valueOf("1234");
        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, o);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

    }

    @Test
    public void testAddProperty() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        final RDFNode rdfNode = ResourceFactory.createResource("http://example.com/newResource");
        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, rdfNode);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            getSecuredResource().addLiteral(SecuredRDFNodeTest.p, "string");
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        final Literal l = ResourceFactory.createTypedLiteral(3.14F);
        try {
            getSecuredResource().addProperty(SecuredRDFNodeTest.p, l.getLexicalForm(), l.getDatatype());
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            getSecuredResource().addProperty(SecuredRDFNodeTest.p, "dos", "sp");
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testAnonFuncs() {

        final SecuredResource anonResource = (SecuredResource) securedModel.createResource();
        setSecuredRDFNode(anonResource, null);

        try {
            getSecuredResource().getId();
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
    public void testAsResource() {
        getSecuredResource().asResource();
    }

    @Test
    public void testEquals() {
        try {
            getSecuredResource().equals(SecuredRDFNodeTest.s);
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

    public void testGetLocalName() {
        try {
            getSecuredResource().getLocalName();
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

    public void testGetNameSpace() {
        try {
            getSecuredResource().getNameSpace();
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
    public void testGetProperty() {
        try {
            Statement actual = getSecuredResource().getProperty(SecuredRDFNodeTest.p);
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(Triple.create(getBaseRDFNode().asNode(), SecuredRDFNodeTest.p.asNode(),
                        SecuredRDFNodeTest.o.asNode()), actual.asTriple());
            } else {
                assertNull(actual);
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail("Should not have thrown ReadDeniedException Exception");
            }
        }
    }

    @Test
    public void testGetPropertyResourceValue() {
        try {
            Resource actual = getSecuredResource().getPropertyResourceValue(SecuredRDFNodeTest.p);
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(SecuredRDFNodeTest.o, actual);
            } else {
                assertNull(actual);
            }
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    private void testGetRequiredProperty(Supplier<Statement> supplier, RDFNode expected) {
        try {
            Statement actual = supplier.get();
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if ((expected == null)
                    || (!securityEvaluator.evaluate(Action.Read) && !securityEvaluator.isHardReadError())) {
                fail("Should have thrown PropertyNotFoundException");
            }
            assertEquals(expected, actual.getObject());
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } catch (final PropertyNotFoundException e) {
            if (expected != null && !securityEvaluator.evaluate(Action.Read) && securityEvaluator.isHardReadError()) {
                Assert.fail(String.format("Should not have thrown PropertyNotFound Exception: %s", e));
            }
        }

    }

    @Test
    public void testGetRequiredProperty() {

        testGetRequiredProperty(() -> getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p), o);
        testGetRequiredProperty(() -> getSecuredResource()
                .getRequiredProperty(ResourceFactory.createProperty("http://example.com/graph/p3")), null);
    }

    private void testGetPropertyWithLang(Supplier<Statement> supplier, String expected) {
        try {
            Statement result = supplier.get();
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (hasP2() && securityEvaluator.evaluate(Action.Read)) {
                if (expected == null) {
                    assertNull(result);
                } else {
                    assertEquals(expected, result.getObject().asLiteral().getString());
                }
            } else {
                assertNull(result);
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

        testGetPropertyWithLang(() -> getSecuredResource().getProperty(SecuredRDFNodeTest.p2, ""), "yeehaw");
        testGetPropertyWithLang(() -> getSecuredResource().getProperty(SecuredRDFNodeTest.p2, "us"), "yeehaw yall");
        testGetPropertyWithLang(() -> getSecuredResource().getProperty(SecuredRDFNodeTest.p2, "uk"), "whohoo");
        testGetPropertyWithLang(() -> getSecuredResource().getProperty(SecuredRDFNodeTest.p2, "non"), null);
    }

    private void testGetRequiredPropertyWithLang(Supplier<Statement> supplier, String expected) {
        try {
            Statement actual = supplier.get();
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (expected == null) {
                assertNull(actual);
            } else {
                assertEquals(expected, actual.getObject().asLiteral().getString());
            }

        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } catch (final PropertyNotFoundException e) {
            boolean shouldFail = (expected == null);
            shouldFail |= !(shouldRead() && hasP2());
            shouldFail |= (!securityEvaluator.evaluate(Action.Read) && !securityEvaluator.isHardReadError());

            if (!shouldFail) {
                Assert.fail("Should not have thrown PropertyNotFoundException");
            }
        }

    }

    @Test
    public void testGetRequiredPropertyWithLang() {

        testGetRequiredPropertyWithLang(() -> getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p2, ""),
                "yeehaw");
        testGetRequiredPropertyWithLang(() -> getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p2, "us"),
                "yeehaw yall");
        testGetRequiredPropertyWithLang(() -> getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p2, "uk"),
                "whohoo");
        testGetRequiredPropertyWithLang(() -> getSecuredResource().getRequiredProperty(SecuredRDFNodeTest.p2, "non"),
                null);
    }

    @Test
    public void testGetURI() {
        try {
            getSecuredResource().getURI();
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
    public void testHasLiteral() {
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, true), false);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 'c'), false);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 3.14d), false);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 3.14f), false);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 6l), false);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p2, o), false);

        Model m = getBaseRDFNode().getModel();
        m.addLiteral(getBaseResource(), p, true);
        m.addLiteral(getBaseResource(), p, 'c');
        m.addLiteral(getBaseResource(), p, 3.14d);
        m.addLiteral(getBaseResource(), p, 3.14f);
        m.addLiteral(getBaseResource(), p, 6l);
        m.addLiteral(getBaseResource(), p2, m.createTypedLiteral(o));

        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, true), true);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 'c'), true);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 3.14d), true);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 3.14f), true);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p, 6l), true);
        testHasProperty(() -> getSecuredResource().hasLiteral(SecuredRDFNodeTest.p2, o), true);
    }

    private void testHasProperty(Supplier<Boolean> supplier, boolean expected) {

        try {
            boolean actual = supplier.get();
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(expected, actual);
            } else {
                assertFalse(actual);
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testHasProperty() {
        Model m = getBaseRDFNode().getModel();
        m.addLiteral(getBaseRDFNode().asResource(), p, m.createLiteral("yeehaw"));
        testHasProperty(() -> getSecuredResource().hasProperty(SecuredRDFNodeTest.p), true);
        testHasProperty(() -> getSecuredResource().hasProperty(SecuredRDFNodeTest.p, SecuredRDFNodeTest.o), true);
        testHasProperty(() -> getSecuredResource().hasProperty(SecuredRDFNodeTest.p, "yeehaw"), true);
        testHasProperty(() -> getSecuredResource().hasProperty(SecuredRDFNodeTest.p, "dos", "sp"), false);
    }

    @Test
    public void testHasURI() {
        try {
            getSecuredResource().hasURI("http://example.com/yeeHaw");
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

    private void testListProperties(Supplier<StmtIterator> supplier, boolean expected) {
        try {
            StmtIterator iter = supplier.get();
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(expected, iter.hasNext());
            } else {
                assertFalse(iter.hasNext());
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testListProperties() {
        testListProperties(() -> getSecuredResource().listProperties(), true);
        testListProperties(() -> getSecuredResource().listProperties(SecuredRDFNodeTest.p), hasP());
    }

    private void testListProperties(Supplier<StmtIterator> supplier, boolean expected, String txt) {
        try {
            StmtIterator iter = supplier.get();
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(expected, iter.hasNext());
                if (expected) {
                    Statement stmt = iter.next();
                    assertEquals(txt, stmt.getObject().asLiteral().getString());
                }
            } else {
                assertFalse(iter.hasNext());
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }
    }

    @Test
    public void testListPropertiesWithLang() {
        testListProperties(() -> getSecuredResource().listProperties(SecuredRDFNodeTest.p2, ""), hasP2(), "yeehaw");

        testListProperties(() -> getSecuredResource().listProperties(SecuredRDFNodeTest.p2, "us"), hasP2(),
                "yeehaw yall");

        testListProperties(() -> getSecuredResource().listProperties(SecuredRDFNodeTest.p2, "uk"), hasP2(), "whohoo");
    }

    @Test
    public void testRemoveList() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });
        final int count = baseModel.listStatements(getBaseRDFNode().asResource(), SecuredRDFNodeTest.p, (RDFNode) null)
                .toSet().size();

        try {
            getSecuredResource().removeAll(SecuredRDFNodeTest.p);
            // only throw on delete if count > 0
            if (!securityEvaluator.evaluate(Action.Update)
                    || ((count > 0) && !securityEvaluator.evaluate(Action.Delete))) {
                Assert.fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testRemoveProperties() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });
        final int count = baseModel.listStatements(getBaseRDFNode().asResource(), SecuredRDFNodeTest.p, (RDFNode) null)
                .toSet().size();

        try {
            getSecuredResource().removeProperties();
            // only throw on delete if count > 0
            if (!securityEvaluator.evaluate(Action.Update)
                    || ((count > 0) && !securityEvaluator.evaluate(Action.Delete))) {
                Assert.fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }
}
