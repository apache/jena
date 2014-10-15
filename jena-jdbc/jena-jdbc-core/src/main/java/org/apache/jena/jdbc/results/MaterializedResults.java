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
 * Represents a set of materialized results backed by some
 * {@link QueryExecution}, materialized results permit scrolling but are not
 * sensitive to changes in the underlying data
 * 
 * @param <T>
 *            Type of the underlying result rows
 * 
 */
public abstract class MaterializedResults<T> extends QueryExecutionResults {

    private T currItem;
    private int currRow = 0;

    /**
     * Creates new materialized results
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
    public MaterializedResults(JenaStatement statement, QueryExecution qe, boolean commit) throws SQLException {
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
     * further rows available to move forwards to
     * 
     * @return True if further rows are available, false otherwise
     * @throws SQLException
     *             Thrown if an error determining whether rows are available
     */
    protected abstract boolean hasNext() throws SQLException;

    /**
     * Method which derived classes must implement to indicate whether they have
     * further rows available to move backwards to
     * 
     * @return True if further rows are available, false otherwise
     * @throws SQLException
     *             Thrown if an error determining whether rows are available
     */
    protected abstract boolean hasPrevious() throws SQLException;

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

    /**
     * Method which derived classes must implement to provide the previous row
     * available
     * 
     * @return Previous row available
     * @throws SQLException
     *             Thrown if this method is invoked when no previous rows are
     *             available
     */
    protected abstract T movePrevious() throws SQLException;

    protected abstract int getTotalRows() throws SQLException;

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
        } else if (row == 0) {
            // Try and move to before first row
            this.beforeFirst();
            return false;
        } else if (row > this.getTotalRows()) {
            // Try and move to after last row
            this.afterLast();
            return false;
        } else if (row <= 0) {
            // Move to a row relative to the end of the result set
            int destRow = this.getTotalRows() + 1 + row;
            if (destRow < 1) {
                // Move to before first
                this.beforeFirst();
                return false;
            } else {
                // Call ourselves to avoid repeating logic
                return this.absolute(destRow);
            }
        } else if (row == this.currRow) {
            // Already at the desired row
            return true;
        } else if (row < this.currRow) {
            // After the desired row
            while (this.hasPrevious() && row < this.currRow) {
                this.currItem = this.movePrevious();
                this.currRow--;
            }
            // Since we already checked that the desired row is a valid absolute
            // row it is always possible to move backwards to the desired row
            return true;
        } else {
            // Before the desired row
            while (this.hasNext() && this.currRow < row) {
                this.currItem = this.moveNext();
                this.currRow++;
            }
            // Since we already checked that the desired row is a valid absolute
            // row it is always possible to move forwards to the desired row
            return true;
        }
    }

    @Override
    public final void afterLast() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");

        // Move to end of results if necessary
        while (this.hasNext()) {
            this.currItem = this.moveNext();
            this.currRow++;
        }
        this.currItem = null;
        this.currRow = this.getTotalRows() + 1;
    }

    @Override
    public final void beforeFirst() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");

        // Are we after the last row? If so reset current row or movePrevious()
        // may break
        if (this.isAfterLast())
            this.currRow = this.getTotalRows();

        // Move to start of results if necessary
        while (this.hasPrevious()) {
            this.currItem = this.movePrevious();
            this.currRow--;
        }
        this.currItem = null;
        this.currRow = 0;
    }

    @Override
    protected final void closeInternal() throws SQLException {
        this.currItem = null;
        this.closeStreamInternal();
    }

    protected abstract void closeStreamInternal() throws SQLException;

    @Override
    public final boolean first() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");

        // If no rows this should always return false
        if (this.getTotalRows() == 0)
            return false;

        // Are we already at the first row?
        if (this.currRow == 1)
            return true;

        // Are we after the last row? If so reset current row or movePrevious()
        // may break
        if (this.isAfterLast())
            this.currRow = this.getTotalRows();

        // Before first row?
        if (this.isBeforeFirst()) {
            // Need to move forwards to first row
            if (this.hasNext()) {
                this.currItem = this.moveNext();
                this.currRow = 1;
                return true;
            } else {
                return false;
            }
        } else {
            // Otherwise move backwards to it
            while (this.hasPrevious()) {
                this.currItem = this.movePrevious();
                this.currRow--;
            }
            return true;
        }
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
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    @Override
    public final boolean isAfterLast() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        return this.currRow > this.getTotalRows();
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
        return this.currRow == this.getTotalRows();
    }

    @Override
    public final boolean last() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");

        // If no rows this should always return false
        if (this.getTotalRows() == 0)
            return false;

        // Are we already at the last row?
        if (this.currRow == this.getTotalRows())
            return true;

        // Are we after the last row?
        if (this.isAfterLast()) {
            this.currRow = this.getTotalRows();

            // Move backwards to last row
            if (this.hasPrevious()) {
                this.currItem = this.movePrevious();
                this.currRow = this.getTotalRows();
                return true;
            } else {
                return false;
            }
        } else {
            // Otherwise move forwards to the last row
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
                this.currItem = null;
                this.currRow = this.getTotalRows() + 1;
                return false;
            }
        }
    }

    @Override
    public final boolean previous() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot mvoe to the previous row in a closed result set");
        } else {
            if (this.hasPrevious()) {
                this.currItem = this.movePrevious();
                this.currRow--;
                return true;
            } else {
                this.currItem = null;
                this.currRow = 0;
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
            // Calculate destination row and use absolute() to move there
            int destRow = this.currRow + rows;

            // Need to sanitize resulting values below zero as otherwise
            // absolute will move us to a row relative to end of results when
            // actually this means we should be moving to before the first row
            if (destRow < 0)
                destRow = 0;
            return this.absolute(destRow);
        } else if (this.currRow + rows > this.getTotalRows()) {
            // Would result in moving beyond the end of the results
            this.afterLast();
            return false;
        } else {
            // Before the desired row
            int moved = 0;
            while (this.hasNext() && moved < rows) {
                this.currItem = this.moveNext();
                this.currRow++;
                moved++;
            }
            // Since we already checked if the move would take us beyond the end
            // of the results we will always have reached the desired row
            return true;
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
