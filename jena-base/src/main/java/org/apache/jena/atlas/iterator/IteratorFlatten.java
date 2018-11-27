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

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Iterator that flattens an iterator of iterators ({@code Iterator<Iterator<X>>}). */
public class IteratorFlatten<X> implements Iterator<X> {
    private boolean     finished = false;
    private Iterator<X> current  = null;
    final private Iterator<Iterator<X>> pipeline;

    public IteratorFlatten(Iterator<Iterator<X>> pipeline) {
        this.pipeline = pipeline;
    }
    
    @Override
    public boolean hasNext() {
        if ( finished )
            return false;
        // !finished and current == null : happens at the start.
        if ( current != null && current.hasNext() )
            return true;
        // Stage finished or this is the first call.
        while(pipeline.hasNext()) {
            current = pipeline.next();
            if ( current == null || ! current.hasNext())
                continue;
            // There is at least one item in the new current stage.
            return true;
        }
        // Nothing more.
        current = null;
        finished = true;
        return false;
    }

    @Override
    public X next() {
        if ( !hasNext() )
            throw new NoSuchElementException();
        return current.next();
    }
    
//    /** Advance an iterator, skipping nulls.
//     * Return null iff the iterator has ended.
//     * @implNote
//     * Unlike a filtering out null from an iterator (e.g. {@link Iter#filter(Iterator, Predicate)}),
//     * this code does not create intermediate objects.  
//     */
//    private static <X> X /*Iter.*/advance(Iterator<X> iter) {
//        while (iter.hasNext()) {
//            X item = iter.next();
//            if ( item != null )
//                return item;
//        }
//        return null;
//    }
}
