/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Query.java,v 1.17 2003-08-08 14:29:13 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.mem.*;

import com.hp.hpl.jena.shared.*;

import java.util.*;

import com.hp.hpl.jena.util.iterator.*;

/**
	The class of graph queries, plus some machinery (which should move) for
    implementing them.

	@author hedgehog
*/

public class Query 
	{   
    /**
        A more-or-less internal object for referring to the "default" graph in a query.
    */
    public static final String anon = "<this>";   
    
    /**
        A convenient synonym for Node.ANY, used in a match to match anything.
    */ 
    public static final Node ANY = Node.ANY;
    
    /**
        A query variable called "S".
    */
    public static final Node S = Node.createVariable( "S" );
    /**
        A query variable called "P".
    */
    public static final Node P = Node.createVariable( "P" );
    /**
        A query variable called "O".
    */
    public static final Node O = Node.createVariable( "O" );
    /**
        A query variable called "X".
    */
    public static final Node X = Node.createVariable( "X" );
    /**
        A query variable called "Y".
    */
    public static final Node Y = Node.createVariable( "Y" );
    /**
        A query variable called "Z".
    */
    public static final Node Z = Node.createVariable( "Z" );
    
    /**
        The built-in constraint operator not-equals.
    */
    public static final Node NE = GraphTestBase.node( "&ne" );
        
    /**
        Initialiser for Query; makes an empty Query [no matches, no constraints]
    */
	public Query()
		{
		}
        
    /**
        Initialiser for Query; makes a Query with its matches taken from 
        <code>pattern</code>.
        @param pattern a Graph whose triples are used as match elements
    */
    public Query( Graph pattern )
        { 
        addMatches( pattern );
        }

    /**
        Exception thrown when a query variable is discovered to be unbound.
    */
    public static class UnboundVariableException extends JenaException
        { public UnboundVariableException( Node n ) { super( n.toString() ); } }
                        
    /**
        Add an (S, P, O) match to the query's collection of match triples. Return
        this query for cascading.
        @param S the node to match the subject
        @param P the node to match the predicate
        @param O the node to match the object
        @return this Query, for cascading
    */
    public Query addMatch( Node s, Node p, Node o )
        { return addNamedMatch( anon, s, p, o ); }     
        
    /**
        Add a triple to thw query's collection of match triples. Return this query
        for cascading.
        @param t an (S, P, O) triple to add to the collection of matches
        @return this Query, for cascading
    */
    public Query addMatch( Triple t )
        { return addNamedMatch( anon, t ); }
        
    /**
        Add an (S, P, O) match triple to this query to match against the graph labelled
        with <code>name</code>. Return this query for cascading.
        @param name the name that will identify the graph in the matching
        @param S the node to match the subject
        @param P the node to match the predicate
        @param O the node to match the object
        @return this Query, for cascading.
    */
    public Query addMatch( String name, Node s, Node p, Node o )
        { return addNamedMatch( name, s, p, o ); }   
   
    /**
        Add a constraint (S, P, O) to the query constraints. <code>S</code> and
        <code>O</code> are value nodes, either concrete values or variable that
        will be bound to values. <code>P</code> is a constraint predicate name.
        A match fails if the predication (value of S, predicate P, value of O) is false.
        Return this Query for cascading.
        @param S the node representing the left operand of the predicate
        @param P the node identifying the predicate
        @param O the node representing the right operand of the predicate
        @return this query, for cascading
    */
    public Query addConstraint( Node s, Node p, Node o )
        {
        constraintGraph.add( new Triple( s, p, o ) ); 
        return this;
        }
        
    /**
        Add all the constraints encoded by the triples of <code>g</code> to this Query.
        Return this query.
        @param g a graph of (S, P, O) constraint triples to be added to this query
        @return this Query, for cascading
    */
    public Query addConstraint( Graph g )
        {
        ClosableIterator it = GraphUtil.findAll( g );
        while (it.hasNext()) constraintGraph.add( (Triple) it.next() );
        return this;
        }
                
    /**
        Add all the (S, P, O) triples of <code>p</code> to this Query as matches.
    */
    private void addMatches( Graph p )
        {
        ClosableIterator it = GraphUtil.findAll( p );
        while (it.hasNext()) addMatch( (Triple) it.next() );
        }

    public ExtendedIterator executeBindings( Graph g, Node [] results )
        { return executeBindings( args().put( anon, g ), results ); }
                
    public ExtendedIterator executeBindings( Graph g, List stages, Node [] results )
        { return executeBindings( stages, args().put( anon, g ), results ); }
    
    public ExtendedIterator executeBindings( ArgMap args, Node [] nodes )
        { return executeBindings( new ArrayList(), args, nodes ); }
        
    /**
        the standard "default" implementation of executeBindings.
    */
    public ExtendedIterator executeBindings( List outStages, ArgMap args, Node [] nodes )
        {
        Mapping map = new Mapping( nodes );
        ArrayList stages = new ArrayList();        
        addStages( stages, args, map );
        if (constraintGraph.size() > 0) 
            stages.add( new ConstraintStage( map, constraintGraph ) );
        outStages.addAll( stages );
        final int [] indexes = findIndexes( map, nodes );
        variableCount = map.size();
        Stage allStages = connectStages( stages, variableCount );
        return filter( indexes, allStages );
        }
        
    private int [] findIndexes( Mapping map, Node [] nodes )
        {
        int [] result = new int [nodes.length];
        for (int i = 0; i < nodes.length; i += 1) result[i] = findIndex( map, nodes[i] ); 
        return result;
        }
         
    private int findIndex( Mapping map, Node node )
        {
        if (map.hasBound( node ) == false) map.newIndex( node );
        return map.indexOf( node );
        }
        
    private ExtendedIterator filter( final int [] indexes, final Stage allStages )
        {
        final Pipe complete = allStages.deliver( new BufferPipe() );
        return new NiceIterator()
            {
            public void close() { allStages.close(); clearPipe(); }
            public Object next() { return filter( indexes, complete.get() ); }
            public boolean hasNext() { return complete.hasNext(); }
            private void clearPipe()
                { 
                int count = 0; 
                while (hasNext()) { count += 1; next(); }
                // System.err.println( ">> pulled " + count + " values" );
                }
            };
        }
        
    protected Domain filter( int [] indexes, Domain complete )
        {
        Domain d = new Domain( indexes.length );
        for (int i = 0; i < indexes.length; i += 1) 
            d.setElement( i, (Node) complete.get( indexes[i] ) );
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
        
    private Query addNamedMatch( String name, Node s, Node p, Node o )
        {
        return addNamedMatch( name, new Triple( s, p, o ) );
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
            Cons nodeTriples = (Cons) e.getValue();
            Graph g = arguments.get( name );
            int nBlocks = Cons.size( nodeTriples ), i = nBlocks;
            Triple [] nodes = new Triple[nBlocks];
            while (nodeTriples != null)
                {
                nodes[--i] = nodeTriples.head;
                nodeTriples = nodeTriples.tail;
                }
            Stage next = g.queryHandler().patternStage( map, constraintGraph, nodes );
            stages.add( next );
            }
        }
        
    private int variableCount = -1;
    
    public int getVariableCount()
        { return variableCount; }
        
    private Stage connectStages( ArrayList stages, int count )
        {
        Stage current = Stage.initial( count );
        for (int i = 0; i < stages.size(); i += 1)
            current = ((Stage) stages.get( i )).connectFrom( current );
        return current;
        }
	}

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
