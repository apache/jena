/*
  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  (c) Copyright 2009 TopQuadrant, Inc.
  [See end of file]
  $Id: GraphListener.java,v 1.3 2009-12-11 03:36:51 jeremy_carroll Exp $
*/

package com.hp.hpl.jena.graph;

import java.util.*;

/**
    Interface for listening to graph-level update events. Each time the graph is
    poked to add or remove some triples, and after that poke has completed
    without throwing an exception, all the listeners attached to the Graph are
    informed about the poke.
    
    <p>The notifications are, in general, given before further changes to the graph are made. 
    Listeners are discouraged from making further modifications to the same graph
    since that will invalidate this property for other listeners.</p>
    
    <p>Some modifications may result in multiple notifications in some cases.
    For example, a bulk notification with {@link #notifyAddArray(Graph, Triple[])},
    may, or may not, be accompanied by several {@link #notifyAddTriple(Graph, Triple)}
    notifications, one for each triple. If possible, Graph implementations 
    should avoid such duplicate notifications and only give the bulk notifications,
    see {@link com.hp.hpl.jena.graph.impl.GraphWithPerform}.
    When these duplicate notifications occur, each should happen immediately
    after the change it signifies is complete. Thus, in the previous example,
    if the array has two triples, the pattern is:
    </p>
    <ol>
    <li>The first triple is added.</li>
    <li>{@link #notifyAddTriple(Graph, Triple)} for the first triple.</li>
    <li>The second triple is added.</li>
    <li>{@link #notifyAddTriple(Graph, Triple)} for the second triple.</li>
    <li>{@link #notifyAddArray(Graph, Triple[])} for the array.</li>
    </ol>
    
    <p>To track all changes to a graph it is necessary to consider all the methods
    in this interface, including {@link #notifyEvent(Graph, Object)}.
    </p>
    
    
    
    @author Jeremy Carroll, extensions by kers
*/

public interface GraphListener 
    {
    /**
        Method called when a single triple has been added to the graph.
    */
    void notifyAddTriple( Graph g, Triple t );
    
    /**
        Method called when an array of triples has been added to the graph.
    */
    void notifyAddArray( Graph g, Triple [] triples );
    
    /**
        Method called when a list [of triples] has been added to the graph.
    */
    void notifyAddList( Graph g, List<Triple> triples );
    
    /**
        Method called when an iterator [of triples] has been added to the graph
    */
    void notifyAddIterator( Graph g, Iterator<Triple> it );
    
    /**
        Method called when another graph <code>g</code> has been used to
        specify the triples added to our attached graph.
    	@param g the graph of triples added
     */
    void notifyAddGraph( Graph g, Graph added );
    
    /**
        Method called when a single triple has been deleted from the graph.
    */
    void notifyDeleteTriple( Graph g, Triple t );
    
    /**
        Method called when a list [of triples] has been deleted from the graph.
    */
    void notifyDeleteList( Graph g, List<Triple> L );
    
    /**
        Method called when an array of triples has been deleted from the graph.
    */
    void notifyDeleteArray( Graph g, Triple [] triples );
    
    /**
        Method called when an iterator [of triples] has been deleted from the graph.
    */
    void notifyDeleteIterator( Graph g, Iterator<Triple> it );
    
    /**
        Method to call when another graph has been used to specify the triples 
        deleted from our attached graph. 
    	@param g the graph of triples added
     */
    void notifyDeleteGraph( Graph g, Graph removed );
    
    /**
         method to call for a general event.
         <code>value</code> is usually a {@link GraphEvents}.
         Special attention is drawn to {@link GraphEvents#removeAll}
         and events whose {@link GraphEvents#getTitle()} is <code>"remove"</code>
         (see {@link GraphEvents#remove(Node, Node, Node)}. These correspond
         to the bulk operations {@link BulkUpdateHandler#removeAll()},
         and {@link BulkUpdateHandler#remove(Node, Node, Node)}, respectively.
         Unlike other notifications, the listener cannot tell which triples
         have been modified, since they have already been deleted by the time
         this event is sent, and the event does not include a record of them.
     	@param value
     */
    void notifyEvent( Graph source, Object value );
    }

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
