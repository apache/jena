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

package org.apache.jena.jdbc.statements;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.results.AskResults;
import org.apache.jena.jdbc.results.MaterializedSelectResults;
import org.apache.jena.jdbc.results.SelectResults;
import org.apache.jena.jdbc.results.TripleIteratorResults;
import org.apache.jena.jdbc.results.TripleListResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Abstract Jena JDBC implementation of a statement that only permits read
 * operations
 * 
 */
public abstract class JenaStatement implements Statement {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenaStatement.class);

    protected static final int DEFAULT_HOLDABILITY = ResultSet.CLOSE_CURSORS_AT_COMMIT;
    protected static final int DEFAULT_FETCH_DIRECTION = ResultSet.FETCH_FORWARD;
    protected static final int DEFAULT_FETCH_SIZE = 0;
    protected static final boolean DEFAULT_AUTO_COMMIT = JenaConnection.DEFAULT_AUTO_COMMIT;
    protected static final int DEFAULT_TRANSACTION_LEVEL = JenaConnection.DEFAULT_ISOLATION_LEVEL;
    protected static final int NO_LIMIT = 0;
    protected static final int DEFAULT_TYPE = ResultSet.TYPE_FORWARD_ONLY;
    protected static final int USE_CONNECTION_COMPATIBILITY = Integer.MIN_VALUE;

    private List<String> commands = new ArrayList<String>();
    private SQLWarning warnings = null;
    private JenaConnection connection;
    private ResultSet currResults = null;
    private Queue<ResultSet> results = new LinkedList<ResultSet>();
    private List<ResultSet> openResults = new ArrayList<ResultSet>();
    private boolean closed = false;
    private int type = DEFAULT_TYPE;
    private int fetchDirection = DEFAULT_FETCH_DIRECTION;
    private int fetchSize = DEFAULT_FETCH_SIZE;
    private int holdability = DEFAULT_HOLDABILITY;
    private int updateCount = 0;
    private boolean autoCommit = DEFAULT_AUTO_COMMIT;
    private int transactionLevel = DEFAULT_TRANSACTION_LEVEL;
    private int maxRows = NO_LIMIT;
    @SuppressWarnings("unused")
    private boolean escapeProcessing = false;
    private int timeout = NO_LIMIT;
    private int compatibilityLevel = USE_CONNECTION_COMPATIBILITY;

    /**
     * Creates a new statement
     * 
     * @param connection
     *            Connection
     * @throws SQLException
     *             Thrown if the arguments are invalid
     */
    public JenaStatement(JenaConnection connection) throws SQLException {
        this(connection, DEFAULT_TYPE, DEFAULT_FETCH_DIRECTION, DEFAULT_FETCH_SIZE, DEFAULT_HOLDABILITY, DEFAULT_AUTO_COMMIT,
                DEFAULT_TRANSACTION_LEVEL);
    }

    /**
     * Creates a new statement
     * 
     * @param connection
     *            Connection
     * @param type
     *            Result Set type for result sets produced by this statement
     * @param fetchDir
     *            Fetch Direction
     * @param fetchSize
     *            Fetch Size
     * @param holdability
     *            Result Set holdability
     * @param autoCommit
     *            Auto-commit behaviour
     * @param transactionLevel
     *            Transaction level
     * @throws SQLException
     *             Thrown if there is an error with the statement parameters
     * 
     */
    public JenaStatement(JenaConnection connection, int type, int fetchDir, int fetchSize, int holdability, boolean autoCommit,
            int transactionLevel) throws SQLException {
        if (connection == null)
            throw new SQLException("Cannot create a Statement with a null connection");
        this.connection = connection;
        this.checkFetchDirection(fetchDir);
        this.type = type;
        this.fetchDirection = fetchDir;
        this.fetchSize = fetchSize;
        this.checkHoldability(holdability);
        this.holdability = holdability;
        this.autoCommit = autoCommit;
        this.transactionLevel = transactionLevel;
    }

    /**
     * Gets the underlying {@link JenaConnection} implementation, useful for
     * accessing Jena JDBC specific information such as desired JDBC
     * compatibility level
     * 
     * @return Underlying Jena Connection
     */
    public JenaConnection getJenaConnection() {
        return this.connection;
    }

    /**
     * Gets the JDBC compatibility level that is in use, see
     * {@link JdbcCompatibility} for explanations
     * <p>
     * By default this is set at the connection level and inherited, however you
     * may call {@link #setJdbcCompatibilityLevel(int)} to set the compatibility
     * level for this statement. This allows you to change the compatibility
     * level on a per-query basis if so desired.
     * </p>
     * 
     * @return Compatibility level
     */
    public int getJdbcCompatibilityLevel() {
        if (this.compatibilityLevel == USE_CONNECTION_COMPATIBILITY)
            return this.connection.getJdbcCompatibilityLevel();
        return this.compatibilityLevel;
    }

    /**
     * Sets the JDBC compatibility level that is in use, see
     * {@link JdbcCompatibility} for explanations.
     * <p>
     * By default this is set at the connection level and inherited, however you
     * may call {@link #setJdbcCompatibilityLevel(int)} to set the compatibility
     * level for this statement. This allows you to change the compatibility
     * level on a per-query basis if so desired.
     * </p>
     * <p>
     * Changing the level may not effect existing open objects, behaviour in
     * this case will be implementation specific.
     * </p>
     * 
     * @param level
     *            Compatibility level
     */
    public void setJdbcCompatibilityLevel(int level) {
        if (level == USE_CONNECTION_COMPATIBILITY) {
            this.compatibilityLevel = USE_CONNECTION_COMPATIBILITY;
        } else {
            this.compatibilityLevel = JdbcCompatibility.normalizeLevel(level);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        this.commands.add(sql);
    }

    @Override
    public void cancel() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearBatch() throws SQLException {
        this.commands.clear();
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.warnings = null;
    }

    @Override
    public void close() throws SQLException {
        if (this.closed)
            return;
        LOGGER.info("Closing statement");
        this.closed = true;
        // Close current result set (if any)
        if (this.currResults != null) {
            this.currResults.close();
            this.currResults = null;
        }
        // Close any remaining open results
        if (this.results.size() > 0 || this.openResults.size() > 0) {
            LOGGER.info("Closing " + (this.results.size() + this.openResults.size()) + " open result sets");

            // Queue results i.e. stuff resulting from a query that produced
            // multiple result sets or executeBatch() calls
            while (!this.results.isEmpty()) {
                ResultSet rset = this.results.poll();
                if (rset != null)
                    rset.close();
            }
            // Close open result sets i.e. stuff left around depending on
            // statement correction
            for (ResultSet rset : this.openResults) {
                rset.close();
            }
            this.openResults.clear();
            LOGGER.info("All open result sets were closed");
        }
        LOGGER.info("Statement was closed");
    }

    @Override
    public final boolean execute(String sql) throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");

        // Pre-process the command text
        LOGGER.info("Received input command text:\n {}", sql);
        sql = this.connection.applyPreProcessors(sql);
        LOGGER.info("Command text after pre-processing:\n {}", sql);

        Query q = null;
        UpdateRequest u = null;
        try {
            // Start by assuming a query
            q = QueryFactory.create(sql);
        } catch (Exception e) {
            try {
                // If that fails try as an update instead
                u = UpdateFactory.create(sql);
            } catch (Exception e2) {
                LOGGER.error("Command text was not a valid SPARQL query/update", e2);
                throw new SQLException("Not a valid SPARQL query/update", e);
            }
        }

        if (q != null) {
            // Execute as a query
            LOGGER.info("Treating command text as a query");
            return this.executeQuery(q);
        } else if (u != null) {
            // Execute as an update
            LOGGER.info("Treating command text as an update");
            this.executeUpdate(u);
            return false;
        } else {
            throw new SQLException("Unable to create a SPARQL query/update");
        }
    }

    private boolean executeQuery(Query q) throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");

        // Do we need transactions?
        boolean needsBegin = (!this.autoCommit && this.transactionLevel != Connection.TRANSACTION_NONE && !this
                .hasActiveTransaction());
        boolean needsCommit = (this.autoCommit && this.transactionLevel != Connection.TRANSACTION_NONE);

        // Do this first in a separate try catch so if we fail to start a
        // transaction we don't then try to roll it back which can mask the
        // actual cause of the error
        try {
            // Start a transaction if necessary
            if (needsCommit) {
                LOGGER.info("Running query in auto-commit mode");
                this.beginTransaction(ReadWrite.READ);
            } else if (needsBegin) {
                LOGGER.info("Starting a new transaction to run query, transaction will not be auto-committed");
                this.beginTransaction(ReadWrite.WRITE);
            }
        } catch (Exception e) {
            LOGGER.error("Starting the new transaction failed", e);
            throw new SQLException("Failed to start a new query transaction", e);
        }

        try {
            // Pre-process the query
            q = this.connection.applyPreProcessors(q);

            // Manipulate the query if appropriate
            if (this.maxRows > NO_LIMIT) {
                // If we have no LIMIT or the LIMIT is greater than the
                // permitted max rows
                // then we will set the LIMIT to the max rows
                if (!q.hasLimit() || q.getLimit() > this.maxRows) {
                    LOGGER.info("Enforced max rows on results by applying LIMIT {} to the query", this.maxRows);
                    q.setLimit(this.maxRows);
                }
            }

            // Create the query execution
            QueryExecution qe = this.createQueryExecution(q);

            // Manipulate the query execution if appropriate
            if (this.timeout > NO_LIMIT) {
                qe.setTimeout(this.timeout, TimeUnit.SECONDS, this.timeout, TimeUnit.SECONDS);
            }

            // Return the appropriate result set type
            if (q.isSelectType()) {
                switch (this.type) {
                case ResultSet.TYPE_SCROLL_INSENSITIVE:
                    this.currResults = new MaterializedSelectResults(this, qe, ResultSetFactory.makeRewindable(this.connection
                            .applyPostProcessors(qe.execSelect())), false);
                    break;
                case ResultSet.TYPE_FORWARD_ONLY:
                default:
                    this.currResults = new SelectResults(this, qe, this.connection.applyPostProcessors(qe.execSelect()),
                            needsCommit);
                    break;
                }
            } else if (q.isAskType()) {
                boolean askRes = qe.execAsk();
                qe.close();
                this.currResults = new AskResults(this, this.connection.applyPostProcessors(askRes), needsCommit);
            } else if (q.isDescribeType()) {
                switch (this.type) {
                case ResultSet.TYPE_SCROLL_INSENSITIVE:
                    this.currResults = new TripleListResults(this, qe, Iter.toList(this.connection.applyPostProcessors(qe
                            .execDescribeTriples())), false);
                    break;
                case ResultSet.TYPE_FORWARD_ONLY:
                default:
                    this.currResults = new TripleIteratorResults(this, qe, this.connection.applyPostProcessors(qe
                            .execDescribeTriples()), needsCommit);
                    break;
                }
            } else if (q.isConstructType()) {
                switch (this.type) {
                case ResultSet.TYPE_SCROLL_INSENSITIVE:
                    this.currResults = new TripleListResults(this, qe, Iter.toList(this.connection.applyPostProcessors(qe
                            .execConstructTriples())), false);
                    break;
                case ResultSet.TYPE_FORWARD_ONLY:
                default:
                    this.currResults = new TripleIteratorResults(this, qe, this.connection.applyPostProcessors(qe
                            .execConstructTriples()), needsCommit);
                    break;
                }
            } else {
                throw new SQLException("Unknown SPARQL Query type");
            }

            // Can immediately commit when type is
            // TYPE_SCROLL_INSENSITIVE and auto-committing since we have
            // already materialized results so don't need the read
            // transaction
            if (this.type == ResultSet.TYPE_SCROLL_INSENSITIVE && needsCommit) {
                LOGGER.info("Auto-committing query transaction since results have been materialized");
                this.commitTransaction();
            }

            return true;
        } catch (SQLException e) {
            if (needsCommit) {
                // When auto-committing and query fails roll back immediately
                LOGGER.warn("Rolling back failed query transaction", e);
                this.rollbackTransaction();
            }
            throw e;
        } catch (Throwable e) {
            LOGGER.error("SPARQL Query evaluation failed", e);
            if (needsCommit) {
                // When auto-committing and query fails roll back immediately
                LOGGER.warn("Rolling back failed query transaction");
                this.rollbackTransaction();
            }
            throw new SQLException("Error occurred during SPARQL query evaluation", e);
        }
    }

    /**
     * Helper method which derived classes must implement to provide a query
     * execution
     * 
     * @param q
     *            Query
     * @return Query Execution
     * @throws SQLException
     *             Thrown if there is a problem creating a query execution
     */
    protected abstract QueryExecution createQueryExecution(Query q) throws SQLException;

    private int executeUpdate(UpdateRequest u) throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");
        if (this.connection.isReadOnly())
            throw new SQLException("The JDBC connection is currently in read-only mode, updates are not permitted");

        // Do we need transactions?
        boolean needsBegin = (!this.autoCommit && this.transactionLevel != Connection.TRANSACTION_NONE && !this
                .hasActiveTransaction());
        boolean needsCommit = (this.autoCommit && this.transactionLevel != Connection.TRANSACTION_NONE);

        try {
            // Start a Transaction if necessary
            if (needsCommit || needsBegin) {
                if (this.autoCommit) {
                    LOGGER.info("Running update in auto-commit mode");
                } else {
                    LOGGER.info("Starting a new transaction to run update, transaction will not be auto-committed");
                }
                this.beginTransaction(ReadWrite.WRITE);
            }
        } catch (Exception e) {
            LOGGER.error("Starting the new transaction failed", e);
            throw new SQLException("Failed to start a new query transaction", e);
        }

        try {
            // Pre-process the update
            u = this.connection.applyPreProcessors(u);

            // Execute updates
            UpdateProcessor processor = this.createUpdateProcessor(u);
            processor.execute();

            // If auto-committing can commit immediately
            if (needsCommit) {
                LOGGER.info("Auto-committing update transaction");
                this.commitTransaction();
            }

            return 0;
        } catch (SQLException e) {
            if (needsCommit) {
                LOGGER.warn("Rolling back failed update transaction", e);
                this.rollbackTransaction();
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("SPARQL Update evaluation failed", e);
            if (needsCommit) {
                LOGGER.warn("Rolling back failed update transaction");
                this.rollbackTransaction();
            }
            throw new SQLException("Error occurred during SPARQL update evaluation", e);
        }
    }

    /**
     * Helper method which derived classes must implement to provide an update
     * processor
     * 
     * @param u
     *            Update
     * @return Update Processor
     */
    protected abstract UpdateProcessor createUpdateProcessor(UpdateRequest u) throws SQLException;

    protected abstract boolean hasActiveTransaction() throws SQLException;

    protected abstract void beginTransaction(ReadWrite type) throws SQLException;

    protected abstract void commitTransaction() throws SQLException;

    protected abstract void rollbackTransaction() throws SQLException;

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return this.execute(sql);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return this.execute(sql);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return this.execute(sql);
    }

    @Override
    public final int[] executeBatch() throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");

        // Issue warning where appropriate
        if (this.commands.size() > 1 && this.autoCommit && this.holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
            this.setWarning("Executing this batch of commands may lead to unexpectedly closed result sets because auto-commit is enabled and commit behaviour is set to close cursors at commit");
        }

        // Go ahead and process the batch
        int[] rets = new int[this.commands.size()];
        ResultSet curr = this.currResults;
        for (int i = 0; i < this.commands.size(); i++) {
            if (this.execute(this.commands.get(i))) {
                // True means it returned a ResultSet
                this.results.add(this.getResultSet());
                this.currResults = null;
                rets[i] = SUCCESS_NO_INFO;
            } else {
                // Need to add a null to getMoreResults() to produce correct
                // behavior across subsequent calls to getMoreResults()
                this.results.add(null);
                rets[i] = this.getUpdateCount();
            }
        }
        this.currResults = curr;
        // Make the next available results the current results if there
        // are no current results
        if (this.currResults == null && !this.results.isEmpty()) {
            this.currResults = this.results.poll();
        }
        return rets;
    }

    @Override
    public final ResultSet executeQuery(String sql) throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");

        // Pre-process the command text
        LOGGER.info("Received input command text:\n {}", sql);
        sql = this.connection.applyPreProcessors(sql);
        LOGGER.info("Command text after pre-processing:\n {}", sql);

        Query q = null;
        try {
            q = QueryFactory.create(sql);
        } catch (Exception e) {
            LOGGER.error("Invalid SPARQL query", e);
            throw new SQLException("Not a valid SPARQL query", e);
        }

        if (q == null)
            throw new SQLException("Unable to create a SPARQL Query");
        if (this.executeQuery(q)) {
            return this.currResults;
        } else {
            throw new SQLException("Query did not produce a result set");
        }
    }

    @Override
    public final int executeUpdate(String sql) throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");
        if (this.connection.isReadOnly())
            throw new SQLException("The JDBC connection is currently in read-only mode, updates are not permitted");

        // Pre-process the command text
        LOGGER.info("Received input command text:\n {}", sql);
        sql = this.connection.applyPreProcessors(sql);
        LOGGER.info("Command text after pre-processing:\n {}", sql);

        UpdateRequest u = null;
        try {
            u = UpdateFactory.create(sql);
        } catch (Exception e) {
            LOGGER.error("Invalid SPARQL update", e);
            throw new SQLException("Not a valid SPARQL Update", e);
        }

        if (u == null)
            throw new SQLException("Unable to create a SPARQL Update Request");
        return this.executeUpdate(u);
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return this.executeUpdate(sql);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return this.executeUpdate(sql);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return this.executeUpdate(sql);
    }

    @Override
    public final Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return this.fetchDirection;
    }

    @Override
    public int getFetchSize() throws SQLException {
        return this.fetchSize;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return NO_LIMIT;
    }

    @Override
    public int getMaxRows() throws SQLException {
        return maxRows;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");

        if (this.currResults != null) {
            this.currResults.close();
            this.currResults = null;
        }
        if (!this.results.isEmpty()) {
            this.currResults = this.results.poll();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");

        switch (current) {
        case Statement.CLOSE_CURRENT_RESULT:
            return this.getMoreResults();
        case Statement.CLOSE_ALL_RESULTS:
            for (ResultSet rset : this.openResults) {
                rset.close();
            }
            this.openResults.clear();
            return this.getMoreResults();
        case Statement.KEEP_CURRENT_RESULT:
            if (this.currResults != null) {
                this.openResults.add(this.currResults);
                this.currResults = null;
            }
            return this.getMoreResults();
        default:
            throw new SQLFeatureNotSupportedException(
                    "Unsupported mode for dealing with current results, only Statement.CLOSE_CURRENT_RESULT, Statement.CLOSE_ALL_RESULTS and Statement.KEEP_CURRENT_RESULT are supported");
        }
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return this.timeout;
    }

    @Override
    public final ResultSet getResultSet() throws SQLException {
        if (this.isClosed())
            throw new SQLException("The Statement is closed");
        return this.currResults;
    }

    /**
     * Helper method for use in execute() method implementations to set the
     * current results
     * 
     * @param results
     *            Results
     * @throws SQLException
     *             Thrown if there is an error closing the previous results
     */
    protected void setCurrentResults(ResultSet results) throws SQLException {
        if (this.currResults != null) {
            this.currResults.close();
        }
        this.currResults = results;
    }

    /**
     * Gets that result sets are read-only
     */
    @Override
    public final int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return this.holdability;
    }

    protected void checkHoldability(int h) throws SQLException {
        switch (h) {
        case ResultSet.CLOSE_CURSORS_AT_COMMIT:
        case ResultSet.HOLD_CURSORS_OVER_COMMIT:
            return;
        default:
            throw new SQLException(String.format("Holdability %d is supported for Jena JDBC statements", h));
        }
    }

    @Override
    public final int getResultSetType() throws SQLException {
        return this.type;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return this.updateCount;
    }

    /**
     * Helper method which derived classes may use to set the update count
     * 
     * @param count
     *            Update Count
     */
    protected void setUpdateCount(int count) {
        this.updateCount = count;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.warnings;
    }

    /**
     * Helper method that derived classes may use to set warnings
     * 
     * @param warning
     *            Warning
     */
    protected void setWarning(SQLWarning warning) {
        LOGGER.warn("SQL Warning was issued", warning);
        if (this.warnings == null) {
            this.warnings = warning;
        } else {
            // Chain with existing warnings
            warning.setNextWarning(this.warnings);
            this.warnings = warning;
        }
    }

    /**
     * Helper method that derived classes may use to set warnings
     * 
     * @param warning
     *            Warning
     */
    protected void setWarning(String warning) {
        this.setWarning(new SQLWarning(warning));
    }

    /**
     * Helper method that derived classes may use to set warnings
     * 
     * @param warning
     *            Warning
     * @param cause
     *            Cause
     */
    protected void setWarning(String warning, Throwable cause) {
        this.setWarning(new SQLWarning(warning, cause));
    }

    @Override
    public final boolean isClosed() throws SQLException {
        return this.closed;
    }

    @Override
    public final boolean isPoolable() throws SQLException {
        return true;
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        this.escapeProcessing = enable;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        this.checkFetchDirection(direction);
        this.fetchDirection = direction;
    }

    /**
     * Helper method which checks whether a given fetch direction is valid
     * throwing an error if it is not
     * 
     * @param dir
     *            Fetch Direction
     * @throws SQLException
     *             Thrown if the direction is not valid
     */
    protected void checkFetchDirection(int dir) throws SQLException {
        switch (dir) {
        case ResultSet.FETCH_FORWARD:
            return;
        default:
            throw new SQLFeatureNotSupportedException("Only ResultSet.FETCH_FORWARD is supported as a fetch direction");
        }
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        this.fetchSize = rows;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        // Ignored
        this.setWarning("setMaxFieldSize() was called but there is no field size limit for Jena JDBC connections");
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        if (max <= NO_LIMIT) {
            this.maxRows = NO_LIMIT;
        } else {
            this.maxRows = max;
        }
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        // Ignored
        this.setWarning("setPoolable() was called but Jena JDBC statements are always considered poolable");
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        if (seconds <= NO_LIMIT) {
            this.timeout = NO_LIMIT;
        } else {
            this.timeout = seconds;
        }
    }

    // Java 6/7 compatibility
    @SuppressWarnings("javadoc")
    public boolean isCloseOnCompletion() throws SQLException {
        // Statements do not automatically close
        return false;
    }

    @SuppressWarnings("javadoc")
    public void closeOnCompletion() throws SQLException {
        // We don't support the JDBC 4.1 feature of closing statements
        // automatically
        throw new SQLFeatureNotSupportedException();
    }
}
