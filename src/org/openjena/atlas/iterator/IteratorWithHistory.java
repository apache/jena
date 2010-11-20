/*
 * (c) Copyright 2010 Epimorphics L ;td.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

/** Remembers the last N yields.
 * See also IteratorWithBuffer, for an iterator that looks ahead to what it wil yield.
 * @see IteratorWithBuffer
 * @see org.openjena.atlas.iterator.PeekIterator
 * @see org.openjena.atlas.iterator.PushbackIterator
 */
public class IteratorWithHistory<T> implements Iterator<T>
{
    private List<T> history ;
    private Iterator<T> iter ;
    private int capacity ;
    private boolean hasEnded = false ;
    
    public IteratorWithHistory(Iterator<T> iter, int N)
    {
        this.iter = iter ;
        this.history = new ArrayList<T>(N) ;
        this.capacity = N ;
    }

    //@Override
    public boolean hasNext()
    {
        boolean b = iter.hasNext() ;
        if ( !b ) 
            atEnd() ;
        return b ;
    }

    //@Override
    public T next()
    {
        T item = null ;
        try { item = iter.next() ; }
        catch (NoSuchElementException ex) { atEnd() ; }
        // Shuffle up, add at bottom.
        if ( history.size() >= capacity )
            history.remove(history.size()-1) ;
        history.add(0,item) ;
        return item ;
    }

    //@Override
    public void remove()
    { throw new UnsupportedOperationException("remove") ; }

    /** return the previous i'th element returned by next(). 0 means last call of next.
     * History is retained after the end of iteration.   
     * 
     * @return Element or null for no such element (that is for haven't yielded that many elements).
     * @throws IndexOutOfBoundsException if index is negative.
     */
    public T getPrevious(int idx)
    {
        if ( idx >= capacity || idx < 0 )
            throw new IndexOutOfBoundsException("Index: "+idx) ;
        if ( idx >= history.size() )
            return null ;
        return history.get(idx) ;
    }

    /**
     * Return the current size of the histiory. This can be used to tell the difference between
     * an iterator returning null and an iterator that is just short.  
     */
    public int currentSize()
    { 
        return history.size() ;
    }
    
    /** Called when the underlying iterator ends */
    protected void atEnd()
    {
        if (! hasEnded )
        {
            hasEnded = true ;
            endReached() ;
        }
    }
    
    /** Called, once, at the end */ 
    protected void endReached() { }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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