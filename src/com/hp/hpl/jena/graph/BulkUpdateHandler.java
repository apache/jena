/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: BulkUpdateHandler.java,v 1.11 2005-02-21 11:51:56 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph;

import java.util.*;

/**
    Defines how bulk update may be done on Graphs.
<p>
    Bulk updates are not necessarily transactions; that is, a bulk update may
    fail part-way through, leaving some but not all triples added or deleted.
    However, if a bulk update does not fail (ie throw an exception) then the
    addition or removal of triples must have been successfully completed
    in accordance with the operation of the owning graph.    
    
 	@author kers
*/
public interface BulkUpdateHandler
    {
    /**
        Add all the triples into the graph this is handler for.
        @param triples an array of triples to add
    */
    void add( Triple [] triples );
    
    /**
        Add all the triples in the list into the graph this is handler for.
        Each element of the List must be a Triple.
        @param triples a list of Triple objects to add
    */
    void add( List triples );
    
    /**
        Add all the elements from the iterator into the graph this is handler for.
        Each element of the iterator must be a Triple.  WARNING. An implementation may
        have to expand the iterator into a data structure containing all the component
        elements; hence long iterators may be expensive on store. 
        @param it an Iterator delivering Triples
    */
    void add( Iterator it );
    
    /**
        Add all the triples of the given graph into the graph this is handler for.
        Optionally add g's reified triples.
        @param g a Graph whose triples are to be added
        @param withReifications if true, the reified triples of g are added as well
    */
    void add( Graph g, boolean withReifications );
    
    /**
        Add all the triples of the given graph into the graph this is handler for.
        Leave this graph's reifications unchanged.
        @param g a Graph whose triples are to be added
    */
    void add( Graph g );
    
    /**
        Remove all the triples from the graph this is handler for.
        @param triples an array of triples to remove
    */
    void delete( Triple [] triples );
    
    /**
        Remove all the triples in the list from the graph this is handler for.
        Each element of the List must be a Triple.
        @param triples a list of triples to remove
    */
    void delete( List triples );
    
    /**
        Remove all the triples in the iterator from the graph this is handler for.
        Each element from the iterator must be a Triple. WARNING. An implementation may
        have to expand the iterator into a data structure containing all the component
        elements; hence long iterators may be expensive on store. 
        
        @param it an iterator over Triple
    */
    void delete( Iterator it );
    
    /**
        Remove all the triples of the given graph from the graph this is handler for.
        Do not change the reifications.
        @param g a graph whose triples are to be removed
    */
    void delete( Graph g );
    
    /**
        Remove all the triples of the given graph from the graph this is handler for.
        Reified triples may optionally be removed.
        @param g a graph whose triples are to be removed
        @param withReifications if true, remove g's reifications from this graph
    */
    void delete( Graph g, boolean withReifications );

    /**
    	Remove all the statements from a graph.
    */
    void removeAll();
    
    /**
       Remove all triples that would be delivered by find(s, p, o)
    */
    void remove( Node s, Node p, Node o );
    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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