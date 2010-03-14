/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.util.Stack;

/** A Pool of objects. Base implements a non-blocking pool (returns null on no entry)
 * with infinite upper bound.  Set effective size by creating the right number of
 * entries when created.
 */ 
public class PoolBase<T> implements Pool<T>
{
    // For convenience we operate a LIFO policy.
    // This not part of the extenal contract of a "pool"
    
    //Deque<T> pool = new ArrayDeque<T>(); Better but Java6
    Stack<T> pool = new Stack<T>() ;
    int maxSize = -1 ;  // Unbounded
    
    public PoolBase() {} 
    //public Pool(int maxSize) { this.maxSize = maxSize ; }
    
    public void put(T item)
    {
        // Currently, unbounded
        if ( maxSize >= 0 && pool.size() == 0 )
        {}
        pool.push(item) ;
    }
    
    /** Get an item from the pool - return null if the pool is empty */
    public T get()              
    { 
        if ( pool.size() == 0 ) return null ;
        return pool.pop();
    }
    
    public boolean isEmpty()    { return pool.size() == 0 ; } 
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