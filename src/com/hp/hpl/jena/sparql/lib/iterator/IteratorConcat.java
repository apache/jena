/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lib.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.sparql.lib.DS;


/** Iterator of Iterators */

public class IteratorConcat<T> implements Iterator<T>
{
    private List<Iterator<T>> iterators = DS.list(); 
    int idx = -1 ;
    private Iterator<T> current = null ;
    boolean finished = false ;
    
    public static <T> Iterator<T> concat(Iterator<T> iter1, Iterator<T> iter2)
    {
        if (iter2 == null) return iter1 ;
        if (iter1 == null) return iter2 ;
        IteratorConcat<T> c = new IteratorConcat<T>() ;
        c.add(iter1) ;
        c.add(iter2) ;
        return c ;
    }
    
    public void add(Iterator<T> iter) { iterators.add(iter) ; }
    
    //@Override
    public boolean hasNext()
    {
        if ( finished )
            return false ;

        if ( current != null && current.hasNext() )
            return true ;
        
        while ( idx < iterators.size()-1 )
        {
            idx++ ;
            current = iterators.get(idx) ;
            if ( current.hasNext() )
                return true ;
            // Nothing here - move on.
            current = null ;
        }
        // idx has run off the end.
        return false ;
    }

    //@Override
    public T next()
    {
        if ( ! hasNext() ) throw new NoSuchElementException() ; 
        return current.next();
    }

    //@Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

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