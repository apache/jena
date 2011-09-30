/**
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
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateEngineMain extends UpdateEngineBase 
{
    public UpdateEngineMain(GraphStore graphStore, UpdateRequest request, Binding initialBinding, Context context)
    {
        super(graphStore, request, initialBinding, context) ;
    }

    @Override
    public void execute()
    {
        graphStore.startRequest() ;
        UpdateEngineWorker worker = new UpdateEngineWorker(graphStore, startBinding) ;
        for ( Update up : request.getOperations() )
            up.visit(worker) ;
        graphStore.finishRequest() ;
    }
    
    private static UpdateEngineFactory factory = new UpdateEngineFactory()
    {
        public boolean accept(UpdateRequest request, GraphStore graphStore, Context context)
        {
            return true ;
        }

        public UpdateEngine create(UpdateRequest request, GraphStore graphStore, Binding inputBinding, Context context)
        {
            return new UpdateEngineMain(graphStore, request, inputBinding, context) ;
        }
    } ;

    public static UpdateEngineFactory getFactory() { return factory ; }
}
