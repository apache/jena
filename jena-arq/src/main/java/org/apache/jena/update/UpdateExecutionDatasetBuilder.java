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

package org.apache.jena.update;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecDatasetBuilder;
import org.apache.jena.sparql.exec.UpdateProcessorAdapter;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class UpdateExecutionDatasetBuilder implements UpdateExecutionBuilder {

    /** Create a new builder of {@link QueryExecution} for a local dataset. */
    public static UpdateExecutionDatasetBuilder newBuilder() { return new UpdateExecutionDatasetBuilder(); }

    public static UpdateExecutionDatasetBuilder create() { return newBuilder(); }

    private final UpdateExecDatasetBuilder builder;

    public UpdateExecutionDatasetBuilder() {
        builder = UpdateExec.newBuilder();
    }

    /** Append the updates in an {@link UpdateRequest} to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecutionDatasetBuilder update(UpdateRequest updateRequest) {
        builder.update(updateRequest);
        return this;
    }

    /** Add the {@link Update} to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecutionDatasetBuilder update(Update update) {
        builder.update(update);
        return this;
    }

    /** Parse and update operations to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecutionDatasetBuilder update(String updateRequestString) {
        builder.update(updateRequestString);
        return this;
    }

    public UpdateExecutionDatasetBuilder dataset(Dataset dataset) {
        builder.dataset(dataset.asDatasetGraph());
        return this;
    }

    @Override
    public UpdateExecutionDatasetBuilder set(Symbol symbol, Object value) {
        builder.set(symbol, value);
        return this;
    }

    @Override
    public UpdateExecutionDatasetBuilder set(Symbol symbol, boolean value) {
        builder.set(symbol, value);
        return this;
    }

    /** Set the {@link Context}.
     *  This defaults to the global settings of {@code ARQ.getContext()}.
     *  If there was a previous call of {@code context} the multiple contexts are merged.
     * */
    @Override
    public UpdateExecutionDatasetBuilder context(Context context) {
        builder.context(context);
        return this;
    }

//    public UpdateExecutionBuilder initialBinding(Binding initialBinding) {
//        builder.initialBinding(initialBinding);
//        return this;
//    }

    public UpdateExecutionDatasetBuilder initialBinding(QuerySolution querySolution) {
        if ( querySolution == null )
            return this;
        Binding binding = BindingLib.asBinding(querySolution);
        builder.initialBinding(binding);
        return this;
    }

    @Override
    public UpdateExecutionDatasetBuilder substitution(QuerySolution querySolution) {
        if ( querySolution == null )
           return this;
        Binding binding = BindingLib.asBinding(querySolution);
        builder.substitution(binding);
        return this;
    }

    @Override
    public UpdateExecutionDatasetBuilder substitution(String varName, RDFNode value) {
        Var var = Var.alloc(varName);
        Node val = value.asNode();
        builder.substitution(var, val);
        return this;
    }

    @Override
    public UpdateExecution build() {
        UpdateExec exec = builder.build();
        return UpdateProcessorAdapter.adapt(exec);
    }

    // Abbreviated forms

    @Override
    public void execute() {
        build().execute();
    }

    public void execute(Dataset dataset) {
        dataset(dataset);
        execute();
    }
}
