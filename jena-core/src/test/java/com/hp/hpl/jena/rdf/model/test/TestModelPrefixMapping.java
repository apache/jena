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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.AbstractTestPrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping;

import org.junit.Assert;

/**
 * Test that a model is a prefix mapping.
 * 
 */
public class TestModelPrefixMapping extends AbstractTestPrefixMapping
{
	private final TestingModelFactory modelFactory;
	protected static final String alphaPrefix = "alpha";

	protected static final String betaPrefix = "beta";
	protected static final String alphaURI = "http://testing.jena.hpl.hp.com/alpha#";
	protected static final String betaURI = "http://testing.jena.hpl.hp.com/beta#";
	protected PrefixMapping baseMap = PrefixMapping.Factory
			.create()
			.setNsPrefix(TestModelPrefixMapping.alphaPrefix,
					TestModelPrefixMapping.alphaURI)
			.setNsPrefix(TestModelPrefixMapping.betaPrefix,
					TestModelPrefixMapping.betaURI);

	private PrefixMapping prevMap;

	public TestModelPrefixMapping( final TestingModelFactory modelFactory,
			final String name )
	{
		super(name);
		this.modelFactory = modelFactory;
	}

	/**
	 * Test that existing prefixes are not over-ridden by the default ones.
	 */
	private void doOnlyFreshPrefixes()
	{
		final String newURI = "abc:def/";
		final Graph g = Factory.createDefaultGraph();
		final PrefixMapping pm = g.getPrefixMapping();
		pm.setNsPrefix(TestModelPrefixMapping.alphaPrefix, newURI);
		final Model m = ModelFactory.createModelForGraph(g);
		Assert.assertEquals(newURI,
				m.getNsPrefixURI(TestModelPrefixMapping.alphaPrefix));
		Assert.assertEquals(TestModelPrefixMapping.betaURI,
				m.getNsPrefixURI(TestModelPrefixMapping.betaPrefix));
	}

	@Override
	protected PrefixMapping getMapping()
	{
		return modelFactory.getPrefixMapping();
	}

	public void restorePrefixes()
	{
		ModelCom.setDefaultModelPrefixes(prevMap);
	}

	public void setPrefixes()
	{
		prevMap = ModelCom.setDefaultModelPrefixes(baseMap);
	}

	/**
	 * Test that a freshly-created Model has the prefixes established by the
	 * default in ModelCom.
	 */
	public void testDefaultPrefixes()
	{
		setPrefixes();
		final Model m = ModelFactory.createDefaultModel();
		Assert.assertEquals(baseMap.getNsPrefixMap(), m.getNsPrefixMap());
		restorePrefixes();
	}

	public void testGetDefault()
	{
		setPrefixes();
		try
		{
			Assert.assertSame(baseMap, ModelCom.getDefaultModelPrefixes());
		}
		finally
		{
			restorePrefixes();
		}
	}

	public void testOnlyFreshPrefixes()
	{
		setPrefixes();
		try
		{
			doOnlyFreshPrefixes();
		}
		finally
		{
			restorePrefixes();
		}
	}
}
