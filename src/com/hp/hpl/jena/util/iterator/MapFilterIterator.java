/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: MapFilterIterator.java,v 1.1.1.1 2002-12-19 19:21:18 bwm Exp $
*/

package com.hp.hpl.jena.util.iterator;

import java.util.*;

/**
	@author kers
    
    A MapFilterIterator takes an Iterator and returns a new Iterator which
    delivers the sequence of all non-null elements MapFilter(X) for X
    from the base iterator. 
*/

public class MapFilterIterator extends NiceIterator implements ClosableIterator
    {
    MapFilter f;
    Object current;
    boolean dead;
    ClosableIterator underlying;
    
/** Creates a sub-Iterator.
 * @param fl An object is included if it is accepted by this Filter.
 * @param e The parent Iterator.
 */        
    public MapFilterIterator( MapFilter fl, ClosableIterator e) {
        f = fl;
        current = null;
        dead = false;
        underlying = e;
    }
    
/** Are there any more acceptable objects.
 * @return true if there is another acceptable object.
 */        
    synchronized public boolean hasNext() {
        if (current!=null)
            return true;
        while (  underlying.hasNext() ) {
            current = f.accept( underlying.next() );
            if (current != null)
                return true;
        }
        current = null;
        dead = true;
        return false;
    }
    
    public void close()
        {
        underlying.close();
        }
        
/** remove's the member from the underlying <CODE>Iterator</CODE>; 
   <CODE>hasNext()</CODE> may not be called between calls to 
    <CODE>next()</CODE> and <CODE>remove()</CODE>.
 */        
        synchronized public void remove() {
            if ( current != null || dead )
              throw new IllegalStateException(
              "FilterIterator does not permit calls to hasNext between calls to next and remove.");

            underlying.remove();
        }
/** The next acceptable object in the iterator.
 * @return The next acceptable object.
 */        
    synchronized public Object next() {
        if (hasNext()) {
            Object r = current;
            current = null;
            return r;
        }
        throw new NoSuchElementException();
    }
}


/*
    (c) Copyright Hewlett-Packard Company 2002
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
