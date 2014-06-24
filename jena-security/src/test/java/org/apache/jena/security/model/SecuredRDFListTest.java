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
package org.apache.jena.security.model;

import com.hp.hpl.jena.rdf.model.EmptyListException;
import com.hp.hpl.jena.rdf.model.ListIndexException;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFList.ApplyFn;
import com.hp.hpl.jena.rdf.model.RDFList.ReduceFn;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluatorParameters;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.model.SecuredRDFList;
import org.apache.jena.security.model.impl.SecuredRDFListImpl;
import org.apache.jena.security.utils.RDFListIterator;
import org.apache.jena.security.utils.RDFListSecFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( value = SecurityEvaluatorParameters.class )
public class SecuredRDFListTest extends SecuredResourceTest
{
	private RDFList baseList;
	
	public SecuredRDFListTest( final MockSecurityEvaluator securityEvaluator )
	{
		super(securityEvaluator);
	}

	private int count( final Action action )
	{
		final Iterator<RDFList> iter = new RDFListIterator(
				(RDFList) getBaseRDFNode());
		return WrappedIterator.create(iter)
				.filterKeep(new RDFListSecFilter<RDFList>(getSecuredRDFList(), action))
				.toList().size();
	}

	private int count( final Set<Action> action )
	{
		final Iterator<RDFList> iter = new RDFListIterator(
				(RDFList) getBaseRDFNode());
		return WrappedIterator.create(iter)
				.filterKeep(new RDFListSecFilter<RDFList>(getSecuredRDFList(), action))
				.toList().size();
	}

	private SecuredRDFList getSecuredRDFList()
	{
		return (SecuredRDFList) getSecuredRDFNode();
	}

	@Override
	@Before
	public void setup()
	{
		super.setup();
		final RDFNode[] listElements = {
				ResourceFactory.createResource("http://example.com/ListNode1"),
				ResourceFactory.createResource("http://example.com/ListNode2"),
				ResourceFactory.createResource("http://example.com/ListNode3"),
				ResourceFactory.createResource("http://example.com/ListNode4") };
		baseList = baseModel.createList(listElements);
		setSecuredRDFNode(SecuredRDFListImpl.getInstance(securedModel, baseList), baseList);
	}

