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
import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

public class TestResources extends AbstractModelTestBase
{
	public TestResources( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	protected void checkNumericContent( final Container cont2, final int num )
	{
		final NodeIterator nit = cont2.iterator();
		for (int i = 0; i < num; i += 1)
		{
			Assert.assertEquals(i, ((Literal) nit.nextNode()).getInt());
		}
		Assert.assertFalse(nit.hasNext());
	}

	protected void retainOnlySpecified( final Container cont2, final int num,
			final boolean[] retain )
	{
		final NodeIterator nit = cont2.iterator();
		for (int i = 0; i < num; i++)
		{
			nit.nextNode();
			if (retain[i] == false)
			{
				nit.remove();
			}
		}
		Assert.assertFalse(nit.hasNext());
	}

	protected void seeWhatsThere( final Container cont2, final boolean[] found )
	{
		final NodeIterator nit = cont2.iterator();
		while (nit.hasNext())
		{
			final int v = ((Literal) nit.nextNode()).getInt();
			Assert.assertFalse(found[v]);
			found[v] = true;
		}
	}

	protected Set<Object> setOf( final Object x )
	{
		final Set<Object> result = new HashSet<>();
		result.add(x);
		return result;
	}

	private void testContainer( final Model model, final Container cont1,
			final Container cont2 )
	{
		final Literal tvLiteral = model.createLiteral("test 12 string 2");
		// Resource tvResObj = model.createResource( new ResTestObjF() );
		final Object tvLitObj = new LitTestObj(1234);
		model.createBag();
		model.createAlt();
		model.createSeq();
		final String lang = "en";
		//
		Assert.assertEquals(0, cont1.size());
		Assert.assertEquals(0, cont2.size());
		//
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvBoolean).contains(
				AbstractModelTestBase.tvBoolean));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvByte).contains(
				AbstractModelTestBase.tvByte));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvShort).contains(
				AbstractModelTestBase.tvShort));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvInt).contains(
				AbstractModelTestBase.tvInt));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvLong).contains(
				AbstractModelTestBase.tvLong));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvFloat).contains(
				AbstractModelTestBase.tvFloat));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvDouble).contains(
				AbstractModelTestBase.tvDouble));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvChar).contains(
				AbstractModelTestBase.tvChar));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvString).contains(
				AbstractModelTestBase.tvString));
		Assert.assertFalse(cont1.contains(AbstractModelTestBase.tvString, lang));
		Assert.assertTrue(cont1.add(AbstractModelTestBase.tvString, lang)
				.contains(AbstractModelTestBase.tvString, lang));
		Assert.assertTrue(cont1.add(tvLiteral).contains(tvLiteral));
		// assertTrue( cont1.add( tvResObj ).contains( tvResObj ) );
		Assert.assertTrue(cont1.add(tvLitObj).contains(tvLitObj));
		Assert.assertEquals(12, cont1.size());
		//
		final int num = 10;
		for (int i = 0; i < num; i += 1)
		{
			cont2.add(i);
		}
		Assert.assertEquals(num, cont2.size());
		checkNumericContent(cont2, num);
		//
		final boolean[] found = new boolean[num];
		final boolean[] retain = { true, true, true, false, false, false,
				false, false, true, true };
		retainOnlySpecified(cont2, num, retain);
		seeWhatsThere(cont2, found);
		for (int i = 0; i < num; i += 1)
		{
			Assert.assertEquals(i + "th element of array", retain[i], found[i]);
		}
	}

	// public void testCreateAnonByFactory()
	// {
	// assertTrue( model.createResource( new ResTestObjF() ).isAnon() );
	// }

	// public void testCreateResourceByFactory()
	// {
	// String uri = "http://aldabaran.hpl.hp.com/foo";
	// assertEquals( uri, model.createResource( uri, new ResTestObjF()
	// ).getURI() );
	// }

	public void testCreateAnonResource()
	{
		final Resource r = model.createResource();
		Assert.assertTrue(r.isAnon());
		Assert.assertNull(r.getURI());
		Assert.assertNull(r.getNameSpace());
		Assert.assertNull(r.getLocalName());
	}

	public void testCreateAnonResourceWithNull()
	{
		final Resource r = model.createResource((String) null);
		Assert.assertTrue(r.isAnon());
		Assert.assertNull(r.getURI());
		Assert.assertNull(r.getNameSpace());
		Assert.assertNull(r.getLocalName());
	}

	public void testCreateNamedResource()
	{
		final String uri = "http://aldabaran.hpl.hp.com/foo";
		Assert.assertEquals(uri, model.createResource(uri).getURI());
	}

	public void testCreateNullPropertyFails()
	{
		try
		{
			model.createProperty(null);
			Assert.fail("should not create null property");
		}
		catch (final InvalidPropertyURIException e)
		{
			JenaTestBase.pass();
		}
	}

	public void testCreatePropertyOneArg()
	{
		final Property p = model.createProperty("abc/def");
		Assert.assertEquals("abc/", p.getNameSpace());
		Assert.assertEquals("def", p.getLocalName());
		Assert.assertEquals("abc/def", p.getURI());
	}

	public void testCreatePropertyStrangeURI()
	{
		final String uri = RDF.getURI() + "_345";
		final Property p = model.createProperty(uri);
		Assert.assertEquals(RDF.getURI(), p.getNameSpace());
		Assert.assertEquals("_345", p.getLocalName());
		Assert.assertEquals(uri, p.getURI());
	}

	public void testCreatePropertyStrangeURITwoArgs()
	{
		final String local = "_345";
		final Property p = model.createProperty(RDF.getURI(), local);
		Assert.assertEquals(RDF.getURI(), p.getNameSpace());
		Assert.assertEquals(local, p.getLocalName());
		Assert.assertEquals(RDF.getURI() + local, p.getURI());
	}

	public void testCreatePropertyTwoArgs()
	{
		final Property p = model.createProperty("abc/", "def");
		Assert.assertEquals("abc/", p.getNameSpace());
		Assert.assertEquals("def", p.getLocalName());
		Assert.assertEquals("abc/def", p.getURI());
	}

	public void testCreateTypedAnonResource()
	{
		final Resource r = model.createResource(RDF.Property);
		Assert.assertTrue(r.isAnon());
		Assert.assertTrue(model.contains(r, RDF.type, RDF.Property));
	}

	public void testCreateTypedNamedresource()
	{
		final String uri = "http://aldabaran.hpl.hp.com/foo";
		final Resource r = model.createResource(uri, RDF.Property);
		Assert.assertEquals(uri, r.getURI());
		Assert.assertTrue(model.contains(r, RDF.type, RDF.Property));
	}

	public void testEnhancedResources()
	{
		final Resource r = model.createResource();
		testResource(model, r, 0);

		testResource(model, model.createBag(), 1);
		testContainer(model, model.createBag(), model.createBag());

		testResource(model, model.createAlt(), 1);
		testContainer(model, model.createAlt(), model.createAlt());

		testResource(model, model.createSeq(), 1);
		testContainer(model, model.createSeq(), model.createSeq());
		// testSeq( model, model.createSeq(), model.createSeq(),
		// model.createSeq(),
		// model.createSeq(), model.createSeq(), model.createSeq(),
		// model.createSeq() );
	}

	private void testResource( final Model model, final Resource r,
			final int numProps )
	{
		final Literal tvLiteral = model.createLiteral("test 12 string 2");
		final Resource tvResource = model.createResource();
		final String lang = "fr";
		//
		Assert.assertTrue(r.addLiteral(RDF.value,
				AbstractModelTestBase.tvBoolean).hasLiteral(RDF.value,
				AbstractModelTestBase.tvBoolean));
		Assert.assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvByte)
				.hasLiteral(RDF.value, AbstractModelTestBase.tvByte));
		Assert.assertTrue(r
				.addLiteral(RDF.value, AbstractModelTestBase.tvShort)
				.hasLiteral(RDF.value, AbstractModelTestBase.tvShort));
		Assert.assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvInt)
				.hasLiteral(RDF.value, AbstractModelTestBase.tvInt));
		Assert.assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvLong)
				.hasLiteral(RDF.value, AbstractModelTestBase.tvLong));
		Assert.assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvChar)
				.hasLiteral(RDF.value, AbstractModelTestBase.tvChar));
		Assert.assertTrue(r
				.addLiteral(RDF.value, AbstractModelTestBase.tvFloat)
				.hasLiteral(RDF.value, AbstractModelTestBase.tvFloat));
		Assert.assertTrue(r.addLiteral(RDF.value,
				AbstractModelTestBase.tvDouble).hasLiteral(RDF.value,
				AbstractModelTestBase.tvDouble));
		Assert.assertTrue(r.addProperty(RDF.value,
				AbstractModelTestBase.tvString).hasProperty(RDF.value,
				AbstractModelTestBase.tvString));
		Assert.assertTrue(r.addProperty(RDF.value,
				AbstractModelTestBase.tvString, lang).hasProperty(RDF.value,
				AbstractModelTestBase.tvString, lang));
		Assert.assertTrue(r.addLiteral(RDF.value,
				AbstractModelTestBase.tvObject).hasLiteral(RDF.value,
				AbstractModelTestBase.tvObject));
		Assert.assertTrue(r.addProperty(RDF.value, tvLiteral).hasProperty(
				RDF.value, tvLiteral));
		Assert.assertTrue(r.addProperty(RDF.value, tvResource).hasProperty(
				RDF.value, tvResource));
		Assert.assertTrue(r.getRequiredProperty(RDF.value).getSubject()
				.equals(r));
		//
		final Property p = model.createProperty("foo/", "bar");
		try
		{
			r.getRequiredProperty(p);
			Assert.fail("should detect missing property");
		}
		catch (final PropertyNotFoundException e)
		{
			JenaTestBase.pass();
		}
		//
		Assert.assertEquals(13,
				GraphTestBase.iteratorToSet(r.listProperties(RDF.value)).size());
		Assert.assertEquals(setOf(r), GraphTestBase.iteratorToSet(r
				.listProperties(RDF.value).mapWith(Statement.Util.getSubject)));
		//
		Assert.assertEquals(0, GraphTestBase.iteratorToSet(r.listProperties(p))
				.size());
		Assert.assertEquals(
				new HashSet<Resource>(),
				GraphTestBase.iteratorToSet(r.listProperties(p).mapWith(
						Statement.Util.getSubject)));
		//
		Assert.assertEquals(13 + numProps,
				GraphTestBase.iteratorToSet(r.listProperties()).size());
		Assert.assertEquals(
				setOf(r),
				GraphTestBase.iteratorToSet(r.listProperties().mapWith(
						Statement.Util.getSubject)));
		//
		r.removeProperties();
		Assert.assertEquals(0,
				model.query(new SimpleSelector(r, null, (RDFNode) null)).size());
	}
}
