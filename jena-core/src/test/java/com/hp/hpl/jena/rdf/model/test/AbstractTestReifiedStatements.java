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
import com.hp.hpl.jena.rdf.model.DoesNotReifyException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.Set;

import org.junit.Assert;

public abstract class AbstractTestReifiedStatements extends ModelTestBase
{
	private Model model;

	private Resource S;

	private Property P;
	private RDFNode O;
	private Statement SPO;
	private Statement SPO2;
	private static final String aURI = "jena:test/reifying#someURI";
	private static final String anotherURI = "jena:test/reifying#anotherURI";

	private static final String anchor = "jena:test/Reifying#";
	protected static Set<ReifiedStatement> noStatements = CollectionFactory
			.createHashedSet();

	public AbstractTestReifiedStatements( final String name )
	{
		super(name);
	}

	public abstract Model getModel();

	/**
	 * utility method: get a set of all the elements delivered by
	 * _m.listReifiedStatements_.
	 */
	public Set<ReifiedStatement> getSetRS( final Model m )
	{
		return m.listReifiedStatements().toSet();
	}

	public Set<ReifiedStatement> getSetRS( final Model m, final Statement st )
	{
		return m.listReifiedStatements(st).toSet();
	}

	@Override
	public void setUp()
	{
		model = getModel();
		final Resource S2 = model
				.createResource(AbstractTestReifiedStatements.anchor
						+ "subject2");
		S = model.createResource(AbstractTestReifiedStatements.anchor
				+ "subject");
		P = model.createProperty(AbstractTestReifiedStatements.anchor
				+ "predicate");
		O = model
				.createLiteral(AbstractTestReifiedStatements.anchor + "object");
		SPO = model.createStatement(S, P, O);
		SPO2 = model.createStatement(S2, P, O);
	}

	/**
	 * the simplest case: if we assert all the components of a reification quad,
	 * we can get a ReifiedStatement that represents the reified statement.
	 */
	public void testBasicReification()
	{
		final Resource R = model
				.createResource(AbstractTestReifiedStatements.aURI);
		model.add(R, RDF.type, RDF.Statement);
		model.add(R, RDF.subject, S);
		model.add(R, RDF.predicate, P);
		model.add(R, RDF.object, O);
		final RDFNode rs = R.as(ReifiedStatement.class);
		Assert.assertEquals("can recover statement", SPO,
				((ReifiedStatement) rs).getStatement());
	}

	/**
	 * walk down the set of statements (represented as an array), recursing with
	 * and
	 * without each statement being present. The mask bits record those
	 * statements
	 * that are in the model. At the bottom of the recursion (n == 0), check
	 * that R
	 * can be reified exactly when all four quad components are present; the
	 * other
	 * statements don't matter.
	 */
	private void testCombinations( final Model m, final Resource R,
			final int mask, final Object[][] statements, final int n )
	{
		if (n == 0)
		{
			try
			{
				// System.err.println( "| hello. mask = " + mask );
				final ReifiedStatement rs = R.as(ReifiedStatement.class);
				// System.err.println( "+  we constructed " + rs );

				if ((mask & 15) != 15)
				{
					m.write(System.out, "TTL");
				}

				Assert.assertTrue(
						"should not reify: not all components present [" + mask
								+ "]: " + rs, (mask & 15) == 15);
				// System.err.println( "+  and we passed the assertion." );
			}
			catch (final DoesNotReifyException e)
			{ // System.err.println( "+  we exploded" );
				Assert.assertFalse("should reify: all components present",
						mask == 15);
			}
		}
		else
		{
			final int i = n - 1;
			final Statement s = (Statement) statements[i][0];
			final int bits = (Integer) statements[i][1];
			testCombinations(m, R, mask, statements, i);
			m.add(s);
			testCombinations(m, R, mask + bits, statements, i);
			m.remove(s);
		}
	}

	public void testConstructionByURI()
	{
		final ReifiedStatement rs = model.createReifiedStatement("spoo:handle",
				SPO);
		final ReifiedStatement rs2 = SPO.createReifiedStatement("spoo:gripper");
		Assert.assertEquals("recover statement (URI)", SPO, rs.getStatement());
		Assert.assertEquals("recover URI", "spoo:handle", rs.getURI());
		Assert.assertEquals("recover URI", "spoo:gripper", rs2.getURI());
	}

	public void testConstructionFromModels()
	{
		testStatementAndModel("fromModel", model.createReifiedStatement(SPO),
				model, SPO);
	}

	public void testConstructionFromStatements()
	{
		testStatementAndModel("fromStatement", SPO.createReifiedStatement(),
				model, SPO);
	}

	public void testConversion()
	{
		final String uri = "spoo:handle";
		model.createReifiedStatement(uri, SPO);
		final ReifiedStatement rs2 = model.createResource(uri).as(
				ReifiedStatement.class);
		Assert.assertEquals("recover statement", SPO, rs2.getStatement());
	}

