/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: IteratorCollection.java,v 1.2 2005-02-21 12:18:56 andy_seaborne Exp $
*/

package com.hp.hpl.jena.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.util.iterator.NiceIterator;


/**
 @author hedgehog
 */
public class IteratorCollection
    {
    /**
        Only static methods here - the class cannot be instantiated.
    */
    private IteratorCollection()
        {}
    
    /**
        Answer the elements of the given iterator as a set. The iterator is consumed
        by the operation. Even if an exception is thrown, the iterator will be closed.
        @param i the iterator to convert
        @return A set of the members of i
    */
    public static Set iteratorToSet( Iterator i )
        {
        Set result = CollectionFactory.createHashedSet();
        try { while (i.hasNext()) result.add( i.next() ); }
        finally { NiceIterator.close( i ); }
        return result;
        }

    /**
        Answer the elements of the given iterator as a list, in the order that they
        arrived from the iterator. The iterator is consumed by this operation:
        even if an exception is thrown, the iterator will be closed.
    	@param it the iterator to convert
    	@return a list of the elements of <code>it</code>, in order
     */
    public static List iteratorToList( Iterator it )
        {
        List result = new ArrayList();
        try { while (it.hasNext()) result.add( it.next() ); }
        finally { NiceIterator.close( it ); }
        return result;
        }

    }


/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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