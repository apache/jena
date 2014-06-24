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

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestResourceFactory extends TestCase
{

	class TestFactory implements ResourceFactory.Interface
	{

		Resource resource;

		TestFactory( final Resource r )
		{
			resource = r;
		}

		@Override
		public Literal createLangLiteral( final String string, final String lang )
		{
			return null;
		}

		@Override
		public Literal createPlainLiteral( final String string )
		{
			return null;
		}

		@Override
		public Property createProperty( final String uriref )
		{
			return null;
		}

		@Override
		public Property createProperty( final String namespace,
				final String localName )
		{
			return null;
		}

		@Override
		public Resource createResource()
		{
			return resource;
		}

		@Override
		public Resource createResource( final String uriref )
		{
			return null;
		}

		@Override
		public Statement createStatement( final Resource subject,
				final Property predicate, final RDFNode object )
		{
			return null;
		}

		@Override
		public Literal createTypedLiteral( final Object value )
		{
			return null;
		}

		@Override
		public Literal createTypedLiteral( final String string,
				final RDFDatatype datatype )
		{
			return null;
		}

	}

	static final String uri1 = "http://example.org/example#a1";

	static final String uri2 = "http://example.org/example#a2";

	public static TestSuite suite()
	{
		return new TestSuite(TestResourceFactory.class);
	}

	public TestResourceFactory( final String name )
	{
		super(name);
	}

	public void testCreateLiteral()
	{
		final Literal l = ResourceFactory.createPlainLiteral("lex");
		Assert.assertTrue(l.getLexicalForm().equals("lex"));
		Assert.assertTrue(l.getLanguage().equals(""));
		Assert.assertNull(l.getDatatype());
		Assert.assertNull(l.getDatatypeURI());
	}

	public void testCreateProperty()
	{
		final Property p1 = ResourceFactory
				.createProperty(TestResourceFactory.uri1);
		Assert.assertTrue(p1.getURI().equals(TestResourceFactory.uri1));
		final Property p2 = ResourceFactory.createProperty(
				TestResourceFactory.uri1, "2");
		Assert.assertTrue(p2.getURI().equals(TestResourceFactory.uri1 + "2"));
	}

	public void testCreateResource()
	{
		Resource r1 = ResourceFactory.createResource();
		Assert.assertTrue(r1.isAnon());
		final Resource r2 = ResourceFactory.createResource();
		Assert.assertTrue(r2.isAnon());
		Assert.assertTrue(!r1.equals(r2));

		r1 = ResourceFactory.createResource(TestResourceFactory.uri1);
		Assert.assertTrue(r1.getURI().equals(TestResourceFactory.uri1));
	}

	public void testCreateStatement()
	{
		final Resource s = ResourceFactory.createResource();
		final Property p = ResourceFactory
				.createProperty(TestResourceFactory.uri2);
		final Resource o = ResourceFactory.createResource();
		final Statement stmt = ResourceFactory.createStatement(s, p, o);
		Assert.assertTrue(stmt.getSubject().equals(s));
		Assert.assertTrue(stmt.getPredicate().equals(p));
		Assert.assertTrue(stmt.getObject().equals(o));
	}

	public void testCreateTypedLiteral()
	{
		final Literal l = ResourceFactory.createTypedLiteral("22",
				XSDDatatype.XSDinteger);
		Assert.assertTrue(l.getLexicalForm().equals("22"));
		Assert.assertTrue(l.getLanguage().equals(""));
		Assert.assertTrue(l.getDatatype() == XSDDatatype.XSDinteger);
		Assert.assertTrue(l.getDatatypeURI().equals(
				XSDDatatype.XSDinteger.getURI()));

	}

	public void testCreateTypedLiteralObject()
	{
		final Literal l = ResourceFactory.createTypedLiteral( 22 );
		Assert.assertEquals("22", l.getLexicalForm());
		Assert.assertEquals("", l.getLanguage());
		Assert.assertEquals(XSDDatatype.XSDint, l.getDatatype());
	}

	public void testCreateTypedLiteralOverload()
	{
		final Calendar testCal = new GregorianCalendar(
				TimeZone.getTimeZone("GMT"));
		testCal.set(1999, 4, 30, 15, 9, 32);
		testCal.set(Calendar.MILLISECOND, 0); // ms field can be undefined on
		// Linux
		final Literal lc = ResourceFactory.createTypedLiteral(testCal);
		Assert.assertEquals("calendar overloading test", ResourceFactory
				.createTypedLiteral("1999-05-30T15:09:32Z",
						XSDDatatype.XSDdateTime), lc);

	}

	public void testGetInstance()
	{
		ResourceFactory.getInstance();
		final Resource r1 = ResourceFactory.createResource();
		Assert.assertTrue(r1.isAnon());
		final Resource r2 = ResourceFactory.createResource();
		Assert.assertTrue(r2.isAnon());
		Assert.assertTrue(!r1.equals(r2));
	}

	public void testSetInstance()
	{
		final Resource r = ResourceFactory.createResource();
		final ResourceFactory.Interface oldFactory = ResourceFactory
				.getInstance();
		final ResourceFactory.Interface factory = new TestFactory(r);
		try
		{
			ResourceFactory.setInstance(factory);
			Assert.assertTrue(factory.equals(ResourceFactory.getInstance()));
			Assert.assertTrue(ResourceFactory.createResource() == r);
		}
		finally
		{
			ResourceFactory.setInstance(oldFactory);
		}
	}
}
