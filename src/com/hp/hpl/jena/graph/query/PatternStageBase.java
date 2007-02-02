/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
    [See end of file]
    $Id: PatternStageBase.java,v 1.14 2007-02-02 12:14:12 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query;

import java.util.*;

import org.apache.commons.logging.*;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;

import com.hp.hpl.jena.JenaRuntime;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.StageElement.*;
import com.hp.hpl.jena.shared.JenaException;

/**
    PatternStageBase contains the features that are common to the 
    traditional PatternStage engine and the Faster engine. (Eventually
    the two will merge back together.) Notable, it:
    
    <ul>
    <li>remembers the graph 
    <li>classifies all the triples according to the factory
    <li>constructs the array of applicable guards
    </ul>
    
    @author hedgehog
*/
public abstract class PatternStageBase extends Stage
    {
    protected static int count = 0;
    protected final ValuatorSet [] guards;
    protected final QueryTriple [] classified;
    protected final Graph graph;
    protected final QueryNodeFactory factory;
    
    public PatternStageBase( QueryNodeFactory factory, Graph graph, Mapping map, ExpressionSet constraints, Triple[] triples )
        {
        this.graph = graph;
        this.factory = factory;
        this.classified = QueryTriple.classify( factory, map, triples );
        this.guards = new GuardArranger( triples ).makeGuards( map, constraints );
        }

    static Log log = LogFactory.getLog( PatternStageBase.class );
    
    protected void run( Pipe source, Pipe sink, StageElement se )
        {
        try { while (stillOpen && source.hasNext()) se.run( source.get() ); }
        catch (Exception e) 
            {
            log.debug( "PatternStageBase has caught and forwarded an exception", e );
            sink.close( e ); 
            return; 
            }
        sink.close();
        }
    
    private final class PatternStageThread extends Thread
        {
        private BoundedBuffer buffer = new BoundedBuffer(1);
        
        public PatternStageThread( String name )
            { super( name ); }

        public void put( Work w )
            { 
            try  { buffer.put( w ); }
            catch (InterruptedException e)
                { throw new BufferPipe.BoundedBufferPutException( e ); } 
            }
        
        protected Work get()
            {
            try { return (Work) buffer.take(); }
            catch (InterruptedException e)
                { throw new BufferPipe.BoundedBufferTakeException( e ); }
            }
        
        public void run() 
            { 
            while (true)
                {
                get().run();
                addToAvailableThreads( this );
                }
            }
        }

    public class Work
        {
        protected final Pipe source;
        protected final Pipe sink;
        protected final StageElement e;
        
        public Work( Pipe source, Pipe sink, StageElement e )
            { this.source = source; this.sink = sink; this.e = e; }
        
        public void run()
            { PatternStageBase.this.run( source, sink, e ); }
        }
    
   public static boolean reuseThreads = JenaRuntime.getSystemProperty( "jena.reusepatternstage.threads", "no" ).equals( "yes" );
   
    public synchronized Pipe deliver( final Pipe sink )
        {
        final Pipe source = previous.deliver( new BufferPipe() );
        final StageElement s = makeStageElementChain( sink, 0 );
        if (reuseThreads)
            getAvailableThread().put( new Work( source, sink, s ) ); 
        else
            new Thread( "PatternStage-" + ++count ) 
                { public void run() { PatternStageBase.this.run( source, sink, s ); } } 
            .start();
        return sink;
        }

    private static final List threads = new ArrayList();
    
    private void addToAvailableThreads( PatternStageThread thread )
        {
        synchronized (threads)
            {
            threads.add( thread );
            log.debug( "caching thread " + this + " [currently " + threads.size() + " cached threads]" );
            }
        }
    
    private PatternStageThread getAvailableThread()
        {
        synchronized (threads)
            {
            int size = threads.size();
            if (size > 0)
                {
                PatternStageThread x = (PatternStageThread) threads.remove( size - 1 );
                log.debug( "reusing thread " + x );
                return x;
                }
            }
        PatternStageThread f = new PatternStageThread( "PatternStage-" + ++count );
        log.debug( "created new thread " + f );
        f.start();
        return f;
        }

    protected StageElement makeStageElementChain( Pipe sink, int index )
        {
        return index < classified.length
            ? makeIntermediateStageElement( sink, index )
            : makeFinalStageElement( sink )
            ;
        }

    protected PutBindings makeFinalStageElement( Pipe sink )
        { return new StageElement.PutBindings( sink ); }

    protected StageElement makeIntermediateStageElement( Pipe sink, int index )
        {
        StageElement next = makeNextStageElement( sink, index );
        return makeFindStageElement( index, next );
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

/*
    (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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