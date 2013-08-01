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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import org.apache.jena.jdbc.results.metadata.AskResultsMetadata;
import org.apache.jena.jdbc.statements.JenaStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * Represents an ASK result
 *
 */
public class AskResults extends JenaResultSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(AskResults.class);
    
    private boolean result, closed = false;
    private int currRow = 0;
    private boolean needsCommit = false;
    private AskResultsMetadata metadata;
    private String columnLabel;

    /**
     * Creates a new ASK result
     * @param statement Statement
     * @param result Boolean result
     * @param commit Whether a commit is required when the results are closed
     * @throws SQLException Thrown if the arguments are invalid
     */
    public AskResults(JenaStatement statement, boolean result, boolean commit) throws SQLException {
        super(statement);
        this.result = result;
        this.needsCommit = commit;
        this.metadata = statement.getJenaConnection().applyPostProcessors(new AskResultsMetadata(this));
        this.columnLabel = this.metadata.getColumnLabel(AskResultsMetadata.COLUMN_INDEX_ASK);
    }

    public boolean absolute(int row) throws SQLException {
        // We can move backwards and forwards in an ASK result but there
        // is only ever a single row
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set has been closed");
        } else if (row == 1) {
            return this.first();
        } else if (row == -1) {
            return this.last();
        } else if (row == 0) {
            return true;
        } else {
            throw new SQLException("Moving the requested number of rows would be outside the allowable range of rows");
        }
    }

    public void afterLast() throws SQLException {
        this.currRow = 2;
    }

    public void beforeFirst() throws SQLException {
        this.currRow = 0;
    }

    public void close() throws SQLException {
        if (this.closed) return;
        this.closed = true;
        if (this.needsCommit) {
            LOGGER.info("Result Set associated with an auto-committing transaction, performing a commit now");
            this.getStatement().getConnection().commit();
        }
    }

    public int findColumn(String columnLabel) throws SQLException {
        if (this.columnLabel.equals(columnLabel)) return 1;
        throw new SQLException("The given column does not exist in this result set");
    }

    public boolean first() throws SQLException {
        if (this.isClosed()) throw new SQLException("Cannot move to a row after the result set has been closed");
        this.currRow = 1;
        return true;
    }

    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    public int getFetchSize() throws SQLException {
        return 1;
    }

    public int getRow() throws SQLException {
        return this.currRow;
    }

    public int getType() throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    public boolean isAfterLast() throws SQLException {
        if (this.isClosed()) throw new SQLException("Result Set is closed");
        return this.currRow == 2;
    }

    public boolean isBeforeFirst() throws SQLException {
        if (this.isClosed()) throw new SQLException("Result Set is closed");
        return this.currRow == 0;
    }

    public boolean isClosed() throws SQLException {
        return this.closed;
    }

    public boolean isFirst() throws SQLException {
        if (this.isClosed()) throw new SQLException("Result Set is closed");
        return this.currRow == 1;
    }

    public boolean isLast() throws SQLException {
        if (this.isClosed()) throw new SQLException("Result Set is closed");
        return this.currRow == 1;
    }

    public boolean last() throws SQLException {
        if (this.isClosed()) throw new SQLException("Cannot move to a row after the result set has been closed");
        this.currRow = 1;
        return true;
    }

    public boolean next() throws SQLException {
        if (this.isClosed()) throw new SQLException("Cannot move to a row after the result set has been closed");
        if (this.currRow < 2) {
            this.currRow++;
        }
        return this.currRow == 1;
    }

    public boolean relative(int rows) throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set has been closed");
        } else if (this.currRow == 0 && (rows >= 0 && rows <= 2)) {
            this.currRow += rows;
            return true;
        } else if (this.currRow == 1 && (rows >= -1 && rows <= 1)) {
            this.currRow += rows;
            return true;
        } else if (this.currRow == 2 && (rows >= -2 && rows <= 0)) {
            this.currRow += rows;
            return true;
        } else {
            throw new SQLException("Moving the requested number of rows would be outside the allowable range of rows");
        }
    }

    public void setFetchDirection(int direction) throws SQLException {
        if (direction != ResultSet.FETCH_FORWARD) throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets only support forward fetch");
    }

    public void setFetchSize(int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException("Fetch Size is not relevant for ASK results");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this.metadata;
    }

    @Override
    protected String findColumnLabel(int columnIndex) throws SQLException {
        if (columnIndex == AskResultsMetadata.COLUMN_INDEX_ASK) return this.columnLabel;
        throw new SQLException("Column Index is out of bounds");
    }

    @Override
    protected Node getNode(String columnLabel) throws SQLException {
        if (this.isClosed()) throw new SQLException("Result Set is closed");
        if (this.currRow != 1) throw new SQLException("Not currently at a row");
        if (this.columnLabel.equals(columnLabel)) {
            return NodeFactory.createLiteral(Boolean.toString(this.result), XSDDatatype.XSDboolean);
        } else {
            throw new SQLException("The given column does not exist in the result set");
        }
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        if (this.isClosed()) throw new SQLException("Result Set is closed");
        if (this.currRow != 1) throw new SQLException("Not currently at a row");
        if (this.columnLabel.equals(columnLabel)) {
            this.setNull(false);
            return this.result;
        } else {
            throw new SQLException("The given column does not exist in the result set");
        }
    }


}
