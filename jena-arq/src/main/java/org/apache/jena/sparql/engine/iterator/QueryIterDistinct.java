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

package org.apache.jena.sparql.engine.iterator ;

import java.util.* ;

import org.apache.jena.atlas.data.* ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingComparator ;
import org.apache.jena.sparql.engine.binding.BindingProjectNamed ;
import org.apache.jena.sparql.system.SerializationFactoryFinder;

/**
 * A QueryIterator that suppresses items already seen. This will stream results
 * until a threshold is passed. At that point, it will fill a disk-backed
 * {@link DistinctDataNet}, then yield   
 * not  return any results until the input iterator has been exhausted.
 * 
 * @see DistinctDataBag
 */
public class QueryIterDistinct extends QueryIter1
{
    private long memThreshold = Long.MAX_VALUE ;    // Default "off" value.
    /*package*/ DistinctDataBag<Binding> db = null ;
    private Iterator<Binding> iterator = null ;
    private Set<Binding> seen = new HashSet<>() ;
    private Binding slot = null ;
    private final  List<SortCondition> preserveOrder;

    public QueryIterDistinct(QueryIterator qIter, List<SortCondition> preserveOrder, ExecutionContext execCxt) {
        super(qIter, execCxt) ;
        this.preserveOrder = (preserveOrder!=null) ? preserveOrder : Collections.emptyList();
        if ( execCxt != null ) {
            memThreshold = execCxt.getContext().getLong(ARQ.spillToDiskThreshold, memThreshold) ;
            if ( memThreshold < 0 )
                throw new ARQException("Bad spillToDiskThreshold: "+memThreshold) ;
        }
    }
    
    @Override
    protected boolean hasNextBinding() {
        if ( slot != null )
            return true ;
        if ( iterator != null )
            // Databag active.
            return iterator.hasNext() ;
       
        // At this point, we are currently in the initial pre-threshold mode.
        if ( seen.size() < memThreshold ) {
            Binding b = getInputNextUnseen() ;
            if ( b == null )
                return false ;
            seen.add(b) ;
            slot = b ;
            return true ;
        }
        
        // Hit the threshold.
        loadDataBag() ;
        // Switch to iterating from the data bag.  
        iterator = db.iterator() ;
        // Leave slot null.
        return iterator.hasNext() ;
    }
    
    /**
     * Load the data bag with. Filter incoming by the already seen in-memory elements. 
     * 
     * For DISTINCT-ORDER, and if DISTINCT spills then we need to take
     * account of the ORDER. The normal (non-spill case) already preserves the input
     * order, passing through the first occurence. It is only if a spill happens that
     * we need to ensure the spill buckets respect sort order.
     */  
    private void loadDataBag() {
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(super.getExecContext().getContext()) ;
        Comparator<Binding> comparator = new BindingComparator(preserveOrder, super.getExecContext()) ;
        this.db = BagFactory.newDistinctBag(policy, SerializationFactoryFinder.bindingSerializationFactory(), comparator) ;
        for(;;) {
            Binding b = getInputNextUnseen() ;
            if ( b == null )
                break ;
            db.add(b) ;
        }
    }
    
    /** Return the next binding from the input filtered by seen.
     * This does not update seen.
     * Returns null on end of input.
    */
    private Binding getInputNextUnseen() {
        while( getInput().hasNext() ) {
            Binding b = getInputNext() ;
            if ( seen.contains(b) )
                continue ;
            return b ;
        }
        return null ;
    }

    /** Return the binding from the input, hiding any variables to be ignored. */
    private Binding getInputNext() {
        Binding b = getInput().next() ;
        // Hide unnamed and internal variables.
        b = new BindingProjectNamed(b) ;
        return b ;
    }

    @Override
    protected Binding moveToNextBinding() {
        if ( slot != null ) {
            Binding b = slot ;
            slot = null ;
            return b ;
        }
        if ( iterator != null ) {
            Binding b = iterator.next() ;
            return b ;
        }
        throw new InternalErrorException() ;
    }

    @Override
    protected void closeSubIterator() {
        if ( db != null ) {
            iterator = null ;
            db.close() ;
        }
        db = null ;
    }

    // We don't need to do anything. We're a QueryIter1
    // and that handles the cancellation of the wrapped
    // iterator.
    @Override
    protected void requestSubCancel()
    { }

}
