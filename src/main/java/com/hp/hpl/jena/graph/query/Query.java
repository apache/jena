/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

/**
	The class of graph queries, plus some machinery (which should move) for
    implementing them.

	@author hedgehog
*/

public class Query 
	{   
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
        Initialiser for Query; makes an empty Query [no matches, no constraints]
    */
	public Query()
		{ }
        
    /**
        Initialiser for Query; makes a Query with its matches taken from 
        <code>pattern</code>.
        @param pattern a Graph whose triples are used as match elements
    */
    public Query( Graph pattern )
        { addMatches( pattern ); }

    /**
        Exception thrown when a query variable is discovered to be unbound.
    */
    public static class UnboundVariableException extends JenaException
        { public UnboundVariableException( Node n ) { super( n.toString() ); } }
                        
    /**
        Add an (S, P, O) match to the query's collection of match triples. Return
        this query for cascading.
        @param s the node to match the subject
        @param p the node to match the predicate
        @param o the node to match the object
        @return this Query, for cascading
    */
    public Query addMatch( Node s, Node p, Node o )
        { return addNamedMatch( NamedTripleBunches.anon, s, p, o ); }    
    
    /**
        Add a triple to the query's collection of match triples. Return this query
        for cascading.
        @param t an (S, P, O) triple to add to the collection of matches
        @return this Query, for cascading
    */
    public Query addMatch( Triple t )
        { 
        triplePattern.add( t );
        triples.add( NamedTripleBunches.anon, t );
        return this; 
        }
    
    private Query addNamedMatch( String name, Node s, Node p, Node o )
        { 
        triplePattern.add( Triple.create( s, p, o ) );
        triples.add( name, Triple.create( s, p, o ) ); 
        return this; 
        }
    
    /** 
         The named bunches of triples for graph matching 
    */
    private NamedTripleBunches triples = new NamedTripleBunches();
    
    private List<Triple> triplePattern = new ArrayList<Triple>();
    
    /**
        Answer a list of the triples that have been added to this query.
        (Note: ignores "named triples").
        
     	@return List
    */
    public List<Triple> getPattern()
        { return new ArrayList<Triple>( triplePattern ); }
    
    private ExpressionSet constraint = new ExpressionSet();
    
    public ExpressionSet getConstraints()
        { return constraint; }
        
    public Query addConstraint( Expression e )
        { 
        if (e.isApply() && e.getFun().equals( ExpressionFunctionURIs.AND ))
           for (int i = 0; i < e.argCount(); i += 1) addConstraint( e.getArg( i ) ); 
        else if (e.isApply() && e.argCount() == 2 && e.getFun().equals( ExpressionFunctionURIs.Q_StringMatch))
            constraint.add( Rewrite.rewriteStringMatch( e ) );
        else
            constraint.add( e );
        return this;    
        }
    
    /**
        Add all the (S, P, O) triples of <code>p</code> to this Query as matches.
    */
    private void addMatches( Graph p )
        {
        ClosableIterator<Triple> it = GraphUtil.findAll( p );
        while (it.hasNext()) addMatch( it.next() );
        }

    public ExtendedIterator<Domain> executeBindings( Graph g, Node [] results )
        { return executeBindings( args().put( NamedTripleBunches.anon, g ), results ); }
                
    public ExtendedIterator<Domain> executeBindings( Graph g, List<Stage> stages, Node [] results )
        { return executeBindings( stages, args().put( NamedTripleBunches.anon, g ), results ); }
    
    public ExtendedIterator<Domain> executeBindings( NamedGraphMap args, Node [] nodes )
        { return executeBindings( new ArrayList<Stage>(), args, nodes ); }
        
    /**
        the standard "default" implementation of executeBindings.
    */
    public ExtendedIterator<Domain> executeBindings( List<Stage> outStages, NamedGraphMap args, Node [] nodes )
        {
        SimpleQueryEngine e = new SimpleQueryEngine( triplePattern, sortMethod, constraint );
        ExtendedIterator<Domain> result = e.executeBindings( outStages, args, nodes );
        lastQueryEngine = e;
        return result;
        }
    
    private SimpleQueryEngine lastQueryEngine = null;
        
    /** mapping of graph name -> graph */
    private NamedGraphMap argMap = new NamedGraphMap();
            
    public NamedGraphMap args()
        { return argMap; }

    public TripleSorter getSorter()
        { return sortMethod; }
        
    public void setTripleSorter( TripleSorter ts )
        { sortMethod = ts == null ? TripleSorter.dontSort : ts; }
        
    private TripleSorter sortMethod = TripleSorter.dontSort;
    
    public int getVariableCount()
        { return lastQueryEngine.getVariableCount(); }
	}
