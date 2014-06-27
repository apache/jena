/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.test.helpers;

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
import com.hp.hpl.jena.rdf.model.test.AbstractModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.CollectionFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.junit.Ignore;

/**
    provides useful functionality for testing models, eg building small models
    from strings, testing equality, etc.

    Currently this class extends GraphTestBase and thus TestCase.

    TODO: Refactoring should remove the TestCase dependency in future.

 */
@Ignore // ignore this class as a test case.
public class ModelHelper extends GraphTestBase
{
	private ModelHelper(String name)
	{ super(name); }


	protected static Model aModel;

	static {
		aModel = ModelFactory.createDefaultModel();
		aModel.setNsPrefixes( PrefixMapping.Extended );
	}

	protected static final Model empty = ModelFactory.createDefaultModel();

	protected static Model extendedModel(AbstractModelTestBase base)
	{
		Model result = base.createModel();
		result.setNsPrefixes( PrefixMapping.Extended );
		return result;
	}

	protected static String nice( RDFNode n )
	{ return nice( n.asNode() ); }

	/**
        create a Statement in a given Model with (S, P, O) extracted by parsing a string.

        @param m the model the statement is attached to
        @param fact an "S P O" string.
        @return model.createStatement(S, P, O)
	 */
	public static Statement statement( Model m, String fact )
	{
		StringTokenizer st = new StringTokenizer( fact );
		Resource sub = resource( m, st.nextToken() );
		Property pred = property( m, st.nextToken() );
		RDFNode obj = rdfNode( m, st.nextToken() );
		return m.createStatement( sub, pred, obj );
	}

	public static Statement statement( String fact )
	{ return statement( aModel, fact ); }

	public static RDFNode rdfNode( Model m, String s )
	{ return m.asRDFNode( NodeCreateUtils.create( m, s ) ); }

	public static <T extends RDFNode> T rdfNode( Model m, String s, Class<T> c )
	{ return rdfNode( m, s ).as(  c  );  }

	public static Resource resource()
	{ return ResourceFactory.createResource(); }

	public static Resource resource( String s )
	{ return resource( aModel, s ); }

	public static Resource resource( Model m, String s )
	{ return (Resource) rdfNode( m, s ); }

	public static Property property( String s )
	{ return property( aModel, s ); }

	public static Property property( Model m, String s )
	{ return rdfNode( m, s ).as( Property.class ); }

	public static Literal literal( Model m, String s )
	{ return rdfNode( m, s ).as( Literal.class ); }

	/**
        Create an array of Statements parsed from a semi-separated string.

        @param m a model to serve as a statement factory
        @param facts a sequence of semicolon-separated "S P O" facts
        @return a Statement[] of the (S P O) statements from the string
	 */
	public static Statement [] statements( Model m, String facts )
	{
		ArrayList<Statement> sl = new ArrayList<>();
		StringTokenizer st = new StringTokenizer( facts, ";" );
		while (st.hasMoreTokens()) sl.add( statement( m, st.nextToken() ) );
		return sl.toArray( new Statement[sl.size()] );
	}

	/**
        Create an array of Resources from a whitespace-separated string

        @param m a model to serve as a resource factory
        @param items a whitespace-separated sequence to feed to resource
        @return a RDFNode[] of the parsed resources
	 */
	public static Resource [] resources( Model m, String items )
	{
		ArrayList<Resource> rl = new ArrayList<>();
		StringTokenizer st = new StringTokenizer( items );
		while (st.hasMoreTokens()) rl.add( resource( m, st.nextToken() ) );
		return rl.toArray( new Resource[rl.size()] );
	}

	/**
        Answer the set of resources given by the space-separated
        <code>items</code> string. Each resource specification is interpreted
        as per <code>resource</code>.
	 */
	public static Set<Resource> resourceSet( String items )
	{
		Set<Resource> result = new HashSet<>();
		StringTokenizer st = new StringTokenizer( items );
		while (st.hasMoreTokens()) result.add( resource(  st.nextToken() ) );
		return result;
	}

	/**
        add to a model all the statements expressed by a string.

        @param m the model to be updated
        @param facts a sequence of semicolon-separated "S P O" facts
        @return the updated model
	 */
	public static Model modelAdd( Model m, String facts )
	{
		StringTokenizer semis = new StringTokenizer( facts, ";" );
		while (semis.hasMoreTokens()) m.add( statement( m, semis.nextToken() ) );
		return m;
	}

	/**
        makes a model with a given reiifcation style, initialised with statements parsed
        from a string.

        @param facts a string in semicolon-separated "S P O" format
        @return a model containing those facts
	 */
	public static Model modelWithStatements( AbstractModelTestBase base, String facts )
	{ return modelAdd( createModel( base ), facts ); }

	/**
        make a model and give it Extended prefixes
	 */
	public static Model createModel( AbstractModelTestBase base )
	{
		Model result = base.createModel();
		result.setNsPrefixes( PrefixMapping.Extended );
		return result;
	}

	/**
        Answer a default model; it exists merely to abbreviate the rather long explicit
        invocation.

     	@return a new default [aka memory-based] model
	 */
	//public static Model createMemModel()
	//     { return ModelFactory.createDefaultModel(); }

	/**
        test that two models are isomorphic and fail if they are not.

        @param title a String appearing at the beginning of the failure message
        @param wanted the model value that is expected
        @param got the model value to check
        @exception junit.framework.AssertionFailedError the models are not isomorphic
	 */
	public static void assertIsoModels( String title, Model wanted, Model got )
	{
		if (wanted.isIsomorphicWith( got ) == false)
		{
			Map<Node, Object> map = CollectionFactory.createHashedMap();
			fail( title + ": expected " + nice( wanted.getGraph(), map ) + "\n but had " + nice( got.getGraph(), map ) );
		}
	}

	/**
        Fail if the two models are not isomorphic. See assertIsoModels(String,Model,Model).
	 */
	public static  void assertIsoModels( Model wanted, Model got )
	{ assertIsoModels( "models must be isomorphic", wanted, got ); }

}
