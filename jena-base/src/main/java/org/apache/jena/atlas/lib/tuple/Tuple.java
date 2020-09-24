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

package org.apache.jena.atlas.lib.tuple;

import java.util.Iterator ;
import java.util.List ;
import java.util.function.Consumer ;
import java.util.stream.Stream ;
import java.util.stream.StreamSupport ;

import org.apache.jena.atlas.lib.ArrayUtils ;

/** A Tuple is a sequence of items of the same class of item.
 *  Tuples are immutable.  .equals is "by value".
 */
public interface Tuple<X> extends Iterable<X> {
    /** Get the i'th element, for i in the range 0 to len()-1 
     * @throws IndexOutOfBoundsException for i out of range 
     */
    public X get(int i) ;

    /** length : elements are 0 to len()-1 */
    public int len() ;

    /** Return true if this is a zero-length tuple */
    public default boolean isEmpty() {
        return len() == 0 ;
    }

    /** Convert to a List */
    public default List<X> asList() {
        return new TupleList<>(this) ;
    }

    /** stream */
    public default Stream<X> stream() { 
        return StreamSupport.stream(spliterator(), false) ;
    }

    /** forEach */
    @Override
    public default void forEach(Consumer<? super X> action) { 
        asList().forEach(action) ;
    }

    /** Iterable */
    @Override
    public default Iterator<X> iterator() {
        return asList().iterator() ;
    }

    /** Copy the elements of this Tuple into the array */ 
    public default void copyInto(X[] array) {
        copyInto(array, 0, len());
    }

    /** Copy the elements of this Tuple start at 'start' into the array */ 
    public default void copyInto(X[] array, int start) {
        copyInto(array, start, len());
    }

    /** Copy the elements of this Tuple into the array */ 
    public default void copyInto(X[] array, int start, int length) {
        for ( int i = 0 ; i < Math.min(length, len()) ; i++ )
            array[i+start] = get(i) ;
    }

    /** Copy the elements of this Tuple into a newly created array */ 
    public default X[] asArray(Class<X> cls) {
        X[] elts = ArrayUtils.alloc(cls, len()) ;
        for ( int i = 0 ; i < len() ; i++ )
            elts[i] = get(i) ;
        return elts ;
    }
}
