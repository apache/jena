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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

public class TestListSubjects extends AbstractModelTestBase
{

	static final String subjectPrefix = "http://aldabaran/test8/s";

	static final String predicatePrefix = "http://aldabaran/test8/";

	Resource[] subjects;

	Property[] predicates;

	RDFNode[] objects;
	// Literal [] tvLitObjs;
	Resource[] tvResObjs;
	boolean[] tvBooleans = { false, true };
	long[] tvLongs = { 123, 321 };

	char[] tvChars = { '@', ';' };
	float[] tvFloats = { 456.789f, 789.456f };
	double[] tvDoubles = { 123.456, 456.123 };
	String[] tvStrings = { "test8 testing string 1", "test8 testing string 2" };
	String[] langs = { "en", "fr" };

	public TestListSubjects( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	protected void assertEquiv( final Set<? extends Resource> set,
			final Iterator<? extends Resource> iterator )
	{
		final List<? extends Resource> L = WrappedIterator.create(iterator)
				.toList();
		Assert.assertEquals(set.size(), L.size());
		Assert.assertEquals(set, new HashSet<>(L));
	}

	protected void fillModel()
	{
		final int num = 5;
		// tvLitObjs = new Literal[]
		// { model.createTypedLiteral( new LitTestObjF() ),
		// model.createTypedLiteral( new LitTestObjF() ) };

		// tvResObjs = new Resource[]
		// { model.createResource( new ResTestObjF() ),
		// model.createResource( new ResTestObjF() ) };

		objects = new RDFNode[] { model.createTypedLiteral(tvBooleans[1]),
				model.createTypedLiteral(tvLongs[1]),
				model.createTypedLiteral(tvChars[1]),
				model.createTypedLiteral(tvFloats[1]),
				model.createTypedLiteral(tvDoubles[1]),
				model.createLiteral(tvStrings[1]),
				model.createLiteral(tvStrings[1], langs[1])
		// tvLitObjs[1],
		// tvResObjs[1]
		};

		subjects = new Resource[num];
		predicates = new Property[num];

		for (int i = 0; i < num; i++)
		{
			subjects[i] = model.createResource(TestListSubjects.subjectPrefix
					+ i);
			predicates[i] = model.createProperty(
					TestListSubjects.predicatePrefix + i, "p");
		}

		for (int i = 0; i < num; i += 1)
		{
			model.addLiteral(subjects[i], predicates[4], false);
		}

		for (int i = 0; i < 2; i += 1)
		{
			for (int j = 0; j < 2; j += 1)
			{
				model.add(subjects[i], predicates[j],
						model.createTypedLiteral(tvBooleans[j]));
				model.addLiteral(subjects[i], predicates[j], tvLongs[j]);
				model.addLiteral(subjects[i], predicates[j], tvChars[j]);
				model.add(subjects[i], predicates[j],
						model.createTypedLiteral(tvFloats[j]));
				model.add(subjects[i], predicates[j],
						model.createTypedLiteral(tvDoubles[j]));
				model.add(subjects[i], predicates[j], tvStrings[j]);
				model.add(subjects[i], predicates[j], tvStrings[j], langs[j]);
				// model.add(subjects[i], predicates[j], tvLitObjs[j] );
				// model.add(subjects[i], predicates[j], tvResObjs[j] );
			}
		}
	}

	// the methods are deprecated, the tests eliminated
	// public void testListResourcesOnObject()
	// {
	// Object d = new Date();
	// Model model = modelWithStatements( "" );
	// model.addLiteral( resource( "S" ), property( "P" ), d );
	// model.addLiteral( resource( "X" ), property( "P" ), new Object() );
	// List answers = model.listResourcesWithProperty( property( "P" ), d
	// ).toList();
	// assertEquals( listOfOne( resource( "S" ) ), answers );
	// }

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		fillModel();
	}

	protected Set<Resource> subjectsTo( final String prefix, final int limit )
	{
		final Set<Resource> result = new HashSet<>();
		for (int i = 0; i < limit; i += 1)
		{
			result.add(ModelHelper.resource(prefix + i));
		}
		return result;
	}

	public void testGetRequiredProperty()
	{
		model.getRequiredProperty(subjects[1], predicates[1]);
		try
		{
			model.getRequiredProperty(subjects[1], RDF.value);
			Assert.fail("should not find absent property");
		}
		catch (final PropertyNotFoundException e)
		{
			JenaTestBase.pass();
		}
	}

	public void testListSubjects()
	{
		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 5),
				model.listResourcesWithProperty(predicates[4]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0], tvBooleans[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listResourcesWithProperty(predicates[0], tvBooleans[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0], tvChars[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listResourcesWithProperty(predicates[0], tvChars[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0], tvLongs[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listResourcesWithProperty(predicates[0], tvLongs[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0], tvFloats[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listResourcesWithProperty(predicates[0], tvFloats[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0], tvDoubles[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listResourcesWithProperty(predicates[0], tvDoubles[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0],
						(byte) tvLongs[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listResourcesWithProperty(predicates[0],
						(byte) tvLongs[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0],
						(short) tvLongs[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listResourcesWithProperty(predicates[0],
						(short) tvLongs[1]));

		assertEquiv(
				subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listResourcesWithProperty(predicates[0], (int) tvLongs[0]));

		assertEquiv(
				subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listResourcesWithProperty(predicates[0], (int) tvLongs[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listSubjectsWithProperty(predicates[0], tvStrings[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listSubjectsWithProperty(predicates[0], tvStrings[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 2),
				model.listSubjectsWithProperty(predicates[0], tvStrings[0],
						langs[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listSubjectsWithProperty(predicates[0], tvStrings[1],
						langs[0]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listSubjectsWithProperty(predicates[0], tvStrings[0],
						langs[1]));

		assertEquiv(subjectsTo(TestListSubjects.subjectPrefix, 0),
				model.listSubjectsWithProperty(predicates[0], tvStrings[1],
						langs[1]));

		// assertEquiv( subjectsTo( subjectPrefix, 2 ),
		// model.listResourcesWithProperty( predicates[0], tvLitObjs[0] ) );
		//
		// assertEquiv( subjectsTo( subjectPrefix, 0 ),
		// model.listResourcesWithProperty( predicates[0], tvLitObjs[1] ) );
		//
		// assertEquiv( subjectsTo( subjectPrefix, 0 ),
		// model.listResourcesWithProperty( predicates[0], tvResObjs[0] ) );
		//
		// assertEquiv( subjectsTo( subjectPrefix, 0 ),
		// model.listResourcesWithProperty( predicates[0], tvResObjs[1] ) );

		// assertEquiv( new HashSet( Arrays.asList( objects ) ),
		// model.listObjectsOfProperty( predicates[1] ) );
	}
}
