/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.util.* ;
import EDU.oswego.cs.dl.util.concurrent.* ;

// To do:
//   Debugging version with no threading.

/** An execution of a query.
 *  The query is not modified so can be reused.  A new QueryEngine object
 *  should be created because the internal state after (and during) execution
 *  of a query is not defined.
 *
 *  This implementation executes the triple pattern generation on a one thread,
 *  executes the constraint filters on another, and leaving the application thread
 *  just to return results.
 *
 * @see Query
 * @see QueryResults
 * 
 * @author		Andy Seaborne
 * @version 	$Id: QueryEngine.java,v 1.2 2003-02-20 16:21:58 andy_seaborne Exp $
 */


public class QueryEngine implements QueryExecution
{
    Query query ;
    Set results ;

    static final int bufferCapacity = 5 ;
    Object endOfPipeMarker = new Object() ;
    volatile boolean queryStop = false ;
    boolean queryInitialised = false ;
    static int queryCount = 0 ;
    int idQueryExecution ;
    
    // Statistics
    long triplePatterns = 0 ;
    long queryStartTime = -1 ;

    public QueryEngine(Query q)
    {
        query = q ;
        queryStop = false ;
        idQueryExecution = (++queryCount) ;
    }

    /** Initialise a query execution.  May be called before exec.
     *  If it has not be called, the query engine will initialise
     *  itself during the exec() method.
     */

    public void init()
    {
        if ( queryInitialised ) return ;

        if ( query.getSource() == null )
        {
            if ( query.sourceURL == null )
            {
                query.getLog().warn("No data for query (no URL, no model)") ;
                throw new QueryException("No model for query") ;
            }
            long startTime = System.currentTimeMillis() ;
            query.setSource(com.hp.hpl.jena.util.ModelLoader.loadModel(query.sourceURL, null)) ;
            query.loadTime = System.currentTimeMillis() - startTime ;
        }

        queryInitialised = true ;
    }

    /** Execute a query and get back the results.
     * @return QueryResults
     */
    
    public QueryResults exec() { return exec(null) ; }

    /** Execute a query, passing in an initial binding of some of the variables in the query.
     * @param  startBinding Initial values of variables.
     * @return QueryResults
     */
    
    public QueryResults exec(ResultBinding startBinding)
    {
        init() ;

        //if ( startBinding == null )
        //    startBinding = new ResultBinding() ;

        // Pipeline from generators to constraints
        // These are final because they are passed into the anonymous classes.
        final BoundedBuffer pipe1 = new BoundedBuffer(bufferCapacity) ;
        // Pipeline from constraints to results iterator

        final BoundedBuffer pipe2 = query.constraints.isEmpty() ? pipe1 : new BoundedBuffer(bufferCapacity) ;

        final ResultBinding environment = startBinding ;
        
        queryStartTime = System.currentTimeMillis() ;
        // Triple patterns : create thread and start.
        new Thread("Triples-"+idQueryExecution) { public void run() { execTriples(pipe1, environment) ; } }.start() ;

        //query.constraints.isEmpty()
        if ( pipe2 != pipe1 )
            // If there are any tests to do, create thread and start.
            new Thread("Constraints-"+idQueryExecution) { public void run() { execConstraints(pipe1, pipe2) ; } }.start() ;
        else
            if ( query.loggingOn )
                query.getLog().debug("No constraint pipe stage");

        Iterator resultsIter = new ResultsIterator(pipe2) ;
        return new QueryResultsStream(query, this, resultsIter) ;
    }

    public void abort()
    {
        if ( ! queryStop )
            this.queryStop = true ;
    }

    /** Normal end of use of this execution  */
    public void close()
    {
        if ( ! queryStop )
            this.queryStop = true ;
    }
    