	@Test
	public void testAdd()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredRDFList().add(baseModel.createResource());
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testAppendNodeIterator()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredRDFList().append(baseModel.listObjects());
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}
	
	@Test
	public void testAppendRDFList()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });

		try {
			getSecuredRDFList().append(baseModel.createList());
			if (!securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
			if (!securityEvaluator.evaluate(Action.Create) && (baseList.size()>0 && securityEvaluator.evaluate(Action.Read) ))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}

		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testApply()
	{

		final ApplyFn fn = new ApplyFn() {

			@Override
			public void apply( final RDFNode node )
			{
				// do nothing
			}
		};

		try
		{
			getSecuredRDFList().apply(fn);
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Read });
		try
		{
			getSecuredRDFList().apply(perms, fn);
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testAsJaveList()
	{
		try
		{
			getSecuredRDFList().asJavaList();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testConcatenate()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredRDFList().concatenate(baseModel.listObjects());
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			final List<Resource> lst = new ArrayList<Resource>();
			lst.add(ResourceFactory
					.createResource("http://example.com/dummyList"));
			getSecuredRDFList().concatenate(
					baseModel.createList(lst.iterator()));
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testCons()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });
		try
		{
			getSecuredRDFList().cons(SecuredRDFNodeTest.s);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testContains()
	{
		try
		{
			getSecuredRDFList().contains(SecuredRDFNodeTest.s);
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}
	
	@Test
	public void testCopy()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Read, Action.Update, Action.Create });
		try
		{
			getSecuredRDFList().copy();
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms) ) 
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGet()
	{
		try
		{
			getSecuredRDFList().get(0);
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final ListIndexException e)
		{
			if (((RDFList) getBaseRDFNode()).size() < 0)
			{
				// acceptable exception
			}
			else
			{
				throw e;
			}
		}
	}

	@Test
	public void testGetHead()
	{
		try
		{
			getSecuredRDFList().getHead();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final ListIndexException e)
		{
			if (((RDFList) getBaseRDFNode()).size() == 0)
			{
				// acceptable exception
			}
			else
			{
				throw e;
			}
		}
	}

	@Test
	public void testGetTail()
	{
		try
		{
			getSecuredRDFList().getTail();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final ListIndexException e)
		{
			if (((RDFList) getBaseRDFNode()).size() == 0)
			{
				// acceptable exception
			}
			else
			{
				throw e;
			}
		}
	}

	@Test
	public void testGetValidityErrorMessage()
	{
		try
		{
			getSecuredRDFList().getValidityErrorMessage();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testIndexOf()
	{
		try
		{
			getSecuredRDFList().indexOf(SecuredRDFNodeTest.s);
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final ListIndexException e)
		{
			if (((RDFList) getBaseRDFNode()).size() == 0)
			{
				// acceptable exception
			}
			else
			{
				throw e;
			}
		}

		try
		{
			getSecuredRDFList().indexOf(SecuredRDFNodeTest.s, 1);
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final ListIndexException e)
		{
			if (((RDFList) getBaseRDFNode()).size() <= 0)

			{
				// acceptable exception
			}
			else
			{
				throw e;
			}
		}
	}

	@Test
	public void testIsEmpty()
	{
		try
		{
			getSecuredRDFList().isEmpty();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testIterator()
	{
		try
		{
			getSecuredRDFList().iterator();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });

		try
		{
			getSecuredRDFList().iterator(perms);
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testReduce()
	{
		final ReduceFn fn = new ReduceFn() {

			@Override
			public Object reduce( final RDFNode node, final Object accumulator )
			{
				return accumulator;
			}
		};

		try
		{
			getSecuredRDFList().reduce(fn, "Hello");
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });

		try
		{
			getSecuredRDFList().reduce(perms, fn, "Hello");
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testRemove()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Delete });

		try
		{
			final int count = count(Action.Delete);
			getSecuredRDFList().remove(SecuredRDFNodeTest.s);
			if (!securityEvaluator.evaluate(Action.Update)
					|| ((count > 0) && !securityEvaluator
							.evaluate(Action.Delete)))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@SuppressWarnings("deprecation")
    @Override
	@Test
	public void testRemoveAll()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Delete });

		try
		{
			final int count = count(SecurityEvaluator.Util.asSet(new Action[] {
					Action.Delete, Action.Read }));
			getSecuredRDFList().removeAll();
			if (!securityEvaluator.evaluate(Action.Update)
					|| ((count > 0) && !securityEvaluator
							.evaluate(Action.Delete)))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final EmptyListException e)
		{
			if (count(Action.Read) == 0)
			{
				// expected.
			}
			else
			{
				throw e;
			}
		}
	}

	@Test
	public void testRemoveHead()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Delete });

		try
		{
			getSecuredRDFList().removeHead();
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final EmptyListException e)
		{
			if (count(Action.Read) == 0)
			{
				// expected.
			}
			else
			{
				throw e;
			}
		}
	}

	@Test
	public void testRemoveList()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Delete });

		try
		{
			final int count = count(Action.Delete);
			getSecuredRDFList().removeList();
			if (!securityEvaluator.evaluate(Action.Update)
					|| ((count > 0) && !securityEvaluator
							.evaluate(Action.Delete)))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testReplace()
	{
		try
		{
			getSecuredRDFList().replace(1, SecuredRDFNodeTest.s);
			if (!securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final ListIndexException e)
		{
			if (count(Action.Read) == 0)
			{
				// expected.
			}
			else
			{
				throw e;
			}
		}
	}

	@Test
	public void testSameListAs()
	{
		try
		{
			getSecuredRDFList().sameListAs(baseModel.createList());
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testSetHead()
	{

		try
		{
			getSecuredRDFList().setHead(SecuredRDFNodeTest.s);
			if (!securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		catch (final EmptyListException e)
		{
			if (count(Action.Read) == 0)
			{
				// expected.
			}
			else
			{
				throw e;
			}
		}
	}

	@Test
	public void testSetStrict()
	{
		try
		{
			getSecuredRDFList().setStrict(true);
			if (!securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testSize()
	{
		try
		{
			getSecuredRDFList().size();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testValid()
	{
		try
		{
			getSecuredRDFList().isValid();
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testWith()
	{
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });

		try
		{
			getSecuredRDFList().with(SecuredRDFNodeTest.s);
			if (!securityEvaluator.evaluate(perms))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(perms))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

}
