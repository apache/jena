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

package org.apache.jena.sparql.mgt;

import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.algebra.Op ;

public class QueryEngineInfo implements QueryEngineInfoMBean
{
    // Has to be careful about concurrency.
    // It is possible that the count may be momentarily wrong
    // (reading longs is not atomic).
    
    private AtomicLong count = new AtomicLong(0) ;
    @Override
    public long getQueryCount()                 { return count.get() ; }
    public void incQueryCount()                 { count.incrementAndGet() ; }
    
    Query query = null ;
    @Override
    public String getLastQueryString()
    { 
        Query q = query ;    // Get once.
        if ( q != null ) return q.toString() ;
        // Sometimes an alegra expression is executited without a query.
        return getLastAlgebra() ;
    }
    public void setLastQueryString(Query q)     { query = q ; }

    private Op op = null ;
    @Override
    public String getLastAlgebra()
    {
        Op _op = op ;   // Get once.
        return _op == null ? "none" : _op.toString() ;
    }
    public void setLastOp(Op op)                { this.op = op ; }

    private String timeSeen = "" ;
    @Override
    public String getLastQueryExecAt()          { return timeSeen ; }
    public void setLastQueryExecAt()            { timeSeen = DateTimeUtils.nowAsString() ; }

//    private long lastExecTime ;
//    public long getLastQueryExecTime()          { return lastExecTime ; }
//    public void setLastQueryExecTime(long timeMillis)   { lastExecTime = timeMillis ; }
    
    
}
