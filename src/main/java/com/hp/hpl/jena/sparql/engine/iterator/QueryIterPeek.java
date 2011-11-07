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

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

public class QueryIterPeek extends QueryIter1
{
    private Binding binding = null ; 
    private boolean closed = false ;
    
    public static QueryIterPeek create(QueryIterator iterator, ExecutionContext cxt)
    {
        if ( iterator instanceof QueryIterPeek)
            return (QueryIterPeek)iterator ;
        return new QueryIterPeek(iterator, cxt) ;
    }
    
    private QueryIterPeek(QueryIterator iterator, ExecutionContext cxt)
    {
        super(iterator, cxt) ;
    }

    /** Returns the next binding without moving on.  Returns "null" for no such element. */
    public Binding peek() 
    {
        if ( closed ) return null ;
        if ( ! hasNextBinding() )
            return null ;
        return binding ;
    }

    @Override
    protected boolean hasNextBinding()
    {
        if ( binding != null )
            return true ;
        if ( ! getInput().hasNext() )
            return false ;
        binding = getInput().nextBinding() ;
        return true ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            throw new ARQInternalErrorException("No next binding") ;
        Binding b = binding ;
        binding = null ;
        return b ;
    }

    @Override
    protected void closeSubIterator()
    { closed = true ; }
    
    @Override
    protected void requestSubCancel()
    { }
}
