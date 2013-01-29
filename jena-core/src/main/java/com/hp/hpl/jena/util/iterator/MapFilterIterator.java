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

package com.hp.hpl.jena.util.iterator;

import java.util.*;

/**
    A MapFilterIterator takes a MapFilter and an [Extended]Iterator and returns a new 
    ExtendedIterator which delivers the sequence of all non-null elements MapFilter(X) 
    for X from the base iterator.
*/

public class MapFilterIterator<T,X> extends NiceIterator<X> implements ExtendedIterator<X>
    {
    MapFilter<T,X> f;
    X current;
    boolean dead;
    ClosableIterator<T> underlying;
    
/** Creates a sub-Iterator.
 * @param fl An object is included if it is accepted by this Filter.
 * @param e The parent Iterator.
 */        
    public MapFilterIterator( MapFilter<T,X> fl, ExtendedIterator<T> e) {
        f = fl;
        current = null;
        dead = false;
        underlying = e;
    }
    
/** Are there any more acceptable objects.
 * @return true if there is another acceptable object.
 */        
    @Override
    synchronized public boolean hasNext() {
        if (current!=null)
            return true;
        while (  underlying.hasNext() ) {
            current = f.accept( underlying.next() );
            if (current != null)
                return true;
        }
        current = null;
        dead = true;
        return false;
    }
    
    @Override
    public void close()
        {
        underlying.close();
        }
        
/** remove's the member from the underlying <CODE>Iterator</CODE>; 
   <CODE>hasNext()</CODE> may not be called between calls to 
    <CODE>next()</CODE> and <CODE>remove()</CODE>.
 */        
        @Override
        synchronized public void remove() {
            if ( current != null || dead )
              throw new IllegalStateException(
              "FilterIterator does not permit calls to hasNext between calls to next and remove.");

            underlying.remove();
        }
/** The next acceptable object in the iterator.
 * @return The next acceptable object.
 */        
    @Override
    synchronized public X next() {
        if (hasNext()) {
            X r = current;
            current = null;
            return r;
        }
        throw new NoSuchElementException();
    }
}
