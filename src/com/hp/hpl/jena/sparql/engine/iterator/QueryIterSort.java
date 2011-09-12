/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software Licnese (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.data.BagFactory ;
import org.openjena.atlas.data.SortedDataBag ;
import org.openjena.atlas.data.ThresholdPolicy ;
import org.openjena.atlas.data.ThresholdPolicyCount ;
import org.openjena.atlas.data.ThresholdPolicyNever ;
import org.openjena.atlas.iterator.IteratorDelayedInitialization ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.riot.SerializationFactoryFinder ;

import com.hp.hpl.jena.query.ARQ ;
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
    private static final long defaultSpillOnDiskSortingThreshold = -1 ; // off by default
    
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
        
        long threshold = (Long)context.getContext().get(ARQ.spillOnDiskSortingThreshold, defaultSpillOnDiskSortingThreshold) ;
        ThresholdPolicy<Binding> policy = (threshold >= 0) ? new ThresholdPolicyCount<Binding>(threshold) : new ThresholdPolicyNever<Binding>() ;
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

        //@Override
        public void close()
        {
            db.close();
        }
    }
    
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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