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

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

public class QueryIterReduced extends QueryIterDistinctReduced
{
    List<Binding> window = new ArrayList<>() ;
    int N = 1 ;
    
    public QueryIterReduced(QueryIterator iter, ExecutionContext context)
    { super(iter, context)  ; }

    @Override
    protected void closeSubIterator()
    {
        window = null ;
        super.closeSubIterator() ;
    }

    @Override
    protected boolean isFreshSighting(Binding b)
    {
        if ( window.contains(b) )
            return false ;
        if ( window.size() >= N )
            window.remove(window.size()-1) ;
        window.add(0, b) ;
        return true ;
    }
}
