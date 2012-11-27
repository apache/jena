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

import com.hp.hpl.jena.util.iterator.NiceIterator ;

/** Iterate while a condition return true, then stop */
public class IteratorTruncate<T> implements Iterator<T>
{
    static public interface Test { boolean accept(Object object) ; }
    private Test test ;
    private T slot = null ;
    private boolean active = true ;
    private Iterator<T> iter ;

    public IteratorTruncate (Test test, Iterator<T> iter)
    { this.test = test ; this.iter = iter ; }

    @Override
    public boolean hasNext()
    {
        if ( ! active ) return false ;
        if ( slot != null )
            return true ;

        if ( ! iter.hasNext() )
        {
            active = false ;
            return false ;
        }

        slot = iter.next() ;
        if ( test.accept(slot) )
            return true ;
        // Once the test goes false, no longer yield anything.
        NiceIterator.close(iter) ;
        active = false ;
        iter = null ;
        slot = null ;
        return false ;
    }

    @Override
    public T next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("IteratorTruncate.next") ;    
        T x = slot ;
        slot = null ;
        return x ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("IteratorTruncate.remove"); }

    public void close()
    { if ( iter != null ) NiceIterator.close(iter) ; }

}
