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

package com.hp.hpl.jena.sparql.util.graph;

import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphEvents ;
import com.hp.hpl.jena.graph.GraphListener ;
import com.hp.hpl.jena.graph.Triple ;

/** Convert the full, wide GraphListener interface into something more specific to loading
 *  including flagging the start and finish of a load operation.
 */

public abstract class GraphListenerBase implements GraphListener
{
    // ToDo: notifyAddGraph, notifyDeleteGraph
    public GraphListenerBase() {}

    @Override
    public void notifyAddTriple(Graph g, Triple t) { addEvent(t) ; }

    @Override
    public void notifyAddArray(Graph g, Triple[] triples)
    {
        for ( Triple triple : triples )
        {
            addEvent( triple );
        }
//        for ( Triple t : triples )
//            addEvent(t) ;
    }

    @Override
    public void notifyAddList(Graph g, List<Triple> triples) 
    { 
        notifyAddIterator(g, triples.iterator()) ;
    }

    @Override
    public void notifyAddIterator(Graph g, Iterator<Triple> it)
    {
        for ( ; it.hasNext() ; )
            addEvent(it.next()) ;
    }

    @Override
    public void notifyAddGraph(Graph g, Graph added)
    {}

    @Override
    public void notifyDeleteTriple(Graph g, Triple t)
    { deleteEvent(t) ; }

    @Override
    public void notifyDeleteList(Graph g, List<Triple> triples)
    {
        notifyDeleteIterator(g, triples.iterator()) ;
    }

    @Override
    public void notifyDeleteArray(Graph g, Triple[] triples)
    {
        for ( Triple triple : triples )
        {
            deleteEvent( triple );
        }
//        for ( Triple t : triples )
//            deleteEvent(t) ;
    }

    @Override
    public void notifyDeleteIterator(Graph g, Iterator<Triple> it)
    {
        for ( ; it.hasNext() ; )
            deleteEvent(it.next()) ;
    }

    @Override
    public void notifyDeleteGraph(Graph g, Graph removed)
    {}
    
    protected abstract void addEvent(Triple t) ;

    protected abstract void deleteEvent(Triple t) ;
    
    // --------

//    @Override
    @Override
    public void notifyEvent(Graph source, Object value)
    {
        if ( value.equals(GraphEvents.startRead) )
            startRead() ;
        else if ( value.equals(GraphEvents.finishRead) )
            finishRead() ;
        //super.notifyEvent(source, value) ;
    }

    protected void startRead()
    {}
            
    protected void finishRead()
    {}
}
