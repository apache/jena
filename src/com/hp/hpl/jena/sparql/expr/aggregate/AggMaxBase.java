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
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

abstract class AggMaxBase extends AggregatorBase
{
    // ---- MAX(expr) and MAX(DISTINCT expr)
    protected final Expr expr ;

    public AggMaxBase(Expr expr) { this.expr = expr ; } 

    @Override
    public
    final Accumulator createAccumulator()
    { 
        return new AccMax(expr) ;
    }

    public final Expr getExpr() { return expr ; }

    @Override
    public final Node getValueEmpty()     { return null ; } 

    // ---- Accumulator
    private static class AccMax extends AccumulatorExpr
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue maxSoFar = null ;

        public AccMax(Expr expr) { super(expr) ; }

        static final boolean DEBUG = false ;

        @Override
        public void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { 
            if ( maxSoFar == null )
            {
                maxSoFar = nv ;
                if ( DEBUG ) System.out.println("max: init : "+nv) ;
                return ;
            }

            int x = NodeValue.compareAlways(maxSoFar, nv) ;
            if ( x < 0 )
                maxSoFar = nv ;

            if ( DEBUG ) System.out.println("max: "+nv+" ==> "+maxSoFar) ;
        }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}

        @Override
        public NodeValue getAccValue()
        { return maxSoFar ; }
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