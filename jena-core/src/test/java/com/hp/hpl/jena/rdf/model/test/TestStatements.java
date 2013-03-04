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

import com.hp.hpl.jena.graph.FrontsTriple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

public class TestStatements extends AbstractModelTestBase
{
	public TestStatements( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void testHasWellFormedXML()
	{
		Assert.assertFalse(ModelHelper.statement("s P 1").hasWellFormedXML());
		Assert.assertFalse(ModelHelper.statement("S P '<x>/x>'rdf:XMLLiteral")
				.hasWellFormedXML());
		Assert.assertTrue(ModelHelper.statement("S P '<x></x>'rdf:XMLLiteral")
				.hasWellFormedXML());
	}

	public void testOtherStuff()
	{
		final Model A = createModel();
		final Model B = createModel();
		final Resource S = A.createResource("jena:S");
		final Resource R = A.createResource("jena:R");
		final Property P = A.createProperty("jena:P");
		final RDFNode O = A.createResource("jena:O");
		A.add(S, P, O);
		B.add(S, P, O);
		Assert.assertTrue("X1", A.isIsomorphicWith(B));
		/* */
		A.add(R, RDF.subject, S);
		B.add(R, RDF.predicate, P);
		Assert.assertFalse("X2", A.isIsomorphicWith(B));
		/* */
		A.add(R, RDF.predicate, P);
		B.add(R, RDF.subject, S);
		Assert.assertTrue("X3", A.isIsomorphicWith(B));
		/* */
		A.add(R, RDF.object, O);
		B.add(R, RDF.type, RDF.Statement);
		Assert.assertFalse("X4", A.isIsomorphicWith(B));
		/* */
		A.add(R, RDF.type, RDF.Statement);
		B.add(R, RDF.object, O);
		Assert.assertTrue("X5", A.isIsomorphicWith(B));
	}

	public void testPortingBlankNodes()
	{
		final Model B = createModel();
		final Resource anon = model.createResource();
		final Resource bAnon = anon.inModel(B);
		Assert.assertTrue("moved resource should still be blank",
				bAnon.isAnon());
		Assert.assertEquals("move resource should equal original", anon, bAnon);
	}

	public void testSet()
	{
		final Model A = createModel();
		createModel();
		final Resource S = A.createResource("jena:S");
		A.createResource("jena:R");
		final Property P = A.createProperty("jena:P");
		final RDFNode O = A.createResource("jena:O");
		final Statement spo = A.createStatement(S, P, O);
		A.add(spo);
		final Statement sps = A.createStatement(S, P, S);
		Assert.assertEquals(sps, spo.changeObject(S));
		Assert.assertFalse(A.contains(spo));
		Assert.assertTrue(A.contains(sps));
	}

	/**
	 * Feeble test that toString'ing a Statement[Impl] will display the
	 * data-type
	 * of its object if it has one.
	 */
	public void testStatementPrintsType()
	{
		final String fakeURI = "fake:URI";
		final Resource S = model.createResource();
		final Property P = ModelHelper.property(model, "PP");
		final RDFNode O = model.createTypedLiteral("42", fakeURI);
		final Statement st = model.createStatement(S, P, O);
		Assert.assertTrue(st.toString().indexOf(fakeURI) > 0);
	}

	public void testStatmentMap1Selectors()
	{
		final Statement s = ModelHelper.statement("sub pred obj");
		Assert.assertEquals(ModelHelper.resource("sub"),
				Statement.Util.getSubject.map1(s));
		Assert.assertEquals(ModelHelper.resource("pred"),
				Statement.Util.getPredicate.map1(s));
		Assert.assertEquals(ModelHelper.resource("obj"),
				Statement.Util.getObject.map1(s));
	}

	/**
	 * A resource created in one
	 * model and incorporated into a statement asserted constructed by a
	 * different model should test equal to the resource extracted from that
	 * statement, even if it's a bnode.
	 */
	public void testStuff()
	{
		final Model red = createModel();
		final Model blue = createModel();
		final Resource r = red.createResource();
		final Property p = red.createProperty("");
		final Statement s = blue.createStatement(r, p, r);
		Assert.assertEquals("subject preserved", r, s.getSubject());
		Assert.assertEquals("object preserved", r, s.getObject());
	}

	public void testTripleWrapper()
	{
		JenaTestBase.assertInstanceOf(FrontsTriple.class,
				ModelHelper.statement(model, "s p o"));
	}
}
