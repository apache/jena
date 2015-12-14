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

package org.apache.jena.sparql.core.mem;

import static java.util.stream.Stream.empty;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.persistent.PMap;
import org.apache.jena.atlas.lib.persistent.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;
import org.slf4j.Logger;

/**
 * A {@link TripleTable} employing persistent maps to index triples in one particular slot order (e.g. SPO, OSP or POS).
 *
 */
public class PMapTripleTable extends PMapTupleTable<ThreeTupleMap, Triple> implements TripleTable {

    private final static Logger log = getLogger(PMapTripleTable.class);

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    protected ThreeTupleMap initial() {
        return new ThreeTupleMap();
    }

    /**
     * @param order the internal ordering for this table
     */
    public PMapTripleTable(final TripleOrder order) {
        super(order);
    }

    @Override
    public Stream<Triple> find(final Node s, final Node p, final Node o) {
        final Node[] mapped = ordering().map(s, p, o);
        return _find(mapped[0], mapped[1], mapped[2]);
    }

    /**
     * We descend through the nested {@link PMap}s building up {@link Stream}s of partial tuples from which we develop a
     * {@link Stream} of full tuples which is our result. Use {@link Node#ANY} or <code>null</code> for a wildcard.
     *
     * @param first the value in the first slot of the tuple
     * @param second the value in the second slot of the tuple
     * @param third the value in the third slot of the tuple
     * @return a <code>Stream</code> of tuples matching the pattern
     */
    @SuppressWarnings("unchecked") // Because of (Stream<Triple>) -- but why is that needed?
    public Stream<Triple> _find(final Node first, final Node second, final Node third) {
        debug("Querying on three-tuple pattern: {} {} {} .", first, second, third);
        ordering().unmap(first, second, third);
        final ThreeTupleMap threeTuples = local().get();
        if (isConcrete(first)) {
            debug("Using a specific first slot value.");
            return (Stream<Triple>) threeTuples.get(first).map(twoTuples -> {
                if (isConcrete(second)) {
                    debug("Using a specific second slot value.");
                    return twoTuples.get(second).map(oneTuples -> {
                        if (isConcrete(third)) {
                            debug("Using a specific third slot value.");
                            return oneTuples.contains(third) ? Stream.of(triple(first, second, third)) : empty();
                        }
                        debug("Using a wildcard third slot value.");
                        return oneTuples.stream().map(slot3 -> triple(first, second, slot3));
                    }).orElse(empty());
                }
                debug("Using wildcard second and third slot values.");
                return twoTuples
                        .flatten((slot2, oneTuples) -> oneTuples.stream().map(slot3 -> triple(first, slot2, slot3)));
            }).orElse(empty());
        }
        debug("Using a wildcard for all slot values.");
        return threeTuples.flatten((slot1, twoTuples) -> twoTuples
                .flatten((slot2, oneTuples) -> oneTuples.stream().map(slot3 -> triple(slot1, slot2, slot3))));
    }

    /**
     * Constructs a {@link Triple} from the nodes given, using the appropriate order for this table. E.g. a POS table
     * should return a {@code Triple} using ({@code second}, {@code third}, {@code first}).
     *
     * @param first
     * @param second
     * @param third
     * @return a {@code Triple}
     */
    protected Triple triple(final Node first, final Node second, final Node third) {
        return ordering().unmapAndCreate(first, second, third);
    }

    @Override
    public void add(final Triple t) {
        mutate(t, _add);
    }

    @Override
    public void delete(final Triple t) {
        mutate(t, _delete);
    }

    protected Consumer<Node[]> _add = nodes -> {
        debug("Adding three-tuple {} {} {}", nodes[0], nodes[1], nodes[2]);
        final ThreeTupleMap threeTuples = local().get();
        TwoTupleMap twoTuples = threeTuples.get(nodes[0]).orElse(new TwoTupleMap());
        PersistentSet<Node> oneTuples = twoTuples.get(nodes[1]).orElse(PersistentSet.empty());

        oneTuples = oneTuples.plus(nodes[2]);
        twoTuples = twoTuples.minus(nodes[1]).plus(nodes[1], oneTuples);
        local().set(threeTuples.minus(nodes[0]).plus(nodes[0], twoTuples));
    };

    protected Consumer<Node[]> _delete = nodes -> {
        debug("Deleting three-tuple {} {} {}", nodes[0], nodes[1], nodes[2]);
        final ThreeTupleMap threeTuples = local().get();
        threeTuples.get(nodes[0]).ifPresent(twoTuples -> twoTuples.get(nodes[1]).ifPresent(oneTuples -> {
            if (oneTuples.contains(nodes[2])) {
                final TwoTupleMap newTwoTuples = twoTuples.minus(nodes[1]).plus(nodes[1], oneTuples.minus(nodes[2]));
                debug("Setting transactional index to new value.");
                local().set(threeTuples.minus(nodes[0]).plus(nodes[0], newTwoTuples));
            }
        }));
    };

    protected static class TripleOrder extends TupleOrdering<Triple> {

        public TripleOrder(String order) {
            super(order, "SPO", order);
        }

        @Override
        public Node[] map(Triple t) {
            return map(t.getSubject(), t.getPredicate(), t.getObject());
        }

        @Override
        public Triple unmapAndCreate(Node... nodes) {
            if (nodes.length != 3) throw new IllegalArgumentException("Triples must have three nodes!");
            final Node[] unmapped = unmap(nodes);
            return Triple.create(unmapped[0], unmapped[1], unmapped[2]);
        }
    }
}
