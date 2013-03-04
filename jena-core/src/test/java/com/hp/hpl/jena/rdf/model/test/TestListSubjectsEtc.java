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
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.test.JenaTestBase;

import org.junit.Assert;

/**
 * TestListSubjectsEtc - tests for listSubjects, listObjects [and
 * listPredicates, if
 * it were to exist]
 * TODO make preperly generic, add missing test cases [we're relying, at root,
 * on SimpleQueryHandler]
 */
public class TestListSubjectsEtc extends AbstractModelTestBase
{
	public TestListSubjectsEtc( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void testListObjectsNoRemove()
	{
		final Model m = ModelHelper.modelWithStatements(this,
				"a P b; b Q c; c R a");
		final NodeIterator it = m.listObjects();
		it.next();
		try
		{
			it.remove();
			Assert.fail("listObjects should not support .remove()");
		}
		catch (final UnsupportedOperationException e)
		{
			JenaTestBase.pass();
		}
	}

	public void testListSubjectsNoRemove()
	{
		final Model m = ModelHelper.modelWithStatements(this,
				"a P b; b Q c; c R a");
		final ResIterator it = m.listSubjects();
		it.next();
		try
		{
			it.remove();
			Assert.fail("listSubjects should not support .remove()");
		}
		catch (final UnsupportedOperationException e)
		{
			JenaTestBase.pass();
		}
	}

	public void testListSubjectsWorksAfterRemoveProperties()
	{
		final Model m = ModelHelper.modelWithStatements(this,
				"p1 before terminal; p2 before terminal");
		m.createResource("eh:/p1").removeProperties();
		ModelHelper.assertIsoModels(
				ModelHelper.modelWithStatements(this, "p2 before terminal"), m);
		Assert.assertEquals(ModelHelper.resourceSet("p2"), m.listSubjects()
				.toSet());
	}

	public void testListSubjectsWorksAfterRemovePropertiesWIthLots()
	{
		final Model m = ModelHelper.modelWithStatements(this,
				"p2 before terminal");
		for (int i = 0; i < 100; i += 1)
		{
			ModelHelper.modelAdd(m, "p1 hasValue " + i);
		}
		m.createResource("eh:/p1").removeProperties();
		ModelHelper.assertIsoModels(
				ModelHelper.modelWithStatements(this, "p2 before terminal"), m);
		Assert.assertEquals(ModelHelper.resourceSet("p2"), m.listSubjects()
				.toSet());
	}
}
