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

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.modify.UpdateProcessorStreamingBase;
import org.apache.jena.sparql.util.Context;

public class UpdateStreaming {

    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param datasetGraph
     * @return UpdateExecution
     */
    @Deprecated
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph) {
        return makeStreaming(datasetGraph, null, null);
    }

    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateExecution
     */
    @Deprecated
    public static UpdateProcessorStreaming createStreaming(Dataset dataset, QuerySolution inputBinding) {
        return createStreaming(dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding));
    }

    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateExecution
     * @deprecated Use {@code UpdateExecution.dataset(dataset)... build()}
     */
    @Deprecated
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph, Binding inputBinding) {
        return makeStreaming(datasetGraph, inputBinding, null);
    }

    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param dataset
     * @param context (null means use merge of global and graph store context))
     * @return UpdateExecution
     */
    @Deprecated
    public static UpdateProcessorStreaming createStreaming(Dataset dataset, Context context) {
        return makeStreaming(dataset.asDatasetGraph(), null, context);
    }

    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param datasetGraph
     * @param context (null means use merge of global and graph store context))
     * @return UpdateExecution
     */
    @Deprecated
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph, Context context) {
        return makeStreaming(datasetGraph, null, context);
    }

    // Everything for local updates comes through one of these two make methods
    /*package*/ static UpdateProcessorStreaming makeStreaming(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        Prologue prologue = new Prologue();
        Context cxt = Context.setupContextForDataset(context, datasetGraph);
        UpdateEngineFactory f = UpdateEngineRegistry.get().find(datasetGraph, cxt);
        UpdateProcessorStreamingBase uProc = new UpdateProcessorStreamingBase(datasetGraph, inputBinding, prologue, cxt, f);
        return uProc;
    }

}
