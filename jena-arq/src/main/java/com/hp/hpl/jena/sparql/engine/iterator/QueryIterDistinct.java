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

import org.apache.jena.atlas.data.BagFactory ;
import org.apache.jena.atlas.data.DistinctDataNet ;
import org.apache.jena.atlas.data.ThresholdPolicy ;
import org.apache.jena.atlas.data.ThresholdPolicyFactory ;
import org.apache.jena.riot.system.SerializationFactoryFinder ;

import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator ;

/**
 * A QueryIterator that suppresses items already seen. This will stream results
 * until the spill to disk threshold is passed. At that point, it will not
 * return any results until the input iterator has been exhausted.
 * 
 * @see DistinctDataNet
 */
public class QueryIterDistinct extends QueryIterDistinctReduced
{
    final DistinctDataNet<Binding> db ;

    public QueryIterDistinct(QueryIterator qIter, ExecutionContext context)
    {
        super(qIter, context) ;
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(context.getContext()) ;
        Comparator<Binding> comparator = new BindingComparator(new ArrayList<SortCondition>(), context) ;
        this.db = BagFactory.newDistinctNet(policy, SerializationFactoryFinder.bindingSerializationFactory(), comparator) ;
    }
    
    @Override
    protected void closeSubIterator()
    { db.close() ; }

    @Override
    protected void requestSubCancel()
    { db.close() ; }

    @Override
    protected boolean isFreshSighting(Binding binding)
    {
        return db.netAdd(binding) ;
    }
}
