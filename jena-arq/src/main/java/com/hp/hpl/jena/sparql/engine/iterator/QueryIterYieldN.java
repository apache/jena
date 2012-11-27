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

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** A query iterator that yields the same thing N times. */

public class QueryIterYieldN extends QueryIter
{
    protected int limitYielded ;
    protected int countYielded = 0 ;
    protected Binding binding ;
    
    public QueryIterYieldN(int num, Binding b)
    {
        this(num, b, null) ;
    }
    
    public QueryIterYieldN(int num, Binding b, ExecutionContext context)
    {
        super(context) ;
        binding = b ;
        limitYielded = num ;
    }
    
    public Binding getBinding() { return binding ; }
    
    @Override
    protected boolean hasNextBinding()
    {
        return countYielded < limitYielded ;
    }
    
    @Override
    protected Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            // Try to get the class name as specific as possible for subclasses
            throw new NoSuchElementException(Utils.className(this)) ;
        countYielded++ ;
        return binding ;
    }

    @Override
    protected void closeIterator()
    {
        //binding = null ;
    }
    
    @Override
    protected void requestCancel()
    {
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.print("QueryIterYieldN: "+limitYielded+" of "+binding);
    }

}
