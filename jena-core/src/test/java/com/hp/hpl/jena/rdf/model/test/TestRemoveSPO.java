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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.test.JenaTestBase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

public class TestRemoveSPO extends AbstractModelTestBase
{

	public TestRemoveSPO( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public Model createModel( final Graph base )
	{
		return modelFactory.createModel(base);
	}

	public void testRemoveSPOCallsGraphDeleteTriple()
	{
		final List<Triple> deleted = new ArrayList<>();
		final Graph base = new WrappedGraph(model.getGraph()) {
			@Override
			public void delete( final Triple t )
			{
				deleted.add(t);
			}
		};
		model = createModel(base);
		model.remove(ModelHelper.resource("R"), ModelHelper.property("P"),
				ModelHelper.rdfNode(model, "17"));
		Assert.assertEquals(
				JenaTestBase.listOfOne(NodeCreateUtils.createTriple("R P 17")),
				deleted);
	}

	public void testRemoveSPOReturnsModel()
	{
		Assert.assertSame(
				model,
				model.remove(ModelHelper.resource("R"),
						ModelHelper.property("P"),
						ModelHelper.rdfNode(model, "17")));
	}
}
