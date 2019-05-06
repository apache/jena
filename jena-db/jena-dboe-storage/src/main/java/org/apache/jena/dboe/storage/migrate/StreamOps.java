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

package org.apache.jena.dboe.storage.migrate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;

public class StreamOps {
    /** Iterator to Stream.
     * Call to {@linkplain Iter#asStream}.
     */
    public static <X> Stream<X> stream(Iterator<X> iter) {
        return Iter.asStream(iter);
    }

    public static <X> List<X> toList(Stream<X> stream) {
        return stream.collect(Collectors.toList());
    }

    public static <X> Set<X> toSet(Stream<X> stream) {
        return stream.collect(Collectors.toSet());
    }

    public static <X> X first(Stream<X> stream) {
        return stream.findFirst().orElse(null);
    }

    public static <X> X element(Collection<X> collection) {
        return first(collection.stream());
    }

    public static <X> Stream<X> print(Stream<X> stream) {
        stream = stream.map(item -> { System.out.println(item); return item; });
        return toList(stream).stream();
    }
}

