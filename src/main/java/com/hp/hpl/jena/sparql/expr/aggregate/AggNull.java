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

/** The null aggregate (which can't be written in SPARQL) 
 * calculates nothering but does help remember the group key  
 */
public class AggNull extends AggregatorBase
{
    public AggNull() { } 
    @Override
    public Aggregator copy(Expr expr) { return this ; }
    
    @Override
    public String toString() { return "aggnull()" ; }
    @Override
    public String toPrefixString() { return "(aggnull)" ; }

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
        return ( other instanceof AggNull ) ;
    } 

    public static Accumulator createAccNull() { return new  AccNull() ; }
    
    // ---- Accumulator
    private static class AccNull implements Accumulator
    {
        private int nBindings = 0 ;

        public AccNull() { }

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
