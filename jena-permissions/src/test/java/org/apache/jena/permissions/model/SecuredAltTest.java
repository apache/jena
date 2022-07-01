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
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.model.impl.SecuredAltImpl;
import org.apache.jena.rdf.model.Alt;
import org.apache.jena.rdf.model.AltHasNoDefaultException;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredAltTest extends SecuredContainerTest {
    private Alt alt;

    public SecuredAltTest(final MockSecurityEvaluator securityEvaluator) {
        super(securityEvaluator);
    }

    private SecuredAlt getSecuredAlt() {
        return (SecuredAlt) getSecuredRDFNode();
    }

    @Override
    @Before
    public void setup() {
        super.setup();
        alt = baseModel.createAlt(SecuredRDFNodeTest.s.getURI());
        setSecuredRDFNode(SecuredAltImpl.getInstance(securedModel, alt), alt);
    }

    private <T> void testGetDefault(Supplier<T> supplier, T expected) {
        try {
            T actual = supplier.get();
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(expected, actual);
            } else {
                fail("Should have throws AltHasNoDefaultException");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail(String.format("Should not have thrown ReadDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        } catch (final AltHasNoDefaultException e) {
            boolean noAlt = expected == null;
            noAlt |= (!securityEvaluator.evaluate(Action.Read) && !securityEvaluator.isHardReadError());
            if (!noAlt) {
                Assert.fail("Should not have thrown AltHasNoDefaultException Exception");

            }

        }

    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this, RDF.li(1), o )
     */
    @Test
    public void testGetDefault() {
        alt.add("SomeDummyItem");
        testGetDefault(() -> getSecuredAlt().getDefault(), ResourceFactory.createPlainLiteral("SomeDummyItem"));
    }

    @Test
    public void testGetDefaultAlt() {
        Alt alt2 = baseModel.createAlt("urn:alt2");
        alt.setDefault(alt2);
        testGetDefault(() -> getSecuredAlt().getDefaultAlt(), alt2);
    }

    @Test
    public void testGetDefaultBag() {
        Bag bag = baseModel.createBag("urn:alt2");
        alt.setDefault(bag);
        testGetDefault(() -> getSecuredAlt().getDefaultBag(), bag);
    }

    @Test
    public void testGetDefaultSeq() {
        Seq seq = baseModel.createSeq("urn:alt2");
        alt.setDefault(seq);
        testGetDefault(() -> getSecuredAlt().getDefaultSeq(), seq);
    }

    @Test
    public void testGetDefaultBoolean() {
        alt.setDefault(Boolean.TRUE);
        testGetDefault(() -> getSecuredAlt().getDefaultBoolean(), Boolean.TRUE);
    }

    @Test
    public void testGetDefaultByte() {
        alt.setDefault(Byte.MAX_VALUE);
        testGetDefault(() -> getSecuredAlt().getDefaultByte(), Byte.MAX_VALUE);
    }

    @Test
    public void testGetDefaultChar() {
        alt.setDefault('c');
        testGetDefault(() -> getSecuredAlt().getDefaultChar(), 'c');
    }

    @Test
    public void testGetDefaultDouble() {
        alt.setDefault(3.14d);
        testGetDefault(() -> getSecuredAlt().getDefaultDouble(), Double.valueOf(3.14d));
    }

    @Test
    public void testGetDefaultFloat() {
        alt.setDefault(3.14f);
        testGetDefault(() -> getSecuredAlt().getDefaultFloat(), Float.valueOf(3.14f));
    }

    @Test
    public void testGetDefaultInt() {
        alt.setDefault(2);
        testGetDefault(() -> getSecuredAlt().getDefaultInt(), Integer.valueOf(2));
    }

    @Test
    public void testGetDefaultLiteral_NoLanguage() {
        Literal expected = ResourceFactory.createStringLiteral("SomeDummyItem");
        alt.setDefault(expected);
        testGetDefault(() -> getSecuredAlt().getDefaultLanguage(), "");
        testGetDefault(() -> getSecuredAlt().getDefaultLiteral(), expected);
    }

    @Test
    public void testGetDefaultLiteral_Language() {
        Literal expected = ResourceFactory.createLangLiteral("Hola", "es");
        alt.setDefault(expected);
        testGetDefault(() -> getSecuredAlt().getDefaultLanguage(), "es");
        testGetDefault(() -> getSecuredAlt().getDefaultLiteral(), expected);
    }

    @Test
    public void testGetDefaultLong() {
        alt.add(3L);
        testGetDefault(() -> getSecuredAlt().getDefaultLong(), Long.valueOf(3l));
    }

    @Test
    public void testGetDefaultResource() {
        Resource expected = ResourceFactory.createResource("http://example.com/exampleResourec");
        alt.setDefault(expected);
        testGetDefault(() -> getSecuredAlt().getDefaultResource(), expected);
    }

    @Test
    public void testGetDefaultShort() {
        alt.setDefault(Short.MAX_VALUE);
        testGetDefault(() -> getSecuredAlt().getDefaultShort(), Short.MAX_VALUE);
    }

    @Test
    public void testGetDefaultString() {
        alt.setDefault("Hello World");
        testGetDefault(() -> getSecuredAlt().getDefaultString(), "Hello World");
    }

    private <T> void testSetDefault(Consumer<T> consumer, Supplier<T> supplier, T expected) {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            consumer.accept(expected);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException");
            }
            T actual = supplier.get();
            assertEquals(expected, actual);
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            consumer.accept(expected);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException on update");
            }
            T actual = supplier.get();
            assertEquals(expected, actual);
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail("Should not have thrown AccessDeniedException on Update");
            }
        }

    }

    @Test
    public void testSetDefaultBoolean() {
        testSetDefault((b) -> getSecuredAlt().setDefault(b.booleanValue()), () -> alt.getDefaultBoolean(),
                Boolean.TRUE);
    }

    @Test
    public void testSetDefaultChar() {
        testSetDefault((b) -> getSecuredAlt().setDefault(b), () -> alt.getDefaultChar(), 'c');
    }

    @Test
    public void testSetDefaultDouble() {
        testSetDefault((b) -> getSecuredAlt().setDefault(b), () -> alt.getDefaultDouble(), Double.valueOf(3.14d));
    }

    @Test
    public void testSetDefaultFloat() {
        testSetDefault((b) -> getSecuredAlt().setDefault(b), () -> alt.getDefaultFloat(), Float.valueOf(3.14f));
    }

    @Test
    public void testSetDefaultLong() {
        testSetDefault((b) -> getSecuredAlt().setDefault(b), () -> alt.getDefaultLong(), Long.valueOf(2L));
    }

    @Test
    public void testSetDefaultObject() {
        Object o = 2;
        Literal expected = ResourceFactory.createTypedLiteral(o);
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            getSecuredAlt().setDefault(o);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException");
            }
            RDFNode actual = alt.getDefault();
            assertEquals(expected, actual);
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            getSecuredAlt().setDefault(o);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException on update");
            }
            RDFNode actual = alt.getDefault();
            assertEquals(expected, actual);
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail("Should not have thrown AccessDeniedException on Update");
            }
        }
    }

    @Test
    public void testSetDefaultResource() {
        Resource expected = ResourceFactory.createResource("http://example.com/resource");
        testSetDefault((b) -> getSecuredAlt().setDefault(b), () -> alt.getDefaultResource(), expected);
    }

    @Test
    public void testSetDefaultString() {
        testSetDefault((b) -> getSecuredAlt().setDefault(b), () -> alt.getDefaultString(), "Test");
    }

    @Test
    public void testSetDefaultStringAndLang() {
        final Literal expected = ResourceFactory.createLangLiteral("dos", "es");
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            getSecuredAlt().setDefault("dos", "es");
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException");
            }
            Literal actual = alt.getDefaultLiteral();
            assertEquals(expected, actual);
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail(String.format("Should not have thrown AccessDeniedException Exception: %s - %s", e,
                        e.getTriple()));
            }
        }

        try {
            getSecuredAlt().setDefault("dos", "es");
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException on update");
            }
            Literal actual = alt.getDefaultLiteral();
            assertEquals(expected, actual);
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                Assert.fail("Should not have thrown AccessDeniedException on Update");
            }
        }

    }

}
