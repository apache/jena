/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: NamedTripleBunches.java,v 1.5 2005-02-21 11:52:15 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.CollectionFactory;

import java.util.*;

/**
    A NamedTripleBunches maps a [graph] name to a bunch of triples associated
    with that name. 
    
 	@author hedgehog
*/
public class NamedTripleBunches
    {
    private Map triples = CollectionFactory.createHashedMap();

    /**
        A more-or-less internal object for referring to the "default" graph in a query.
    */
    public static final String anon = "<this>";   
    
    /**
        Initialise an empty set of named bunches.
    */
    public NamedTripleBunches() 
        {}
    
    /**
        Associate another triple with the given name.
    	@param name the [graph] name for the buinch to add this triple to
    	@param pattern the triple to add to the bunch
    */
    public void add( String name, Triple pattern )
        { triples.put( name, SimpleQueryEngine.cons( pattern, triples.get( name ) ) ); }    
    
    /**
        Answer an iterator over the entry set of the associated map: this will be
        cleaned up as we refactor.
     */    
    public Iterator entrySetIterator()
        { return triples.entrySet().iterator(); }
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
