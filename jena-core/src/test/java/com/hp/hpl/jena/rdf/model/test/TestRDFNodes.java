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

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.LiteralRequiredException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceRequiredException;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.test.JenaTestBase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

/**
 * This class tests various properties of RDFNodes.
 */
public class TestRDFNodes extends AbstractModelTestBase
{

	public TestRDFNodes( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void testInModel()
	{
		final Model m1 = ModelHelper.modelWithStatements(this, "");
		final Model m2 = ModelHelper.modelWithStatements(this, "");
		final Resource r1 = ModelHelper.resource(m1, "r1");
		final Resource r2 = ModelHelper.resource(m1, "_r2");
		/* */
		Assert.assertEquals(r1.getModel(), m1);
		Assert.assertEquals(r2.getModel(), m1);
		Assert.assertFalse(r1.isAnon());
		Assert.assertTrue(r2.isAnon());
		/* */
		Assert.assertEquals(r1.inModel(m2).getModel(), m2);
		Assert.assertEquals(r2.inModel(m2).getModel(), m2);
		/* */
		Assert.assertEquals(r1, r1.inModel(m2));
		Assert.assertEquals(r2, r2.inModel(m2));
	}

	public void testIsAnon()
	{
		final Model m = ModelHelper.modelWithStatements(this, "");
		Assert.assertEquals(false, m.createResource("eh:/foo").isAnon());
		Assert.assertEquals(true, m.createResource().isAnon());
		Assert.assertEquals(false, m.createTypedLiteral(17).isAnon());
		Assert.assertEquals(false, m.createTypedLiteral("hello").isAnon());
	}

	public void testIsLiteral()
	{
		final Model m = ModelHelper.modelWithStatements(this, "");
		Assert.assertEquals(false, m.createResource("eh:/foo").isLiteral());
		Assert.assertEquals(false, m.createResource().isLiteral());
		Assert.assertEquals(true, m.createTypedLiteral(17).isLiteral());
		Assert.assertEquals(true, m.createTypedLiteral("hello").isLiteral());
	}

	public void testIsResource()
	{
		final Model m = ModelHelper.modelWithStatements(this, "");
		Assert.assertEquals(true, m.createResource("eh:/foo").isResource());
		Assert.assertEquals(true, m.createResource().isResource());
		Assert.assertEquals(false, m.createTypedLiteral(17).isResource());
		Assert.assertEquals(false, m.createTypedLiteral("hello").isResource());
	}

	public void testIsURIResource()
	{
		final Model m = ModelHelper.modelWithStatements(this, "");
		Assert.assertEquals(true, m.createResource("eh:/foo").isURIResource());
		Assert.assertEquals(false, m.createResource().isURIResource());
		Assert.assertEquals(false, m.createTypedLiteral(17).isURIResource());
		Assert.assertEquals(false, m.createTypedLiteral("hello")
				.isURIResource());
	}

	public void testLiteralAsResourceThrows()
	{
		final Model m = ModelHelper.modelWithStatements(this, "");
		final Resource r = m.createResource("eh:/spoo");
		try
		{
			r.asLiteral();
			Assert.fail("should not be able to do Resource.asLiteral()");
		}
		catch (final LiteralRequiredException e)
		{
		}
	}

	public void testRDFNodeAsLiteral()
	{
		final Model m = ModelHelper.modelWithStatements(this, "");
		final Literal l = m.createLiteral("hello, world");
		Assert.assertSame(l, ((RDFNode) l).asLiteral());
	}

	public void testRDFNodeAsResource()
	{
		final Model m = ModelHelper.modelWithStatements(this, "");
		final Resource r = m.createResource("eh:/spoo");
		Assert.assertSame(r, ((RDFNode) r).asResource());
	}

	public void testRDFVisitor()
	{
		final List<String> history = new ArrayList<>();
		final Model m = ModelFactory.createDefaultModel();
		final RDFNode S = m.createResource();
		final RDFNode P = m.createProperty("eh:PP");
		final RDFNode O = m.createLiteral("LL");
		/* */
		final RDFVisitor rv = new RDFVisitor() {
			@Override
			public Object visitBlank( final Resource R, final AnonId id )
			{
				history.add("blank");
				Assert.assertTrue("must visit correct node", R == S);
				Assert.assertEquals("must have correct field", R.getId(), id);
				return "blank result";
			}

			@Override
			public Object visitLiteral( final Literal L )
			{
				history.add("literal");
				Assert.assertTrue("must visit correct node", L == O);
				return "literal result";
			}

			@Override
			public Object visitURI( final Resource R, final String uri )
			{
				history.add("uri");
				Assert.assertTrue("must visit correct node", R == P);
				Assert.assertEquals("must have correct field", R.getURI(), uri);
				return "uri result";
			}
		};
		/* */
		Assert.assertEquals("blank result", S.visitWith(rv));
		Assert.assertEquals("uri result", P.visitWith(rv));
		Assert.assertEquals("literal result", O.visitWith(rv));
		Assert.assertEquals(JenaTestBase.listOfStrings("blank uri literal"),
				history);
	}

	public void testRemoveAllBoring()
	{
		final Model m1 = ModelHelper.modelWithStatements(this, "x P a; y Q b");
		final Model m2 = ModelHelper.modelWithStatements(this, "x P a; y Q b");
		ModelHelper.resource(m2, "x").removeAll(ModelHelper.property(m2, "Z"));
		ModelHelper.assertIsoModels("m2 should be unchanged", m1, m2);
	}

	public void testRemoveAllRemoves()
	{
		final String ps = "x P a; x P b", rest = "x Q c; y P a; y Q b";
		final Model m = ModelHelper.modelWithStatements(this, ps + "; " + rest);
		final Resource r = ModelHelper.resource(m, "x");
		final Resource r2 = r.removeAll(ModelHelper.property(m, "P"));
		Assert.assertSame("removeAll should deliver its receiver", r, r2);
		ModelHelper.assertIsoModels("x's P-values should go",
				ModelHelper.modelWithStatements(this, rest), m);
	}

	public void testResourceAsLiteralThrows()
	{
		final Model m = ModelHelper.modelWithStatements(this, "");
		final Literal l = m.createLiteral("hello, world");
		try
		{
			l.asResource();
			Assert.fail("should not be able to do Literal.asResource()");
		}
		catch (final ResourceRequiredException e)
		{
		}
	}
}
