/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: Spike.java,v 1.4 2005-08-26 11:20:25 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.query.StageElement.PutBindings;
import com.hp.hpl.jena.graph.query.test.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.mem.faster.*;
import com.hp.hpl.jena.mem.faster.test.Spike.MagicPatternStage.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.*;
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
            
        protected abstract static class Block
            {
            public abstract void app( Domain d, Node n );
            }
        
        protected abstract static class  Bunch
            {
            public abstract void add( Node n );
            public abstract int size();
            public abstract boolean contains( Node n );
            public abstract void each( Domain d, Block b );
            public abstract void only( Bunch other );
            }
        
        protected static class MapDomainBunch extends Bunch
            {
            protected Map map;
            
            public MapDomainBunch( Map map )
                { this.map = map; }
            
            public void add( Node n )
                { throw new UnsupportedOperationException( "" ); }

            public int size()
                { return map.size(); }

            public boolean contains( Node n )
                { return map.containsKey( n ); }

            public void each( Domain d, Block b )
                { throw new UnsupportedOperationException( "" ); }

            public void only( Bunch other )
                {
                for (Iterator it = map.keySet().iterator(); it.hasNext(); )
                    {
                    if (!other.contains( (Node) it.next() )) it.remove();
                    }
                }
            }
        
        protected static class MapRangeBunch extends Bunch
            {
            protected Map map;
            protected Set elements;
            
            public MapRangeBunch( Map map )
                { 
                this.map = map; 
                computeElements();
                }

            protected Set computeElements()
                {
                if (this.elements == null)
                    {
                    this.elements = new HashSet();
                    for (Iterator it = map.entrySet().iterator(); it.hasNext(); )
                        {
                        Map.Entry e = (Map.Entry) it.next();
                        Bunch b = (Bunch) e.getValue();
                        b.each( null, new Block() 
                            {
                            public void app( Domain d, Node n )
                                { elements.add( n ); }
                            } );
                        }
                    }
                return this.elements;
                }
            
            public void add( Node n )
                { throw new UnsupportedOperationException( "" ); }

            public int size()
                { return computeElements().size(); }

            public boolean contains( Node n )
                { return computeElements().contains( n );  }

            public void each( Domain d, Block b )
                {
                for (Iterator it = computeElements().iterator(); it.hasNext();)
                    {
                    b.app( d, (Node) it.next() );
                    }
                }

            public void only( Bunch other )
                { 
//               System.err.println( ">> only " + other );
                for (Iterator it = map.entrySet().iterator(); it.hasNext();)
                    {
                    Map.Entry e = (Map.Entry) it.next();
//                    System.err.println( ">> updating " + e.getValue() );
                    ((Bunch) e.getValue()).only( other );
//                    System.err.println( "]]   to " + e.getValue() );
                    }
                }
        
            }
        
        protected static class BunchList extends Bunch
            {
            protected final List elements;
            
            public BunchList()
                { this( new ArrayList() ); }
            
            public BunchList( List elements )
                { this.elements = elements; }
            
            public void add( Node n )
                { elements.add( n ); }

            public int size()
                { return elements.size(); }

            public boolean contains( Node n )
                { return elements.contains( n ); }

            public void each( Domain d, Block b )
                { 
                for (int i = 0; i < elements.size(); i += 1)
                    b.app( d, (Node) elements.get( i ) ); 
                }

            public void only( Bunch other )
                { for (int i = 0; i < elements.size(); i += 1)
                    if (!other.contains( (Node) elements.get(i) ))
                        elements.remove(i);
                }
            
            public String toString()
                { return elements.toString(); }
        
            }
        
        protected Bunch [] listForVariable = new Bunch[27];
        
        protected void constrainBunch( int index, Bunch elements )
            {
            listForVariable[index].only( elements );
            }
        
        protected Bunch listFor( int varIndex )
            {
            if (listForVariable[varIndex] == null) 
                {
                System.err.println( ">> listFor " + varIndex + "!" );
                for (int i = 0; i < classified.length; i += 1)
                    System.err.println( "]]   " + classified[i] );
                }
            return listForVariable[varIndex];
            }
        
        protected void setListFor( int varIndex, Bunch L )
            {
            listForVariable[varIndex] = L;
            }
        
        protected StageElement makeIntermediateStageElement( Pipe sink, int index )
            {
            final SpikeTriple t = (SpikeTriple) classified[index];
            if (t.isGenerator())
                {
                return createSubjectGenerator( sink, index, t );
                }
            else if (t.isFilter())
                {
                return createSubjectFilter( sink, index, t );
                }
            else if (t.isJoin())
                {
                return createSOJoin( sink, index, t );
                }
            else
                {
                System.err.println( "]] " + t );
                final StageElement next = makeNextStageElement( sink, index );
                return makeFindStageElement( index, next );
                }
            }

        protected StageElement createSOJoin( Pipe sink, int index, final SpikeTriple t )
            {
            final Map m = new HashMap();
            Iterator it = graph.find( Node.ANY, t.P.node, Node.ANY );
            while (it.hasNext())
                {
                Triple tt = (Triple) it.next();
                Node S = tt.getSubject(), O = tt.getObject();
                Bunch L = (Bunch) m.get( S );
                if (L == null)
                    {
                    L = new BunchList();
                    L.add( O );
                    m.put( S, L );
                    }
                else
                    L.add( O );
                }
            setListFor( t.S.index, new MapDomainBunch( m ) );
            setListFor( t.O.index, new MapRangeBunch( m ) );
            final StageElement next = makeNextStageElement( sink, index );
            final Block block = new Block()
                {
                public void app( Domain d, Node n )
                    {
                    d.setElement( t.O.index, n );
                    next.run( d );
                    }
                };
            return new StageElement()
                {
                public void run( Domain d )
                    {
                    Iterator sIt = m.entrySet().iterator();
                    while (sIt.hasNext())
                        {
                        Map.Entry e = (Map.Entry) sIt.next();
                        Node S = (Node) e.getKey();
                        Bunch Os = (Bunch) e.getValue();
                        d.setElement( t.S.index, S );
                        Os.each( d, block );
                        }
                    }
                };
            }

        protected StageElement createSubjectFilter( Pipe sink, int index, final SpikeTriple t )
            {
            Bunch current = listFor( t.S.index );
            assertNotNull( current );
            Bunch fresh = new BunchList();
            Iterator it = graph.find( Node.ANY, t.P.node, t.O.node ).mapWith( getSubject );
            while (it.hasNext())
                {
                Node x = (Node) it.next();
                if (current.contains( x )) fresh.add( x );
                }
            constrainBunch( t.S.index, fresh );    
            return makeNextStageElement( sink, index ); 
            }

        protected StageElement createSubjectGenerator( Pipe sink, int index, final SpikeTriple t )
            {
            final Bunch elements = new BunchList( iteratorToList
                ( graph.find( Node.ANY, t.P.node, t.O.node ).mapWith( getSubject ) ) );
            setListFor( t.S.index, elements );
            final StageElement next = makeNextStageElement( sink, index );
            final Block block = new Block() 
                {
                public void app( Domain d, Node n )
                    {
                    d.setElement( t.S.index, n );
                    next.run( d );
                    }
                };
            return new StageElement()
                {
                public void run( final Domain current )
                    { listFor( t.S.index ).each( current, block ); }               
                };
            }

        protected StageElement makeNextStageElement( Pipe sink, int index )
            {
            ValuatorSet s = guards[index];
            StageElement rest = makeStageElementChain( sink, index + 1 );
            return s.isNonTrivial() ? new StageElement.RunValuatorSet( s, rest ) : rest;
            }

        protected StageElement makeFindStageElement( int index, StageElement next )
            {
            Applyer f = classified[index].createApplyer( graph );
            Matcher m = classified[index].createMatcher();
            return new StageElement.FindTriples( this, m, f, next );
            }
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