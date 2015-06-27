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

public class AggSampleDistinct extends AggregatorBase
{
    // ---- Sample(DISTINCT expr)
    public AggSampleDistinct(Expr expr) { super("SAMPLE", true, expr) ; } 
    @Override
    public Aggregator copy(ExprList exprs) { return new AggSampleDistinct(exprs.get(0)) ; }

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccSampleDistict(getExpr()) ;
    }

    @Override
    public int hashCode()   { return HC_AggSample ^ getExpr().hashCode() ; }
    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggSampleDistinct ) )
            return false ;
        AggSampleDistinct agg = (AggSampleDistinct)other ;
        return this.exprList.equals(agg.exprList, bySyntax) ;
    } 

    @Override
    public Node getValueEmpty()     { return null ; } 

    // ---- Accumulator
    private static class AccSampleDistict extends AccumulatorExpr
    {
        // NOT AccumulatorDistinctExpr - avoid "distinct" overheads. 
        // Sample: first evaluation of the expression that is not an error.
        // For sample, DISTINCT is a no-op - this code is picks the last element. 
        private NodeValue sampleSoFar = null ;

        public AccSampleDistict(Expr expr) { super(expr)  ; }

        @Override
        public void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        {
            // Last value seen.
            sampleSoFar = nv ;
        }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}
        
        @Override
        public NodeValue getAccValue()
        { return sampleSoFar ; }
    }
}
