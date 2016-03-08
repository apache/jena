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

import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprLib ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;

/** Accumulator that passes down every value of an expression */
public abstract class AccumulatorExpr implements Accumulator
{
    private long accCount = 0 ;
    protected long errorCount = 0 ; 
    private final Expr expr ;
    
    protected AccumulatorExpr(Expr expr) {
        this.expr = expr;
    }
    
    @Override
    final public void accumulate(Binding binding, FunctionEnv functionEnv) {
        NodeValue nv = ExprLib.evalOrNull(expr, binding, functionEnv);
        if ( nv != null ) {
            try {
                accumulate(nv, binding, functionEnv);
                accCount++;
                return;
            }
            catch (ExprEvalException ex) {}
            // Drop to error case.
        }
        accumulateError(binding, functionEnv);
        errorCount++;
    }
    
    // COUNT(?v) is different : errors of the expression/variable do not cause an aggregate eval error. 
    // SAMPLE is different : it treats errors as "just another value" and tries to return a defined value if any have been seen. 

    @Override
    public NodeValue getValue() {
        if ( errorCount == 0 )
            return getAccValue();
        return null;
    }

    /** Get the count of accumulated values */ 
    protected long getAccCount() { return accCount ; }
    
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
