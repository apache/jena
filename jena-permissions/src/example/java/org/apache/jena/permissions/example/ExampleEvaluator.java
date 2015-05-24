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
package org.apache.jena.permissions.example;

import java.security.Principal;
import java.util.Set;

import org.apache.http.auth.BasicUserPrincipal;
import org.apache.jena.permissions.SecurityEvaluator;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

/**
 * An example evaluator that only provides access to messages in the graph that 
 * are from or to the principal.
 *
 */
public class ExampleEvaluator implements SecurityEvaluator {
	
	private Principal principal;
	private Model model;
	private RDFNode msgType = ResourceFactory.createResource( "http://example.com/msg" );
	private Property pTo = ResourceFactory.createProperty( "http://example.com/to" );
	private Property pFrom = ResourceFactory.createProperty( "http://example.com/from" );
	
	/**
	 * 
	 * @param model The graph we are going to evaluate against.
	 */
	public ExampleEvaluator( Model model )
	{
		this.model = model;
	}
	
	@Override
	public boolean evaluate(Object principal, Action action, SecNode graphIRI) {
		// we allow any action on a graph.
		return true;
	}

	private boolean evaluate( Object principalObj, Resource r )
	{
		Principal principal = (Principal)principalObj;
		// a message is only available to sender or recipient
		if (r.hasProperty( RDF.type, msgType ))
		{
			return r.hasProperty( pTo, principal.getName() ) ||
					r.hasProperty( pFrom, principal.getName());
		}
		return true;	
	}
	
	private boolean evaluate( Object principal, SecNode node )
	{
		if (node.equals( SecNode.ANY )) {
			return false;  // all wild cards are false
		}
		
		if (node.getType().equals( SecNode.Type.URI)) {
			Resource r = model.createResource( node.getValue() );
			return evaluate( principal, r );
		}
		else if (node.getType().equals( SecNode.Type.Anonymous)) {
			Resource r = model.getRDFNode( NodeFactory.createAnon( new AnonId( node.getValue()) ) ).asResource();
			return evaluate( principal, r );
		}
		else
		{
			return true;
		}

	}
	
	private boolean evaluate( Object principal, SecTriple triple ) {
		return evaluate( principal, triple.getSubject()) &&
				evaluate( principal, triple.getObject()) &&
				evaluate( principal, triple.getPredicate());
	}
	
	@Override
	public boolean evaluate(Object principal, Action action, SecNode graphIRI, SecTriple triple) {
		return evaluate( principal, triple );
	}

	@Override
	public boolean evaluate(Object principal, Set<Action> actions, SecNode graphIRI) {
		return true;
	}

	@Override
	public boolean evaluate(Object principal, Set<Action> actions, SecNode graphIRI,
			SecTriple triple) {
		return evaluate( principal, triple );
	}

	@Override
	public boolean evaluateAny(Object principal, Set<Action> actions, SecNode graphIRI) {
		return true;
	}

	@Override
	public boolean evaluateAny(Object principal, Set<Action> actions, SecNode graphIRI,
			SecTriple triple) {
		return evaluate( principal, triple );
	}

	@Override
	public boolean evaluateUpdate(Object principal, SecNode graphIRI, SecTriple from, SecTriple to) {
		return evaluate( principal, from ) && evaluate( principal, to );
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
