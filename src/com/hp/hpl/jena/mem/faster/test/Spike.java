/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: Spike.java,v 1.3 2005-08-25 10:14:19 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.query.StageElement.PutBindings;
import com.hp.hpl.jena.graph.query.test.*;
import com.hp.hpl.jena.graph.query.test.AbstractTestQuery;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.mem.faster.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.util.iterator.*;

import junit.framework.*;

public class Spike extends AbstractTestQuery
    {
    public static class SpikeTriple extends QueryTriple
        {    
        public SpikeTriple( QueryNode S, QueryNode P, QueryNode O ) 
            { super( S, P, O ); }

        static final QueryNodeFactory factory = new QueryNodeFactoryBase()
            {
            public QueryTriple createTriple( QueryNode S, QueryNode P, QueryNode O )
                { return new SpikeTriple( S, P, O ); }
            
            public QueryTriple [] createArray( int size )
                { return new SpikeTriple[size]; }
            };

        public Applyer createApplyer( final Graph g )
            { 
            return new Applyer()
                {        
                public void applyToTriples( Domain d, Matcher m, StageElement next )
                    {
                    Iterator it = g.find( S.finder( d ), P.finder( d ), O.finder( d ) );
                    while (it.hasNext())
                        {
                        Triple t = (Triple) it.next();
                        if (m.match( d, t )) next.run( d );
                        }
                    }
                }; 
            }

        public boolean isGenerator()
            { return S instanceof QueryNode.Bind && !P.node.isVariable() && !O.node.isVariable(); }
        
        public boolean isFilter()
            { return S instanceof QueryNode.Bound && !P.node.isVariable() && !O.node.isVariable(); }
        
        public boolean isJoin()
            { 
            return S instanceof QueryNode.Bind && !P.node.isVariable() && O instanceof QueryNode.Bind;
            }

        }

    public static class MagicPatternStage extends PatternStageBase
        {        
        public MagicPatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple[] t )
            {
            super( SpikeTriple.factory, graph, map, constraints, t );
            }
        
        protected void run( Pipe source, Pipe sink, StageElement se )
            {
            try { while (stillOpen && source.hasNext()) se.run( source.get() ); }
            catch (Exception e) { sink.close( e ); return; }
            sink.close();
            }
        
        public Pipe deliver( final Pipe sink )
            { final Pipe stream = previous.deliver( new BufferPipe() );
            final StageElement s = makeStageElementChain( sink, 0 );
            new Thread( "PatternStage-" + ++count ) 
                { public void run() { MagicPatternStage.this.run( stream, sink, s ); } } 
                .start();
            return sink; }
        
        protected final Map1 getSubject = new Map1()
            {
            public Object map1( Object o ) { return ((Triple) o).getSubject(); }
            };
            
        protected List [] listForVariable = new List[27];
        
        protected List listFor( int varIndex )
            {
            if (listForVariable[varIndex] == null) 
                {
                System.err.println( ">> listFor " + varIndex + "!" );
                for (int i = 0; i < classified.length; i += 1)
                    System.err.println( "]]   " + classified[i] );
                }
            return listForVariable[varIndex];
            }
        
        protected void setListFor( int varIndex, List L )
            {
            listForVariable[varIndex] = L;
            }
            
        protected StageElement makeStageElementChain( final Pipe sink, final int index )
            {
            if (index == classified.length)
                return new StageElement.PutBindings( sink );
            else
                {
                final ValuatorSet s = guards[index];
                SpikeTriple st = (SpikeTriple) classified[index];
                Matcher m = st.createMatcher();
                Applyer f = st.createApplyer( graph );
                if (st.isJoin())
                    {
                    Iterator it = graph.find( Node.ANY, st.P.node, Node.ANY );
                    final int Sindex = st.S.index, Oindex = st.O.index;
                    final Map SO = new HashMap();
                    Set Os = new HashSet();
                    while (it.hasNext())
                        {
                        Triple t = (Triple) it.next();
                        Node S = t.getSubject(), O = t.getObject();
                        Os.add( O );
                        List already = (List) SO.get( S );
                        if (already == null)
                            SO.put( S, unitList( O ) );
                        else
                            already.add( O );
                        }
                    setListFor( Sindex, new ArrayList( SO.keySet() ) );
                    setListFor( Oindex, new ArrayList( Os ) );
                    return new StageElement()
                        {
                        public void run( Domain d )
                            {
                            Iterator it = SO.entrySet().iterator();
                            while (it.hasNext())
                                {
                                Map.Entry e = (Map.Entry) it.next();
                                Node S = (Node) e.getKey();
                                d.setElement( Sindex, S );
                                List Os = (List) e.getValue();
                                for (int i = 0; i < Os.size(); i += 1)
                                    {
                                    Node O = (Node) Os.get( i );
                                    d.setElement( Oindex, O );
                                    constructNext( sink, index, s ).run( d );
                                    }
                                }
                            }
                        };
                    }
                if (st.isGenerator()) return generatorStage( st, constructNext( sink, index, s ) );
                if (st.isFilter()) return filterStage( st, constructNext( sink, index, s ) );
                return new StageElement.FindTriples( this, m, f, constructNext( sink, index, s ) );
                }
            }

        /**
         	@param sink
         	@param index
         	@param s
         	@return
        */
        protected StageElement constructNext( Pipe sink, int index, ValuatorSet s )
            {
            final StageElement next = makeStageElementChain( sink, index + 1 );
            final StageElement then = s.isNonTrivial() ? new StageElement.RunValuatorSet( s, next ) : next;
            return then;
            }

        protected Object unitList( Node o )
            { 
            List result = new ArrayList();
            result.add( o );
            return result;
            }

        protected StageElement filterStage( SpikeTriple st, final StageElement then )
            {
            final int j = st.S.index;
            List already = listFor( j );
            final List restricted = new ArrayList();
            Iterator it = 
                graph.find( Node.ANY, st.P.node, st.O.node )
                .mapWith( getSubject );
            while (it.hasNext())
                {
                Node x = (Node) it.next();
                if (already.contains( x )) restricted.add( x );
                }
            setListFor( j, restricted );
            final int size = restricted.size();
            return new StageElement() 
                {
                public void run( Domain current )
                    {
                    for (int i = 0; i < size; i += 1)
                        {
                        current.setElement( j, (Node) restricted.get( i ) );
                        then.run( current );
                        }
                    }
                };
            }

        protected StageElement generatorStage( SpikeTriple st, final StageElement then )
            {
            final List answers = IteratorCollection.iteratorToList
                (
                graph.find( Node.ANY, st.P.node, st.O.node )
                .mapWith( getSubject )
                );
            final int size = answers.size();
            final int j = st.S.index;
            return new StageElement() 
                {
                public void run( Domain current )
                    {
                    for (int i = 0; i < size; i += 1)
                        {
                        current.setElement( j, (Node) answers.get( i ) );
                        then.run( current );
                        }
                    }
                };
            }

        }

    public Spike( String name )
        { super( name ); }
    
    public static TestSuite suite()  
        {
        return new TestSuite( Spike.class );
//        result.addTest( new Spike( "testMultiplePatterns" ) );
//        return result;
        }
    
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