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

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateProcessorBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecDataset extends UpdateProcessorBase implements UpdateExec {

    protected UpdateExecDataset(UpdateRequest request, DatasetGraph datasetGraph,
                                Binding inputBinding, Context context, UpdateEngineFactory factory) {
        super(request, datasetGraph, inputBinding, context, factory);
    }

    @Override
    public void execute() {
        super.execute();
    }
}

