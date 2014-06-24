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

import java.util.ArrayDeque ;
import java.util.Deque ;
import java.util.Iterator ;

public class PushbackIterator<T> implements Iterator<T>
{
    private Deque<T> items = new ArrayDeque<>() ;
    private Iterator<T> iter ;

    public PushbackIterator(Iterator <T> iter)
    {
        if ( iter == null ) throw new IllegalArgumentException("Wrapped iterator can't be null") ; 
        this.iter = iter ;
    }
    
    public void pushback(T item)
    {
        items.push(item) ;
    }
    
    @Override
    public boolean hasNext()
    {
        if ( !items.isEmpty() ) return true ;
        return iter.hasNext() ;
    }

    @Override
    public T next()
    {
        if ( !items.isEmpty() ) 
            return items.pop() ;
        return iter.next() ;
    }

    @Override
    public void remove()
    {
        // Need to track if last next() was from the stack or not.
        throw new UnsupportedOperationException() ;
    }

}
