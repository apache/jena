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
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.CollectionGraph;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

import java.util.Arrays;
import java.util.Set;

import org.junit.Assert;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.Factory;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluatorParameters;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.graph.SecuredBulkUpdateHandler;
import org.apache.jena.security.graph.SecuredGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( value = SecurityEvaluatorParameters.class )
public class BulkUpdateHandlerTest
{
	protected SecuredBulkUpdateHandler handler;
	private final SecurityEvaluator securityEvaluator;
	private final Triple[] tripleArray;
	private final Set<Action> deleteAndUpdate;
	private final Set<Action> createAndUpdate;

	public BulkUpdateHandlerTest( final SecurityEvaluator securityEvaluator )
	{
		this.securityEvaluator = securityEvaluator;

		tripleArray = new Triple[] {
				new Triple(NodeFactory.createURI("http://example.com/1"),
						NodeFactory.createURI("http://example.com/v"),
						NodeFactory.createAnon()),
				new Triple(NodeFactory.createURI("http://example.com/2"),
						NodeFactory.createURI("http://example.com/v"),
						NodeFactory.createAnon()),
				new Triple(NodeFactory.createURI("http://example.com/3"),
						NodeFactory.createURI("http://example.com/v"),
						NodeFactory.createAnon()) };
		createAndUpdate = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });
		deleteAndUpdate = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Delete, Action.Update });
	}

	@Before
	public void setup()
	{
		final Graph g = GraphFactory.createDefaultGraph();

		final SecuredGraph sg = Factory.getInstance(securityEvaluator,
				"http://example.com/testGraph", g);
		handler = sg.getBulkUpdateHandler();
	}

	@Test
	public void testAdd()
	{
		try
		{
			handler.add(tripleArray);
			if (!securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.add(Arrays.asList(tripleArray));
			if (!securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.add(Arrays.asList(tripleArray).iterator());
			if (!securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.add(new CollectionGraph(Arrays.asList(tripleArray)));
			if (!securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.add(new CollectionGraph(Arrays.asList(tripleArray)));
			if (!securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.add(new CollectionGraph(Arrays.asList(tripleArray)), true);
			if (!securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testDelete()
	{

		try
		{
			handler.delete(tripleArray);
			if (!securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.delete(Arrays.asList(tripleArray));
			if (!securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.delete(Arrays.asList(tripleArray).iterator());
			if (!securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.delete(new CollectionGraph(Arrays.asList(tripleArray)));
			if (!securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			handler.delete(new CollectionGraph(Arrays.asList(tripleArray)),
					true);
			if (!securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	public void testRemove()
	{
		try
		{
			handler.remove(NodeFactory.createURI("http://example.com/1"),
					NodeFactory.createURI("http://example.com/v"), NodeFactory.createAnon());
			if (!securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	public void testRemoveAll()
	{
		try
		{
			handler.removeAll();
			if (!securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{

				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(deleteAndUpdate,
					handler.getModelNode()))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

}
