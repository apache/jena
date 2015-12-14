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
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.ColumnMap;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.slf4j.Logger;

/**
 * A partial implementation of {@link TupleTable} that contains some common state management.
 *
 * @param <TupleMapType> the type of the internal structure holding table data
 * @param <TupleType> the type of tuple in which a subclass of this class transacts
 */
public abstract class PMapTupleTable<TupleMapType, TupleType> implements TupleTable<TupleType> {

    private final TupleOrdering<TupleType> ordering;
    
    protected TupleOrdering<TupleType> ordering() { return ordering; }
    
    /**
     * A specialization of {@link ColumnMap} for work with tuples of {@link Node}s.
     *
     * @param <TupleType>
     */
    public static abstract class TupleOrdering<TupleType> extends ColumnMap {
        
        /**
         * @param label A uninterpreted label for this ordering
         * @param input the canonical (external) ordering of TupleType
         * @param output this internal ordering of TupleType
         */
        public TupleOrdering(final String label, final String input, final String output) {
            super(label, input, output);
        }
        
        /**
         * @param t a tuple
         * @return the elements of the tuple, in internal order
         */
        public abstract Node[] map(TupleType t);
        
        /**
         * Unmap and create a tuple in external order.
         * 
         * @param elements
         * @return a TupleType
         */
        public abstract TupleType unmapAndCreate(Node... elements);
    }
    
    /**
     * This method should always return the same value, but note that the same value may not necessarily be the same
     * instance.
     *
     * @return a value to which to initialize the master table data.
     */
    protected abstract TupleMapType initial();

    private final AtomicReference<TupleMapType> master = new AtomicReference<>(initial());

    /**
     * We use an {@link AtomicReference} to the internal structure that holds our table data to be able to swap
     * transactional versions of the data with the shared version atomically.
     */
    protected AtomicReference<TupleMapType> master() {
        return master;
    }

    private final ThreadLocal<TupleMapType> local = withInitial(() -> master().get());

    /**
     * @return a thread-local transactional reference to the internal table structure
     */
    protected ThreadLocal<TupleMapType> local() {
        return local;
    }

    private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

    @Override
    public boolean isInTransaction() {
        return isInTransaction.get();
    }

    protected void isInTransaction(final boolean b) {
        isInTransaction.set(b);
    }

    /**
     * @param order the internal ordering for this table
     */
    public PMapTupleTable(final TupleOrdering<TupleType> order) {
        this.ordering = order;
    }

    protected abstract Logger log();
    
    protected void mutate(TupleType t, Consumer<Node[]> action) {
        action.accept(ordering().map(t));
    }

    /**
     * Logs to DEBUG prepending the table name in order to distinguish amongst different indexes
     */
    protected void debug(final String msg, final Object... values) {
        if (log().isDebugEnabled()) log().debug(ordering.getLabel() + ": " + msg, values);
    }

    @Override
    public void begin(final ReadWrite rw) {
        isInTransaction(true);
    }

    @Override
    public void end() {
        debug("Abandoning transactional reference.");
        local.remove();
        isInTransaction.remove();
    }

    @Override
    public void commit() {
        debug("Swapping transactional reference in for shared reference");
        master().set(local.get());
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
