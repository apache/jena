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
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import org.junit.Assert;

public class TestLiteralsInModel extends AbstractModelTestBase
{
	private Resource X;
	private Property P;

	public TestLiteralsInModel( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		X = ModelHelper.resource("X");
		P = ModelHelper.property("P");
	}

	public void testAddWithBooleanObject()
	{
		model.addLiteral(X, P, true);
		Assert.assertTrue(model.contains(X, P, model.createTypedLiteral(true)));
		Assert.assertTrue(model.containsLiteral(X, P, true));
	}

	public void testAddWithCharObject()
	{
		model.addLiteral(X, P, 'x');
		Assert.assertTrue(model.contains(X, P, model.createTypedLiteral('x')));
		Assert.assertTrue(model.containsLiteral(X, P, 'x'));
	}

	public void testAddWithDoubleObject()
	{
		model.addLiteral(X, P, 14.0d);
		Assert.assertTrue(model.contains(X, P, model.createTypedLiteral(14.0d)));
		Assert.assertTrue(model.containsLiteral(X, P, 14.0d));
	}

	public void testAddWithFloatObject()
	{
		model.addLiteral(X, P, 14.0f);
		Assert.assertTrue(model.contains(X, P, model.createTypedLiteral(14.0f)));
		Assert.assertTrue(model.containsLiteral(X, P, 14.0f));
	}

	public void testAddWithIntObject()
	{
		model.addLiteral(X, P, 99);
		Assert.assertTrue(model.contains(X, P, model.createTypedLiteral(99)));
		Assert.assertTrue(model.containsLiteral(X, P, 99));
	}

	public void testAddWithLiteralObject()
	{
		final Literal lit = model.createLiteral("spoo");
		model.addLiteral(X, P, lit);
		Assert.assertTrue(model.contains(X, P, lit));
		Assert.assertTrue(model.containsLiteral(X, P, lit));
	}

	public void testAddWithLongObject()
	{
		model.addLiteral(X, P, 99L);
		Assert.assertTrue(model.contains(X, P, model.createTypedLiteral(99L)));
		Assert.assertTrue(model.containsLiteral(X, P, 99L));
	}

	// that version of addLiteral is deprecated; test removed.
	// public void testAddWithAnObject()
	// {
	// Object z = new Date();
	// model.addLiteral( X, P, z );
	// assertTrue( model.contains( X, P, model.createTypedLiteral( z ) ) );
	// assertTrue( model.containsLiteral( X, P, z ) );
	// }
}
