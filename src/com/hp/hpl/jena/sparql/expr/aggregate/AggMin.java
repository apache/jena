/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class AggMin implements AggregateFactory
{
    // ---- MIN(?var)
    
    // ---- AggregatorFactory
    private Expr expr ;

    public AggMin(Expr var) { this.expr = var ; } 

    public Aggregator create()
    {
        // One per each time there is an aggregation.
        return new AggMinWorker() ;
    }
    
    private static final NodeValue noValuesToMin = null ; 
    
    // ---- Aggregator
    class AggMinWorker extends AggregatorBase
    {
        public AggMinWorker()
        {
            super() ;
        }

        @Override
        public String toString() { return "min("+ExprUtils.fmtSPARQL(expr)+")" ; }
        public String toPrefixString() { return "(min "+WriterExpr.asString(expr)+")" ; }

        @Override
        protected Accumulator createAccumulator()
        { 
            return new AccMinVar() ;
        }
        
        private final Expr getExpr() { return expr ; }
        
        public boolean equalsAsExpr(Aggregator other)
        {
            if ( ! ( other instanceof AggMinWorker ) )
                return false ;
            AggMinWorker agg = (AggMinWorker)other ;
            return agg.getExpr().equals(getExpr()) ;
        } 

        
        /* null is SQL-like. */ 
        @Override
        public Node getValueEmpty()     { return null ; } 
    }

    // ---- Accumulator
    class AccMinVar implements Accumulator
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue minSoFar = null ;
        
        public AccMinVar() {}
        
        static final boolean DEBUG = false ;

        public void accumulate(Binding binding, FunctionEnv functionEnv)
        { 
            try {
                NodeValue nv = expr.eval(binding, functionEnv) ;
                if ( minSoFar == null )
                {
                    minSoFar = nv ;
                    if ( DEBUG ) System.out.println("min: init : "+nv) ;
                    return ;
                }
                
                int x = NodeValue.compareAlways(minSoFar, nv) ;
                if ( x > 0 )
                    minSoFar = nv ;
                
                if ( DEBUG ) System.out.println("min: "+nv+" ==> "+minSoFar) ;

                
            } catch (ExprEvalException ex)
            {}
        }
        public NodeValue getValue()
        { return minSoFar ; }
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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