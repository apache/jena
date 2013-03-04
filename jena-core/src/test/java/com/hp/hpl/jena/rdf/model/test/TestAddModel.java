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
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

public class TestAddModel extends AbstractModelTestBase
{
	private Model model2;

	public TestAddModel( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	protected void assertContainsAll( final Model model, final Model model2 )
	{
		for (final StmtIterator s = model2.listStatements(); s.hasNext();)
		{
			Assert.assertTrue(model.contains(s.nextStatement()));
		}
	}

	protected void assertSameStatements( final Model model, final Model model2 )
	{
		assertContainsAll(model, model2);
		assertContainsAll(model2, model);
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

	public void testAddByIterator()
	{

		ModelHelper.modelAdd(model, "a P b; c P d; x Q 1; y Q 2");
		model2.add(model.listStatements());
		Assert.assertEquals(model.size(), model2.size());
		assertSameStatements(model, model2);
		model.add(model.createResource(), RDF.value, model.createResource());
		model.add(model.createResource(), RDF.value, model.createResource());
		model.add(model.createResource(), RDF.value, model.createResource());
		final StmtIterator s = model.listStatements();
		model2.remove(s.nextStatement()).remove(s);
		Assert.assertEquals(0, model2.size());
	}

	public void testAddByModel()
	{

		ModelHelper.modelAdd(model, "a P b; c P d; x Q 1; y Q 2");
		model2.add(model);
		Assert.assertEquals(model.size(), model2.size());
		assertSameStatements(model, model2);
	}

	public void testRemoveByModel()
	{

		ModelHelper.modelAdd(model, "a P b; c P d; x Q 1; y Q 2");
		model2.add(model).remove(model);
		Assert.assertEquals(0, model2.size());
		Assert.assertFalse(model2.listStatements().hasNext());
	}
}
