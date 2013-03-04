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

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.junit.Assert;

public class TestSimpleSelector extends AbstractModelTestBase
{

	public TestSimpleSelector( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		model.createResource().addProperty(RDF.type, RDFS.Resource)
				.addProperty(RDFS.label, "foo").addProperty(RDF.value, "123");
		model.createResource().addProperty(RDF.type, RDFS.Resource)
				.addProperty(RDFS.label, "bar").addProperty(RDF.value, "123");

	}

	public void testAll()
	{
		final StmtIterator iter = model.listStatements(new SimpleSelector(null,
				null, (RDFNode) null));
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			iter.next();
		}
		Assert.assertEquals(6, i);
	}

	public void testFindObject()
	{
		final StmtIterator iter = model.listStatements(new SimpleSelector(null,
				null, RDFS.Resource));
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			final Statement stmt = iter.nextStatement();
			Assert.assertEquals(RDFS.Resource, stmt.getObject());
		}
		Assert.assertEquals(2, i);
	}

	public void testFindProperty()
	{
		final StmtIterator iter = model.listStatements(new SimpleSelector(null,
				RDFS.label, (RDFNode) null));
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			final Statement stmt = iter.nextStatement();
			Assert.assertEquals(RDFS.label, stmt.getPredicate());
		}
		Assert.assertEquals(2, i);
	}

	public void testFindPropertyAndObject()
	{
		final StmtIterator iter = model.listStatements(new SimpleSelector(null,
				RDF.value, 123));
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			final Statement stmt = iter.nextStatement();
			Assert.assertEquals(RDF.value, stmt.getPredicate());
			Assert.assertEquals(123, stmt.getInt());
		}
		Assert.assertEquals(2, i);
	}

	public void testFindSubject()
	{
		StmtIterator iter = model.listStatements(new SimpleSelector(null, null,
				RDFS.Resource));
		Assert.assertTrue(iter.hasNext());
		final Resource subject = iter.nextStatement().getSubject();
		iter = model.listStatements(new SimpleSelector(subject, null,
				(RDFNode) null));
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			final Statement stmt = iter.nextStatement();
			Assert.assertEquals(subject, stmt.getSubject());
		}
		Assert.assertEquals(3, i);
	}

	/**
	 * A plain SimpleSelector must be simple.
	 */
	public void testSimpleIsSimple()
	{
		Assert.assertTrue(new SimpleSelector(null, null, (RDFNode) null)
				.isSimple());
	}

	/**
	 * A random sub-class of SimpleSelector must not be simple.
	 */
	public void testSimpleSubclassIsntSimple()
	{
		Assert.assertFalse(new SimpleSelector(null, null, (RDFNode) null) {
		}.isSimple());
	}

}
