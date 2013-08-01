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

package org.apache.jena.jdbc.results;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import org.apache.jena.jdbc.statements.JenaStatement;

import com.hp.hpl.jena.query.QueryExecution;

/**
 * Represents a set of streamed results backed by some {@link QueryExecution},
 * streamed results are considered to be forward only
 * 
 * @param <T>
 *            Type of the underlying result rows
 * 
 */
public abstract class StreamedResults<T> extends QueryExecutionResults {

    private T currItem;
    private boolean finished = false;
    private int currRow = 0;

    /**
     * Creates new streamed results
     * 
     * @param statement
     *            Statement that created the result set
     * @param qe
     *            Query Execution
     * @param commit
     *            Whether a commit is necessary when the results are closed
     * @throws SQLException
     *             Thrown if the arguments are invalid
     */
    public StreamedResults(JenaStatement statement, QueryExecution qe, boolean commit) throws SQLException {
        super(statement, qe, commit);
    }

    /**
     * Gets the current result row (if any)
     * 
     * @return Result row, null if not at a row
     * @throws SQLException
     *             Thrown if the result set is closed
     */
    protected T getCurrentRow() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result set is closed");
        return this.currItem;
    }

    /**
     * Method which derived classes must implement to indicate whether they have
     * further rows available
     * 
     * @return True if further rows are available, false otherwise
     * @throws SQLException
     *             Thrown if an error determining whether rows are available
     */
    protected abstract boolean hasNext() throws SQLException;

    /**
     * Method which derived classes must implement to provide the next row
     * available
     * 
     * @return Next row available
     * @throws SQLException
     *             Thrown if this method is invoked when no further rows are
     *             available
     */
    protected abstract T moveNext() throws SQLException;

    @Override
    public final boolean absolute(int row) throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set has been closed");
        } else if (row == 1) {
            // Try and move to the first row
            return this.first();
        } else if (row == -1) {
            // Try and move to the last row
            return this.last();
        } else if (row <= 0) {
            // Can't move to an arbitrary relative row from the end of the
            // results
            throw new SQLException(
                    "Jena JDBC result sets are forward only, cannot move to a row which is relative to the end of the result set since the number of result rows is not known in advance");
        } else if (row == this.currRow) {
            // Already at the desired row
            return true;
        } else if (row < this.currRow) {
            throw new SQLException("Jena JDBC result sets are forward only, cannot move backwards");
        } else {
            // Before the desired row
            while (this.hasNext() && this.currRow < row) {
                this.currItem = this.moveNext();
                this.currRow++;
            }
            // If we didn't reach it we hit the end of the result set
            if (this.currRow < row) {
                this.finished = true;
                this.currItem = null;
            }
            return (row == this.currRow);
        }
    }

    @Override
    public final void afterLast() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        if (finished)
            return;

        // Move to end of results
        while (this.hasNext()) {
            this.currItem = this.moveNext();
            this.currRow++;
        }
        this.currItem = null;
        this.finished = true;
    }

    @Override
    public final void beforeFirst() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        // If we've started throw an error as we can't move backwards
        if (this.currRow > 0)
            throw new SQLException(
                    "Jena JDBC result sets are forward only, can't move to before the start of the result set after navigation through the result set has begun");
        // Otherwise OK
        this.currItem = null;
    }

    @Override
    protected final void closeInternal() throws SQLException {
        this.currItem = null;
        this.finished = true;
        this.closeStreamInternal();
    }

    protected abstract void closeStreamInternal() throws SQLException;

    @Override
    public final boolean first() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        if (this.currRow == 1)
            return true;
        throw new SQLException(
                "Jena JDBC result sets are forward only, can't move backwards to the first row after the first row has been passed");
    }

    @Override
    public final int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public final int getFetchSize() throws SQLException {
        // TODO Need a buffering wrapper around ResultSet to make this
        // configurable
        return 0;
    }

    @Override
    public final int getRow() throws SQLException {
        return this.currRow;
    }

    @Override
    public final int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public final boolean isAfterLast() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        return this.finished;
    }

    @Override
    public final boolean isBeforeFirst() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        return this.currRow == 0;
    }

    @Override
    public final boolean isFirst() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        return this.currRow == 1;
    }

    @Override
    public final boolean isLast() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        return !this.hasNext();
    }

    @Override
    public final boolean last() throws SQLException {
        if (this.isClosed() || this.finished) {
            throw new SQLException("Jena JDBC Result Sets are forward-only");
        } else {
            while (this.hasNext()) {
                this.currItem = this.moveNext();
                this.currRow++;
            }
            return true;
        }
    }

    @Override
    public final boolean next() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to the next row in a closed result set");
        } else {
            if (this.hasNext()) {
                this.currItem = this.moveNext();
                this.currRow++;
                return true;
            } else {
                if (!this.finished)
                    this.currRow++;
                this.finished = true;
                return false;
            }
        }
    }

    @Override
    public final boolean relative(int rows) throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set has been closed");
        } else if (rows == 0) {
            // Already at the desired row
            return true;
        } else if (rows < 0) {
            throw new SQLException("Jena JDBC result sets are forward only, cannot move backwards");
        } else {
            // Before the desired row
            int moved = 0;
            while (this.hasNext() && moved < rows) {
                this.currItem = this.moveNext();
                this.currRow++;
                moved++;
            }
            // If we didn't reach it we hit the end of the result set
            if (moved < rows) {
                this.finished = true;
                this.currItem = null;
            }
            return (rows == moved);
        }
    }

    @Override
    public final void setFetchDirection(int direction) throws SQLException {
        if (direction != ResultSet.FETCH_FORWARD)
            throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets only support forward fetch");
    }

    @Override
    public final void setFetchSize(int rows) throws SQLException {
        // TODO Need to provide some buffering wrapper over a ResultSet to make
        // this possible
        throw new SQLFeatureNotSupportedException();
    }
}
