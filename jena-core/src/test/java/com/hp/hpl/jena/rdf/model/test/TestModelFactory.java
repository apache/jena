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

import com.hp.hpl.jena.graph.compose.Union;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.test.JenaTestBase;

import org.junit.Assert;
import junit.framework.TestCase;

/**
 * Tests the ModelFactory code. Very skeletal at the moment. It's really
 * testing that the methods actually exists, but it doesn't check much in
 * the way of behaviour.
 * 
 */

public class TestModelFactory extends TestCase
{

	public TestModelFactory( final String name )
	{
		super(name);
	}

	public void testAssembleModelFromModel()
	{
		// TODO Model ModelFactory.assembleModelFrom( Model singleRoot )
	}

	public void testAssmbleModelFromRoot()
	{
		// TODO Model assembleModelFrom( Resource root )
	}

	/**
	 * Test that ModelFactory.createDefaultModel() exists. [Should check that
	 * the Model
	 * is truly a "default" model.]
	 */
	public void testCreateDefaultModel()
	{
		ModelFactory.createDefaultModel().close();
	}

	public void testCreateInfModel()
	{
		final String rule = "-> (eg:r eg:p eg:v).";
		final Reasoner r = new GenericRuleReasoner(Rule.parseRules(rule));
		final InfGraph ig = r
				.bind(ModelFactory.createDefaultModel().getGraph());
		final InfModel im = ModelFactory.createInfModel(ig);
		JenaTestBase.assertInstanceOf(InfModel.class, im);
		Assert.assertEquals(1, im.size());
	}

	/**
	 * test that a union model is a model over the union of the two underlying
	 * graphs. (We don't check that Union works - that's done in the Union
	 * tests, we hope.)
	 */
	public void testCreateUnion()
	{
		final Model m1 = ModelFactory.createDefaultModel();
		final Model m2 = ModelFactory.createDefaultModel();
		final Model m = ModelFactory.createUnion(m1, m2);
		JenaTestBase.assertInstanceOf(Union.class, m.getGraph());
		Assert.assertSame(m1.getGraph(), ((Union) m.getGraph()).getL());
		Assert.assertSame(m2.getGraph(), ((Union) m.getGraph()).getR());
	}

	public void testFindAssemblerRoots()
	{
		// TODO Set ModelFactory.findAssemblerRoots( Model model )
	}

	public void testGetDefaultPrefixMapping()
	{
		Assert.assertSame(ModelCom.getDefaultModelPrefixes(),
				ModelFactory.getDefaultModelPrefixes());
	}

	public void testSetDefaultPrefixMapping()
	{
		final PrefixMapping original = ModelCom.getDefaultModelPrefixes();
		final PrefixMapping pm = PrefixMapping.Factory.create();
		ModelFactory.setDefaultModelPrefixes(pm);
		Assert.assertSame(pm, ModelCom.getDefaultModelPrefixes());
		Assert.assertSame(pm, ModelFactory.getDefaultModelPrefixes());
		ModelCom.setDefaultModelPrefixes(original);
	}
}
