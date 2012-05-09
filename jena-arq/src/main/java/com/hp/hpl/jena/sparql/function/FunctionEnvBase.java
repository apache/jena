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

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Environment passed to functions -- see also {@link com.hp.hpl.jena.sparql.engine.ExecutionContext} */

public class FunctionEnvBase implements FunctionEnv
{
    private Context context ;
    private Graph activeGraph ;
    private DatasetGraph dataset ;
    private ExecutionContext execContext = null ;

    /** Create an execution environment suitable for testing functions and expressions */ 
    public static FunctionEnv createTest()
    {
        return new FunctionEnvBase(ARQ.getContext()) ;
    }
    
    public FunctionEnvBase() { this(ARQ.getContext(), null, null) ; }
    
    public FunctionEnvBase(Context context) { this ( context, null, null) ; }
    
    public FunctionEnvBase(ExecutionContext execCxt)
    { 
        this(execCxt.getContext(), execCxt.getActiveGraph(), execCxt.getDataset()) ;
        execContext = execCxt ;
    }

    public FunctionEnvBase(Context context, Graph activeGraph, DatasetGraph dataset)
    {
        this.context = context ;
        this.activeGraph = activeGraph ;
        this.dataset = dataset ;
    }

    @Override
    public Graph getActiveGraph()
    {
        return activeGraph ;
    }

    @Override
    public Context getContext()
    {
        return context ;
    }

//    public ExecutionContext getExecutionContext()
//    {
//        if ( execContext == null )
//            execContext = new ExecutionContext(context, activeGraph, dataset, QC.getFactory(context)) ;
//        return execContext ;
//    }

    @Override
    public DatasetGraph getDataset()
    {
        return dataset ;
    }
}
