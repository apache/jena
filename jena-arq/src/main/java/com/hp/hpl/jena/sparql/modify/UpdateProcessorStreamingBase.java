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
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateProcessorStreaming ;

/** Class to hold the general state of a update request execution.
 *  See query ExecutionContext
 */
public class UpdateProcessorStreamingBase implements UpdateProcessorStreaming
{
    protected final GraphStore graphStore ;
    protected final Context context ;
    
    protected final UpdateEngine proc;

    public UpdateProcessorStreamingBase(GraphStore graphStore, Binding inputBinding, Context context, UpdateEngineFactory factory)
    {
        this.graphStore = graphStore ;
        this.context = Context.setupContext(context, graphStore) ;
        
        proc = factory.create(graphStore, inputBinding, context) ;
    }
    
    @Override
    public void startRequest()
    {
        proc.startRequest();
    }
    
    @Override
    public void finishRequest()
    {
        proc.finishRequest();
    }
    
    @Override
    public UpdateSink getUpdateSink()
    {
        return proc.getUpdateSink();
    }

    @Override
    public GraphStore getGraphStore()
    {
        return graphStore ;
    }
    
    @Override
    public Context getContext()
    {
        return context ;
    }
}
