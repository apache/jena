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
 * A variant on the closable/extended iterator that filters out
 * duplicate values. There is one complication that the value
 * which filtering is done on might not be the actual value
 * to be returned by the iterator. 
 * <p>
 * This class may be deprecated in the future.  New development should 
 * use use <code>UniqueFilter</code> for simple filtering of an ExtendedIterator, for 
 * more complex filtering implement a custom <code>Filter</code>.
 */

public class UniqueExtendedIterator<T> extends WrappedIterator<T> {

    /** The set of objects already seen */
    protected HashSet<T> seen = new HashSet<>();
    
    /** One level lookahead */
    protected T next = null;
    
    /**
     * Constructor. Note the use of {@link #create} as reliable means of
     * creating a unique iterator without double-wrapping iterators that 
     * are already unique iterators.
     */
    public UniqueExtendedIterator(Iterator<T> underlying) {
        super(underlying, true);
    }
    
    /**
     * Factory method for generating an iterator that is guaranteed
     * only to return one instance of every result from the wrapped
     * iterator <code>it</code>.
     * @param it An iterator to wrap
     * @return A iterator that returns the elements of the wrapped
     * iterator exactly once.  If <code>it</code> is already a unique
     * extended iteator, it is not further wrapped.
     */
    public static <T> ExtendedIterator<T> create( Iterator<T> it ) {
        return (it instanceof UniqueExtendedIterator<?>) ? 
                    ((UniqueExtendedIterator<T>) it) : new UniqueExtendedIterator<>( it );
    }
    
    /**
     * Fetch the next object to be returned, only if not already seen.
     * Subclasses which need to filter on different objects than the
     * return values should override this method.
     * @return the object to be returned or null if the object has been filtered.
     */
    protected T nextIfNew() {
        T value = super.next();
        return seen.add( value ) ? value : null;
    }
    
    /**
     * @see Iterator#hasNext()
     */
    @Override public boolean hasNext() {
        while (next == null && super.hasNext()) next = nextIfNew();
        return next != null;
    }

    /**
     * @see Iterator#next()
     */
    @Override public T next() {
        ensureHasNext();
        T result = next;
        next = null;
        return result;
    }
}
