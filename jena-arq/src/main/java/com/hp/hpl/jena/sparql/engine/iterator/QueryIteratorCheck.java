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

import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Query iterator that checks everything was closed correctly */

public class QueryIteratorCheck extends QueryIteratorWrapper
{
    private ExecutionContext execCxt ;
    
    private QueryIteratorCheck(QueryIterator qIter, ExecutionContext execCxt)
    {
        super(qIter) ;
        if ( qIter instanceof QueryIteratorCheck )
            Log.warn(this, "Checking checked iterator") ;
        
        this.execCxt = execCxt ;
        
    }
    @Override
    public void close()
    {
        super.close() ;
        checkForOpenIterators(execCxt) ;
    }
    
    // Be silent about ourselves.
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { iterator.output(out, sCxt) ; }
    
    public static void checkForOpenIterators(ExecutionContext execContext)
    { dump(execContext, false); }
    
    public static QueryIteratorCheck check(QueryIterator qIter, ExecutionContext execCxt)
    {
        if ( qIter instanceof QueryIteratorCheck )
            return (QueryIteratorCheck)qIter ;
        return new QueryIteratorCheck(qIter, execCxt) ;
    }
    
    private static void dump(ExecutionContext execContext, boolean includeAll)
    {
        if ( includeAll )
        {
            Iterator<QueryIterator> iterAll = execContext.listAllIterators() ;

            if ( iterAll != null )
                while(iterAll.hasNext())
                {
                    QueryIterator qIter = iterAll.next() ;
                    warn(qIter, "Iterator: ") ;
                }
        }

        Iterator<QueryIterator> iterOpen = execContext.listOpenIterators() ;
        while(iterOpen.hasNext())
        {
            QueryIterator qIterOpen = iterOpen.next() ;
            warn(qIterOpen, "Open iterator: ") ;
            iterOpen.remove() ;
        }
    }

    private static void warn(QueryIterator qIter, String str)
    {
        str = str + Utils.className(qIter) ;

        if ( qIter instanceof QueryIteratorBase )
        {
            QueryIteratorBase qIterBase = (QueryIteratorBase)qIter ;
            {
                QueryIter qIterLN = (QueryIter)qIter ;
                str = str+"/"+qIterLN.getIteratorNumber() ;
            }
            String x = qIterBase.debug() ;
            if ( x.length() > 0 )
                str = str+" : "+x ;
        }
        Log.warn(QueryIteratorCheck.class, str) ;
    }
}
