/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

public class PoolFiniteBlocking<T> implements Pool<T> 
{
    private PoolFiniteBlocking(int size) { }
    
    public T get()
    {
        return null ;
    }

    public boolean isEmpty()
    {
        return false ;
    }

    public void put(T item)
    {}
}

// **** Java6
//import java.util.concurrent.BlockingDeque;
//import java.util.concurrent.LinkedBlockingDeque;
//import com.hp.hpl.jena.sparql.ARQException;
//
///** Finite capacity pool - capacity is fixed at create time */ 
//public class PoolFiniteBlocking<T> implements Pool<T>
//{
//    BlockingDeque<T> pool  ;
//    
//    public PoolFiniteBlocking(int size) { pool = new LinkedBlockingDeque<T>(size) ; }
//    
//    //@Override
//    public final void put(T item)
//    {
//        pool.addLast(item) ;
//    }
//    
//    //@Override
//    public T get()              
//    { 
//        try
//        { 
//            return pool.takeFirst() ;
//        } catch (InterruptedException ex)
//        {
//            throw new ARQException("Failed to get an item from the pool (InterruptedException): "+ex.getMessage()) ;
//        }
//    }
//    
//    //@Override
//    public boolean isEmpty()    { return pool.isEmpty() ; } 
//}

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