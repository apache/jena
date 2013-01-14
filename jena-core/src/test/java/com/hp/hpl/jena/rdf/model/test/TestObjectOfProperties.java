package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import org.junit.Assert;

public class TestObjectOfProperties extends AbstractModelTestBase
{
	/*
	 * 
	 * boolean predf[] = new boolean[num];
	 * 
	 * boolean objf[] = new boolean[numObj];
	 * 
	 * try {
	 */

	int num = 5;

	Resource subject[] = new Resource[num];
	Property predicate[] = new Property[num];
	Statement stmts[];
	Statement stmt;

	String suri = "http://aldabaran/test8/s";
	String puri = "http://aldabaran/test8/";

	boolean tvBoolean[] = { false, true };
	long tvLong[] = { 123, 321 };
	char tvChar[] = { '@', ';' };
	float tvFloat[] = { 456.789f, 789.456f };
	double tvDouble[] = { 123.456, 456.123 };
	String tvString[] = { "testing string 1", "testing string 2" };
	String lang[] = { "en", "fr" };

	int numObj = 7;
	RDFNode object[] = new RDFNode[numObj];

	// Literal tvLitObj[];
	// Resource tvResObj[] =;

	public TestObjectOfProperties( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	private void assertFoundAll( final boolean[] subjf )
	{
		for (int i = 0; i < num; i++)
		{
			Assert.assertTrue("Should have found " + subject[i], subjf[i]);
		}
	}

	private void assertFoundNone( final boolean[] subjf )
	{
		for (int i = 0; i < num; i++)
		{
			Assert.assertFalse("Should not have found " + subject[i], subjf[i]);
		}
	}

	private void checkBooleanSubjects( final boolean[] subjf )
	{
		for (int i = 0; i < num; i++)
		{
			if (subjf[i])
			{
				Assert.assertFalse(i > 1);
			}
			else
			{
				Assert.assertFalse(i < 2);
			}
		}
	}

	private void processIterator( final ResIterator rIter, final boolean[] subjf )
	{
		for (int i = 0; i < num; i++)
		{
			subjf[i] = false;
		}

		while (rIter.hasNext())
		{
			final Resource subj = rIter.nextResource();
			Boolean found = false;
			for (int i = 0; i < num; i++)
			{
				if (subj.equals(subject[i]))
				{
					found = true;
					Assert.assertFalse("Should not have found " + subject[i]
							+ " already", subjf[i]);
					subjf[i] = true;
				}
			}
			Assert.assertTrue("Should have found " + subj, found);
		}
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		// tvLitObj = { model.createTypedLiteral(new LitTestObjF()),
		// model.createTypedLiteral(new LitTestObjF()) };
		// tvResObj = { model.createResource(new ResTestObjF()),
		// model.createResource(new ResTestObjF()) };

		for (int i = 0; i < num; i++)
		{
			subject[i] = model.createResource(suri + Integer.toString(i));
			predicate[i] = model
					.createProperty(puri + Integer.toString(i), "p");
		}

		for (int i = 0; i < num; i++)
		{
			model.addLiteral(subject[i], predicate[4], false);
		}

		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				stmt = model.createStatement(subject[i], predicate[j],
						model.createTypedLiteral(tvBoolean[j]));
				model.add(stmt);
				stmt = model.createLiteralStatement(subject[i], predicate[j],
						tvLong[j]);
				model.add(stmt);
				stmt = model.createLiteralStatement(subject[i], predicate[j],
						tvChar[j]);
				model.add(stmt);

				stmt = model.createStatement(subject[i], predicate[j],
						model.createTypedLiteral(tvFloat[j]));
				model.add(stmt);
				stmt = model.createStatement(subject[i], predicate[j],
						model.createTypedLiteral(tvDouble[j]));
				model.add(stmt);

				stmt = model.createStatement(subject[i], predicate[j],
						tvString[j]);
				model.add(stmt);
				stmt = model.createStatement(subject[i], predicate[j],
						tvString[j], lang[j]);
				model.add(stmt);
				// stmt = model.createStatement(subject[i], predicate[j],
				// tvLitObj[j]);
				// model.add(stmt);
				// stmt = model.createStatement(subject[i], predicate[j],
				// tvResObj[j]);
				model.add(stmt);
			}
		}
		object[0] = model.createTypedLiteral(tvBoolean[1]);
		object[1] = model.createTypedLiteral(tvLong[1]);
		object[2] = model.createTypedLiteral(tvChar[1]);
		object[3] = model.createTypedLiteral(tvFloat[1]);
		object[4] = model.createTypedLiteral(tvDouble[1]);
		object[5] = model.createLiteral(tvString[1]);
		object[6] = model.createLiteral(tvString[1], lang[1]);
		// object[7] = tvLitObj[1];
		// object[7] = tvResObj[1];

	}

	public void testListObjectsOfProperty()
	{
		final boolean objf[] = new boolean[numObj];

		final NodeIterator nIter = model.listObjectsOfProperty(predicate[1]);
		while (nIter.hasNext())
		{
			final RDFNode obj = nIter.nextNode();
			Boolean found = false;
			for (int i = 0; i < numObj; i++)
			{
				if (obj.equals(object[i]))
				{
					found = true;
					Assert.assertFalse("Should not have found " + object[i]
							+ " already", objf[i]);
					objf[i] = true;
				}
			}
			Assert.assertTrue("Should have found " + obj, found);
		}
		for (int i = 0; i < numObj; i++)
		{
			Assert.assertTrue("Should have found " + object[i], objf[i]);
		}

	}

	public void testListResourcesWIthProperty()
	{
		final boolean subjf[] = new boolean[num];
		processIterator(model.listResourcesWithProperty(predicate[4]), subjf);
		assertFoundAll(subjf);

		processIterator(model.listResourcesWithProperty(predicate[0]), subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0],
						model.createTypedLiteral(tvBoolean[0])), subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0],
						model.createTypedLiteral(tvBoolean[1])), subjf);
		assertFoundNone(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0], (byte) tvLong[0]),
				subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0], (byte) tvLong[1]),
				subjf);
		assertFoundNone(subjf);

		processIterator(model.listResourcesWithProperty(predicate[0],
				(short) tvLong[0]), subjf);
		checkBooleanSubjects(subjf);

		processIterator(model.listResourcesWithProperty(predicate[0],
				(short) tvLong[1]), subjf);
		assertFoundNone(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0], (int) tvLong[0]),
				subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0], (int) tvLong[1]),
				subjf);
		assertFoundNone(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0], tvLong[0]), subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0], tvLong[1]), subjf);
		assertFoundNone(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0], tvChar[0]), subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0], tvChar[1]), subjf);
		assertFoundNone(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0],
						model.createTypedLiteral(tvDouble[0])), subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0],
						model.createTypedLiteral(tvDouble[1])), subjf);
		assertFoundNone(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0],
						model.createTypedLiteral(tvDouble[0])), subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listResourcesWithProperty(predicate[0],
						model.createTypedLiteral(tvDouble[1])), subjf);
		assertFoundNone(subjf);
	}

	public void testListSubjectsWithProperty()
	{
		final boolean subjf[] = new boolean[num];
		processIterator(
				model.listSubjectsWithProperty(predicate[0], tvString[0]),
				subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listSubjectsWithProperty(predicate[0], tvString[1]),
				subjf);
		assertFoundNone(subjf);

		processIterator(model.listSubjectsWithProperty(predicate[0],
				tvString[0], lang[0]), subjf);
		checkBooleanSubjects(subjf);

		processIterator(
				model.listSubjectsWithProperty(predicate[0], tvString[1]),
				subjf);
		assertFoundNone(subjf);

		// n=1200;
		// // System.out.println( "* -- n := " + n );
		// for (int i=0; i<num; i++) {
		// subjf[i] = false;
		// }
		// found = false;
		// rIter = model.listResourcesWithProperty(predicate[0], tvLitObj[0]);
		// while (rIter.hasNext()) {
		// Resource subj = rIter.nextResource();
		// found = false;
		// for (int i=0; i<num; i++) {
		// if (subj.equals(subject[i])) {
		// found = true;
		// if (subjf[i]) error(test, n+10);
		// subjf[i] = true;
		// }
		// }
		// if (! found) error(test, n+20);
		// }
		// for (int i=0; i<num; i++) {
		// if (subjf[i]) {
		// if (i>1) error(test, n+30+i);
		// } else {
		// if (i<2) error(test, n+40+i);
		// }
		// }
		//
		// for (int i=0; i<num; i++) {
		// subjf[i] = false;
		// }
		// found = false;
		// rIter = model.listResourcesWithProperty(predicate[0], tvLitObj[1]);
		// while (rIter.hasNext()) {
		// Resource subj = rIter.nextResource();
		// found = false;
		// for (int i=0; i<num; i++) {
		// if (subj.equals(subject[i])) {
		// found = true;
		// if (subjf[i]) error(test, n+50);
		// subjf[i] = true;
		// }
		// }
		// if (! found) error(test, n+60);
		// }
		// for (int i=0; i<num; i++) {
		// if (subjf[i]) error(test, n+70+i);
		// }

		// n=1300;
		// // System.out.println( "* -- n := " + n );
		// for (int i=0; i<num; i++) {
		// subjf[i] = false;
		// }
		// found = false;
		// rIter = model.listResourcesWithProperty(predicate[0], tvResObj[0]);
		// while (rIter.hasNext()) {
		// Resource subj = rIter.nextResource();
		// found = false;
		// for (int i=0; i<num; i++) {
		// if (subj.equals(subject[i])) {
		// found = true;
		// if (subjf[i]) error(test, n+10);
		// subjf[i] = true;
		// }
		// }
		// if (! found) error(test, n+20);
		// }
		// for (int i=0; i<num; i++) {
		// if (subjf[i]) {
		// if (i>1) error(test, n+30+i);
		// } else {
		// if (i<2) error(test, n+40+i);
		// }
		// }
		//
		// for (int i=0; i<num; i++) {
		// subjf[i] = false;
		// }
		// found = false;
		// rIter = model.listResourcesWithProperty(predicate[0], tvResObj[1]);
		// while (rIter.hasNext()) {
		// Resource subj = rIter.nextResource();
		// found = false;
		// for (int i=0; i<num; i++) {
		// if (subj.equals(subject[i])) {
		// found = true;
		// if (subjf[i]) error(test, n+50);
		// subjf[i] = true;
		// }
		// }
		// if (! found) error(test, n+60);
		// }
		// for (int i=0; i<num; i++) {
		// if (subjf[i]) error(test, n+70+i);
		// }
	}
}
