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

package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton ;

public class IterLib
{
    public static QueryIterator noResults(ExecutionContext execCxt)
    {
        return QueryIterNullIterator.create(execCxt) ;
    }
    
    public static QueryIterator oneResult(Binding binding, Var var, Node value, ExecutionContext execCxt)
    {
        return QueryIterSingleton.create(binding, var, value, execCxt) ;
    }
    
    public static QueryIterator result(Binding binding, ExecutionContext execCxt)
    {
        return QueryIterSingleton.create(binding, execCxt) ;
    }
}
