package com.hp.hpl.jena.sparql.expr.aggregate;

import java.util.HashSet ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** Accumulator that only passes down unique values of an expression (the first encountered) */
abstract class AccumulatorDistinctExpr implements Accumulator
{
    // Better?? record a large hash. 
    private final Set<NodeValue> values = new HashSet<NodeValue>() ;
    private long errorCount = 0 ; 
    private long count = 0 ;
    private final Expr expr ;
    
    protected AccumulatorDistinctExpr(Expr expr)
    {
        this.expr = expr ;
    }
    
    final public void accumulate(Binding binding, FunctionEnv functionEnv)
    {
        try { 
            NodeValue nv = expr.eval(binding, functionEnv) ;
            if ( values.contains(nv) )
                return ;
            values.add(nv) ;
            accumulateDistinct(nv, binding, functionEnv) ;
            count++ ;
        } catch (ExprEvalException ex)
        {
            errorCount++ ;
            accumulateError(binding, functionEnv) ;
        }
    }
    
    // Count(DISTINCT ?v) is different
    public NodeValue getValue()
    {
        if ( errorCount == 0 )
            return getAccValue() ;  
        return null ;
    }
    
    protected long getErrorCount() { return errorCount ; }
     
    protected abstract NodeValue getAccValue() ; 
    protected abstract void accumulateDistinct(NodeValue nv, Binding binding, FunctionEnv functionEnv) ;
    protected abstract void accumulateError(Binding binding, FunctionEnv functionEnv) ;
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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