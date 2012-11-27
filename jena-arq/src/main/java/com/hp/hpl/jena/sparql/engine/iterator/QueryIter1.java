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
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;

/**
 * This class supports a QueryIter that takes one QueryIterator as input. */
public abstract class QueryIter1 extends QueryIter
{
    private QueryIterator input ; 
    
    public QueryIter1(QueryIterator input, ExecutionContext execCxt)
    { 
        super(execCxt) ;
        this.input = input ;
    }
    
    protected QueryIterator getInput() { return input ; }
    
    @Override
    protected final
    void closeIterator()
    {
        closeSubIterator() ;
        performClose(input) ;
        input = null ;
    }
    
    @Override
    protected final
    void requestCancel()
    {
        requestSubCancel() ;
        performRequestCancel(input) ;
    }
    
    /** Cancellation of the query execution is happening */
    protected abstract void requestSubCancel() ;
    
    /** Pass on the close method - no need to close the QueryIterator passed to the QueryIter1 constructor */
    protected abstract void closeSubIterator() ;
    
    // Do better
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        // Linear form.
        getInput().output(out, sCxt) ;
        out.ensureStartOfLine() ;
        details(out, sCxt) ;
        out.ensureStartOfLine() ;

//        details(out, sCxt) ;
//        out.ensureStartOfLine() ;
//        out.incIndent() ;
//        getInput().output(out, sCxt) ;
//        out.decIndent() ;
//        out.ensureStartOfLine() ;
    }

    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.println(Utils.className(this)) ;
    }

}
