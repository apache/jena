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

import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;

public abstract class UpdateEngineBase implements UpdateEngine
{
    protected final DatasetGraph datasetGraph ;
    protected final Binding inputBinding;
    protected final Context context ;

    public UpdateEngineBase(DatasetGraph datasetGraph,
                            Binding inputBinding,
                            Context context)
    {
        this.datasetGraph = datasetGraph ;
        this.inputBinding = inputBinding ;
        this.context = setupContext(context, datasetGraph) ;
    }
    
    private static Context setupContext(Context context, DatasetGraph dataset)
    {
        // To many copies?
        if ( context == null )      // Copy of global context to protect against change.
            context = ARQ.getContext() ;
        context = context.copy() ;

        if ( dataset.getContext() != null )
            context.putAll(dataset.getContext()) ;
        
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        return context ; 
    }
}
