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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.test.JenaTestBase;

import org.junit.Assert;

public class TestModel extends AbstractModelTestBase
{

	/**
	 * Test cases for RemoveSPO(); each entry is a triple (add, remove, result).
	 * <ul>
	 * <li>add - the triples to add to the graph to start with
	 * <li>remove - the pattern to use in the removal
	 * <li>result - the triples that should remain in the graph
	 * </ul>
	 */
	protected String[][] cases = { { "x R y", "x R y", "" },
			{ "x R y; a P b", "x R y", "a P b" },
			{ "x R y; a P b", "?? R y", "a P b" },
			{ "x R y; a P b", "x R ??", "a P b" },
			{ "x R y; a P b", "x ?? y", "a P b" },
			{ "x R y; a P b", "?? ?? ??", "" },
			{ "x R y; a P b; c P d", "?? P ??", "x R y" },
			{ "x R y; a P b; x S y", "x ?? ??", "a P b" }, };

	public TestModel( final TestingModelFactory modelFactory, final String name )
	{
		super(modelFactory, name);
	}

	protected Model copy( final Model m )
	{
		return createModel().add(m);
	}

	public void testAsRDF()
	{
		testPresentAsRDFNode(GraphTestBase.node("a"), Resource.class);
		testPresentAsRDFNode(GraphTestBase.node("17"), Literal.class);
		testPresentAsRDFNode(GraphTestBase.node("_b"), Resource.class);
	}

	public void testContainsResource()
	{
		ModelHelper.modelAdd(model, "x R y; _a P _b");
		Assert.assertTrue(model.containsResource(ModelHelper.resource(model,
				"x")));
		Assert.assertTrue(model.containsResource(ModelHelper.resource(model,
				"R")));
		Assert.assertTrue(model.containsResource(ModelHelper.resource(model,
				"y")));
		Assert.assertTrue(model.containsResource(ModelHelper.resource(model,
				"_a")));
		Assert.assertTrue(model.containsResource(ModelHelper.resource(model,
				"P")));
		Assert.assertTrue(model.containsResource(ModelHelper.resource(model,
				"_b")));
		Assert.assertFalse(model.containsResource(ModelHelper.resource(model,
				"i")));
		Assert.assertFalse(model.containsResource(ModelHelper.resource(model,
				"_j")));
	}

	public void testCreateBlankFromNode()
	{
		final RDFNode S = model.getRDFNode(NodeCreateUtils.create("_Blank"));
		JenaTestBase.assertInstanceOf(Resource.class, S);
		Assert.assertEquals(new AnonId("_Blank"), ((Resource) S).getId());
	}

	public void testCreateLiteralFromNode()
	{
		final RDFNode S = model.getRDFNode(NodeCreateUtils.create("42"));
		JenaTestBase.assertInstanceOf(Literal.class, S);
		Assert.assertEquals("42", ((Literal) S).getLexicalForm());
	}

	public void testCreateResourceFromNode()
	{
		final RDFNode S = model.getRDFNode(NodeCreateUtils.create("spoo:S"));
		JenaTestBase.assertInstanceOf(Resource.class, S);
		Assert.assertEquals("spoo:S", ((Resource) S).getURI());
	}

	/**
	 * Test the new version of getProperty(), which delivers null for not-found
	 * properties.
	 */
	public void testGetProperty()
	{
		ModelHelper.modelAdd(model, "x P a; x P b; x R c");
		final Resource x = ModelHelper.resource(model, "x");
		Assert.assertEquals(ModelHelper.resource(model, "c"),
				x.getProperty(ModelHelper.property(model, "R")).getObject());
		final RDFNode ob = x.getProperty(ModelHelper.property(model, "P"))
				.getObject();
		Assert.assertTrue(ob.equals(ModelHelper.resource(model, "a"))
				|| ob.equals(ModelHelper.resource(model, "b")));
		Assert.assertNull(x.getProperty(ModelHelper.property(model,
				"noSuchPropertyHere")));
	}

