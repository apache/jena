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

import java.util.Objects;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateException;
import org.apache.jena.update.UpdateRequest;

/**
 * The main dataset-centric update exec builder of ARQ.
 * Chooses the appropriate update engine from the {@link UpdateEngineRegistry}.
 */
public class UpdateExecDatasetBuilderImpl
    extends UpdateExecBuilderBase<UpdateExecDatasetBuilderImpl>
    implements UpdateExecDatasetBuilder
{
    public static UpdateExecDatasetBuilderImpl create() { return new UpdateExecDatasetBuilderImpl(); }

    private UpdateExecDatasetBuilderImpl() {}

    @Override
    public UpdateExecDatasetBuilderImpl dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return this;
    }

    /** Always parse updates regardless of the parse check hint. */
    @Override
    protected boolean effectiveParseCheck() {
        return true;
    }

    @Override
    public UpdateExec build() {
        Objects.requireNonNull(dataset, "No dataset for update");
        UpdateRequest actualUpdate = updateEltAcc.buildUpdateRequest();

        if ( substitutionMap != null && ! substitutionMap.isEmpty() )
            actualUpdate = UpdateTransformOps.transform(actualUpdate, substitutionMap);

        Context cxt = getContext();
        UpdateEngineFactory f = UpdateEngineRegistry.get().find(dataset, cxt);
        if ( f == null )
            throw new UpdateException("Failed to find an UpdateEngine");

        Timeout timeout = timeoutBuilder.build();

        Binding initialBinding = null;
        UpdateExec uExec = new UpdateExecDataset(actualUpdate, dataset, initialBinding, cxt, f, timeout);
        return uExec;
    }

    // Abbreviated forms

    public void execute(DatasetGraph dsg) {
        dataset(dsg);
        execute();
    }
}

