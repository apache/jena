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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.junit.Assert;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.Factory;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluatorParameters;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.graph.SecuredGraph;
import org.apache.jena.security.graph.SecuredPrefixMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( value = SecurityEvaluatorParameters.class )
public class SecuredPrefixMappingTest
{
	public static void runTests( final SecurityEvaluator securityEvaluator,
			final PrefixMapping prefixMapping ) throws Exception
	{
		final PrefixMapping pm = prefixMapping;
		Assert.assertNotNull("PrefixMapping may not be null", pm);
		Assert.assertTrue("PrefixMapping should be secured",
				pm instanceof SecuredPrefixMapping);
		final SecuredPrefixMappingTest pmTest = new SecuredPrefixMappingTest(
				securityEvaluator) {
			@Override
			public void setup()
			{
				this.securedMapping = (SecuredPrefixMapping) pm;
			}
		};
		Method lockTest = null;
		for (final Method m : pmTest.getClass().getMethods())
		{
			if (m.isAnnotationPresent(Test.class))
			{
				// lock test must come last
				if (!m.getName().equals("lockTest"))
				{
					lockTest = m;
				}
				else
				{
					pmTest.setup();
					m.invoke(pmTest);
				}

			}
		}
		pmTest.setup();
		lockTest.invoke(pmTest);

	}

	private final SecurityEvaluator securityEvaluator;

	protected SecuredPrefixMapping securedMapping;

	public SecuredPrefixMappingTest( final SecurityEvaluator securityEvaluator )
	{
		this.securityEvaluator = securityEvaluator;
	}

	@Before
	public void setup()
	{
		final Graph g = GraphFactory.createDefaultGraph();

		final SecuredGraph sg = Factory.getInstance(securityEvaluator,
				"http://example.com/testGraph", g);
		this.securedMapping = sg.getPrefixMapping();
	}

	@Test
	public void testExpandPrefix()
	{
		try
		{
			securedMapping.expandPrefix("foo");
			if (!securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetNsPrefixMap()
	{
		try
		{
			securedMapping.getNsPrefixMap();
			if (!securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetNsPrefixURI()
	{

		try
		{
			securedMapping.getNsPrefixURI("foo");
			if (!securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testGetNsURIPrefix()
	{

		try
		{
			securedMapping.getNsURIPrefix("http://example.com/foo");
			if (!securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testLock()
	{
		try
		{
			securedMapping.lock();
			if (!securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testQnameFor()
	{
		try
		{
			securedMapping.qnameFor("http://example.com/foo/bar");
			if (!securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testRemoveNsPrefix()
	{
		try
		{
			securedMapping.removeNsPrefix("foo");
			if (!securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testSamePrefixMappingAs()
	{
		try
		{
			securedMapping.samePrefixMappingAs(GraphFactory
					.createDefaultGraph().getPrefixMapping());
			if (!securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testSetNsPrefix()
	{
		try
		{
			securedMapping.setNsPrefix("foo", "http://example.com/foo");
			if (!securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedMapping.setNsPrefixes(GraphFactory.createDefaultGraph()
					.getPrefixMapping());
			if (!securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedMapping.setNsPrefixes(new HashMap<String, String>());
			if (!securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testShortForm()
	{
		try
		{
			securedMapping.shortForm("http://example.com/foo/bar");
			if (!securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Read,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testWithDefaultMappings()
	{
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefix( "example", "http://example.com");
		try
		{
			// make sure that it must update
			securedMapping.withDefaultMappings(pm);
			if (!securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}
	
	@Test
	public void testWithDefaultMappingsNoAdd()
	{
		PrefixMapping pm = new PrefixMappingImpl();
		try
		{
			// make sure that it must update
			securedMapping.withDefaultMappings(pm);
//			if (!securityEvaluator.evaluate(Action.Update,
//					securedMapping.getModelNode()))
//			{
//				Assert.fail("Should have thrown AccessDenied Exception");
//			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update,
					securedMapping.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}
}
