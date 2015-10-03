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

public class AggSum  extends AggregatorBase
{
    // ---- SUM(expr)
    private static final NodeValue noValuesToSum = NodeValue.nvZERO ; 

    public AggSum(Expr expr) { super("SUM", false, expr) ; } 
    @Override
    public Aggregator copy(ExprList exprs) { return new AggSum(exprs.get(0)) ; }

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccSum(getExpr()) ;
    }

    @Override
    public Node getValueEmpty() { return NodeValue.toNode(noValuesToSum) ; } 

    @Override
    public int hashCode()   { return HC_AggSum ^ getExpr().hashCode() ; }
    
    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ; 
        if ( ! ( other instanceof AggSum ) )
            return false ;
        AggSum agg = (AggSum)other ;
        return getExpr().equals(agg.getExpr(), bySyntax) ;
    } 

    // ---- Accumulator
    private static class AccSum extends AccumulatorExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = null ;

        public AccSum(Expr expr) { super(expr) ; }

        @Override
        protected void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        {
            if ( nv.isNumber() )
            {
                if ( total == null )
                    total = nv ;
                else
                    total = XSDFuncOp.numAdd(nv, total) ;
            }
            else
                throw new ExprEvalException("Not a number: "+nv) ;
        }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}

        @Override
        public NodeValue getAccValue()
        { return total ; }
    }
}
