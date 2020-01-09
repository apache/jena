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

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingProjectNamed;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.serializer.SerializationContext ;

public class AggCountDistinct extends AggregatorBase
{
    // ---- COUNT(DISTINCT *)
    public AggCountDistinct() { super("COUNT", true, (ExprList)null) ; }
    @Override
    public Aggregator copy(ExprList expr)
    { 
        if ( expr != null )
            Log.warn(this, "Copying non-null expression for COUNT(DISTINCT *)") ;
        return new AggCountDistinct() ; 
    }
    
    @Override
    public String asSparqlExpr(SerializationContext sCxt)       { return "count(distinct *)" ; }
    @Override
    public String toString()        { return "count(distinct *)" ; }
    @Override
    public String toPrefixString()  { return "(count distinct)" ; }

    @Override
    public Expr getExpr()           { return null ; }
    
    @Override
    public Accumulator createAccumulator() {
        return new AccumulatorDistinctAll() ;
    }

    @Override
    public Node getValueEmpty()     { return NodeConst.nodeZero ; }

    @Override
    public int hashCode()   { return HC_AggCountDistinct ; }

    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggCountDistinct ) ) return false ;
        return true ;
    }

    /** Accumulator that only passes down the first unique binding */
    static class AccumulatorDistinctAll implements Accumulator {
        // COUNT(DISTINCT *)
        private final Set<Binding> rows  = new HashSet<>() ;
        private long               count = 0 ;

        @Override
        final public void accumulate(Binding binding, FunctionEnv functionEnv) {
            // Hide system vars.
            binding = new BindingProjectNamed(binding) ;
            if ( rows.contains(binding) )
                return ;
            rows.add(binding) ;
            accumulateDistinct(binding, functionEnv) ;
        }

        public void accumulateDistinct(Binding binding, FunctionEnv functionEnv) {
            count++ ;
        }

        // Errors can't occur.

        @Override
        public NodeValue getValue() {
            return NodeValue.makeInteger(count) ;
        }
    }
}
