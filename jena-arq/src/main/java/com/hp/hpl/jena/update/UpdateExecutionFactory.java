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

package com.hp.hpl.jena.update;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.modify.* ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Create UpdateProcessors (one-time executions of a SPARQL Update request) */
public class UpdateExecutionFactory
{

    /** Create an UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore)
    {
        return create(update, graphStore, (Binding)null) ;
    }

    /** Create an UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @param initialSolution
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore, QuerySolution initialSolution)
    {        
        return create(new UpdateRequest(update), graphStore, initialSolution) ;
    }

    /** Create an UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @param initialBinding
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore, Binding initialBinding)
    {        
        return create(new UpdateRequest(update), graphStore, initialBinding) ;
    }

    /** Create an UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore)
    {
        return create(updateRequest, graphStore, (Binding)null) ;
    }

    /** Create an UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialSolution
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, QuerySolution initialSolution)
    {
        return create(updateRequest, graphStore, BindingUtils.asBinding(initialSolution)) ;
    }
    
    /** Create an UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialBinding
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, Binding initialBinding)
    {        
        return make(updateRequest, graphStore, initialBinding, null) ;
    }

    /** Create an UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialBinding (may be null for none)
     * @param context  (null means use merge of global and graph store context))
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, Binding initialBinding, Context context)
    {        
        return make(updateRequest, graphStore, initialBinding, context) ;
    }

    // Everything comes through here
    private static UpdateProcessor make(UpdateRequest updateRequest, GraphStore graphStore, Binding initialBinding, Context context)
    {
        if ( context == null )
        {
            context = ARQ.getContext().copy();
            context.putAll(graphStore.getContext()) ;
        }
        
        UpdateEngineFactory f = UpdateEngineRegistry.get().find(updateRequest, graphStore, context) ;
        if ( f == null )
            return null ;
        
        UpdateProcessorBase uProc = new UpdateProcessorBase(updateRequest, graphStore, context, f) ;
        if ( initialBinding != null )
            uProc.setInitialBinding(initialBinding) ;
        return uProc ;
    }
    
    /** Create an UpdateProcessor that send the update to a remote SPARQL Update service.
     * @param update
     * @param remoteEndpoint
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint)
    {
        return createRemote(update, remoteEndpoint, null) ;
    }
    
    /** Create an UpdateProcessor that send the update to a remote SPARQL Update service.
     * @param update
     * @param remoteEndpoint
     * @param context
     */
    public static UpdateProcessor createRemote(Update update, String remoteEndpoint, Context context)
    {
        return createRemote(new UpdateRequest(update), remoteEndpoint, context) ;
    }
    
    /** Create an UpdateProcessor that send the update request to a remote SPARQL Update service.
     * @param updateRequest
     * @param remoteEndpoint
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint)
    {
        return new UpdateProcessRemote(updateRequest, remoteEndpoint, null) ;
    }

    /** Create an UpdateProcessor that send the update request to a remote SPARQL Update service.
     * @param updateRequest
     * @param remoteEndpoint
     * @param context
     */
    public static UpdateProcessor createRemote(UpdateRequest updateRequest, String remoteEndpoint, Context context)
    {
        return new UpdateProcessRemote(updateRequest, remoteEndpoint, context) ;
    }
    
    /** Create an UpdateProcessor that send the update request to a remote SPARQL Update service using an HTML form
     * @param update
     * @param remoteEndpoint
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint)
    {
        return createRemoteForm(update, remoteEndpoint, null) ;
    }
    
    /** Create an UpdateProcessor that send the update request to a remote SPARQL Update service using an HTML form
     * @param update
     * @param remoteEndpoint
     * @param context
     */
    public static UpdateProcessor createRemoteForm(Update update, String remoteEndpoint, Context context)
    {
        return new UpdateProcessRemoteForm(new UpdateRequest(update), remoteEndpoint, null) ;
    }
    
    /** Create an UpdateProcessor that send the update request to a remote SPARQL Update service using an HTML form
     * @param updateRequest
     * @param remoteEndpoint
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint)
    {
        return createRemoteForm(updateRequest, remoteEndpoint, null) ;
    }
    
    /** Create an UpdateProcessor that send the update request to a remote SPARQL Update service using an HTML form
     * @param updateRequest
     * @param remoteEndpoint
     * @param context
     */
    public static UpdateProcessor createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint, Context context)
    {
        return new UpdateProcessRemoteForm(updateRequest, remoteEndpoint, context) ;
    }
}
