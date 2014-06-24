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

package com.hp.hpl.jena.sparql.resultset;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.iterator.PeekIterator ;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.ResultBinding ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** An in-memory result set.  
 * Also useful for writing input processors which
 * keep the result set in memory.
 */


public class ResultSetMem implements com.hp.hpl.jena.query.ResultSetRewindable, ResultSetPeekable
{
    // ??? Convert to use a ResultSetProcessor
    // The result set in memory
    // .hasPrevious() and .previous()
    protected List<Binding> rows = new ArrayList<>();
    protected List<String> varNames = null ;

    private int rowNumber = 0 ;
    private PeekIterator<Binding> iterator = null ;
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
    
    /** Create an in-memory result set from an array of 
     * ResulSets. It is assumed that all the ResultSets 
     * from the array have the same variables.
     * 
     * @param sets the ResultSet objects to concatenate.
     */
    
    public ResultSetMem(ResultSet... sets) 
    {
        varNames = sets[0].getResultVars();
        
        for (ResultSet rs : sets) 
        {
        	if ( !varNames.equals(rs.getResultVars()) )
        		throw new ResultSetException("ResultSet must have the same variables.") ;
            if (rs instanceof ResultSetMem)
                rows.addAll(((ResultSetMem) rs).rows);
            else 
                while (rs.hasNext()) rows.add(rs.nextBinding());
        }
        reset();
    }

    public ResultSetMem()
    {
        this.varNames = new ArrayList<>() ;
        reset() ;
    }
    
   // -------- ResultSet interface ------------------------------
   /**
     *  @throws UnsupportedOperationException always thrown.
     */

    @Override
    public void remove() throws java.lang.UnsupportedOperationException
    {
        throw new java.lang.UnsupportedOperationException(
            Utils.className(this)+": Attempt to remove an element");
    }

    /**
     * Is there another possibility?
     */
    @Override
    public boolean hasNext() { return iterator.hasNext() ; }

    /** Moves onto the next result possibility.
     */
    
    @Override
    public QuerySolution nextSolution()  { return new ResultBinding(model, nextBinding()) ; }
    
    @Override
    public Binding nextBinding()  { rowNumber++ ; return iterator.next() ; }

    /** Moves onto the next result possibility.
     *  The returned object should be of class QuerySolution
     */

    @Override
    public QuerySolution next() { return nextSolution() ; }

    /** Reset this result set back to the beginning */
    public void rewind( ) { reset() ; }

    @Override
    public void reset() { iterator = new PeekIterator<>(rows.iterator()) ; rowNumber = 0 ; }

    /** Return the "row" number for the current iterator item
     */
    @Override
    public int getRowNumber() { return rowNumber ; }

    @Override
    public Model getResourceModel()
    {
        return model ;
    }
    
    /** Return the number of rows
     */
    @Override
    public int size() { return rows.size() ; }
    
    /** Get the variable names for the projection
     */
    @Override
    public List<String> getResultVars() { return varNames ; }

    @Override
    public QuerySolution peek() {
        return new ResultBinding(model, peekBinding());
    }

    @Override
    public Binding peekBinding() {
        //PeekIterator.element() is the one that throws NoSuchElementException.
        return iterator.element();
    }

}
