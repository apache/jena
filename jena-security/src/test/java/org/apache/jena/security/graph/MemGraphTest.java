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
package org.apache.jena.security.graph;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.EqualityTester;
import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluatorParameters;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.graph.SecuredBulkUpdateHandler;
import org.apache.jena.security.graph.SecuredGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( value = SecurityEvaluatorParameters.class )
public class MemGraphTest
{
	private SecuredGraph securedGraph;
	private final MockSecurityEvaluator securityEvaluator;
	private Node s;
	private Node p;
	private Node o;
	private Triple t;

	private Graph baseGraph;

	public MemGraphTest( final MockSecurityEvaluator securityEvaluator )
	{
		this.securityEvaluator = securityEvaluator;
	}

	protected Graph createGraph() throws Exception
	{
		return GraphFactory.createDefaultGraph();
	}

	@SuppressWarnings("deprecation")
    @Before
	public void setUp() throws Exception
	{
		baseGraph = createGraph();
		baseGraph.getBulkUpdateHandler().removeAll();
		securedGraph = org.apache.jena.security.Factory
				.getInstance(securityEvaluator,
						"http://example.com/securedGraph", baseGraph);
		s = NodeFactory.createURI("http://example.com/securedGraph/s");
		p = NodeFactory.createURI("http://example.com/securedGraph/p");
		o = NodeFactory.createURI("http://example.com/securedGraph/o");
		t = new Triple(s, p, o);
		baseGraph.add(t);
	}

	@Test
	public void testBulkUpdateHandler() throws Exception
	{
		final BulkUpdateHandler buh = securedGraph.getBulkUpdateHandler();
		Assert.assertNotNull("BulkUpdateHandler may not be null", buh);
		Assert.assertTrue("BulkUpdateHandler should be secured",
				buh instanceof SecuredBulkUpdateHandler);
		final BulkUpdateHandlerTest buhTest = new BulkUpdateHandlerTest(
				securityEvaluator) {
			@Override
			public void setup()
			{
				this.handler = (SecuredBulkUpdateHandler) buh;
			}
		};
		for (final Method m : buhTest.getClass().getMethods())
		{
			if (m.isAnnotationPresent(Test.class))
			{
				buhTest.setup();
				m.invoke(buhTest);
			}
		}
	}

	@Test
	public void testContainsNodes() throws Exception
	{
		try
		{
			Assert.assertTrue(securedGraph.contains(s, p, o));
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testContainsTriple() throws Exception
	{
		try
		{
			Assert.assertTrue(securedGraph.contains(t));
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testDelete() throws Exception
	{
		final Set<Action> UD = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Delete });
		try
		{
			securedGraph.delete(t);

			if (!securityEvaluator.evaluate(UD))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
			Assert.assertEquals(0, baseGraph.size());

		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(UD))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testDependsOn() throws Exception
	{
		try
		{
			Assert.assertFalse(securedGraph.dependsOn(GraphFactory
					.createDefaultGraph()));
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			Assert.assertTrue(securedGraph.dependsOn(baseGraph));
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testFindNodes() throws Exception
	{
		try
		{

			Assert.assertFalse(securedGraph.find(Node.ANY, Node.ANY, Node.ANY)
					.toList().isEmpty());
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testFindTriple() throws Exception
	{
		try
		{
			Assert.assertFalse(securedGraph.find(t).toList().isEmpty());
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetPrefixMapping() throws Exception
	{
		SecuredPrefixMappingTest.runTests(securityEvaluator,
				securedGraph.getPrefixMapping());
	}

	@Test
	public void testInequality()
	{
		EqualityTester
				.testInequality("proxy and base", securedGraph, baseGraph);
		final Graph g2 = org.apache.jena.security.graph.impl.Factory
				.getInstance(securityEvaluator,
						"http://example.com/securedGraph", baseGraph);
		EqualityTester.testEquality("proxy and proxy2", securedGraph, g2);
		EqualityTester.testInequality("base and proxy2", baseGraph, g2);
	}

	@Test
	public void testIsIsomorphicWith() throws Exception
	{
		try
		{
			Assert.assertFalse(securedGraph.isIsomorphicWith(GraphFactory
					.createDefaultGraph()));
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			Assert.assertTrue(securedGraph.isIsomorphicWith(baseGraph));
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testSize() throws Exception
	{
		try
		{
			Assert.assertEquals(1, securedGraph.size());
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testTripleMatch() throws Exception
	{
		try
		{
			Assert.assertFalse(securedGraph.find(new TripleMatch() {

				@Override
				public Triple asTriple()
				{
					return Triple.ANY;
				}

				@Override
				public Node getMatchObject()
				{
					return Node.ANY;
				}

				@Override
				public Node getMatchPredicate()
				{
					return Node.ANY;
				}

				@Override
				public Node getMatchSubject()
				{
					return Node.ANY;
				}
			}).toList().isEmpty());
			if (!securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

}
