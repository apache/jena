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
package org.apache.jena.security.query;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

import org.apache.jena.security.Factory;
import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator.SecNode.Type;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.query.SecuredQueryEngineFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryEngineTest
{

	@BeforeClass
	public static void setupFactory()
	{
		SecuredQueryEngineFactory.register();
	}

	@AfterClass
	public static void teardownFactory()
	{
		SecuredQueryEngineFactory.unregister();
	}

	Model baseModel;

	public QueryEngineTest()
	{

	}

	@Before
	public void setUp()
	{
		baseModel = ModelFactory.createDefaultModel();
		Resource r = ResourceFactory
				.createResource("http://example.com/resource/1");
		final Resource o = ResourceFactory
				.createResource("http://example.com/class");
		baseModel.add(r, RDF.type, o);
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_1"),
				ResourceFactory.createTypedLiteral(1));
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_2"),
				ResourceFactory.createTypedLiteral("foo"));
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_3"),
				ResourceFactory.createTypedLiteral(3.14));
		r = ResourceFactory.createResource("http://example.com/resource/2");
		baseModel.add(r, RDF.type, o);
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_1"),
				ResourceFactory.createTypedLiteral(2));
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_2"),
				ResourceFactory.createTypedLiteral("bar"));
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_3"),
				ResourceFactory.createTypedLiteral(6.28));

		r = ResourceFactory.createResource("http://example.com/resource/3");
		baseModel.add(r, RDF.type, ResourceFactory
				.createResource("http://example.com/anotherClass"));
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_1"),
				ResourceFactory.createTypedLiteral(3));
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_2"),
				ResourceFactory.createTypedLiteral("baz"));
		baseModel.add(r, ResourceFactory
				.createProperty("http://example.com/property/_3"),
				ResourceFactory.createTypedLiteral(9.42));
	}

	@After
	public void tearDown()
	{
		baseModel.close();
	}

	@Test
	public void testOpenQueryType()
	{
		final SecurityEvaluator eval = new MockSecurityEvaluator(true, true,
				true, true, true, true);
		final SecuredModel model = Factory.getInstance(eval,
				"http://example.com/securedModel", baseModel);
		try
		{
			final String query = "prefix fn: <http://www.w3.org/2005/xpath-functions#>  "
					+ " SELECT ?foo ?bar WHERE "
					+ " { ?foo a <http://example.com/class> ; "
					+ "?bar [] ."
					+ "  } ";
			final QueryExecution qexec = QueryExecutionFactory.create(query,
					model);
			try
			{
				final ResultSet results = qexec.execSelect();
				int count = 0;
				for (; results.hasNext();)
				{
					count++;
					final QuerySolution soln = results.nextSolution();
				}
				Assert.assertEquals(8, count);
			}
			finally
			{
				qexec.close();
			}
		}
		finally
		{
			model.close();
		}
	}

	@Test
	public void testRestrictedQueryType()
	{
		final SecurityEvaluator eval = new MockSecurityEvaluator(true, true,
				true, true, true, true) {

			@Override
			public boolean evaluate( final Action action,
					final SecNode graphIRI, final SecTriple triple )
			{
				if (triple.getSubject().equals(
						new SecNode(Type.URI, "http://example.com/resource/1")))
				{
					return false;
				}
				return super.evaluate(action, graphIRI, triple);
			}
		};
		final SecuredModel model = Factory.getInstance(eval,
				"http://example.com/securedModel", baseModel);
		try
		{
			final String query = "prefix fn: <http://www.w3.org/2005/xpath-functions#>  "
					+ " SELECT ?foo ?bar WHERE "
					+ " { ?foo a <http://example.com/class> ; "
					+ "?bar [] ."
					+ "  } ";
			final QueryExecution qexec = QueryExecutionFactory.create(query,
					model);
			try
			{
				final ResultSet results = qexec.execSelect();
				int count = 0;
				for (; results.hasNext();)
				{
					count++;
					results.nextSolution();
				}
				Assert.assertEquals(4, count);
			}
			finally
			{
				qexec.close();
			}
		}
		finally
		{
			model.close();
		}
	}

}