	public void testIsClosedDelegatedToGraph()
	{
		Assert.assertFalse(model.isClosed());
		model.close();
		Assert.assertTrue(model.isClosed());
	}

	public void testIsEmpty()
	{
		final Statement S1 = ModelHelper.statement(model,
				"model rdf:type nonEmpty");
		final Statement S2 = ModelHelper.statement(model, "pinky rdf:type Pig");
		Assert.assertTrue(model.isEmpty());
		model.add(S1);
		Assert.assertFalse(model.isEmpty());
		model.add(S2);
		Assert.assertFalse(model.isEmpty());
		model.remove(S1);
		Assert.assertFalse(model.isEmpty());
		model.remove(S2);
		Assert.assertTrue(model.isEmpty());
	}

	public void testLiteralNodeAsResourceFails()
	{
		try
		{
			model.wrapAsResource(GraphTestBase.node("17"));
			Assert.fail("should fail to convert literal to Resource");
		}
		catch (final UnsupportedOperationException e)
		{
			JenaTestBase.pass();
		}
	}

	private void testPresentAsRDFNode( final Node n,
			final Class<? extends RDFNode> nodeClass )
	{
		final RDFNode r = model.asRDFNode(n);
		Assert.assertSame(n, r.asNode());
		JenaTestBase.assertInstanceOf(nodeClass, r);
	}

	public void testRemoveAll()
	{
		testRemoveAll("");
		testRemoveAll("a RR b");
		testRemoveAll("x P y; a Q b; c R 17; _d S 'e'");
		testRemoveAll("subject Predicate 'object'; http://nowhere/x scheme:cunning not:plan");
	}

	protected void testRemoveAll( final String statements )
	{
		ModelHelper.modelAdd(model, statements);
		Assert.assertSame(model, model.removeAll());
		Assert.assertEquals("model should have size 0 following removeAll(): ",
				0, model.size());
	}

	/**
	 * Test that remove(s, p, o) works, in the presence of inferencing graphs
	 * that
	 * mean emptyness isn't available. This is why we go round the houses and
	 * test that expected ~= initialContent + addedStuff - removed -
	 * initialContent.
	 */
	public void testRemoveSPO()
	{
		final Model mc = createModel();
		for (final String[] case1 : cases)
		{
			for (int j = 0; j < 3; j += 1)
			{
				final Model content = createModel();
				final Model baseContent = copy(content);
				ModelHelper.modelAdd(content, case1[0]);
				final Triple remove = GraphTestBase.triple(case1[1]);
				final Node s = remove.getSubject(), p = remove.getPredicate(), o = remove
						.getObject();
				final Resource S = (Resource) (s.equals(Node.ANY) ? null : mc
						.getRDFNode(s));
				final Property P = ((p.equals(Node.ANY) ? null : mc.getRDFNode(
						p).as(Property.class)));
				final RDFNode O = o.equals(Node.ANY) ? null : mc.getRDFNode(o);
				final Model expected = ModelHelper.modelWithStatements(this,
						case1[2]);
				content.removeAll(S, P, O);
				final Model finalContent = copy(content).remove(baseContent);
				ModelHelper.assertIsoModels(case1[1], expected, finalContent);
			}
		}
	}

	public void testToStatement()
	{
		final Triple t = GraphTestBase.triple("a P b");
		final Statement s = model.asStatement(t);
		Assert.assertEquals(GraphTestBase.node("a"), s.getSubject().asNode());
		Assert.assertEquals(GraphTestBase.node("P"), s.getPredicate().asNode());
		Assert.assertEquals(GraphTestBase.node("b"), s.getObject().asNode());
	}

	public void testTransactions()
	{
		final Command cmd = new Command() {
			@Override
			public Object execute()
			{
				return null;
			}
		};
		if (model.supportsTransactions())
		{
			model.executeInTransaction(cmd);
		}
	}

	public void testURINodeAsResource()
	{
		final Node n = GraphTestBase.node("a");
		final Resource r = model.wrapAsResource(n);
		Assert.assertSame(n, r.asNode());
	}
}
