/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Query.java,v 1.6 2003-06-06 09:15:48 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.mem.*;

import java.util.*;

import com.hp.hpl.jena.util.iterator.*;

/**
	The class of graph queries. 
<br>
	@author hedgehog
*/

public class Query 
	{   
    public static final String anon = "<this>";   
     
    public static final Node ANY = Node.ANY;
    
    public static final Node S = Node.createVariable( "S" );
    public static final Node P = Node.createVariable( "P" );
    public static final Node O = Node.createVariable( "O" );
    public static final Node X = Node.createVariable( "X" );
    public static final Node Y = Node.createVariable( "Y" );
    public static final Node Z = Node.createVariable( "Z" );
    
    public static final Node NE = GraphTestBase.node( "&ne" );
        
	public Query()
		{
		}
		
    public Query( Graph pattern )
        { 
        addMatches( pattern );
        }
        
    public Query addMatch( Node S, Node P, Node O )
        { return addNamedMatch( anon, S, P, O ); }     
        
    public Query addMatch( Triple t )
        { return addNamedMatch( anon, t ); }
        
    public Query addMatch( String name, Node S, Node P, Node O )
        { return addNamedMatch( name, S, P, O ); }   
   
    public Query addConstraint( Node S, Node P, Node O )
        { 
        constraintGraph.add( new Triple( S, P, O ) ); 
        return this;
        }
        
    public Query addConstraint( Graph g )
        {
        ClosableIterator it = GraphUtil.findAll( g );
        while (it.hasNext()) constraintGraph.add( (Triple) it.next() );
        return this;
        }
                
    private void addMatches( Graph p )
        {
        ClosableIterator it = GraphUtil.findAll( p );
        while (it.hasNext()) addMatch( (Triple) it.next() );
        }

    public ExtendedIterator executeBindings( Graph g, Node [] results )
        { return executeBindings( args().put( anon, g ), results ); }
                
    /**
        the standard "default" implementation of executeBindings.
    */
    public ExtendedIterator executeBindings( ArgMap args, Node [] nodes )
        {
        Pipe result = new BufferPipe();
        Mapping map = new Mapping();
        ArrayList stages = new ArrayList();
        addStages( stages, args, map );
        stages.add( new ConstraintStage( map, constraintGraph ) );
        final int [] indexes = findIndexes( map, nodes );
        return filter( indexes, connectStages( stages ).deliver( result ) );
        }

    private int [] findIndexes( Mapping map, Node [] nodes )
        {
        int [] result = new int [nodes.length];
        for (int i = 0; i < nodes.length; i += 1) result[i] = findIndex( map, nodes[i] ); 
        return result;
        }
         
    private int findIndex( Mapping map, Node node )
        {
        if (map.maps( node ) == false) map.newIndex( node );
        return map.indexOf( node );
        }
        
    private ExtendedIterator filter( final int [] indexes, final Pipe complete )
        {
        return new NiceIterator()
            {
            public Object next() { return filter( indexes, complete.get() ); }
            public boolean hasNext() { return complete.hasNext(); }
            };
        }
        
    private Domain filter( int [] indexes, Domain complete )
        {
        Domain d = new Domain( new Object[indexes.length] );
        for (int i = 0; i < indexes.length; i += 1) 
            d.setElement( i, complete.get( indexes[i] ) );
        return d;
        }     
                          
    /** collection of triple patterns, graph name -> Cons[Triple] */
    private HashMap triples = new HashMap();
    
    /** the combined constraint graph */
    private Graph constraintGraph = new GraphMem();
    
    /** mapping of graph name -> graph */
    private ArgMap argMap = new ArgMap();
            
    /**
        a mapping from from names to Graphs
    */
    public static class ArgMap
        {
        private HashMap map = new HashMap();    
        ArgMap() {}      
        public ArgMap put( String name, Graph g ) { map.put( name, g ); return this; }       
        public Graph get( String name ) { return (Graph) map.get( name ); } 
        }
        
    public ArgMap args()
        { return argMap; }
        
    private static class Cons
        {
        Triple head;
        Cons tail;
        Cons( Triple head, Cons tail ) { this.head = head; this.tail = tail; }
        static int size( Cons L ) { int n = 0; while (L != null) { n += 1; L = L.tail; } return n; }
        }
        
    private Query addNamedMatch( String name, Node S, Node P, Node O )
        {
        return addNamedMatch( name, new Triple( S, P, O ) );
        }
        
    private Query addNamedMatch( String name, Triple pattern )
    	{
        triples.put( name, new Cons( pattern, (Cons) triples.get( name ) ) );
    	return this;
    	}
              
    private void addStages( ArrayList stages, ArgMap arguments, Mapping map )
        {
        Iterator it2 = triples.entrySet().iterator();
        while (it2.hasNext())
            {
            Map.Entry e = (Map.Entry) it2.next();
            String name = (String) e.getKey();
            Cons triples = (Cons) e.getValue();
            Graph g = (Graph) arguments.get( name );
            int nBlocks = Cons.size( triples ), i = 0;
            Triple [] nodes = new Triple[nBlocks];
            while (triples != null)
                {
                nodes[i++] = triples.head;
                triples = triples.tail;
                }
            Stage next = g.queryHandler().patternStage( map, constraintGraph, nodes );
            stages.add( next );
            }
        }
        
    private Stage connectStages( ArrayList stages )
        {
        Stage current = Stage.initial();
        for (int i = 0; i < stages.size(); i += 1)
            current = ((Stage) stages.get( i )).connectFrom( current );
        return current;
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
