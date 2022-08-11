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

import java.util.HashMap ;
import java.util.Locale ;
import java.util.Map ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.sse.writers.WriterExpr ;
import org.apache.jena.sparql.util.ExprUtils ;

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
    
    protected final String name ; 
    protected final boolean isDistinct ;
    protected final ExprList exprList ;
    
    protected AggregatorBase(String name, boolean isDistinct, Expr expr) {
        this(name, isDistinct, new ExprList(expr)) ;
    }
    
    protected AggregatorBase(String name, boolean isDistinct, ExprList exprList) {
        this.name = name ;
        this.isDistinct = isDistinct ;  
        this.exprList = exprList ;
    }
    
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
        ExprList e = getExprList() ;
        if ( e != null )
            e = e.applyNodeTransform(transform) ;
        return copy(e) ;
    }
    
    /** Many aggregate use a single expression.
     *  This convenience operation gets the expression if there is exactly one.
     */
    protected Expr getExpr() {
        if ( exprList != null && exprList.size() == 1 )
            return getExprList().get(0) ;
        return null ;
    }
    
    @Override
    public ExprList getExprList()           { return exprList ; }

    @Override
    public String getName()                 { return name ; } 

    @Override
    public String toString()                { return asSparqlExpr(null) ; }

    @Override
    public String asSparqlExpr(SerializationContext sCxt) {
        IndentedLineBuffer x = new IndentedLineBuffer() ;
        x.append(getName()) ;
        x.append("(") ;
        if ( isDistinct )
            x.append("DISTINCT ") ;
        if ( getExprList() != null )
            ExprUtils.fmtSPARQL(x, getExprList(), sCxt) ;
        x.append(")") ;
        return x.asString() ;
    }

    @Override
    public String toPrefixString() {
        IndentedLineBuffer x = new IndentedLineBuffer() ;
        x.append("(") ;
        x.append(getName().toLowerCase(Locale.ROOT)) ;
        x.incIndent(); 
        if ( isDistinct )
            x.append(" distinct") ;
        for ( Expr e : getExprList() ) {
            x.append(" ");
            WriterExpr.output(x, e, null) ;
        }
        x.decIndent();
        x.append(")") ;
        return x.asString() ;
    }
    
    @Override
    public abstract int hashCode() ;

    @Override
    public final boolean equals(Object other) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof Aggregator ) ) return false ;
        return equals((Aggregator)other, false) ;
    }
    
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
    protected static final int HC_AggCustom                 =  0x181 ;

    protected static final int HC_AggMedian                 =  0x182 ;
    protected static final int HC_AggMedianDistinct         =  0x183 ;

    protected static final int HC_AggMode                   =  0x184 ;
    protected static final int HC_AggModeDistinct           =  0x185 ;

    protected static final int HC_AggFold                   =  0x186 ;

}
