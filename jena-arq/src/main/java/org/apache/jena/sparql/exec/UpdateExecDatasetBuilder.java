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

package org.apache.jena.sparql.exec;

import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.tracker.UpdateExecTransform;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public interface UpdateExecDatasetBuilder
    extends UpdateExecBuilder
{
    public static UpdateExecDatasetBuilder create() { return new UpdateExecDatasetBuilderDeferred(); }

    public UpdateExecDatasetBuilder dataset(DatasetGraph dataset);

    @Override public UpdateExecDatasetBuilder update(UpdateRequest request);
    @Override public UpdateExecDatasetBuilder update(Update update);
    @Override public UpdateExecDatasetBuilder update(String updateString);
    @Override public UpdateExecDatasetBuilder parseCheck(boolean parseCheck);
    @Override public UpdateExecDatasetBuilder set(Symbol symbol, Object value);
    @Override public UpdateExecDatasetBuilder set(Symbol symbol, boolean value);
    @Override public UpdateExecDatasetBuilder context(Context context);
    @Override public UpdateExecDatasetBuilder substitution(Binding binding);
    @Override public UpdateExecDatasetBuilder substitution(Var var, Node value);
    @Override public default UpdateExecBuilder substitution(String var, Node value) {
        return substitution(Var.alloc(var), value);
    }

    /** Add a transform that gets applied when building the UpdateExec instance. */
    @Override public UpdateExecDatasetBuilder transformExec(UpdateExecTransform updateExecTransform);
    @Override public UpdateExecDatasetBuilder timeout(long value, TimeUnit timeUnit);
}

