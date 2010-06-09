/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openjena.atlas.lib.Closeable ;


import com.hp.hpl.jena.sparql.util.Utils;

public abstract class RepeatApplyIterator<T> implements Iterator<T>, Closeable
{
    private Iterator<T> input ;
    private boolean finished = false ;
    private Iterator<T> currentStage = null ;

    protected RepeatApplyIterator(Iterator<T> input)
    {
        this.input = input ;
    }

    //@Override
    public boolean hasNext()
    {
        if  ( finished )
            return false ;
        for ( ;; )
        {
            if ( currentStage == null && input.hasNext() )
            {
                T nextItem = input.next();
                currentStage = makeNextStage(nextItem) ;
            }
            
            if ( currentStage == null  )
            {
                finished = true ;
                return false ;
            }
            
            if ( currentStage.hasNext() )
                return true ;
            
            currentStage = null ;
        }
    }

    protected abstract Iterator<T> makeNextStage(T t) ;
    
    //@Override
    public T next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException(Utils.className(this)+".next()/finished") ;
        return currentStage.next() ;
    }

    //@Override
    public final void remove()
    { throw new UnsupportedOperationException() ; }
    
    //@Override
    public void close()
    {
        Iter.close(input) ;
    }
}
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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