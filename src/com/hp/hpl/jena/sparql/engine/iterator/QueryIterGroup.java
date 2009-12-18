/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingKey;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.E_Aggregator;

public class QueryIterGroup extends QueryIterPlainWrapper
{
    public QueryIterGroup(QueryIterator qIter, 
                          VarExprList groupVars,
                          List<E_Aggregator> aggregators,
                          ExecutionContext execCxt)
    {
        super(null, execCxt) ;
        Iterator<Binding> iter = calc(qIter, groupVars, aggregators, execCxt) ;
        setIterator(iter) ;
    }

    // Phase 1 : Consume the input iterator, assigning groups (keys) 
    //           and push rows through the aggregator function. 
    
    // Phase 2 : Go over the group bindings and assign the value of each aggregation.
    
    private static Iterator<Binding> calc(QueryIterator iter, 
                                          VarExprList groupVars, List<E_Aggregator> aggregators,
                                          ExecutionContext execCxt)
    {
        // Phase 1 : assign bindings to buckets by key and pump through the aggregrators.
        Map<BindingKey, Binding> buckets = new HashMap<BindingKey, Binding>() ;    
        
        for ( ; iter.hasNext() ; )
        {
            Binding b = iter.nextBinding() ;
            BindingKey key = genKey(groupVars, b, execCxt) ;
            
            // Assumes key binding has value based .equals/.hashCode. 
            if ( ! buckets.containsKey(key) )
                buckets.put(key, key.getBinding()) ;
            
            // Assumes an aggregator is a per-execution mutable thingy
            if ( aggregators != null )
            {
                for ( Iterator<E_Aggregator> aggIter = aggregators.iterator() ; aggIter.hasNext() ; )
                {
                    E_Aggregator agg = aggIter.next();
                    agg.getAggregator().accumulate(key, b, execCxt) ;
                }
            }
        }
        
        // Phase 2 : Empty input
        
        // If there are no binding from the input stage, two things can happen.
        //   If there are no aggregators, there are no groups.
        //   If there are aggregators, then they may have a default value. 
        
        if ( buckets.isEmpty() )
        {
            // The answer of an empty pattern. 
            boolean valueExists = false ;
            Binding binding = new BindingMap() ;
            
            if ( aggregators != null )
            {
                for ( Iterator<E_Aggregator> aggIter = aggregators.iterator() ; aggIter.hasNext() ; )
                {
                    E_Aggregator agg = aggIter.next();
                    Var v = agg.asVar() ;
                    Node value = agg.getAggregator().getValueEmpty() ;
                    if ( value != null )
                    {
                        binding.add(v, value) ;
                        valueExists = true ;
                    }
                }
            }
            
            if ( valueExists )
                return QueryIterSingleton.create(binding, execCxt) ;
            else 
                return new QueryIterNullIterator(execCxt) ;
        }
        
        
        // Phase 2 : There was input and so there are some groups.
        
        // For each bucket, get binding, add aggregator values to the binding.
        if ( aggregators != null )
        {
            for ( Iterator<BindingKey> bIter = buckets.keySet().iterator() ; bIter.hasNext(); )
            {
                BindingKey key = bIter.next();
                
                // Maybe null
                Binding binding = buckets.get(key) ; // == key.getBinding() ;
                
                for ( Iterator<E_Aggregator> aggIter = aggregators.iterator() ; aggIter.hasNext() ; )
                {
                    E_Aggregator agg = aggIter.next();
                    Var v = agg.asVar() ;
                    Node value =  agg.getAggregator().getValue(key) ;
                    if ( value != null )
                        // Extend with the aggregations.
                        binding.add(v, value) ;
                }
            }
        }

        // Results - the binding modified by the aggregations.
        
        return buckets.values().iterator() ;
    }
    
    static private BindingKey genKey(VarExprList vars, Binding binding, ExecutionContext execCxt) 
    {
        return new BindingKey(copyProject(vars, binding, execCxt)) ;
    }
    
    static private Binding copyProject(VarExprList vars, Binding binding, ExecutionContext execCxt)
    {
        // No group vars (implicit or explicit) => working on whole result set. 
        // Still need a BindingMap to assign to later.
        Binding x = new BindingMap() ;
        for ( Iterator<Var> iter = vars.getVars().iterator() ; iter.hasNext() ; )
        {
            Var var = iter.next() ;
            Node node = vars.get(var, binding, execCxt) ;
            if ( node != null )
                x.add(var, node) ;
        }
        return x ;
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