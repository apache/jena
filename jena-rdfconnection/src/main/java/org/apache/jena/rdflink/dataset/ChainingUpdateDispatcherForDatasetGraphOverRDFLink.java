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

package org.apache.jena.rdflink.dataset;

import java.util.function.Supplier;

import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.dispatch.ChainingUpdateDispatcher;
import org.apache.jena.sparql.engine.dispatch.SparqlDispatcherRegistry;
import org.apache.jena.sparql.engine.dispatch.UpdateDispatcher;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public class ChainingUpdateDispatcherForDatasetGraphOverRDFLink
    implements ChainingUpdateDispatcher
{
    @Override
    public UpdateExec create(String updateRequestString, DatasetGraph dsg, Binding initialBinding, Context context, UpdateDispatcher chain) {
        UpdateExec result = dsg instanceof DatasetGraphOverRDFLink d
            ? newUpdate(d, initialBinding, context, null, updateRequestString)
            : chain.create(updateRequestString, dsg, initialBinding, context);
        return result;
    }

    @Override
    public UpdateExec create(UpdateRequest updateRequest, DatasetGraph dsg, Binding initialBinding, Context context, UpdateDispatcher chain) {
        UpdateExec result = dsg instanceof DatasetGraphOverRDFLink d
                ? newUpdate(d, initialBinding, context, updateRequest, null)
                : chain.create(updateRequest, dsg, initialBinding, context);
            return result;
    }

    private static UpdateExec newUpdate(DatasetGraphOverRDFLink d, Binding binding, Context requestCxt, UpdateRequest updateRequest, String updateRequestString) {
        return new UpdateExecDeferred(d::newLink, binding, requestCxt, updateRequest, updateRequestString);
    }

    private static void applyTimeouts(UpdateExecBuilder uExec, Timeout t) {
        if (t != null) {
            if (t.hasOverallTimeout()) {
                uExec.timeout(t.overallTimeout().amount(), t.overallTimeout().unit());
            }
        }
    }

    /**
     * Deferred update execution that allocates all resources in the
     * execute() method. Note, that UpdateExec does not have a close method.
     */
    private static class UpdateExecDeferred
        implements UpdateExec
    {
        protected Supplier<RDFLink> linkSupplier;
        protected Binding binding;
        protected Context context;

        protected UpdateRequest updateRequest;
        protected String updateRequestString;

        protected Object cancelLock = new Object();

        protected volatile boolean isAborted = false;
        protected volatile boolean isExecStarted = false;
        protected volatile UpdateExec delegate = null;

        public UpdateExecDeferred(Supplier<RDFLink> linkSupplier, Binding binding, Context context,
            UpdateRequest updateRequest, String updateRequestString) {
            super();
            this.linkSupplier = linkSupplier;
            this.binding = binding;
            this.context = context;
            this.updateRequest = updateRequest;
            this.updateRequestString = updateRequestString;
        }

        @Override
        public Context getContext() {
            return delegate == null ? context : delegate.getContext();
        }

        @Override
        public void abort() {
            synchronized (cancelLock) {
                isAborted = true;
                if (delegate != null) {
                    delegate.abort();
                }
            }
        }

        @Override
        public void execute() {
            RDFLink link = null;
            try {
                synchronized (cancelLock) {
                    if (isExecStarted) {
                        throw new IllegalStateException("Execution was already stated.");
                    }
                    isExecStarted = true;

                    if (isAborted) {
                        throw new QueryCancelledException();
                    }

                    link = linkSupplier.get();
                    UpdateExecBuilder r = link.newUpdate().context(context);

                    if (binding != null) {
                        r.substitution(binding);
                    }

                    Boolean parseCheck = SparqlDispatcherRegistry.getParseCheck(context);
                    if (parseCheck != null) {
                        r.parseCheck(parseCheck);
                    }

                    Timeout timeout = Timeouts.extractUpdateTimeout(context);
                    applyTimeouts(r, timeout);

                    if (updateRequest != null) {
                        r.update(updateRequest);
                    } else {
                        r.update(updateRequestString);
                    }

                    delegate = r.build();
                }

                delegate.execute();
            } finally {
                if (link != null) {
                    link.close();
                }
            }
        }
    }
}
