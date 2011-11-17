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

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** Class to hold the general state of a update request execution.
 *  See query ExecutionContext
 */
public class UpdateProcessorBase implements UpdateProcessor
{
    protected Binding initialBinding = BindingFactory.root() ;
    protected final UpdateRequest request ;
    protected final GraphStore graphStore ;
    protected final UpdateEngineFactory factory ;
    protected final Context context ;

    public UpdateProcessorBase(UpdateRequest request, 
                               GraphStore graphStore, 
                               Context context, 
                               UpdateEngineFactory factory)
    {
        this.request = request ;
        this.graphStore = graphStore ;
        if ( context == null )
            context = ARQ.getContext().copy() ;
        
        this.context = context ;
        this.factory = factory ;
    }

    @Override
    public void execute()
    {
        UpdateEngine proc = factory.create(request, graphStore, initialBinding, context) ;
        proc.execute() ;
    }

    @Override
    public GraphStore getGraphStore()
    {
        return graphStore ;
    }

    @Override
    public void setInitialBinding(QuerySolution binding)
    {
        setInitialBinding(BindingUtils.asBinding(binding)) ;
    }

    public void setInitialBinding(Binding binding)
    {
        initialBinding = binding ;
    }

    public Context getContext()
    {
        return context ;
    }
}
