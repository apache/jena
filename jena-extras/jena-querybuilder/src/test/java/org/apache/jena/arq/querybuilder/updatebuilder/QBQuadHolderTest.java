package org.apache.jena.arq.querybuilder.updatebuilder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

public class QBQuadHolderTest {
	
	private QBQuadHolder holder;
	
	
	@Test
	public void anonymousTest()
	{
		WhereBuilder builder = new WhereBuilder();
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		builder.addWhere( s, p, o );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );
		builder.addWhere( s2, p2, o2 );

		holder = new QBQuadHolder( builder );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(1)), lst.get(1));
	}

	@Test
	public void anonymousTest_Var()
	{
		WhereBuilder builder = new WhereBuilder();
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		builder.addWhere( s, p, o );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s, p2, o ) );
		tLst.add( new Triple( s2, p2, o2 ) );
		builder.addWhere( s2, p, o2 );

		holder = new QBQuadHolder( builder );

		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(1)), lst.get(1));
	}
	
	@Test
	public void namedTest()
	{
		WhereBuilder builder = new WhereBuilder();
		
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		builder.addGraph( g, s, p, o );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );
		
		builder.addGraph( g, s2, p2, o2 );

		holder = new QBQuadHolder( builder );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( g, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));	
	}

	@Test
	public void namedTest_Var()
	{
		WhereBuilder builder = new WhereBuilder();
		
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		builder.addGraph( g, s, p, o );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s, p2, o ) );
		tLst.add( new Triple( s2, p2, o2 ) );
		
		builder.addGraph( g, s2, p, o2 );

		holder = new QBQuadHolder( builder );

		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( g, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));	
	}

	
	@Test
	public void mixedTest()
	{
		WhereBuilder builder = new WhereBuilder();
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		builder.addWhere( s, p, o );
		
		Node g = NodeFactory.createURI( "g" );
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );
		
		builder.addGraph( g, s2, p2, o2 );

		holder = new QBQuadHolder( builder );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));	
	}
	
	@Test
	public void mixedTest_Var()
	{
		WhereBuilder builder = new WhereBuilder();
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		builder.addWhere( s, p, o );
		
		Node g = NodeFactory.createURI( "g" );
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s, p2, o ) );
		tLst.add( new Triple( s2, p2, o2 ) );
		
		builder.addGraph( g, s2, p, o2 );

		holder = new QBQuadHolder( builder );

		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));	
	}
}
