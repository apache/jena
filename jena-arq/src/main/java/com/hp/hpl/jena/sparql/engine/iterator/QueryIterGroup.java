/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorDelayedInitialization ;
import org.apache.jena.atlas.lib.MultiMap ;
import org.apache.jena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.aggregate.Accumulator ;

public class QueryIterGroup extends QueryIterPlainWrapper
{
	private final QueryIterator embeddedIterator;

	public QueryIterGroup(QueryIterator qIter, 
                          VarExprList groupVars,
                          List<ExprAggregator> aggregators,
                          ExecutionContext execCxt)
    {
        super(null, execCxt) ;
        this.embeddedIterator = qIter;
        Iterator<Binding> iter = calc(qIter, groupVars, aggregators, execCxt) ;
        setIterator(iter) ;
    }

	@Override
	public void requestCancel()
	{
	    this.embeddedIterator.cancel();
	    super.requestCancel() ;
	}
	
    // Phase 1 : Consume the input iterator, assigning groups (keys) 
    //           and push rows through the aggregator function. 
    
    // Phase 2 : Go over the group bindings and assign the value of each aggregation.
	
	private static Pair<Var, Accumulator> placeholder = Pair.create((Var)null, (Accumulator)null) ; 
	
    
    private static Iterator<Binding> calc(final QueryIterator iter, 
                                          final VarExprList groupVarExpr, final List<ExprAggregator> aggregators,
                                          final ExecutionContext execCxt)
    {
        return new IteratorDelayedInitialization<Binding>() {
            @Override
            protected Iterator<Binding> initializeIterator() {

                boolean noAggregators =  ( aggregators == null || aggregators.size() == 0 ) ;
                
                // Phase 1 : assign bindings to buckets by key and pump through the aggregrators.
                MultiMap<Binding, Pair<Var, Accumulator>> accumulators = MultiMap.createMapList() ;

                for ( ; iter.hasNext() ; )
                {
                    Binding b = iter.nextBinding() ;
                    Binding key = genKey(groupVarExpr, b, execCxt) ;

                    if ( noAggregators )
                    {
                        // Put in a dummy to remember the input.
                        accumulators.put(key, placeholder ) ;
                        continue ;
                    }

                    Collection<Pair<Var, Accumulator>> accs = accumulators.get(key) ;
                    // Create if does not exist.
                    if ( accs == null )
                    {
                        for ( ExprAggregator agg : aggregators )
                        {
                            Accumulator x = agg.getAggregator().createAccumulator() ;
                            Var v = agg.getVar() ;
                            accumulators.put(key, Pair.create(v, x)) ;
                        }
                        accs = accumulators.get(key) ;
                    }

                    // Do the per-accumulator calculation.
                    for ( Pair<Var, Accumulator> pair : accs )
                        pair.getRight().accumulate(b, execCxt) ;
                }

                // Phase 2 : Empty input
                // has as iter.hasNext false at start.

                // If there are no binding from the input stage, two things can happen.
                //   If there are no aggregators, there are no groups.
                //   If there are aggregators, then they may have a default value. 

                if ( accumulators.isEmpty() )
                {
                    if ( noAggregators )
                    {
                        // No rows to group, no aggregators.
                        // ==> No result rows.
                        return Iter.nullIterator() ;
                    }
                    
                    BindingMap binding = BindingFactory.create() ;

                    for ( ExprAggregator agg : aggregators )
                    {
                        Var v = agg.getVar();
                        Node value = agg.getAggregator().getValueEmpty();
                        if ( value != null )
                        {
                            binding.add( v, value );
                        }
                    }
                        
                    if ( binding == null )
                        // This does not happen if there are any aggregators. 
                        return Iter.nullIterator() ;
                    // cast to get the static type inference to work.
                    return Iter.singletonIter((Binding)binding) ;
                }

                // Phase 2 : There was input and so there are some groups.
                // For each bucket, get binding, add aggregator values to the binding.
                // We used AccNull so there are always accumulators. 
                
                if ( noAggregators )
                    // We used placeholder so there are always the key. 
                    return accumulators.keys().iterator() ;
                
                List<Binding> results = new ArrayList<>() ;

                for ( Binding k : accumulators.keys() )
                {
                    Collection<Pair<Var, Accumulator>> accs = accumulators.get(k) ;
                    BindingMap b = BindingFactory.create(k) ;
                    
                    for ( Pair<Var, Accumulator> pair : accs )
                    {
                        Var v = pair.getLeft() ;
                        NodeValue value = pair.getRight().getValue() ;
                        Node n = (value==null) ? null : value.asNode() ;
                        if ( v == null || n == null )
                        {} 
                        else
                            b.add(v, n) ;
                    }
                    results.add(b) ;
                }
                return results.iterator() ;
            }
        };
    }
    
    static private Binding genKey(VarExprList vars, Binding binding, ExecutionContext execCxt) 
    {
        return copyProject(vars, binding, execCxt) ;
    }
    
    static private Binding copyProject(VarExprList vars, Binding binding, ExecutionContext execCxt)
    {
        // No group vars (implicit or explicit) => working on whole result set. 
        // Still need a BindingMap to assign to later.
        BindingMap x = BindingFactory.create() ;
        for ( Var var : vars.getVars() )
        {
            Node node = vars.get( var, binding, execCxt );
            // Null returned for unbound and error.
            if ( node != null )
            {
                x.add( var, node );
            }
        }
        return x ;
    }
}
