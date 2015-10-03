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
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.graph.NodeConst ;

public class AggCountVar extends AggregatorBase
{
    // ---- COUNT(?var)
    public AggCountVar(Expr expr) { super("COUNT", false, expr) ; }
    @Override
    public Aggregator copy(ExprList expr) { return new AggCountVar(expr.get(0)) ; }

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccCountVar(getExpr()) ;
    }

    @Override
    public int hashCode()   { return HC_AggCountVar ^ exprList.hashCode() ; }
    
    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggCountVar ) )
            return false ;
        AggCountVar agg = (AggCountVar)other ;
        return agg.getExpr().equals(getExpr(), bySyntax) ;
    }

    @Override
    public Node getValueEmpty()     { return NodeConst.nodeZero ; } 

    // ---- Accumulator
    private static class AccCountVar extends AccumulatorExpr
    {
        private long count = 0 ;
        public AccCountVar(Expr expr)   { super(expr) ; }

        @Override
        public void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { count++ ; }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}
        
        // Ignore errors.
        @Override
        public NodeValue getValue()
        { return getAccValue() ; }

        @Override
        public NodeValue getAccValue()             { return NodeValue.makeInteger(count) ; }
    }
}
