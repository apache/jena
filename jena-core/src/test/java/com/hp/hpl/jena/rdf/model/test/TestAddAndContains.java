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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

public class TestAddAndContains extends AbstractModelTestBase
{

	protected Resource S;

	protected Property P;

	public TestAddAndContains( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		S = model.createResource("http://nowhere.man/subject");
		P = model.createProperty("http://nowhere.man/predicate");
	}

	@Override
	public void tearDown() throws Exception
	{
		S = null;
		P = null;
		super.tearDown();
	}

	public void testAddContainLiteralByStatement()
	{
		final Literal L = model.createTypedLiteral(210);
		final Statement s = model.createStatement(S, RDF.value, L);
		Assert.assertTrue(model.add(s).contains(s));
		Assert.assertTrue(model.contains(S, RDF.value));
	}

	public void testAddContainsBoolean()
	{
		model.addLiteral(S, P, AbstractModelTestBase.tvBoolean);
		Assert.assertTrue(model.containsLiteral(S, P,
				AbstractModelTestBase.tvBoolean));
	}

	public void testAddContainsByte()
	{
		model.addLiteral(S, P, AbstractModelTestBase.tvByte);
		Assert.assertTrue(model.containsLiteral(S, P,
				AbstractModelTestBase.tvByte));
	}

	public void testAddContainsChar()
	{
		model.addLiteral(S, P, AbstractModelTestBase.tvChar);
		Assert.assertTrue(model.containsLiteral(S, P,
				AbstractModelTestBase.tvChar));
	}

	public void testAddContainsDouble()
	{
		model.addLiteral(S, P, AbstractModelTestBase.tvDouble);
		Assert.assertTrue(model.containsLiteral(S, P,
				AbstractModelTestBase.tvDouble));
	}

	public void testAddContainsFloat()
	{
		model.addLiteral(S, P, AbstractModelTestBase.tvFloat);
		Assert.assertTrue(model.containsLiteral(S, P,
				AbstractModelTestBase.tvFloat));
	}

	public void testAddContainsInt()
	{
		model.addLiteral(S, P, AbstractModelTestBase.tvInt);
		Assert.assertTrue(model.containsLiteral(S, P,
				AbstractModelTestBase.tvInt));
	}

	public void testAddContainsLanguagedString()
	{
		model.add(S, P, "test string", "en");
		Assert.assertFalse(model.contains(S, P, "test string"));
		Assert.assertTrue(model.contains(S, P, "test string", "en"));
	}

	public void testAddContainsLong()
	{
		model.addLiteral(S, P, AbstractModelTestBase.tvLong);
		Assert.assertTrue(model.containsLiteral(S, P,
				AbstractModelTestBase.tvLong));
	}

	public void testAddContainsPlainString()
	{
		model.add(S, P, "test string");
		Assert.assertTrue(model.contains(S, P, "test string"));
		Assert.assertFalse(model.contains(S, P, "test string", "en"));
	}

	// public void testAddContainsObject()
	// {
	// LitTestObj O = new LitTestObj( 12345 );
	// model.addLiteral( S, P, O );
	// assertTrue( model.containsLiteral( S, P, O ) );
	// }

	public void testAddContainsResource()
	{
		final Resource r = model.createResource();
		model.add(S, P, r);
		Assert.assertTrue(model.contains(S, P, r));
	}

	public void testAddContainsShort()
	{
		model.addLiteral(S, P, AbstractModelTestBase.tvShort);
		Assert.assertTrue(model.containsLiteral(S, P,
				AbstractModelTestBase.tvShort));
	}

	public void testAddDuplicateLeavesSizeSame()
	{
		final Statement s = model.createStatement(S, RDF.value, "something");
		model.add(s);
		final long size = model.size();
		model.add(s);
		Assert.assertEquals(size, model.size());
	}

	public void testEmpty()
	{
		Assert.assertFalse(model.containsLiteral(S, P,
				AbstractModelTestBase.tvBoolean));
		Assert.assertFalse(model.contains(S, P, model.createResource()));
		Assert.assertFalse(model.containsLiteral(S, P,
				AbstractModelTestBase.tvByte));
		Assert.assertFalse(model.containsLiteral(S, P,
				AbstractModelTestBase.tvShort));
		Assert.assertFalse(model.containsLiteral(S, P,
				AbstractModelTestBase.tvInt));
		Assert.assertFalse(model.containsLiteral(S, P,
				AbstractModelTestBase.tvLong));
		Assert.assertFalse(model.containsLiteral(S, P,
				AbstractModelTestBase.tvChar));
		Assert.assertFalse(model.containsLiteral(S, P,
				AbstractModelTestBase.tvFloat));
		Assert.assertFalse(model.containsLiteral(S, P,
				AbstractModelTestBase.tvDouble));
		Assert.assertFalse(model.containsLiteral(S, P, new LitTestObj(12345)));
		Assert.assertFalse(model.contains(S, P, "test string"));
		Assert.assertFalse(model.contains(S, P, "test string", "en"));
	}

}
