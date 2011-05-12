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
import com.hp.hpl.jena.sparql.graph.NodeConst ;

public class AggCountVar extends AggregatorBase
{
    // ---- COUNT(?var)
    private Expr expr ;

    public AggCountVar(Expr expr) { this.expr = expr ; }
    public Aggregator copy(Expr expr) { return new AggCountVar(expr) ; }

    @Override
    public String toString() { return "count("+expr+")" ; }
    @Override
    public String toPrefixString() { return "(count "+expr+")" ; }

    @Override
    protected Accumulator createAccumulator()
    { 
        return new AccCountVar(expr) ;
    }

    public Expr getExpr() { return expr ; }

    @Override
    public int hashCode()   { return HC_AggCountVar ^ expr.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof AggCountVar ) )
            return false ;
        AggCountVar agg = (AggCountVar)other ;
        return agg.getExpr().equals(getExpr()) ;
    }

    @Override
    public Node getValueEmpty()     { return NodeConst.nodeZero ; } 

    // ---- Accumulator
    private static class AccCountVar extends AccumulatorExpr
    {
        private long count = 0 ;
        public AccCountVar(Expr expr)   { super(expr) ; }

        @Override
        public void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv)
        { count++ ; }

        @Override
        protected void accumulateError(Binding binding, FunctionEnv functionEnv)
        {}
        
        // Ignore errors.
        @Override
        public NodeValue getValue()
        { return getAccValue() ; }

        @Override
        public NodeValue getAccValue()             { return NodeValue.makeInteger(count) ; }
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