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

package org.apache.jena.sparql.engine.iterator;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.serializer.SerializationContext ;


/**
 * A query iterator that joins two or more iterators into a single iterator. */ 

public class QueryIterConcat extends QueryIter
{
    boolean initialized = false ;
    List<QueryIterator> iteratorList = new ArrayList<>() ;
    Iterator<QueryIterator> iterator ;
    QueryIterator currentQIter = null ;

    Binding binding ;
    boolean doneFirst = false ;

    public QueryIterConcat(ExecutionContext context)
    {
        super(context) ;
    }

    private void init()
    {
        if ( ! initialized )
        {
            currentQIter = null ;
            if ( iterator == null )
                iterator = iteratorList.listIterator() ;
            if ( iterator.hasNext() )
                currentQIter = iterator.next() ;
            initialized = true ;
        }
    }
    
    public void add(QueryIterator qIter)
    {
        if ( qIter != null )
            iteratorList.add(qIter) ; 
    }
    
    
    @Override
    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false ;

        init() ;
        if ( currentQIter == null )
            return false ;
        
        while ( ! currentQIter.hasNext() )
        {
            // End sub iterator
            //currentQIter.close() ;
            currentQIter = null ;
            if ( iterator.hasNext() )
                currentQIter = iterator.next() ;
            if ( currentQIter == null )
            {
                // No more.
                //close() ;
                return false ;
            }
        }
        
        return true ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            throw new NoSuchElementException(Lib.className(this)) ; 
        if ( currentQIter == null )
            throw new NoSuchElementException(Lib.className(this)) ; 
        
        Binding binding = currentQIter.nextBinding() ;
        return binding ;
    }

    
    @Override
    protected void closeIterator()
    {
        for ( QueryIterator qIter : iteratorList )
        {
            performClose( qIter );
        }
    }
    
    @Override
    protected void requestCancel()
    {
        for ( QueryIterator qIter : iteratorList )
        {
            performRequestCancel( qIter );
        }
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { 
        out.println(Lib.className(this)) ;
        out.incIndent() ;
        for ( QueryIterator qIter : iteratorList )
        {
            qIter.output( out, sCxt );
        }
        out.decIndent() ;
        out.ensureStartOfLine() ;
    }
}
