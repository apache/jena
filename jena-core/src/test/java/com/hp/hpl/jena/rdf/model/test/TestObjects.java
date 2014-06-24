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
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

public class TestObjects extends AbstractModelTestBase
{

	protected Resource S;
	protected Property P;

	protected static int numberSubjects = 7;

	protected static int numberPredicates = 3;

	protected static final String subjectPrefix = "http://aldabaran/test6/s";

	protected static final String predicatePrefix = "http://aldabaran/test6/";

	public TestObjects( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	protected Set<Statement> fill( final Model model )
	{
		final Set<Statement> statements = new HashSet<>();
		for (int i = 0; i < TestObjects.numberSubjects; i += 1)
		{
			for (int j = 0; j < TestObjects.numberPredicates; j += 1)
			{
				final Statement s = model
						.createLiteralStatement(ModelHelper
								.resource(TestObjects.subjectPrefix + i),
								ModelHelper
										.property(TestObjects.predicatePrefix
												+ j + "/p"),
								(i * TestObjects.numberPredicates) + j);
				model.add(s);
				statements.add(s);
			}
		}
		Assert.assertEquals(TestObjects.numberSubjects
				* TestObjects.numberPredicates, model.size());
		return statements;
	}

	protected Set<Literal> literalsFor( final int predicate )
	{
		final Set<Literal> result = new HashSet<>();
		for (int i = 0; i < TestObjects.numberSubjects; i += 1)
		{
			result.add(model
					.createTypedLiteral((i * TestObjects.numberPredicates)
							+ predicate));
		}
		return result;
	}

	protected Set<Literal> literalsUpto( final int limit )
	{
		final Set<Literal> result = new HashSet<>();
		for (int i = 0; i < limit; i += 1)
		{
			result.add(model.createTypedLiteral(i));
		}
		return result;
	}

	protected Set<String> predicateSet( final int limit )
	{
		final Set<String> result = new HashSet<>();
		for (int i = 0; i < limit; i += 1)
		{
			result.add(TestObjects.predicatePrefix + i + "/");
		}
		return result;
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		S = model.createResource("http://nowhere.man/subject");
		P = model.createProperty("http://nowhere.man/predicate");
	}

	protected Set<Resource> subjectSet( final int limit )
	{
		final Set<Resource> result = new HashSet<>();
		for (int i = 0; i < limit; i += 1)
		{
			result.add(ModelHelper.resource(TestObjects.subjectPrefix + i));
		}
		return result;
	}

	@Override
	public void tearDown() throws Exception
	{
		S = null;
		P = null;
		super.tearDown();
	}

	public void testListNamespaces()
	{
		fill(model);
		final List<String> L = model.listNameSpaces().toList();
		Assert.assertEquals(TestObjects.numberPredicates, L.size());
		final Set<String> wanted = predicateSet(TestObjects.numberPredicates);
		Assert.assertEquals(wanted, new HashSet<>(L));
	}

	public void testListObjects()
	{
		fill(model);
		final Set<Literal> wanted = literalsUpto(TestObjects.numberSubjects
				* TestObjects.numberPredicates);
		Assert.assertEquals(wanted,
				GraphTestBase.iteratorToSet(model.listObjects()));
	}

	public void testListObjectsOfPropertyByProperty()
	{
		fill(model);
		final List<RDFNode> L = GraphTestBase.iteratorToList(model
				.listObjectsOfProperty(ModelHelper
						.property(TestObjects.predicatePrefix + "0/p")));
		Assert.assertEquals(TestObjects.numberSubjects, L.size());
		final Set<Literal> wanted = literalsFor(0);
		Assert.assertEquals(wanted, new HashSet<>(L));
	}

	public void testListObjectsOfPropertyBySubject()
	{
		final int size = 10;
		final Resource s = model.createResource();
		for (int i = 0; i < size; i += 1)
		{
			model.addLiteral(s, RDF.value, i);
		}
		final List<RDFNode> L = GraphTestBase.iteratorToList(model
				.listObjectsOfProperty(s, RDF.value));
		Assert.assertEquals(size, L.size());
		final Set<Literal> wanted = literalsUpto(size);
		Assert.assertEquals(wanted, new HashSet<>(L));
	}

	public void testListStatements()
	{
		final Set<Statement> statements = fill(model);
		final List<Statement> L = model.listStatements().toList();
		Assert.assertEquals(statements.size(), L.size());
		Assert.assertEquals(statements, new HashSet<>(L));
	}

	public void testListSubjects()
	{
		fill(model);
		final List<Resource> L = model.listSubjects().toList();
		Assert.assertEquals(TestObjects.numberSubjects, L.size());
		final Set<Resource> wanted = subjectSet(TestObjects.numberSubjects);
		Assert.assertEquals(wanted, GraphTestBase.iteratorToSet(L.iterator()));
	}

}
