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

package com.hp.hpl.jena.sparql.engine.iterator ;

import java.util.ArrayList ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.openjena.atlas.data.BagFactory ;
import org.openjena.atlas.data.DistinctDataNet ;
import org.openjena.atlas.data.ThresholdPolicy ;
import org.openjena.atlas.data.ThresholdPolicyFactory ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.riot.SerializationFactoryFinder ;

import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;

/**
 * A QueryIterator that suppresses items already seen. This will stream results
 * until the spill to disk threshold is passed. At that point, it will not
 * return any results until the input iterator has been exhausted.
 * 
 * @see DistinctDataNet
 */
public class QueryIterDistinct extends QueryIter
{
    private final QueryIterator inputIterator ;
    final DistinctDataNet<Binding> db ;

    boolean initialized = false ;
    boolean finished = false ;
    Binding slot ;
    Iterator<Binding> dbIter ;

    public QueryIterDistinct(QueryIterator qIter, ExecutionContext context)
    {
        super(context) ;
        this.inputIterator = qIter ;

        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(context.getContext()) ;
        Comparator<Binding> comparator = new BindingComparator(new ArrayList<SortCondition>(), context) ;
        this.db = BagFactory.newDistinctNet(policy, SerializationFactoryFinder.bindingSerializationFactory(), comparator) ;
    }

    @Override
    public void requestCancel()
    {
        inputIterator.cancel() ;
    }
    
    private void init()
    {
        if ( !initialized )
        {
            fill() ;
            initialized = true ;
        }
    }

    private void fill()
    {
        while ( inputIterator.hasNext() )
        {
            slot = inputIterator.next() ;
            if ( db.netAdd(slot) )
            {
                return ;
            }
        }
        if ( null == dbIter )
        {
            dbIter = db.netIterator() ;
        }
        while ( dbIter.hasNext() )
        {
            slot = dbIter.next() ;
            return ;
        }
        close() ;
    }

    @Override
    protected boolean hasNextBinding()
    {
        init() ;
        return !finished ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        if ( finished )
            throw new NoSuchElementException() ;
        init() ;
        Binding toReturn = slot ;
        fill() ;
        return toReturn ;
    }

    @Override
    protected void closeIterator()
    {
        if ( inputIterator != null )
        {
            NiceIterator.close(inputIterator) ;
            // In case we wrapped, for example, another QueryIterator.
            Iter.close(inputIterator) ;
        }
        if ( dbIter != null )
        {
            Iter.close(dbIter) ;
        }
        finished = true ;
        slot = null ;
        dbIter = null ;
        db.close() ;
    }
}
