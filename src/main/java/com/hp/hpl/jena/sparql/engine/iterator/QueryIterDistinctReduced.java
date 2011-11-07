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

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Implementation skeleton for DISTINCT and REDUCED. */

public abstract class QueryIterDistinctReduced extends QueryIter1
{
    Binding slot = null ;       // ready to go.
    
    public QueryIterDistinctReduced(QueryIterator iter, ExecutionContext context)
    { super(iter, context)  ; }

    // Subclasses will want to implement this as well. 
    @Override
    protected void closeSubIterator()
    { slot = null ; }

    // Subclasses will want to implement this as well. 
    @Override
    protected void requestSubCancel()
    { }
    
    @Override
    final
    protected boolean hasNextBinding()
    {
        // Already waiting to go.
        if ( slot != null )
            return true ;
        
        // Always moves.
        for ( ; getInput().hasNext() ; )
        {
            Binding b = getInput().nextBinding() ;
            if ( ! isDuplicate(b) )
            {
                // new - remember and return
                remember(b) ;
                slot = b ;
                return true ;
            }
        }
        return false ;
    }

    @Override
    final
    protected Binding moveToNextBinding()
    {
        Binding r = slot ;
        slot = null ;
        return r ;
    }
    
    protected abstract boolean isDuplicate(Binding binding) ;
    
    protected abstract void remember(Binding binding) ;
}
