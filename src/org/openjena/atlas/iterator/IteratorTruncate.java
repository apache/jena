/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.util.iterator.NiceIterator;

/** Iterate while a condition return true, then stop */
public class IteratorTruncate<T> implements Iterator<T>
{
    static public interface Test { boolean accept(Object object) ; }
    private Test test ;
    private T slot = null ;
    private boolean active = true ;
    private Iterator<T> iter ;

    public IteratorTruncate (Test test, Iterator<T> iter)
    { this.test = test ; this.iter = iter ; }

    public boolean hasNext()
    {
        if ( ! active ) return false ;
        if ( slot != null )
            return true ;

        if ( ! iter.hasNext() )
        {
            active = false ;
            return false ;
        }

        slot = iter.next() ;
        if ( test.accept(slot) )
            return true ;
        // Once the test goes false, no longer yield anything.
        NiceIterator.close(iter) ;
        active = false ;
        iter = null ;
        slot = null ;
        return false ;
    }

    public T next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("IteratorTruncate.next") ;    
        T x = slot ;
        slot = null ;
        return x ;
    }

    public void remove()
    { throw new UnsupportedOperationException("IteratorTruncate.remove"); }

    public void close()
    { if ( iter != null ) NiceIterator.close(iter) ; }

}



/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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