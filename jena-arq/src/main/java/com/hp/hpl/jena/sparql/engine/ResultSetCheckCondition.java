/**
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

package com.hp.hpl.jena.sparql.engine;

import java.util.List ;

import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** ResultSet wrapper that check whether some condition is true
 * (e.g. the QueryExecution has not been closed). 
 */
public class ResultSetCheckCondition implements ResultSet
{
    interface Condition { boolean check() ; }
    private final ResultSet other ;
    private final Condition condition ;

    public ResultSetCheckCondition(ResultSet other, QueryExecution qExec) {
        this(other, checkQExec(qExec) ) ;
    }

    public  ResultSetCheckCondition(ResultSet other, Condition condition) {
        this.other = other ;
        this.condition = condition ;
    }

    // Feel free to replace with a lambda expression for Java8!
    private static Condition checkQExec(final QueryExecution qExec) {
        return new Condition() {
            @Override
            public boolean check() { return ! qExec.isClosed() ; }  
        } ;
    }

    @Override
    public boolean hasNext() {
        check() ;
        return other.hasNext() ;
    }

    @Override
    public QuerySolution next() {
        check() ;
        return other.next() ;
    }

    @Override
    public void remove() {
        check() ;
        other.remove() ;
    }

    @Override
    public QuerySolution nextSolution() {
        check() ;
        return other.nextSolution() ;
    }

    @Override
    public Binding nextBinding() {
        check() ;
        return other.nextBinding() ;
    }

    @Override
    public int getRowNumber() {
        check() ;
        return other.getRowNumber() ;
    }

    @Override
    public List<String> getResultVars() {
        check() ;
        return other.getResultVars() ;
    }

    @Override
    public Model getResourceModel() {
        check() ;
        return other.getResourceModel() ;
    }
    
    private final void check() {
        if ( ! condition.check()  ) {
            throw new ARQException("ResultSet no longer valid (QueryExecution has been closed)") ;    
        }
    }


}
