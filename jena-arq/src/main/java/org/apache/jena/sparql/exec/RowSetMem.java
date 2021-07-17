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

package org.apache.jena.sparql.exec;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.iterator.PeekIterator ;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding ;

/**
 *  A row set held in-memory which is rewindable and peekable
 */

public class RowSetMem implements RowSetRewindable
{
    protected final List<Binding> rows;
    protected final List<Var> vars ;

    private int rowNumber = 0 ;
    private PeekIterator<Binding> iterator = null ;

    public static RowSetRewindable create(RowSet rowSet) {
        if ( rowSet instanceof RowSetMem )
            return new RowSetMem((RowSetMem)rowSet);
        else
            return new RowSetMem(rowSet);
    }

    /** Create an in-memory result set from another one
     *
     * @param other     The other RowSetMem object
     */
    private RowSetMem(RowSetMem other) {
        // Should be no need to isolate the rows list.
        this(other, false);
    }

    /**
     * Create an in-memory result set from another one
     *
     * @param other
     *            The other ResultSetMem object
     * @param takeCopy
     *            Should we copy the rows?
     */

    private RowSetMem(RowSetMem other, boolean takeCopy) {
        vars = other.vars;
        if ( takeCopy )
            rows = new ArrayList<>(other.rows);
        else
            // Share results (not the iterator).
            rows = other.rows;
        reset();
    }

    /**
     * Create an in-memory result set from any RowSet object. If the
     * ResultSet is an in-memory one already, then no copying is done - the
     * necessary internal datastructures are shared. This operation destroys
     * (uses up) a RowSet object that is not an in-memory one.
     */

    private RowSetMem(RowSet other) {
        this.rows = new ArrayList<>();
        other.forEachRemaining(rows::add);
        this.vars = other.getResultVars();
        reset();
    }

    /**
     * Is there another possibility?
     */
    @Override
    public boolean hasNext() { return iterator.hasNext() ; }

    /**
     * Moves onto the next result possibility.
     */
    @Override
    public Binding next()  { rowNumber++ ; return iterator.next() ; }

    /** Reset this result set back to the beginning */
    public void rewind() {
        reset();
    }

    @Override
    public void reset() {
        iterator = new PeekIterator<>(rows.iterator());
        rowNumber = 0;
    }

    /** Return the "row" number for the current iterator item
     */
    @Override
    public long getRowNumber() { return rowNumber ; }

    /**
     *  Return the number of rows
     */
    @Override
    public long size() { return rows.size() ; }

    /** Get the variable names for the projection
     */
    @Override
    public List<Var> getResultVars() { return vars ; }

    public Binding peek() {
        return iterator.element();
    }

    @Override
    public void close() {}
}