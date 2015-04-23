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
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;

abstract class AggMinBase extends AggregatorBase
{
    // ---- MIN(expr) and MIN(DISTINCT expr)
    protected AggMinBase(Expr expr, boolean isDistinct) { super("MIN", isDistinct, expr) ; } 

    @Override
    public
    final Accumulator createAccumulator()
    { 
        return new AccMin(getExpr()) ;
    }

    @Override
    public final Node getValueEmpty()     { return null ; } 

    // ---- Accumulator
    private static class AccMin extends AccumulatorExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue minSoFar = null ;

        public AccMin(Expr expr) { super(expr) ; }

        static final boolean DEBUG = false ;

        @Override
        public void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { 
            if ( minSoFar == null )
            {
                minSoFar = nv ;
                if ( DEBUG ) System.out.println("min: init : "+nv) ;
                return ;
            }

            int x = NodeValue.compareAlways(minSoFar, nv) ;
            if ( x > 0 )
                minSoFar = nv ;

            if ( DEBUG ) System.out.println("min: "+nv+" ==> "+minSoFar) ;
        }
        
        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}

        @Override
        public NodeValue getAccValue()
        { return minSoFar ; }
    }
}
