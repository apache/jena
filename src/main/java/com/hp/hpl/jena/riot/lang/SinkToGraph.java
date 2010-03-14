/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import org.openjena.atlas.event.Event ;
import org.openjena.atlas.event.EventListener ;
import org.openjena.atlas.event.EventManager ;
import org.openjena.atlas.event.EventType ;
import org.openjena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.Triple;


public abstract class SinkToGraph implements Sink<Triple>
{
    static final EventType startRead = new EventType("SinkToGraph.StartRead") ;
    static final EventType finishRead = new EventType("SinkToGraph.FinishRead") ;
    
    protected final Graph graph ;
    private EventListener el1 ;
    private EventListener el2 ;

    protected SinkToGraph(Graph g)
    { 
        this.graph = g ;
        // Convert between the new global event system (EventManager)
        // and old style Jena graph events.
        el1 = new EventListener(){
            //@Override
            public void event(Object dest, Event event)
            {
                graph.getEventManager().notifyEvent( graph , GraphEvents.startRead ) ;
            }
        } ;

        el2 = new EventListener(){
            //@Override
            public void event(Object dest, Event event)
            {
                graph.getEventManager().notifyEvent( graph , GraphEvents.finishRead ) ;
            }
        } ;
        EventManager.register(this, startRead, el1) ;
        EventManager.register(this, finishRead, el2) ;
    }

    //@Override
    public void flush() { }
    
    //@Override
    public void close()
    {
        EventManager.unregister(this, finishRead, el2) ;
        EventManager.unregister(this, startRead, el1) ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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