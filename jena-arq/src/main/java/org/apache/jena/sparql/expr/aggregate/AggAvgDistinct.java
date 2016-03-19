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
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.function.FunctionEnv ;

public class AggAvgDistinct extends AggregatorBase
{
    // ---- AVG(DISTINCT expr)
    public AggAvgDistinct(Expr expr) { super("AVG", true, expr) ; } 
    @Override
    public Aggregator copy(ExprList expr) { return new AggAvgDistinct(expr.get(0)) ; }

    private static final NodeValue noValuesToAvg = NodeValue.nvZERO ; 

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccAvgDistinct(getExpr()) ;
    }

    @Override
    public Node getValueEmpty()     { return NodeValue.toNode(noValuesToAvg) ; } 

    @Override
    public int hashCode()   {
        return HC_AggAvgDistinct ^ getExprList().hashCode() ;
    }

    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggAvgDistinct ) ) return false ;
        AggAvgDistinct a = (AggAvgDistinct)other ;
        return exprList.equals(a.exprList, bySyntax) ;
    }

    
    // ---- Accumulator
    class AccAvgDistinct extends AccumulatorExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = noValuesToAvg ;
        private int count = 0 ;
        
        static final boolean DEBUG = false ;
        
        public AccAvgDistinct(Expr expr) { super(expr, true) ; }

        @Override
        protected void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { 
            if ( nv.isNumber() )
            {
                count++ ;
                if ( total == noValuesToAvg )
                    total = nv ;
                else
                    total = XSDFuncOp.numAdd(nv, total) ;
            }
            else
                throw new ExprEvalException("avg: not a number: "+nv) ;

            if ( DEBUG ) System.out.println("avg: ("+total+","+count+")") ;
        }

        @Override
        public NodeValue getAccValue()
        {
            if ( count == 0 ) return noValuesToAvg ;
            NodeValue nvCount = NodeValue.makeInteger(count) ;
            return XSDFuncOp.numDivide(total, nvCount) ;
        }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}
    }
}