	/**
	 * "dirty" reifications - those with conflicting quadlets - should fail.
	 */
	public void testDirtyReification()
	{
		final Resource R = model
				.createResource(AbstractTestReifiedStatements.aURI);
		model.add(R, RDF.type, RDF.Statement);
		model.add(R, RDF.subject, S);
		model.add(R, RDF.subject, P);
		testDoesNotReify("boo", R);
	}

	public void testDoesNotReify( final String title, final Resource r )
	{
		try
		{
			r.as(ReifiedStatement.class);
			Assert.fail(title + " (" + r + ")");
		}
		catch (final DoesNotReifyException e)
		{ /* that's what we expect */
		}
	}

	public void testDoesNotReifyElsewhere()
	{
		final String uri = "spoo:rubbish";
		final Model m2 = getModel();
		model.createReifiedStatement(uri, SPO);
		testDoesNotReify("blue model should not reify rubbish",
				m2.createResource(uri));
	}

	// public void testXXX()
	// {
	// String root = "http://root/root#";
	// Model m = ModelFactory.createDefaultModel();
	// Model r = ModelFactory.createRDFSModel( m );
	// Resource S = r.createResource( root + "S" );
	// Property P = r.createProperty( root + "P" );
	// RDFNode O = r.createResource( root + "O" );
	// Statement st = r.createStatement( S, P, O );
	// ReifiedStatement rs = st.createReifiedStatement( root + "RS" );
	// }

	public void testDoesNotReifyUnknown()
	{
		testDoesNotReify("model should not reify rubbish",
				model.createResource("spoo:rubbish"));
	}

	public void testGetAny()
	{
		final Resource r = model.getAnyReifiedStatement(SPO);
		JenaTestBase.assertInstanceOf(ReifiedStatement.class, r);
		Assert.assertEquals("should get me the statement", SPO,
				((ReifiedStatement) r).getStatement());
	}

	public void testIsReified()
	{
		final ReifiedStatement rs = model.createReifiedStatement(
				AbstractTestReifiedStatements.aURI, SPO);
		final Resource BS = model
				.createResource(AbstractTestReifiedStatements.anchor + "BS");
		final Property BP = model
				.createProperty(AbstractTestReifiedStatements.anchor + "BP");
		final RDFNode BO = model
				.createProperty(AbstractTestReifiedStatements.anchor + "BO");
		model.add(rs, P, O);
		Assert.assertTrue("st should be reified now", SPO.isReified());
		Assert.assertTrue("m should have st reified now", model.isReified(SPO));
		Assert.assertFalse("this new statement should not be reified", model
				.createStatement(BS, BP, BO).isReified());
	}

	/**
	 * Leo Bard spotted a problem whereby removing a reified statement from a
	 * model
	 * with style Standard didn't leave the model empty. Here's a test for it.
	 */
	public void testLeosBug()
	{
		final Model A = getModel();
		final Statement st = ModelTestBase.statement(A, "pigs fly south");
		final ReifiedStatement rst = st.createReifiedStatement("eh:pointer");
		A.removeReification(rst);
		ModelTestBase.assertIsoModels(ModelFactory.createDefaultModel(), A);
	}

	/**
	 * this test appeared when TestStatementResources crashed using reified
	 * statements as a step-0 implementation for asSubject()/asObject(). Looks
	 * like there was a problem in modelReifier().getRS(), which we're fixing
	 * ...
	 */
	public void testListDoesntCrash()
	{
		model.createReifiedStatement(SPO);
		model.createReifiedStatement(SPO2);
		Assert.assertTrue("should be non-empty", model.listReifiedStatements()
				.hasNext());
	}

	public void testListReifiedSpecificStatements()
	{
		Assert.assertEquals("no statements should match st",
				AbstractTestReifiedStatements.noStatements,
				getSetRS(model, SPO));
		/* */
		final ReifiedStatement rs = model.createReifiedStatement(
				AbstractTestReifiedStatements.aURI, SPO);
		final ReifiedStatement rs2 = model.createReifiedStatement(
				AbstractTestReifiedStatements.anotherURI, SPO2);
		model.add(rs, P, O);
		// assertEquals( "still no matching statement", empty, getSetRS( m,
		// stOther ) );
		/* */
		final Set<ReifiedStatement> justRS2 = arrayToSet(new ReifiedStatement[] { rs2 });
		model.add(rs2, P, O);
		Assert.assertEquals("now one matching statement", justRS2,
				getSetRS(model, SPO2));
	}

