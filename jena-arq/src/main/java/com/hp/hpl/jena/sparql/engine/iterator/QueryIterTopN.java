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

import java.util.Arrays ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;
import java.util.PriorityQueue ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorArray ;
import org.apache.jena.atlas.iterator.IteratorDelayedInitialization ;
import org.apache.jena.atlas.lib.ReverseComparator ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator ;

public class QueryIterTopN extends QueryIterPlainWrapper
{
    /* We want to keep the N least elements (overall return is an ascending sequence so limit+ascending = least).   
     * To do that we keep a priority heap of upto N eleemnts, ordered descending.
     * To keep another element, it must be less than the max so far.
     * This leaves the least N in the heap.    
     */
	private final QueryIterator embeddedIterator;      // Keep a record of the underlying source for .cancel.
    private PriorityQueue<Binding> heap ;
    private long limit ;
    private final boolean distinct ;
	
    public QueryIterTopN(QueryIterator qIter, List<SortCondition> conditions, long numItems, boolean distinct, ExecutionContext context) {
        this(qIter, new BindingComparator(conditions, context), numItems, distinct, context) ;
    }

    public QueryIterTopN(QueryIterator qIter, Comparator<Binding> comparator, long numItems, boolean distinct, ExecutionContext context) {
        super(null, context) ;
        this.embeddedIterator = qIter ;
        this.distinct = distinct ;

        limit = numItems ;
        if ( limit == Query.NOLIMIT )
            limit = Long.MAX_VALUE ;

        if ( limit < 0 )
            throw new QueryExecException("Negative LIMIT: " + limit) ;

        if ( limit == 0 ) {
            // Keep Java happy.
            Iterator<Binding> iter0 = Iter.nullIterator() ;
            setIterator(iter0) ;
            qIter.close() ;
            return ;
        }

        // Keep heap with maximum accessible.
        this.heap = new PriorityQueue<Binding>((int)numItems, new ReverseComparator<Binding>(comparator)) ;
        this.setIterator(sortTopN(qIter, comparator)) ;
    }

    @Override
    public void requestCancel() {
        this.embeddedIterator.cancel() ;
        super.requestCancel() ;
    }

    private Iterator<Binding> sortTopN(final QueryIterator qIter, final Comparator<Binding> comparator) {
        return new IteratorDelayedInitialization<Binding>() {
            @Override
            protected Iterator<Binding> initializeIterator() {
                for (; qIter.hasNext();) {
                    Binding binding = qIter.next() ;
                    if ( heap.size() < limit )
                        add(binding) ;
                    else {
                        Binding currentMaxLeastN = heap.peek() ;
                        if ( comparator.compare(binding, currentMaxLeastN) < 0 )
                            add(binding) ;
                    }
                }
                qIter.close() ;
                Binding[] y = heap.toArray(new Binding[]{}) ;
                heap = null ;
                Arrays.sort(y, comparator) ;
                IteratorArray<Binding> iter = IteratorArray.create(y) ;
                return iter ;
            }
        } ;
    }

    private void add(Binding binding) {
        if ( distinct && heap.contains(binding) )
            return ;
        if ( heap.size() >= limit )
            heap.poll() ;  // Remove front element.
        heap.add(binding) ;
    }

}
