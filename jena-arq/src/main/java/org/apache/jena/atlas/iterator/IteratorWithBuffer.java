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

/** Iterator that delays output by N slots so you can react to the output before it's yielded.
 * See also PeekIterator (which predates this code).
 * See also IteratorWithHistory for an iterator that remembers what it has yielded.
 * 
 * @see org.apache.jena.atlas.iterator.PeekIterator
 * @see org.apache.jena.atlas.iterator.PushbackIterator
 * @see IteratorWithHistory
 */
public class IteratorWithBuffer<T> implements Iterator<T>
{
    private List<T> lookahead ;
    private Iterator<T> iter ;
    private int capacity ;
    private boolean innerHasEnded = false ;
    
    public IteratorWithBuffer(Iterator<T> iter, int N)
    {
        if ( N < 0 )
            throw new IllegalArgumentException("Buffering size < 0") ;
        this.iter = iter ;
        this.lookahead = new ArrayList<>(N) ;
        this.capacity = N ;
        // Fill the lookahead.
        for ( int i = 0 ; i < N ; i++ )
        {
            if ( ! iter.hasNext() )
            {
                atEndInner() ;
                break ;
            }
            T nextItem = iter.next() ;
            //System.out.println("Fill: "+nextItem) ;
            lookahead.add(nextItem) ;
        }
    }

    @Override
    public boolean hasNext()
    {
        return lookahead.size() > 0 ;
    }

    @Override
    public T next()
    {
        if ( !hasNext() )
            throw new NoSuchElementException(this.getClass().getName()) ;

        if ( ! iter.hasNext() )
            atEndInner() ;
        
        T item = lookahead.remove(0) ;
        //System.out.println("remove: "+item) ;
        if ( iter.hasNext() )
        {
            // Should not throw NoSuchElementException.
            T nextItem = iter.next() ;
            //System.out.println("add   : "+nextItem) ;
            lookahead.add(nextItem) ;
        }
        return item ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("remove") ; }

    /** Look at elements that will be returned by a subsequnet call of .next().
     *  The next element is index 0, then index 1 etc. This operation is valid immediately
     *  after the constructor returns.
     *  Returns null for no such element (underlying iterator didn't yeild enough elements).
     *  Throws IndexOutOfBoundsException if an attempt i smade to go beyond
     *  the buffering window.
     */
    public T peek(int idx)
    {
        if ( idx < 0 || idx >= capacity )
            throw new IndexOutOfBoundsException("Index: "+idx) ;
        if ( idx >= lookahead.size() )
            return null ;
        return lookahead.get(idx) ; 
    }
    
    /**
     * Return the current size of the lookahead. This can be used to tell the difference between
     * an iterator returning null and an iterator that is just short.  
     */
    public int currentSize()
    { 
        return lookahead.size() ;
    }
    
    /** Set the element to be returned by a subsequent .next().
     * Use with care.
     * The original element to be returned at this position is lost. 
     */
    public void set(int idx, T item)
    {
        lookahead.set(idx, item) ; 
    }
    
    /** Called when the underlying iterator ends */
    private void atEndInner()
    {
        if (! innerHasEnded )
        {
            innerHasEnded = true ;
            endReachedInner() ;
        }
    }
    
    
    /** Called, once, at the end of the wrapped iterator.*/ 
    protected void endReachedInner() { }
    
}
