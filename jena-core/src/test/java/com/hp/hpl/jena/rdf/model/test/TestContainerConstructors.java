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
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

public class TestContainerConstructors extends AbstractModelTestBase
{
	public TestContainerConstructors( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void testCreateAnonAlt()
	{
		final Alt tv = model.createAlt();
		Assert.assertTrue(tv.isAnon());
		Assert.assertTrue(model.contains(tv, RDF.type, RDF.Alt));
	}

	public void testCreateAnonBag()
	{
		final Bag tv = model.createBag();
		Assert.assertTrue(tv.isAnon());
		Assert.assertTrue(model.contains(tv, RDF.type, RDF.Bag));
	}

	public void testCreateAnonSeq()
	{
		final Seq tv = model.createSeq();
		Assert.assertTrue(tv.isAnon());
		Assert.assertTrue(model.contains(tv, RDF.type, RDF.Seq));
	}

	public void testCreateNamedAlt()
	{
		final String uri = "http://aldabaran/sirius";
		final Alt tv = model.createAlt(uri);
		Assert.assertEquals(uri, tv.getURI());
		Assert.assertTrue(model.contains(tv, RDF.type, RDF.Alt));
	}

	public void testCreateNamedBag()
	{
		final String uri = "http://aldabaran/foo";
		final Bag tv = model.createBag(uri);
		Assert.assertEquals(uri, tv.getURI());
		Assert.assertTrue(model.contains(tv, RDF.type, RDF.Bag));
	}

	public void testCreateNamedSeq()
	{
		final String uri = "http://aldabaran/andromeda";
		final Seq tv = model.createSeq(uri);
		Assert.assertEquals(uri, tv.getURI());
		Assert.assertTrue(model.contains(tv, RDF.type, RDF.Seq));
	}
}
