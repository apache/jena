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

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

public class AggGroupConcatDistinct extends AggregatorBase
{
    private final Expr expr ;
    private final String separator ;
    private final String effectiveSeparator ;

    public AggGroupConcatDistinct(Expr expr, String separator)
    { 
        this(expr, 
             ( separator != null ) ? separator : AggGroupConcat.SeparatorDefault ,
             separator) ;
    }

    private AggGroupConcatDistinct(Expr expr, String effectiveSeparator, String separatorSeen)
    {
        this.expr = expr ; 
        this.separator = separatorSeen ;
        this.effectiveSeparator = effectiveSeparator ; 
    }
    
    @Override
    public Aggregator copy(Expr expr) { return new AggGroupConcatDistinct(expr, effectiveSeparator, separator) ; }

    @Override
    public String toString()
    {
        String x = "GROUP_CONCAT(DISTINCT "+ExprUtils.fmtSPARQL(expr) ;
        if ( separator != null )
        {
            String y = StrUtils.escapeString(separator) ;
            x = x+"; SEPARATOR='"+y+"'" ;
        }
        x = x+")" ;
        return x ; 
    }    
    
    @Override
    public String toPrefixString()
    {
        String x = "(group_concat distinct " ;
        
        if ( separator != null )
        {
            String y = StrUtils.escapeString(separator) ;
            x = x+"(separator '"+y+"') " ;
        }
        x = x+WriterExpr.asString(expr)+")" ;
        return x ; 
    }

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccGroupConcatDistinct(expr, effectiveSeparator) ;
    }

    @Override
    public Expr getExpr() { return expr ; }
    
    public String getSeparator() { return separator ; }

    @Override
    public Node getValueEmpty()     { return null ; } 

    @Override
    public int hashCode()   { return HC_AggCountVar ^ expr.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof AggGroupConcatDistinct ) )
            return false ;
        AggGroupConcatDistinct agg = (AggGroupConcatDistinct)other ;
        return Lib.equal(agg.getSeparator(),getSeparator()) && agg.getExpr().equals(getExpr()) ;
    }
    
    // ---- Accumulator
    static class AccGroupConcatDistinct extends AccumulatorDistinctExpr
    {
        private StringBuilder stringSoFar = new StringBuilder() ;
        private boolean first = true ;
        private final String separator ;

        public AccGroupConcatDistinct(Expr expr, String sep)
        { super(expr) ; this.separator = sep ; }

        @Override
        public void accumulateDistinct(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        {
            String str = nv.asString() ;
            if ( ! first )
                stringSoFar.append(separator) ;
            stringSoFar.append(str) ;
            first = false ;
        }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}
        
        @Override
        public NodeValue getAccValue()
        { return NodeValue.makeString(stringSoFar.toString()) ; }
    }
}
