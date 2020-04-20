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
import java.util.function.Predicate ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;

/** Iterate while a condition is true, then stop.
 *  This iterator does not touch any elements after the first
 *  where the predicate is false.
 */
final
public class IteratorTruncate<T> extends IteratorSlotted<T> {
    private final Predicate<T> predicate ;
    private Iterator<T> iter ;

    public IteratorTruncate(Iterator<T> iter, Predicate<T> predicate) {
        this.iter = iter ;
        this.predicate = predicate ;
    }

    @Override
    protected boolean hasMore() {
        // OK to return true then deny it in moveToNext by returning null.
        return iter.hasNext() ;
    }

    @Override
    protected T moveToNext() {
        // Add IteratorSlotted.inspect(element).
        if ( ! iter.hasNext() )
            return null ;
        T item = iter.next() ;
        if ( ! predicate.test(item) )
            return null ;
        return item ;
    }

    @Override
    protected void closeIterator() {
        Iter.close(iter);
    }
}
