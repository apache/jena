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
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Closeable;

/** flatMap iterator.
 * See {@link Stream#flatMap}
 */
public class IteratorFlatMap<IN,OUT> implements Iterator<OUT>, Closeable {
    private boolean         finished = false;
    private Iterator<OUT>   current  = null;
    private Iterator<IN>    input;
    final private Function<IN, Iterator<OUT>> mapper;

    public IteratorFlatMap(Iterator<IN> iter, Function<IN, Iterator<OUT>> mapper) {
        this.input = iter;
        this.mapper = mapper;
    }

    @Override
    public boolean hasNext() {
        if ( finished )
            return false;
        // !finished and current == null : happens at the start.
        if ( current != null ) {
            if ( current.hasNext() )
                return true;
            // Move next in the input.
            Iter.close(current);
            current = null;
        }
        // Stage finished or this is the first call.
        while ( input.hasNext() ) {
            IN x = input.next();
            current = mapper.apply(x);
            if ( current == null )
                continue;
            if ( ! current.hasNext() ) {
                Iter.close(current);
                continue;
            }
            // There is at least one item in the new current stage.
            return true;
        }
        if ( current != null )
            Iter.close(current);
        // Nothing more.
        current = null;
        finished = true;
        return false;
    }

    @Override
    public OUT next() {
        if ( !hasNext() )
            throw new NoSuchElementException();
        return current.next();
    }

    @Override
    public void close() {
        if ( current != null )
            Iter.close(current);
        Iter.close(input);
    }
}
