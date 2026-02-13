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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.http.sys.UpdateEltAcc;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.adapter.ParseCheckUtils;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Timeouts.TimeoutBuilderImpl;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.tracker.UpdateExecTransform;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public abstract class UpdateExecDatasetBuilderBase<X extends UpdateExecDatasetBuilderBase<X>>
    implements UpdateExecDatasetBuilder
{

    protected DatasetGraph dataset              = null;
    protected ContextAccumulator contextAcc     = ContextAccumulator.newBuilder(()->ARQ.getContext(), ()->Context.fromDataset(dataset));

    // Uses query rewrite to replace variables by values.
    protected Map<Var, Node> substitutionMap    = null;

    protected TimeoutBuilderImpl timeoutBuilder = new TimeoutBuilderImpl();

    protected Boolean parseCheck                = null;
    protected UpdateEltAcc updateEltAcc         = new UpdateEltAcc();

    protected List<UpdateExecTransform> updateExecTransforms = new ArrayList<>();

    // private UpdateExecBuilder() {}

    @SuppressWarnings("unchecked")
    private X thisBuilder() {
        return (X)this;
    }

    /** Append the updates in an {@link UpdateRequest} to the {@link UpdateRequest} being built. */
    @Override
    public X update(UpdateRequest updateRequest) {
        Objects.requireNonNull(updateRequest);
        add(updateRequest);
        return thisBuilder();
    }

    /** Add the {@link Update} to the {@link UpdateRequest} being built. */
    @Override
    public X update(Update update) {
        Objects.requireNonNull(update);
        add(update);
        return thisBuilder();
    }

    /** Parse and update operations to the {@link UpdateRequest} being built. */
    @Override
    public X update(String updateRequestString) {
        if (effectiveParseCheck()) {
            UpdateRequest more = UpdateFactory.create(updateRequestString);
            add(more);
        } else {
            updateEltAcc.add(updateRequestString);
        }
        return thisBuilder();
    }

    @Override
    public X parseCheck(boolean parseCheck) {
        this.parseCheck = parseCheck;
        return thisBuilder();
    }

    protected boolean effectiveParseCheck() {
        return ParseCheckUtils.effectiveParseCheck(parseCheck, contextAcc);
    }

    @Override
    public X dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return thisBuilder();
    }

    /** Set the {@link Context}.
     *  This defaults to the global settings of {@code ARQ.getContext()}.
     *  If there was a previous call of {@code context} the multiple contexts are merged.
     * */
    @Override
    public X context(Context context) {
        if ( context == null )
            return thisBuilder();
        this.contextAcc.context(context);
        return thisBuilder();
    }

    @Override
    public X set(Symbol symbol, Object value) {
        this.contextAcc.set(symbol, value);
        return thisBuilder();
    }

    @Override
    public X set(Symbol symbol, boolean value) {
        this.contextAcc.set(symbol, value);
        return thisBuilder();
    }

    public Context getContext() {
        return contextAcc.context();
    }

    @Override
    public X substitution(Binding binding) {
        ensureSubstitutionMap();
        binding.forEach(this.substitutionMap::put);
        return thisBuilder();
    }

    @Override
    public X substitution(Var var, Node value) {
        ensureSubstitutionMap();
        this.substitutionMap.put(var, value);
        return thisBuilder();
    }

    private void ensureSubstitutionMap() {
        if ( substitutionMap == null )
            substitutionMap = new HashMap<>();
    }

    @Override
    public X timeout(long timeout, TimeUnit timeoutUnit) {
        this.timeoutBuilder.timeout(timeout, timeoutUnit);
        return thisBuilder();
    }

    @Override
    public X transformExec(UpdateExecTransform updateExecTransform) {
        updateExecTransforms.add(updateExecTransform);
        return thisBuilder();
    }

    private void add(UpdateRequest request) {
        updateEltAcc.add(request);
    }

    private void add(Update update) {
        updateEltAcc.add(update);
    }
}
