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
import com.hp.hpl.jena.rdf.model.LiteralRequiredException;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceRequiredException;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

// import com.hp.hpl.jena.regression.Regression.*;

public class TestStatementMethods extends AbstractModelTestBase
{

	protected Resource r;

	public TestStatementMethods( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	protected void checkChangedStatementSP( final Statement changed )
	{
		Assert.assertEquals(r, changed.getSubject());
		Assert.assertEquals(RDF.value, changed.getPredicate());
	}

	protected void checkCorrectStatements( final Statement sTrue,
			final Statement changed )
	{
		Assert.assertFalse(model.contains(sTrue));
		Assert.assertFalse(model.containsLiteral(r, RDF.value, true));
		Assert.assertTrue(model.contains(changed));
	}

	protected Statement loadInitialStatement()
	{
		final Statement sTrue = model
				.createLiteralStatement(r, RDF.value, true);
		model.add(sTrue);
		return sTrue;
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		r = model.createResource();
	}

	public void testAlt()
	{
		final Alt tvAlt = model.createAlt();
		Assert.assertEquals(tvAlt, model.createStatement(r, RDF.value, tvAlt)
				.getAlt());
	}

	public void testBag()
	{
		final Bag tvBag = model.createBag();
		Assert.assertEquals(tvBag, model.createStatement(r, RDF.value, tvBag)
				.getBag());
	}

	public void testBoolean()
	{
		final Statement s = model.createLiteralStatement(r, RDF.value, true);
		Assert.assertEquals(model.createTypedLiteral(true), s.getObject());
		Assert.assertEquals(true, s.getBoolean());
	}

	public void testByte()
	{
		final Statement s = model.createLiteralStatement(r, RDF.value,
				AbstractModelTestBase.tvByte);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvByte),
				s.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvByte, s.getLong());
	}

	public void testChangeObjectBoolean()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement sFalse = sTrue.changeLiteralObject(false);
		checkChangedStatementSP(sFalse);
		Assert.assertEquals(model.createTypedLiteral(false), sFalse.getObject());
		Assert.assertEquals(false, sFalse.getBoolean());
		checkCorrectStatements(sTrue, sFalse);
		Assert.assertTrue(model.containsLiteral(r, RDF.value, false));
	}

	public void testChangeObjectByte()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeLiteralObject(AbstractModelTestBase.tvByte);
		checkChangedStatementSP(changed);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvByte),
				changed.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvByte, changed.getByte());
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.containsLiteral(r, RDF.value,
				AbstractModelTestBase.tvByte));
	}

	public void testChangeObjectChar()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeLiteralObject(AbstractModelTestBase.tvChar);
		checkChangedStatementSP(changed);
		Assert.assertEquals(AbstractModelTestBase.tvChar, changed.getChar());
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvChar),
				changed.getObject());
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.containsLiteral(r, RDF.value,
				AbstractModelTestBase.tvChar));
	}

	public void testChangeObjectDouble()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeLiteralObject(AbstractModelTestBase.tvDouble);
		checkChangedStatementSP(changed);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvDouble),
				changed.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvDouble,
				changed.getDouble(), AbstractModelTestBase.dDelta);
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.containsLiteral(r, RDF.value,
				AbstractModelTestBase.tvDouble));
	}

	public void testChangeObjectFloat()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeLiteralObject(AbstractModelTestBase.tvFloat);
		checkChangedStatementSP(changed);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvFloat),
				changed.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvFloat, changed.getFloat(),
				AbstractModelTestBase.fDelta);
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.containsLiteral(r, RDF.value,
				AbstractModelTestBase.tvFloat));
	}

	public void testChangeObjectInt()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeLiteralObject(AbstractModelTestBase.tvInt);
		checkChangedStatementSP(changed);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvInt),
				changed.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvInt, changed.getInt());
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.containsLiteral(r, RDF.value,
				AbstractModelTestBase.tvInt));
	}

	public void testChangeObjectLiteral()
	{
		final Statement sTrue = loadInitialStatement();
		model.remove(sTrue);
		Assert.assertFalse(model.contains(sTrue));
		Assert.assertFalse(model.containsLiteral(r, RDF.value, true));
	}

	// public void testResObj()
	// {
	// Resource tvResObj = model.createResource( new ResTestObjF() );
	// assertEquals( tvResObj, model.createStatement( r, RDF.value, tvResObj
	// ).getResource() );
	// }

	// public void testLitObj()
	// {
	// assertEquals( tvLitObj, model.createLiteralStatement( r, RDF.value,
	// tvLitObj ).getObject( new LitTestObjF() ) );
	// }

	public void testChangeObjectLong()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeLiteralObject(AbstractModelTestBase.tvLong);
		checkChangedStatementSP(changed);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvLong),
				changed.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvLong, changed.getLong());
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.containsLiteral(r, RDF.value,
				AbstractModelTestBase.tvLong));
	}

	public void testChangeObjectShort()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeLiteralObject(AbstractModelTestBase.tvShort);
		checkChangedStatementSP(changed);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvShort),
				changed.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvShort, changed.getShort());
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.containsLiteral(r, RDF.value,
				AbstractModelTestBase.tvShort));
	}

	public void testChangeObjectString()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeObject(AbstractModelTestBase.tvString);
		checkChangedStatementSP(changed);
		Assert.assertEquals(AbstractModelTestBase.tvString, changed.getString());
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.contains(r, RDF.value,
				AbstractModelTestBase.tvString));
	}

	public void testChangeObjectStringWithLanguage()
	{
		final String lang = "en";
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue.changeObject(
				AbstractModelTestBase.tvString, lang);
		checkChangedStatementSP(changed);
		Assert.assertEquals(AbstractModelTestBase.tvString, changed.getString());
		Assert.assertEquals(lang, changed.getLanguage());
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.contains(r, RDF.value,
				AbstractModelTestBase.tvString, lang));
	}

	public void testChangeObjectYByte()
	{
		final Statement sTrue = loadInitialStatement();
		final Statement changed = sTrue
				.changeLiteralObject(AbstractModelTestBase.tvByte);
		checkChangedStatementSP(changed);
		Assert.assertEquals(AbstractModelTestBase.tvByte, changed.getByte());
		checkCorrectStatements(sTrue, changed);
		Assert.assertTrue(model.containsLiteral(r, RDF.value,
				AbstractModelTestBase.tvByte));
	}

	public void testChar()
	{
		final Statement s = model.createLiteralStatement(r, RDF.value,
				AbstractModelTestBase.tvChar);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvChar),
				s.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvChar, s.getChar());
	}

	public void testDouble()
	{
		final Statement s = model.createLiteralStatement(r, RDF.value,
				AbstractModelTestBase.tvDouble);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvDouble),
				s.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvDouble, s.getDouble(),
				AbstractModelTestBase.dDelta);
	}

	public void testFloat()
	{
		final Statement s = model.createLiteralStatement(r, RDF.value,
				AbstractModelTestBase.tvFloat);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvFloat),
				s.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvFloat, s.getFloat(),
				AbstractModelTestBase.fDelta);
	}

	public void testGetLiteralFailure()
	{
		try
		{
			model.createStatement(r, RDF.value, r).getLiteral();
			Assert.fail("should trap non-literal object");
		}
		catch (final LiteralRequiredException e)
		{
			JenaTestBase.pass();
		}
	}

	public void testGetResource()
	{
		Assert.assertEquals(r, model.createStatement(r, RDF.value, r)
				.getResource());
	}

	public void testGetResourceFailure()
	{
		try
		{
			model.createLiteralStatement(r, RDF.value, false).getResource();
			Assert.fail("should trap non-resource object");
		}
		catch (final ResourceRequiredException e)
		{
			JenaTestBase.pass();
		}
	}

	public void testGetTrueBoolean()
	{
		Assert.assertEquals(true,
				model.createLiteralStatement(r, RDF.value, true).getLiteral()
						.getBoolean());
	}

	public void testInt()
	{
		final Statement s = model.createLiteralStatement(r, RDF.value,
				AbstractModelTestBase.tvInt);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvInt),
				s.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvInt, s.getInt());
	}

	// public void testChangeObjectResObject()
	// {
	// Resource tvResObj = model.createResource( new ResTestObjF() );
	// Statement sTrue = loadInitialStatement();
	// Statement changed = sTrue.changeObject( tvResObj );
	// checkChangedStatementSP( changed );
	// assertEquals( tvResObj, changed.getResource() );
	// checkCorrectStatements( sTrue, changed );
	// assertTrue( model.contains( r, RDF.value, tvResObj ) );
	// }

	public void testLong()
	{
		final Statement s = model.createLiteralStatement(r, RDF.value,
				AbstractModelTestBase.tvLong);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvLong),
				s.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvLong, s.getLong());
	}

	public void testSeq()
	{
		final Seq tvSeq = model.createSeq();
		Assert.assertEquals(tvSeq, model.createStatement(r, RDF.value, tvSeq)
				.getSeq());
	}

	public void testShort()
	{
		final Statement s = model.createLiteralStatement(r, RDF.value,
				AbstractModelTestBase.tvShort);
		Assert.assertEquals(
				model.createTypedLiteral(AbstractModelTestBase.tvShort),
				s.getObject());
		Assert.assertEquals(AbstractModelTestBase.tvShort, s.getShort());
	}

	public void testString()
	{
		Assert.assertEquals(AbstractModelTestBase.tvString, model
				.createStatement(r, RDF.value, AbstractModelTestBase.tvString)
				.getString());
	}

	public void testStringWithLanguage()
	{
		final String lang = "fr";
		Assert.assertEquals(
				AbstractModelTestBase.tvString,
				model.createStatement(r, RDF.value,
						AbstractModelTestBase.tvString, lang).getString());
		Assert.assertEquals(
				lang,
				model.createStatement(r, RDF.value,
						AbstractModelTestBase.tvString, lang).getLanguage());
	}
}
