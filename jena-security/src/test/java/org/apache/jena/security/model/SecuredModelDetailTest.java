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
package org.apache.jena.security.model;

import java.net.URL;
import java.security.Principal;
import java.util.Set;

import org.apache.http.auth.BasicUserPrincipal;
import org.apache.jena.security.Factory;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.model.SecuredModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Tests secured model functions against graph where only partial data is 
 * available to the user.
 *
 */
public class SecuredModelDetailTest {

	private static String NS_FMT = "http://example.com/%s";
	private Model baseModel;
	private SecuredModel securedModel;
	private DetailEvaluator secEval;
	private Property pTo = ResourceFactory.createProperty("http://example.com/to");
	private Property pFrom = ResourceFactory
			.createProperty( "http://example.com/from");
	private Property pSubj = ResourceFactory
			.createProperty("http://example.com/subj");
	
	@Before
	public void setup()
	{
		baseModel = ModelFactory.createDefaultModel();
		baseModel.removeAll();
		URL url = SecuredModelDetailTest.class.getClassLoader().getResource( "org/apache/jena/security/model/detail.ttl");
		baseModel.read( url.toExternalForm() );
		secEval = new DetailEvaluator( baseModel );
		securedModel = Factory.getInstance(secEval,
				"http://example.com/detailModelTest", baseModel);
	}


	@Test
	public void testContains()
	{
		secEval.setPrincipal("darla");
		/* darla can only add values to msg4
		 ex:msg4  rdf:type ex:msg; 
			ex:to "darla" ;
			ex:from "bob" ;
			ex:subj "bob to darla 1"
		 */
		
		Resource s = ResourceFactory.createResource( String.format( NS_FMT, "msg3") );
		Assert.assertTrue( "should contain msg3", baseModel.contains( s, null ));
		Assert.assertFalse( "should not see msg3", securedModel.contains( s, null ));
		Assert.assertTrue( "Sould contain a resource msg3", baseModel.containsResource( s));
		Assert.assertFalse( "Should not contain a resource msg3'", securedModel.containsResource( s) );
		
		s = ResourceFactory.createResource( String.format( NS_FMT, "msg4") );
		Assert.assertTrue( "should contain msg4", baseModel.contains( s, null ));
		Assert.assertTrue( "should see msg4", securedModel.contains( s, null ));
		Assert.assertTrue( "Sould contain a resource msg4", baseModel.containsResource( s));
		Assert.assertTrue( "Should contain a resource msg4'", securedModel.containsResource( s) );
		
		Assert.assertTrue( "Sould contain a to 'bob'", baseModel.contains( null, pTo, "bob"));
		Assert.assertFalse( "Should not see to 'bob'", securedModel.contains( null, pTo, "bob") );
		
		Assert.assertTrue( "Sould contain a from 'bob'", baseModel.contains( null, pFrom, "bob"));
		Assert.assertTrue( "Should see from 'bob'", securedModel.contains( null, pFrom, "bob") );

	}
	
	@Test
	public void testListObjects()
	{
		secEval.setPrincipal("darla");
		/* darla can only add values to msg4
		 ex:msg4  rdf:type ex:msg; 
			ex:to "darla" ;
			ex:from "bob" ;
			ex:subj "bob to darla 1"
		 */
		
		Assert.assertTrue( baseModel.listObjects().toList().size() > 4);
		Assert.assertEquals( 4,  securedModel.listObjects().toList().size());
		
		Assert.assertTrue( baseModel.listObjectsOfProperty( pFrom ).toList().size() > 1);
		Assert.assertEquals( 1, securedModel.listObjectsOfProperty( pFrom ).toList().size());
		
		Resource s = ResourceFactory.createResource( String.format( NS_FMT, "msg3"));
		Assert.assertEquals( 1, baseModel.listObjectsOfProperty( s, pFrom).toList().size());
		Assert.assertEquals( 0, securedModel.listObjectsOfProperty( s, pFrom ).toList().size());
		
		s = ResourceFactory.createResource( String.format( NS_FMT, "msg4"));
		Assert.assertEquals( 1, baseModel.listObjectsOfProperty( s, pFrom).toList().size());
		Assert.assertEquals( 1, securedModel.listObjectsOfProperty( s, pFrom ).toList().size());
	}
	
