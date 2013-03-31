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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

import junit.framework.TestSuite;

/**
 * Implementation of the basic Model TestPackage. Uses the standard
 * ModelFactory to create models for testing.
 * 
 */

public class TestPackage extends AbstractTestPackage
{

	public static class PlainModelFactory implements TestingModelFactory
	{
		@Override
		public Model createModel()
		{
			return ModelFactory.createDefaultModel();
		}

		@Override
		public Model createModel( final Graph base )
		{
			return ModelFactory.createModelForGraph(base);
		}

		@Override
		public PrefixMapping getPrefixMapping()
		{
			return ModelFactory.createDefaultModel().getGraph()
					.getPrefixMapping();
		}
	}

	static public TestSuite suite()
	{
		return new TestPackage();
	}

	public TestPackage()
	{
		super("Model", new PlainModelFactory());
	}
}