    private void execConstraints(BoundedBuffer pipe1, BoundedBuffer pipe2)
    {
        // This is very stupid because it takes no account of the
        // variables an expression actually uses.

        int inputCount = 0 ;
        int outputCount = 0 ;

        try {
        outerLoop:
            for ( ;; )
            {
                if ( queryStop )
                    break outerLoop;
                Object x = pipe1.take() ;
                if ( x == endOfPipeMarker )
                    break outerLoop;

                inputCount ++ ;

                ResultBinding env = (ResultBinding)x ;
                boolean passesTests = true ;
                for ( Iterator cIter = query.constraints.iterator() ; cIter.hasNext() ; )
                {
                    Constraint constraint = (Constraint)cIter.next() ;
                    if ( ! constraint.isSatified(query, env) )
                    {
                        passesTests = false ;
                        break ;
                    }
                }
                if ( passesTests )
                {
                    outputCount ++ ;
                    pipe2.put(env);
                }
            }

            pipe2.put(endOfPipeMarker);
            return ;
        } catch (InterruptedException e) { QSys.unhandledException(e, "QueryEngine", "execConstraints") ; }
    }

    private void execTriples(BoundedBuffer pipe, ResultBinding startBinding)
    {
        try {
            execTriplesWorker(pipe, startBinding, 0) ;
            pipe.put(endOfPipeMarker);
        } catch (InterruptedException e) { QSys.unhandledException(e, "QueryEngine", "execTriples"); }
    }


    private void execTriplesWorker(BoundedBuffer results, ResultBinding env, int index)
    {
        if ( query.loggingOn )
        {
            query.getLog().debug("QueryEngine.execTriplesWorker: "+
                                 "Triple matching: "+(index+1)+" of "+query.triplePatterns.size()) ;
            query.getLog().debug("QueryEngine.execTriplesWorker: "+                                 "Triple matching: "+(env==null ? "<<null ResultBinding>>" : env.toString())) ;
        }

        if ( queryStop )
            return ;

        if ( index > query.triplePatterns.size()-1 )
        {
            try {
            	if ( env != null )
	                results.put(env);
            } catch (InterruptedException e) { QSys.unhandledException(e, "QueryEngine", "execTriplesWorker") ; }
            return ;
        }

        triplePatterns ++ ;
        int matchesFound = 0 ;

        TriplePattern tp = (TriplePattern)query.triplePatterns.get(index) ;

        // Would like stop the triple matcher mid-flow as well.
        // Null return means nothing will match (unusual occurence but possible).
        Iterator iter = tp.match((query.loggingOn ? query.log : null), this, query.source, env ) ;

        if ( iter != null )
            for ( ; iter.hasNext() ; )
            {
                ResultBinding rb2 = (ResultBinding)iter.next();
                if ( query.loggingOn && query.log != null )
                    query.log.debug("Env: "+rb2) ;
                if ( rb2 == null )
                    continue ;
                matchesFound++ ;
                execTriplesWorker(results, rb2, index+1) ;
            }
    }

    class ResultsIterator implements Iterator
    {
        BoundedBuffer pipe ;
        Object nextThing ;

        ResultsIterator(BoundedBuffer p) { pipe = p ; nextThing = null ; }

        public boolean hasNext()
        {
            try {
                if ( queryStop )
                    return false ;

                // Implements "blocking poll"
                if ( nextThing == null )
                    nextThing = pipe.take() ;

                boolean isMore = ( nextThing != endOfPipeMarker ) ;
                if ( query.executeTime == -1 )
                    query.executeTime = System.currentTimeMillis() - queryStartTime ;

                return isMore ;
            } catch (InterruptedException e) { QSys.unhandledException(e, "ResultsIterator", "hasNext") ; }
            return false ;
        }

        public Object next()
        {
            //if ( query.logging )
            if ( ! hasNext() )
                return null ;
            Object x = nextThing ;
            nextThing = null ;
            return x ;
        }

        public void remove() { throw new java.lang.UnsupportedOperationException("ResultsIterator.remove") ; }
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
