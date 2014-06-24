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

package com.hp.hpl.jena.sparql.expr.aggregate;

import java.util.HashSet ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** Accumulator that only passes down unique values of an expression (the first encountered) */
abstract class AccumulatorDistinctExpr implements Accumulator
{
    // Better?? record a large hash. 
    private final Set<NodeValue> values = new HashSet<>() ;
    private long errorCount = 0 ; 
    private long count = 0 ;
    private final Expr expr ;
    
    protected AccumulatorDistinctExpr(Expr expr)
    {
        this.expr = expr ;
    }
    
    @Override
    final public void accumulate(Binding binding, FunctionEnv functionEnv)
    {
        try { 
            NodeValue nv = expr.eval(binding, functionEnv) ;
            if ( values.contains(nv) )
                return ;
            values.add(nv) ;
            accumulateDistinct(nv, binding, functionEnv) ;
            count++ ;
        } catch (ExprEvalException ex)
        {
            errorCount++ ;
            accumulateError(binding, functionEnv) ;
        }
    }
    
    // Count(DISTINCT ?v) is different
    @Override
    public NodeValue getValue()
    {
        if ( errorCount == 0 )
            return getAccValue() ;  
        return null ;
    }
    
    protected long getErrorCount() { return errorCount ; }
     
    protected abstract NodeValue getAccValue() ; 
    protected abstract void accumulateDistinct(NodeValue nv, Binding binding, FunctionEnv functionEnv) ;
    protected abstract void accumulateError(Binding binding, FunctionEnv functionEnv) ;
}
