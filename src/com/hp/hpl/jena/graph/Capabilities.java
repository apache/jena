/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Capabilities.java,v 1.5 2004-01-16 16:05:56 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

/**
    Interface for expressing capabilities.
 	@author kers
*/
public interface Capabilities
    {
    /**
        Answer true iff Graph::size() is accurate.
     */
    boolean sizeAccurate();
    
    /**
        Answer true if Graph::add() can be used to add at least some triples to
        the graph.
     */
    boolean addAllowed();
    
    /**
        Answer true if Graph::add() can be used to add at least some triples to the
        graph. If everyTriple is true, answer true iff *any* triple can be added (ie the
        graph places no special restrictions on triples).
     */
    boolean addAllowed( boolean everyTriple );
    
    /**
        Answer true iff Graph::delete() can be used to remove at least some triples
        from the graph.
     */
    boolean deleteAllowed();
    
    /**
        Answer true if Graph::delete() can be used to remove at least some triples 
        from the graph. If everyTriple is true, any such triple may be removed.
     */
    boolean deleteAllowed( boolean everyTriple );
    
    /**
    	Answer true iff the iterators returned from <b>find</b> support the .remove()
        operation. 
    */
    boolean iteratorRemoveAllowed();
    
    /**
        Answer true iff the graph can be completely empty.
     */
    boolean canBeEmpty();
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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