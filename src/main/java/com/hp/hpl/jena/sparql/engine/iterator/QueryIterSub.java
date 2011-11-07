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

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;

/**
 * This class supports a QueryIter that uses a single sub iterator.
 * Unlike QueryIter1, it's assumes the subiterator will be reset and manipulated.
 */
public abstract class QueryIterSub extends QueryIter
{
    protected QueryIterator iter ; 
    
    public QueryIterSub(QueryIterator input, ExecutionContext execCxt)
    { 
        super(execCxt) ;
        this.iter = input ;
    }
    
    @Override
    protected final
    void closeIterator()
    {
        closeSubIterator() ;
        performClose(iter) ;
        iter = null ;
    }
    
    @Override
    protected final
    void requestCancel()
    {
        requestSubCancel() ;
        performRequestCancel(iter) ;
    }
    
    /** Cancellation of the query execution is happening */
    protected abstract void requestSubCancel() ;
    
    /** Pass on the close method - no need to close the QueryIterator passed to the QueryIter1 constructor */
    protected abstract void closeSubIterator() ;
}
