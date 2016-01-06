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

import static org.apache.jena.sparql.core.mem.QuadTable.accept;
import static org.apache.jena.sparql.core.mem.QuadTable.apply;
import static org.apache.jena.sparql.core.mem.TripleTable.accept;
import static org.apache.jena.sparql.core.mem.TripleTable.apply;

import java.util.function.Consumer;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.atlas.lib.tuple.QuadConsumer.Consumer4;
import org.apache.jena.atlas.lib.tuple.QuadFunction.QuadOperator;
import org.apache.jena.atlas.lib.tuple.TriConsumer.Consumer3;
import org.apache.jena.atlas.lib.tuple.TriFunction.TriOperator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/**
 * A table of {@code TupleType} tuples that uses an internal order recorded via {@link TupleMap}.
 * 
 * @param <TupleType> the tuple type in which an instance of this class works, typically {@link Triple} or {@link Quad}
 * @param <ConsumerType> a consumer type that can accept the elements of a {@link TupleType}, typically
 *            {@link Consumer3} or {@link Consumer4}
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

    protected Consumer<Quad> map(final Consumer4<Node> consumer) {
        return q -> {
            final Node g = q.getGraph();
            final Node s = q.getSubject();
            final Node p = q.getPredicate();
            final Node o = q.getObject();
            accept(order, g, s, p, o, consumer);
        };
    }

    protected <X> QuadOperator<Node, X> map(final QuadOperator<Node, X> f) {
        return (g, s, p, o) -> apply(order, g, s, p, o, f);
    }

    protected Quad unmap(final Node first, final Node second, final Node third, final Node fourth) {
        return apply(reverse, first, second, third, fourth, Quad::new);
    }

    protected Consumer<Triple> map(final Consumer3<Node> consumer) {
        return t -> {
            final Node s = t.getSubject();
            final Node p = t.getPredicate();
            final Node o = t.getObject();
            accept(order, s, p, o, consumer);
        };
    }

    protected <T, X> TriOperator<T, X> map(final TriOperator<T, X> f) {
        return (s, p, o) -> apply(order, s, p, o, f);
    }

    protected Triple unmap(final Node first, final Node second, final Node third) {
        return apply(reverse, first, second, third, Triple::new);
    }
}
