/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util.graph;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;

/** Convert the full, wide GraphListener interface into something more specific to loading
 *  including flagging the start and finish of a load operation.
 */

public abstract class GraphListenerBase implements GraphListener
{
    // ToDo: notifyAddGraph, notifyDeleteGraph
    public GraphListenerBase() {}

    public void notifyAddTriple(Graph g, Triple t) { addEvent(t) ; }

    public void notifyAddArray(Graph g, Triple[] triples)
    {
        for ( int i = 0 ; i < triples.length ; i++ )
            addEvent(triples[i]) ;
//        for ( Triple t : triples )
//            addEvent(t) ;
    }

    public void notifyAddList(Graph g, List<Triple> triples) 
    { 
        notifyAddIterator(g, triples.iterator()) ;
    }

    public void notifyAddIterator(Graph g, Iterator<Triple> it)
    {
        for ( ; it.hasNext() ; )
            addEvent(it.next()) ;
    }

    public void notifyAddGraph(Graph g, Graph added)
    {}

    public void notifyDeleteTriple(Graph g, Triple t)
    { deleteEvent(t) ; }

    public void notifyDeleteList(Graph g, List<Triple> triples)
    {
        notifyDeleteIterator(g, triples.iterator()) ;
    }

    public void notifyDeleteArray(Graph g, Triple[] triples)
    {
        for ( int i = 0 ; i < triples.length ; i++ )
            deleteEvent(triples[i]) ;
//        for ( Triple t : triples )
//            deleteEvent(t) ;
    }

    public void notifyDeleteIterator(Graph g, Iterator<Triple> it)
    {
        for ( ; it.hasNext() ; )
            deleteEvent(it.next()) ;
    }

    public void notifyDeleteGraph(Graph g, Graph removed)
    {}
    
    protected abstract void addEvent(Triple t) ;

    protected abstract void deleteEvent(Triple t) ;
    
    // --------

//    @Override
    public void notifyEvent(Graph source, Object value)
    {
        if ( value.equals(GraphEvents.startRead) )
            startRead() ;
        else if ( value.equals(GraphEvents.finishRead) )
            finishRead() ;
        //super.notifyEvent(source, value) ;
    }

    //@Override
    protected void startRead()
    {}
            
    //@Override
    protected void finishRead()
    {}
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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