	@Test
	public void testListResources() {
		secEval.setPrincipal("darla");
		/* darla can only add values to msg4
		 ex:msg4  rdf:type ex:msg; 
			ex:to "darla" ;
			ex:from "bob" ;
			ex:subj "bob to darla 1"
		 */
		Assert.assertEquals( 5, baseModel.listResourcesWithProperty( pFrom ).toList().size());
		Assert.assertEquals( 1, securedModel.listResourcesWithProperty( pFrom ).toList().size());
		
		RDFNode o = ResourceFactory.createPlainLiteral("bob");
		Assert.assertEquals( 3, baseModel.listResourcesWithProperty( pFrom, o ).toList().size());
		Assert.assertEquals( 1, securedModel.listResourcesWithProperty( pFrom, o ).toList().size());
		Assert.assertEquals( 1, baseModel.listResourcesWithProperty( pTo, o ).toList().size());
		Assert.assertEquals( 0, securedModel.listResourcesWithProperty( pTo, o ).toList().size());
		
		Assert.assertEquals( 4, baseModel.listResourcesWithProperty( null, o ).toList().size());
		Assert.assertEquals( 1, securedModel.listResourcesWithProperty( null, o ).toList().size());
		
		o = ResourceFactory.createPlainLiteral("alice");
		Assert.assertEquals( 4, baseModel.listResourcesWithProperty( null, o ).toList().size());
		Assert.assertEquals( 0, securedModel.listResourcesWithProperty( null, o ).toList().size());	
	}
	
	@Test
	public void testListStatements()
	{
		secEval.setPrincipal("darla");
		/* darla can only add values to msg4
		 ex:msg4  rdf:type ex:msg; 
			ex:to "darla" ;
			ex:from "bob" ;
			ex:subj "bob to darla 1"
		 */
		Assert.assertEquals( 20, baseModel.listStatements().toList().size());
		Assert.assertEquals( 4, securedModel.listStatements().toList().size());
		
		RDFNode o = ResourceFactory.createPlainLiteral("bob");
		Assert.assertEquals( 1, baseModel.listStatements( null, pTo, o).toList().size());
		Assert.assertEquals( 0, securedModel.listStatements( null, pTo, o).toList().size());
		Assert.assertEquals( 3, baseModel.listStatements( null, pFrom, o).toList().size());
		Assert.assertEquals( 1, securedModel.listStatements( null, pFrom, o).toList().size());
		
		Resource s = ResourceFactory.createResource( String.format( NS_FMT, "msg3"));
		Assert.assertEquals( 4, baseModel.listStatements( s, null, (RDFNode)null).toList().size());
		Assert.assertEquals( 0, securedModel.listStatements( s, null, (RDFNode)null).toList().size());
		
		Assert.assertEquals( 1, baseModel.listStatements( s, pTo, (RDFNode)null).toList().size());
		Assert.assertEquals( 0, securedModel.listStatements( s, pTo, (RDFNode)null).toList().size());

		Assert.assertEquals( 0, baseModel.listStatements( s, pTo, o).toList().size());
		Assert.assertEquals( 0, securedModel.listStatements( s, pTo, o).toList().size());
		o = ResourceFactory.createPlainLiteral("chuck");
		Assert.assertEquals( 1, baseModel.listStatements( s, pTo, o).toList().size());
		Assert.assertEquals( 0, securedModel.listStatements( s, pTo, o).toList().size());
			
			
		s = ResourceFactory.createResource( String.format( NS_FMT, "msg4"));
		Assert.assertEquals( 4, baseModel.listStatements( s, null, (RDFNode)null).toList().size());
		Assert.assertEquals( 4, securedModel.listStatements( s, null, (RDFNode)null).toList().size());
		
		Assert.assertEquals( 1, baseModel.listStatements( s, pTo, (RDFNode)null).toList().size());
		Assert.assertEquals( 1, securedModel.listStatements( s, pTo, (RDFNode)null).toList().size());

		Assert.assertEquals( 0, baseModel.listStatements( s, pTo, o).toList().size());
		Assert.assertEquals( 0, securedModel.listStatements( s, pTo, o).toList().size());
		o = ResourceFactory.createPlainLiteral("darla");
		Assert.assertEquals( 1, baseModel.listStatements( s, pTo, o).toList().size());
		Assert.assertEquals( 1, securedModel.listStatements( s, pTo, o).toList().size());
	}
	
