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

package com.hp.hpl.jena.sparql.lang;

import java.util.* ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.modify.request.QuadAcc ;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc ;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** Class that has all the parse event operations and other query/update specific things */  
public class SPARQLParserBase extends ParserBase 
{
    private Deque<Query> stack = new ArrayDeque<Query>() ;
    protected Query query ;
    
    protected SPARQLParserBase() {}

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

    // Places to push settings across points where we reset.
    private boolean oldBNodesAreVariables ;
    private boolean oldBNodesAreAllowed ;

    // Count of subSelect nesting.
    // Level 0 is top level.
    // Level -1 is not in a pattern WHERE clause.
    private int queryLevel = -1 ;
    private Deque<Set<String>>    stackPreviousLabels = new ArrayDeque<Set<String>>() ;
    private Deque<LabelToNodeMap> stackCurrentLabels = new ArrayDeque<LabelToNodeMap>() ;

    protected UpdateRequest getUpdateRequest() { return request ; }
    public void setUpdateRequest(UpdateRequest request)
    { 
        this.request = request ;
        this.query = new Query() ; 
        setPrologue(request) ;
    }

    // Signal start/finish of units
    
    protected void startQuery() {}
    protected void finishQuery() {}

    protected void startUpdateRequest()    {}
    protected void finishUpdateRequest()   {}
    
//    protected void startBasicGraphPattern()
//    { activeLabelMap.clear() ; }
//
//    protected void endBasicGraphPattern()
//    { oldLabels.addAll(activeLabelMap.getLabels()) ; }
    
    protected void startUpdateOperation()  {}
    protected void finishUpdateOperation() {}
    
    protected void startModifyUpdate()     { }
    protected void finishModifyUpdate()    { }
    
    protected void startDataInsert(QuadDataAcc qd, int line, int col) 
    {
        oldBNodesAreVariables = getBNodesAreVariables() ;
        setBNodesAreVariables(false) ;
        activeLabelMap.clear() ;
    } 
    
    protected void finishDataInsert(QuadDataAcc qd, int line, int col)
    {
        previousLabels.addAll(activeLabelMap.getLabels()) ;
        activeLabelMap.clear() ;
        setBNodesAreVariables(oldBNodesAreVariables) ;
    }
    
    protected void startDataDelete(QuadDataAcc qd,int line, int col)
    {
        oldBNodesAreAllowed = getBNodesAreAllowed() ;
        setBNodesAreAllowed(false) ;
    } 
    
    protected void finishDataDelete(QuadDataAcc qd, int line, int col)
    {
        setBNodesAreAllowed(oldBNodesAreAllowed) ;
    }

    // These can be nested with subSELECTs but subSELECTs share bNodeLabel state.
    protected void startWherePattern()
    {
        queryLevel += 1 ;
        if ( queryLevel == 0 )
        {
            pushLabelState() ;
            clearLabelState() ;
        }
    }
    
    protected void finishWherePattern()
    {
        if ( queryLevel == 0 )
            popLabelState() ;
        queryLevel -= 1 ;
    }

    // This holds the accumulation of labels from earlier INSERT DATA
    // across template creation (bNode in templates get cloned before
    // going into the data).

    protected void startInsertTemplate(QuadAcc qd, int line, int col)
    {
        oldBNodesAreVariables = getBNodesAreVariables() ;
        setBNodesAreVariables(false) ;
        pushLabelState() ;
    }

    protected void finishInsertTemplate(QuadAcc qd, int line, int col)
    {
        // Restore accumulated labels. 
        popLabelState() ;
        // This also set the bnode syntax to node functionality - must be after popLabelState. 
        setBNodesAreVariables(oldBNodesAreVariables) ;
    }
    
    // No bNodes in delete templates.
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
        if ( query == null )
            throw new ARQInternalErrorException("Parser query object is null") ;
        stack.push(query) ;
        query = new Query(getPrologue()) ;
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
    
    protected void startValuesClause(int line, int col)               
    { 
        variables = new ArrayList<Var>() ;
        values = new ArrayList<Binding>() ;
    }
    
    protected void finishValuesClause(int line, int col)
    {
        getQuery().setValuesDataBlock(variables, values) ;
    }
    
    protected void startInlineData(List<Var> vars, List<Binding> rows, int line, int col)
    {
        variables = vars ;
        values = rows ;
    }

    protected void finishInlineData(int line, int col)
    {}
    
    private BindingMap currentValueRow()                            { return (BindingMap)values.get(values.size()-1) ; }
    
    protected void emitDataBlockVariable(Var v)                     { variables.add(v) ; }
    
    protected void startDataBlockValueRow(int line, int col)
    { 
        values.add(BindingFactory.create()) ;
        currentColumn = -1 ;
    }
    
    protected void emitDataBlockValue(Node n, int line, int col)      
    { 
        currentColumn++ ;
        
        if ( currentColumn >= variables.size() )
            // Exception will be thrown later when we have the complete row count.
            return ;
        
        Var v = variables.get(currentColumn) ;
        if ( n != null )
            currentValueRow().add(v, n) ;
        
    }

    protected void finishDataBlockValueRow(int line, int col)      
    {
        //if ( variables.size() != currentValueRow().size() )
        
        if ( currentColumn+1 != variables.size() )
        {
            String msg = String.format("Mismatch: %d variables but %d values",variables.size(), currentColumn+1) ;
            msg = QueryParseException.formatMessage(msg, line, col) ;
            throw new QueryParseException(msg, line , col) ;
        }
    }

    private void pushLabelState()
    { 
        // Hide used labels already tracked.
        stackPreviousLabels.push(previousLabels) ;
        stackCurrentLabels.push(activeLabelMap) ;
        previousLabels = new HashSet<String>() ;
        activeLabelMap.clear() ;
    }

    private void popLabelState()
    {
        previousLabels = stackPreviousLabels.pop() ;
        activeLabelMap = stackCurrentLabels.pop();
    }

    private void clearLabelState()
    {
        activeLabelMap.clear() ;
        previousLabels.clear() ;
    }
}
