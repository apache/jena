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

package com.hp.hpl.jena.sparql.engine;

import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.lib.Closeable ;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.ResultBinding ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;


/** The main ResultSet implementation for returning results from queries.
 * This version is "use once" - you can not reset the result set because
 * the results of the query are not remembered so as not to consume potentially
 * large amounts of memory. */

public class ResultSetStream implements ResultSet
{
    // Could use QueryIteratorWrapper 
    private Iterator<Binding> queryExecutionIter ;
    private List<String> resultVars ;
    private QuerySolution currentQuerySolution ;
    private int rowNumber ;
    private Model model ;
    
    public ResultSetStream(List<String> resultVars, Model m, Iterator<Binding> iter)
    {
        queryExecutionIter = iter ;
        this.resultVars = resultVars ;
        currentQuerySolution = null ;
        rowNumber = 0 ;
        model = m ;
    }
    
    /**
     *  @throws UnsupportedOperationException Always thrown.
     */

    @Override
    public void remove() throws java.lang.UnsupportedOperationException
    {
        throw new UnsupportedOperationException(this.getClass().getName()+".remove") ;
    }

    /**
     * Is there another possibility?
     */
    @Override
    public boolean hasNext()
    {
        if ( queryExecutionIter == null )
            return false ;
        boolean r = queryExecutionIter.hasNext() ;
        if ( !r )
            close() ;
        return r;
    }

    @Override
    public Binding nextBinding()
    {
        if (queryExecutionIter == null) throw new NoSuchElementException(this.getClass() + ".next") ;

        try {
            Binding binding = queryExecutionIter.next() ;
            if (binding != null) rowNumber++ ;
            return binding ;
        } catch (NoSuchElementException ex) {
            close() ;
            throw ex ;
        }
    }
    
    private void close()
    {
        // ARQ QueryIterators are org.apache.jena.atlas.lib.Closable.
        if ( queryExecutionIter instanceof Closeable )
            ((Closeable)queryExecutionIter).close() ;
    }

    /** Moves onto the next result possibility.
     *  The returned object is actual the binding for this
     *  result.
     */
    @Override
    public QuerySolution nextSolution()
    {
        if ( queryExecutionIter == null )
//            ||  
//             ( queryExecution != null && ! queryExecution.isActive() ) )
            throw new NoSuchElementException(this.getClass()+".next") ;
        
        Binding binding = nextBinding() ;
        currentQuerySolution = new ResultBinding(model, binding) ;
        return currentQuerySolution ;
    }

    
    /** Moves onto the next result possibility.*/
    
    @Override
    public QuerySolution next() { return nextSolution() ; }
    
    /** Return the "row number" - a count of the number of possibilities returned so far.
     *  Remains valid (as the total number of possibilities) after the iterator ends.
     */

    @Override
    public int getRowNumber()
    {
        return rowNumber ;
    }

    /** Get the variable names for the projection
     */

    @Override
    public List<String> getResultVars() { return resultVars ; }
    
    public Model getModel() { return model ; }
    
    @Override
    public Model getResourceModel() { return model ; }

}
