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

package org.apache.jena.sparql.exec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.http.sys.UpdateEltAcc;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.Timeouts.TimeoutBuilderImpl;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.dispatch.SparqlDispatcherRegistry;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecDatasetBuilder implements UpdateExecBuilder {

    public static UpdateExecDatasetBuilder create() { return new UpdateExecDatasetBuilder(); }

    private DatasetGraph dataset              = null;
    private ContextAccumulator contextAcc     = ContextAccumulator.newBuilder(()->ARQ.getContext(), ()->Context.fromDataset(dataset));

    // Uses query rewrite to replace variables by values.
    private Map<Var, Node> substitutionMap    = null;

    private Binding        initialBinding     = null;

    private TimeoutBuilderImpl timeoutBuilder = new TimeoutBuilderImpl();

    private Optional<Boolean> parseCheck      = Optional.empty();
    private UpdateEltAcc updateEltAcc         = new UpdateEltAcc();

    private UpdateExecDatasetBuilder() {}

    /** Append the updates in an {@link UpdateRequest} to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecDatasetBuilder update(UpdateRequest updateRequest) {
        Objects.requireNonNull(updateRequest);
        add(updateRequest);
        return this;
    }

    /** Add the {@link Update} to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecDatasetBuilder update(Update update) {
        Objects.requireNonNull(update);
        add(update);
        return this;
    }

    /** Parse and update operations to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecDatasetBuilder update(String updateRequestString) {
        if (effectiveParseCheck()) {
            UpdateRequest more = UpdateFactory.create(updateRequestString);
            add(more);
        } else {
            updateEltAcc.add(updateRequestString);
        }
        return this;
    }

    @Override
    public UpdateExecBuilder parseCheck(boolean parseCheck) {
        this.parseCheck = Optional.of(parseCheck);
        return this;
    }

    protected boolean effectiveParseCheck() {
        return SparqlDispatcherRegistry.effectiveParseCheck(parseCheck, contextAcc);
    }

    public UpdateExecDatasetBuilder dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return this;
    }

    /** Set the {@link Context}.
     *  This defaults to the global settings of {@code ARQ.getContext()}.
     *  If there was a previous call of {@code context} the multiple contexts are merged.
     * */
    @Override
    public UpdateExecDatasetBuilder context(Context context) {
        if ( context == null )
            return this;
        this.contextAcc.context(context);
        return this;
    }

    @Override
    public UpdateExecDatasetBuilder set(Symbol symbol, Object value) {
        this.contextAcc.set(symbol, value);
        return this;
    }

    @Override
    public UpdateExecDatasetBuilder set(Symbol symbol, boolean value) {
        this.contextAcc.set(symbol, value);
        return this;
    }

    public Context getContext() {
        return contextAcc.context();
    }

    @Override
    public UpdateExecDatasetBuilder substitution(Binding binding) {
        ensureSubstitutionMap();
        binding.forEach(this.substitutionMap::put);
        return this;
    }

    @Override
    public UpdateExecDatasetBuilder substitution(Var var, Node value) {
        ensureSubstitutionMap();
        this.substitutionMap.put(var, value);
        return this;
    }

    private void ensureSubstitutionMap() {
        if ( substitutionMap == null )
            substitutionMap = new HashMap<>();
    }

    @Override
    public UpdateExecDatasetBuilder timeout(long timeout, TimeUnit timeoutUnit) {
        this.timeoutBuilder.timeout(timeout, timeoutUnit);
        return this;
    }

    /** @deprecated Use {@link #substitution(Binding)} */
    @Deprecated(forRemoval = true)
    public UpdateExecDatasetBuilder initialBinding(Binding initialBinding) {
        this.initialBinding = initialBinding;
        return this;
    }

    @Override
    public UpdateExec build() {
        UpdateRequest actualUpdate = null;

        if ( substitutionMap != null && ! substitutionMap.isEmpty() ) {
            actualUpdate = updateEltAcc.buildUpdateRequest();
            actualUpdate = UpdateTransformOps.transform(actualUpdate, substitutionMap);
        }

        Context cxt = getContext();

        Timeouts.applyDefaultUpdateTimeoutFromContext(timeoutBuilder, cxt);
        Timeout timeout = timeoutBuilder.build();
        Timeouts.setUpdateTimeout(cxt, timeout);

        UpdateExec uExec;
        if (updateEltAcc.isParsed()) {
            if (actualUpdate == null) {
                actualUpdate = updateEltAcc.buildUpdateRequest();
            }
            uExec = SparqlDispatcherRegistry.exec(actualUpdate, dataset, initialBinding, cxt);
        } else {
            String actualString = updateEltAcc.buildString();
            uExec = SparqlDispatcherRegistry.exec(actualString, dataset, initialBinding, cxt);
        }
        return uExec;
    }

    // Abbreviated forms

    @Override
    public void execute() {
        build().execute();
    }

    public void execute(DatasetGraph dsg) {
        dataset(dsg);
        execute();
    }

    private void add(UpdateRequest request) {
        updateEltAcc.add(request);
    }

    private void add(Update update) {
        updateEltAcc.add(update);
    }
}
