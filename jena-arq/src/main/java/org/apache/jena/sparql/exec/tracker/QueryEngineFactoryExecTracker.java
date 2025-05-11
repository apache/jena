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

package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineFactoryWrapper;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.exec.tracker.ExecTracker.CompletionRecord;
import org.apache.jena.sparql.util.Context;

public class QueryEngineFactoryExecTracker
    extends QueryEngineFactoryWrapper
{
    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        boolean result = false;
        if (dataset instanceof DatasetGraphWithExecTracker tracker) {
            DatasetGraph backend = tracker.getWrapped();
            QueryEngineFactory f = QueryEngineRegistry.findFactory(query, backend, context);
            result = f.accept(query, backend, context);
        }
        return result;
    }

    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphWithExecTracker tracker = (DatasetGraphWithExecTracker)dataset;
        ExecTracker execTracker = ExecTracker.requireTracker(tracker.getContext());
        DatasetGraph next = tracker.getWrapped();
        QueryEngineFactory f = QueryEngineRegistry.findFactory(query, next, context);
        Plan base = f.create(query, next, inputBinding, context);
        return new TrackingPlan(base, execTracker, context, query);
    }

    @Override
    public boolean accept(Op op, DatasetGraph dataset, Context context) {
        boolean result = false;
        if (dataset instanceof DatasetGraphWithExecTracker tracker) {
            DatasetGraph next = tracker.getWrapped();
            QueryEngineFactory f = QueryEngineRegistry.findFactory(op, next, context);
            result = f.accept(op, next, context);
        }
        return result;
    }

    @Override
    public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphWithExecTracker tracker = (DatasetGraphWithExecTracker)dataset;
        ExecTracker execTracker = ExecTracker.requireTracker(tracker.getContext());
        DatasetGraph next = tracker.getWrapped();
        QueryEngineFactory f = QueryEngineRegistry.findFactory(op, next, context);
        Plan base = f.create(op, next, inputBinding, context);
        return new TrackingPlan(base, execTracker, context, op);
    }

    static class TrackingPlan
        extends PlanWrapperBase
    {
        protected ExecTracker execTracker;
        protected Object label;
        protected Context context;

        public TrackingPlan(Plan delegate, ExecTracker execTracker, Context context, Object label) {
            super(delegate);
            this.execTracker = execTracker;
            this.context = context;
            this.label = label;
        }

        public Context getContext() {
            return context;
        }

        @Override
        public QueryIterator iterator() {
            QueryIterator base = getDelegate().iterator();

            // Set before this method returns.
            long[] idRef = {-1};

            QueryIterator result = new QueryIteratorWrapper(base) {
                protected Throwable t = null;

                @Override
                protected boolean hasNextBinding() {
                    try {
                        return super.hasNextBinding();
                    } catch (Throwable throwable) {
                        throwable.addSuppressed(new RuntimeException("Tracked exception"));
                        t = throwable;
                        throw throwable;
                    }
                }

                @Override
                protected Binding moveToNextBinding() {
                    try {
                        return super.moveToNextBinding();
                    } catch (Throwable throwable) {
                        throwable.addSuppressed(new RuntimeException("Tracked exception"));
                        t = throwable;
                        throw throwable;
                    }
                }

                @Override
                protected void closeIterator() {
                    CompletionRecord completionRecord = execTracker.remove(idRef[0], t);
                    super.closeIterator();
                }
            };
            idRef[0] = execTracker.put(label, result::cancel);
            return result;
        }
    }
}
