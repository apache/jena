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

package com.hp.hpl.jena.tdb.solver;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

/** Wrap a iterator of BindingNodeId to get a QueryIterator of Bindings. */ 

public class QueryIteratorIdToNode extends QueryIter
{
    Iterator<BindingNodeId> iterator ;
    Iterator<Binding> iteratorBinding ;
    
    // UNUSED - See SolverLib
    private QueryIteratorIdToNode(Iterator<BindingNodeId> iterator, NodeTable nodeTable, ExecutionContext execCxt)
    {
        super(execCxt) ;
        this.iterator = iterator ;
        this.iteratorBinding = SolverLib.convertToNodes(iterator, nodeTable) ;
    }
    
    @Override
    protected void closeIterator()
    {
        Iter.close(iterator) ;
        Iter.close(iteratorBinding) ;
    }
    
    // Asynchronous request to cancel.
    @Override
    protected void requestCancel()
    {}
    
    @Override
    protected boolean hasNextBinding()
    {
        return iteratorBinding.hasNext() ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        return iteratorBinding.next() ;
    }
}
