/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

public class AggGroupConcat extends AggregatorBase
{
    static final String SeparatorDefault = " " ;
    private final Expr expr ;
    private final String separatorSeen ;
    private final String separator ;

    public AggGroupConcat(Expr expr, String separator)
    {
        this(expr, 
             ( separator != null ) ? separator : SeparatorDefault ,
             separator) ;
//        this.expr = expr ; 
//        separatorSeen = separator ;
//        this.separator = ( separator != null ) ? separator : SeparatorDefault ; 
    } 
    
    private AggGroupConcat(Expr expr, String separator, String separatorSeen)
    {
        this.expr = expr ; 
        this.separatorSeen = separatorSeen ;
        this.separator = separator ; 
    }
    
    public Aggregator copy(Expr expr) { return new AggGroupConcat(expr, separator, separatorSeen) ; }

    @Override
    public String toString()
    {
        String x = "GROUP_CONCAT("+ExprUtils.fmtSPARQL(expr) ;
        if ( separatorSeen != null )
        {
            String y = StrUtils.escapeString(separatorSeen) ;
            x = x+"; SEPARATOR='"+y+"'" ;
        }
        x = x+")" ;
        return x ; 
    }    
    
    @Override
    public String toPrefixString()
    {
        String x = "(group_concat " ;
        
        if ( separatorSeen != null )
        {
            String y = StrUtils.escapeString(separatorSeen) ;
            x = x+"(separator '"+y+"') " ;
        }
        x = x+WriterExpr.asString(expr)+")" ;
        return x ; 
    }

    @Override
    protected Accumulator createAccumulator()
    { 
        return new AccGroupConcat(expr, separator) ;
    }

    public Expr getExpr() { return expr ; }
    protected final String getSeparator() { return separator ; }

    @Override
    public Node getValueEmpty() { return NodeConst.emptyString ; } 
    
    @Override
    public int hashCode()   { return HC_AggCountVar ^ expr.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof AggGroupConcat ) )
            return false ;
        AggGroupConcat agg = (AggGroupConcat)other ;
        return Lib.equal(agg.getSeparator(),getSeparator()) && agg.getExpr().equals(getExpr()) ;
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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