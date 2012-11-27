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

import org.apache.jena.atlas.logging.Log ;

public class AggCountDistinct extends AggregatorBase
{
    // ---- COUNT(DISTINCT *)
    public AggCountDistinct() { super() ; }
    @Override
    public Aggregator copy(Expr expr)
    { 
        if ( expr != null )
            Log.warn(this, "Copying non-null expression for COUNT(DISTINCT *)") ;
        return new AggCountDistinct() ; 
    }
    
    @Override
    public String toString()        { return "count(distinct *)" ; }
    @Override
    public String toPrefixString()  { return "(count distinct)" ; }

    @Override
    public Expr getExpr()           { return null ; }
    
    @Override
    public Accumulator createAccumulator()
    { 
        return new AccCountDistinct() ; 
    }

    @Override
    public Node getValueEmpty()     { return NodeConst.nodeZero ; }

    @Override
    public int hashCode()   { return HC_AggCountDistinct ; }

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggCountDistinct ) ) return false ;
        return true ;
    }

    static class AccCountDistinct extends AccumulatorDistinctAll
    {
        private long count = 0 ;
        public AccCountDistinct()   { }

        @Override public void accumulateDistinct(Binding binding, FunctionEnv functionEnv)
        { count++ ; }

        // Errors can't occur.

        @Override
        public NodeValue getValue()
        { return NodeValue.makeInteger(count) ; }
    }
}
