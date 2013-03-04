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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.junit.Assert;

public class TestStatementCreation extends AbstractModelTestBase
{

	static final String subjURI = "http://aldabaran.hpl.hp.com/foo";
	static final String predURI = "http://aldabaran.hpl.hp.com/bar";

	protected Resource r;
	protected Property p;

	public TestStatementCreation( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		r = model.createResource(TestStatementCreation.subjURI);
		p = model.createProperty(TestStatementCreation.predURI);
	}

	@Override
	public void tearDown() throws Exception
	{
		r = null;
		p = null;
		super.tearDown();
	}

	public void testCreateStatementByteMax()
	{
		final Statement s = model.createLiteralStatement(r, p, Byte.MAX_VALUE);
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(Byte.MAX_VALUE, s.getByte());
	}

	public void testCreateStatementChar()
	{
		final Statement s = model.createLiteralStatement(r, p, '$');
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals('$', s.getChar());
	}

	public void testCreateStatementDouble()
	{
		final Statement s = model.createStatement(r, p,
				model.createTypedLiteral(12345.67890d));
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(12345.67890d, s.getDouble(), 0.0000005);
	}

	public void testCreateStatementFactory()
	{
		final LitTestObj tv = new LitTestObj(Long.MIN_VALUE);
		final Statement s = model.createLiteralStatement(r, p, tv);
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		// assertEquals( tv, s.getObject( new LitTestObjF() ) );
	}

	public void testCreateStatementFloat()
	{
		final Statement s = model.createStatement(r, p,
				model.createTypedLiteral(123.456f));
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(123.456f, s.getFloat(), 0.0005);
	}

	public void testCreateStatementIntMax()
	{
		final Statement s = model.createLiteralStatement(r, p,
				Integer.MAX_VALUE);
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(Integer.MAX_VALUE, s.getInt());
	}

	public void testCreateStatementLongMax()
	{
		final Statement s = model.createLiteralStatement(r, p, Long.MAX_VALUE);
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(Long.MAX_VALUE, s.getLong());
	}

	public void testCreateStatementResource()
	{
		final Resource tv = model.createResource();
		final Statement s = model.createStatement(r, p, tv);
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(tv, s.getResource());
	}

	public void testCreateStatementShortMax()
	{
		final Statement s = model.createLiteralStatement(r, p, Short.MAX_VALUE);
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(Short.MAX_VALUE, s.getShort());
	}

	public void testCreateStatementString()
	{
		final String string = "this is a plain string", lang = "en";
		final Statement s = model.createStatement(r, p, string);
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(string, s.getString());
		Assert.assertEquals(lang, model.createStatement(r, p, string, lang)
				.getLanguage());
	}

	public void testCreateStatementTrue()
	{
		final Statement s = model.createLiteralStatement(r, p, true);
		Assert.assertEquals(r, s.getSubject());
		Assert.assertEquals(p, s.getPredicate());
		Assert.assertEquals(true, s.getBoolean());
	}

	public void testCreateStatementTypeLiteral()
	{
		final Model model = ModelFactory.createDefaultModel();
		final Resource R = model.createResource("http://example/r");
		final Property P = model.createProperty("http://example/p");
		model.add(R, P, "2", XSDDatatype.XSDinteger);
		final Literal L = ResourceFactory.createTypedLiteral("2",
				XSDDatatype.XSDinteger);
		Assert.assertTrue(model.contains(R, P, L));
		Assert.assertFalse(model.contains(R, P, "2"));
	}

	public void testNotReified()
	{
		Statement s1 = null;
		Statement s2 = null;
		s1 = model
				.createStatement(model.createResource(), RDF.type, RDFS.Class);
		Assert.assertFalse("Should not be reified", s1.isReified());
		model.add(s1);
		Assert.assertFalse("Should not be reified", s1.isReified());

		s2 = model
				.createStatement(model.createResource(), RDF.type, RDFS.Class);
		Assert.assertFalse("Should not be reified", s2.isReified());
		model.add(s2);
		Assert.assertFalse("Should not be reified", s2.isReified());
	}
}
