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

import static java.lang.ThreadLocal.withInitial;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.slf4j.Logger;

/**
 * A partial implementation of {@link TupleTable} that contains some common state management.
 *
 * @param <TupleMapType> the type of the internal structure holding table data
 * @param <TupleType> the type of tuple in which a subclass of this class transacts
 * @param <ConsumerType> a type of consumer that can accept as many elements as exist in {@code TupleType}
 */
public abstract class PMapTupleTable<TupleMapType, TupleType, ConsumerType>
        extends OrderedTupleTable<TupleType, ConsumerType> implements TupleTable<TupleType> {

    /**
     * This method should always return the same value, but note that the same value may not necessarily be the same
     * instance.
     *
     * @return a value to which to initialize the table data.
     */
    protected abstract TupleMapType initial();

    // Current and committed version.
    private final AtomicReference<TupleMapType> current = new AtomicReference<>(initial());

    /**
     * We use an {@link AtomicReference} to the internal structure that holds our table data to be able to swap
     * transactional versions of the data with the shared version atomically.
     */
    protected AtomicReference<TupleMapType> primary() {
        return current;
    }

    private final ThreadLocal<TupleMapType> local = withInitial(()->null);

    /**
     * @return a thread-local transactional reference to the internal table structure
     */
    protected ThreadLocal<TupleMapType> local() {
        return local;
    }

    private final String tableName;

    /**
     * @param n a name for this table
     */
    public PMapTupleTable(final String n, final TupleMap order) {
        super(order);
        this.tableName = n;
    }

    protected abstract Logger log();

    /**
     * Logs to DEBUG prepending the table name in order to distinguish amongst different indexes
     */
    protected void debug(final String msg, final Object... values) {
        if ( log().isDebugEnabled() )
            log().debug(tableName + ": " + msg, values);
    }

    /**
     * {@link #local} is initialized via {@link #initial()}
     */
    @Override
    public void begin(final ReadWrite rw) {
        local.set(primary().get());
    }

    @Override
    public void end() {
        debug("Abandoning transactional reference.");
        local.remove();
    }

    @Override
    public void commit() {
        debug("Swapping transactional reference in for shared reference");
        primary().set(local.get());
        end();
    }

    @Override
    public void clear() {
        local().set(initial());
    }

    protected boolean isConcrete(final Node n) {
        return n != null && n.isConcrete();
    }
}
