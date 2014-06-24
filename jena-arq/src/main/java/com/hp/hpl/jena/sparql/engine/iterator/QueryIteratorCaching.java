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

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;

/** A caching QueryIterator.  On demand, the application can ask for a new
 *  query iterator which will repeat the bindings yielded so far.
 */  

public 
class QueryIteratorCaching extends QueryIteratorWrapper
{
    // Not tracked.
    List<Binding> cache = new ArrayList<>() ;
    
    public QueryIteratorCaching(QueryIterator qIter)
    {
        super(qIter) ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        Binding b = super.moveToNextBinding() ;
        cache.add(b) ;
        return b ;
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {}
    
    
    public QueryIteratorCaching createRepeat()
    {
        List<Binding> elements = cache ;
        if ( super.hasNext() )
            // If the iterator isn't finished, copy what we have so far.
            elements = new ArrayList<>(cache) ;
        
        return new QueryIteratorCaching(new QueryIterPlainWrapper(elements.iterator(), null)) ;
    }
    
    public static QueryIterator reset(QueryIterator qIter)
    {
        if ( qIter instanceof QueryIteratorCaching )
        {
            QueryIteratorCaching cIter = (QueryIteratorCaching)qIter ;
            return cIter.createRepeat() ;
        }
            
        return qIter ;
    }
}
