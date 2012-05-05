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

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

public class AggAvg extends AggregatorBase
{
    // ---- AVG(?var)
    private Expr expr ;

    public AggAvg(Expr expr) { this.expr = expr ; } 
    @Override
    public Aggregator copy(Expr expr) { return new AggAvg(expr) ; }

    // XQuery/XPath Functions&Operators suggests zero
    // SQL suggests null.
    private static final NodeValue noValuesToAvg = NodeValue.nvZERO ; // null 

    @Override
    public String toString() { return "avg("+ExprUtils.fmtSPARQL(expr)+")" ; }
    @Override
    public String toPrefixString() { return "(avg "+WriterExpr.asString(expr)+")" ; }

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccAvg(expr) ;
    }

    @Override
    public final Expr getExpr() { return expr ; }

    @Override
    public Node getValueEmpty()     { return NodeValue.toNode(noValuesToAvg) ; } 
    
    @Override
    public int hashCode()   { return HC_AggAvg ^ expr.hashCode() ; }

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggAvg ) ) return false ;
        AggAvg a = (AggAvg)other ;
        return expr.equals(a.expr) ;
    }

    
    // ---- Accumulator
    private static class AccAvg extends AccumulatorExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = noValuesToAvg ;
        private int count = 0 ;
        
        static final boolean DEBUG = false ;
        
        public AccAvg(Expr expr) { super(expr) ; }

        @Override
        protected void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { 
            if ( DEBUG ) System.out.println("avg: "+nv) ;

            if ( nv.isNumber() )
            {
                count++ ;
                if ( total == noValuesToAvg )
                    total = nv ;
                else
                    total = XSDFuncOp.numAdd(nv, total) ;
            }
            else
            {
                //ARQ.getExecLogger().warn("Evaluation error: avg() on "+nv) ;
                throw new ExprEvalException("avg: not a number: "+nv) ;
            }
            
            if ( DEBUG ) System.out.println("avg: ("+total+","+count+")") ;
        }
        
        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}

        @Override
        public NodeValue getAccValue()
        {
            if ( count == 0 ) return noValuesToAvg ;
            if ( super.errorCount != 0 )
                //throw new ExprEvalException("avg: error in group") ; 
                return null ;
            NodeValue nvCount = NodeValue.makeInteger(count) ;
            return XSDFuncOp.numDivide(total, nvCount) ;
        }
    }
}
