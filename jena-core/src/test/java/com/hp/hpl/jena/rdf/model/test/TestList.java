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

// Package
// /////////////
package com.hp.hpl.jena.rdf.model.test;

// Imports
// /////////////
import com.hp.hpl.jena.rdf.model.ListIndexException;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A collection of unit tests for the standard implementation of {@link RDFList}
 * .
 * </p>
 * 
 * 
 */
public class TestList extends AbstractModelTestBase
{
	// Constants
	// ////////////////////////////////

	public static final String NS = "uri:urn:x-rdf:test#";

	/** Test that an iterator delivers the expected values */
	protected static void iteratorTest( final Iterator<?> i,
			final Object[] expected )
	{
		final Logger logger = LoggerFactory.getLogger(TestList.class);
		final List<Object> expList = new ArrayList<>();
		for (final Object element : expected)
		{
			expList.add(element);
		}

		while (i.hasNext())
		{
			final Object next = i.next();

			// debugging
			if (!expList.contains(next))
			{
				logger.debug("TestList - Unexpected iterator result: " + next);
			}

			Assert.assertTrue("Value " + next
					+ " was not expected as a result from this iterator ",
					expList.contains(next));
			Assert.assertTrue("Value " + next
					+ " was not removed from the list ", expList.remove(next));
		}

		if (!(expList.size() == 0))
		{
			logger.debug("TestList - Expected iterator results not found");
			for (final Object object : expList)
			{
				logger.debug("TestList - missing: " + object);
			}
		}
		Assert.assertEquals(
				"There were expected elements from the iterator that were not found",
				0, expList.size());
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
	/*
	 * public static TestSuite suite() {
	 * TestSuite s = new TestSuite( "TestList" );
	 * TestList tl = new TestList();
	 * for (int i = 0; i <= 5; i++) {
	 * s.addTest( new CountTest( i ) );
	 * s.addTest( new TailTest( i ) );
	 * }
	 * 
	 * s.addTest( new ValidityTest() );
	 * s.addTest( new HeadTest() );
	 * s.addTest( new SetHeadTest() );
	 * s.addTest( new SetTailTest() );
	 * s.addTest( new ConsTest() );
	 * s.addTest( new AddTest() );
	 * s.addTest( new TestListGet() );
	 * s.addTest( new ReplaceTest() );
	 * s.addTest( new IndexTest1() );
	 * s.addTest( new IndexTest2() );
	 * s.addTest( new AppendTest() );
	 * s.addTest( new ConcatenateTest() );
	 * s.addTest( new ConcatenateTest2() );
	 * s.addTest( new ApplyTest() );
	 * s.addTest( new ReduceTest() );
	 * s.addTest( new RemoveTest() );
	 * s.addTest( new Map1Test() );
	 * s.addTest( new ListEqualsTest() );
	 * s.addTest( new ListSubclassTest() );
	 * s.addTest( new UserDefinedListTest() );
	 * 
	 * return s;
	 * }
	 */

	// public ListTest( String n ) {super(n);}

	public TestList( final TestingModelFactory modelFactory, final String name )
	{
		super(modelFactory, name);
	}

	protected void checkValid( final String testName, final RDFList l,
			final boolean validExpected )
	{
		l.setStrict(true);
		final boolean valid = l.isValid();
		// for debugging ... String s = l.getValidityErrorMessage();
		Assert.assertEquals("Validity test " + testName
				+ " returned wrong isValid() result", validExpected, valid);
	}

	// Internal implementation methods
	// ////////////////////////////////

	protected RDFList getListRoot( final Model m )
	{
		final Resource root = m.getResource(TestList.NS + "root");
		Assert.assertNotNull("Root resource should not be null", root);

		final Resource listHead = root.getRequiredProperty(
				m.getProperty(TestList.NS + "p")).getResource();

		final RDFList l = listHead.as(RDFList.class);
		Assert.assertNotNull("as(RDFList) should not return null for root", l);

		return l;
	}

	public void testAdd()
	{

		final Resource root = model.createResource(TestList.NS + "root");
		final Property p = model.createProperty(TestList.NS, "p");

		final Resource nil = model.getResource(RDF.nil.getURI());
		RDFList list = nil.as(RDFList.class);

		final Resource[] toAdd = new Resource[] {
				model.createResource(TestList.NS + "a"),
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "d"),
				model.createResource(TestList.NS + "e"), };

		// add each of these resources onto the end of the list
		for (final Resource element : toAdd)
		{
			final RDFList list0 = list.with(element);

			checkValid("addTest0", list0, true);
			Assert.assertTrue("added'ed lists should be equal",
					list.equals(nil) || list0.equals(list));

			list = list0;
		}

		// relate the root to the list
		model.add(root, p, list);

		// should be isomorphic with list 5
		final Model m0 = ModelFactory.createDefaultModel();
		m0.read( getFileName( "ontology/list5.rdf"));

		Assert.assertTrue("Add'ed and read models should be the same",
				m0.isIsomorphicWith(model));

	}

