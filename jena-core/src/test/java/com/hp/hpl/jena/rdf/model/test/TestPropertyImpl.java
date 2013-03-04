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

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.junit.Assert;
import junit.framework.TestCase;

public class TestPropertyImpl extends TestCase
{
	public TestPropertyImpl( final String name )
	{
		super(name);
	}

	protected Property createProperty( final String uri )
	{
		return new PropertyImpl(uri);
	}

	public void testNonOrdinalRDFURIs()
	{
		testRDFOrdinalValue(0, "x");
		testRDFOrdinalValue(0, "x1");
		testRDFOrdinalValue(0, "_x");
		testRDFOrdinalValue(0, "x123");
		testRDFOrdinalValue(0, "0xff");
		testRDFOrdinalValue(0, "_xff");
	}

	public void testNonRDFElementURIsHaveOrdinal0()
	{
		testOrdinalValue(0, "foo:bar");
		testOrdinalValue(0, "foo:bar1");
		testOrdinalValue(0, "foo:bar2");
		testOrdinalValue(0, RDFS.getURI() + "_17");
	}

	private void testOrdinalValue( final int i, final String URI )
	{
		final String message = "property should have expected ordinal value for "
				+ URI;
		Assert.assertEquals(message, i, createProperty(URI).getOrdinal());
	}

	public void testOrdinalValues()
	{
		testRDFOrdinalValue(1, "_1");
		testRDFOrdinalValue(2, "_2");
		testRDFOrdinalValue(3, "_3");
		testRDFOrdinalValue(4, "_4");
		testRDFOrdinalValue(5, "_5");
		testRDFOrdinalValue(6, "_6");
		testRDFOrdinalValue(7, "_7");
		testRDFOrdinalValue(8, "_8");
		testRDFOrdinalValue(9, "_9");
		testRDFOrdinalValue(10, "_10");
		testRDFOrdinalValue(100, "_100");
		testRDFOrdinalValue(1234, "_1234");
		testRDFOrdinalValue(67890, "_67890");
	}

	private void testRDFOrdinalValue( final int i, final String local )
	{
		testOrdinalValue(i, RDF.getURI() + local);
	}
}
