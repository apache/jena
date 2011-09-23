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

public class AggAvgDistinct extends AggregatorBase
{
    // ---- AVG(DISTINCT expr)
    private Expr expr ;

    public AggAvgDistinct(Expr expr) { this.expr = expr ; } 
    public Aggregator copy(Expr expr) { return new AggAvgDistinct(expr) ; }

    private static final NodeValue noValuesToAvg = NodeValue.nvZERO ; 

    @Override
    public String toString() { return "avg(distinct "+ExprUtils.fmtSPARQL(expr)+")" ; }
    @Override
    public String toPrefixString() { return "(avg distinct "+WriterExpr.asString(expr)+")" ; }

    @Override
    public Accumulator createAccumulator()
    { 
        return new AccAvgDistinct(expr) ;
    }

    public final Expr getExpr() { return expr ; }

    @Override
    public Node getValueEmpty()     { return NodeValue.toNode(noValuesToAvg) ; } 

    @Override
    public int hashCode()   { return HC_AggAvgDistinct ^ expr.hashCode() ; }

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof AggAvgDistinct ) ) return false ;
        AggAvgDistinct a = (AggAvgDistinct)other ;
        return expr.equals(a.expr) ;
    }

    
    // ---- Accumulator
    class AccAvgDistinct extends AccumulatorDistinctExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = noValuesToAvg ;
        private int count = 0 ;
        
        static final boolean DEBUG = false ;
        
        public AccAvgDistinct(Expr expr) { super(expr) ; }

        @Override
        protected void accumulateDistinct(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { 
            if ( nv.isNumber() )
            {
                count++ ;
                if ( total == noValuesToAvg )
                    total = nv ;
                else
                    total = XSDFuncOp.add(nv, total) ;
            }
            else
                throw new ExprEvalException("avg: not a number: "+nv) ;

            if ( DEBUG ) System.out.println("avg: ("+total+","+count+")") ;
        }

        @Override
        public NodeValue getAccValue()
        {
            if ( count == 0 ) return noValuesToAvg ;
            NodeValue nvCount = NodeValue.makeInteger(count) ;
            return XSDFuncOp.divide(total, nvCount) ;
        }

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