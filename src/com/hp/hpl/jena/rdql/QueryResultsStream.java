/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.util.*;

/** The main QueryResults implementation for returning results from queries.
 * This version is "use once" - you can not reset the result set because
 * the resulys of the query are not remembered so as not to consume potentially
 * large amounts of memory.
 * 
 * @see Query
 * @see QueryEngine
 * @see ResultBinding
 * @see QueryResultsStream
 * 
 * @author   Andy Seaborne
 * @version  $Id: QueryResultsStream.java,v 1.4 2003-04-08 22:12:03 ian_dickinson Exp $
 */

public class QueryResultsStream implements QueryResults
{
    Iterator queryExecutionIter ;
    QueryExecution queryExecution ;
    List resultVars ;
    ResultBinding currentEnv ;
    int rowNumber ;
    volatile boolean finished = false ;

    
    public QueryResultsStream(Query query, QueryExecution qe, Iterator iter)
    {
        queryExecutionIter = iter ;
        queryExecution = qe ;
        // Maybe we should take a copy.
        resultVars = query.getResultVars() ;
        currentEnv = null ;
        rowNumber = 0 ;
    }

    /**
     *  @throws UnsupportedOperationException Always thrown.
     */

    public void remove() throws java.lang.UnsupportedOperationException
    {
        throw new java.lang.UnsupportedOperationException("com.hp.hpl.jena.rdf.query.QueryResults.remove") ;
    }

    /**
     * Is there another possibility?
     */
    public boolean hasNext()
    {
        return queryExecutionIter.hasNext() ;
    }

    /** Moves onto the next result possibility.
     *  The returned object is actual the binding for this
     *  result; it is possible to access the bound variables
     *  for the current possibility through the additional variable
     *  accessor opertations.
     */
    public Object next()
    {
        currentEnv = (ResultBinding)queryExecutionIter.next() ;
        if ( currentEnv != null )
            rowNumber++ ;
        return currentEnv ;
    }

    /** Close the results iterator and stop query evaluation as soon as convenient.
     */

    public void close()
    {
        if ( ! finished )
        {
            queryExecution.abort() ;
            finished = true ;
        }
        else
            queryExecution.close() ;
    }

    /** Access a binding (a mapping from variable name to value).  RDF does not explicitly type values so we only provide a string
     *  form and leave it to the application context to interpret as an integer, date etc.
     */

    public String getBinding(String name)
    {
        // Coudl mask with the result variables list.  But we don't.
        return getBindingWorker(name, false) ;
    }

    /** Return the "row number" - a count of the number of possibilities returned so far.
     *  Remains valid (as the total number of possibilities) after the iterator ends.
     */

    public int getRowNumber()
    {
        return rowNumber ;
    }

    /** Get the variable names for the projection
     */

    public List getResultVars() { return resultVars ; }

    /** Convenience function to consume a query.
     *  Returns a list of {@link ResultBinding}s.
     *
     *  @return List
     *  @deprecated  QueryResultsStream do not have all the results at once - {@link QueryResultsMem}
     */

    public List getAll()
    {
        List all = new ArrayList() ;
        while(this.hasNext())
        {
            all.add(next());
        }
		close() ;
        return all ;
    }

    private String getBindingWorker(String name, boolean projectResultVars)
    {
        if ( ! projectResultVars || resultVars.contains(name) )
        {
            Value v = currentEnv.getValue(name) ;
            if ( v == null ) return null ;
            return v.toString() ;
        }
        return null ;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
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
