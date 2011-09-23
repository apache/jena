/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
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
    
    private Map<Binding, Accumulator> buckets = new HashMap<Binding, Accumulator>() ;   // Bindingkey => Accumulator

    final
    public void accumulate(Binding key, Binding binding, FunctionEnv functionEnv)
    {
        Accumulator acc = buckets.get(key) ;
        if ( acc == null )
        {
            acc = createAccumulator() ;
            buckets.put(key, acc) ;
        }
        acc.accumulate(binding, functionEnv) ;
    }

    // Temporary for development.
    public Accumulator createAcc() { return createAccumulator() ; }
    protected abstract Accumulator createAccumulator() ;
    
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
    
    public String key() {  return toPrefixString() ; }
    
    public final Aggregator copyTransform(NodeTransform transform)
    {
        Expr e = getExpr() ;
        if ( e != null )
            e = e.applyNodeTransform(transform) ;
        return copy(e) ;
    }
    
    @Override
    public abstract String toString() ;

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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */