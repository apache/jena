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

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** Aggregate that does everything except the per-group aggregation that is needed for each operation */  
public abstract class AggregatorBase implements Aggregator 
{
    // Aggregator -- handles one aggregation over one group, and is the syntax unit.
    
    // AggregateFactory -- delays the creating of Aggregator so multiple mentions over the same group gives the same Aggregator
    
    // Accumulator -- the per-group, per-key accumulator for the aggregate
    // queries track their aggregators so if one is used twice, the calculataion is only done once.
    // For distinct, that means only uniquefier. 
    
    // Built-ins: COUNT, SUM, MIN, MAX, AVG, GROUP_CONCAT, and SAMPLE
    // but COUNT(*) and COUNT(Expr) are different beasts
    // each in DISTINCT and non-DISTINCT versions 
    
    protected AggregatorBase() {}
    
    private Map<Binding, Accumulator> buckets = new HashMap<>() ;   // Bindingkey => Accumulator

    @Override
    public abstract Accumulator createAccumulator() ;
    
    @Override
    public abstract Node getValueEmpty() ;

    public Node getValue(Binding key)
    {
        Accumulator acc = buckets.get(key) ;
        if ( acc == null )
            throw new ARQInternalErrorException("Null for accumulator") ;
        NodeValue nv = acc.getValue();
        if ( nv == null ) 
            return null ;
        return nv.asNode() ;
    }
    
    @Override
    public String key() {  return toPrefixString() ; }
    
    @Override
    public final Aggregator copyTransform(NodeTransform transform)
    {
        Expr e = getExpr() ;
        if ( e != null )
            e = e.applyNodeTransform(transform) ;
        return copy(e) ;
    }
    
    @Override
    public abstract String toString() ;

    @Override
    public abstract String toPrefixString() ;
    
    @Override
    public abstract int hashCode() ;

    @Override
    public abstract boolean equals(Object other) ;
    
    protected static final int HC_AggAvg                    =  0x170 ;
    protected static final int HC_AggAvgDistinct            =  0x171 ;

    protected static final int HC_AggCount                  =  0x172 ;
    protected static final int HC_AggCountDistinct          =  0x173 ;

    protected static final int HC_AggCountVar               =  0x174 ;
    protected static final int HC_AggCountVarDistinct       =  0x175 ;

    protected static final int HC_AggMin                    =  0x176 ;
    protected static final int HC_AggMinDistinct            =  0x177 ;
    
    protected static final int HC_AggMax                    =  0x178 ;
    protected static final int HC_AggMaxDistinct            =  0x179 ;

    protected static final int HC_AggSample                 =  0x17A ;
    protected static final int HC_AggSampleDistinct         =  0x17B ;
    
    protected static final int HC_AggSum                    =  0x17C ;
    protected static final int HC_AggSumDistinct            =  0x17D ;
    
    protected static final int HC_AggGroupConcat            =  0x17E ;
    protected static final int HC_AggGroupConcatDistinct    =  0x17F ;
    
    protected static final int HC_AggNull                   =  0x180 ;
    
}
