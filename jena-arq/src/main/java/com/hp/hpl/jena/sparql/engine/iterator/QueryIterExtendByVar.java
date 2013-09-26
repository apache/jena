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

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;

/**
 * Yield new bindings, with a fixed parent, with values from an iterator. 
 */
public class QueryIterExtendByVar extends QueryIter
{
    // Use QueryIterProcessBinding?
    private Binding binding ;
    private Var var ;
    private Iterator<Node> members ;
    
    public QueryIterExtendByVar(Binding binding, Var var, Iterator<Node> members, ExecutionContext execCxt)
    {
        super(execCxt) ;
        if ( true ) { // Assume not too costly.
            if ( binding.contains(var) )
                throw new ARQInternalErrorException("Var "+var+" already set in "+binding) ;
        }
        this.binding = binding ;
        this.var = var ;
        this.members = members ;
    }

    @Override
    protected boolean hasNextBinding()
    {
        return members.hasNext() ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        Node n = members.next() ;
        Binding b = BindingFactory.binding(binding, var, n) ;
        return b ;
    }

    @Override
    protected void closeIterator()
    { }
    
    @Override
    protected void requestCancel()
    { }
}
