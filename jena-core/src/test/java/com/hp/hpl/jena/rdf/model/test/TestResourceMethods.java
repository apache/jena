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

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

public class TestResourceMethods extends AbstractModelTestBase
{
	protected Resource r;

	protected final String lang = "en";

	protected Literal tvLiteral;

	protected Resource tvResource;

	public TestResourceMethods( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		tvLiteral = model.createLiteral("test 12 string 2");
		tvResource = model.createResource();
		r = model.createResource()
				.addLiteral(RDF.value, AbstractModelTestBase.tvBoolean)
				.addLiteral(RDF.value, AbstractModelTestBase.tvByte)
				.addLiteral(RDF.value, AbstractModelTestBase.tvShort)
				.addLiteral(RDF.value, AbstractModelTestBase.tvInt)
				.addLiteral(RDF.value, AbstractModelTestBase.tvLong)
				.addLiteral(RDF.value, AbstractModelTestBase.tvChar)
				.addLiteral(RDF.value, AbstractModelTestBase.tvFloat)
				.addLiteral(RDF.value, AbstractModelTestBase.tvDouble)
				.addProperty(RDF.value, AbstractModelTestBase.tvString)
				.addProperty(RDF.value, AbstractModelTestBase.tvString, lang)
				.addLiteral(RDF.value, AbstractModelTestBase.tvObject)
				.addProperty(RDF.value, tvLiteral)
				.addProperty(RDF.value, tvResource);
	}

	public void testAllSubjectsCorrect()
	{
		testHasSubjectR(model.listStatements());
		testHasSubjectR(r.listProperties());
	}

	public void testBoolean()
	{
		Assert.assertTrue(r.hasLiteral(RDF.value,
				AbstractModelTestBase.tvBoolean));
	}

	public void testByte()
	{
		Assert.assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvByte));
	}

	public void testChar()
	{
		Assert.assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvChar));
	}

	public void testCorrectSubject()
	{
		Assert.assertEquals(r, r.getRequiredProperty(RDF.value).getSubject());
	}

	public void testCountsCorrect()
	{
		Assert.assertEquals(13,
				GraphTestBase.iteratorToList(model.listStatements()).size());
		Assert.assertEquals(13,
				GraphTestBase.iteratorToList(r.listProperties(RDF.value))
						.size());
		Assert.assertEquals(0,
				GraphTestBase.iteratorToList(r.listProperties(RDF.type)).size());
	}

	public void testDouble()
	{
		Assert.assertTrue(r.hasLiteral(RDF.value,
				AbstractModelTestBase.tvDouble));
	}

	public void testFloat()
	{
		Assert.assertTrue(r
				.hasLiteral(RDF.value, AbstractModelTestBase.tvFloat));
	}

	protected void testHasSubjectR( final StmtIterator it )
	{
		while (it.hasNext())
		{
			Assert.assertEquals(r, it.nextStatement().getSubject());
		}
	}

	public void testInt()
	{
		Assert.assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvInt));
	}

	public void testLiteral()
	{
		Assert.assertTrue(r.hasProperty(RDF.value, tvLiteral));
	}

	public void testLong()
	{
		Assert.assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvLong));
	}

	public void testNoSuchPropertyException()
	{
		try
		{
			r.getRequiredProperty(RDF.type);
			Assert.fail("missing property should throw exception");
		}
		catch (final PropertyNotFoundException e)
		{
			JenaTestBase.pass();
		}
	}

	public void testNoSuchPropertyNull()
	{
		Assert.assertNull(r.getProperty(RDF.type));
	}

	public void testObject()
	{
		Assert.assertTrue(r.hasLiteral(RDF.value,
				AbstractModelTestBase.tvObject));
	}

	public void testRemoveProperties()
	{
		r.removeProperties();
		Assert.assertEquals(false, model
				.listStatements(r, null, (RDFNode) null).hasNext());
	}

	public void testResource()
	{
		Assert.assertTrue(r.hasProperty(RDF.value, tvResource));
	}

	public void testShort()
	{
		Assert.assertTrue(r
				.hasLiteral(RDF.value, AbstractModelTestBase.tvShort));
	}

	public void testString()
	{
		Assert.assertTrue(r.hasProperty(RDF.value,
				AbstractModelTestBase.tvString));
	}

	public void testStringWithLanguage()
	{
		Assert.assertTrue(r.hasProperty(RDF.value,
				AbstractModelTestBase.tvString, lang));
	}
}
