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

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

public abstract class AbstractContainerMethods extends AbstractModelTestBase
{

	protected Resource resource;

	public AbstractContainerMethods( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	protected abstract Container createContainer();

	protected abstract Resource getContainerType();

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		resource = model.createResource();
	}

	public void testContainerOfIntegers()
	{
		final int num = 10;
		final Container c = createContainer();
		for (int i = 0; i < num; i += 1)
		{
			c.add(i);
		}
		Assert.assertEquals(num, c.size());
		final NodeIterator it = c.iterator();
		for (int i = 0; i < num; i += 1)
		{
			Assert.assertEquals(i, ((Literal) it.nextNode()).getInt());
		}
		Assert.assertFalse(it.hasNext());
	}

	public void testContainerOfIntegersRemovingA()
	{
		final boolean[] retain = { true, true, true, false, false, false,
				false, false, true, true };
		testContainerOfIntegersWithRemoving(retain);
	}

	public void testContainerOfIntegersRemovingB()
	{
		final boolean[] retain = { false, true, true, false, false, false,
				false, false, true, false };
		testContainerOfIntegersWithRemoving(retain);
	}

	public void testContainerOfIntegersRemovingC()
	{
		final boolean[] retain = { false, false, false, false, false, false,
				false, false, false, false };
		testContainerOfIntegersWithRemoving(retain);
	}

	protected void testContainerOfIntegersWithRemoving( final boolean[] retain )
	{
		final int num = retain.length;
		final boolean[] found = new boolean[num];
		final Container c = createContainer();
		for (int i = 0; i < num; i += 1)
		{
			c.add(i);
		}
		final NodeIterator it = c.iterator();
        for ( boolean aRetain : retain )
        {
            it.nextNode();
            if ( aRetain == false )
            {
                it.remove();
            }
        }
		final NodeIterator s = c.iterator();
		while (s.hasNext())
		{
			final int v = ((Literal) s.nextNode()).getInt();
			Assert.assertFalse(found[v]);
			found[v] = true;
		}
		for (int i = 0; i < num; i += 1)
		{
			Assert.assertEquals("element " + i, retain[i], found[i]);
		}
	}

	public void testEmptyContainer()
	{
		final Container c = createContainer();
		Assert.assertTrue(model.contains(c, RDF.type, getContainerType()));
		Assert.assertEquals(0, c.size());
		Assert.assertFalse(c.contains(AbstractModelTestBase.tvBoolean));
		Assert.assertFalse(c.contains(AbstractModelTestBase.tvByte));
		Assert.assertFalse(c.contains(AbstractModelTestBase.tvShort));
		Assert.assertFalse(c.contains(AbstractModelTestBase.tvInt));
		Assert.assertFalse(c.contains(AbstractModelTestBase.tvLong));
		Assert.assertFalse(c.contains(AbstractModelTestBase.tvChar));
		Assert.assertFalse(c.contains(AbstractModelTestBase.tvFloat));
		Assert.assertFalse(c.contains(AbstractModelTestBase.tvString));
	}

	public void testFillingContainer()
	{
		final Container c = createContainer();
		final String lang = "fr";
		final Literal tvLiteral = model.createLiteral("test 12 string 2");
		// Resource tvResObj = model.createResource( new ResTestObjF() );
		c.add(AbstractModelTestBase.tvBoolean);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvBoolean));
		c.add(AbstractModelTestBase.tvByte);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvByte));
		c.add(AbstractModelTestBase.tvShort);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvShort));
		c.add(AbstractModelTestBase.tvInt);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvInt));
		c.add(AbstractModelTestBase.tvLong);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvLong));
		c.add(AbstractModelTestBase.tvChar);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvChar));
		c.add(AbstractModelTestBase.tvFloat);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvFloat));
		c.add(AbstractModelTestBase.tvString);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvString));
		c.add(AbstractModelTestBase.tvString, lang);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvString, lang));
		c.add(tvLiteral);
		Assert.assertTrue(c.contains(tvLiteral));
		// c.add( tvResObj ); assertTrue( c.contains( tvResObj ) );
		c.add(AbstractModelTestBase.tvLitObj);
		Assert.assertTrue(c.contains(AbstractModelTestBase.tvLitObj));
		Assert.assertEquals(11, c.size());
	}
}
