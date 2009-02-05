/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import java.util.*;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


/** The main ResultSet implementation for returning results from queries.
 * This version is "use once" - you can not reset the result set because
 * the results of the query are not remembered so as not to consume potentially
 * large amounts of memory.
 * 
 * @author   Andy Seaborne
 */

public class ResultSetStream implements ResultSet
{
    // Could use QueryIteratorWrapper 
    private QueryIterator queryExecutionIter ;
    private List<String> resultVars ;
    private QuerySolution currentQuerySolution ;
    private int rowNumber ;
    private Model model ;
    
    private boolean ordered = false ; 
    private boolean distinct = false ;
    
    public ResultSetStream(List<String> resultVars, Model m, QueryIterator iter)
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

    public void remove() throws java.lang.UnsupportedOperationException
    {
        throw new UnsupportedOperationException(this.getClass().getName()+".remove") ;
    }

    /**
     * Is there another possibility?
     */
    public boolean hasNext()
    {
        if ( queryExecutionIter == null )
            return false ;
        boolean r = queryExecutionIter.hasNext() ;
        return r;
    }

    public Binding nextBinding()
    {
        if ( queryExecutionIter == null )
//          ||  
//           ( queryExecution != null && ! queryExecution.isActive() ) )
          throw new NoSuchElementException(this.getClass()+".next") ;
      
      Binding binding = queryExecutionIter.nextBinding() ;
      if ( binding != null )
          rowNumber++ ;
      return binding ;
    }
    
    /** Moves onto the next result possibility.
     *  The returned object is actual the binding for this
     *  result.
     */
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
    
    public QuerySolution next() { return nextSolution() ; }
    
    /** Return the "row number" - a count of the number of possibilities returned so far.
     *  Remains valid (as the total number of possibilities) after the iterator ends.
     */

    public int getRowNumber()
    {
        return rowNumber ;
    }

    /** Get the variable names for the projection
     */

    public List<String> getResultVars() { return resultVars ; }
    
    public Model getModel() { return model ; }
    
    public Model getResourceModel() { return model ; }

}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
