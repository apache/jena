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

import com.hp.hpl.jena.sparql.modify.request.UpdateVisitor ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;

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
     * @param context Execution Context
     */
    public UpdateEngineMain(GraphStore graphStore, Context context)
    {
        super(graphStore, context) ;
    }

    @Override
    public void startRequest()
    {
        graphStore.startRequest();
    }
    
    @Override
    public void finishRequest()
    {
        graphStore.finishRequest();
    }
    
    /**
     * Creates an {@link UpdateVisitorSink} by using the {@link UpdateVisitor} provided by the {@link #prepareWorker()} method, which will be used to visit each update operation.
     */
    @Override
    public UpdateSink getUpdateSink()
    {
        return new UpdateVisitorSink(this.prepareWorker());
    }
    
    /**
     * Creates the {@link UpdateVisitor} which will do the work of applying the updates
     * @return The update visitor to be used to apply the updates
     */
    protected UpdateVisitor prepareWorker() {
        return new UpdateEngineWorker(graphStore, context) ;
    }
    
    private static UpdateEngineFactory factory = new UpdateEngineFactory()
    {
        @Override
        public boolean accept(GraphStore graphStore, Context context)
        {
            return true ;
        }
        
        @Override
        public UpdateEngine create(GraphStore graphStore, Context context)
        {
            return new UpdateEngineMain(graphStore, context);
        }
    } ;

    public static UpdateEngineFactory getFactory() { return factory ; }
}
