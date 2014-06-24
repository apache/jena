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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import java.util.List;

import org.junit.Assert;

// import com.hp.hpl.jena.regression.Regression.*;

public class TestSelectorUse extends AbstractModelTestBase
{

	boolean tvBooleans[] = { false, true };
	long tvLongs[] = { 123, 321 };
	char tvChars[] = { '@', ';' };
	double tvDoubles[] = { 123.456, 456.123 };
	String tvStrings[] = { "testing string 1", "testing string 2" };
	String langs[] = { "en", "fr" };
	Literal tvLitObjs[];

	// Resource tvResObjs[] = { model.createResource(new ResTestObjF()),
	// model.createResource(new ResTestObjF()) };

	final int num = 2;
	Resource subject[] = new Resource[num];
	Property predicate[] = new Property[num];

	String suri = "http://aldabaran/test9/s";
	String puri = "http://aldabaran/test9/";

	public TestSelectorUse( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		final Literal tvLitObjs[] = {
				model.createTypedLiteral(new LitTestObj(1)),
				model.createTypedLiteral(new LitTestObj(2)) };

		for (int i = 0; i < num; i += 1)
		{
			subject[i] = model.createResource(suri + i);
			predicate[i] = model.createProperty(puri + i, "p");
		}

		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < num; j++)
			{
				model.addLiteral(subject[i], predicate[j], tvBooleans[j]);
				model.addLiteral(subject[i], predicate[j], tvLongs[j]);
				model.addLiteral(subject[i], predicate[j], tvChars[j]);
				model.addLiteral(subject[i], predicate[j], tvDoubles[j]);
				model.add(subject[i], predicate[j], tvStrings[j]);
				model.add(subject[i], predicate[j], tvStrings[j], langs[j]);
				model.add(subject[i], predicate[j], tvLitObjs[j]);
				// model.add( subject[i], predicate[j], tvResObjs[j] );
			}
		}

	}

	public void testListWithLiteralSelector()
	{

		final StmtIterator it6 = model.listStatements(new SimpleSelector(null,
				null, tvStrings[1], langs[1]));
		final List<Statement> L6 = GraphTestBase.iteratorToList(it6);
        for ( Statement aL6 : L6 )
        {
            Assert.assertEquals( langs[1], aL6.getLanguage() );
        }
		Assert.assertEquals(2, L6.size());
	}

	public void testListWithNullSelector()
	{
		final StmtIterator it1 = model.listStatements(new SimpleSelector(null,
				null, (RDFNode) null));
		final List<Statement> L1 = GraphTestBase.iteratorToList(it1);
		Assert.assertEquals(num * num * 7, L1.size());

	}

	public void testListWithPredicateSelector()
	{

		final StmtIterator it3 = model.listStatements(new SimpleSelector(null,
				predicate[1], (RDFNode) null));
		final List<Statement> L3 = GraphTestBase.iteratorToList(it3);
        for ( Statement aL3 : L3 )
        {
            Assert.assertEquals( predicate[1], aL3.getPredicate() );
        }
		Assert.assertEquals(num * 7, L3.size());
	}

	// StmtIterator it4 = model.listStatements( new SimpleSelector( null,
	// null, tvResObjs[1] ) );
	// List<Statement> L4 = iteratorToList( it4 );
	// for (int i = 0; i < L4.size(); i += 1)
	// assertEquals( tvResObjs[1], L4.get(i).getObject() );
	// assertEquals( 2, L4.size() );

	public void testListWithRDFSelector()
	{

		final StmtIterator it5 = model.listStatements(new SimpleSelector(null,
				null, model.createTypedLiteral(false)));
		final List<Statement> L5 = GraphTestBase.iteratorToList(it5);
        for ( Statement aL5 : L5 )
        {
            Assert.assertEquals( false, aL5.getBoolean() );
        }
		Assert.assertEquals(2, L5.size());
	}

	public void testListWithSubjectSelector()
	{
		final StmtIterator it2 = model.listStatements(new SimpleSelector(
				subject[0], null, (RDFNode) null));
		final List<Statement> L2 = GraphTestBase.iteratorToList(it2);
        for ( Statement aL2 : L2 )
        {
            Assert.assertEquals( subject[0], aL2.getSubject() );
        }
		Assert.assertEquals(num * 7, L2.size());
	}

	public void testNullArgs()
	{

		/*
		 * the _null_ argument to LiteralImpl was preserved only for backward
		 * compatability. It was be logged and has now become an exception.
		 */
		try
		{
			final Literal lit = model.createLiteral(null, "");
			model.query(new SimpleSelector(null, null, lit));
			Assert.fail("SHould have thrown a null pointer exception");
		}
		catch (final NullPointerException expected)
		{ // expected}
		}

		try
		{
			final Literal lit = model.createLiteral(null, "en");
			model.query(new SimpleSelector(null, null, lit));
			Assert.fail("SHould have thrown a null pointer exception");
		}
		catch (final NullPointerException expected)
		{ // expected}
		}

		StmtIterator iter = model.listStatements(new SimpleSelector(null, null,
				(String) null));
		while (iter.hasNext())
		{
			iter.nextStatement().getObject();
		}

		iter = model.listStatements(new SimpleSelector(null, null,
				(Object) null));
		while (iter.hasNext())
		{
			iter.nextStatement().getObject();
		}
	}

	public void testQueryWithLiteralSelector()
	{
		Statement stmt;
		final Model mm = model.query(new SimpleSelector(null, null,
				tvStrings[0], langs[0]));
		Assert.assertEquals(2, mm.size());
		final StmtIterator iter = mm.listStatements();
		try
		{
			while (iter.hasNext())
			{
				stmt = iter.nextStatement();
				Assert.assertEquals(langs[0], stmt.getLanguage());
			}
		}
		finally
		{
			iter.close();
		}
	}

	public void testQueryWithNullSelector()
	{
		int count = 0;
		final Model mm = model.query(new SimpleSelector(null, null,
				(RDFNode) null));
		final StmtIterator iter = mm.listStatements();
		try
		{
			while (iter.hasNext())
			{
				iter.nextStatement();
				count++;
			}
			Assert.assertEquals(num * num * 7, count);
			Assert.assertEquals(mm.size(), count);
		}
		finally
		{
			iter.close();
		}
	}

	public void testQueryWithPredicateSelector()
	{
		Statement stmt;
		int count = 0;
		final Model mm = model.query(new SimpleSelector(null, predicate[1],
				(RDFNode) null));
		final StmtIterator iter = mm.listStatements();
		try
		{
			while (iter.hasNext())
			{
				stmt = iter.nextStatement();
				Assert.assertEquals(predicate[1], stmt.getPredicate());
				count++;
			}
			Assert.assertEquals(num * 7, count);
			Assert.assertEquals(mm.size(), count);
		}
		finally
		{
			iter.close();
		}
	}

	// n=130;
	// count = 0;
	// n++; mm = model.query(new SimpleSelector(null, null, tvResObj[1]));
	// n++; iter = mm.listStatements();
	// while (iter.hasNext()) {
	// stmt = iter.nextStatement();
	// if (! stmt.getObject().equals(tvResObj[1])) error(test, n);
	// count++;
	// }
	// n++; iter.close();
	// n++; if (! (count==2)) error(test,n);
	// n++; if (! (mm.size()==count)) error(test,n);

	public void testQueryWithRDFSelector()
	{
		Statement stmt;
		int count = 0;
		final Model mm = model.query(new SimpleSelector(null, null, model
				.createTypedLiteral(false)));
		final StmtIterator iter = mm.listStatements();
		try
		{
			while (iter.hasNext())
			{
				stmt = iter.nextStatement();
				Assert.assertFalse("Should only return false values",
						stmt.getBoolean());
				count++;
			}
			Assert.assertEquals(2, count);
			Assert.assertEquals(mm.size(), count);
		}
		finally
		{
			iter.close();
		}
	}

	public void testQueryWithSubjectSelector()
	{
		Statement stmt;
		int count = 0;
		final Model mm = model.query(new SimpleSelector(subject[0], null,
				(RDFNode) null));
		final StmtIterator iter = mm.listStatements();
		try
		{
			while (iter.hasNext())
			{
				stmt = iter.nextStatement();
				Assert.assertEquals(subject[0], stmt.getSubject());
				count++;
			}
			Assert.assertEquals(num * 7, count);
			Assert.assertEquals(mm.size(), count);
		}
		finally
		{
			iter.close();
		}
	}

}