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
package org.apache.jena.security.model;

import java.io.* ;
import java.net.URL ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.security.* ;
import org.apache.jena.security.SecurityEvaluator.Action ;
import org.apache.jena.security.graph.SecuredGraph ;
import org.apache.jena.security.graph.SecuredPrefixMappingTest ;
import org.junit.Assert ;
import org.junit.Before ;
import org.junit.Test ;
import org.junit.runner.RunWith ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.* ;

@RunWith( value = SecurityEvaluatorParameters.class )
public class SecuredModelTest
{
	protected final MockSecurityEvaluator securityEvaluator;
	protected SecuredModel securedModel;
	protected Model baseModel;
	protected Resource s;
	protected Property p;
	protected Resource o;

	public SecuredModelTest( final MockSecurityEvaluator securityEvaluator )
	{
		this.securityEvaluator = securityEvaluator;
	}

	/**
	 * create an unsecured securedModel.
	 * 
	 * @return
	 */
	protected Model createModel()
	{
		return ModelFactory.createDefaultModel();
	}

	@Before
	public void setup()
	{
		baseModel = createModel();
		baseModel.removeAll();
		securedModel = Factory.getInstance(securityEvaluator,
				"http://example.com/securedGraph", baseModel);
		s = ResourceFactory.createResource("http://example.com/graph/s");
		p = ResourceFactory.createProperty("http://example.com/graph/p");
		o = ResourceFactory.createResource("http://example.com/graph/o");
		baseModel.add(s, p, o);
	}

