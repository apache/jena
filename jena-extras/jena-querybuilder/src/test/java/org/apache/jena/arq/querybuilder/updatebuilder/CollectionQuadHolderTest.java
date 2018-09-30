package org.apache.jena.arq.querybuilder.updatebuilder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.junit.Test;

public class CollectionQuadHolderTest {
	
	private CollectionQuadHolder holder;
	
	@Test
	public void namedGraphTest_List()
	{
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );

		holder = new CollectionQuadHolder( g, tLst );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( g, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));
	}
	
	@Test
	public void namedGraphTest_List_Var()
	{
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p, o2 ) );

		holder = new CollectionQuadHolder( g, tLst );
		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Triple> aLst = new ArrayList<Triple>();
		aLst.add( new Triple( s, p2, o ) );
		aLst.add( new Triple( s2, p2, o2 ) );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( g, aLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, aLst.get(1)), lst.get(1));
	}

	@Test
	public void namedGraphTest_Iterator()
	{
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );

		holder = new CollectionQuadHolder( g, tLst.iterator() );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( g, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, tLst.get(1)), lst.get(1));
	}
	
	@Test
	public void namedGraphTest_Iterator_Var()
	{
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p, o2 ) );

		holder = new CollectionQuadHolder( g, tLst.iterator() );
		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Triple> aLst = new ArrayList<Triple>();
		aLst.add( new Triple( s, p2, o ) );
		aLst.add( new Triple( s2, p2, o2 ) );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( g, aLst.get(0)), lst.get(0));
		assertEquals( new Quad( g, aLst.get(1)), lst.get(1));
	}



	@Test
	public void anonymousGraphTest_List()
	{
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );

		holder = new CollectionQuadHolder( tLst );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(1)), lst.get(1));
	}
	
	@Test
	public void anonymousGraphTest_List_Var()
	{		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p, o2 ) );

		holder = new CollectionQuadHolder( tLst );
		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Triple> aLst = new ArrayList<Triple>();
		aLst.add( new Triple( s, p2, o ) );
		aLst.add( new Triple( s2, p2, o2 ) );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, aLst.get(0)), lst.get(0));
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, aLst.get(1)), lst.get(1));
	}

	
	@Test
	public void anonymousGraphTest_Iterator()
	{
		Node g = NodeFactory.createURI( "g" );
		
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createURI( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p2, o2 ) );

		holder = new CollectionQuadHolder( tLst.iterator() );

		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(0)), lst.get(0));
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, tLst.get(1)), lst.get(1));
	}

	@Test
	public void anonymousGraphTest_Iterator_Var()
	{
		List<Triple> tLst = new ArrayList<Triple>();
		Node s = NodeFactory.createURI( "s" );
		Node p = NodeFactory.createVariable( "p" );
		Node o = NodeFactory.createURI( "o" );
		tLst.add( new Triple( s, p, o ) );
		
		Node s2 = NodeFactory.createURI( "s2" );
		Node p2 = NodeFactory.createURI( "p2" );
		Node o2 = NodeFactory.createURI( "o2" );
		tLst.add( new Triple( s2, p, o2 ) );

		holder = new CollectionQuadHolder( tLst.iterator() );
		Map<Var,Node> map = new HashMap<>();
		map.put( Var.alloc(p), p2);
		holder.setValues( map );
		
		List<Triple> aLst = new ArrayList<Triple>();
		aLst.add( new Triple( s, p2, o ) );
		aLst.add( new Triple( s2, p2, o2 ) );
		
		List<Quad> lst = holder.getQuads().toList();
		assertEquals( 2, lst.size() );
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, aLst.get(0)), lst.get(0));
		assertEquals( new Quad( Quad.defaultGraphNodeGenerated, aLst.get(1)), lst.get(1));
	}

//	@Test
//	public void anonymousGraphTest_var()
//	{
//		Node g = NodeFactory.createURI( "g" );
//		Node s = NodeFactory.createURI( "s" );
//		Node p = NodeFactory.createVariable( "p" );
//		Node o = NodeFactory.createURI( "o" );
//		Triple triple = new Triple( s, p, o );
//	Quad quad = new Quad( Quad.defaultGraphNodeGenerated,  s, p, o );
//	holder = new SingleQuadHolder( triple );
//	List<Quad> lst = holder.getQuads().toList();
//	assertEquals( 1, lst.size() );
//	assertEquals( quad, lst.get(0));
//	
//	Map<Var,Node> map = new HashMap<>();
//	Node p2 = NodeFactory.createURI( "p2" );
//	map.put( Var.alloc(p), p2);
//	holder.setValues( map );
//	Quad quad2 = new Quad( Quad.defaultGraphNodeGenerated, s, p2, o );
//	lst = holder.getQuads().toList();
//	assertEquals( 1, lst.size() );
//	assertEquals( quad2, lst.get(0));
//	
//	 /**
//     * Constructor.
//     * 
//     * @param graph
//     *            the default graph name for the triples
//     * @param triples
//     *            the collection of triples.
//     */
//    public CollectionQuadHolder(final Node graph, Collection<Triple> triples) {
//        this.collection = new HashSet<Triple>();
//        this.collection.addAll( triples );
//        defaultGraphName = graph;
//    }
//
//    /**
//     * Constructor.
//     * 
//     * @param graph
//     *            the default graph name for the triples
//     * @param triples
//     *            the iterator of triples.
//     */
//    public CollectionQuadHolder(final Node graph, Iterator<Triple> triples) {
//        this.collection = WrappedIterator.create( triples ).toSet();
//        defaultGraphName = graph;
//    }
//
//    /**
//     * Constructor. Uses Quad.defaultGraphNodeGenerated for the graph name.
//     * 
//     * @see Quad#defaultGraphNodeGenerated
//     * @param triples
//     *            the collection of triples.
//     */
//    public CollectionQuadHolder(final Collection<Triple> triples) {
//        this( Quad.defaultGraphNodeGenerated, triples );
//    }
//
//    /**
//     * Constructor.
//     * 
//     * @param triples
//     *            the iterator of triples.
//     */
//    public CollectionQuadHolder(Iterator<Triple> triples) {
//        this.collection = WrappedIterator.create( triples ).toSet();
//        defaultGraphName =  Quad.defaultGraphNodeGenerated;
//    }
//
//    @Override
//    public ExtendedIterator<Quad> getQuads() {
//        return WrappedIterator.create(collection.iterator())
//        		.mapWith( triple -> new Quad( defaultGraphName, triple ) );
//    }
//
//    /**
//     * This implementation does nothing.
//     */
//    @Override
//    public QuadHolder setValues(final Map<Var, Node> values) {
//        return this;
//    }
}
