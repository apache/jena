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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.model.impl.SecuredRDFListImpl;
import org.apache.jena.permissions.utils.RDFListIterator;
import org.apache.jena.permissions.utils.RDFListSecFilter;
import org.apache.jena.rdf.model.EmptyListException;
import org.apache.jena.rdf.model.ListIndexException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFList.ApplyFn;
import org.apache.jena.rdf.model.RDFList.ReduceFn;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = SecurityEvaluatorParameters.class)
public class SecuredRDFListTest extends SecuredResourceTest {
    private RDFList baseList;
    private Resource resource1 = ResourceFactory.createResource("http://example.com/ListNode1");
    private Resource resource2 = ResourceFactory.createResource("http://example.com/ListNode2");
    private Resource resource3 = ResourceFactory.createResource("http://example.com/ListNode3");
    private Resource resource4 = ResourceFactory.createResource("http://example.com/ListNode4");

    private Resource newResource1 = ResourceFactory.createResource("http://example.com/NewNode1");
    private Resource newResource2 = ResourceFactory.createResource("http://example.com/NewNode2");
    private Resource newResource3 = ResourceFactory.createResource("http://example.com/NewNode3");
    private Resource newResource4 = ResourceFactory.createResource("http://example.com/NewNode4");

    public SecuredRDFListTest(final MockSecurityEvaluator securityEvaluator) {
        super(securityEvaluator);
    }

    private int count(final Action action) {
        final Iterator<RDFList> iter = new RDFListIterator((RDFList) getBaseRDFNode());
        return WrappedIterator.create(iter).filterKeep(new RDFListSecFilter<>(getSecuredRDFList(), action)).toList()
                .size();
    }

    private SecuredRDFList getSecuredRDFList() {
        return (SecuredRDFList) getSecuredRDFNode();
    }

    @Override
    @Before
    public void setup() {
        super.setup();
        baseModel.removeAll();

        final RDFNode[] listElements = { resource1, resource2, resource3, resource4 };
        baseList = baseModel.createList(listElements);
        addSPO(baseList);
        setSecuredRDFNode(SecuredRDFListImpl.getInstance(securedModel, baseList), baseList);
    }

