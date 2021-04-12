/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.atlas.lib;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;

/**
 * Collect some stream operations into one place.
 * Sometimes, the function form reads better.
 * @see Iter Iter - a stream-like class for iterators.
 */
public class StreamOps {
    /**
     * Iterator to Stream.
     * Call to {@linkplain Iter#asStream}.
     */
    public static <X> Stream<X> stream(Iterator<X> iter) {
        return Iter.asStream(iter);
    }

    /** Stream to {@link List} */
    public static <X> List<X> toList(Stream<X> stream) {
        return stream.collect(Collectors.toList());
    }

    /** Stream to {@link Set} */
    public static <X> Set<X> toSet(Stream<X> stream) {
        return stream.collect(Collectors.toSet());
    }

    /** First element or null */
    public static <X> X first(Stream<X> stream) {
        return stream.findFirst().orElse(null);
    }

    /** An element from a {@link Collection} */
    public static <X> X element(Collection<X> collection) {
        return first(collection.stream());
    }

    /** Debug : print stream.
     * This operation prints the whole stream at the point it is used,
     * and then returns a new stream of the same elements.
     */
    public static <X> Stream<X> print(Stream<X> stream) {
        return print(System.out, stream);
    }

    public static <X> Stream<X> print(PrintStream out, Stream<X> stream) {
        stream = stream.map(item -> { out.println(item); return item; });
        return toList(stream).stream();
    }

    public static <X> Stream<X> print(PrintStream out, String leader, Stream<X> stream) {
        String prefix = (leader==null) ? "" : leader;
        stream = stream.map(item -> { out.print(prefix); out.println(item); return item; });
        return toList(stream).stream();
    }

}
