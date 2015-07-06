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

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.sse.writers.WriterExpr ;
import org.apache.jena.sparql.util.ExprUtils ;

public class AggGroupConcat extends AggregatorBase
{
    static final String SeparatorDefault = " " ;
    private final String separator ;
    private final String effectiveSeparator ;

    public AggGroupConcat(Expr expr, String separator)
    {
        this(expr, 
             ( separator != null ) ? separator : SeparatorDefault ,
             separator) ;
    } 
    
    private AggGroupConcat(Expr expr, String effectiveSeparator, String separatorSeen)
    {
        super("GROUP_CONCAT", false, expr) ;
        this.separator = separatorSeen ;
        this.effectiveSeparator = effectiveSeparator ; 
    }
    
    @Override
    public Aggregator copy(ExprList expr) { return new AggGroupConcat(expr.get(0), effectiveSeparator, separator) ; }

    @Override
    public String toPrefixString() {
        return prefixGroupConcatString(super.isDistinct,  separator, getExprList()) ;
    }
    
    @Override
    public String asSparqlExpr(SerializationContext sCxt) {
        return asSparqlExpr(isDistinct, separator, exprList, sCxt) ;
    }
    
    protected static String asSparqlExpr(boolean isDistinct, String separator, ExprList exprs, SerializationContext sCxt) {
        IndentedLineBuffer x = new IndentedLineBuffer() ;
        x.append("GROUP_CONCAT") ;
        if ( isDistinct )
            x.append(" DISTINCT") ;
        x.append(" (") ;
        ExprUtils.fmtSPARQL(x, exprs, sCxt) ;
        if ( separator != null ) {
            x.append(" ; separator=") ;
            String y = StrUtils.escapeString(separator) ;
            x.append("'") ;
            x.append(y) ;
            x.append("'") ;
        }
            
        x.append(")") ;
        
        return x.asString() ;
    }
    
    protected static String prefixGroupConcatString(boolean isDistinct, String separator, ExprList exprs) { 
        IndentedLineBuffer x = new IndentedLineBuffer() ;
        x.append("(") ;
        x.append("group_concat") ;
        if ( isDistinct )
            x.append(" distinct") ;
        if ( separator != null )
        {
            String y = StrUtils.escapeString(separator) ;
            x.append("(separator '") ;
            x.append(y) ;
            x.append("')") ;
        }
        x.incIndent(); 
        for ( Expr e : exprs ) {
            x.append(" ");
            WriterExpr.output(x, e, null) ;
        }
        x.decIndent();
        x.append(")") ;
        return x.asString() ;
    }
    
    @Override
    public Accumulator createAccumulator()
    { 
        return new AccGroupConcat(getExpr(), effectiveSeparator) ;
    }

    public String getSeparator() { return separator ; }

    @Override
    public Node getValueEmpty() { return NodeConst.emptyString ; } 
    
    @Override
    public int hashCode()   { return HC_AggCountVar ^ exprList.hashCode() ; }
    
    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggGroupConcat ) )
            return false ;
        AggGroupConcat agg = (AggGroupConcat)other ;
        return Objects.equals(agg.getSeparator(), getSeparator()) &&
               agg.getExpr().equals(getExpr(), bySyntax) ;
    }

    // ---- Accumulator
    private static class AccGroupConcat extends AccumulatorExpr
    {
        private StringBuilder stringSoFar = new StringBuilder() ;
        private boolean first = true ;
        private final String separator ;

        public AccGroupConcat(Expr expr, String sep)
        { super(expr) ; this.separator = sep ; }

        @Override
        protected void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
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
