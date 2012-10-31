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
import com.hp.hpl.jena.sparql.modify.request.UpdateVisitor;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

/**
 * Default implementation of an update engine
 * <p>
 * Developers who only want to change/extend the processing of individual updates can easily 
 * </p>
 */
public class UpdateEngineMain extends UpdateEngineBase 
{
    /**
     * Creates a new Update Engine
     * @param graphStore Graph Store the updates operate over
     * @param request Update Request
     * @param initialBinding Initial Bindings
     * @param context Execution Context
     */
    public UpdateEngineMain(GraphStore graphStore, UpdateRequest request, Binding initialBinding, Context context)
    {
        super(graphStore, request, initialBinding, context) ;
    }

    /**
     * Executes the updates by creating a {@link UpdateVisitor} using the {@link #prepareWorker()} method and then using that to visit each update command in the Update Request
     */
    @Override
    public void execute()
    {
        graphStore.startRequest(request) ;
        UpdateVisitor worker = this.prepareWorker();
        for ( Update up : request ) {
            up.visit(worker) ;
        }
        graphStore.finishRequest(request) ;
    }
    
    /**
     * Creates the {@link UpdateVisitor} which will do the work of applying the updates
     * @return The update visitor to be used to apply the updates
     */
    protected UpdateVisitor prepareWorker() {
        return new UpdateEngineWorker(graphStore, startBinding, context) ;
    }
    
    private static UpdateEngineFactory factory = new UpdateEngineFactory()
    {
        @Override
        public boolean accept(UpdateRequest request, GraphStore graphStore, Context context)
        {
            return true ;
        }

        @Override
        public UpdateEngine create(UpdateRequest request, GraphStore graphStore, Binding inputBinding, Context context)
        {
            return new UpdateEngineMain(graphStore, request, inputBinding, context) ;
        }
    } ;

    public static UpdateEngineFactory getFactory() { return factory ; }
}
