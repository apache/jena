/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

public class AggSumDistinct  extends AggregatorBase
{
    // ---- SUM(DISTINCT expr)
    private Expr expr ;

    public AggSumDistinct(Expr expr) { this.expr = expr ; } 
    public Aggregator copy(Expr expr) { return new AggSumDistinct(expr) ; }

    private static final NodeValue noValuesToSum = NodeValue.nvZERO ; 
    
    @Override
    public String toString() { return "sum(distinct "+ExprUtils.fmtSPARQL(expr)+")" ; }
    @Override
    public String toPrefixString() { return "(sum distinct "+WriterExpr.asString(expr)+")" ; }

    @Override
    protected Accumulator createAccumulator()
    { 
        return new AccSumDistinct(expr) ;
    }

    public Expr getExpr() { return expr ; }

    @Override
    public int hashCode()   { return HC_AggSumDistinct ^ expr.hashCode() ; }
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ; 
        if ( ! ( other instanceof AggSumDistinct ) )
            return false ;
        AggSumDistinct agg = (AggSumDistinct)other ;
        return agg.getExpr().equals(getExpr()) ;
    } 
 
    @Override
    public Node getValueEmpty()     { return NodeValue.toNode(noValuesToSum) ; } 

    // ---- Accumulator
    class AccSumDistinct extends AccumulatorDistinctExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = null ;

        public AccSumDistinct(Expr expr) { super(expr) ; }

        @Override
        public void accumulateDistinct(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { 
            if ( nv.isNumber() )
            {
                if ( total == null )
                    total = nv ;
                else
                    total = XSDFuncOp.add(nv, total) ;
            }
            else
                throw new ExprEvalException("Not a number: "+nv) ;
        }
        
        @Override
        public NodeValue getAccValue()
        { return total ; }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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