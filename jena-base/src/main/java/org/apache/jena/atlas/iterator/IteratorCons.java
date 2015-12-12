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

import org.apache.jena.atlas.lib.Lib ;

/** IteratorCons : the concatenation of two iterators.
 * See also {@link IteratorConcat}.
 * If there potentially many iterators to be joined, it is better to 
 *  create an IteratorConcat explicitly and add each iterator.
 *  IteratorCons is slightly better in the two iterator case.
 */
public class IteratorCons<T> implements Iterator<T>, Iterable<T>
{
    // No - we don't really need IteratorCons and IteratorConcat
    // Historical - IteratorCons came first.
    // IteratorConcat is nearly as good as IteratorCons in the small when it
    // it is hard to see when it woudl matter much.
    
    private Iterator<? extends T> iter1 ;
    private Iterator<? extends T> iter2 ;
    private Iterator<? extends T> removeFrom ;

    public static <X> Iterator<X> create(Iterator<? extends X> iter1, Iterator<? extends X> iter2)
    {
        if ( iter1 == null && iter2 == null )
            return Iter.nullIterator() ;
        
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
        
        return new IteratorCons<>(iter1, iter2) ;
    }
    
    private IteratorCons(Iterator<? extends T> iter1, Iterator<? extends T> iter2)
    {
        this.iter1 = iter1 ;
        this.iter2 = iter2 ;
    }

    @Override
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

    @Override
    public T next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException(Lib.className(this)+".next") ;
        if ( iter1 != null )
        {
            removeFrom = iter1 ;
            return iter1.next();
        }
        if ( iter2 != null )
        {
            removeFrom = iter2 ;
            return iter2.next();
        }
        throw new Error(Lib.className(this)+".next (two null iterators)") ;
    }

    @Override
    public void remove()
    {
        if ( null == removeFrom )
            throw new IllegalStateException("no calls to next() since last call to remove()") ;
        
        removeFrom.remove() ;
        removeFrom = null ;
    }

    @Override
    public Iterator<T> iterator()
    {
        return this ;
    }
}
