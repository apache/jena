/*
 * (c) Copyright 2009 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import java.util.Iterator ;

public class IteratorInteger implements Iterator<Long>
{
    private final long start ;
    private final long finish ;
    private long current ;

    public static IteratorInteger range(long start, long finish)
    {
        return new IteratorInteger(start, finish) ;
    }
    
    
    /** [start, finish) */
    public IteratorInteger(long start, long finish)
    {
        this.start = start ;
        this.finish = finish ;
        this.current = start ;
    }
    
    public boolean hasNext()
    {
        return ( current < finish ) ;
    }

    public Long next()
    {
        Long v = Long.valueOf(current) ;
        current++ ;
        return v ;
    }

    public void remove()
    { throw new UnsupportedOperationException() ; }

}

/*
 * (c) Copyright 2009 Talis Information Ltd
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