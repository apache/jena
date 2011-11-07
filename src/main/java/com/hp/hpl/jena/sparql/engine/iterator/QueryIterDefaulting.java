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

import java.util.NoSuchElementException ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** An iterator that returns at least one element from another iterator
 *  or a default value (once) if the wrapped iterator returns nothing. */ 

public class QueryIterDefaulting extends QueryIterSub
{
    Binding defaultObject ;
    
    boolean returnDefaultObject = false ;
    boolean haveReturnedSomeObject = false ; 

    public QueryIterDefaulting(QueryIterator cIter, Binding _defaultObject, ExecutionContext qCxt) 
    {
        super(cIter, qCxt) ;
        defaultObject = _defaultObject ;
    }

    /** Returns true if the returned binding was the default object. Undef if before the iterator's first .hasNext() */
    public boolean wasDefaultObject()
    { return returnDefaultObject ; }
    
    @Override
    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false ;

        if ( iter != null && iter.hasNext() )
            return true ;
        
        // Wrapped iterator has ended (or does not exist).  Have we returned anything yet? 
        
        if ( haveReturnedSomeObject )
            return false ;
        
        returnDefaultObject = true ;
        return true ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        if ( isFinished() )
            throw new NoSuchElementException(Utils.className(this)) ;
        
        if ( returnDefaultObject )
        {
            haveReturnedSomeObject = true ;
            return defaultObject ;
        }

        Binding binding = null ;
        if ( iter != null && iter.hasNext() )
            binding = iter.next() ;
        else
        {
            if ( haveReturnedSomeObject )
                throw new NoSuchElementException("DefaultingIterator - without hasNext call first") ;
            binding = defaultObject ;
        }
        
        haveReturnedSomeObject = true ;
        return binding ;
    }

    @Override
    protected void requestSubCancel()
    {}

    @Override
    protected void closeSubIterator()
    {}
}
