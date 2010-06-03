package com.hp.hpl.jena.sparql.expr.aggregate;

import java.util.HashSet ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** Accumulator that only passes down the first unique binding */
abstract class AccumulatorDistinctAll implements Accumulator
{
    private final Set<Binding> rows = new HashSet<Binding>() ;
    
    final public void accumulate(Binding binding, FunctionEnv functionEnv)
    {
        if ( rows.contains(binding) )
            return ;
        rows.add(binding) ;
        accumulateDistinct(binding, functionEnv) ;
    }
    
    protected abstract void accumulateDistinct(Binding binding, FunctionEnv functionEnv) ;
}