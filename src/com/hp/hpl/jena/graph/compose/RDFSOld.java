/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: RDFSOld.java,v 1.4 2003-06-10 10:45:24 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.mem.*;

import java.util.*;

/**
	@author kers
<br>
    new RDFSOld( G ) is a new graph which is the RDFSOld closure of the
    graph G.
    
    type @type Property
    
    if x @r y then r @type Property
    
*/

public class RDFSOld extends Dyadic implements Vocabulary
    {
    private Graph properties;
    
    public RDFSOld( Graph L )
        {
        super( L, GraphTestBase.graphWith( "" ) );
        properties = extractProperties( L );
        }
        
    private Graph extractProperties( Graph x )
    	{
    	Graph result = new GraphMem();
    	Query q = new Query();
    	// Variable P = q.variable( "p" );
        Node P = Node.createVariable( "?p" ); 
    	ExtendedIterator it = q.addMatch( Query.ANY, P, Query.ANY ).executeBindings( x, new Node [] {P} );
    	while (it.hasNext())
    		{
    		Domain d = (Domain) it.next();
    		Node n = (Node) d.get( 0 );
    		result.add( new Triple( n, rdfType, rdfProperty ) );
    		}
    	return result;
    	}

	protected void addSubThing( Triple t, Node property )
		{
		Node p = t.getPredicate();
		// System.err.println( "| addSubProperty( " + t + " )" );
		if (p.equals( property ))
			{
			ArrayList pending = new ArrayList();
			Node s = t.getSubject(), o = t.getObject();
			Query q = new Query();
    		// Variable X = q.variable( "x" ), Y = q.variable( "Y" );
            Node X = Node.createVariable( "?x" ); 
            Node Y = Node.createVariable( "?y" ); 
    		ExtendedIterator it = q.addMatch( X, property, Y ).executeBindings( properties, new Node [] {X, Y} );	
    		while (it.hasNext())
    			{
    			Domain xy = (Domain) it.next();
    			Node x = (Node) xy.get(0), y = (Node) xy.get(1);
    			// System.err.println( "| found: " + x + " @sub " + y + " when doing " + t );
    			// System.err.println( "| y.equals(s): " + y.equals( s ) );
    			// System.err.println( "| o.equals(x): " + o.equals( x ) );
    			if (y.equals( s )) pending.add( new Triple( x, property, o ) );
    			if (o.equals( x )) pending.add( new Triple( s, property, y ) );
    			}		
    		// System.err.println( "| got " + pending.size() + " (+1) things to add" );
			for (int i = 0; i < pending.size(); i += 1) 
				{
				// System.err.println( "+ viz: " + pending.get(i) );
				properties.add( (Triple) pending.get( i ) );
				}
			// properties.add( t );
			// Useful.printGraph( System.err, properties );
			}
		properties.add( t );
		}
		
	protected void addSubProperty( Triple t )
		{
		addSubThing( t, rdfsSubPropertyOf );
		}
		
	protected void addSubClass( Triple t )
		{
		addSubThing( t, rdfsSubClassOf );
		}
		
	private HashSet setWithResource()
		{
		HashSet result = new HashSet();
		result.add( rdfsResource );
		return result;
		}
		
	private void addSuper( HashMap result, Triple t )
		{ // t = x @subClassOf y
		Node sub = t.getSubject(), sup = t.getObject();
		if (result.containsKey( sub ) == false) result.put( sub, setWithResource() );
		HashSet supers = (HashSet) result.get( sub );
		supers.add( sup );
		}
		
	/** map from each class to all its superclasses */        
	public HashMap superClasses()
		{
		HashMap result = new HashMap();
		ExtendedIterator it = find( null, rdfsSubClassOf, null );
		while (it.hasNext()) addSuper( result, (Triple) it.next() ); 
		return result;
		}
		

    public void add( Triple t )
        {
        // System.err.println( "| add( " + t + " )" );
        L.add( t );
        addSubProperty( t );
        addSubClass( t );
        properties.add( new Triple( t.getPredicate(), rdfType, rdfProperty ) );
        }

    public void delete( Triple t )
        {
        }

	private boolean wouldMatch( Triple m, Node s, Node p, Node o )
		{
		return 
            (s == null || m.subjectMatches( s ))
            && (p == null || m.predicateMatches( p ))
            && (o == null || m.objectMatches( o ));
		}
		
	protected ExtendedIterator getTheseResources( Node X, Node S, Node P, Node O )
		{
        Map1 map = new Map1() {public Object map1( Object x ) { return ((Domain) x).get(0); }};
        Query q = new Query();
        return q.addMatch( S, P, O ).executeBindings( L, new Node [] {X} ) .mapWith( map );
        }
		
	protected ExtendedIterator getObjectResources()
		{
        Node O = Node.createVariable( "o" );
        return getTheseResources( O, Query.ANY, Query.ANY, O );
		}

	protected ExtendedIterator getSubjectResources()
		{
        Node S = Node.createVariable( "s" );
        return getTheseResources( S, S, Query.ANY, Query.ANY );
		}
		
    private static class AcceptTriples implements Filter
        {
        private Triple m;
        
        public AcceptTriples( Triple m )
            { this.m = m; }
            
        public boolean accept( Object o )
            {
            return m.matches( (Triple) o );
            }
        }
        
    private static class TypeAsResource implements Map1
        {
        public Object map1( Object x )
            { return new Triple( (Node) x, rdfType, rdfsResource ); }
        }
        
	protected ExtendedIterator mapAsResources( Triple m, ExtendedIterator nodes )
		{
        return nodes.mapWith( new TypeAsResource() ) .filterKeep( new AcceptTriples( m ) );
		}
		
    public static Triple typeIt( Domain d )	
    	{
    	return new Triple( (Node) d.get(0), RDFSOld.rdfType, (Node) d.get(1) );
    	}
 
    private static final Node XX = Node.createVariable( "x" );
    
    public static ExtendedIterator typedBy( Graph g, Node S, Node filter, Node O )
    	{
    	Query q = new Query();
        Node PP = Node.createVariable( "p" ), TT = Node.createVariable( "t" );
        q.addMatch( S, PP, O );
        q.addMatch( PP, filter, TT );
        Map1 map = new Map1() {public Object map1( Object x ) { return typeIt( (Domain) x ); }};
        return q.executeBindings( g, new Node [] {XX, TT} ) .mapWith ( map );
    	}       
   	
    public static ExtendedIterator typedByDomain( Graph g )
    	{
    	return typedBy( g, XX, RDFSOld.rdfsDomain, Query.ANY );
    	}       
    	
    public static ExtendedIterator typedByRange( Graph g )
    	{
    	return typedBy( g, Query.ANY, RDFSOld.rdfsRange, XX );
    	}
        
    public ExtendedIterator typedBySubclass( final Triple m, Graph g )
    	{
    	final HashMap supers = superClasses();
        TripleMatch matcher = new StandardTripleMatch( m.getSubject(), rdfType, null );
        Filter isSuper = new Filter()
            {
            public boolean accept( Object x )
                { return supers.containsKey( ((Triple) x).getObject() ); }
            };
    	MapFiller sm = new MapFiller()
    		{
    		public boolean refill( Object x, ArrayList a )
    			{
    			Triple t = (Triple) x;
    			Node S = t.getSubject(), P = t.getPredicate() ,ty = t.getObject();
    			for (Iterator it = ((HashSet) supers.get( ty )).iterator(); it.hasNext(); a.add( new Triple( S, P, (Node) it.next() ) )) {}
    			return true;
    			}
    		};
    	return new MapMany( g.find( matcher ) .filterKeep ( isSuper ), sm );
    	}
    	
        public ExtendedIterator find(final TripleMatch m)
        {
       	ExtendedIterator basic = properties.find( m ).andThen( rdfsAxioms.find( m ) ).andThen( L.find( m ) );
        Triple tm = m.asTriple();
        if (wouldMatch( tm, null, rdfType, null ))
            if (m instanceof StandardTripleMatch)
                {
                Filter FF = new AcceptTriples( tm );
                ExtendedIterator t12 = typedByDomain( new Union( properties, L ) );
                ExtendedIterator r12 = typedByRange( new Union( properties, L ) );
                ExtendedIterator s = typedBySubclass( tm, new Union( properties, L ) );
            /* */
                ExtendedIterator typed = t12.filterKeep( FF ) .andThen ( r12.filterKeep( FF ) );
                basic = basic.andThen( typed ) .andThen( s.filterKeep( FF ) );
                }
            else
                {
                throw new RuntimeException( "only StandardTripleMatch works" );
                }
        if (wouldMatch( tm, null, rdfType, rdfsResource ))
        	{ /* have to deliver all resource-type things */
            // System.err.println( "+  hairy " + m );
        	ExtendedIterator subjects = getSubjectResources();
        	ExtendedIterator objects = getObjectResources();
            ExtendedIterator predicates = getTheseResources( Query.P, Query.ANY, Query.P, Query.ANY );
        	ExtendedIterator typedResource = mapAsResources( tm, subjects.andThen( objects ).andThen( predicates ) );
        	return basic.andThen( typedResource );
        	}
        else
        	return basic;
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
