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

package com.hp.hpl.jena.sparql.modify;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.modify.request.UpdateVisitor ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

/**
 * Example implementation of an update engine that does not stream data, instead it will build it up into an
 * in-memory UpdateRequest, and then traverse it after all update operations have finished.
 */
public class UpdateEngineNonStreaming extends UpdateEngineMain
{
    // This is the internal accumulator of update operations.
    // It is used to accumulate Updates so as not to alter
    // the UpdateRequest at the application level.
    protected final UpdateRequest accRequests ;
    protected final UpdateSink    updateSink ;
    
    /**
     * Creates a new Update Engine
     * @param graphStore Graph Store the updates operate over
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context Execution Context
     */
    public UpdateEngineNonStreaming(GraphStore graphStore, Binding inputBinding, Context context)
    {
        super(graphStore, inputBinding, context) ;
        accRequests = new UpdateRequest();
        updateSink = new UpdateRequestSink(accRequests)
        {
            @Override
            public void close()
            {
                // Override the close() method to call execute() when we're done accepting update operations
                super.close();
                execute();
            }
        } ;
    }

    @Override
    public void startRequest()
    {
        graphStore.startRequest() ;
    }
    
    @Override
    public void finishRequest()
    {
        graphStore.finishRequest();
    }
    
    /**
     * Returns an {@link UpdateSink} that adds all update operations into an internal {@link UpdateRequest} object.
     * After the last update operation has been added, the {@link #execute()} method is called.
     */
    @Override
    public UpdateSink getUpdateSink() { return updateSink ; }
    
    /**
     * Called after all of the update operations have been added to {@link #accRequests}.
     */
    protected void execute()
    {
        UpdateVisitor worker = this.prepareWorker() ;
        for ( Update up : accRequests )
        {
            up.visit(worker) ;
        }
    }
    
    private static UpdateEngineFactory factory = new UpdateEngineFactory()
    {
        @Override
        public boolean accept(GraphStore graphStore, Context context)
        {
            return true ;
        }
        
        @Override
        public UpdateEngine create(GraphStore graphStore, Binding inputBinding, Context context)
        {
            return new UpdateEngineNonStreaming(graphStore, inputBinding, context);
        }
    } ;

    public static UpdateEngineFactory getFactory() { return factory ; }
}