	public void testAppend()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final Resource nil = model.getResource(RDF.nil.getURI());
		RDFList list = nil.as(RDFList.class);

		final Resource r = model.createResource(TestList.NS + "foo");

		// create a list of foos
		for (int i = 0; i < 5; i++)
		{
			list = list.cons(r);
		}

		final int listLen = list.size();

		// now append foos to the root list
		final RDFList root = getListRoot(model);
		final int rootLen = root.size();

		final RDFList appended = root.append(list);

		// original list should be unchanged
		checkValid("appendTest0", root, true);
		Assert.assertEquals("Original list should be unchanged", rootLen,
				root.size());

		checkValid("appendTest1", list, true);
		Assert.assertEquals("Original list should be unchanged", listLen,
				list.size());

		// new list should be length of combined
		checkValid("appendTest2", appended, true);
		Assert.assertEquals("Appended list not correct length", rootLen
				+ listLen, appended.size());
	}

	public void testApply()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final RDFList root = getListRoot(model);

		class MyApply implements RDFList.ApplyFn
		{
			String collect = "";

			@Override
			public void apply( final RDFNode n )
			{
				collect = collect + ((Resource) n).getLocalName();
			}
		}

		final MyApply f = new MyApply();
		root.apply(f);

		Assert.assertEquals(
				"Result of apply should be concatentation of local names",
				"abcde", f.collect);

	}

	public void testConcatenate()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final Resource nil = model.getResource(RDF.nil.getURI());
		RDFList list = nil.as(RDFList.class);

		final Resource r = model.createResource(TestList.NS + "foo");

		// create a list of foos
		for (int i = 0; i < 5; i++)
		{
			list = list.cons(r);
		}

		final int listLen = list.size();

		// now append foos to the root list
		final RDFList root = getListRoot(model);
		final int rootLen = root.size();
		root.concatenate(list);

		// original list should be unchanged
		checkValid("concatTest0", list, true);
		Assert.assertEquals("Original list should be unchanged", listLen,
				list.size());

		// but lhs list has changed
		checkValid("concatTest1", root, true);
		Assert.assertEquals("Root list should be new length",
				rootLen + listLen, root.size());
	}

	public void testConcatenate2()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final Resource a = model.createResource(TestList.NS + "a");

		// create a list of foos
		final Resource[] rs = new Resource[] {
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "d"),
				model.createResource(TestList.NS + "e") };

		final RDFList aList = model.createList().cons(a);
		final RDFList rsList = model.createList(rs);

		// concatenate the above resources onto the empty list
		aList.concatenate(rsList);
		checkValid("concatTest3", aList, true);

		final RDFList root = getListRoot(model);
		Assert.assertTrue("Constructed and loaded lists should be the same",
				aList.sameListAs(root));
	}

	public void testCons()
	{
		final Resource root = model.createResource(TestList.NS + "root");
		final Property p = model.createProperty(TestList.NS, "p");

		final Resource nil = model.getResource(RDF.nil.getURI());
		RDFList list = nil.as(RDFList.class);

		final Resource[] toAdd = new Resource[] {
				model.createResource(TestList.NS + "e"),
				model.createResource(TestList.NS + "d"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "a"), };

		// cons each of these resources onto the front of the list
		for (final Resource element : toAdd)
		{
			final RDFList list0 = list.cons(element);

			checkValid("constest1", list0, true);
			Assert.assertTrue("cons'ed lists should not be equal",
					!list0.equals(list));

			list = list0;
		}

		// relate the root to the list
		model.add(root, p, list);

		// should be isomorphic with list 5
		final Model m0 = ModelFactory.createDefaultModel();
		m0.read(getFileName("ontology/list5.rdf"));

		Assert.assertTrue("Cons'ed and read models should be the same",
				m0.isIsomorphicWith(model));
	}

	public void testCount()
	{
		for (int i = 0; i <= 5; i++)
		{
			model.removeAll();
			model.read( getFileName("ontology/list" + i + ".rdf"));

			final RDFList l0 = getListRoot(model);
			Assert.assertEquals("List size should be " + i, i, l0.size());
		}

	}

	public void testHead()
	{
		model.read(getFileName("ontology/list5.rdf"));

		RDFList l0 = getListRoot(model);

		final String[] names = { "a", "b", "c", "d", "e" };
		for (final String name : names)
		{
			Assert.assertEquals("head of list has incorrect URI", TestList.NS
					+ name, ((Resource) l0.getHead()).getURI());
			l0 = l0.getTail();
		}
	}

	public void testIndex1()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final Resource[] toGet = new Resource[] {
				model.createResource(TestList.NS + "a"),
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "d"),
				model.createResource(TestList.NS + "e"), };

		final RDFList l1 = getListRoot(model);

		// check the indexes are correct
		for (int i = 0; i < toGet.length; i++)
		{
			Assert.assertTrue("list should contain element " + i,
					l1.contains(toGet[i]));
			Assert.assertEquals("list element " + i + " is not correct", i,
					l1.indexOf(toGet[i]));
		}
	}

	public void testIndex2()
	{

		final Resource nil = model.getResource(RDF.nil.getURI());
		RDFList list = nil.as(RDFList.class);

		final Resource r = model.createResource(TestList.NS + "a");

		// cons each a's onto the front of the list
		for (int i = 0; i < 10; i++)
		{
			list = list.cons(r);
		}

		// now index them back again
		for (int j = 0; j < 10; j++)
		{
			Assert.assertEquals("index of j'th item should be j", j,
					list.indexOf(r, j));
		}

	}

	public void testListEquals()
	{
		final Resource nil = model.getResource(RDF.nil.getURI());
		final RDFList nilList = nil.as(RDFList.class);

		// create a list of foos
		final Resource[] r0 = new Resource[] {
				model.createResource(TestList.NS + "a"), // canonical
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "d"),
				model.createResource(TestList.NS + "e") };
		final Resource[] r1 = new Resource[] {
				model.createResource(TestList.NS + "a"), // same
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "d"),
				model.createResource(TestList.NS + "e") };
		final Resource[] r2 = new Resource[] {
				model.createResource(TestList.NS + "a"), // one shorter
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "d") };
		final Resource[] r3 = new Resource[] {
				model.createResource(TestList.NS + "a"), // elements
				// swapped
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "d"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "e") };
		final Resource[] r4 = new Resource[] {
				model.createResource(TestList.NS + "a"), // different
				// name
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "D"),
				model.createResource(TestList.NS + "e") };

		final Object[][] testSpec = new Object[][] { { r0, r1, Boolean.TRUE },
				{ r0, r2, Boolean.FALSE }, { r0, r3, Boolean.FALSE },
				{ r0, r4, Boolean.FALSE }, { r1, r2, Boolean.FALSE },
				{ r1, r3, Boolean.FALSE }, { r1, r4, Boolean.FALSE },
				{ r2, r3, Boolean.FALSE }, { r2, r4, Boolean.FALSE }, };

		for (int i = 0; i < testSpec.length; i++)
		{
			final RDFList l0 = nilList.append(Arrays.asList(
					(Resource[]) testSpec[i][0]).iterator());
			final RDFList l1 = nilList.append(Arrays.asList(
					(Resource[]) testSpec[i][1]).iterator());
			final boolean expected = ((Boolean) testSpec[i][2]).booleanValue();

			Assert.assertEquals("sameListAs testSpec[" + i + "] incorrect",
					expected, l0.sameListAs(l1));
			Assert.assertEquals("sameListAs testSpec[" + i
					+ "] (swapped) incorrect", expected, l1.sameListAs(l0));
		}
	}

	public void testListGet()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final Resource[] toGet = new Resource[] {
				model.createResource(TestList.NS + "a"),
				model.createResource(TestList.NS + "b"),
				model.createResource(TestList.NS + "c"),
				model.createResource(TestList.NS + "d"),
				model.createResource(TestList.NS + "e"), };

		final RDFList l1 = getListRoot(model);

		// test normal gets
		for (int i = 0; i < toGet.length; i++)
		{
			Assert.assertEquals("list element " + i + " is not correct",
					toGet[i], l1.get(i));
		}

		// now test we get an exception for going beyong the end of the list
		boolean gotEx = false;
		try
		{
			l1.get(toGet.length + 1);
		}
		catch (final ListIndexException e)
		{
			gotEx = true;
		}

		Assert.assertTrue(
				"Should see exception raised by accessing beyond end of list",
				gotEx);
	}

	public void testMap1()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final RDFList root = getListRoot(model);
		TestList.iteratorTest(root.mapWith(new Map1<RDFNode, String>() {
			@Override
			public String map1( final RDFNode x )
			{
				return ((Resource) x).getLocalName();
			}
		}), new Object[] { "a", "b", "c", "d", "e" });

	}

	public void testReduce()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final RDFList root = getListRoot(model);

		final RDFList.ReduceFn f = new RDFList.ReduceFn() {
			@Override
			public Object reduce( final RDFNode n, final Object acc )
			{
				return ((String) acc) + ((Resource) n).getLocalName();
			}
		};

		Assert.assertEquals(
				"Result of reduce should be concatentation of local names",
				"abcde", root.reduce(f, ""));
	}

	public void testRemove()
	{

		final Resource nil = model.getResource(RDF.nil.getURI());
		RDFList list0 = nil.as(RDFList.class);
		RDFList list1 = nil.as(RDFList.class);

		final Resource r0 = model.createResource(TestList.NS + "x");
		final Resource r1 = model.createResource(TestList.NS + "y");
		final Resource r2 = model.createResource(TestList.NS + "z");

		for (int i = 0; i < 10; i++)
		{
			list0 = list0.cons(r0);
			list1 = list1.cons(r1);
		}

		// delete the elements of list0 one at a time
		while (!list0.isEmpty())
		{
			list0 = list0.removeHead();
			checkValid("removeTest0", list0, true);
		}

		// delete all of list1 in one go
		list1.removeList();

		// model should now be empty
		Assert.assertEquals("Model should be empty after deleting two lists",
				0, model.size());

		// selective remove
		RDFList list2 = (nil.as(RDFList.class)).cons(r2).cons(r1).cons(r0);

		Assert.assertTrue("list should contain x ", list2.contains(r0));
		Assert.assertTrue("list should contain y ", list2.contains(r1));
		Assert.assertTrue("list should contain z ", list2.contains(r2));

		list2 = list2.remove(r1);
		Assert.assertTrue("list should contain x ", list2.contains(r0));
		Assert.assertTrue("list should contain y ", !list2.contains(r1));
		Assert.assertTrue("list should contain z ", list2.contains(r2));

		list2 = list2.remove(r0);
		Assert.assertTrue("list should contain x ", !list2.contains(r0));
		Assert.assertTrue("list should contain y ", !list2.contains(r1));
		Assert.assertTrue("list should contain z ", list2.contains(r2));

		list2 = list2.remove(r2);
		Assert.assertTrue("list should contain x ", !list2.contains(r0));
		Assert.assertTrue("list should contain y ", !list2.contains(r1));
		Assert.assertTrue("list should contain z ", !list2.contains(r2));
		Assert.assertTrue("list should be empty", list2.isEmpty());
	}

	public void testReplace()
	{
		model.read(getFileName("ontology/list5.rdf"));

		final Literal[] toSet = new Literal[] { model.createLiteral("a"),
				model.createLiteral("b"), model.createLiteral("c"),
				model.createLiteral("d"), model.createLiteral("e"), };

		final RDFList l1 = getListRoot(model);

		// change all the values
		for (int i = 0; i < toSet.length; i++)
		{
			l1.replace(i, toSet[i]);
		}

		// then check them
		for (int i = 0; i < toSet.length; i++)
		{
			Assert.assertEquals("list element " + i + " is not correct",
					toSet[i], l1.get(i));
		}

		// now test we get an exception for going beyong the end of the list
		boolean gotEx = false;
		try
		{
			l1.replace(toSet.length + 1, toSet[0]);
		}
		catch (final ListIndexException e)
		{
			gotEx = true;
		}

		Assert.assertTrue(
				"Should see exception raised by accessing beyond end of list",
				gotEx);

	}

	public void testSetHead()
	{

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

		Assert.assertEquals("List head should be 'fred'", "fred",
				((Literal) l1.getHead()).getString());

		l1.setHead(model.createTypedLiteral(42));
		checkValid("sethead2", l1, true);
		Assert.assertEquals("List head should be '42'", 42,
				((Literal) l1.getHead()).getInt());

	}

	public void testSetTail()
	{
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
		Assert.assertNotNull("as(RDFList) should not return null for root", l2);
		checkValid("settail2", l2, true);

		Assert.assertEquals("l1 should have length 1", 1, l1.size());
		Assert.assertEquals("l2 should have length 1", 1, l2.size());

		// use set tail to join the lists together
		l1.setTail(l2);

		checkValid("settail3", l1, true);
		checkValid("settail4", l2, true);

		Assert.assertEquals("l1 should have length 2", 2, l1.size());
		Assert.assertEquals("l2 should have length 1", 1, l2.size());

	}

	public void testTail()
	{
		for (int i = 0; i <= 5; i++)
		{
			model.read( getFileName("ontology/list" + i + ".rdf"));

			RDFList l0 = getListRoot(model);

			// get the tail n times, should be nil at the end
			for (int j = 0; j < i; j++)
			{
				l0 = l0.getTail();
			}

			Assert.assertTrue("Should have reached the end of the list after "
					+ i + " getTail()'s", l0.isEmpty());
		}
	}

	public void testValidity()
	{

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

	// public void testListSubclass() {
	// String NS = "http://example.org/test#";
	// Resource a = model.createResource( NS + "a" );
	// Resource b = model.createResource( NS + "b" );
	//
	// Resource cell0 = model.createResource();
	// Resource cell1 = model.createResource();
	// cell0.addProperty( RDF.first, a );
	// cell0.addProperty( RDF.rest, cell1 );
	// cell1.addProperty( RDF.first, b );
	// cell1.addProperty( RDF.rest, RDF.nil );
	//
	// UserList ul = getUserListInstance(cell0);
	//
	// assertEquals( "User list length ", 2, ul.size() );
	// assertEquals( "head of user list ", a, ul.getHead() );
	//
	// RDFList l = ul.as( RDFList.class );
	// assertNotNull( "RDFList facet of user-defined list subclass", l );
	//
	// }
	//
	// /** A simple extension to RDFList to test user-subclassing of RDFList */
	// protected static interface UserList extends RDFList {
	// }
	//
	// /** Impl of a simple extension to RDFList to test user-subclassing of
	// RDFList */
	// protected static class UserListImpl extends RDFListImpl implements
	// UserList {
	// public UserListImpl( Node n, EnhGraph g ) {
	// super( n, g );
	// }
	// }
	//
	// public UserList getUserListInstance( Resource r )
	// {
	// return new UserListImpl( r.asNode(), (EnhGraph) model );
	// }
	//
	// public void testUserDefinedList() {
	// BuiltinPersonalities.model.add( UserDefList.class,
	// UserDefListImpl.factoryForTests );
	//
	// String NS = "http://example.org/test#";
	// Resource a = model.createResource( NS + "a" );
	// Resource b = model.createResource( NS + "b" );
	//
	// Resource empty = model.createResource( UserDefListImpl.NIL.getURI() );
	// UserDefList ul = empty.as( UserDefList.class );
	// assertNotNull( "UserList facet of empty list", ul );
	//
	// UserDefList ul0 = (UserDefList) ul.cons( b );
	// ul0 = (UserDefList) ul0.cons( a );
	// assertEquals( "should be length 2", 2, ul0.size() );
	// assertTrue( "first statement", model.contains( ul0,
	// UserDefListImpl.FIRST, a ) );
	// }
	//
	// protected static interface UserDefList extends RDFList {}
	//
	// protected static class UserDefListImpl extends RDFListImpl implements
	// UserDefList {
	// @SuppressWarnings("hiding") public static final String NS =
	// "http://example.org/testlist#";
	// public static final Property FIRST = ResourceFactory.createProperty(
	// NS+"first" );
	// public static final Property REST = ResourceFactory.createProperty(
	// NS+"rest" );
	// public static final Resource NIL = ResourceFactory.createResource(
	// NS+"nil" );
	// public static final Resource LIST = ResourceFactory.createResource(
	// NS+"List" );
	//
	// /**
	// * A factory for generating UserDefList facets from nodes in enhanced
	// graphs.
	// */
	// public static Implementation factoryForTests = new Implementation() {
	// @Override public EnhNode wrap( Node n, EnhGraph eg ) {
	// if (canWrap( n, eg )) {
	// UserDefListImpl impl = new UserDefListImpl( n, eg );
	//
	// Model model = impl.getModel();
	// impl.m_listFirst = FIRST.inModel( model );
	// impl.m_listRest = REST.inModel( model );
	// impl.m_listNil = NIL.inModel( model );
	// impl.m_listType = LIST.inModel( model );
	//
	// return impl;
	// }
	// else {
	// throw new JenaException( "Cannot convert node " + n + " to UserDefList");
	// }
	// }
	//
	// @Override public boolean canWrap( Node node, EnhGraph eg ) {
	// Graph g = eg.asGraph();
	//
	// return node.equals( NIL.asNode() ) ||
	// g.contains( node, FIRST.asNode(), Node.ANY ) ||
	// g.contains( node, REST.asNode(), Node.ANY ) ||
	// g.contains( node, RDF.type.asNode(), LIST.asNode() );
	// }
	// };
	//
	// /** This method returns the Java class object that defines which
	// abstraction facet is presented */
	// @Override public Class<? extends RDFList> listAbstractionClass() {
	// return UserDefList.class;
	// }
	//
	// public UserDefListImpl( Node n, EnhGraph g ) {
	// super( n, g );
	// }
	//
	// }

}
