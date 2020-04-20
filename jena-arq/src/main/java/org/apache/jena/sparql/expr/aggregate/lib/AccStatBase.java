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

package org.apache.jena.sparql.expr.aggregate.lib;

import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.aggregate.AccumulatorExpr ;
import org.apache.jena.sparql.function.FunctionEnv ;

/** Base for statistics aggregations */
abstract class AccStatBase extends AccumulatorExpr {
    // Could also be used for AVG and SUM but those came before this.

    private static final NodeValue noValuesToAvg = NodeValue.nvZERO ; // null? NaN?
    private static final NodeValue errorValue    = null ;

    public AccStatBase(Expr expr, boolean distinct) {
        // Merge "distinct" into AccumulatorExpr
        super(expr, distinct);
    }
    protected long   count          = 0 ;
    protected double K              = 0 ;
    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
    // Var = (SumSq − (Sum × Sum) / n) / (n − 1)
    // These are offset by K.
    protected double sum          = 0 ;
    protected double sumSquared   = 0 ;

    @Override
    final protected NodeValue getAccValue() {
        if ( super.errorCount != 0 )
            //throw new ExprEvalException("avg: error in group") ;
            return null ;
        if ( count <= 0 ) return noValuesToAvg ;
        try {
            double x1 = calc() ;
            return NodeValue.makeDouble(x1) ;
        } catch (ExprEvalException ex) { return errorValue ; }
    }

    abstract protected double calc() ;

    /** Calculate the variance (sample) */
    final protected double calcVarianceSample() {
        // (N*sum(?x*?x) - sum(?x) ) / N*(N-1)
        return AccStatLib.calcVarianceSample(sumSquared, sum, count) ;
    }

    /** Calculate the variance (population) */
    final protected double calcVariancePop() {
        // (N*sum(?x*?x) - sum(?x) ) / N*N
        return AccStatLib.calcVariancePopulation(sumSquared, sum, count) ;
    }

    @Override
    protected void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv) {
        // shifted_data_variance
        if ( nv.isNumber() ) {
            double d = nv.getDouble() ;
            count++ ;
            if ( count == 1 ) {
                K = d ;
                sum = (d-K) ; // == 0 of K set.
                sumSquared = (d-K)*(d-K) ; // == 0
                return ;
            }
            else {
                double dk = (d-K) ;
                double dk2 = dk * dk ;
                sum = sum + dk ;
                sumSquared = sumSquared + dk2 ;
            }
        }
        else
            throw new ExprEvalException("Not a number: "+nv) ;
    }

    @Override
    protected void accumulateError(Binding binding, FunctionEnv functionEnv)
    {}
}
