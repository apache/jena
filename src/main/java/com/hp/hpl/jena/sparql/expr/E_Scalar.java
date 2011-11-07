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

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.syntax.Element ;

/** A scalar subquery that returns a single row/column. */
public class E_Scalar extends ExprFunctionOp
{
    // Work-in-progress
    private static final String symbol = "scalar" ;
    
    protected E_Scalar(Element el, Op op)
    {
        super(symbol, el, op) ;
    }

    @Override
    public ExprFunctionOp copy(ExprList args, Op x)
    {
        return null ;
    }

    @Override
    protected NodeValue eval(Binding binding, QueryIterator iter, FunctionEnv env)
    {
        return null ;
    }

    @Override
    public Expr applyNodeTransform(NodeTransform transform)
    {
        return null ;
    }

    @Override
    public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        return null ;
    }
}
