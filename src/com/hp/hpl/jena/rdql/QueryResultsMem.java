/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.util.*;
import java.io.* ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.vocabulary.* ;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.util.*;

/**
 * @author      Andy Seaborne
 * @version     $Id: QueryResultsMem.java,v 1.9 2003-07-18 12:50:49 chris-dollin Exp $
 */


public class QueryResultsMem implements QueryResultsRewindable
{
    static final boolean DEBUG = false;
    // The result set in memory
    List rows = new ArrayList();
    List varNames = null ;
    int rowNumber = 0 ;
    Iterator iterator = null ;

    /** Create an in-memory result set from another one
     *
     * @param imrs2     The other QueryResultsMem object
     */

    public QueryResultsMem(QueryResultsMem imrs2)
    {
        this(imrs2, false) ;
    }

    /** Create an in-memory result set from another one
     *
     * @param imrs2     The other QueryResultsMem object
     * @param takeCopy  Should we copy the rows?
     */

    public QueryResultsMem(QueryResultsMem imrs2, boolean takeCopy)
    {
        varNames = imrs2.varNames;
        if ( takeCopy )
        {
            for (Iterator iter = imrs2.rows.iterator(); iter.hasNext();)
            {
                rows.add((ResultBinding) iter.next());
            }
        }
        else
            // Share results (not the iterator).
            rows = imrs2.rows ;
        reset() ;
    }

    /** Create an in-memory result set from any QueryResults object.
     *  If the QueryResults is an in-memory one already, then no
     *  copying is done - the necessary internal datastructures
     *  are shared.  This operation destroys (uses up) a QueryResults
     *  object that is not an in memory one.
     */

    public QueryResultsMem(QueryResults qr)
    {
        if (qr instanceof QueryResultsMem)
        {
            QueryResultsMem qrm = (QueryResultsMem) qr;
            this.rows = qrm.rows;
            this.varNames = qrm.varNames;
        }
        else
        {
            varNames = qr.getResultVars();
            while (qr.hasNext())
            {
                ResultBinding rb = (ResultBinding) qr.next();
                rows.add(rb);
            }
            qr.close();
        }
        reset();
    }

    /** Prcoess a result set encoded in RDF according to
     * <code>http://jena.hpl.hp.com/2003/03/result-set#</code>
     *
     * @param model
     */
    public QueryResultsMem(Model model)
    {
        buildFromDumpFormat(model);
    }

    /** Read in a result set encoded in RDF according to
     * <code>http://jena.hpl.hp.com/2003/03/result-set#</code>
     *
     * @param urlStr  URL string
     */

    public QueryResultsMem(String urlStr)
        throws java.io.FileNotFoundException
    {
        Model m = ModelLoader.loadModel(urlStr) ;
        buildFromDumpFormat(m);
    }


   // -------- QueryResults interface ------------------------------
   /**
     *  @throws UnsupportedOperationException Always thrown.
     */

    public void remove() throws java.lang.UnsupportedOperationException
    {
        throw new java.lang.UnsupportedOperationException(
            "QueryResultsMem: Attempt to remove an element");
    }

    /**
     * Is there another possibility?
     */
    public boolean hasNext() { return iterator.hasNext() ; }

    /** Moves onto the next result possibility.
     *  The returned object should be of class ResultBinding
     */

    public Object next() { rowNumber++ ; return iterator.next() ; }

    /** Close the results set.
     *  Should be called on all QueryResults objects
     */

    public void close()
    {
        // Free early - may be large
        rows = null ;
        iterator = null ;
        varNames = null ;
        return ;
    }

    public void rewind( ) { reset() ; }

    public void reset() { iterator = rows.iterator() ; rowNumber = 0 ; }

    /** Return the "row" number for the current iterator item
     */
    public int getRowNumber() { return rowNumber ; }

    /** Return the number of rows
     */
    public int size() { return rows.size() ; }

    /** Get the variable names for the projection
     */
    public List getResultVars() { return varNames ; }

