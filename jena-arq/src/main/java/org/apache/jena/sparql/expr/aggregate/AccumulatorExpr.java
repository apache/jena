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

package org.apache.jena.sparql.expr.aggregate;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;

/** Accumulator that passes down every value of an expression */
abstract class AccumulatorExpr implements Accumulator
{
    private long count = 0 ;
    protected long errorCount = 0 ; 
    private final Expr expr ;
    
    protected AccumulatorExpr(Expr expr)
    {
        this.expr = expr ;
    }
    
    @Override
    final public void accumulate(Binding binding, FunctionEnv functionEnv)
    {
        NodeValue nv = evalOrNull(expr, binding, functionEnv) ;
        if ( nv != null ) {
            accumulate(nv, binding, functionEnv) ;
            count++ ;
        } else {
            accumulateError(binding, functionEnv) ;
            errorCount++ ;
        }
    }
    
    
    private static NodeValue evalOrException(Expr expr, Binding binding, FunctionEnv functionEnv) {
        return expr.eval(binding, functionEnv) ;
    }
    
    // ==> ExprLib
    /** This is better (faster) than the simple implementation which captures {@link ExprEvalException}
     * and returns null.
     */
    
    /*ExprLib*/ private static NodeValue evalOrNull(Expr expr, Binding binding, FunctionEnv functionEnv) {
        return evalOrElse(expr, binding, functionEnv, null) ;
    }
    
    private static NodeValue evalOrElse(Expr expr, Binding binding, FunctionEnv functionEnv, NodeValue exceptionValue) {
        // Exceptions in java are expensive if the stack information is
        // collected which is the default behaviour.  The expensive step is
        // Throwable.fillInStackTrace.
        // 
        // Otherwise, they are reasonable cheap. It needs special exceptions
        // which overrides fillInStackTrace to be cheap but they loose the 
        // general information for development.
        // 
        // Instead, pick out specal cases, the expression being a single variable
        // being the important one.
        // 
        // BOUND(?x) is a important case where the expression is often an exception
        // in general evaluation.

        if ( expr.isConstant() )
            // Easy case.
            return expr.getConstant() ;
        if ( expr.isVariable() ) {
            // The case of the expr being a single variable.
            Var v = expr.asVar() ;
            Node n = binding.get(v) ;
            if ( n == null )
                return exceptionValue ; 
            NodeValue nv = NodeValue.makeNode(n) ;
            return nv ;
        }

        try { 
            return expr.eval(binding, functionEnv) ;
        } catch (ExprEvalException ex) {
            return exceptionValue ;
        }
    }
    
    // COUNT(?v) is different : errors of the expression/variable do not cause an aggregate eval error. 
    // SAMPLE is different : it treats errors as "just another value" and tries to 
    @Override
    public NodeValue getValue()
    {
        if ( errorCount == 0 )
            return getAccValue() ;  
        return null ;
    }

    protected long getErrorCount() { return errorCount ; }
    
    /** Called if no errors to get the accumulated result */
    protected abstract NodeValue getAccValue() ; 

    /** Called when the expression beeing aggregated evaluates OK.
     * Can throw ExprEvalException - in which case the accumulateError is called */
    protected abstract void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv) ;
    /** Called when an evaluation of the expression causes an error
     * or when the accumulation step throws ExprEvalException  
     */
    protected abstract void accumulateError(Binding binding, FunctionEnv functionEnv) ;
}
