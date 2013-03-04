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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.List;

import org.junit.Assert;

public class TestSimpleListStatements extends AbstractModelTestBase
{

	static boolean booleanValue = true;

	static char charValue = 'c';
	static long longValue = 456;
	static float floatValue = 5.67F;
	static double doubleValue = 6.78;
	static String stringValue = "stringValue";
	static String langValue = "en";

	public TestSimpleListStatements( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void checkReturns( final String things, final StmtIterator it )
	{
		final Model wanted = ModelHelper.modelWithStatements(this, things);
		final Model got = modelWithStatements(it);
		if (wanted.isIsomorphicWith(got) == false)
		{
			Assert.fail("wanted " + wanted + " got " + got);
		}
	}

	public Model modelWithStatements( final StmtIterator it )
	{
		final Model m = createModel();
		while (it.hasNext())
		{
			m.add(it.nextStatement());
		}
		return m;
	}

	@Override
	public void setUp() throws Exception
	{

		super.setUp();
		model.createResource("http://example.org/boolean").addLiteral(
				RDF.value, TestSimpleListStatements.booleanValue);
		model.createResource("http://example.org/char").addLiteral(RDF.value,
				TestSimpleListStatements.charValue);
		model.createResource("http://example.org/long").addLiteral(RDF.value,
				TestSimpleListStatements.longValue);
		model.createResource("http://example.org/float").addLiteral(RDF.value,
				TestSimpleListStatements.floatValue);
		model.createResource("http://example.org/double").addLiteral(RDF.value,
				TestSimpleListStatements.doubleValue);
		model.createResource("http://example.org/string").addProperty(
				RDF.value, TestSimpleListStatements.stringValue);
		model.createResource("http://example.org/langString").addProperty(
				RDF.value, TestSimpleListStatements.stringValue,
				TestSimpleListStatements.langValue);

	}

	public void testAll()
	{
		final StmtIterator iter = model.listStatements(null, null,
				(RDFNode) null);
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			iter.next();
		}
		Assert.assertEquals(7, i);
	}

	public void testAllString()
	{
		final StmtIterator iter = model.listStatements(null, null,
				(String) null);
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			iter.next();
		}
		Assert.assertEquals(7, i);
	}

	public void testBoolean()
	{
		final List<Statement> got = model.listLiteralStatements(null, null,
				TestSimpleListStatements.booleanValue).toList();
		Assert.assertEquals(1, got.size());
		final Statement it = got.get(0);
		Assert.assertEquals(ModelHelper.resource("http://example.org/boolean"),
				it.getSubject());
		Assert.assertEquals(
				model.createTypedLiteral(TestSimpleListStatements.booleanValue),
				it.getObject());
	}

	public void testChar()
	{
		final List<Statement> got = model.listLiteralStatements(null, null,
				TestSimpleListStatements.charValue).toList();
		Assert.assertEquals(1, got.size());
		final Statement it = got.get(0);
		Assert.assertEquals(ModelHelper.resource("http://example.org/char"),
				it.getSubject());
		Assert.assertEquals(
				model.createTypedLiteral(TestSimpleListStatements.charValue),
				it.getObject());
	}

	public void testDouble()
	{
		final List<Statement> got = model.listLiteralStatements(null, null,
				TestSimpleListStatements.doubleValue).toList();
		Assert.assertEquals(1, got.size());
		final Statement it = got.get(0);
		Assert.assertEquals(ModelHelper.resource("http://example.org/double"),
				it.getSubject());
		Assert.assertEquals(
				model.createTypedLiteral(TestSimpleListStatements.doubleValue),
				it.getObject());
	}

	public void testFloat()
	{
		final List<Statement> got = model.listLiteralStatements(null, null,
				TestSimpleListStatements.floatValue).toList();
		Assert.assertEquals(1, got.size());
		final Statement it = got.get(0);
		Assert.assertEquals(ModelHelper.resource("http://example.org/float"),
				it.getSubject());
		Assert.assertEquals(
				model.createTypedLiteral(TestSimpleListStatements.floatValue),
				it.getObject());
	}

	public void testLangString()
	{
		final StmtIterator iter = model.listStatements(null, null,
				TestSimpleListStatements.stringValue,
				TestSimpleListStatements.langValue);
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			Assert.assertEquals(iter.nextStatement().getSubject().getURI(),
					"http://example.org/langString");
		}
		Assert.assertEquals(1, i);
	}

	public void testListStatementsClever()
	{

		ModelHelper.modelAdd(model, "S P O; S P O2; S P2 O; S2 P O");
		final Selector sel = new SimpleSelector(null, null, (RDFNode) null) {
			@Override
			public boolean isSimple()
			{
				return false;
			}

			@Override
			public boolean test( final Statement st )
			{
				return (st.getSubject().toString().length()
						+ st.getPredicate().toString().length() + st
						.getObject().toString().length()) == 15; /*
																 * eh:/S + eh:/P
																 * + eh:/O
																 */
			}
		};
		checkReturns("S P O", model.listStatements(sel));
	}

	public void testListStatementsSPO()
	{

		final Resource A = ModelHelper.resource(model, "A"), X = ModelHelper
				.resource(model, "X");
		final Property P = ModelHelper.property(model, "P"), P1 = ModelHelper
				.property(model, "P1");
		final RDFNode O = ModelHelper.resource(model, "O"), Y = ModelHelper
				.resource(model, "Y");
		final String S1 = "S P O; S1 P O; S2 P O";
		final String S2 = "A P1 B; A P1 B; A P1 C";
		final String S3 = "X P1 Y; X P2 Y; X P3 Y";
		ModelHelper.modelAdd(model, S1);
		ModelHelper.modelAdd(model, S2);
		ModelHelper.modelAdd(model, S3);
		checkReturns(S1, model.listStatements(null, P, O));
		checkReturns(S2, model.listStatements(A, P1, (RDFNode) null));
		checkReturns(S3, model.listStatements(X, null, Y));
	}

	public void testLong()
	{
		final List<Statement> got = model.listLiteralStatements(null, null,
				TestSimpleListStatements.longValue).toList();
		Assert.assertEquals(1, got.size());
		final Statement it = got.get(0);
		Assert.assertEquals(ModelHelper.resource("http://example.org/long"),
				it.getSubject());
		Assert.assertEquals(
				model.createTypedLiteral(TestSimpleListStatements.longValue),
				it.getObject());
	}

	public void testString()
	{
		final StmtIterator iter = model.listStatements(null, null,
				TestSimpleListStatements.stringValue);
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			Assert.assertEquals(iter.nextStatement().getSubject().getURI(),
					"http://example.org/string");
		}
		Assert.assertEquals(1, i);
	}
}
