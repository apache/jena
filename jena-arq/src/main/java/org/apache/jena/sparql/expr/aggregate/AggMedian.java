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
import java.util.Arrays;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class AggMedian extends AggregatorBase
{
    // ---- MEDIAN(?var)
    private static Logger log = LoggerFactory.getLogger("Median") ;

    public AggMedian(Expr expr) { super("MEDIAN", false, expr) ; } 
    @Override
    public Aggregator copy(ExprList expr) { return new AggMedian(expr.get(0)) ; }

    // XQuery/XPath Functions&Operators suggests zero
    // SQL suggests null.
    private static final NodeValue noValuesToMedian = NodeValue.nvZERO ; // null 

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccMedian(getExpr()) ;
    }

    @Override
    public Node getValueEmpty()     { return NodeValue.toNode(noValuesToMedian) ; } 
    
    @Override
    public int hashCode()   { return HC_AggMedian ^ getExprList().hashCode() ; }

    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggMedian ) ) return false ;
        AggMedian a = (AggMedian)other ;
        return exprList.equals(a.exprList, bySyntax) ;
    }
    
    // ---- Accumulator
    private static class AccMedian extends AccumulatorExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = noValuesToMedian ;
        private int count = 0 ;
        ArrayList<NodeValue> collection=new ArrayList<NodeValue>(); 
                
        public AccMedian(Expr expr) { super(expr, false) ; }

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
            {
                //ARQ.getExecLogger().warn("Evaluation error: median() on "+nv) ;
                throw new ExprEvalException("median: not a number: "+nv) ;
            }

            log.debug("median count {}", count);
        }

        @Override
        public NodeValue getAccValue()
        {
            double median;
            if ( count == 0 ) return noValuesToMedian ;
            if ( super.errorCount != 0 )
                return null ;

            int indexsize = collection.size();
            double[] arrDouble = new double[indexsize];
            for(int i=0; i<indexsize; i++){
            	arrDouble[i] = collection.get(i).getDouble();	
            }

            Arrays.sort(arrDouble);

            if(indexsize%2!=0) {
            	median = arrDouble[(indexsize/2)];
            }else {	
            	median = ((arrDouble[(indexsize/2)]+arrDouble[((indexsize/2)-1)])/2);
            }

            return NodeValue.makeDecimal(median);
        }
    	
        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}
    }
}
