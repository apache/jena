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

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import org.junit.Assert;

public class TestContains extends AbstractModelTestBase
{
	public TestContains( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	private Property prop( final String uri )
	{
		return ResourceFactory.createProperty("eh:/" + uri);
	}

	private Resource res( final String uri )
	{
		return ResourceFactory.createResource("eh:/" + uri);
	}

	public void testContains()
	{
		testContains(false, "", "x");
		testContains(false, "a R b", "x");
		testContains(false, "a R b; c P d", "x");
		/* */
		testContains(false, "a R b", "z");
		/* */
		testContains(true, "x R y", "x");
		testContains(true, "a P b", "P");
		testContains(true, "i  Q  j", "j");
		testContains(true, "x R y; a P b; i Q j", "y");
		/* */
		testContains(true, "x R y; a P b; i Q j", "y");
		testContains(true, "x R y; a P b; i Q j", "R");
		testContains(true, "x R y; a P b; i Q j", "a");
	}

	public void testContains( final boolean yes, final String facts,
			final String resource )
	{
		final Model m = ModelHelper.modelWithStatements(this, facts);
		final RDFNode r = ModelHelper.rdfNode(m, resource);
		if (ModelHelper.modelWithStatements(this, facts).containsResource(r) != yes)
		{
			Assert.fail("[" + facts + "] should" + (yes ? "" : " not")
					+ " contain " + resource);
		}
	}

	public void testContainsWithNull()
	{
		testCWN(false, "", null, null, null);
		testCWN(true, "x R y", null, null, null);
		testCWN(false, "x R y", null, null, res("z"));
		testCWN(true, "x RR y", res("x"), prop("RR"), null);
		testCWN(true, "a BB c", null, prop("BB"), res("c"));
		testCWN(false, "a BB c", null, prop("ZZ"), res("c"));
	}

	public void testCWN( final boolean yes, final String facts,
			final Resource S, final Property P, final RDFNode O )
	{
		Assert.assertEquals(yes, ModelHelper.modelWithStatements(this, facts)
				.contains(S, P, O));
	}

	public void testModelComContainsSPcallsContainsSPO()
	{
		final Graph g = Factory.createDefaultGraph();
		final boolean[] wasCalled = { false };
		// FIXME change to dynamic proxy
		final Model m = new ModelCom(g) {
			@Override
			public boolean contains( final Resource s, final Property p,
					final RDFNode o )
			{
				wasCalled[0] = true;
				return super.contains(s, p, o);
			}
		};
		Assert.assertFalse(m.contains(ModelHelper.resource("r"),
				ModelHelper.property("p")));
		Assert.assertTrue("contains(S,P) should call contains(S,P,O)",
				wasCalled[0]);
	}
}
