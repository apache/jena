/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.util.Utils;

/** An in-memory result set.  
 * Also useful for writing input processors which
 * keep the result set in memory.
 *  
 * @author      Andy Seaborne
 */


public class ResultSetMem implements com.hp.hpl.jena.query.ResultSetRewindable
{
    // ??? Convert to use a ResultSetProcessor
    // The result set in memory
    // .hasPrevious() and .previous()
    protected List<Binding> rows = new ArrayList<Binding>();
    protected List<String> varNames = null ;

    private int rowNumber = 0 ;
    private Iterator<Binding> iterator = null ;
    private Model model = null ;

    /** Create an in-memory result set from another one
     *
     * @param imrs2     The other QueryResultsMem object
     */

    public ResultSetMem(ResultSetMem imrs2)
    {
        this(imrs2, false) ;
    }

    /** Create an in-memory result set from another one
     *
     * @param imrs2     The other ResultSetMem object
     * @param takeCopy  Should we copy the rows?
     */

    public ResultSetMem(ResultSetMem imrs2, boolean takeCopy)
    {
        varNames = imrs2.varNames;
        if ( takeCopy )
            rows.addAll(imrs2.rows) ;
        else
            // Share results (not the iterator).
            rows = imrs2.rows ;
        reset() ;
    }

    /** Create an in-memory result set from any ResultSet object.
     *  If the ResultSet is an in-memory one already, then no
     *  copying is done - the necessary internal datastructures
     *  are shared.  This operation destroys (uses up) a ResultSet
     *  object that is not an in memory one.
     */

    public ResultSetMem(ResultSet qr)
    {
        model = qr.getResourceModel() ;
        if (qr instanceof ResultSetMem)
        {
            ResultSetMem qrm = (ResultSetMem) qr;
            this.rows = qrm.rows;
            this.varNames = qrm.varNames;
        }
        else
        {
            varNames = qr.getResultVars();
            while (qr.hasNext())
            {
                Binding rb = qr.nextBinding();
                rows.add(rb);
            }
        }
        reset();
    }

    public ResultSetMem()
    {
        this.varNames = new ArrayList<String>() ;
        reset() ;
    }
    
   // -------- ResultSet interface ------------------------------
   /**
     *  @throws UnsupportedOperationException always thrown.
     */

    public void remove() throws java.lang.UnsupportedOperationException
    {
        throw new java.lang.UnsupportedOperationException(
            Utils.className(this)+": Attempt to remove an element");
    }

    /**
     * Is there another possibility?
     */
    public boolean hasNext() { return iterator.hasNext() ; }

    /** Moves onto the next result possibility.
     */
    
    public QuerySolution nextSolution()  { return new ResultBinding(model, nextBinding()) ; }
    
    public Binding nextBinding()  { rowNumber++ ; return iterator.next() ; }

    /** Moves onto the next result possibility.
     *  The returned object should be of class QuerySolution
     */

    public QuerySolution next() { return nextSolution() ; }

    /** Reset this result set back to the beginning */
    public void rewind( ) { reset() ; }

    public void reset() { iterator = rows.iterator() ; rowNumber = 0 ; }

    /** Return the "row" number for the current iterator item
     */
    public int getRowNumber() { return rowNumber ; }

    public Model getResourceModel()
    {
        return model ;
    }
    
    /** Return the number of rows
     */
    public int size() { return rows.size() ; }
    
    /** Get the variable names for the projection
     */
    public List<String> getResultVars() { return varNames ; }

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