	@Test
	public void testListSubjects()
	{
		secEval.setPrincipal("darla");
		/* darla can only add values to msg4
		 ex:msg4  rdf:type ex:msg; 
			ex:to "darla" ;
			ex:from "bob" ;
			ex:subj "bob to darla 1"
		 */
		Assert.assertEquals( 5, baseModel.listSubjects().toList().size());
		Assert.assertEquals( 1, securedModel.listSubjects().toList().size());
		
		Assert.assertEquals( 5, baseModel.listSubjectsWithProperty( pTo ).toList().size());
		Assert.assertEquals( 1, securedModel.listSubjectsWithProperty( pTo ).toList().size());
		
		RDFNode o = ResourceFactory.createPlainLiteral("darla");
		Assert.assertEquals( 1, baseModel.listSubjectsWithProperty( pTo, o ).toList().size());
		Assert.assertEquals( 1, securedModel.listSubjectsWithProperty( pTo, o ).toList().size());
		
		o = ResourceFactory.createPlainLiteral("bob");
		Assert.assertEquals( 1, baseModel.listSubjectsWithProperty( pTo, o ).toList().size());
		Assert.assertEquals( 0, securedModel.listSubjectsWithProperty( pTo, o ).toList().size());
	
		Assert.assertEquals( 4, baseModel.listSubjectsWithProperty( null, o ).toList().size());
		Assert.assertEquals( 1, securedModel.listSubjectsWithProperty( null, o ).toList().size());
		
	}
	
	/**
	 * An example evaluator that only provides access ot messages in the graph that 
	 * are from or to the principal.
	 *
	 */
	private class DetailEvaluator implements SecurityEvaluator {
		
		private Principal principal;
		private Model model;
		private RDFNode msgType = ResourceFactory.createResource( "http://example.com/msg" );
		private Property pTo = ResourceFactory.createProperty( "http://example.com/to" );
		private Property pFrom = ResourceFactory.createProperty( "http://example.com/from" );
		
		/**
		 * 
		 * @param model The graph we are going to evaluate against.
		 */
		public DetailEvaluator( Model model )
		{
			this.model = model;
		}
		
		@Override
		public boolean evaluate(Action action, SecNode graphIRI) {
			// we allow any action on a graph.
			return true;
		}

		private boolean evaluate( Resource r )
		{
			// a message is only available to sender or recipient
			if (r.hasProperty( RDF.type, msgType ))
			{
				return r.hasProperty( pTo, principal.getName() ) ||
						r.hasProperty( pFrom, principal.getName());
			}
			return true;	
		}
		
		private boolean evaluate( SecNode node )
		{
			if (node.equals( SecNode.ANY )) {
				return false;  // all wild cards are false
			}
			
			if (node.getType().equals( SecNode.Type.URI)) {
				Resource r = model.createResource( node.getValue() );
				return evaluate( r );
			}
			else if (node.getType().equals( SecNode.Type.Anonymous)) {
				Resource r = model.getRDFNode( NodeFactory.createAnon( new AnonId( node.getValue()) ) ).asResource();
				return evaluate( r );
			}
			else
			{
				return true;
			}

		}
		
		private boolean evaluate( SecTriple triple ) {
			return evaluate( triple.getSubject()) &&
					evaluate( triple.getObject()) &&
					evaluate( triple.getPredicate());
		}
		
		@Override
		public boolean evaluate(Action action, SecNode graphIRI, SecTriple triple) {
			return evaluate( triple );
		}

		@Override
		public boolean evaluate(Set<Action> actions, SecNode graphIRI) {
			return true;
		}

		@Override
		public boolean evaluate(Set<Action> actions, SecNode graphIRI,
				SecTriple triple) {
			return evaluate( triple );
		}

		@Override
		public boolean evaluateAny(Set<Action> actions, SecNode graphIRI) {
			return true;
		}

		@Override
		public boolean evaluateAny(Set<Action> actions, SecNode graphIRI,
				SecTriple triple) {
			return evaluate( triple );
		}

		@Override
		public boolean evaluateUpdate(SecNode graphIRI, SecTriple from, SecTriple to) {
			return evaluate( from ) && evaluate( to );
		}

		public void setPrincipal( String userName )
		{
			if (userName == null)
			{
				principal = null;
			}
			principal = new BasicUserPrincipal( userName );
		}
		@Override
		public Principal getPrincipal() {
			return principal;
		}

	}

}
