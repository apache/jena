/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphAddList.java,v 1.1 2009-06-29 08:55:43 castagna Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;

/**
    A List that implements the GraphAdd interface, so that it can be passed
    to things that want to add triples to Graphs. The triples are filtered.
*/

public class GraphAddList implements GraphAdd
    {
    protected Triple match;
    protected final ArrayList<Triple> triples = new ArrayList<Triple>();
    
    /**
         Initialise a GraphAddList with a triple [pattern] that specifies what triples
         will be accepted into the list. 
    */
    public GraphAddList( Triple match ) { this.match = match; }
    
    /**
         Add the triple <code>t</code> to this list if it is matched by the pattern.
    */
    @Override
    public void add( Triple t ) { if (match.matches( t )) triples.add( t ); }
    
    /**
        The number of triples held.
    */
    public int size() { return triples.size(); }
    
    /**
        Answer the last triple, and remove it.
    */
    public Triple removeLast() { return triples.remove( triples.size() - 1 ); }

    /**
        Answer an iterator over all the triples in this add-list.
    */
    public Iterator<Triple> iterator()
        { return triples.iterator(); }
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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