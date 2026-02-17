/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.engine.iterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.SortedDataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.atlas.iterator.IteratorDelayedInitialization;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.system.SerializationFactoryFinder;

/**
 * Sort a query iterator. The sort will happen in-memory unless the size of the
 * iterator exceeds a configurable threshold. In that case, a disk sort is used.
 *
 * @see SortedDataBag
 */

public class QueryIterSort extends QueryIterPlainWrapper {
    private final QueryIterator inputIterator;
    /*package*/ final SortedDataBag<Binding> dataBag;

    public static QueryIterator create(QueryIterator qIter, List<SortCondition> conditions, ExecutionContext context) {
        return create(qIter, new BindingComparator(conditions, context), context);
    }

    public static  QueryIterator create(QueryIterator qIter, Comparator<Binding> comparator, ExecutionContext context) {
        return new QueryIterSort(qIter, comparator, context);
    }

    private QueryIterSort(QueryIterator qIter, Comparator<Binding> comparator, ExecutionContext context) {
        super(null, context);
        this.inputIterator = qIter;
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(context.getContext());
        this.dataBag = BagFactory.newSortedBag(policy, SerializationFactoryFinder.bindingSerializationFactory(), comparator);
        this.setIterator(new SortedBindingIterator(qIter));
    }

    @Override
    public void requestCancel() {
        this.dataBag.cancel();
        this.inputIterator.cancel();
        super.requestCancel();
    }

    @Override
    protected void closeIterator() {
        this.dataBag.close();
        this.inputIterator.close();
        super.closeIterator();
    }

    private class SortedBindingIterator extends IteratorDelayedInitialization<Binding> implements Closeable {
        private final QueryIterator qIter;

        public SortedBindingIterator(final QueryIterator qIter) {
            this.qIter = qIter;
        }

        @Override
        protected Iterator<Binding> initializeIterator() {
            try {
                dataBag.addAll(qIter);
                return dataBag.iterator();
            }
            // Should we catch other exceptions too? Theoretically
            // the user should be using this
            // iterator in a try/finally block, and thus will call
            // close() themselves.
            catch (QueryCancelledException e) {
                QueryIterSort.this.close();
                close();
                throw e;
            }
        }

        @Override
        public void close() {
            dataBag.close();
        }
    }

}
