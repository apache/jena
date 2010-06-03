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
    private final Set<NodeValue> rows = new HashSet<NodeValue>() ;
    private long errorCount = 0 ; 
    private final Expr expr ;
    
    protected AccumulatorDistinctExpr(Expr expr)
    {
        this.expr = expr ;
    }
    
    final public void accumulate(Binding binding, FunctionEnv functionEnv)
    {
        try { 
            NodeValue nv = expr.eval(binding, functionEnv) ;
            if ( rows.contains(nv) )
                return ;
            accumulateDistinct(nv, binding, functionEnv) ;
        } catch (ExprEvalException ex)
        {
            errorCount++ ;
            accumulateError(binding, functionEnv) ;
        }
    }
    
    protected long getErrorCount() { return errorCount ; }
     
    protected abstract void accumulateDistinct(NodeValue nv, Binding binding, FunctionEnv functionEnv) ;
    protected abstract void accumulateError(Binding binding, FunctionEnv functionEnv) ;
}