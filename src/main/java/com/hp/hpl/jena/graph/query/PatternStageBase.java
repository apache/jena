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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.JenaRuntime;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.StageElement.PutBindings;

/**
    PatternStageBase contains the features that are common to the 
    traditional PatternStage engine and the Faster engine. (Eventually
    the two will merge back together.) Notable, it:
    
    <ul>
    <li>remembers the graph 
    <li>classifies all the triples according to the factory
    <li>constructs the array of applicable guards
    </ul>
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

    static Logger log = LoggerFactory.getLogger( PatternStageBase.class );
    
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
        private BlockingQueue<Work> buffer = new ArrayBlockingQueue<Work>(1);
        
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
            try { return buffer.take(); }
            catch (InterruptedException e)
                { throw new BufferPipe.BoundedBufferTakeException( e ); }
            }
        
        @Override
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
    
   public static boolean reuseThreads = JenaRuntime.getSystemProperty( "jena.reusepatternstage.threads", "yes" ).equals( "yes" );
   
    @Override
    public synchronized Pipe deliver( final Pipe sink )
        {
        final Pipe source = previous.deliver( new BufferPipe() );
        final StageElement s = makeStageElementChain( sink, 0 );
        if (reuseThreads)
            getAvailableThread().put( new Work( source, sink, s ) ); 
        else
            new Thread( "PatternStage-" + ++count ) 
                { @Override
                public void run() { PatternStageBase.this.run( source, sink, s ); } } 
            .start();
        return sink;
        }

    private static final List<PatternStageThread> threads = new ArrayList<PatternStageThread>();
    
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
                PatternStageThread x = threads.remove( size - 1 );
                log.debug( "reusing thread " + x );
                return x;
                }
            }
        PatternStageThread f = new PatternStageThread( "PatternStage-" + ++count );
        log.debug( "created new thread " + f );
        f.setDaemon( true );
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
