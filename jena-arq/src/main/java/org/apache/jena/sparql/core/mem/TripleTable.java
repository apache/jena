/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.jena.sparql.core.mem;

import java.util.stream.Stream;
import org.apache.jena.atlas.lib.tuple.TriFunction.TriOperator;
import org.apache.jena.atlas.lib.tuple.TriConsumer.Consumer3;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * A simplex or multiplex table of {@link Triple}s.
 */
public interface TripleTable extends TupleTable<Triple> {

    /**
     * Search the table using a pattern of slots. {@link Node#ANY} or <code>null</code> will work as a wildcard.
     *
     * @param s the subject node of the pattern
     * @param p the predicate node of the pattern
     * @param o the object node of the pattern
     * @return an {@link Stream} of matched triples
     */
    Stream<Triple> find(final Node s, final Node p, final Node o);

    @Override
    default void clear() {
        find(null, null, null).forEach(this::delete);
    }

    static <X> void accept(final TupleMap ordering, final X x1, final X x2, final X x3, final Consumer3<X> c) {
        final X x1a = get(ordering.mapIdx(0), x1, x2, x3);
        final X x2a = get(ordering.mapIdx(1), x1, x2, x3);
        final X x3a = get(ordering.mapIdx(2), x1, x2, x3);
        c.accept(x1a, x2a, x3a);
    }

    static <X, Z> Z apply(final TupleMap ordering, final X x1, final X x2, final X x3, final TriOperator<X, Z> f) {
        final X x1a = get(ordering.mapIdx(0), x1, x2, x3);
        final X x2a = get(ordering.mapIdx(1), x1, x2, x3);
        final X x3a = get(ordering.mapIdx(2), x1, x2, x3);
        return f.apply(x1a, x2a, x3a);
    }

    default Triple unmap(final TupleMap order, final Node first, final Node second, final Node third) {
        return apply(order, first, second, third, Triple::new);
    }

    static <X> X get(final int i, final X x1, final X x2, final X x3) {
        switch (i) {
        case 0: return x1;
        case 1: return x2;
        case 2: return x3;
        default: throw new IndexOutOfBoundsException("Triples have components 0, 1, 2 but index = " + i + "!");
        }
    }
}