	@Test
	public void testAdd() throws Exception
	{
		final List<Statement> stmt = baseModel.listStatements().toList();
		final Set<Action> createAndUpdate = SecurityEvaluator.Util
				.asSet(new Action[] { Action.Update, Action.Create });
		try
		{
			securedModel.add(stmt);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(baseModel);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(stmt.get(0));
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{

			securedModel.add(stmt.toArray(new Statement[stmt.size()]));
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(baseModel.listStatements());
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(baseModel);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(s, p, o);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(s, p, "foo");
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(s, p, "foo", false);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(s, p, "foo", XSDDatatype.XSDstring);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.add(s, p, "foo", "en");
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testAnonymousInModel()
	{
		// test anonymous
		final RDFNode rdfNode = ResourceFactory.createResource();
		final RDFNode rdfNode2 = rdfNode.inModel(securedModel);
		Assert.assertEquals(
				"Should have placed RDFNode in secured securedModel",
				securedModel, rdfNode2.getModel());
	}

	@Test
	public void testAsRDFNode() throws Exception
	{
		securedModel.asRDFNode(NodeFactory.createURI("http://example.com/rdfNode"));
	}

	@Test
	public void testAsStatement()
	{
		final Triple t = new Triple(s.asNode(), p.asNode(), o.asNode());
		try
		{
			securedModel.asStatement(t);
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
	public void testContains() throws Exception
	{
		final Statement stmt = baseModel.listStatements().next();
		try
		{
			securedModel.contains(stmt);
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
			securedModel.contains(s, p);
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
			securedModel.contains(s, p, o);
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
			securedModel.contains(s, p, "foo");
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
			securedModel.contains(s, p, "foo", "en");
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
	public void testContainsAll() throws Exception
	{
		try
		{
			securedModel.containsAll(baseModel);
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
			securedModel.containsAll(baseModel.listStatements());
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
	public void testCreateAlt() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });
		try
		{
			securedModel.createAlt();
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.createAlt("foo");
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testCreateBag() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });
		try
		{
			securedModel.createBag();
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.createBag("foo");
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testCreateList() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create });

		final List<RDFNode> nodeList = new ArrayList<RDFNode>();
		try
		{
			securedModel.createList();
			if (!securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		baseModel.removeAll();

		try
		{
			securedModel.createList(nodeList.iterator());
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		baseModel.removeAll();

		try
		{
			final RDFNode[] list = new RDFNode[] {
					ResourceFactory.createResource(),
					ResourceFactory.createResource(),
					ResourceFactory.createResource(),
					ResourceFactory.createResource(), };

			securedModel.createList(list);
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		baseModel.removeAll();

	}

	@Test
	public void testCreateLiteral() throws Exception
	{
		securedModel.createLiteral("foo");
		securedModel.createLiteral("foo", false);
	}

	@Test
	public void testCreateLiteralBoolean() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });

		try
		{
			securedModel.createLiteralStatement(s, p, true);
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testCreateLiteralChar() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });
		try
		{
			securedModel.createLiteralStatement(s, p, 'a');
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testCreateLiteralDouble() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });

		try
		{
			securedModel.createLiteralStatement(s, p, 1.0d);
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testCreateLiteralFloat() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });

		try
		{
			securedModel.createLiteralStatement(s, p, 1.0f);
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testCreateLiteralInt() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });

		try
		{
			securedModel.createLiteralStatement(s, p, 1);
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testCreateLiteralLong() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });

		try
		{
			securedModel.createLiteralStatement(s, p, 1L);
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testCreateLiteralObject() throws Exception
	{
		final Set<Action> CU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Update });

		try
		{
			securedModel.createLiteralStatement(s, p, new URL( "http://example.com/testing/URIType"));
			if (!securityEvaluator.evaluate(CU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(CU))
			{
				e.printStackTrace();
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testDifference() throws Exception
	{
		try
		{
			securedModel.difference(baseModel);
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
	public void testEquals() throws Exception
	{
		securedModel.equals(baseModel);
		baseModel.equals(securedModel);
	}

	@Test
	public void testExpandPrefix() throws Exception
	{
		try
		{
			securedModel.expandPrefix("foo");
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
	public void testGetAlt() throws Exception
	{
		final Resource a = baseModel
				.createAlt("http://example.com/securedModel/alt");
		try
		{

			securedModel.getAlt(a);
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
			securedModel.getAlt("http://example.com/securedModel/alt");
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
	public void testGetAnyReifiedStmt()
	{
		// first with create.
		final Set<Action> UCR = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Update, Action.Create, Action.Read });
		try
		{
			securedModel.getAnyReifiedStatement(baseModel.listStatements()
					.next());
			if (!securityEvaluator.evaluate(UCR))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(UCR))
			{
				e.printStackTrace();
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		final Statement st = baseModel.listStatements().next();
		baseModel.createReifiedStatement(st);
		// now it is there so try with read
		try
		{
			securedModel.getAnyReifiedStatement(st);
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
	public void testGetBag()
	{
		final Resource b = baseModel
				.createBag("http://example.com/securedModel/bag");
		try
		{
			securedModel.getBag(b);
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
			securedModel.getBag("http://example.com/securedModel/bag");
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
	public void testGetGraph() throws Exception
	{
		final Graph g = securedModel.getGraph();
		Assert.assertTrue(g instanceof SecuredGraph);
		EqualityTester.testInequality("getGraph test", g, baseModel.getGraph());
	}

	@Test
	public void testGetLock()
	{
		securedModel.getLock();
	}

	@Test
	public void testGetProperty()
	{

		try
		{
			securedModel.getProperty("foo");
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
			securedModel.getProperty(s, p);
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
			securedModel.getProperty("fooNS", "foo");
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
	public void testGetQNameFor() throws Exception
	{
		try
		{
			securedModel.qnameFor("foo");
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
	public void testgetRDFNode()
	{

		try
		{
			securedModel.getRDFNode(NodeFactory.createURI("foo"));
			if (!securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testGetReader()
	{
		securedModel.getReader();
		securedModel.getReader("TURTLE");
	}

	@Test
	public void testGetResource()
	{
		securedModel.getResource("foo");
	}

	@Test
	public void testGetSeq()
	{
		final Resource s = baseModel
				.createSeq("http://example.com/securedModel/seq");
		try
		{
			securedModel.getSeq(s);
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
			securedModel.getSeq("http://example.com/securedModel/seq");
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
	public void testGetWriter()
	{
		securedModel.getWriter();
		securedModel.getWriter("TURTLE");
	}

	@Test
	public void testIndependent() throws Exception
	{
		Assert.assertFalse(securedModel.independent());
	}

	@Test
	public void testIntersection() throws Exception
	{
		try
		{
			securedModel.intersection(baseModel);
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
	public void testIsClosed() throws Exception
	{
		securedModel.isClosed();
	}

	@Test
	public void testIsEmpty() throws Exception
	{
		try
		{
			securedModel.isEmpty();
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
	public void testIsIsomorphicWith()
	{
		try
		{
			securedModel.isIsomorphicWith(baseModel);
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
			baseModel.isIsomorphicWith(securedModel);
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
	public void testIsReified()
	{
		try
		{
			securedModel.isReified(baseModel.listStatements().next());
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
	public void testListLiteralStatements() throws Exception
	{
		try
		{
			securedModel.listLiteralStatements(s, p, true);
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
			securedModel.listLiteralStatements(s, p, '0');
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
			securedModel.listLiteralStatements(s, p, 2.0d);
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
			securedModel.listLiteralStatements(s, p, 2.0f);
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
			securedModel.listLiteralStatements(s, p, 1);
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
	public void testLock() throws Exception
	{
		try
		{
			securedModel.lock();
			if (!securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testPrefixMapping() throws Exception
	{
		SecuredPrefixMappingTest.runTests(securityEvaluator, securedModel);
	}

	@Test
	public void testQuery() throws Exception
	{
		final Selector s = new SimpleSelector();
		try
		{
			securedModel.query(s);
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
	public void testRDFNodeInModel()
	{
		// test uri
		final RDFNode rdfNode = ResourceFactory
				.createResource("http://exmple.com/testInModel");
		final RDFNode rdfNode2 = rdfNode.inModel(securedModel);
		Assert.assertEquals(
				"Should have placed RDFNode in secured securedModel",
				securedModel, rdfNode2.getModel());
	}

	@Test
	public void testReadEmpty() throws Exception
	{
		final Set<Action> createAndUpdate = SecurityEvaluator.Util
				.asSet(new Action[] { Action.Update, Action.Create });

		final String XML_INPUT = "<rdf:RDF"
				+ "   xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' "
				+ "   xmlns:rt='http://example.com/readTest#' "
				+ "   xmlns:j.0='http://example.com/readTest#3' > "
				+ "  <rdf:Description rdf:about='http://example.com/readTest#1'> "
				+ "    <rdf:type rdf:resource='http://example.com/readTest#3'/>"
				+ "  </rdf:Description>" + "</rdf:RDF>";
		final String TTL_INPUT = "@prefix rt: <http://example.com/readTest#> . rt:1 a rt:3 .";
		final String base = "http://example.com/test";
		final String lang = "TURTLE";
		try
		{
			final URL url = SecuredModelTest.class.getResource("./test.xml");
			securedModel.read(url.toString());
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		finally
		{
			baseModel.removeAll();
		}

		try
		{
			final InputStream in = new ByteArrayInputStream(
					XML_INPUT.getBytes());
			securedModel.read(in, base);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		finally
		{
			baseModel.removeAll();
		}

		try
		{
			final Reader reader = new StringReader(XML_INPUT);
			securedModel.read(reader, base);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		finally
		{
			baseModel.removeAll();
		}

		try
		{
			final URL url = SecuredModelTest.class.getResource("./test.ttl");
			securedModel.read(url.toString(), lang);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		finally
		{
			baseModel.removeAll();
		}

		try
		{
			final InputStream in = new ByteArrayInputStream(
					TTL_INPUT.getBytes());
			securedModel.read(in, base, lang);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		finally
		{
			baseModel.removeAll();
		}

		try
		{
			final Reader reader = new StringReader(TTL_INPUT);
			securedModel.read(reader, base, lang);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		finally
		{
			baseModel.removeAll();
		}

		try
		{
			final URL url = SecuredModelTest.class.getResource("./test.ttl");
			securedModel.read(url.toString(), base, lang);
			if (!securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(createAndUpdate))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		finally
		{
			baseModel.removeAll();
		}

	}

	@Test
	public void testRemove() throws Exception
	{
		final Set<Action> DU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Delete, Action.Update });

		final List<Statement> stmt = baseModel.listStatements().toList();
		try
		{
			securedModel.remove(baseModel.listStatements().toList());
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedModel.remove(baseModel);
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
		try
		{
			securedModel.remove(stmt.get(0));
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedModel.remove(stmt.toArray(new Statement[stmt.size()]));
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedModel.remove(baseModel.listStatements());
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedModel.remove(baseModel);
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		try
		{
			securedModel.remove(s, p, o);
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

	}

	@Test
	public void testRemoveAll() throws Exception
	{
		final Set<Action> DU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Delete, Action.Update });

		try
		{
			securedModel.removeAll();
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}

		// put some data back
		baseModel.add(s, p, o);
		try
		{
			securedModel.removeAll(s, p, o);
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testRemoveAllReifications()
	{
		final Set<Action> DU = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Delete, Action.Update });

		final List<Statement> stmt = baseModel.listStatements().toList();
		baseModel.createReifiedStatement(stmt.get(0));

		try
		{
			securedModel.removeAllReifications(stmt.get(0));
			if (!securityEvaluator.evaluate(DU))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(DU))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testRequiredProperty()
	{

		try
		{
			securedModel.getRequiredProperty(s, p);
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
			securedModel.size();
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
	public void testUnion() throws Exception
	{
		try
		{
			securedModel.union(baseModel);
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
			baseModel.union(securedModel);
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
	public void testVariableInModel()
	{
		try
		{
			final RDFNode rdfNode = ResourceFactory
					.createTypedLiteral("yeehaw");
			final RDFNode rdfNode2 = rdfNode.inModel(securedModel);
			if (!securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail("Should have thrown AccessDenied Exception");
			}
			Assert.assertEquals(
					"Should have placed RDFNode in secured securedModel",
					securedModel, rdfNode2.getModel());

		}
		catch (final AccessDeniedException e)
		{
			if (securityEvaluator.evaluate(Action.Update))
			{
				Assert.fail(String
						.format("Should not have thrown AccessDenied Exception: %s - %s",
								e, e.getTriple()));
			}
		}
	}

	@Test
	public void testWrapAsResource() throws Exception
	{
		securedModel.wrapAsResource(NodeFactory.createURI("http://example.com/rdfNode"));
	}

	@Test
	public void testWrite() throws Exception
	{
		final OutputStream out = new ByteArrayOutputStream();
		final Writer writer = new CharArrayWriter();
		final String lang = "TURTLE";
		try
		{
			securedModel.write(out);
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
			securedModel.write(writer);
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
			securedModel.write(out, lang);
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
			securedModel.write(writer, lang);
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
			securedModel.write(out, lang, "http://example.com/securedGraph");
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
			securedModel.write(writer, lang, "http://example.com/securedGraph");
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