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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import org.junit.Assert;

/**
 * A revamped version of the regression set-operation tests.
 */
public class TestModelSetOperations extends AbstractModelTestBase
{
	private Model model2;

	public TestModelSetOperations( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		model2 = createModel();
	}

	@Override
	public void tearDown() throws Exception
	{
		super.tearDown();
		model2.close();
	}

	public void testDifference()
	{
		ModelHelper.modelAdd(model, "a P b; w R x");
		ModelHelper.modelAdd(model2, "w R x; y S z");
		final Model dm = model.difference(model2);
		for (final StmtIterator it = dm.listStatements(); it.hasNext();)
		{
			final Statement s = it.nextStatement();
			Assert.assertTrue(model.contains(s) && !model2.contains(s));
		}
		for (final StmtIterator it = model.union(model2).listStatements(); it
				.hasNext();)
		{
			final Statement s = it.nextStatement();
			Assert.assertEquals(model.contains(s) && !model2.contains(s),
					dm.contains(s));
		}
		Assert.assertTrue(dm.containsAny(model));
		Assert.assertTrue(dm.containsAny(model.listStatements()));
		Assert.assertFalse(dm.containsAny(model2));
		Assert.assertFalse(dm.containsAny(model2.listStatements()));
		Assert.assertTrue(model.containsAll(dm));

	}

	public void testIntersection()
	{

		ModelHelper.modelAdd(model, "a P b; w R x");
		ModelHelper.modelAdd(model2, "w R x; y S z");
		final Model im = model.intersection(model2);
		Assert.assertFalse(model.containsAll(model2));
		Assert.assertFalse(model2.containsAll(model));
		Assert.assertTrue(model.containsAll(im));
		Assert.assertTrue(model2.containsAll(im));
		for (final StmtIterator it = im.listStatements(); it.hasNext();)
		{
			final Statement s = it.nextStatement();
			Assert.assertTrue(model.contains(s) && model2.contains(s));
		}
		for (final StmtIterator it = im.listStatements(); it.hasNext();)
		{
			Assert.assertTrue(model.contains(it.nextStatement()));
		}
		for (final StmtIterator it = im.listStatements(); it.hasNext();)
		{
			Assert.assertTrue(model2.contains(it.nextStatement()));
		}
		Assert.assertTrue(model.containsAll(im.listStatements()));
		Assert.assertTrue(model2.containsAll(im.listStatements()));

	}

	public void testUnion()
	{

		ModelHelper.modelAdd(model, "a P b; w R x");
		ModelHelper.modelAdd(model2, "w R x; y S z");
		final Model um = model.union(model2);
		Assert.assertFalse(model.containsAll(model2));
		Assert.assertFalse(model2.containsAll(model));
		Assert.assertTrue(um.containsAll(model));
		Assert.assertTrue(um.containsAll(model2));
		for (final StmtIterator it = um.listStatements(); it.hasNext();)
		{
			final Statement s = it.nextStatement();
			Assert.assertTrue(model.contains(s) || model2.contains(s));
		}
		for (final StmtIterator it = model.listStatements(); it.hasNext();)
		{
			Assert.assertTrue(um.contains(it.nextStatement()));
		}
		for (final StmtIterator it = model2.listStatements(); it.hasNext();)
		{
			Assert.assertTrue(um.contains(it.nextStatement()));
		}
		Assert.assertTrue(um.containsAll(model.listStatements()));
		Assert.assertTrue(um.containsAll(model2.listStatements()));

	}
}
