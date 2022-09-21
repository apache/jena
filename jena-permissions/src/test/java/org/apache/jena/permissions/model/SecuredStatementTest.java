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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.function.Supplier;

import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.Factory;
import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.model.impl.SecuredRSIterator;
import org.apache.jena.permissions.model.impl.SecuredStatementImpl;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredStatementTest {
    private final MockSecurityEvaluator securityEvaluator;
    private Statement baseStatement;
    private SecuredStatement securedStatement;
    private Model baseModel;
    private SecuredModel securedModel;
    private Property property;
    private Resource subject;

    public static Resource s = ResourceFactory.createResource("http://example.com/graph/s");
    public static Property p = ResourceFactory.createProperty("http://example.com/graph/p");
    public static Resource o = ResourceFactory.createResource("http://example.com/graph/o");

    public SecuredStatementTest(final MockSecurityEvaluator securityEvaluator) {
        this.securityEvaluator = securityEvaluator;
    }

    protected Model createModel() {
        return ModelFactory.createDefaultModel();
    }

    private boolean shouldRead() {
        return securedStatement.canRead() || !securityEvaluator.isHardReadError();
    }

    @Before
    public void setup() {
        baseModel = createModel();
        property = ResourceFactory.createProperty("http://example.com/property");
        subject = ResourceFactory.createResource();
        baseModel.add(subject, property, ResourceFactory.createResource());
        baseStatement = baseModel.listStatements().next();
        securedModel = Factory.getInstance(securityEvaluator, "http://example.com/securedModel", baseModel);
        securedStatement = SecuredStatementImpl.getInstance(securedModel, baseStatement);
    }

    /**
     * Sets the secured statement. Sets the baseStatement and creates the
     * securedStatement from it.
     * 
     * @param stmt The statement to use as the baseStatement.
     */
    private void setSecuredStatement(Statement stmt) {
        baseStatement = stmt;
        securedStatement = SecuredStatementImpl.getInstance(securedModel, stmt);
    }

    private void testChangeObject(Supplier<Statement> supplier, Triple expected) {
        try {
            SecuredStatement actual = (SecuredStatement) supplier.get();
            if (!securityEvaluator.evaluate(Action.Update)) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
            assertEquals(expected, ((Statement) actual.getBaseItem()).asTriple());
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(Action.Update)) {
                fail("Should not have thrown UpdateDeniedException Exception");
            }
        }
    }

    @Test
    public void testChangeLiteralObject_boolean() {
        Triple t = Triple.create(subject.asNode(), property.asNode(), ResourceFactory.createTypedLiteral(true).asNode());
        testChangeObject(() -> securedStatement.changeLiteralObject(true), t);
    }

    @Test
    public void testChangeLiteralObject_char() {
        Triple t = Triple.create(subject.asNode(), property.asNode(), ResourceFactory.createTypedLiteral('c').asNode());
        testChangeObject(() -> securedStatement.changeLiteralObject('c'), t);
    }

    @Test
    public void testChangeLiteralObject_double() {
        Triple t = Triple.create(subject.asNode(), property.asNode(), ResourceFactory.createTypedLiteral(3.14d).asNode());
        testChangeObject(() -> securedStatement.changeLiteralObject(3.14d), t);
    }

    @Test
    public void testChangeLiteralObject_float() {
        Triple t = Triple.create(subject.asNode(), property.asNode(), ResourceFactory.createTypedLiteral(3.14f).asNode());
        testChangeObject(() -> securedStatement.changeLiteralObject(3.14f), t);
    }

    @Test
    public void testChangeLiteralObject_int() {
        Triple t = Triple.create(subject.asNode(), property.asNode(), ResourceFactory.createTypedLiteral(2).asNode());
        testChangeObject(() -> securedStatement.changeLiteralObject(2), t);
    }

    @Test
    public void testChangeLiteralObject_long() {
        Triple t = Triple.create(subject.asNode(), property.asNode(), ResourceFactory.createTypedLiteral(2L).asNode());
        testChangeObject(() -> securedStatement.changeLiteralObject(2L), t);
    }

    @Test
    public void testChangeObject_resource() {
        Resource r = ResourceFactory.createResource("http://example.com/resource");
        Triple t = Triple.create(subject.asNode(), property.asNode(), r.asNode());
        testChangeObject(() -> securedStatement.changeObject(r), t);
    }

    @Test
    public void testChangeObject_string() {
        Literal l = ResourceFactory.createPlainLiteral("Waaa hooo");
        Triple t = Triple.create(subject.asNode(), property.asNode(), l.asNode());
        testChangeObject(() -> securedStatement.changeObject("Waaa hooo"), t);
    }

    @Test
    public void testChangeObject_lexicalform() {
        final Literal l = ResourceFactory.createPlainLiteral(String.valueOf(Integer.MAX_VALUE));
        Triple t = Triple.create(subject.asNode(), property.asNode(), l.asNode());
        testChangeObject(() -> securedStatement.changeObject(l.getLexicalForm(), true), t);
    }

    @Test
    public void testChangeObject_langString() {
        final Literal l = ResourceFactory.createLangLiteral("dos", "es");
        Triple t = Triple.create(subject.asNode(), property.asNode(), l.asNode());
        testChangeObject(() -> securedStatement.changeObject("dos", "es"), t);
    }

    @Test
    public void testChangeObject_langString_notwellformed() {
        final Literal l = ResourceFactory.createLangLiteral("dos", "es");
        Triple t = Triple.create(subject.asNode(), property.asNode(), l.asNode());
        testChangeObject(() -> securedStatement.changeObject("dos", "es", false), t);
    }

    private void testCreateReifiedStatement(Supplier<ReifiedStatement> supplier, Resource expected) {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            SecuredReifiedStatement rs = (SecuredReifiedStatement) supplier.get();
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
            ReifiedStatement actual = (ReifiedStatement) rs.getBaseItem();
            if (expected == null) {
                assertTrue(actual.isAnon());
            } else {
                assertEquals(expected, actual);
            }

        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail("Should not have thrown AccessDeniedException Exception");
            }
        }

    }

    @Test
    public void testCreateReifiedStatement() {
        testCreateReifiedStatement(() -> securedStatement.createReifiedStatement(), null);
        Resource r = ResourceFactory.createResource("http://example.com/rsURI");
        testCreateReifiedStatement(() -> securedStatement.createReifiedStatement("http://example.com/rsURI"), r);
    }

    @Test
    public void testGetProperty() {
        // get property of the object
        baseModel.add(baseStatement.getObject().asResource(), property, ResourceFactory.createResource());
        Statement expected = baseStatement.getProperty(property);
        testGet(() -> securedStatement.getProperty(property), expected);
    }

    private <T> void testGet(Supplier<T> supplier, T expected) {
        try {
            T actual = supplier.get();
            if (!shouldRead()) { // securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            assertEquals(expected, actual);
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have thrown ReadDeniedException Exception");
            }
        }

    }

    @Test
    public void testGetBoolean() {
        setSecuredStatement(baseStatement.changeLiteralObject(true));
        testGet(() -> securedStatement.getBoolean(), true);
        setSecuredStatement(baseStatement.changeLiteralObject(false));
        testGet(() -> securedStatement.getBoolean(), false);
    }

    @Test
    public void testGetByte() {
        setSecuredStatement(baseStatement.changeLiteralObject(Byte.MAX_VALUE));
        testGet(() -> securedStatement.getByte(), Byte.MAX_VALUE);
        setSecuredStatement(baseStatement.changeLiteralObject(Byte.MIN_VALUE));
        testGet(() -> securedStatement.getByte(), Byte.MIN_VALUE);
    }

    @Test
    public void testGetChar() {
        setSecuredStatement(baseStatement.changeLiteralObject('c'));
        testGet(() -> securedStatement.getChar(), 'c');
    }

    @Test
    public void testGetDouble() {
        setSecuredStatement(baseStatement.changeLiteralObject(3.14d));
        testGet(() -> securedStatement.getDouble(), 3.14d);
    }

    @Test
    public void testGetFloat() {
        setSecuredStatement(baseStatement.changeLiteralObject(3.14f));
        testGet(() -> securedStatement.getFloat(), 3.14f);
    }

    @Test
    public void testGetInt() {
        setSecuredStatement(baseStatement.changeLiteralObject(3));
        testGet(() -> securedStatement.getInt(), 3);
    }

    @Test
    public void testGetLanguage() {
        setSecuredStatement(baseStatement.changeObject("dos", "es"));
        testGet(() -> securedStatement.getLanguage(), "es");
        setSecuredStatement(baseStatement.changeObject("plain"));
        testGet(() -> securedStatement.getLanguage(), "");
    }

    @Test
    public void testGetLong() {
        setSecuredStatement(baseStatement.changeLiteralObject(3L));
        testGet(() -> securedStatement.getLong(), 3L);
    }

    @Test
    public void testGetShort() {
        setSecuredStatement(baseStatement.changeLiteralObject(Short.MAX_VALUE));
        testGet(() -> securedStatement.getShort(), Short.MAX_VALUE);
        setSecuredStatement(baseStatement.changeLiteralObject(Short.MIN_VALUE));
        testGet(() -> securedStatement.getShort(), Short.MIN_VALUE);
    }

    @Test
    public void testGetString() {
        setSecuredStatement(baseStatement.changeObject("Whooo hooo"));
        testGet(() -> securedStatement.getString(), "Whooo hooo");
    }

    @Test
    public void testHasWellFormedXML_false() {
        setSecuredStatement(baseStatement.changeObject("Whooo hooo"));
        testGet(() -> securedStatement.hasWellFormedXML(), false);
    }

    @Test
    public void testHasWellFormedXML_true() {
        Literal l = ResourceFactory.createTypedLiteral("true", XMLLiteralType.theXMLLiteralType);
        setSecuredStatement(baseStatement.changeObject(l));
        testGet(() -> securedStatement.hasWellFormedXML(), true);
    }

    @Test
    public void testGetStatementProperty() {
        Resource resource = baseModel.getAnyReifiedStatement(baseStatement);
        resource.addProperty(p, o);
        Statement expected = baseStatement.getStatementProperty(p);
        try {
            Statement actual = securedStatement.getStatementProperty(p);
            if (!securityEvaluator.evaluate(Action.Read)) { // securityEvaluator.evaluate(Action.Read)) {
                Assert.fail("Should have thrown PropertyNotFoundException Exception");
            }
            assertEquals(expected, actual);
        } catch (final PropertyNotFoundException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have thrown PropertyNotFoundException Exception");
            }
        }

    }

    @Test
    public void testIsReified() {
        testGet(() -> securedStatement.isReified(), false);

        baseStatement.createReifiedStatement();
        testGet(() -> securedStatement.isReified(), securityEvaluator.evaluate(Action.Read));
    }

    @Test
    public void testListReifiedStatements() {
        baseStatement.createReifiedStatement();
        try {
            SecuredRSIterator iter = (SecuredRSIterator) securedStatement.listReifiedStatements();
            try {
                if (!shouldRead()) {
                    fail("Should have thrown ReadDeniedException Exception");
                }
                if (securedStatement.canRead()) {
                    assertTrue(iter.hasNext());
                } else {
                    assertFalse(iter.hasNext());
                }
            } finally {
                iter.close();
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException Exception");
            }
        }
    }

    @Test
    public void testRemove() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });
        assertTrue(baseModel.contains((Statement) securedStatement.getBaseItem()));

        try {
            SecuredStatement stmt = (SecuredStatement) securedStatement.remove();
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
            assertFalse(baseModel.contains((Statement) stmt.getBaseItem()));
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail("Should not have thrown AccessDeniedException Exception");
            }
        }
    }

    @Test
    public void testRemoveReification() {
        baseStatement.createReifiedStatement();
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });

        assertTrue(baseStatement.isReified());
        try {
            securedStatement.removeReification();
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
            assertFalse(baseStatement.isReified());
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail("Should not have thrown AccessDeniedException Exception");
            }
        }
    }

    @Test
    public void testGetAlt() {
        SecuredAlt alt = (SecuredAlt) securedStatement.getAlt();
        assertEquals(baseStatement.getAlt(), alt.getBaseItem());
    }

    @Test
    public void testGetBag() {
        SecuredBag bag = (SecuredBag) securedStatement.getBag();
        assertEquals(baseStatement.getBag(), bag.getBaseItem());
    }

    @Test
    public void testGetSeq() {
        SecuredSeq seq = (SecuredSeq) securedStatement.getSeq();
        assertEquals(baseStatement.getSeq(), seq.getBaseItem());
    }

    @Test
    public void testGetResource() {
        testGet(() -> securedStatement.getResource(), baseStatement.getResource());
    }

    @Test
    public void testGetSubject() {
        testGet(() -> securedStatement.getSubject(), baseStatement.getSubject());
    }

    @Test
    public void testGetLiteral() {
        setSecuredStatement(baseStatement.changeLiteralObject(true));
        SecuredLiteral l = (SecuredLiteral) securedStatement.getLiteral();
        assertEquals(baseStatement.getLiteral(), l.getBaseItem());

    }

}
