/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.iterator;

import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.Queue ;

/** PeekIterator - is one slot ahead from the wrapped iterator */ 
public class PeekIterator<T> implements Iterator<T>
{
    private final Iterator<T> iter ;
    private boolean finished = false ;
    // Slot always full when iterator active.  Null is a a valid element.
    private T slot ;
    
    public static <T> PeekIterator<T> create(PeekIterator<T> iter) { return iter ; }
    public static <T> PeekIterator<T> create(Iterator<T> iter)
    { 
        if ( iter instanceof PeekIterator<?> )
            return (PeekIterator<T>)iter ;
        return new PeekIterator<>(iter) ;
    }

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

    @Override
    public boolean hasNext()
    {
        if ( finished )
            return false ;
        return true ;
    }

    /** Peek the next element or return null
     *  @see Queue#peek
     */
    public T peek()
    {
        if ( finished )
            return null  ;
        return slot ;
    }
    
    /** Peek the next element or throw  NoSuchElementException */
    public T element()
    {
        if ( finished )
            throw new NoSuchElementException() ;
        return slot ;
    }
    
    @Override
    public T next()
    {
        if ( finished )
            throw new NoSuchElementException() ;
        T x = slot ;
        // Move on now so the slot is loaded for peek.
        fill() ;
        return x ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

}
