/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphListener.java,v 1.13 2004-03-23 13:47:03 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import java.util.*;

/**
    Interface for listening to graph-level update events. Each time the graph is
    oked to add or remove some triples, and after that poke has completed
    without throwing an exception, all the listeners attached to the Graph are
    informed about the poke.
    
    @author Jeremy Carroll, extensions by kers
*/

public interface GraphListener 
    {
    /**
        Method called when a single triple has been added to the graph.
    */
    void notifyAddTriple( Triple t );
    
    /**
        Method called when an array of triples has been added to the graph.
    */
    void notifyAddArray( Triple [] triples );
    
    /**
        Method called when a list [of triples] has been added to the graph.
    */
    void notifyAddList( List triples );
    
    /**
        Method called when an iterator [of triples] has been added to the graph
    */
    void notifyAddIterator( Iterator it );
    
    /**
        Method called when another graph <code>g</code> has been used to
        specify the triples added to our attached graph.
    	@param g the graph of triples added
     */
    void notifyAddGraph( Graph g );
    
    /**
        Method called when a single triple has been deleted from the graph.
    */
    void notifyDeleteTriple( Triple t );
    
    /**
        Method called when a list [of triples] has been deleted from the graph.
    */
    void notifyDeleteList( List L );
    
    /**
        Method called when an array of triples has been deleted from the graph.
    */
    void notifyDeleteArray( Triple [] triples );
    
    /**
        Method called when an iterator [of triples] has been deleted from the graph.
    */
    void notifyDeleteIterator( Iterator it );
    
    /**
        Method to call when another graph has been used to specify the triples 
        deleted from our attached graph. 
    	@param g the graph of triples added
     */
    void notifyDeleteGraph( Graph g );
    
    /**
         method to call for a general event
     	@param value
     */
    void notifyEvent( Graph source, Object value );
    }

/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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
