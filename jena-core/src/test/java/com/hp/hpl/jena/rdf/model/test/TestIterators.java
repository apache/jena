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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import org.junit.Assert;

public class TestIterators extends AbstractModelTestBase
{
	int num = 5;
	Resource subject[] = new Resource[num];
	Property predicate[] = new Property[num];
	Statement stmts[] = new Statement[num * num];

	String suri = "http://aldabaran/test6/s";
	String puri = "http://aldabaran/test6/";

	public TestIterators( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		for (int i = 0; i < num; i++)
		{
			subject[i] = model.createResource(suri + Integer.toString(i));
			predicate[i] = model
					.createProperty(puri + Integer.toString(i), "p");
		}

		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < num; j++)
			{
				final Statement stmt = model.createStatement(subject[i],
						predicate[j], model.createTypedLiteral((i * num) + j));
				model.add(stmt);
				model.add(stmt);
				stmts[(i * num) + j] = stmt;
			}
		}
	}

	/**
	 * bug detected in StatementIteratorImpl - next does not
	 * advance current, so remove doesn't work with next;
	 * this test should expose the bug.
	 */
	public void testIteratorRemove()
	{
		final StmtIterator it = model.listStatements();
		try
		{
			while (it.hasNext())
			{
				it.next();
				it.remove();
			}
			Assert.assertEquals("Remove failed", 0, model.size());
		}
		finally
		{
			it.close();
		}

	}

	public void testListObjects()
	{
		int count = 0;
		NodeIterator iter;
		iter = model.listObjects();
		while (iter.hasNext())
		{
			iter.nextNode();
			count++;
		}
		Assert.assertEquals(num * num, count);
	}

	public void testNamespaceIterator()
	{
		final boolean predf[] = new boolean[num];
		for (int i = 0; i < num; i++)
		{
			predf[i] = false;
		}
		final NsIterator nIter = model.listNameSpaces();
		while (nIter.hasNext())
		{
			final String ns = nIter.nextNs();
			boolean found = false;
			for (int i = 0; i < num; i++)
			{
				if (ns.equals(predicate[i].getNameSpace()))
				{
					found = true;
					Assert.assertFalse("Should not have found " + predicate[i]
							+ " already.", predf[i]);
					predf[i] = true;
				}
			}
			Assert.assertTrue("Should have found " + ns, found);
		}
		for (int i = 0; i < num; i++)
		{
			Assert.assertTrue("Should have found " + predicate[i], predf[i]);
		}
	}

	public void testObjectsOfProperty()
	{

		NodeIterator iter;
		final boolean[] object = new boolean[num * num];
		for (int i = 0; i < (num * num); i++)
		{
			object[i] = false;
		}
		iter = model.listObjectsOfProperty(predicate[0]);
		while (iter.hasNext())
		{
			final Literal l = (Literal) iter.nextNode();
			final int i = l.getInt();
			object[i] = true;
		}
		for (int i = 0; i < (num * num); i++)
		{
			if ((i % num) == 0)
			{
				Assert.assertTrue(object[i]);
			}
			else
			{
				Assert.assertFalse(object[i]);
			}
		}

	}

	public void testObjectsOfPropertyAndValue()
	{
		NodeIterator iter;
		final boolean[] object = new boolean[num];
		final Resource subj = model.createResource();
		for (int i = 0; i < num; i++)
		{
			model.addLiteral(subj, RDF.value, i);
			object[i] = false;
		}

		iter = model.listObjectsOfProperty(subj, RDF.value);
		while (iter.hasNext())
		{
			final int i = ((Literal) iter.nextNode()).getInt();
			object[i] = true;
		}
		for (int i = 0; i < (num); i++)
		{
			Assert.assertTrue(object[i]);
		}
	}

	public void testResourceIterator()
	{

		final boolean subjf[] = new boolean[num];

		for (int i = 0; i < num; i++)
		{
			subjf[i] = false;
		}

		boolean found = false;
		final ResIterator rIter = model.listSubjects();
		while (rIter.hasNext())
		{
			final Resource subj = rIter.nextResource();
			found = false;
			for (int i = 0; i < num; i++)
			{
				if (subj.equals(subject[i]))
				{
					found = true;
					Assert.assertFalse("Should not have found " + subject[i]
							+ " already.", subjf[i]);
					subjf[i] = true;
				}
			}
			Assert.assertTrue("Should have found " + subj, found);
		}
		for (int i = 0; i < num; i++)
		{
			Assert.assertTrue("Should have found " + subject[i], subjf[i]);
		}

		// System.err.println(
		// "WARNING: listNameSpace testing wonky for the moment" );
		// NsIterator nIter = model.listNameSpaces();
		// HashSet fromIterator = new HashSet();
		// HashSet fromPredicates = new HashSet();
		// while (nIter.hasNext()) fromIterator.add( nIter.next() );
		// for (int i = 0; i < num; i += 1) fromPredicates.add(
		// predicate[i].getNameSpace() );
		// if (fromIterator.equals( fromPredicates ))
		// {}
		// else
		// {
		// System.err.println( "| oh dear." );
		// System.err.println( "|  predicate namespaces: " + fromPredicates );
		// System.err.println( "|  iterator namespaces: " + fromIterator );
		// }

	}

	public void testStatementIter()
	{
		final int numStmts = num * num;
		final boolean stmtf[] = new boolean[numStmts];
		Assert.assertEquals(numStmts, model.size());
		for (int i = 0; i < numStmts; i++)
		{
			stmtf[i] = false;
		}

		final StmtIterator sIter = model.listStatements();
		while (sIter.hasNext())
		{
			final Statement stmt = sIter.nextStatement();
			boolean found = false;
			for (int i = 0; i < numStmts; i++)
			{
				if (stmt.equals(stmts[i]))
				{
					found = true;
					Assert.assertFalse("Should not have found " + stmts[i]
							+ " already.", stmtf[i]);
					stmtf[i] = true;
				}
			}
			Assert.assertTrue("Should have found " + stmt, found);
		}
		for (int i = 0; i < numStmts; i++)
		{
			Assert.assertTrue("Should have found " + stmts[i], stmtf[i]);
		}
	}
	// SEE the tests in model.test: TestReifiedStatements and
	// TestStatementResources
	// {
	// System.err.println(
	// "WARNING: reification testing suppressed for the moment" );
	// /* Reification is not working properly
	//
	// for (int i=0; i<num; i++) {
	// stmtf[i] = false;
	// model.add(stmts[i], predicate[i], i);
	// }
	// sIter = model.listReifiedStatements();
	// while (sIter.hasNext()) {
	// Statement stmt = sIter.next();
	// found = false;
	// for (int i=0; i<num; i++) {
	// if (stmt.equals(stmts[i])) {
	// found = true;
	// if (stmtf[i]) error(test, 200);
	// stmtf[i] = true;
	// }
	// }
	// if (! found) error(test, 210);
	// }
	// for (int i=0; i<num; i++) {
	// if (! stmtf[i]) error(test, 220+i);
	// } */
	// }
}
