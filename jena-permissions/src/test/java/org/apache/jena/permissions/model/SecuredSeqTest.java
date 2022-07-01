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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.function.Supplier;

import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.model.impl.SecuredSeqImpl;
import org.apache.jena.rdf.model.Alt;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.rdf.model.SeqIndexBoundsException;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredSeqTest extends SecuredContainerTest {
    private Seq seq;

    public SecuredSeqTest(final MockSecurityEvaluator securityEvaluator) {
        super(securityEvaluator);
    }

    private SecuredSeq getSecuredSeq() {
        return (SecuredSeq) getSecuredRDFNode();
    }

    @Override
    @Before
    public void setup() {
        super.setup();
        seq = baseModel.createSeq(SecuredRDFNodeTest.s.getURI());
        setSecuredRDFNode(SecuredSeqImpl.getInstance(securedModel, seq), seq);
    }

    private <T> void testAdd_idx(Supplier<Seq> supplier, T expected, Supplier<T> test) {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            SecuredSeq securedSeq = (SecuredSeq) supplier.get();
            if (!securityEvaluator.evaluate(perms)) {
                fail("Should have thrown AccessDeniedException Exception");
            }
            assertNotNull(securedSeq);
            assertEquals(expected, test.get());
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail("Should not have thrown AccessDeniedException Exception");
            }
        }
    }

    @Test
    public void testAdd_idx_boolean() {
        testAdd_idx(() -> getSecuredSeq().add(1, true), true, () -> seq.getBoolean(1));
        testAdd_idx(() -> getSecuredSeq().add(1, false), false, () -> seq.getBoolean(1));
    }

    @Test
    public void testAdd_idx_char() {
        testAdd_idx(() -> getSecuredSeq().add(1, 'c'), 'c', () -> seq.getChar(1));
    }

    @Test
    public void testAdd_idx_double() {
        testAdd_idx(() -> getSecuredSeq().add(1, 3.14d), 3.14d, () -> seq.getDouble(1));
    }

    @Test
    public void testAdd_idx_float() {
        testAdd_idx(() -> getSecuredSeq().add(1, 3.14F), 3.14f, () -> seq.getFloat(1));
    }

    @Test
    public void testAdd_idx_long() {
        testAdd_idx(() -> getSecuredSeq().add(1, 3L), 3l, () -> seq.getLong(1));
    }

    @Test
    public void testAdd_idx_object() {
        final Object o = Integer.MAX_VALUE;
        Literal l = ResourceFactory.createTypedLiteral(o);
        testAdd_idx(() -> getSecuredSeq().add(1, o), l, () -> seq.getObject(1));
    }

    @Test
    public void testAdd_idx_Resource() {
        Resource r = ResourceFactory.createResource();
        testAdd_idx(() -> getSecuredSeq().add(1, r), r, () -> seq.getResource(1));
    }

    @Test
    public void testAdd_idx_String() {
        testAdd_idx(() -> getSecuredSeq().add(1, "Waa hoo"), "Waa hoo", () -> seq.getString(1));
    }

    @Test
    public void testAdd_idx_LangString() {
        Literal l = ResourceFactory.createLangLiteral("dos", "es");
        testAdd_idx(() -> getSecuredSeq().add(1, "dos", "es"), l, () -> seq.getLiteral(1));
    }

    private <T> void testGet(Supplier<T> supplier, T expected, Class<? extends T> clazz) {
        try {
            T actual = supplier.get();
            if (clazz != null) {
                Assert.assertTrue(String.format("Should be a %s", clazz), clazz.isAssignableFrom(actual.getClass()));
            }
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            assertEquals(expected, actual);
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                Assert.fail("Should not have thrown ReadDeniedException Exception");
            }
        } catch (SeqIndexBoundsException e) {
            if (!securityEvaluator.evaluate(Action.Read) && securityEvaluator.isHardReadError()) {
                Assert.fail("Should not have thrown SeqIndexBoundsException Exception");
            }
        }

    }

    @Test
    public void testGetAlt() {
        Alt a = seq.getModel().createAlt();
        a.add(false);
        seq.add(1, a);
        testGet(() -> getSecuredSeq().getAlt(1), a, SecuredAlt.class);
    }

    @Test
    public void testGetBag() {
        Bag b = seq.getModel().createBag();
        b.add(false);
        seq.add(1, b);
        testGet(() -> getSecuredSeq().getBag(1), b, SecuredBag.class);
    }

    @Test
    public void testGetBoolean() {
        seq.add(1, true);
        testGet(() -> getSecuredSeq().getBoolean(1), true, null);
    }

    @Test
    public void testGetByte() {
        seq.add(1, Byte.MAX_VALUE);
        testGet(() -> getSecuredSeq().getByte(1), Byte.MAX_VALUE, Byte.class);
    }

    @Test
    public void testGetChar() {
        seq.add(1, 'c');
        testGet(() -> getSecuredSeq().getChar(1), 'c', null);
    }

    @Test
    public void testGetDouble() {
        seq.add(1, 3.14D);
        testGet(() -> getSecuredSeq().getDouble(1), 3.14D, null);
    }

    @Test
    public void testGetFloat() {
        seq.add(1, 3.14F);
        testGet(() -> getSecuredSeq().getFloat(1), 3.14F, null);
    }

    @Test
    public void testGetInt() {
        seq.add(1, 2);
        testGet(() -> getSecuredSeq().getInt(1), 2, null);
    }

    @Test
    public void testGetLanguage() {
        seq.add(1, "foo");
        seq.add(2, "three", "en");
        seq.add(3, "quatro", "es");
        testGet(() -> getSecuredSeq().getLanguage(1), "", null);
        testGet(() -> getSecuredSeq().getLanguage(2), "en", null);
        testGet(() -> getSecuredSeq().getLanguage(3), "es", null);
    }

    @Test
    public void testGetLiteral() {
        seq.add(1, "foo");
        seq.add(2, "three", "en");
        seq.add(3, "quatro", "es");
        testGet(() -> getSecuredSeq().getLiteral(1), ResourceFactory.createPlainLiteral("foo"), null);
        testGet(() -> getSecuredSeq().getLiteral(2), ResourceFactory.createLangLiteral("three", "en"), null);
        testGet(() -> getSecuredSeq().getLiteral(3), ResourceFactory.createLangLiteral("quatro", "es"), null);
    }

    @Test
    public void testGetLong() {
        seq.add(1, 2L);
        testGet(() -> getSecuredSeq().getLong(1), 2L, null);
    }

    @Test
    public void testGetObject() {
        final Object o = Integer.MAX_VALUE;
        final Literal l = ResourceFactory.createTypedLiteral(o);
        seq.add(1, o);
        testGet(() -> getSecuredSeq().getObject(1), l, null);
    }

    @Test
    public void testGetResource() {
        Resource r1 = ResourceFactory.createResource();
        Resource r2 = ResourceFactory.createResource("http://example.com/2");
        seq.add(1, r1);
        seq.add(2, r2);
        testGet(() -> getSecuredSeq().getResource(1), r1, SecuredResource.class);
        testGet(() -> getSecuredSeq().getResource(2), r2, SecuredResource.class);
    }

    @Test
    public void testGetSeq() {
        Seq seq2 = seq.getModel().createSeq();
        seq.add(1, seq2);
        testGet(() -> getSecuredSeq().getSeq(1), seq2, SecuredSeq.class);
    }

    @Test
    public void testGetShort() {
        seq.add(1, Short.MAX_VALUE);
        testGet(() -> getSecuredSeq().getShort(1), Short.MAX_VALUE, null);
    }

    @Test
    public void testGetString() {
        seq.add(1, "Waaa hoo");
        testGet(() -> getSecuredSeq().getString(1), "Waaa hoo", null);
    }

    private void testIndex(Supplier<Integer> supplier, int expected) {
        try {
            int actual = supplier.get();
            if (!shouldRead()) {
                Assert.fail("Should have thrown ReadDeniedException Exception");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(expected, actual);
            } else {
                assertEquals(0, actual);
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException Exception");
            }
        }

    }

    @Test
    public void testIndexOf_boolean() {
        seq.add(true);
        seq.add(false);
        testIndex(() -> getSecuredSeq().indexOf(true), 1);
        testIndex(() -> getSecuredSeq().indexOf(false), 2);
    }

    @Test
    public void testIndexOf_char() {
        seq.add('c');
        seq.add('d');
        testIndex(() -> getSecuredSeq().indexOf('c'), 1);
        testIndex(() -> getSecuredSeq().indexOf('d'), 2);
    }

    @Test
    public void testIndexOf_double() {
        seq.add(3.14d);
        seq.add(1.57d);
        testIndex(() -> getSecuredSeq().indexOf(3.14d), 1);
        testIndex(() -> getSecuredSeq().indexOf(1.57d), 2);
    }

    @Test
    public void testIndexOf_float() {
        seq.add(3.14f);
        seq.add(1.57f);
        testIndex(() -> getSecuredSeq().indexOf(3.14f), 1);
        testIndex(() -> getSecuredSeq().indexOf(1.57f), 2);
    }

    @Test
    public void testIndexOf_long() {
        seq.add(3L);
        seq.add(1L);
        testIndex(() -> getSecuredSeq().indexOf(3L), 1);
        testIndex(() -> getSecuredSeq().indexOf(1L), 2);
    }

    @Test
    public void testIndexOf_object() {
        Object o1 = Integer.MAX_VALUE;
        Object o2 = Long.MAX_VALUE;
        seq.add(o1);
        seq.add(o2);
        testIndex(() -> getSecuredSeq().indexOf(o1), 1);
        testIndex(() -> getSecuredSeq().indexOf(o2), 2);
    }

    @Test
    public void testIndexOf_resource() {
        Resource r1 = ResourceFactory.createResource();
        Resource r2 = ResourceFactory.createResource("http://example.com/2");
        seq.add(r1);
        seq.add(r2);
        testIndex(() -> getSecuredSeq().indexOf(r1), 1);
        testIndex(() -> getSecuredSeq().indexOf(r2), 2);
    }

    @Test
    public void testIndexOf_string() {
        String s1 = "waaa";
        String s2 = "hoo";
        seq.add(s1);
        seq.add(s2);
        testIndex(() -> getSecuredSeq().indexOf(s1), 1);
        testIndex(() -> getSecuredSeq().indexOf(s2), 2);
    }

    @Test
    public void testIndexOf_langString() {
        Literal l1 = ResourceFactory.createLangLiteral("uno", "es");
        Literal l2 = ResourceFactory.createLangLiteral("uno", "en");
        seq.add(l1);
        seq.add(l2);
        testIndex(() -> getSecuredSeq().indexOf("uno", "es"), 1);
        testIndex(() -> getSecuredSeq().indexOf("uno", "en"), 2);
    }

    @Override
    @Test
    public void testRemove() {
        seq.add("The thing");
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });
        try {
            SecuredSeq result = (SecuredSeq) getSecuredSeq().remove(1);
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown AccessDeniedException Exception");
            }
            assertFalse(seq.contains("The thing"));
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail("Should not have thrown AccessDeniedException Exception");
            }
        }
    }

    private <T> void testSet_idx(Supplier<Seq> supplier, T expected, Supplier<T> test) {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update });
        try {
            seq.add("A dummy entry");
            SecuredSeq securedSeq = (SecuredSeq) supplier.get();
            if (!securityEvaluator.evaluate(perms)) {
                Assert.fail("Should have thrown UpdateDeniedException Exception");
            }
            assertNotNull(securedSeq);
            assertEquals(expected, test.get());
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail("Should not have thrown UpdateDeniedException Exception");
            }
        }

    }

    @Test
    public void testSet_idx_boolean() {
        testSet_idx(() -> getSecuredSeq().set(1, true), true, () -> seq.getBoolean(1));
        testSet_idx(() -> getSecuredSeq().set(1, false), false, () -> seq.getBoolean(1));
    }

    @Test
    public void testSet_idx_char() {
        testSet_idx(() -> getSecuredSeq().set(1, 'c'), 'c', () -> seq.getChar(1));
    }

    @Test
    public void testSet_idx_double() {
        testSet_idx(() -> getSecuredSeq().set(1, 3.14d), 3.14d, () -> seq.getDouble(1));
    }

    @Test
    public void testSet_idx_float() {
        testSet_idx(() -> getSecuredSeq().set(1, 3.14F), 3.14f, () -> seq.getFloat(1));
    }

    @Test
    public void testSet_idx_long() {
        testSet_idx(() -> getSecuredSeq().set(1, 3L), 3l, () -> seq.getLong(1));
    }

    @Test
    public void testSet_idx_object() {
        final Object o = Integer.MAX_VALUE;
        Literal l = ResourceFactory.createTypedLiteral(o);
        testSet_idx(() -> getSecuredSeq().set(1, o), l, () -> seq.getObject(1));
    }

    @Test
    public void testSet_idx_Resource() {
        Resource r = ResourceFactory.createResource();
        testSet_idx(() -> getSecuredSeq().set(1, r), r, () -> seq.getResource(1));
    }

    @Test
    public void testSet_idx_String() {
        testSet_idx(() -> getSecuredSeq().set(1, "Waa hoo"), "Waa hoo", () -> seq.getString(1));
    }

    @Test
    public void testSet_idx_LangString() {
        Literal l = ResourceFactory.createLangLiteral("dos", "es");
        testSet_idx(() -> getSecuredSeq().set(1, "dos", "es"), l, () -> seq.getLiteral(1));
    }

}
