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

import java.util.Iterator ;

import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Diff by materializing the RHS - this is not streamed on the right */
public class QueryIterDiff extends QueryIter2LoopOnLeft
{
    public QueryIterDiff(QueryIterator left, QueryIterator right, ExecutionContext qCxt)
    {
        super(left, right, qCxt) ;
    }

    @Override
    protected Binding getNextSlot(Binding bindingLeft)
    {
        boolean accept = true ;

        for ( Iterator<Binding> iter = tableRight.iterator(null) ; iter.hasNext() ; )
        {
            Binding bindingRight = iter.next() ;
            if ( Algebra.compatible(bindingLeft, bindingRight) )
            {
                accept = false ;
                break ;
            }
        }

        if ( accept )
            return bindingLeft ;
        return null ;
    }
}
