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

import java.util.Objects;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateSendMode;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.modify.UpdateProcessorStreamingBase;
import org.apache.jena.sparql.util.Context ;

/**
 * Create {@link UpdateProcessor} execution objects.
 * <p>
 * For more control of building a local or remote {@link UpdateProcessor} object see the builder pattern:
 * <ul>
 * <li>{@code UpdateProcessor.create(). ... .build()} for querying local data.</li>
 * <li>{@code UpdateProcessorHTTP.service(url). ... .build()} for querying a remote store using HTTP.</li>
 * </ul>
 * <p>
 * See also {@code RDFConnection} for working with SPARQL Query, SPARQL Update and SPARQL Graph Store Protocol together.
 *
 * @see UpdateExecutionBuilder
 * @see UpdateExecutionHTTPBuilder
 */
public class UpdateExecutionFactory
{
    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param update
     * @param dataset
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, Dataset dataset) {
        return create(new UpdateRequest(update), dataset);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param update
     * @param datasetGraph
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, DatasetGraph datasetGraph) {
        return create(new UpdateRequest(update), datasetGraph);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param update
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, Dataset dataset, QuerySolution inputBinding) {
        return create(update, dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding));
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param update
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, DatasetGraph datasetGraph, Binding inputBinding) {
        return create(new UpdateRequest(update), datasetGraph, inputBinding);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param updateRequest
     * @param dataset
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset) {
        return make(updateRequest, dataset, null, null);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param updateRequest
     * @param datasetGraph
     * @return UpdateProcessor or null
     * @deprecated Use {@code UpdateExec.newBuilder(). ... build()}
     */
    @Deprecated
    public static UpdateProcessor create(UpdateRequest updateRequest, DatasetGraph datasetGraph) {
        return make(updateRequest, datasetGraph, null, null);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param updateRequest
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset, QuerySolution inputBinding) {
        return make(updateRequest, dataset, inputBinding, null);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param updateRequest
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     * @deprecated Use {@code UpdateExec.newBuilder(). ... build()}
     */
    @Deprecated
    public static UpdateProcessor create(UpdateRequest updateRequest, DatasetGraph datasetGraph, Binding inputBinding) {
        return make(updateRequest, datasetGraph, inputBinding, null);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param dataset
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset) {
        return makeStreaming(dataset.asDatasetGraph(), null, null);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param datasetGraph
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph) {
        return makeStreaming(datasetGraph, null, null);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset, QuerySolution inputBinding) {
        return createStreaming(dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding));
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph, Binding inputBinding) {
        return makeStreaming(datasetGraph, inputBinding, null);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param dataset
     * @param context (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset, Context context) {
        return makeStreaming(dataset.asDatasetGraph(), null, context);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param datasetGraph
     * @param context (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph, Context context) {
        return makeStreaming(datasetGraph, null, context);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset, QuerySolution inputBinding, Context context) {
        return createStreaming(dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding), context);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        return makeStreaming(datasetGraph, inputBinding, context);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param updateRequest
     * @param dataset
     * @param context (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset, Context context) {
        return make(updateRequest, dataset, null, context);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param updateRequest
     * @param datasetGraph
     * @param context (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, DatasetGraph datasetGraph, Context context) {
        return make(updateRequest, datasetGraph, null, context);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param updateRequest
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset, QuerySolution inputBinding, Context context) {
        return make(updateRequest, dataset, inputBinding, context);
    }

    /**
     * Create an UpdateProcessor appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateProcessor
     *
     * @param updateRequest
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that
     *     can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        return make(updateRequest, datasetGraph, inputBinding, context);
    }

    // Everything for local updates comes through one of these two make methods
    private static UpdateProcessor make(UpdateRequest updateRequest, Dataset dataset, QuerySolution inputBinding, Context context) {
        return UpdateExecution.create().update(updateRequest).dataset(dataset).initialBinding(inputBinding).build();
    }

    // Everything for local updates comes through one of these two make methods
    private static UpdateExec make(UpdateRequest updateRequest, DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        Context cxt = Context.setupContextForDataset(context, datasetGraph);
        return UpdateExec.newBuilder().update(updateRequest).dataset(datasetGraph).initialBinding(inputBinding).context(cxt).build();
    }

    // Everything for local updates comes through one of these two make methods
    private static UpdateProcessorStreaming makeStreaming(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        Prologue prologue = new Prologue();
        Context cxt = Context.setupContextForDataset(context, datasetGraph);
        UpdateEngineFactory f = UpdateEngineRegistry.get().find(datasetGraph, cxt);
        UpdateProcessorStreamingBase uProc = new UpdateProcessorStreamingBase(datasetGraph, inputBinding, prologue, cxt, f);
        return uProc;
    }

    /**
     * Create an UpdateProcessor that sends the update to a remote SPARQL Update
     * service.
     *
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint) {
        return makeRemote(new UpdateRequest(update), remoteEndpoint, null);
    }

    /**
     * Create an UpdateProcessor that sends the update to a remote SPARQL Update
     * service.
     *
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint, Context context) {
        return makeRemote(new UpdateRequest(update), remoteEndpoint, context);
    }

    /**
     * Create an UpdateProcessor that sends the update request to a remote SPARQL
     * Update service.
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint) {
        return makeRemote(updateRequest, remoteEndpoint, null);
    }

    /**
     * Create an UpdateProcessor that sends the update request to a remote SPARQL
     * Update service.
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint, Context context) {
        return makeRemote(updateRequest, remoteEndpoint, context);
    }

    /**
     * Create an UpdateProcessor that sends the update request to a remote SPARQL
     * Update service.
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */

    private static UpdateProcessor makeRemote(UpdateRequest updateRequest, String remoteEndpoint, Context context) {
        return makeRemote(updateRequest, remoteEndpoint, context, UpdateSendMode.asPost);
    }

    /**
     * Create an UpdateProcessor that sends the update request to a remote SPARQL
     * Update service using an HTML form
     *
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint) {
        return makeRemoteForm(new UpdateRequest(update), remoteEndpoint, null);
    }

    /**
     * Create an UpdateProcessor that sends the update request to a remote SPARQL
     * Update service using an HTML form
     *
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint, Context context) {
        return makeRemoteForm(new UpdateRequest(update), remoteEndpoint, context);
    }

    /**
     * Create an UpdateProcessor that sends the update request to a remote SPARQL
     * Update service using an HTML form
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint) {
        return makeRemoteForm(updateRequest, remoteEndpoint, null);
    }

    /**
     * Create an UpdateProcessor that sends the update request to a remote SPARQL
     * Update service using an HTML form
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint, Context context) {
        return makeRemoteForm(updateRequest, remoteEndpoint, context);
    }

    /**
     * Create an UpdateProcessor that sends the update request to a remote SPARQL
     * Update service using an HTML form
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    private static UpdateProcessor makeRemoteForm(UpdateRequest updateRequest, String remoteEndpoint, Context context) {
        return makeRemote(updateRequest, remoteEndpoint, context, UpdateSendMode.asPostForm);
    }

    private static UpdateProcessor makeRemote(UpdateRequest updateRequest, String remoteEndpoint, Context context, UpdateSendMode updateSendMode) {
        Objects.requireNonNull(updateRequest, "updateRequest");
        Objects.requireNonNull(remoteEndpoint, "remoteEndpoint");
        Objects.requireNonNull(updateSendMode, "updateSendMode");
        context = (context==null) ? ARQ.getContext() : context;

        return UpdateExecutionHTTP.create()
                .context(context)
                .endpoint(remoteEndpoint)
                .update(updateRequest)
                .sendMode(updateSendMode)
                .build();
    }

}
