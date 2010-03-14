/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import java.util.Iterator ;
import java.util.NoSuchElementException;

/** PeekIterator - is one slot ahead from the wrapped iterator */ 
public class PeekIterator<T> implements Iterator<T>
{

    private final Iterator<T> iter ;
    private boolean finished = false ;
    // Slot always full when iterator active.  Null is a a valid element.
    private T slot ;            

    public PeekIterator(Iterator<T> iter)
    {
        this.iter = iter ;
        fill() ;
    }
    
    private void fill()
    {
        if ( finished ) return ;
        if ( iter.hasNext() )
            slot = iter.next();
        else
        {
            finished = true ;
            slot = null ;
        }
    }

    //@Override
    public boolean hasNext()
    {
        if ( finished )
            return false ;
        return true ;
    }

    /** Peek the next element or throw NoSuchElementException */
    public T peek()
    {
        if ( finished )
            throw new NoSuchElementException() ;
        return slot ;
    }
    
    /** Peek the next element or return null */
    public T peekOrNull()
    {
        if ( finished )
            return null  ;
        return slot ;
    }
    
    //@Override
    public T next()
    {
        if ( finished )
            throw new NoSuchElementException() ;
        T x = slot ;
        // Move on now so the slot is loaded for peek.
        fill() ;
        return x ;
    }

    //@Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */