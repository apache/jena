/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleQueryHandler.java,v 1.9 2003-08-04 13:28:27 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
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

    public Stage patternStage( Mapping map, Graph constraints, Triple [] t )
        {
        if (t.length == 1)
            return new PatternStage( graph, map, t );
        else
            {
            final Stage [] stages = new Stage[t.length];
            for (int i = 0; i < t.length; i += 1) stages[i] = patternStage( map, constraints, new Triple[] {t[i]} );
            return new Stage() 
                {
                public Stage connectFrom( Stage s ) 
                    { 
                    for (int i = 0; i < stages.length; i += 1) 
                        {
                        stages[i].connectFrom( s );
                        s = stages[i];
                        }
                    return super.connectFrom( s ); 
                    }
                public Pipe deliver( Pipe L ) 
                    { 
                    return stages[stages.length - 1].deliver( L ); 
                    }
                };
            }       
        }
        
    public BindingQueryPlan prepareBindings( Query q, Node [] variables )   
        { return new SimpleQueryPlan( graph, q, variables ); }
        
    public TreeQueryPlan prepareTree( Graph pattern )
    	{ return new SimpleTreeQueryPlan( graph, pattern ); }
    	
	public ExtendedIterator objectsFor( Node s, Node p )
		{ 
        HashSet objects = new HashSet();
        ClosableIterator it = graph.find( s, p, null );
        while (it.hasNext()) objects.add( ((Triple) it.next()).getObject() );
		return WrappedIterator.create( objects.iterator() );
		}
		
	public ExtendedIterator subjectsFor( Node p, Node o )
		{ 
        HashSet objects = new HashSet();
        ClosableIterator it = graph.find( null, p, o );
        while (it.hasNext()) objects.add( ((Triple) it.next()).getSubject() );
		return WrappedIterator.create( objects.iterator() );
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
        
    /**
        Answer true iff the subject graph is empty.
    */
    public boolean isEmpty()
        { return graph.size() == 0; }
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
