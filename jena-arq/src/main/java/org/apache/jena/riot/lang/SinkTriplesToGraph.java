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

package org.apache.jena.riot.lang;

import org.apache.jena.atlas.event.Event ;
import org.apache.jena.atlas.event.EventListener ;
import org.apache.jena.atlas.event.EventManager ;
import org.apache.jena.atlas.event.EventType ;
import org.apache.jena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphEvents ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.SystemARQ ;

/**
 * Send triples to a graph.
 * This Sink must be closed after use. 
 */
public class SinkTriplesToGraph implements Sink<Triple>
{
    static final EventType startRead = new EventType("SinkToGraph.StartRead") ;
    static final EventType finishRead = new EventType("SinkToGraph.FinishRead") ;
    
    protected final Graph graph ;
    private EventListener el1 ;
    private EventListener el2 ;

    public SinkTriplesToGraph(boolean x , Graph g)
    { 
        this.graph = g ;
        // Convert between the new global event system (EventManager)
        // and old style Jena graph events.
        el1 = new EventListener(){
            @Override
            public void event(Object dest, Event event)
            {
                graph.getEventManager().notifyEvent( graph , GraphEvents.startRead ) ;
            }
        } ;

        el2 = new EventListener(){
            @Override
            public void event(Object dest, Event event)
            {
                graph.getEventManager().notifyEvent( graph , GraphEvents.finishRead ) ;
            }
        } ;
        EventManager.register(this, startRead, el1) ;
        EventManager.register(this, finishRead, el2) ;
    }

    @Override
    public void send(Triple triple)
    {
        graph.add(triple) ;
    }

    @Override
    public void flush() { SystemARQ.sync(graph) ; }
    
    @Override
    public void close()
    {
        EventManager.unregister(this, finishRead, el2) ;
        EventManager.unregister(this, startRead, el1) ;
    }



}
