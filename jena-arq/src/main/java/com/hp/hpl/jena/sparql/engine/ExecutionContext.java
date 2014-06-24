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

package com.hp.hpl.jena.sparql.engine;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.util.Context ;

public class ExecutionContext implements FunctionEnv
{
    private static boolean TrackAllIterators = false ;

    private Context context       = null ;
    private DatasetGraph dataset  = null ;
    
    // Iterator tracking
    private Collection<QueryIterator> openIterators    = null ;
    // Tracking all iterators leads to a build up of state,
    private Collection<QueryIterator> allIterators     = null ; 
    private Graph activeGraph           = null ;
    private OpExecutorFactory executor  = null ;

    /** Clone */
    public ExecutionContext(ExecutionContext other) 
    {
        this.context = other.context ;
        this.dataset = other.dataset ;
        this.openIterators = other.openIterators ;
        this.allIterators = other.allIterators ;
        this.activeGraph = other.activeGraph ;
        this.executor = other.executor ;
    }
    
    /** Clone and change active graph - shares tracking */
    public ExecutionContext(ExecutionContext other, Graph activeGraph) 
    {
        this(other) ; 
        this.activeGraph = activeGraph ; 
    }

    public ExecutionContext(Context params, Graph activeGraph, DatasetGraph dataset, OpExecutorFactory factory)
    {
        this.context = params ;
        this.dataset = dataset ;
        this.openIterators = new ArrayList<>() ;
        if ( TrackAllIterators )
            this.allIterators  = new ArrayList<>() ;
        this.activeGraph = activeGraph ;
        this.executor = factory ;
    }

    @Override
    public Context getContext()       { return context ; }

//    public ExecutionContext getExecutionContext()       { return this ; }

    
    public void openIterator(QueryIterator qIter)
    {
        openIterators.add(qIter) ;
        if ( allIterators != null )
            allIterators.add(qIter) ;
    }

    public void closedIterator(QueryIterator qIter)
    {
        openIterators.remove(qIter) ;
    }

    public Iterator<QueryIterator> listOpenIterators()  { return openIterators.iterator() ; }
    public Iterator<QueryIterator> listAllIterators()
    { 
        if ( allIterators == null ) return null ;
        return allIterators.iterator() ;
    }
    
    public OpExecutorFactory getExecutor()
    {
        return executor ;
    }
    
    /** Setter for the policy for algebra expression evaluation - use with care */
    public void setExecutor(OpExecutorFactory executor)
    {
        this.executor = executor ;
    }

    @Override
    public DatasetGraph getDataset()  { return dataset ; }

//    /** Setter for the dataset - use with care */
//    public void setDataset(DatasetGraph dataset)
//    {
//        this.dataset = dataset ;
//    }

    /** Return the active graph (the one matching is against at this point in the query.
     * May be null if unknown or not applicable - for example, doing quad store access or
     * when sorting  
     */ 
    @Override
    public Graph getActiveGraph()     { return activeGraph ; }

//    /** Setter for the active graph - use with care */
//    public void setActiveGraph(Graph activeGraph)
//    {
//        this.activeGraph = activeGraph ;
//    }
}
