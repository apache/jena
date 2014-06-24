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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

/** Remembers the last N yields.
 * See also IteratorWithBuffer, for an iterator that looks ahead to what it wil yield.
 * @see IteratorWithBuffer
 * @see org.apache.jena.atlas.iterator.PeekIterator
 * @see org.apache.jena.atlas.iterator.PushbackIterator
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
        this.history = new ArrayList<>(N) ;
        this.capacity = N ;
    }

    @Override
    public boolean hasNext()
    {
        boolean b = iter.hasNext() ;
        if ( !b ) 
            atEnd() ;
        return b ;
    }

    @Override
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

    @Override
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
