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
import org.apache.jena.rdf.model.RDFNode;
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
