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

import org.apache.jena.atlas.lib.Closeable ;

/** Class to delay the initialization of an iterator until first call of an Iterator operation. */

public abstract class IteratorDelayedInitialization<T> implements Iterator<T>, Closeable
{
    private boolean initialized = false ;
    private Iterator<T> iterator ; 
    
    public IteratorDelayedInitialization() {}

    private void init()
    {
        if ( ! initialized )
        {
            initialized = true ;
            iterator = initializeIterator() ;
        }
    }
    
    // Called exactly once
    protected abstract Iterator<T> initializeIterator() ;
    
    @Override
    public boolean hasNext()
    {
        init() ;
        boolean b = iterator.hasNext() ;
        if ( ! b )
            close() ;
        return b ;
    }

    @Override
    public T next()
    {
        init() ;
        try { return iterator.next() ; }
        catch (NoSuchElementException ex) { close() ; throw(ex) ; }
    }

    @Override
    public void remove()
    {
        init() ;
        iterator.remove() ;
    }
    
    @Override
    public void close()
    {
        Iter.close(iterator) ;
        iterator = null ;
    }
}
