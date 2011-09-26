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

package com.hp.hpl.jena.sparql.lang;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Stack ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.modify.request.QuadAcc ;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** Class that has all the parse event operations and other query/update specific things */  
public class ParserQueryBase extends ParserBase 
{
    private Stack<Query> stack = new Stack<Query>() ;
    protected Query query ;

    public void setQuery(Query q)
    { 
        query = q ;
        setPrologue(q) ;
    }

    public Query getQuery() { return query ; }

    // The ARQ parser is both query and update languages.

//    // ---- SPARQL/Update (Submission)
//    private UpdateRequest requestSubmission = null ;
//
//    protected UpdateRequest getUpdateRequestSubmission() { return requestSubmission ; }
//    public void setUpdateRequest(UpdateRequest request)
//    {
//        setPrologue(request) ;
//        this.requestSubmission = request ;
//        // And create a query because we may have nested selects.
//        this.query = new Query () ;
//    }

    // SPARQL Update (W3C RECommendation)
    private UpdateRequest request = null ;

    protected UpdateRequest getUpdateRequest() { return request ; }
    public void setUpdateRequest(UpdateRequest request)
    { 
        this.request = request ;
        setPrologue(request) ;
    }
    
    // Move down to SPARQL 1.1 or rename as ParserBase
    protected void startQuery() {}
    protected void finishQuery() {}

    // Move down to SPARQL 1.1 or rename as ParserBase
    protected void startUpdateOperation() {}
    protected void finishUpdateOperation() {}
    
    protected void startUpdateRequest() {}
    protected void finishUpdateRequest() {}
    
    private boolean oldBNodesAreVariables ;
    protected void startDataInsert(QuadDataAcc qd, int line, int col) 
    {
        oldBNodesAreVariables = getBNodesAreVariables() ;
        setBNodesAreVariables(false) ;
    } 
    protected void finishDataInsert(QuadDataAcc qd, int line, int col)
    {
        setBNodesAreVariables(oldBNodesAreVariables) ;
    }
    
    private boolean oldBNodesAreAllowed ;
    
    protected void startDataDelete(QuadDataAcc qd,int line, int col)
    {
        oldBNodesAreAllowed = getBNodesAreAllowed() ;
        setBNodesAreAllowed(false) ;
    } 
    
    protected void finishDataDelete(QuadDataAcc qd, int line, int col)
    {
        setBNodesAreAllowed(oldBNodesAreAllowed) ;
    }
    
    protected void startInsertTemplate(QuadAcc qd, int line, int col)
    {
        oldBNodesAreVariables = getBNodesAreVariables() ;
        setBNodesAreVariables(false) ;
    }
    
    protected void finishInsertTemplate(QuadAcc qd, int line, int col)
    {
        setBNodesAreVariables(oldBNodesAreVariables) ;
    }
    
    protected void startDeleteTemplate(QuadAcc qd, int line, int col)
    {
        oldBNodesAreAllowed = getBNodesAreAllowed() ;
        setBNodesAreAllowed(false) ;
    }
    
    protected void finishDeleteTemplate(QuadAcc qd, int line, int col)
    {
        setBNodesAreAllowed(oldBNodesAreAllowed) ;
    }
    
    protected void emitUpdate(Update update)
    {
        request.add(update) ;
    }
    
    protected void startSubSelect(int line, int col)
    {
        // Query is null in an update.
        stack.push(query) ;
        Query subQuery = new Query(getPrologue()) ;
        query = subQuery ;
    }
    
    protected Query endSubSelect(int line, int column)
    {
        Query subQuery = query ;
        if ( ! subQuery.isSelectType() )
            throwParseException("Subquery not a SELECT query", line, column) ;
        query = stack.pop();
        return subQuery ;
    }
    
    private List<Var> variables = null ;
    private List<Binding> values = null ;
    private int currentColumn = -1 ;
    
    protected void startBinding(int line, int col)               
    { 
        variables = new ArrayList<Var>() ;
        values = new ArrayList<Binding>() ;
    }
    
    private BindingMap currentValueRow()                            { return (BindingMap)values.get(values.size()-1) ; }
    
    protected void emitBindingVariable(Var v, int line, int col)    { variables.add(v) ; }
    
    protected void startBindingValueRow(int line, int col)
    { 
        values.add(BindingFactory.create()) ;
        currentColumn = -1 ;
    }
    
    protected void emitBindingValue(Node n, int line, int col)      
    { 
        currentColumn++ ;
        
        if ( currentColumn >= variables.size() )
            // Exception will be thrown later when we have the complete row count.
            return ;
        
        Var v = variables.get(currentColumn) ;
        if ( n != null )
            currentValueRow().add(v, n) ;
        
    }

    protected void finishBindingValueRow(int line, int col)      
    {
        //if ( variables.size() != currentValueRow().size() )
        
        if ( currentColumn+1 != variables.size() )
        {
            String msg = String.format("Mismatch: %d variables but %d values",variables.size(), currentColumn+1) ;
            msg = QueryParseException.formatMessage(msg, line, col) ;
            throw new QueryParseException(msg, line , col) ;
        }
    }
    
    protected void finishBinding(int line, int col)
    {
        getQuery().setBindings(variables, values) ;
    }
}
