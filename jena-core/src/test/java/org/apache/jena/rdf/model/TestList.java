/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdf.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

// Imports
// /////////////
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A collection of unit tests for the standard implementation of {@link RDFList} .
 * </p>
 */
@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestList extends AbstractModelTestBase {
    // Constants
    // ////////////////////////////////

    public static final String NS = "uri:urn:x-rdf:test#";

    /** Test that an iterator delivers the expected values */
    protected static void iteratorTest(final Iterator<? > i, final Object[] expected) {
        final Logger logger = LoggerFactory.getLogger(TestList.class);
        final List<Object> expList = new ArrayList<>();
        for ( final Object element : expected ) {
            expList.add(element);
        }

        while (i.hasNext()) {
            final Object next = i.next();

            // debugging
            if ( !expList.contains(next) ) {
                logger.debug("TestList - Unexpected iterator result: " + next);
            }

            assertTrue(expList.contains(next), "Value " + next + " was not expected as a result from this iterator ");
            assertTrue(expList.remove(next), "Value " + next + " was not removed from the list ");
        }

        if ( !(expList.size() == 0) ) {
            logger.debug("TestList - Expected iterator results not found");
            for ( final Object object : expList ) {
                logger.debug("TestList - missing: " + object);
            }
        }
        assertEquals(0, expList.size(), "There were expected elements from the iterator that were not found");
    }

    // Static variables
    // ////////////////////////////////

    // Instance variables
    // ////////////////////////////////

    // Constructors
    // ////////////////////////////////

    // public TestList( String name ) {
    // super( name );
    // }

    // External signature methods
    // ////////////////////////////////
    /* public static TestSuite suite() { TestSuite s = new TestSuite( "TestList" );
     * TestList tl = new TestList(); for (int i = 0; i <= 5; i++) { s.addTest( new
     * CountTest( i ) ); s.addTest( new TailTest( i ) ); }
     * 
     * s.addTest( new ValidityTest() ); s.addTest( new HeadTest() ); s.addTest( new
     * SetHeadTest() ); s.addTest( new SetTailTest() ); s.addTest( new ConsTest() );
     * s.addTest( new AddTest() ); s.addTest( new TestListGet() ); s.addTest( new
     * ReplaceTest() ); s.addTest( new IndexTest1() ); s.addTest( new IndexTest2() );
     * s.addTest( new AppendTest() ); s.addTest( new ConcatenateTest() ); s.addTest(
     * new ConcatenateTest2() ); s.addTest( new ApplyTest() ); s.addTest( new
     * ReduceTest() ); s.addTest( new RemoveTest() ); s.addTest( new Map1Test() );
     * s.addTest( new ListEqualsTest() ); s.addTest( new ListSubclassTest() );
     * s.addTest( new UserDefinedListTest() );
     * 
     * return s; } */

    // public ListTest( String n ) {super(n);}

    protected void checkValid(final String testName, final RDFList l, final boolean validExpected) {
        l.setStrict(true);
        final boolean valid = l.isValid();
        // for debugging ... String s = l.getValidityErrorMessage();
        assertEquals(validExpected, valid, "Validity test " + testName + " returned wrong isValid() result");
    }

    // Internal implementation methods
    // ////////////////////////////////

    protected RDFList getListRoot(final Model m) {
        final Resource root = m.getResource(TestList.NS + "root");
        assertNotNull(root, "Root resource should not be null");

        final Resource listHead = root.getRequiredProperty(m.getProperty(TestList.NS + "p")).getResource();

        final RDFList l = listHead.as(RDFList.class);
        assertNotNull(l, "as(RDFList) should not return null for root");

        return l;
    }

    @Test
    public void testAdd() {

        final Resource root = model.createResource(TestList.NS + "root");
        final Property p = model.createProperty(TestList.NS, "p");

        final Resource nil = model.getResource(RDF.nil.getURI());
        RDFList list = nil.as(RDFList.class);

        final Resource[] toAdd = new Resource[]{model.createResource(TestList.NS + "a"), model.createResource(TestList.NS + "b"),
            model.createResource(TestList.NS + "c"), model.createResource(TestList.NS + "d"), model.createResource(TestList.NS + "e"),};

        // add each of these resources onto the end of the list
        for ( final Resource element : toAdd ) {
            final RDFList list0 = list.with(element);

            checkValid("addTest0", list0, true);
            assertTrue(list.equals(nil) || list0.equals(list), "added'ed lists should be equal");

            list = list0;
        }

        // relate the root to the list
        model.add(root, p, list);

        // should be isomorphic with list 5
        final Model m0 = ModelFactory.createDefaultModel();
        m0.read(getFileName("ontology/list5.rdf"));

        assertTrue(m0.isIsomorphicWith(model), "Add'ed and read models should be the same");

    }

    @Test
    public void testAppend() {
        model.read(getFileName("ontology/list5.rdf"));

        final Resource nil = model.getResource(RDF.nil.getURI());
        RDFList list = nil.as(RDFList.class);

        final Resource r = model.createResource(TestList.NS + "foo");

        // create a list of foos
        for ( int i = 0 ; i < 5 ; i++ ) {
            list = list.cons(r);
        }

        final int listLen = list.size();

        // now append foos to the root list
        final RDFList root = getListRoot(model);
        final int rootLen = root.size();

        final RDFList appended = root.append(list);

        // original list should be unchanged
        checkValid("appendTest0", root, true);
        assertEquals(rootLen, root.size(), "Original list should be unchanged");

        checkValid("appendTest1", list, true);
        assertEquals(listLen, list.size(), "Original list should be unchanged");

        // new list should be length of combined
        checkValid("appendTest2", appended, true);
        assertEquals(rootLen + listLen, appended.size(), "Appended list not correct length");
    }

    @Test
    public void testApply() {
        model.read(getFileName("ontology/list5.rdf"));

        final RDFList root = getListRoot(model);

        class MyApply implements RDFList.ApplyFn {
            String collect = "";

            @Override
            public void apply(final RDFNode n) {
                collect = collect + ((Resource)n).getLocalName();
            }
        }

        final MyApply f = new MyApply();
        root.apply(f);

        assertEquals("abcde", f.collect, "Result of apply should be concatentation of local names");

    }

    @Test
    public void testConcatenate() {
        model.read(getFileName("ontology/list5.rdf"));

        final Resource nil = model.getResource(RDF.nil.getURI());
        RDFList list = nil.as(RDFList.class);

        final Resource r = model.createResource(TestList.NS + "foo");

        // create a list of foos
        for ( int i = 0 ; i < 5 ; i++ ) {
            list = list.cons(r);
        }

        final int listLen = list.size();

        // now append foos to the root list
        final RDFList root = getListRoot(model);
        final int rootLen = root.size();
        root.concatenate(list);

        // original list should be unchanged
        checkValid("concatTest0", list, true);
        assertEquals(listLen, list.size(), "Original list should be unchanged");

        // but lhs list has changed
        checkValid("concatTest1", root, true);
        assertEquals(rootLen + listLen, root.size(), "Root list should be new length");
    }

    @Test
    public void testConcatenate2() {
        model.read(getFileName("ontology/list5.rdf"));

        final Resource a = model.createResource(TestList.NS + "a");

        // create a list of foos
        final Resource[] rs = new Resource[]{model.createResource(TestList.NS + "b"), model.createResource(TestList.NS + "c"),
            model.createResource(TestList.NS + "d"), model.createResource(TestList.NS + "e")};

        final RDFList aList = model.createList().cons(a);
        final RDFList rsList = model.createList(rs);

        // concatenate the above resources onto the empty list
        aList.concatenate(rsList);
        checkValid("concatTest3", aList, true);

        final RDFList root = getListRoot(model);
        assertTrue(aList.sameListAs(root), "Constructed and loaded lists should be the same");
    }

    @Test
    public void testCons() {
        final Resource root = model.createResource(TestList.NS + "root");
        final Property p = model.createProperty(TestList.NS, "p");

        final Resource nil = model.getResource(RDF.nil.getURI());
        RDFList list = nil.as(RDFList.class);

        final Resource[] toAdd = new Resource[]{model.createResource(TestList.NS + "e"), model.createResource(TestList.NS + "d"),
            model.createResource(TestList.NS + "c"), model.createResource(TestList.NS + "b"), model.createResource(TestList.NS + "a"),};

        // cons each of these resources onto the front of the list
        for ( final Resource element : toAdd ) {
            final RDFList list0 = list.cons(element);

            checkValid("constest1", list0, true);
            assertTrue(!list0.equals(list), "cons'ed lists should not be equal");

            list = list0;
        }

        // relate the root to the list
        model.add(root, p, list);

        // should be isomorphic with list 5
        final Model m0 = ModelFactory.createDefaultModel();
        m0.read(getFileName("ontology/list5.rdf"));

        assertTrue(m0.isIsomorphicWith(model), "Cons'ed and read models should be the same");
    }

    @Test
    public void testCount() {
        for ( int i = 0 ; i <= 5 ; i++ ) {
            model.removeAll();
            model.read(getFileName("ontology/list" + i + ".rdf"));

            final RDFList l0 = getListRoot(model);
            assertEquals(i, l0.size(), "List size should be " + i);
        }

    }

    @Test
    public void testHead() {
        model.read(getFileName("ontology/list5.rdf"));

        RDFList l0 = getListRoot(model);

        final String[] names = {"a", "b", "c", "d", "e"};
        for ( final String name : names ) {
            assertEquals(TestList.NS + name, ((Resource)l0.getHead()).getURI(), "head of list has incorrect URI");
            l0 = l0.getTail();
        }
    }

    @Test
    public void testIndex1() {
        model.read(getFileName("ontology/list5.rdf"));

        final Resource[] toGet = new Resource[]{model.createResource(TestList.NS + "a"), model.createResource(TestList.NS + "b"),
            model.createResource(TestList.NS + "c"), model.createResource(TestList.NS + "d"), model.createResource(TestList.NS + "e"),};

        final RDFList l1 = getListRoot(model);

        // check the indexes are correct
        for ( int i = 0 ; i < toGet.length ; i++ ) {
            assertTrue(l1.contains(toGet[i]), "list should contain element " + i);
            assertEquals(i, l1.indexOf(toGet[i]), "list element " + i + " is not correct");
        }
    }

    @Test
    public void testIndex2() {

        final Resource nil = model.getResource(RDF.nil.getURI());
        RDFList list = nil.as(RDFList.class);

        final Resource r = model.createResource(TestList.NS + "a");

        // cons each a's onto the front of the list
        for ( int i = 0 ; i < 10 ; i++ ) {
            list = list.cons(r);
        }

        // now index them back again
        for ( int j = 0 ; j < 10 ; j++ ) {
            assertEquals(j, list.indexOf(r, j), "index of j'th item should be j");
        }

    }

    @Test
    public void testListEquals() {
        final Resource nil = model.getResource(RDF.nil.getURI());
        final RDFList nilList = nil.as(RDFList.class);

        // create a list of foos
        final Resource[] r0 = new Resource[]{model.createResource(TestList.NS + "a"), // canonical
            model.createResource(TestList.NS + "b"), model.createResource(TestList.NS + "c"), model.createResource(TestList.NS + "d"),
            model.createResource(TestList.NS + "e")};
        final Resource[] r1 = new Resource[]{model.createResource(TestList.NS + "a"), // same
            model.createResource(TestList.NS + "b"), model.createResource(TestList.NS + "c"), model.createResource(TestList.NS + "d"),
            model.createResource(TestList.NS + "e")};
        final Resource[] r2 = new Resource[]{model.createResource(TestList.NS + "a"), // one
                                                                                      // shorter
            model.createResource(TestList.NS + "b"), model.createResource(TestList.NS + "c"), model.createResource(TestList.NS + "d")};
        final Resource[] r3 = new Resource[]{model.createResource(TestList.NS + "a"), // elements
            // swapped
            model.createResource(TestList.NS + "b"), model.createResource(TestList.NS + "d"), model.createResource(TestList.NS + "c"),
            model.createResource(TestList.NS + "e")};
        final Resource[] r4 = new Resource[]{model.createResource(TestList.NS + "a"), // different
            // name
            model.createResource(TestList.NS + "b"), model.createResource(TestList.NS + "c"), model.createResource(TestList.NS + "D"),
            model.createResource(TestList.NS + "e")};

        final Object[][] testSpec = new Object[][]{{r0, r1, Boolean.TRUE}, {r0, r2, Boolean.FALSE}, {r0, r3, Boolean.FALSE},
            {r0, r4, Boolean.FALSE}, {r1, r2, Boolean.FALSE}, {r1, r3, Boolean.FALSE}, {r1, r4, Boolean.FALSE}, {r2, r3, Boolean.FALSE},
            {r2, r4, Boolean.FALSE},};

        for ( int i = 0 ; i < testSpec.length ; i++ ) {
            final RDFList l0 = nilList.append(Arrays.asList((Resource[])testSpec[i][0]).iterator());
            final RDFList l1 = nilList.append(Arrays.asList((Resource[])testSpec[i][1]).iterator());
            final boolean expected = ((Boolean)testSpec[i][2]).booleanValue();

            assertEquals(expected, l0.sameListAs(l1), "sameListAs testSpec[" + i + "] incorrect");
            assertEquals(expected, l1.sameListAs(l0), "sameListAs testSpec[" + i + "] (swapped) incorrect");
        }
    }

    @Test
    public void testListGet() {
        model.read(getFileName("ontology/list5.rdf"));

        final Resource[] toGet = new Resource[]{model.createResource(TestList.NS + "a"), model.createResource(TestList.NS + "b"),
            model.createResource(TestList.NS + "c"), model.createResource(TestList.NS + "d"), model.createResource(TestList.NS + "e"),};

        final RDFList l1 = getListRoot(model);

        // test normal gets
        for ( int i = 0 ; i < toGet.length ; i++ ) {
            assertEquals(toGet[i], l1.get(i), "list element " + i + " is not correct");
        }

        // now test we get an exception for going beyong the end of the list
        boolean gotEx = false;
        try {
            l1.get(toGet.length + 1);
        } catch (final ListIndexException e) {
            gotEx = true;
        }

        assertTrue(gotEx, "Should see exception raised by accessing beyond end of list");
    }

    @Test
    public void testMap1() {
        model.read(getFileName("ontology/list5.rdf"));

        final RDFList root = getListRoot(model);
        TestList.iteratorTest(root.mapWith(n -> ((Resource)n).getLocalName()), new Object[]{"a", "b", "c", "d", "e"});

    }

    @Test
    public void testReduce() {
        model.read(getFileName("ontology/list5.rdf"));

        final RDFList root = getListRoot(model);

        final RDFList.ReduceFn f = new RDFList.ReduceFn() {
            @Override
            public Object reduce(final RDFNode n, final Object acc) {
                return ((String)acc) + ((Resource)n).getLocalName();
            }
        };

        assertEquals("abcde", root.reduce(f, ""), "Result of reduce should be concatentation of local names");
    }

    @Test
    public void testRemove() {

        final Resource nil = model.getResource(RDF.nil.getURI());
        RDFList list0 = nil.as(RDFList.class);
        RDFList list1 = nil.as(RDFList.class);

        final Resource r0 = model.createResource(TestList.NS + "x");
        final Resource r1 = model.createResource(TestList.NS + "y");
        final Resource r2 = model.createResource(TestList.NS + "z");

        for ( int i = 0 ; i < 10 ; i++ ) {
            list0 = list0.cons(r0);
            list1 = list1.cons(r1);
        }

        // delete the elements of list0 one at a time
        while (!list0.isEmpty()) {
            list0 = list0.removeHead();
            checkValid("removeTest0", list0, true);
        }

        // delete all of list1 in one go
        list1.removeList();

        // model should now be empty
        assertEquals(0, model.size(), "Model should be empty after deleting two lists");

        // selective remove
        RDFList list2 = (nil.as(RDFList.class)).cons(r2).cons(r1).cons(r0);

        assertTrue(list2.contains(r0), "list should contain x ");
        assertTrue(list2.contains(r1), "list should contain y ");
        assertTrue(list2.contains(r2), "list should contain z ");

        list2 = list2.remove(r1);
        assertTrue(list2.contains(r0), "list should contain x ");
        assertTrue(!list2.contains(r1), "list should contain y ");
        assertTrue(list2.contains(r2), "list should contain z ");

        list2 = list2.remove(r0);
        assertTrue(!list2.contains(r0), "list should contain x ");
        assertTrue(!list2.contains(r1), "list should contain y ");
        assertTrue(list2.contains(r2), "list should contain z ");

        list2 = list2.remove(r2);
        assertTrue(!list2.contains(r0), "list should contain x ");
        assertTrue(!list2.contains(r1), "list should contain y ");
        assertTrue(!list2.contains(r2), "list should contain z ");
        assertTrue(list2.isEmpty(), "list should be empty");
    }

    @Test
    public void testReplace() {
        model.read(getFileName("ontology/list5.rdf"));

        final Literal[] toSet = new Literal[]{model.createLiteral("a"), model.createLiteral("b"), model.createLiteral("c"),
            model.createLiteral("d"), model.createLiteral("e"),};

        final RDFList l1 = getListRoot(model);

        // change all the values
        for ( int i = 0 ; i < toSet.length ; i++ ) {
            l1.replace(i, toSet[i]);
        }

        // then check them
        for ( int i = 0 ; i < toSet.length ; i++ ) {
            assertEquals(toSet[i], l1.get(i), "list element " + i + " is not correct");
        }

        // now test we get an exception for going beyong the end of the list
        boolean gotEx = false;
        try {
            l1.replace(toSet.length + 1, toSet[0]);
        } catch (final ListIndexException e) {
            gotEx = true;
        }

        assertTrue(gotEx, "Should see exception raised by accessing beyond end of list");

    }

    @Test
    public void testSetHead() {

        final Resource root = model.createResource(TestList.NS + "root");
        final Property p = model.createProperty(TestList.NS, "p");

        // a list of the nil object, but not typed
        final Resource nil = RDF.nil;
        model.add(nil, RDF.type, RDF.List);

        final Resource list = model.createResource();
        model.add(list, RDF.type, RDF.List);
        model.add(list, RDF.first, "fred");
        model.add(list, RDF.rest, nil);

        model.add(root, p, list);
        final RDFList l1 = getListRoot(model);
        checkValid("sethead1", l1, true);

        assertEquals("fred", ((Literal)l1.getHead()).getString(), "List head should be 'fred'");

        l1.setHead(model.createTypedLiteral(42));
        checkValid("sethead2", l1, true);
        assertEquals(42, ((Literal)l1.getHead()).getInt(), "List head should be '42'");

    }

    @Test
    public void testSetTail() {
        final Model m = ModelFactory.createDefaultModel();

        final Resource root = m.createResource(TestList.NS + "root");
        final Property p = m.createProperty(TestList.NS, "p");

        final Resource nil = RDF.nil;
        m.add(nil, RDF.type, RDF.List);

        final Resource list0 = m.createResource();
        m.add(list0, RDF.type, RDF.List);
        m.add(list0, RDF.first, "fred");
        m.add(list0, RDF.rest, nil);

        m.add(root, p, list0);
        final RDFList l1 = getListRoot(m);
        checkValid("settail1", l1, true);

        final Resource list1 = m.createResource();
        m.add(list1, RDF.type, RDF.List);
        m.add(list1, RDF.first, "george");
        m.add(list1, RDF.rest, nil);

        final RDFList l2 = list1.as(RDFList.class);
        assertNotNull(l2, "as(RDFList) should not return null for root");
        checkValid("settail2", l2, true);

        assertEquals(1, l1.size(), "l1 should have length 1");
        assertEquals(1, l2.size(), "l2 should have length 1");

        // use set tail to join the lists together
        l1.setTail(l2);

        checkValid("settail3", l1, true);
        checkValid("settail4", l2, true);

        assertEquals(2, l1.size(), "l1 should have length 2");
        assertEquals(1, l2.size(), "l2 should have length 1");

    }

    @Test
    public void testTail() {
        for ( int i = 0 ; i <= 5 ; i++ ) {
            model.read(getFileName("ontology/list" + i + ".rdf"));

            RDFList l0 = getListRoot(model);

            // get the tail n times, should be nil at the end
            for ( int j = 0 ; j < i ; j++ ) {
                l0 = l0.getTail();
            }

            assertTrue(l0.isEmpty(), "Should have reached the end of the list after " + i + " getTail()'s");
        }
    }

    @Test
    public void testValidity() {
        final Resource root = model.createResource(TestList.NS + "root");
        final Property p = model.createProperty(TestList.NS, "p");

        // a list of the nil object, but not typed
        final Resource nil = RDF.nil;
        model.add(root, p, nil);
        final RDFList l0 = getListRoot(model);
        checkValid("valid1", l0, true);

        // add another node to the head of the list
        final Resource badList = model.createResource();
        model.getRequiredProperty(root, p).remove();
        model.add(root, p, badList);
        model.add(badList, RDF.type, RDF.List);

        final RDFList l1 = getListRoot(model);
        checkValid("valid2", l1, false);

        // checkValid( "valid3", l1, false );

        model.add(badList, RDF.first, "fred");
        checkValid("valid4", l1, false);

        model.add(badList, RDF.rest, nil);
        checkValid("valid5", l1, true);
    }

    @Test
    public void testStmtGetList() {
        Resource root = model.createResource(TestList.NS + "root");
        Property p = model.createProperty(TestList.NS, "p");
        Resource r = model.createResource(TestList.NS + "r");
        Resource r1 = model.createResource(TestList.NS + "r1");
        Resource r2 = model.createResource(TestList.NS + "r2");

        RDFList list0 = model.createList(r1, r2);
        model.add(r, p, list0);

        Resource obj = model.listStatements(r, p, (Resource)null).next().getResource();

        RDFList list1 = model.getList(obj);

        boolean b = list0.sameListAs(list1);
        assertTrue(b, "Different lists: expected: " + list0 + " : got: " + list1);
    }

    @Test
    public void testModelGetList() {
        Resource root = model.createResource(TestList.NS + "root");
        Property p = model.createProperty(TestList.NS, "p");
        Resource r = model.createResource(TestList.NS + "r");
        Resource r1 = model.createResource(TestList.NS + "r1");
        Resource r2 = model.createResource(TestList.NS + "r2");

        RDFList list0 = model.createList(r1, r2);
        model.add(r, p, list0);

        RDFList list1 = model.listStatements(r, p, (Resource)null).next().getList();

        boolean b = list0.sameListAs(list1);
        assertTrue(b, "Different lists: expected: " + list0 + " : got: " + list1);
    }

    @Test
    public void testModelGetEmptyList() {
        Resource root = model.createResource(TestList.NS + "root");
        Property p = model.createProperty(TestList.NS, "p");
        Resource r = model.createResource(TestList.NS + "r");

        RDFList list0 = model.createList();
        model.add(r, p, list0);

        RDFList list1 = model.listStatements(r, p, (Resource)null).next().getList();

        boolean b = list0.sameListAs(list1);
        assertTrue(b, "Different lists: expected: " + list0 + " : got: " + list1);
    }

}
