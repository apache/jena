/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphUtil.java,v 1.6 2004-06-30 12:57:57 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;
import java.util.*;

/**
    An ad-hoc collection of useful code for graphs; starting with findAll, which
    is graph-specific, and extending to iteratorToSet and iteratorToList, which
    are here because they are used in tests and in the bulk update handlers.
 	@author kers
*/
public class GraphUtil
    {
    /**
        Only static methods here - the class cannot be instantiated.
    */
    private GraphUtil()
        {}

    /**
        Answer an iterator covering all the triples in the specified graph.
    	@param g the graph from which to extract triples
    	@return an iterator over all the graph's triples
    */
    public static ExtendedIterator findAll( Graph g )
        { return g.find( Node.ANY, Node.ANY, Node.ANY ); }

    /**
        Answer the elements of the given iterator as a set. The iterator is consumed
        by the operation. (Hence, if it is closable, it will be closed.)
        @param i the iterator to convert
        @return A set of the members of i
    */
    public static Set iteratorToSet( Iterator i )
        {
        Set result = HashUtils.createSet();
        while (i.hasNext()) result.add( i.next() );
        return result;
        }
        
    /**
        Answer the elements of the given iterator as a list, in the order that they
        arrived from the iterator. The iterator is consumed by this operation.
    	@param it the iterator to convert
    	@return a list of the elements of <code>it</code>, in order
     */
    public static List iteratorToList( Iterator it )
        {
        List result = new ArrayList();
        while (it.hasNext()) result.add( it.next() );
        return result;
        }
                
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