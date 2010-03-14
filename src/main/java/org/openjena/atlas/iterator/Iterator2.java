/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Iterator2 : the concatenation of two iterators.
 * 
 * @author Andy Seaborne
 * @version $Id$
 */

public class Iterator2<T> implements Iterator<T>, Iterable<T>
{
    private Iterator<? extends T> iter1 ;
    private Iterator<? extends T> iter2 ;

    public static <X> Iterator<X> create(Iterator<? extends X> iter1, Iterator<? extends X> iter2)
    {
        // The casts are safe because an iterator can only return X, and does not take an X an an assignment.  
        if ( iter1 == null )
        {
            @SuppressWarnings("unchecked")
            Iterator<X> x = (Iterator<X>)iter2 ;
            return x ;
        }
        
        if ( iter2 == null )
        {
            @SuppressWarnings("unchecked")
            Iterator<X> x = (Iterator<X>)iter1 ;
            return x ;
        }
        
        return new Iterator2<X>(iter1, iter2) ;
    }
    
    private Iterator2(Iterator<? extends T> iter1, Iterator<? extends T> iter2)
    {
        this.iter1 = iter1 ;
        this.iter2 = iter2 ;
    }

    //@Override
    public boolean hasNext()
    {
        if ( iter1 != null )
        {
            if ( iter1.hasNext() ) return true ;
            // Iter1 ends
            iter1 = null ;
        }
        
        if ( iter2 != null )
        {
            if ( iter2.hasNext() ) return true ;
            // Iter2 ends
            iter2 = null ;
        }
        return false ; 
    }

    //@Override
    public T next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("Iterator2.next") ;
        if ( iter1 != null )
            return iter1.next();
        if ( iter2 != null )
            return iter2.next();
        throw new Error("Iterator2.next") ;
    }

    //@Override
    public void remove()
    { 
        if ( iter1 != null )
        {
            iter1.remove();
            return ;
        }
        if ( iter2 != null )
        {
            iter2.remove();
            return ;
        }
        throw new NoSuchElementException("Iterator2.remove") ;
    }

    //@Override
    public Iterator<T> iterator()
    {
        return this ;
    }
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