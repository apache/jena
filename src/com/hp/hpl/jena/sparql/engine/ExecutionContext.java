/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.Context;

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
        this.openIterators = new ArrayList<QueryIterator>() ;
        if ( TrackAllIterators )
            this.allIterators  = new ArrayList<QueryIterator>() ;
        this.activeGraph = activeGraph ;
        this.executor = factory ;
    }

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
    public Graph getActiveGraph()     { return activeGraph ; }

//    /** Setter for the active graph - use with care */
//    public void setActiveGraph(Graph activeGraph)
//    {
//        this.activeGraph = activeGraph ;
//    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */