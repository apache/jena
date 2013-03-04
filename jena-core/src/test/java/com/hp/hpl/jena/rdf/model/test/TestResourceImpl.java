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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.ResourceRequiredException;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

/**
 * TestResourceImpl - fresh tests, make sure as-ing works a bit.
 */
public class TestResourceImpl extends AbstractModelTestBase
{
	public TestResourceImpl( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void testAddLiteralPassesLiteralUnmodified()
	{
		final Resource r = model.createResource();
		final Literal lit = model.createLiteral("spoo");
		r.addLiteral(RDF.value, lit);
		Assert.assertTrue("model should contain unmodified literal",
				model.contains(null, RDF.value, lit));
	}

	public void testAddTypedPropertyBoolean()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, true);
		Assert.assertEquals(model.createTypedLiteral(true),
				r.getProperty(RDF.value).getLiteral());
	}

	public void testAddTypedPropertyChar()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 'x');
		Assert.assertEquals(model.createTypedLiteral('x'),
				r.getProperty(RDF.value).getLiteral());
	}

	public void testAddTypedPropertyDouble()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 1.0d);
		Assert.assertEquals(model.createTypedLiteral(1.0d),
				r.getProperty(RDF.value).getLiteral());
	}

	public void testAddTypedPropertyFloat()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 1.0f);
		Assert.assertEquals(model.createTypedLiteral(1.0f),
				r.getProperty(RDF.value).getLiteral());
	}

	public void testAddTypedPropertyInt()
	{
		// Model model = ModelFactory.createDefaultModel();
		// Resource r = model.createResource();
		// r.addLiteral( RDF.value, 1 );
		// assertEquals( model.createTypedLiteral( 1 ), r.getProperty( RDF.value
		// ).getLiteral() );
	}

	public void testAddTypedPropertyLong()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 1L);
		Assert.assertEquals(model.createTypedLiteral(1L),
				r.getProperty(RDF.value).getLiteral());
	}

	public void testAddTypedPropertyObject()
	{
		final Object z = new Object();
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, z);
		Assert.assertEquals(model.createTypedLiteral(z),
				r.getProperty(RDF.value).getLiteral());
	}

	public void testAddTypedPropertyString()
	{

	}

	/**
	 * Test that a literal node cannot be as'ed into a resource.
	 */
	public void testAsLiteral()
	{
		try
		{
			ModelHelper.literal(model, "17").as(Resource.class);
			Assert.fail("literals cannot be resources");
		}
		catch (final ResourceRequiredException e)
		{
			JenaTestBase.pass();
		}
	}

	/**
	 * Test that a non-literal node can be as'ed into a resource
	 */
	public void testCannotAsNonLiteral()
	{
		ModelHelper.resource(model, "plumPie").as(Resource.class);
	}

	public void testGetLocalNameReturnsLocalName()
	{
		Assert.assertEquals("xyz", ModelHelper.resource("eh:xyz")
				.getLocalName());
	}

	public void testGetModel()
	{

		Assert.assertSame(model, model.createResource("eh:/wossname")
				.getModel());
	}

	public void testGetPropertyResourceValueReturnsNull()
	{
		final Model model = ModelHelper.modelWithStatements(this, "x p 17");
		final Resource r = model.createResource("eh:/x");
		Assert.assertNull(r.getPropertyResourceValue(ModelHelper.property("q")));
		Assert.assertNull(r.getPropertyResourceValue(ModelHelper.property("p")));
	}

	public void testGetPropertyResourceValueReturnsResource()
	{
		final Model model = ModelHelper.modelWithStatements(this,
				"x p 17; x p y");
		final Resource r = model.createResource("eh:/x");
		final Resource value = r.getPropertyResourceValue(ModelHelper
				.property("p"));
		Assert.assertEquals(ModelHelper.resource("y"), value);
	}

	public void testHasTypedPropertyBoolean()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, false);
		Assert.assertTrue(r.hasLiteral(RDF.value, false));
	}

	public void testHasTypedPropertyChar()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 'x');
		Assert.assertTrue(r.hasLiteral(RDF.value, 'x'));
	}

	public void testHasTypedPropertyDouble()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 1.0d);
		Assert.assertTrue(r.hasLiteral(RDF.value, 1.0d));
	}

	public void testHasTypedPropertyFloat()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 1.0f);
		Assert.assertTrue(r.hasLiteral(RDF.value, 1.0f));
	}

	public void testHasTypedPropertyInt()
	{
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 1);
		Assert.assertTrue(r.hasLiteral(RDF.value, 1));
	}

	public void testHasTypedPropertyLong()
	{

		final Resource r = model.createResource();
		r.addLiteral(RDF.value, 1L);
		Assert.assertTrue(r.hasLiteral(RDF.value, 1L));
	}

	public void testHasTypedPropertyObject()
	{
		final Object z = new Object();
		final Resource r = model.createResource();
		r.addLiteral(RDF.value, z);
		Assert.assertTrue(r.hasLiteral(RDF.value, z));
	}

	public void testHasTypedPropertyString()
	{

	}

	public void testHasURI()
	{
		Assert.assertTrue(ModelHelper.resource("eh:xyz").hasURI("eh:xyz"));
		Assert.assertFalse(ModelHelper.resource("eh:xyz").hasURI("eh:1yz"));
		Assert.assertFalse(ResourceFactory.createResource().hasURI("42"));
	}

	public void testNameSpace()
	{
		Assert.assertEquals("eh:", ModelHelper.resource("eh:xyz")
				.getNameSpace());
		Assert.assertEquals("http://d/", ModelHelper.resource("http://d/stuff")
				.getNameSpace());
		Assert.assertEquals("ftp://dd.com/12345",
				ModelHelper.resource("ftp://dd.com/12345").getNameSpace());
		Assert.assertEquals("http://domain/spoo#",
				ModelHelper.resource("http://domain/spoo#anchor")
						.getNameSpace());
		Assert.assertEquals("ftp://abd/def#ghi#",
				ModelHelper.resource("ftp://abd/def#ghi#e11-2").getNameSpace());
	}
}
