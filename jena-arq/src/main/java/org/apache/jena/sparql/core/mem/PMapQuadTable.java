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
import static java.util.stream.Stream.of;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.persistent.PMap;
import org.apache.jena.atlas.lib.persistent.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;
import org.slf4j.Logger;

/**
 * An implementation of {@link QuadTable} based on the use of nested {@link PMap}s. Intended for high-speed in-memory
 * use.
 *
 */
public class PMapQuadTable extends PMapTupleTable<FourTupleMap, Quad> implements QuadTable {

    /**
     * @param order the internal ordering for this table
     */
    public PMapQuadTable(final QuadOrdering order) {
        super(order);
    }

    private static final Logger log = getLogger(PMapQuadTable.class);

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    protected FourTupleMap initial() {
        return new FourTupleMap();
    }

    /**
     * Constructs a {@link Quad} from the nodes given, using the appropriate order for this table. E.g. a OPSG table
     * should return a {@code Quad} using ({@code fourth}, {@code third}, {@code second}, {@code first}).
     *
     * @param first
     * @param second
     * @param third
     * @param fourth
     * @return a {@code Quad}
     */
    protected Quad quad(final Node first, final Node second, final Node third, final Node fourth) {
        return ordering().unmapAndCreate(first, second, third, fourth);
    }
    

    @Override
    public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
        final Node[] mapped = ordering().map(g, s, p, o);
        return _find(mapped[0], mapped[1], mapped[2], mapped[3]);
    }


    /**
     * We descend through the nested {@link PMap}s building up {@link Stream}s of partial tuples from which we develop a
     * {@link Stream} of full tuples which is our result. Use {@link Node#ANY} or <code>null</code> for a wildcard.
     *
     * @param first the value in the first slot of the tuple
     * @param second the value in the second slot of the tuple
     * @param third the value in the third slot of the tuple
     * @param fourth the value in the fourth slot of the tuple
     * @return a <code>Stream</code> of tuples matching the pattern
     */
    @SuppressWarnings("unchecked") // Because of (Stream<Quad>) -- but why is that needed?
    protected Stream<Quad> _find(final Node first, final Node second, final Node third, final Node fourth) {
        debug("Querying on four-tuple pattern: {} {} {} {} .", first, second, third, fourth);
        final FourTupleMap fourTuples = local().get();
        if (isConcrete(first)) {
            debug("Using a specific first slot value.");
            return (Stream<Quad>) fourTuples.get(first).map(threeTuples -> {
                if (isConcrete(second)) {
                    debug("Using a specific second slot value.");
                    return threeTuples.get(second).map(twoTuples -> {
                        if (isConcrete(third)) {
                            debug("Using a specific third slot value.");
                            return twoTuples.get(third).map(oneTuples -> {
                                if (isConcrete(fourth)) {
                                    debug("Using a specific fourth slot value.");
                                    return oneTuples
                                            .contains(fourth) ? of(quad(first, second, third, fourth)) : empty();
                                }
                                debug("Using a wildcard fourth slot value.");
                                return oneTuples.stream().map(slot4 -> quad(first, second, third, slot4));
                            }).orElse(empty());

                        }
                        debug("Using wildcard third and fourth slot values.");
                        return twoTuples.flatten((slot3, oneTuples) -> oneTuples.stream()
                                .map(slot4 -> quad(first, second, slot3, slot4)));
                    }).orElse(empty());
                }
                debug("Using wildcard second, third and fourth slot values.");
                return threeTuples.flatten((slot2, twoTuples) -> twoTuples.flatten(
                        (slot3, oneTuples) -> oneTuples.stream().map(slot4 -> quad(first, slot2, slot3, slot4))));
            }).orElse(empty());
        }
        debug("Using a wildcard for all slot values.");
        return fourTuples.flatten((slot1, threeTuples) -> threeTuples.flatten((slot2, twoTuples) -> twoTuples
                .flatten((slot3, oneTuples) -> oneTuples.stream().map(slot4 -> quad(slot1, slot2, slot3, slot4)))));
    }

    @Override
    public void add(Quad q) {
        mutate(q, _add);
    }

    @Override
    public void delete(Quad q) {
        mutate(q, _delete);
    }

    protected Consumer<Node[]> _add = nodes -> {
        debug("Adding four-tuple: {} {} {} {} .", nodes[0], nodes[1], nodes[2], nodes[3]);
        final FourTupleMap fourTuples = local().get();
        ThreeTupleMap threeTuples = fourTuples.get(nodes[0]).orElse(new ThreeTupleMap());
        TwoTupleMap twoTuples = threeTuples.get(nodes[1]).orElse(new TwoTupleMap());
        PersistentSet<Node> oneTuples = twoTuples.get(nodes[2]).orElse(PersistentSet.empty());

        if (!oneTuples.contains(nodes[3])) oneTuples = oneTuples.plus(nodes[3]);
        twoTuples = twoTuples.minus(nodes[2]).plus(nodes[2], oneTuples);
        threeTuples = threeTuples.minus(nodes[1]).plus(nodes[1], twoTuples);
        debug("Setting transactional index to new value.");
        local().set(fourTuples.minus(nodes[0]).plus(nodes[0], threeTuples));
    };

    protected Consumer<Node[]> _delete = nodes -> {
        debug("Removing four-tuple: {} {} {} {} .", nodes[0], nodes[1], nodes[2], nodes[3]);
        final FourTupleMap fourTuples = local().get();
        fourTuples.get(nodes[0]).ifPresent(threeTuples -> threeTuples.get(nodes[1])
                .ifPresent(twoTuples -> twoTuples.get(nodes[2]).ifPresent(oneTuples -> {
            if (oneTuples.contains(nodes[3])) {
                oneTuples = oneTuples.minus(nodes[3]);
                final TwoTupleMap newTwoTuples = twoTuples.minus(nodes[2]).plus(nodes[2], oneTuples);
                final ThreeTupleMap newThreeTuples = threeTuples.minus(nodes[1]).plus(nodes[1], newTwoTuples);
                debug("Setting transactional index to new value.");
                local().set(fourTuples.minus(nodes[0]).plus(nodes[0], newThreeTuples));
            }
        })));
    };

    protected static class QuadOrdering extends TupleOrdering<Quad> {

        public QuadOrdering(String order) {
            super(order, "GSPO", order);
        }

        @Override
        public Node[] map(Quad q) {
            return map(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
        }

        @Override
        public Quad unmapAndCreate(Node... nodes) {
            if (nodes.length != 4) throw new IllegalArgumentException("Quads must have four nodes!");
            final Node[] unmapped = unmap(nodes);
            return Quad.create(unmapped[0], unmapped[1], unmapped[2], unmapped[3]);
        }
    }
}