	/**
	 * test that listReifiedStatements produces an iterator that contains
	 * the right reified statements. We *don't* test that they're not
	 * duplicated, because they might be; disallowing duplicates
	 * could be expensive.
	 */
	public void testListReifiedStatements()
	{
		Assert.assertEquals("initially: no reified statements",
				AbstractTestReifiedStatements.noStatements, getSetRS(model));
		final ReifiedStatement rs = model.createReifiedStatement(
				AbstractTestReifiedStatements.aURI, SPO);
		// assertEquals( "still: no reified statements", empty, getSetRS( m ) );
		/* */
		model.add(rs, P, O);
		final Set<ReifiedStatement> justRS = arrayToSet(new ReifiedStatement[] { rs });
		Assert.assertEquals("post-add: one reified statement", justRS,
				getSetRS(model));
		model.add(S, P, rs);
		Assert.assertEquals("post-add: still one reified statement", justRS,
				getSetRS(model));
		/* */
		final ReifiedStatement rs2 = model.createReifiedStatement(
				AbstractTestReifiedStatements.anotherURI, SPO2);
		final Set<ReifiedStatement> bothRS = arrayToSet(new ReifiedStatement[] {
				rs, rs2 });
		model.add(rs2, P, O);
		Assert.assertEquals("post-add: still one reified statement", bothRS,
				getSetRS(model));
	}

	private void testNotReifying( final Model m, final String uri )
	{
		try
		{
			m.createResource(uri).as(ReifiedStatement.class);
			Assert.fail("there should be no reifiedStatement for " + uri);
		}
		catch (final DoesNotReifyException e)
		{ /* that's what we require */
		}
	}

	public void testQuintetOfQuadlets()
	{
		final Resource rs = model.createResource();
		rs.addProperty(RDF.type, RDF.Statement);
		model.createResource().addProperty(RDF.value, rs);
		rs.addProperty(RDF.subject, model.createResource());
		rs.addProperty(RDF.predicate,
				model.createProperty("http://example.org/foo"));
		rs.addProperty(RDF.object, model.createResource());
		rs.addProperty(RDF.object, model.createResource());
		final StmtIterator it = model.listStatements();
		while (it.hasNext())
		{
			final Statement s = it.nextStatement();
			Assert.assertFalse(s.getObject().equals(s.getSubject()));
		}
	}

	/**
	 * check that, from a model with any combination of the statements given,
	 * we can convert R into a ReifiedStatement iff the four components of the
	 * quad are in the model.
	 */
	public void testReificationCombinations()
	{
		final Resource RR = model
				.createResource(AbstractTestReifiedStatements.aURI), SS = model
				.createResource(AbstractTestReifiedStatements.anotherURI);
		final Property PP = RR.as(Property.class);
		final Object[][] statements = {
				{ model.createStatement(RR, RDF.type, RDF.Statement), 1 },
				{ model.createStatement(RR, RDF.subject, SS), 2 },
				{ model.createStatement(RR, RDF.predicate, PP), 4 },
				{ model.createStatement(RR, RDF.object, O), 8 },
				{ model.createStatement(SS, PP, O), 16 },
				{ model.createStatement(RR, PP, O), 32 },
				{ model.createStatement(SS, RDF.subject, SS), 64 },
				{ model.createStatement(SS, RDF.predicate, PP), 128 },
				{ model.createStatement(SS, RDF.object, O), 256 },
				{ model.createStatement(SS, RDF.type, RDF.Statement), 512 } };
		testCombinations(model, RR, 0, statements, statements.length);
	}

	public void testRemoveReificationWorks()
	{
		final Statement st = SPO;
		final Model m = model;
		m.createReifiedStatement(AbstractTestReifiedStatements.aURI, st);
		Assert.assertTrue("st is now reified", st.isReified());
		m.removeAllReifications(st);
		Assert.assertFalse("st is no longer reified", st.isReified());
	}

	public void testRR()
	{
		final Statement st = SPO;
		final Model m = model;
		final ReifiedStatement rs1 = m.createReifiedStatement(
				AbstractTestReifiedStatements.aURI, st);
		final ReifiedStatement rs2 = m.createReifiedStatement(
				AbstractTestReifiedStatements.anotherURI, st);
		m.removeReification(rs1);
		testNotReifying(m, AbstractTestReifiedStatements.aURI);
		Assert.assertTrue("st is still reified", st.isReified());
		m.removeReification(rs2);
		Assert.assertFalse("st should no longer be reified", st.isReified());
	}

	public void testStatementAndModel( final String title,
			final ReifiedStatement rs, final Model m, final Statement st )
	{
		Assert.assertEquals(title + ": recover statement", st,
				rs.getStatement());
		Assert.assertEquals(title + ": recover model", m, rs.getModel());
	}

	public void testStatementListReifiedStatements()
	{
		final Statement st = SPO;
		final Model m = model;
		Assert.assertEquals("it's not there yet",
				AbstractTestReifiedStatements.noStatements,
				GraphTestBase.iteratorToSet(st.listReifiedStatements()));
		final ReifiedStatement rs = m.createReifiedStatement(
				AbstractTestReifiedStatements.aURI, st);
		final Set<ReifiedStatement> justRS = arrayToSet(new ReifiedStatement[] { rs });
		m.add(rs, P, O);
		Assert.assertEquals("it's here now", justRS,
				GraphTestBase.iteratorToSet(st.listReifiedStatements()));
	}

	public void testThisWillBreak()
	{
		final Resource R = model
				.createResource(AbstractTestReifiedStatements.aURI);
		SPO.createReifiedStatement(AbstractTestReifiedStatements.aURI);
		model.add(R, RDF.subject, R);
	}
}
