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

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateExecutionBuilder;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionBuilderAdapter implements UpdateExecutionBuilder {

    private final UpdateExecBuilder builder;

    public UpdateExecutionBuilderAdapter(UpdateExecBuilder builder) {
        this.builder = builder;
    }

    /** Adapter that attempts to unwrap an UpdateExecBuilderAdapter builder */
    public static UpdateExecutionBuilder adapt(UpdateExecBuilder builder) {
        UpdateExecutionBuilder result = builder instanceof UpdateExecBuilderAdapter
                ? ((UpdateExecBuilderAdapter)builder).getExecBuilder()
                : new UpdateExecutionBuilderAdapter(builder);

        return result;
    }

    public UpdateExecBuilder getExecBuilder() { return builder; }

    @Override
    public UpdateExecutionBuilder update(UpdateRequest updateRequest) {
        builder.update(updateRequest);
        return this;
    }

    @Override
    public UpdateExecutionBuilder update(Update update) {
        builder.update(update);
        return this;
    }

    @Override
    public UpdateExecutionBuilder update(String updateRequestString) {
        builder.update(updateRequestString);
        return this;
    }

    @Override
    public UpdateExecutionBuilder set(Symbol symbol, Object value) {
        builder.set(symbol, value);
        return this;
    }

    @Override
    public UpdateExecutionBuilder set(Symbol symbol, boolean value) {
        builder.set(symbol, value);
        return this;
    }

    @Override
    public UpdateExecutionBuilder context(Context context) {
        builder.context(context);
        return this;
    }

    @Override
    public UpdateExecutionBuilder substitution(QuerySolution querySolution) {
        Binding binding = BindingLib.toBinding(querySolution);
        builder.substitution(binding);
        return this;
    }

    @Override
    public UpdateExecutionBuilder substitution(String varName, RDFNode value) {
        builder.substitution(varName, value.asNode());
        return this;
    }

    @Override
    public UpdateExecution build() {
        return UpdateExecutionAdapter.adapt(builder.build());
    }
}
