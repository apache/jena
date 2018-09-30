package org.apache.jena.arq.querybuilder.updatebuilder;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.SingletonIterator;
import org.junit.Test;

public class SingleQuadHolderTest {
	
	private SingleQuadHolder holder;
	
	@Test
	public void getQuads_Quad()
	{
		Node g = NodeFactory.createURI( "g" );
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		Quad quad = new Quad( g, s, p, o );
		holder = new SingleQuadHolder( quad );
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 1, lst.size() );
		assertEquals( quad, lst.get(0));
	}

	
	@Test
	public void getQuads_Quad_vars()
	{
		Node g = NodeFactory.createURI( "g" );
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		Quad quad = new Quad( g, s, p, o );
		holder = new SingleQuadHolder( quad );
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 1, lst.size() );
		assertEquals( quad, lst.get(0));
		
		Map<Var,Node> map = new HashMap<>();
		Node p2 = NodeFactory.createURI( "p2" );
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		Quad quad2 = new Quad( g, s, p2, o );
		lst = holder.getQuads().toList();
		assertEquals( 1, lst.size() );
		assertEquals( quad2, lst.get(0));
	}

	@Test
	public void getQuads_Triple()
	{
		
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		Triple triple = new Triple( s, p, o );
		Quad quad = new Quad( Quad.defaultGraphNodeGenerated,  s, p, o );
		holder = new SingleQuadHolder( triple );
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 1, lst.size() );
		assertEquals( quad, lst.get(0));
	}

	
	@Test
	public void getQuads_Triple_vars()
	{
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		Triple triple = new Triple( s, p, o );
		Quad quad = new Quad( Quad.defaultGraphNodeGenerated,  s, p, o );
		holder = new SingleQuadHolder( triple );
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 1, lst.size() );
		assertEquals( quad, lst.get(0));
		
		Map<Var,Node> map = new HashMap<>();
		Node p2 = NodeFactory.createURI( "p2" );
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		Quad quad2 = new Quad( Quad.defaultGraphNodeGenerated, s, p2, o );
		lst = holder.getQuads().toList();
		assertEquals( 1, lst.size() );
		assertEquals( quad2, lst.get(0));
	}

}
