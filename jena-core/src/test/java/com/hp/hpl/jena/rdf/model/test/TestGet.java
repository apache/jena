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

import com.hp.hpl.jena.rdf.model.Alt;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

public class TestGet extends AbstractModelTestBase
{

	protected Resource S;
	protected Property P;

	public TestGet( final TestingModelFactory modelFactory, final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		S = model.createResource("http://nowhere.man/subject");
		P = model.createProperty("http://nowhere.man/predicate");
	}

	@Override
	public void tearDown() throws Exception
	{
		S = null;
		P = null;
		super.tearDown();
	}

	public void testGetAlt()
	{
		final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 160;
		model.createAlt(uri);
		final Alt a = model.getAlt(uri);
		Assert.assertEquals(uri, a.getURI());
		Assert.assertTrue(model.contains(a, RDF.type, RDF.Alt));
	}

	// public void testGetResourceFactory()
	// {
	// String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 120;
	// Resource r = model.getResource( uri, new ResTestObjF() );
	// assertEquals( uri, r.getURI() );
	// }

	public void testGetBag()
	{
		final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 150;
		model.createBag(uri);
		final Bag b = model.getBag(uri);
		Assert.assertEquals(uri, b.getURI());
		Assert.assertTrue(model.contains(b, RDF.type, RDF.Bag));
	}

	public void testGetPropertyOneArg()
	{
		final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 130;
		final Property p = model.getProperty(uri);
		Assert.assertEquals(uri, p.getURI());
	}

	public void testGetPropertyTwoArgs()
	{
		final String ns = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 140 + "/";
		final Property p = model.getProperty(ns, "foo");
		Assert.assertEquals(ns + "foo", p.getURI());
	}

	public void testGetResource()
	{
		final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 110;
		final Resource r = model.getResource(uri);
		Assert.assertEquals(uri, r.getURI());
	}

	public void testGetSeq()
	{
		final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 170;
		model.createSeq(uri);
		final Seq s = model.getSeq(uri);
		Assert.assertEquals(uri, s.getURI());
		Assert.assertTrue(model.contains(s, RDF.type, RDF.Seq));
	}
}
