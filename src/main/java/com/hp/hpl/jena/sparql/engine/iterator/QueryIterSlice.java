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

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Iterator until a limit is reached. */

public class QueryIterSlice extends QueryIter1
{
    long count = 0 ;
    long limit ;
    long offset ;
    
    /** Create an iterator that limits the number of returns of
     * another CloseableIterator.
     * 
     * @param cIter            The closable iterator to throttle 
     * @param startPosition    Offset of start after - 0 is the no-op.
     * @param numItems         Maximium number of items to yield.  
     */
    
    public QueryIterSlice(QueryIterator cIter, long startPosition, long numItems, ExecutionContext context)
    {
        super(cIter, context) ;
        
        offset = startPosition ;
        if ( offset == Query.NOLIMIT )
            offset = 0 ;
        
        limit = numItems ;
        if ( limit == Query.NOLIMIT )
            limit = Long.MAX_VALUE ;

        if ( limit < 0 )
            throw new QueryExecException("Negative LIMIT: "+limit) ;
        if ( offset < 0 )
            throw new QueryExecException("Negative OFFSET: "+offset) ;
        
        count = 0 ;
        // Offset counts from 0 (the no op).
        for ( int i = 0 ; i < offset ; i++ )
        {
            // Not subtle
            if ( !cIter.hasNext() ) { close() ; break ; }
            cIter.next() ;
        }
    }
    
    @Override
    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false;
        
        if ( ! getInput().hasNext() )
            return false ;
        
        if ( count >= limit )
            return false ;

        return true ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        count ++ ;
        return getInput().nextBinding() ;
    }

    @Override
    protected void closeSubIterator() {}
    
    @Override
    protected void requestSubCancel() {}
}
