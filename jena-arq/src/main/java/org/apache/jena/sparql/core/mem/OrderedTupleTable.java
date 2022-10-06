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

import java.util.function.Consumer;

import org.apache.jena.atlas.lib.tuple.TConsumer3;
import org.apache.jena.atlas.lib.tuple.TConsumer4;
import org.apache.jena.atlas.lib.tuple.TFunction3;
import org.apache.jena.atlas.lib.tuple.TFunction4;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/**
 * A table of {@code TupleType} tuples that uses an internal order recorded via {@link TupleMap}. In this code, names
 * {@code g}, {@code s}, {@code p} and {@code o} are used for the components of a tuple in canonical order, and
 * {@code x1} through {@code x4} are used for the components of a tuple in internal order.
 *
 * @param <TupleType> the tuple type in which an instance of this class works, typically {@link Triple} or {@link Quad}
 * @param <ConsumerType> a consumer type that can accept the elements of a {@code TupleType}, typically
 *            {@link TConsumer3} or {@link TConsumer4}
 */
public abstract class OrderedTupleTable<TupleType, ConsumerType> implements TupleTable<TupleType> {

    /**
     * The order in which elements are held in this table, e.g. SPO or OSGP.
     */
    protected final TupleMap order;

    /**
     * The reverse of {@link #order}.
     */
    protected final TupleMap reverse;

    /**
     * @param order the order of elements in this table
     */
    public OrderedTupleTable(final TupleMap order) {
        this.order = order;
        this.reverse = order.reverse();
    }

    /**
     * @return a consumer that adds a tuple in the form of the elements in a {@code TupleType} to this table
     */
    protected abstract ConsumerType add();

    /**
     * @return a consumer that removes a tuple in the form of the elements in a {@code TupleType} from this table
     */
    protected abstract ConsumerType delete();

    protected Consumer<Quad> map(final TConsumer4<Node> consumer) {
        return q -> {
            final Node g = q.getGraph();
            final Node s = q.getSubject();
            final Node p = q.getPredicate();
            final Node o = q.getObject();
            final Node x1 = get(order.mapIdx(0), g, s, p, o);
            final Node x2 = get(order.mapIdx(1), g, s, p, o);
            final Node x3 = get(order.mapIdx(2), g, s, p, o);
            final Node x4 = get(order.mapIdx(3), g, s, p, o);
            consumer.accept(x1, x2, x3, x4);
        };
    }

    protected <X> TFunction4<Node, X> map(final TFunction4<Node, X> f) {
        return (g, s, p, o) -> apply(order, g, s, p, o, f);
    }

    protected Quad unmap(final Node x1, final Node x2, final Node x3, final Node x4) {
        return apply(reverse, x1, x2, x3, x4, Quad::new);
    }

    protected Consumer<Triple> map(final TConsumer3<Node> consumer) {
        return t -> {
            final Node s = t.getSubject();
            final Node p = t.getPredicate();
            final Node o = t.getObject();
            final Node x1 = get(order.mapIdx(0), s, p, o);
            final Node x2 = get(order.mapIdx(1), s, p, o);
            final Node x3 = get(order.mapIdx(2), s, p, o);
            consumer.accept(x1, x2, x3);
        };
    }

    protected <T, X> TFunction3<T, X> map(final TFunction3<T, X> f) {
        return (s, p, o) -> OrderedTupleTable.apply(order, s, p, o, f);
    }

    protected Triple unmap(final Node x1, final Node x2, final Node x3) {
        return apply(reverse, x1, x2, x3, Triple::create);
    }

    private static <X> X get(final int i, final X x1, final X x2, final X x3) {
        switch (i) {
        case 0:
            return x1;
        case 1:
            return x2;
        case 2:
            return x3;
        default:
            throw new IndexOutOfBoundsException("Triples have components 0, 1, 2 but index = " + i + "!");
        }
    }

    private static <X> X get(final int i, final X x1, final X x2, final X x3, final X x4) {
        switch (i) {
        case 0:
            return x1;
        case 1:
            return x2;
        case 2:
            return x3;
        case 3:
            return x4;
        default:
            throw new IndexOutOfBoundsException("Quads have components 0, 1, 2, 3 but index = " + i + "!");
        }
    }

    private static <X, Z> Z apply(final TupleMap tupleMap, final X x1, final X x2, final X x3, final X x4,
            final TFunction4<X, Z> f) {
        final X x1a = get(tupleMap.mapIdx(0), x1, x2, x3, x4);
        final X x2a = get(tupleMap.mapIdx(1), x1, x2, x3, x4);
        final X x3a = get(tupleMap.mapIdx(2), x1, x2, x3, x4);
        final X x4a = get(tupleMap.mapIdx(3), x1, x2, x3, x4);
        return f.apply(x1a, x2a, x3a, x4a);
    }

    private static <X, Z> Z apply(final TupleMap ordering, final X x1, final X x2, final X x3,
            final TFunction3<X, Z> f) {
        final X x1a = get(ordering.mapIdx(0), x1, x2, x3);
        final X x2a = get(ordering.mapIdx(1), x1, x2, x3);
        final X x3a = get(ordering.mapIdx(2), x1, x2, x3);
        return f.apply(x1a, x2a, x3a);
    }
}