    @Test
    public void testAdd() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            getSecuredRDFList().add(baseModel.createResource());
            if (!securityEvaluator.evaluate(perms)) {
                fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }

    }

    @Test
    public void testAppendNodeIterator() {
        Iterator<Resource> newIter = Arrays.asList(newResource1, newResource2, newResource3, newResource4).iterator();
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            SecuredRDFList actual = (SecuredRDFList) getSecuredRDFList().append(newIter);
            if (!securityEvaluator.evaluate(perms) || !shouldRead()) {
                fail("Should have thrown AccessDeniedException");
            }
            Iterator<RDFNode> iter = ((RDFList) actual.getBaseItem()).iterator();
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(resource1, iter.next());
                assertEquals(resource2, iter.next());
                assertEquals(resource3, iter.next());
                assertEquals(resource4, iter.next());
            }
            assertEquals(newResource1, iter.next());
            assertEquals(newResource2, iter.next());
            assertEquals(newResource3, iter.next());
            assertEquals(newResource4, iter.next());
            assertFalse(iter.hasNext());
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms) && shouldRead()) {
                fail("Should not have thrown AccessDeniedException");
            }
        }
    }

    @Test
    public void testAppendRDFList() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        Model m = ModelFactory.createDefaultModel();
        RDFList lst = m.createList(newResource1, newResource2, newResource3, newResource4);
        try {
            SecuredRDFList actual = (SecuredRDFList) getSecuredRDFList().append(lst);
            if (!securityEvaluator.evaluate(perms) || !shouldRead()) {
                fail("Should have thrown AccessDeniedException");
            }
            Iterator<RDFNode> iter = ((RDFList) actual.getBaseItem()).iterator();
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(resource1, iter.next());
                assertEquals(resource2, iter.next());
                assertEquals(resource3, iter.next());
                assertEquals(resource4, iter.next());
            }
            assertEquals(newResource1, iter.next());
            assertEquals(newResource2, iter.next());
            assertEquals(newResource3, iter.next());
            assertEquals(newResource4, iter.next());
            assertFalse(iter.hasNext());
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms) && shouldRead()) {
                fail("Should not have thrown AccessDeniedException");
            }
        }
    }

    private class TestApply implements ApplyFn {
        int cnt = 0;

        @Override
        public void apply(final RDFNode node) {
            cnt++;
        }
    }

    @Test
    public void testApply() {

        TestApply fn = new TestApply();

        try {
            getSecuredRDFList().apply(fn);
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(4, fn.cnt);
            } else {
                assertEquals(0, fn.cnt);
            }

        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }

        fn = new TestApply();
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Read });
        try {
            getSecuredRDFList().apply(perms, fn);
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read) && securityEvaluator.evaluate(Action.Update)) {
                assertEquals(4, fn.cnt);
            } else {
                assertEquals(0, fn.cnt);
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }
    }

    @Test
    public void testAsJaveList() {
        try {
            List<RDFNode> lst = getSecuredRDFList().asJavaList();
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(4, lst.size());
                for (RDFNode n : lst) {
                    assertEquals("Should be in secured model", securedModel, n.getModel());
                }
            } else {
                assertEquals(0, lst.size());
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }

    }

    @Test
    public void testConcatenate() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            getSecuredRDFList().concatenate(baseModel.listObjects());
            if (!securityEvaluator.evaluate(perms)) {
                fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }

        try {
            final List<Resource> lst = new ArrayList<>();
            lst.add(ResourceFactory.createResource("http://example.com/dummyList"));
            getSecuredRDFList().concatenate(baseModel.createList(lst.iterator()));
            if (!securityEvaluator.evaluate(perms)) {
                fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testCons() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });
        try {
            getSecuredRDFList().cons(SecuredRDFNodeTest.s);
            if (!securityEvaluator.evaluate(perms)) {
                fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testContains() {
        try {
            boolean actual = getSecuredRDFList().contains(resource2);
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertTrue(actual);
            } else {
                assertFalse(actual);
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }
    }

    @Test
    public void testCopy() {
        final Set<Action> perms = SecurityEvaluator.Util
                .asSet(new Action[] { Action.Read, Action.Update, Action.Create });
        try {
            getSecuredRDFList().copy();
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail("Should not have thrown AccessDeniedException");
            }
        }
    }

    @Test
    public void testGet() {
        try {
            RDFNode actual = getSecuredRDFList().get(0);
            if (!securityEvaluator.evaluate(Action.Read)) {
                if (securityEvaluator.isHardReadError()) {
                    fail("Should have thrown ReadDeniedException");
                } else {
                    fail("Should have thrown ListIndexException");
                }
            }
            assertEquals(resource1, actual);
            assertEquals(securedModel, actual.getModel());
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail(String.format("Should not have thrown ReadDeniedException: %s - %s", e, e.getTriple()));
            }
        } catch (final ListIndexException e) {
            boolean expected = !securityEvaluator.isHardReadError();
            if (!expected) {
                fail("Should not have thrown ListIndexException");
            }
        }
    }

    @Test
    public void testGetHead() {
        try {
            RDFNode actual = getSecuredRDFList().getHead();
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(actual, getBaseRDFNode().as(RDFList.class).getHead());
            } else {
                fail("Should have thrown ListIndexException");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        } catch (final ListIndexException e) {
            if (getBaseRDFNode().as(RDFList.class).size() > 0 && securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have throw ListIndexException");
            }
        }
    }

    @Test
    public void testGetTail() {
        try {
            RDFList actual = getSecuredRDFList().getTail();
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (!securityEvaluator.evaluate(Action.Read)) {
                fail("Should have thrown ListIndexException");
            }
            Iterator<RDFNode> actualI = actual.asJavaList().iterator();
            Iterator<RDFNode> expectedI = getBaseRDFNode().as(RDFList.class).getTail().asJavaList().iterator();
            while (expectedI.hasNext()) {
                assertEquals(expectedI.next(), actualI.next());
            }
            assertFalse(actualI.hasNext());
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        } catch (final ListIndexException e) {
            if (((RDFList) getBaseRDFNode()).size() > 0 && securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have thrown ListIndexException");
            }
        }
    }

    @Test
    public void testGetValidityErrorMessage() {
        try {
            getSecuredRDFList().getValidityErrorMessage();
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }
    }

    @Test
    public void testIndexOf() {
        try {
            int expected = baseList.indexOf(resource2);
            int actual = getSecuredRDFList().indexOf(resource2);
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(expected, actual);
            } else {
                assertEquals(-1, actual);
            }

        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have thrown ReadDeniedException");
            }
        }

        try {
            baseList.add(resource1);
            int expected = baseList.indexOf(resource1, 1);
            int actual = getSecuredRDFList().indexOf(resource1, 1);
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(expected, actual);
            } else {
                assertEquals(-1, actual);
            }

        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have thrown ReadDeniedException");
            }
        }
    }

    @Test
    public void testIsEmpty() {
        try {
            boolean actual = getSecuredRDFList().isEmpty();
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertFalse(actual);
            } else {
                assertTrue(actual);
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail(String.format("Should not have thrown ReadDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testIterator() {
        try {
            ExtendedIterator<RDFNode> iter = getSecuredRDFList().iterator();
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertTrue(iter.hasNext());
                List<RDFNode> lst = iter.toList();
                assertEquals(4, lst.size());
            } else {
                assertFalse(iter.hasNext());
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }

        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        try {
            ExtendedIterator<RDFNode> iter = getSecuredRDFList().iterator(perms);
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            perms.add(Action.Read);
            if (securityEvaluator.evaluate(perms)) {
                assertTrue(iter.hasNext());
                List<RDFNode> lst = iter.toList();
                assertEquals(4, lst.size());
            } else {
                assertFalse(iter.hasNext());
            }

        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }
    }

    @Test
    public void testReduce() {

        final ReduceFn fn = new ReduceFn() {

            @SuppressWarnings("unchecked")
            @Override
            public Object reduce(final RDFNode node, final Object accumulator) {
                ((List<RDFNode>) accumulator).add(node);
                return accumulator;
            }
        };

        try {
            @SuppressWarnings("unchecked")
            List<RDFNode> lst = (List<RDFNode>) getSecuredRDFList().reduce(fn, new ArrayList<RDFNode>());
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(4, lst.size());
            } else {
                assertTrue(lst.isEmpty());
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }

        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        try {
            @SuppressWarnings("unchecked")
            List<RDFNode> lst = (List<RDFNode>) getSecuredRDFList().reduce(perms, fn, new ArrayList<RDFNode>());
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            perms.add(Action.Read);
            if (securityEvaluator.evaluate(perms)) {
                assertEquals(4, lst.size());
            } else {
                assertTrue(lst.isEmpty());
            }

        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }
    }

    @Test
    public void testRemove() {
        try {
            getSecuredRDFList().remove(resource2);
            if (!securityEvaluator.evaluate(Action.Update) || !securityEvaluator.evaluate(Action.Delete)) {
                fail("Should have thrown AccessDeniedException");
            }
            Iterator<RDFNode> iter = getBaseRDFNode().as(RDFList.class).asJavaList().iterator();
            assertEquals(resource1, iter.next());
            assertEquals(resource3, iter.next());
            assertEquals(resource4, iter.next());
            assertFalse(iter.hasNext());
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(Action.Update) && securityEvaluator.evaluate(Action.Delete)) {
                fail("Should not have thrown AccessDeniedException");
            }
        }
    }

    @Test
    public void testRemoveHead() {
        try {
            RDFList rdfList = getSecuredRDFList().removeHead();
            if (!shouldRead() || !securityEvaluator.evaluate(Action.Update)
                    || !securityEvaluator.evaluate(Action.Delete)) {
                fail("Should have thrown AccessDeniedException");
            }
            Iterator<RDFNode> iter = rdfList.asJavaList().iterator();
            assertEquals(resource2, iter.next());
            assertEquals(resource3, iter.next());
            assertEquals(resource4, iter.next());
            assertFalse(iter.hasNext());
        } catch (final AccessDeniedException e) {
            if (shouldRead() && securityEvaluator.evaluate(Action.Update)
                    && securityEvaluator.evaluate(Action.Delete)) {
                fail("Should not have thrown AccessDeniedException");
            }
        } catch (final EmptyListException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have thrown EmptyListException");
            }
        }
    }

    @Override
    @Test
    public void testRemoveList() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Delete });

        try {
            final int count = count(Action.Delete);
            getSecuredRDFList().removeList();
            if (!securityEvaluator.evaluate(Action.Update)
                    || ((count > 0) && !securityEvaluator.evaluate(Action.Delete))) {
                fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testReplace() {
        try {
            RDFNode result = getSecuredRDFList().replace(1, newResource1);

            if (!shouldRead() || !securityEvaluator.evaluate(Action.Update)) {
                fail("Should have thrown AccessDeniedException");
            }
            assertEquals(resource2, result);
            List<RDFNode> lst = ((RDFList) getBaseRDFNode()).asJavaList();
            Iterator<RDFNode> iter = lst.iterator();
            assertEquals(resource1, iter.next());
            assertEquals(newResource1, iter.next());
            assertEquals(resource3, iter.next());
            assertEquals(resource4, iter.next());
            assertFalse(iter.hasNext());
        } catch (final AccessDeniedException e) {
            if (shouldRead() && securityEvaluator.evaluate(Action.Update)
                    && securityEvaluator.evaluate(Action.Delete)) {
                fail("Should not have thrown AccessDeniedException");
            }
        } catch (final ListIndexException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have thrown ListIndexException");
            }
        }
    }

    @Test
    public void testSameListAs() {
        try {
            boolean actual = getSecuredRDFList().sameListAs(baseModel.createList());

            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (!securityEvaluator.evaluate(Action.Read)) {
                assertTrue(actual);
            } else {
                assertFalse(actual);
            }

        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail("Should not have thrown ReadDeniedException");
            }
        }
    }

    @Test
    public void testSetHead() {
        try {
            RDFNode result = getSecuredRDFList().setHead(newResource1);

            if (!shouldRead() || !securityEvaluator.evaluate(Action.Update)) {
                fail("Should have thrown AccessDeniedException");
            }
            assertEquals(resource1, result);
            List<RDFNode> lst = ((RDFList) getBaseRDFNode()).asJavaList();
            Iterator<RDFNode> iter = lst.iterator();
            assertEquals(newResource1, iter.next());
            assertEquals(resource2, iter.next());
            assertEquals(resource3, iter.next());
            assertEquals(resource4, iter.next());
            assertFalse(iter.hasNext());
        } catch (final AccessDeniedException e) {
            if (shouldRead() && securityEvaluator.evaluate(Action.Update)
                    && securityEvaluator.evaluate(Action.Delete)) {
                fail("Should not have thrown AccessDeniedException");
            }
        } catch (final EmptyListException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                fail("Should not have thrown EmptyListException");
            }
        }
    }

    @Test
    public void testSetStrict() {
        try {
            getSecuredRDFList().setStrict(true);
            if (!securityEvaluator.evaluate(Action.Update)) {
                fail("Should have thrown UpdateDeniedException");
            }
        } catch (final UpdateDeniedException e) {
            if (securityEvaluator.evaluate(Action.Update)) {
                fail(String.format("Should not have thrown UpdateDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testSize() {
        try {
            int actual = getSecuredRDFList().size();
            if (!shouldRead()) {
                fail("Should have thrown ReadDeniedException");
            }
            if (securityEvaluator.evaluate(Action.Read)) {
                assertEquals(4, actual);
            } else {
                assertEquals(0, actual);
            }
        } catch (final ReadDeniedException e) {
            if (shouldRead()) {
                fail(String.format("Should not have thrown ReadDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testValid() {
        try {
            getSecuredRDFList().isValid();
            if (!securityEvaluator.evaluate(Action.Read)) {
                fail("Should have thrown ReadDeniedException");
            }
        } catch (final ReadDeniedException e) {
            if (securityEvaluator.evaluate(Action.Read)) {
                fail(String.format("Should not have thrown ReadDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

    @Test
    public void testWith() {
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Update, Action.Create });

        try {
            getSecuredRDFList().with(SecuredRDFNodeTest.s);
            if (!securityEvaluator.evaluate(perms)) {
                fail("Should have thrown AccessDeniedException");
            }
        } catch (final AccessDeniedException e) {
            if (securityEvaluator.evaluate(perms)) {
                fail(String.format("Should not have thrown AccessDeniedException: %s - %s", e, e.getTriple()));
            }
        }
    }

}
