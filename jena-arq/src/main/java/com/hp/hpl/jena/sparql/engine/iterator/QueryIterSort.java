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

import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.data.BagFactory ;
import org.apache.jena.atlas.data.SortedDataBag ;
import org.apache.jena.atlas.data.ThresholdPolicy ;
import org.apache.jena.atlas.data.ThresholdPolicyFactory ;
import org.apache.jena.atlas.iterator.IteratorDelayedInitialization ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.riot.system.SerializationFactoryFinder ;

import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator ;

/** 
 * Sort a query iterator.  The sort will happen in-memory unless the size of the
 * iterator exceeds a configurable threshold. In that case, a disk sort is used.
 * 
 * @see SortedDataBag
 */

public class QueryIterSort extends QueryIterPlainWrapper
{
	private final QueryIterator embeddedIterator;      // Keep a record of the underlying source for .cancel.
	final SortedDataBag<Binding> db;
	
    public QueryIterSort(QueryIterator qIter, List<SortCondition> conditions, ExecutionContext context)
    {
        this(qIter, new BindingComparator(conditions, context), context) ;
    }

    public QueryIterSort(final QueryIterator qIter, final Comparator<Binding> comparator, final ExecutionContext context)
    {
        super(null, context) ;
        this.embeddedIterator = qIter ;
        
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(context.getContext());
        this.db = BagFactory.newSortedBag(policy, SerializationFactoryFinder.bindingSerializationFactory(), comparator);
        
        this.setIterator(new SortedBindingIterator(qIter));
    }

    @Override
    public void requestCancel()
    {
        this.embeddedIterator.cancel() ;
        super.requestCancel() ;
    }

    private class SortedBindingIterator extends IteratorDelayedInitialization<Binding> implements Closeable
    {
        private final QueryIterator qIter;
        
        public SortedBindingIterator(final QueryIterator qIter)
        {
            this.qIter = qIter;
        }
        
        @Override
        protected Iterator<Binding> initializeIterator()
        {
            try
            {
                db.addAll(qIter);
            }
            // Should we catch other exceptions too?  Theoretically the user should be using this
            // iterator in a try/finally block, and thus will call close() themselves. 
            catch (QueryCancelledException e)
            {
                close();
                throw e;
            }
            
            return db.iterator();
        }

        @Override
        public void close()
        {
            db.close();
        }
    }
    
}
