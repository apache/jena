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

package com.hp.hpl.jena.sparql.engine.iterator ;

import java.util.HashSet ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Memory limited QueryIterDistinct */
public class QueryIterDistinctMem extends QueryIterDistinctReduced
{
    private Set<Binding> seen = new HashSet<>() ;
    
    public QueryIterDistinctMem(QueryIterator iter, ExecutionContext context)
    {
        super(iter, context)  ;
    }

    @Override
    protected void closeSubIterator()
    {
        seen = null ;
        super.closeSubIterator() ;
    }

    @Override
    protected boolean isFreshSighting(Binding binding)
    {
        return seen.contains(binding) ;
    }
}