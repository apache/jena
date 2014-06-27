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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.CollectionFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.junit.Assert;

/**
 * provides useful functionality for testing models, eg building small models
 * from strings, testing equality, etc.
 */

public class ModelTestBase extends GraphTestBase
{
	protected static Model aModel = ModelTestBase.extendedModel();

	protected static final Model empty = ModelFactory.createDefaultModel();

	/**
	 * Fail if the two models are not isomorphic. See
	 * assertIsoModels(String,Model,Model).
	 */
	public static void assertIsoModels( final Model wanted, final Model got )
	{
		ModelTestBase.assertIsoModels("models must be isomorphic", wanted, got);
	}

	/**
	 * test that two models are isomorphic and fail if they are not.
	 * 
	 * @param title
	 *            a String appearing at the beginning of the failure message
	 * @param wanted
	 *            the model value that is expected
	 * @param got
	 *            the model value to check
	 * @exception junit.framework.AssertionFailedError if the models are not isomorphic
	 */
	public static void assertIsoModels( final String title, final Model wanted,
			final Model got )
	{
		if (wanted.isIsomorphicWith(got) == false)
		{
			final Map<Node, Object> map = CollectionFactory.createHashedMap();
			Assert.fail(title + ": expected "
					+ GraphTestBase.nice(wanted.getGraph(), map)
					+ "\n but had " + GraphTestBase.nice(got.getGraph(), map));
		}
	}

	/**
	 * Answer a default model; it exists merely to abbreviate the rather long
	 * explicit
	 * invocation.
	 * 
	 * @return a new default [aka memory-based] model
	 */
	public static Model createMemModel()
	{
		return ModelFactory.createDefaultModel();
	}

	/**
	 * make a model, give it Extended prefixes
	 */
	public static Model createModel()
	{
		final Model result = ModelFactory.createDefaultModel();
		result.setNsPrefixes(PrefixMapping.Extended);
		return result;
	}

	protected static Model extendedModel()
	{
		final Model result = ModelFactory.createDefaultModel();
		result.setNsPrefixes(PrefixMapping.Extended);
		return result;
	}

	public static Literal literal( final Model m, final String s )
	{
		return ModelTestBase.rdfNode(m, s).as(Literal.class);
	}

	/**
	 * add to a model all the statements expressed by a string.
	 * 
	 * @param m
	 *            the model to be updated
	 * @param facts
	 *            a sequence of semicolon-separated "S P O" facts
	 * @return the updated model
	 */
	public static Model modelAdd( final Model m, final String facts )
	{
		final StringTokenizer semis = new StringTokenizer(facts, ";");
		while (semis.hasMoreTokens())
		{
			m.add(ModelTestBase.statement(m, semis.nextToken()));
		}
		return m;
	}

	/**
	 * makes a model initialised with statements parsed from a string.
	 * 
	 * @param facts
	 *            a string in semicolon-separated "S P O" format
	 * @return a model containing those facts
	 */
	public static Model modelWithStatements( final String facts )
	{
		return ModelTestBase.modelAdd(ModelTestBase.createModel(), facts);
	}

	protected static String nice( final RDFNode n )
	{
		return GraphTestBase.nice(n.asNode());
	}

	public static Property property( final Model m, final String s )
	{
		return ModelTestBase.rdfNode(m, s).as(Property.class);
	}

	public static Property property( final String s )
	{
		return ModelTestBase.property(ModelTestBase.aModel, s);
	}

	public static RDFNode rdfNode( final Model m, final String s )
	{
		return m.asRDFNode(NodeCreateUtils.create(m, s));
	}

	public static <T extends RDFNode> T rdfNode( final Model m, final String s,
			final Class<T> c )
	{
		return ModelTestBase.rdfNode(m, s).as(c);
	}

	protected static Resource resource()
	{
		return ResourceFactory.createResource();
	}

	public static Resource resource( final Model m, final String s )
	{
		return (Resource) ModelTestBase.rdfNode(m, s);
	}

	public static Resource resource( final String s )
	{
		return ModelTestBase.resource(ModelTestBase.aModel, s);
	}

	/**
	 * Create an array of Resources from a whitespace-separated string
	 * 
	 * @param m
	 *            a model to serve as a resource factory
	 * @param items
	 *            a whitespace-separated sequence to feed to resource
	 * @return a RDFNode[] of the parsed resources
	 */
	public static Resource[] resources( final Model m, final String items )
	{
		final ArrayList<Resource> rl = new ArrayList<>();
		final StringTokenizer st = new StringTokenizer(items);
		while (st.hasMoreTokens())
		{
			rl.add(ModelTestBase.resource(m, st.nextToken()));
		}
		return rl.toArray(new Resource[rl.size()]);
	}

	/**
	 * Answer the set of resources given by the space-separated
	 * <code>items</code> string. Each resource specification is interpreted
	 * as per <code>resource</code>.
	 */
	public static Set<Resource> resourceSet( final String items )
	{
		final Set<Resource> result = new HashSet<>();
		final StringTokenizer st = new StringTokenizer(items);
		while (st.hasMoreTokens())
		{
			result.add(ModelTestBase.resource(st.nextToken()));
		}
		return result;
	}

	/**
	 * create a Statement in a given Model with (S, P, O) extracted by parsing a
	 * string.
	 * 
	 * @param m
	 *            the model the statement is attached to
	 * @param fact
	 *            "S P O" string.
	 * @return m.createStatement(S, P, O)
	 */
	public static Statement statement( final Model m, final String fact )
	{
		final StringTokenizer st = new StringTokenizer(fact);
		final Resource sub = ModelTestBase.resource(m, st.nextToken());
		final Property pred = ModelTestBase.property(m, st.nextToken());
		final RDFNode obj = ModelTestBase.rdfNode(m, st.nextToken());
		return m.createStatement(sub, pred, obj);
	}

	public static Statement statement( final String fact )
	{
		return ModelTestBase.statement(ModelTestBase.aModel, fact);
	}

	/**
	 * Create an array of Statements parsed from a semi-separated string.
	 * 
	 * @param m
	 *            a model to serve as a statement factory
	 * @param facts
	 *            a sequence of semicolon-separated "S P O" facts
	 * @return a Statement[] of the (S P O) statements from the string
	 */
	public static Statement[] statements( final Model m, final String facts )
	{
		final ArrayList<Statement> sl = new ArrayList<>();
		final StringTokenizer st = new StringTokenizer(facts, ";");
		while (st.hasMoreTokens())
		{
			sl.add(ModelTestBase.statement(m, st.nextToken()));
		}
		return sl.toArray(new Statement[sl.size()]);
	}

	public ModelTestBase( final String name )
	{
		super(name);
	}

}
