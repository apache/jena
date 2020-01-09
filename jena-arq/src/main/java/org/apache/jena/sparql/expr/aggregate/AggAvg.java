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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggAvg extends AggregatorBase
{
    // ---- AVG(?var)
    public AggAvg(Expr expr) { super("AVG", false, expr) ; } 
    
    private static Logger log = LoggerFactory.getLogger("AVG") ;

    @Override
    public Aggregator copy(ExprList expr) { return new AggAvg(expr.get(0)) ; }

    // XQuery/XPath Functions&Operators suggests zero
    // SQL suggests null.
    private static final NodeValue noValuesToAvg = NodeValue.nvZERO ; // null 

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccAvg(getExpr()) ;
    }

    @Override
    public Node getValueEmpty()     { return NodeValue.toNode(noValuesToAvg) ; } 
    
    @Override
    public int hashCode()   { return HC_AggAvg ^ getExprList().hashCode() ; }

    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggAvg ) ) return false ;
        AggAvg a = (AggAvg)other ;
        return exprList.equals(a.exprList, bySyntax) ;
    }
    
    // ---- Accumulator
    private static class AccAvg extends AccumulatorExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = noValuesToAvg ;
        private int count = 0 ;

        public AccAvg(Expr expr) { super(expr, false) ; }

        @Override
        protected void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { 
			log.debug("avg {}", nv);

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

            log.debug("avg count {}", count);

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
