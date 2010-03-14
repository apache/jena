/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

/** Iterator over a blocking queue until queue end seen */

public class IteratorBlockingQueue<T> implements Iterator<T>
{
    private BlockingQueue<T> queue ;
    private boolean finished = false ;
    private T slot = null ;
    private T endMarker ; 

    public IteratorBlockingQueue(BlockingQueue<T> queue, T endMarker) { this.queue = queue ; this.endMarker = endMarker ; }
    
    //@Override
    public boolean hasNext()
    {
        if ( finished ) return false ;
        if ( slot != null ) return true ;
        try
        {
            slot = queue.take() ;
            if ( slot == endMarker )
            {
                finished = true ;
                slot = null ;
                return false ;
            }
            return true ;
            
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
            
        }
        return false ;
    }

    //@Override
    public T next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        T item = slot ;
        slot = null ;
        return item ;
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