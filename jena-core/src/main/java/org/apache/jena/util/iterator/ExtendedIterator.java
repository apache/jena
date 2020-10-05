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

package org.apache.jena.util.iterator;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An ExtendedIterator is a {@link ClosableIterator} on which other operations
 * are defined for convenience in iterator composition: composition, filtering
 * in, filtering out, and element mapping.<br>
 * NOTE that the result of these operations consumes the base iterator(s); they
 * do not make independent copies. <br>
 * The canonical implementation of ExtendedIterator is {@link NiceIterator},
 * which also defines static methods for these operations that will work on any
 * {@link ClosableIterator}s. <br>
 * 
 * @param <T>
 *            the type of element over which an instance of ExtendedIterator
 *            iterates
 */
public interface ExtendedIterator<T> extends ClosableIterator<T>
{
    /**
         Answer the next object, and remove it. Equivalent to next(); remove().
    */
    public T removeNext();
    
    /**
         return a new iterator which delivers all the elements of this iterator and
         then all the elements of the other iterator. Does not copy either iterator;
         they are consumed as the result iterator is consumed.
     */
     public <X extends T> ExtendedIterator<T> andThen( Iterator<X> other );

     /**
         return a new iterator containing only the elements of _this_ which
         pass the filter _f_. The order of the elements is preserved. Does not
         copy _this_, which is consumed as the result is consumed.
     */
     public ExtendedIterator<T> filterKeep( Predicate<T> f );

     /**
         return a new iterator containing only the elements of _this_ which
         are rejected by the filter _f_. The order of the elements is preserved.
         Does not copy _this_, which is consumed as the result is consumed.
     */
     public ExtendedIterator<T> filterDrop( Predicate<T> f );

     /**
         return a new iterator where each element is the result of applying
         _map1_ to the corresponding element of _this_. _this_ is not
         copied; it is consumed as the result is consumed.
     */
     public <U> ExtendedIterator<U> mapWith( Function<T, U> map1 );

     /**
      * Execute an action on each element of the iterator.
      * This operation ends and closes the {@code ExtendedIterator} even if there is an exception.
      * Shorter name for "{@code Iterator.forEachRemaining}", adding exception handling.  
      */
     public default void forEach( Consumer<T> action) { 
         try { 
             forEachRemaining(action);
         } finally { close() ; } 
     }
     
     /**
         Answer a list of the [remaining] elements of this iterator, in order,
         consuming this iterator.
    */
    public List<T> toList();

    /**
        Answer a set of the [remaining] elements of this iterator,
        consuming this iterator.
    */
    public Set<T> toSet();
    
    
    /**
        Answer with an {@link Optional}.
        This operation assumes that the {@code ExtendedIterator} does not return null for {@code next()}. 
        If it does,  {@code NullPointerException} is thrown.
        <ul>
        <li>If there is no next, return {@code Optional.empty()}
        <li>If the next object exists, and is not null, return that in the {@link Optional}.
        <li>If the next object exists, and is null, throw {@code NullPointerException}
        </ul>
     */
    public default Optional<T> nextOptional() {
        if ( ! hasNext() )
            return Optional.empty();
        T obj = next();
        Objects.requireNonNull(obj, "ExtendedIterator.next is null");
        return Optional.of(obj);
    }
}
