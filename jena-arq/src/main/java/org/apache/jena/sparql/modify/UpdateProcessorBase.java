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

package org.apache.jena.sparql.modify;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.update.UpdateProcessor ;
import org.apache.jena.update.UpdateRequest ;

/** Class to hold the general state of a update request execution.
 *  See query ExecutionContext
 */
public class UpdateProcessorBase implements UpdateProcessor
{
    protected final UpdateRequest request ;
    protected final DatasetGraph datasetGraph ;
    protected final Binding inputBinding;
    protected final UpdateEngineFactory factory ;
    protected final Context context ;

    public UpdateProcessorBase(UpdateRequest request, 
                               DatasetGraph datasetGraph, 
                               Binding inputBinding,
                               Context context, 
                               UpdateEngineFactory factory)
    {
        this.request = request ;
        this.datasetGraph = datasetGraph ;
        this.inputBinding = inputBinding;
        this.context = context;
        Context.setCurrentDateTime(this.context) ;
        this.factory = factory ;
    }

    @Override
    public void execute() {
        UpdateEngine uProc = factory.create(datasetGraph, inputBinding, context);
        uProc.startRequest();
        
        try {
            UpdateSink sink = uProc.getUpdateSink();
            Iter.sendToSink(request.iterator(), sink);     // Will call close on sink if there are no exceptions
        } finally {
            uProc.finishRequest() ;
        }
    }

    @Override
    public DatasetGraph getDatasetGraph() {
        return datasetGraph ;
    }

    @Override
    public Context getContext() {
        return context ;
    }
}
