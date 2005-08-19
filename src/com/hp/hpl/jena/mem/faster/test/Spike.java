/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: Spike.java,v 1.1 2005-08-19 15:07:31 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.query.test.AbstractTestQuery;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.util.iterator.*;

import junit.framework.*;

public class Spike extends AbstractTestQuery
    {
    public static class MagicPatternStage extends PatternStageBase
        {
        protected ArrayList [] pre;
        
        public MagicPatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple[] t )
            {
            super( QueryNode.factory, graph, map, constraints, t );
            pre = new ArrayList[t.length];
            for (int i = 0; i < t.length; i += 1)
                pre[i] = (ArrayList) IteratorCollection.iteratorToList( graph.find( generalise( t[i] ) ) );
            }

        private Triple generalise( Triple t )
            {
            return Triple.create
                ( generalise( t.getSubject() ), 
                generalise( t.getPredicate() ),
                generalise( t.getObject() ) );
            }

        private Node generalise( Node n )
            { return n.isVariable() ? Node.ANY : n; }

        protected void nest( Pipe sink, Domain current, int index )
            {
            if (index == classified.length)
                sink.put( current.copy() );
            else
                {
                for (int i = 0; i < pre[index].size(); i += 1)
                    {
                    Triple t = (Triple) pre[index].get(i);
                    
                    }
                }
            }
        
        protected void run( Pipe source, Pipe sink )
            {
            nest( sink, source.get(), 0 );
            sink.close();
            }
        
        public Pipe deliver( final Pipe sink )
            { final Pipe stream = previous.deliver( new BufferPipe() );
            new Thread( "PatternStage-" + ++count ) 
                { public void run() { MagicPatternStage.this.run( stream, sink ); } } 
                .start();
            return sink; }

        }

    public Spike( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( Spike.class ); }
    
    protected Graph wrap( Graph g )
        {
        return new WrappedGraph( g ) 
            {
            public QueryHandler queryHandler()
                { return new SimpleQueryHandler( this ) 
                    {
                    public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] t )
                        { 
                        return new MagicPatternStage( graph, map, constraints, t ); 
                        }
                    };
                }
            };
        }

    public Graph getGraph()
        {
        return wrap( Factory.createDefaultGraph() );
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/