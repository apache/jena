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

import java.util.stream.Stream;

import org.apache.jena.atlas.lib.persistent.PMap;
import org.apache.jena.atlas.lib.persistent.PersistentSet;
import org.apache.jena.atlas.lib.tuple.TConsumer4;
import org.apache.jena.atlas.lib.tuple.TFunction4;
import org.apache.jena.atlas.lib.tuple.TupleMap;
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
public class PMapQuadTable extends PMapTupleTable<FourTupleMap, Quad, TConsumer4<Node>>implements QuadTable {

    /**
     * @param order an internal order for this table
     */
    public PMapQuadTable(final String order) {
        this("GSPO", order);
    }

    /**
     * @param canonical the canonical order outside this table
     * @param order the internal order for this table
     */
    public PMapQuadTable(final String canonical, final String order) {
        this(canonical + "->" + order, TupleMap.create(canonical, order));
    }

    /**
     * @param tableName a name for this table
     * @param order the order of elements in this table
     */
    public PMapQuadTable(final String tableName, final TupleMap order) {
        super(tableName, order);
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
    
    
    @Override
    public void add(final Quad q) {
        map(add()).accept(q);
    }

    @Override
    public void delete(final Quad q) {
        map(delete()).accept(q);
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return map(find).apply(g, s, p, o);
    }
    
    /**
     * We descend through the nested {@link PMap}s building up {@link Stream}s of partial tuples from which we develop a
     * {@link Stream} of full tuples which is our result. Use {@link Node#ANY} or <code>null</code> for a wildcard.
     */
    @SuppressWarnings("unchecked") // Because of (Stream<Quad>) -- but why is that needed?
    private TFunction4<Node, Stream<Quad>> find = (first, second, third, fourth) -> {
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
                                    return oneTuples.contains(fourth) ? of(unmap(first, second, third, fourth))
                                            : empty();
                                }
                                debug("Using a wildcard fourth slot value.");
                                return oneTuples.stream().map(slot4 -> unmap(first, second, third, slot4));
                            }).orElse(empty());

                        }
                        debug("Using wildcard third and fourth slot values.");
                        return twoTuples.flatten((slot3, oneTuples) -> oneTuples.stream()
                                .map(slot4 -> unmap(first, second, slot3, slot4)));
                    }).orElse(empty());
                }
                debug("Using wildcard second, third and fourth slot values.");
                return threeTuples.flatten((slot2, twoTuples) -> twoTuples.flatten(
                        (slot3, oneTuples) -> oneTuples.stream().map(slot4 -> unmap(first, slot2, slot3, slot4))));
            }).orElse(empty());
        }
        debug("Using a wildcard for all slot values.");
        return fourTuples.flatten((slot1, threeTuples) -> threeTuples.flatten((slot2, twoTuples) -> twoTuples
                .flatten((slot3, oneTuples) -> oneTuples.stream().map(slot4 -> unmap(slot1, slot2, slot3, slot4)))));
    };
    
    @Override
    protected TConsumer4<Node> add() {
        return (first, second, third, fourth) -> {
            debug("Adding four-tuple: {} {} {} {} .", first, second, third, fourth);
            final FourTupleMap fourTuples = local().get();
            ThreeTupleMap threeTuples = fourTuples.get(first).orElse(new ThreeTupleMap());
            TwoTupleMap twoTuples = threeTuples.get(second).orElse(new TwoTupleMap());
            PersistentSet<Node> oneTuples = twoTuples.get(third).orElse(PersistentSet.empty());

            if (!oneTuples.contains(fourth)) oneTuples = oneTuples.plus(fourth);
            twoTuples = twoTuples.minus(third).plus(third, oneTuples);
            threeTuples = threeTuples.minus(second).plus(second, twoTuples);
            debug("Setting transactional index to new value.");
            local().set(fourTuples.minus(first).plus(first, threeTuples));
        };
    }

    @Override
    protected TConsumer4<Node> delete() {
        return (first, second, third, fourth) -> {
            debug("Removing four-tuple: {} {} {} {} .", first, second, third, fourth);
            final FourTupleMap fourTuples = local().get();
            fourTuples.get(first).ifPresent(threeTuples -> threeTuples.get(second)
                    .ifPresent(twoTuples -> twoTuples.get(third).ifPresent(oneTuples -> {
                        if (oneTuples.contains(fourth)) {
                            oneTuples = oneTuples.minus(fourth);
                            final TwoTupleMap newTwoTuples = oneTuples.asSet().isEmpty()
                                    ? twoTuples.minus(third)
                                    : twoTuples.minus(third).plus(third, oneTuples);
                            final ThreeTupleMap newThreeTuples = newTwoTuples.asMap().isEmpty()
                                    ? threeTuples.minus(second)
                                    : threeTuples.minus(second).plus(second, newTwoTuples);
                            final FourTupleMap newFourTuples = newThreeTuples.asMap().isEmpty()
                                    ? fourTuples.minus(first)
                                    : fourTuples.minus(first).plus(first, newThreeTuples);
                            debug("Setting transactional index to new value.");
                            local().set(newFourTuples);
                        }
                    })));
        };
    }
}
