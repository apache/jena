/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SimpleQueryHandler.java,v 1.20 2004-12-03 14:56:38 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    A SimpleQueryHandler is a more-or-less straightforward implementation of QueryHandler
    suitable for use on graphs with no special query engines.
    
	@author kers
*/

public class SimpleQueryHandler implements QueryHandler
    {
    /** the Graph this handler is working for */
    protected Graph graph;
    
    /** make an instance, remember the graph */
    public SimpleQueryHandler( Graph graph )
        { this.graph = graph; }

    public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] t )
        { return new PatternStage( graph, map, constraints, t ); }
                
    public BindingQueryPlan prepareBindings( Query q, Node [] variables )   
        { return new SimpleQueryPlan( graph, q, variables ); }
        
    public TreeQueryPlan prepareTree( Graph pattern )
    	{ return new SimpleTreeQueryPlan( graph, pattern ); }
    	
	public ExtendedIterator objectsFor( Node s, Node p )
		{ return objectsFor( graph, s, p ); }
		
	public ExtendedIterator subjectsFor( Node p, Node o )
		{ return subjectsFor( graph, p, o ); }
    
    public ExtendedIterator predicatesFor( Node s, Node o )
        { return predicatesFor( graph, s, o ); }
    
    public static ExtendedIterator objectsFor( Graph g, Node s, Node p )
        { 
        Set objects = CollectionFactory.createHashedSet();
        ClosableIterator it = g.find( s, p, Node.ANY );
        while (it.hasNext()) objects.add( ((Triple) it.next()).getObject() );
        return WrappedIterator.createNoRemove( objects.iterator() );
        }
        
    public static ExtendedIterator subjectsFor( Graph g, Node p, Node o )
        { 
        Set objects = CollectionFactory.createHashedSet();
        ClosableIterator it = g.find( Node.ANY, p, o );
        while (it.hasNext()) objects.add( ((Triple) it.next()).getSubject() );
        return WrappedIterator.createNoRemove( objects.iterator() );
        }
    
    public static ExtendedIterator predicatesFor( Graph g, Node s, Node o )
        {
        Set predicates = CollectionFactory.createHashedSet();
        ClosableIterator it = g.find( s, Node.ANY, o );
        while (it.hasNext()) predicates.add( ((Triple) it.next()).getPredicate() );
        return WrappedIterator.createNoRemove( predicates.iterator() );
        }
        
    /**
        this is a simple-minded implementation of containsNode that uses find
        up to three times to locate the node. Almost certainly particular graphs
        will be able to offer better query-handlers ...
    */
    public boolean containsNode( Node n )
        {
        return 
            graph.contains( n, Node.ANY, Node.ANY )
            || graph.contains( Node.ANY, n, Node.ANY )
            || graph.contains( Node.ANY, Node.ANY, n )
            ;
        }
    }

/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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
