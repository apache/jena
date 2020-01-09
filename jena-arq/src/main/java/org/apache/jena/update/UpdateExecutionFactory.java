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

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingUtils ;
import org.apache.jena.sparql.modify.* ;
import org.apache.jena.sparql.util.Context ;

/** Create UpdateProcessors (one-time executions of a SPARQL Update request) */
public class UpdateExecutionFactory
{
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param dataset
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, Dataset dataset)
    {
        return create(new UpdateRequest(update), dataset) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param datasetGraph
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, DatasetGraph datasetGraph)
    {
        return create(new UpdateRequest(update), datasetGraph) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, Dataset dataset, QuerySolution inputBinding)
    {
        return create(update, dataset.asDatasetGraph(), BindingUtils.asBinding(inputBinding)) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, DatasetGraph datasetGraph, Binding inputBinding)
    {
        return create(new UpdateRequest(update), datasetGraph, inputBinding) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param dataset
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset)
    {        
        return make(updateRequest, dataset.asDatasetGraph(), null, null) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param datasetGraph
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, DatasetGraph datasetGraph)
    {        
        return make(updateRequest, datasetGraph, null, null) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset, QuerySolution inputBinding)
    {        
        return create(updateRequest, dataset.asDatasetGraph(), BindingUtils.asBinding(inputBinding)) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, DatasetGraph datasetGraph, Binding inputBinding)
    {        
        return make(updateRequest, datasetGraph, inputBinding, null) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param dataset
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset)
    {        
        return makeStreaming(dataset.asDatasetGraph(), null, null) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param datasetGraph
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph)
    {        
        return makeStreaming(datasetGraph, null, null) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset, QuerySolution inputBinding)
    {        
        return createStreaming(dataset.asDatasetGraph(), BindingUtils.asBinding(inputBinding)) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph, Binding inputBinding)
    {        
        return makeStreaming(datasetGraph, inputBinding, null) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param dataset
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset, Context context)
    {        
        return makeStreaming(dataset.asDatasetGraph(), null, context) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param datasetGraph
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph, Context context)
    {        
        return makeStreaming(datasetGraph, null, context) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset, QuerySolution inputBinding, Context context)
    {        
        return createStreaming(dataset.asDatasetGraph(), BindingUtils.asBinding(inputBinding), context) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessorStreaming createStreaming(DatasetGraph datasetGraph, Binding inputBinding, Context context)
    {        
        return makeStreaming(datasetGraph, inputBinding, context) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param dataset
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset, Context context)
    {        
        return make(updateRequest, dataset.asDatasetGraph(), null, context) ;
    }
    
    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param datasetGraph
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, DatasetGraph datasetGraph, Context context)
    {        
        return make(updateRequest, datasetGraph, null, context) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param dataset
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset, QuerySolution inputBinding, Context context)
    {        
        return create(updateRequest, dataset.asDatasetGraph(), BindingUtils.asBinding(inputBinding), context) ;
    }

    /** Create an UpdateProcessor appropriate to the datasetGraph, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param datasetGraph
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, DatasetGraph datasetGraph, Binding inputBinding, Context context)
    {        
        return make(updateRequest, datasetGraph, inputBinding, context) ;
    }

    // Everything comes through one of these two make methods
    private static UpdateProcessor make(UpdateRequest updateRequest, DatasetGraph datasetGraph, Binding inputBinding, Context context)
    {
        Context cxt = Context.setupContextForDataset(context, datasetGraph) ;
        
        UpdateEngineFactory f = UpdateEngineRegistry.get().find(datasetGraph, cxt) ;
        if ( f == null )
            return null ;
        
        UpdateProcessorBase uProc = new UpdateProcessorBase(updateRequest, datasetGraph, inputBinding, cxt, f) ;
        return uProc ;
    }
    
    // Everything comes through one of these two make methods
    private static UpdateProcessorStreaming makeStreaming(DatasetGraph datasetGraph, Binding inputBinding, Context context)
    {
        Context cxt = Context.setupContextForDataset(context, datasetGraph) ;
        
        UpdateEngineFactory f = UpdateEngineRegistry.get().find(datasetGraph, cxt) ;
        if ( f == null )
            return null ;
        
        UpdateProcessorStreamingBase uProc = new UpdateProcessorStreamingBase(datasetGraph, inputBinding, cxt, f) ;
        return uProc;
    }
    
    /** Create an UpdateProcessor that sends the update to a remote SPARQL Update service.
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint)
    {
        return createRemote(new UpdateRequest(update), remoteEndpoint, null, null, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update to a remote SPARQL Update service.
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint, HttpClient client)
    {
        return createRemote(update, remoteEndpoint, client, null);
    }
    
    /** Create an UpdateProcessor that sends the update to a remote SPARQL Update service.
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint, HttpClient client, HttpContext httpContext)
    {
        return createRemote(new UpdateRequest(update), remoteEndpoint, null, client, httpContext) ;
    }
    
    /** Create an UpdateProcessor that sends the update to a remote SPARQL Update service.
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint, Context context)
    {
        return createRemote(new UpdateRequest(update), remoteEndpoint, context, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update to a remote SPARQL Update service.
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint, Context context, HttpClient client)
    {
        return createRemote(update, remoteEndpoint, context, client, null);
    }
        
    /** Create an UpdateProcessor that sends the update to a remote SPARQL Update service.
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @param client HTTP client
     * @param httpContext HTTP Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint, Context context, HttpClient client, HttpContext httpContext)
    {
        return createRemote(new UpdateRequest(update), remoteEndpoint, context, client) ;
    }
        
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service.
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint)
    {
        return createRemote(updateRequest, remoteEndpoint, null, null, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service.
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint, HttpClient client)
    {
        return createRemote(updateRequest, remoteEndpoint, null, client) ;
    }

    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service.
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param client HTTP client
     * @param httpContext HTTP Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint, HttpClient client, HttpContext httpContext)
    {
        return createRemote(updateRequest, remoteEndpoint, null, client, httpContext) ;
    }

    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service.
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint, Context context)
    {
        return createRemote(updateRequest, remoteEndpoint, context, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service.
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint, Context context, HttpClient client)
    {
        return new UpdateProcessRemote(updateRequest, remoteEndpoint, context, client, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service.
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @param client HTTP client
     * @param httpContext   HTTP Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint, Context context, HttpClient client, HttpContext httpContext)
    {
        return new UpdateProcessRemote(updateRequest, remoteEndpoint, context, client, httpContext) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint)
    {
        return createRemoteForm(update, remoteEndpoint, null, null, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint, HttpClient client)
    {
        return createRemoteForm(update, remoteEndpoint, null, client) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param client HTTP client
     * @param httpContext   HTTP Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint, HttpClient client, HttpContext httpContext)
    {
        return createRemoteForm(update, remoteEndpoint, null, client, httpContext) ;
    }

    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint, Context context)
    {
        return createRemoteForm(new UpdateRequest(update), remoteEndpoint, context, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint, Context context, HttpClient client)
    {
        return createRemoteForm(new UpdateRequest(update), remoteEndpoint, null, client) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @param client HTTP client
     * @param httpContext   HTTP Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint, Context context, HttpClient client, HttpContext httpContext) {
        return createRemoteForm(new UpdateRequest(update), remoteEndpoint, null, client, httpContext) ;
    }
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint)
    {
        return createRemoteForm(updateRequest, remoteEndpoint, null, null, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint, HttpClient client)
    {
        return createRemoteForm(updateRequest, remoteEndpoint, null, client) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param client HTTP client
     * @param httpContext   HTTP Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint, HttpClient client, HttpContext httpContext)
    {
        return createRemoteForm(updateRequest, remoteEndpoint, null, client, httpContext) ;
    }

    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint, Context context)
    {
        return new UpdateProcessRemoteForm(updateRequest, remoteEndpoint, context) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @param client HTTP client
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint, Context context, HttpClient client)
    {
        return createRemoteForm(updateRequest, remoteEndpoint, context, client, null) ;
    }
    
    /** Create an UpdateProcessor that sends the update request to a remote SPARQL Update service using an HTML form
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @param client HTTP client
     * @param httpContext   HTTP Context
     * @return Remote Update processor
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint, Context context, HttpClient client, HttpContext httpContext) 
    {
        return new UpdateProcessRemoteForm(updateRequest, remoteEndpoint, context, client, httpContext) ;
    }
}
