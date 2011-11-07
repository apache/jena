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

import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Binary operation done by looping on the left, and materializing the right - this is not streamed on the right
 * See also QueryIterRepeatApply */
public abstract class QueryIter2LoopOnLeft extends QueryIter2
{
    Table tableRight ; 
    Binding slot = null ;
    
    public QueryIter2LoopOnLeft(QueryIterator left, QueryIterator right, ExecutionContext qCxt)
    {
        super(left, right, qCxt) ;
        
        // Materialized right.
        tableRight = TableFactory.create(getRight()) ;
        getRight().close();
    }

    @Override
    protected final void closeSubIterator()
    { tableRight.close(); }
    
    @Override
    protected void requestSubCancel()
    { tableRight.close() ; }
   
    @Override
    protected final boolean hasNextBinding()
    {
        if ( slot != null )
            return true ;
        
        while ( getLeft().hasNext() )
        {
            Binding bindingLeft = getLeft().nextBinding() ;
            slot = getNextSlot(bindingLeft) ;
            if ( slot != null )
            {
                slot = bindingLeft ; 
                return true ;
            }
        }
        getLeft().close() ;
        return false ;
    }

    protected abstract Binding getNextSlot(Binding bindingLeft) ;

    @Override
    protected final Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            return null ;
        Binding x = slot ;
        slot = null ;
        return x ;
    }
}
