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
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** Syntax element and framework execution for custom aggregates.  
 */
public class AggCustom extends AggregatorBase
{
    // See also ExprAggregator
    
    private final String iri ;
    private final ExprList exprs ;

    public AggCustom(String iri, ExprList exprs) { this.iri = iri ; this.exprs = exprs ; } 
    
    @Override
    public Aggregator copy(Expr expr) { return this ; }
    
    @Override
    public String toString() {
        return "AGG <>" ;
    }

    @Override
    public String toPrefixString() { return "(agg <"+iri+">)" ; }

    @Override
    public Accumulator createAccumulator()
    { 
        return createAccNull() ;
    }

    @Override
    public Node getValueEmpty()     { return null ; } 

    @Override
    public Expr getExpr()           { return null ; }
    
    @Override
    public int hashCode()   { return HC_AggNull ; }
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ; 
        return ( other instanceof AggCustom ) ;
    } 

    public static Accumulator createAccNull() { return new  AccCustom() ; }
    
    // ---- Accumulator
    private static class AccCustom implements Accumulator
    {
        private int nBindings = 0 ;

        public AccCustom() { }

        @Override
        public void accumulate(Binding binding, FunctionEnv functionEnv)
        { nBindings++ ; }

        @Override
        public NodeValue getValue()
        {
            return null ;
        }
    }

}
