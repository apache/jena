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

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class AggMedianDistinct extends AggregatorBase
{
    // ---- Median(DISTINCT expr)
    private static Logger log = LoggerFactory.getLogger("MedianDistinct") ;

    public AggMedianDistinct(Expr expr) { super("Median", true, expr) ; } 
    @Override
    public Aggregator copy(ExprList expr) { return new AggMedianDistinct(expr.get(0)) ; }

    private static final NodeValue noValuesToMedian = NodeValue.nvZERO ; 

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccMedianDistinct(getExpr()) ;
    }

    @Override
    public Node getValueEmpty()     { return NodeValue.toNode(noValuesToMedian) ; } 

    @Override
    public int hashCode()   {
        return HC_AggMedianDistinct ^ getExprList().hashCode() ;
    }

    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggMedianDistinct ) ) return false ;
        AggMedianDistinct a = (AggMedianDistinct)other ;
        return exprList.equals(a.exprList, bySyntax) ;
    }

    
    // ---- Accumulator
    class AccMedianDistinct extends AccumulatorExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = noValuesToMedian ;
        private int count = 0 ;
        ArrayList<NodeValue> collection=new ArrayList<NodeValue>(); 
        
        static final boolean DEBUG = false ;
        
        public AccMedianDistinct(Expr expr) { super(expr, true) ; }

        @Override
        protected void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { 
			log.debug("median {}", nv);

            if ( nv.isNumber() )
            {
                count++ ;
                collection.add(nv);
            }
            else
                throw new ExprEvalException("median: not a number: "+nv) ;

            log.debug("median count {}", count);
        }

        @Override
        public NodeValue getAccValue()
        {
            if ( count == 0 ) return noValuesToMedian ;
            if ( super.errorCount != 0 )
                return null ;
            
            double[] arrDouble = new double[collection.size()];
            for(int i=0; i<collection.size(); i++){
            	arrDouble[i] = collection.get(i).getDouble();            	
            }

            return (NodeValue.makeDecimal((new Median().evaluate(arrDouble))));
        }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}
    }
}

