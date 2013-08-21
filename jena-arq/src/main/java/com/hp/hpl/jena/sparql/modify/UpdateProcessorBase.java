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

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** Class to hold the general state of a update request execution.
 *  See query ExecutionContext
 */
public class UpdateProcessorBase implements UpdateProcessor
{
    protected final UpdateRequest request ;
    protected final GraphStore graphStore ;
    protected final Binding inputBinding;
    protected final UpdateEngineFactory factory ;
    protected final Context context ;

    public UpdateProcessorBase(UpdateRequest request, 
                               GraphStore graphStore, 
                               Binding inputBinding,
                               Context context, 
                               UpdateEngineFactory factory)
    {
        this.request = request ;
        this.graphStore = graphStore ;
        this.inputBinding = inputBinding;
        this.context = Context.setupContext(context, graphStore) ;
        this.factory = factory ;
    }

    @Override
    public void execute()
    {
        UpdateEngine uProc = factory.create(graphStore, inputBinding, context);
        uProc.startRequest();
        
        try {
            UpdateSink sink = uProc.getUpdateSink();
            Iter.sendToSink(request, sink);     // Will call close on sink if there are no exceptions
        } finally {
            uProc.finishRequest() ;
        }
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
