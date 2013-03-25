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

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;

/** A singleton iterator */

public class QueryIterSingleton extends QueryIterYieldN
{
    // A common usage?
    public static QueryIterSingleton create(Binding parent, Var var, Node value, ExecutionContext execCxt)
    {
        Binding b = BindingFactory.binding(parent, var, value) ;
        return QueryIterSingleton.create(b, execCxt) ;
    }
    
    public static QueryIterSingleton create(Binding binding, ExecutionContext execCxt)
    {
        return new QueryIterSingleton(binding, execCxt) ;
    }

    private QueryIterSingleton(Binding binding) // Not needed
    {
        this(binding, null) ;
    }
    
    protected QueryIterSingleton(Binding binding, ExecutionContext context)
    {
        super(1, binding, context) ;
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.print("QueryIterSingleton "+binding);
    }
    
//    @Override
//    public void closeIterator() { super.closeIterator() ; }
}
