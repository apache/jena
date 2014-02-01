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

import java.util.Map;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;

/**
 * This class provides the general machinary for iterators. */
public abstract class QueryIter extends QueryIteratorBase
{
    // Volatile just to make it safe to concurrent updates
    // It does not matter too much if it is wrong - it's used as a label.
    volatile static int iteratorCounter = 0 ;
    private int iteratorNumber = (iteratorCounter++) ;
    
    private ExecutionContext tracker ;
    
    public QueryIter(ExecutionContext execCxt)
    { 
        tracker = execCxt ;
        register() ;
    }

    public static QueryIter makeTracked(QueryIterator qIter, ExecutionContext execCxt)
    {
        if ( qIter instanceof QueryIter )
            return (QueryIter)qIter ;
        return new QueryIterTracked(qIter, execCxt) ; 
    }

    public static QueryIter materialize(QueryIterator qIter, ExecutionContext execCxt)
    {
        return makeTracked(materialize(qIter), execCxt) ;
    }

    public static QueryIterator materialize(QueryIterator qIter)
    {
        return new QueryIteratorCopy(qIter) ;
    }
    
    public static QueryIterator map(QueryIterator qIter, Map<Var, Var> varMapping)
    {
        return new QueryIteratorMapped(qIter, varMapping);
    }
    
    @Override
    public final void close()
    {
        super.close() ;
        deregister() ;
    }
    
    public ExecutionContext getExecContext() { return tracker ; }
    
    public int getIteratorNumber() { return iteratorNumber ; }
    
    @Override
    public void output(IndentedWriter out)
    {
        output(out, null) ;
//        out.print(Plan.startMarker) ;
//        out.print(Utils.className(this)) ;
//        out.print(Plan.finishMarker) ;
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { out.println(getIteratorNumber()+"/"+debug()) ; }
    
    private void register()
    {
        if ( tracker != null )
            tracker.openIterator(this) ;
    }
    
    private void deregister()
    {
        if ( tracker != null )
            tracker.closedIterator(this) ;
    }
}
