/**
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
package org.apache.jena.security.example;

import java.util.Set;

import org.apache.jena.security.SecurityEvaluator;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Class to use Shiro to provide credentials.
 * Used for same example as ExampleEvaluator
 *
 */
public class ShiroExampleEvaluator implements SecurityEvaluator {

	private static final Logger LOG = LoggerFactory.getLogger(ShiroExampleEvaluator.class);
	private Model model;
	private RDFNode msgType = ResourceFactory.createResource( "http://example.com/msg" );
	private Property pTo = ResourceFactory.createProperty( "http://example.com/to" );
	private Property pFrom = ResourceFactory.createProperty( "http://example.com/from" );
	
	/**
	 * 
	 * @param model The graph we are going to evaluate against.
	 */
	public ShiroExampleEvaluator( Model model )
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
		Subject subject = (Subject)principalObj;
		if (! subject.isAuthenticated())
		{
			LOG.info( "User not authenticated");
			return false;
		}
		// a message is only available to sender or recipient
		LOG.debug( "checking {}", subject.getPrincipal());
		Object principal = subject.getPrincipal();
		if ("admin".equals(principal.toString()))
		{
			return true;
		}
		if (r.hasProperty( RDF.type, msgType ))
		{
			return r.hasProperty( pTo, subject.getPrincipal().toString() ) ||
					r.hasProperty( pFrom, subject.getPrincipal().toString());
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

	@Override
	public Object getPrincipal() {
		return SecurityUtils.getSubject();
	}


}
