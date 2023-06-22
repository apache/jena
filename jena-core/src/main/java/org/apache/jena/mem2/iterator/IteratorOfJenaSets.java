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

package org.apache.jena.mem2.iterator;

import org.apache.jena.mem2.collection.JenaSet;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Iterator that iterates over the entries of sets which are contained in the given iterator of sets.
 *
 * @param <E> the type of the elements
 */
public class IteratorOfJenaSets<E> extends NiceIterator<E> {

    final Iterator<? extends JenaSet<E>> parentIterator;

    ExtendedIterator<E> currentIterator;

    public IteratorOfJenaSets(Iterator<? extends JenaSet<E>> parentIterator) {
        this.parentIterator = parentIterator;
        this.currentIterator = parentIterator.hasNext()
                ? parentIterator.next().keyIterator()
                : NiceIterator.emptyIterator();
    }

    @Override
    public boolean hasNext() {
        if (this.currentIterator.hasNext()) {
            return true;
        }
        while (this.parentIterator.hasNext()) {
            this.currentIterator = this.parentIterator.next().keyIterator();
            if (this.currentIterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public E next() {
        if (this.hasNext()) {
            return this.currentIterator.next();
        }
        throw new NoSuchElementException();
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        this.currentIterator.forEachRemaining(action);
        this.parentIterator.forEachRemaining(i -> i.keyIterator().forEachRemaining(action));
    }
}
