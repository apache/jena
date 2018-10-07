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
package org.apache.jena.arq.querybuilder.updatebuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;

public class ModelQuadHolderTest {

	private Model model;
	private ModelQuadHolder holder;
	
	@Test
	public void anonymousTest() {
		model = ModelFactory.createDefaultModel();
		
		List<Triple> tLst = new ArrayList<Triple>();
		Resource s = ResourceFactory.createResource( "s" );
		Property p = ResourceFactory.createProperty( "p" );
		Resource o = ResourceFactory.createResource( "o" );
		tLst.add( new Triple( s.asNode(), p.asNode(), o.asNode() ) );
		model.add( s, p, o );
		
		Resource s2 = ResourceFactory.createResource( "s2" );
		Property p2 = ResourceFactory.createProperty( "p2" );
		Resource o2 = ResourceFactory.createResource( "o2" );
		tLst.add( new Triple( s2.asNode(), p2.asNode(), o2.asNode() ) );
		model.add( s2, p2, o2 );
		
		holder = new ModelQuadHolder( model );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		Quad q = new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0));
		assertTrue( "missing "+q, lst.contains( q ));
		
		q = new Quad( Quad.defaultGraphNodeGenerated, tLst.get(1));
		assertTrue( "missing "+q, lst.contains( q ));
	}
	
	@Test
	public void namedTest() {
		model = ModelFactory.createDefaultModel();
		
		Node g = NodeFactory.createURI( "g" );
		List<Triple> tLst = new ArrayList<Triple>();
		Resource s = ResourceFactory.createResource( "s" );
		Property p = ResourceFactory.createProperty( "p" );
		Resource o = ResourceFactory.createResource( "o" );
		tLst.add( new Triple( s.asNode(), p.asNode(), o.asNode() ) );
		model.add( s, p, o );
		
		Resource s2 = ResourceFactory.createResource( "s2" );
		Property p2 = ResourceFactory.createProperty( "p2" );
		Resource o2 = ResourceFactory.createResource( "o2" );
		tLst.add( new Triple( s2.asNode(), p2.asNode(), o2.asNode() ) );
		model.add( s2, p2, o2 );
		
		holder = new ModelQuadHolder( g, model );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		Quad q = new Quad( g, tLst.get(0));
		assertTrue( "missing "+q, lst.contains( q ));
		
		q = new Quad( g, tLst.get(1));
		assertTrue( "missing "+q, lst.contains( q ));
	}
}