    /** Convenience function to consume a query.
     *  Returns a list of {@link ResultBinding}s.
     *
     *  @return List
     *  @deprecated   Old QueryResults operation
     */

    public List getAll() { return rows ; }

    // -------- End QueryResults interface ------------------------------

    // Convert from RDF model to in-memory result set

    private void buildFromDumpFormat(Model resultsModel)
    {
        varNames = new ArrayList() ;
        StmtIterator sIter = resultsModel.listStatements(null, RDF.type, ResultSet.ResultSet) ;
        for ( ; sIter.hasNext() ;)
        {
            Statement s = sIter.nextStatement() ;
            Resource root = s.getSubject() ;

            // Variables
            StmtIterator rVarsIter = root.listProperties(ResultSet.resultVariable) ;
            for ( ; rVarsIter.hasNext() ; )
            {
                String varName = rVarsIter.nextStatement().getString() ;
                varNames.add(varName) ;
            }
            rVarsIter.close() ;
            // Now the results themselves
            int count = 0 ;
            StmtIterator solnIter = root.listProperties(ResultSet.solution) ;
            for ( ; solnIter.hasNext() ; )
            {
                // foreach row
                ResultBinding rb = new ResultBinding() ;
                count++ ;

                Resource soln = solnIter.nextStatement().getResource() ;
                StmtIterator bindingIter = soln.listProperties(ResultSet.binding) ;
                for ( ; bindingIter.hasNext() ; )
                {
                    Resource binding = bindingIter.nextStatement().getResource() ;
                    String var = binding.getRequiredProperty(ResultSet.variable).getString() ;
                    RDFNode val = binding.getRequiredProperty(ResultSet.value).getObject() ;
                    // We include the value even if it is the marker term "rs:undefined"
                    //if ( val.equals(ResultSet.undefined))
                    //    continue ;
                    // The QueryResultFormatter code equates null (not found) with
                    // rs:undefined.  When Jena JUnit testing, it does not matter if the
                    // recorded result has the term absent or explicitly undefined.

                    rb.add(var, val) ;
                }
                bindingIter.close() ;
                rows.add(rb) ;
            }
            solnIter.close() ;

            if ( root.hasProperty(ResultSet.size))
            {
                try {
                    int size = root.getRequiredProperty(ResultSet.size).getInt() ;
                    if ( size != count )
                        System.err.println("Warning: Declared size = "+size+" : Count = "+count) ;
                } catch (JenaException rdfEx) {}
            }
            sIter.close() ;
        }

        reset() ;
    }

    /** Are two result sets the same (isomorphic)?
     *
     * @param irs1
     * @param irs2
     * @return boolean
     */

    static public boolean equivalent(
        QueryResultsMem irs1,
        QueryResultsMem irs2)
    {
        QueryResultsFormatter fmt1 = new QueryResultsFormatter(irs1) ;
        Model model1 = fmt1.toModel() ;

        QueryResultsFormatter fmt2 = new QueryResultsFormatter(irs2) ;
        Model model2 = fmt2.toModel() ;

        return model1.isIsomorphicWith(model2) ;
    }

    /** Encode the result set as RDF.
     * @return Model       Model contains the results
     */


    public Model toModel()
    {
        Model m = ModelFactory.createDefaultModel() ;
        asRDF(m) ;
        return m ;
    }

    /** Encode the result set as RDF in the model provided.
      *
      * @param model     The place where to put the RDF.
      * @return Resource The resource for the result set.
      */

    public Resource asRDF(Model model)
    {
        QueryResultsFormatter fmt = new QueryResultsFormatter(this) ;
        Resource r = fmt.asRDF(model) ;
        fmt.close() ;
        return r ;
    }

    /** Print out the result set in dump format.  Easeier to read than computed N3
     */

    public void list(PrintWriter pw)
    {
        // Duplicate so we can reset the iterator.
        QueryResultsMem qrm = new QueryResultsMem(this) ;
        QueryResultsFormatter fmt = new QueryResultsFormatter(qrm) ;
        fmt.dump(pw, false) ;
        qrm.close() ;
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
