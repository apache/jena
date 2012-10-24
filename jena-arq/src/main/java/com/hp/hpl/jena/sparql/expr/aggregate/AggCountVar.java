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
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr ;

public class AggCountVar extends AggregatorBase
{
    // ---- COUNT(?var)
    private Expr expr ;

    public AggCountVar(Expr expr) { this.expr = expr ; }
    @Override
    public Aggregator copy(Expr expr) { return new AggCountVar(expr) ; }

    @Override
    public String toString() { return "count("+expr+")" ; }
    @Override
    public String toPrefixString() { return "(count "+WriterExpr.asString(expr)+")" ; }

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccCountVar(expr) ;
    }

    @Override
    public Expr getExpr() { return expr ; }

    @Override
    public int hashCode()   { return HC_AggCountVar ^ expr.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof AggCountVar ) )
            return false ;
        AggCountVar agg = (AggCountVar)other ;
        return agg.getExpr().equals(getExpr()) ;
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
