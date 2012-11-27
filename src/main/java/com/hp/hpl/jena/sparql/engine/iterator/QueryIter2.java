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

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;

/**
 * This class marks a QueryIter that takes two QueryIterators as input. */
public abstract class QueryIter2 extends QueryIter
{
    private QueryIterator leftInput ; 
    private QueryIterator rightInput ;
    
    public QueryIter2(QueryIterator left, QueryIterator right, ExecutionContext execCxt)
    { 
        super(execCxt) ;
        this.leftInput = left ;
        this.rightInput = right ;
    }
    
    protected QueryIterator getLeft()   { return leftInput ; } 
    protected QueryIterator getRight()  { return rightInput ; } 
    
    @Override
    protected final
    void closeIterator()
    {
        closeSubIterator() ;
        performClose(leftInput) ;
        performClose(rightInput) ;
        leftInput = null ;
        rightInput = null ;
    }
    
    @Override
    protected final
    void requestCancel()
    {
        performRequestCancel(leftInput) ;
        performRequestCancel(rightInput) ;
    }
    
    /** Cancellation of the query execution is happening */
    protected abstract void requestSubCancel() ;
    
    /** Pass on the close method - no need to close the left or right QueryIterators passed to the QueryIter1 constructor */
    protected abstract void closeSubIterator() ;
    
    // Do better
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { 
        out.println(Utils.className(this)) ;
        out.incIndent() ;
        
        out.print(Plan.startMarker) ;
        out.incIndent() ;
        getLeft().output(out, sCxt) ;
        out.decIndent() ;
        //out.ensureStartOfLine() ;
        out.println(Plan.finishMarker) ;
        
        out.print(Plan.startMarker) ;
        out.incIndent() ;
        getRight().output(out, sCxt) ;
        out.decIndent() ;
        //out.ensureStartOfLine() ;
        out.println(Plan.finishMarker) ;
        
        out.decIndent() ;
    }
}
