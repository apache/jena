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

package org.apache.jena.jdbc.connections;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLRecoverableException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.metadata.JenaMetadata;
import org.apache.jena.jdbc.postprocessing.ResultsPostProcessor;
import org.apache.jena.jdbc.preprocessing.CommandPreProcessor;
import org.apache.jena.jdbc.results.metadata.AskResultsMetadata;
import org.apache.jena.jdbc.results.metadata.SelectResultsMetadata;
import org.apache.jena.jdbc.results.metadata.TripleResultsMetadata;
import org.apache.jena.jdbc.statements.JenaPreparedStatement;
import org.apache.jena.jdbc.statements.JenaStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Abstract base implementation of a Jena JDBC connection
 * <p>
 * Generally speaking this is a faithful implementation of a JDBC connection but
 * it also provides a couple of Jena JDBC specific features:
 * </p>
 * <ol>
 * <li>JDBC compatibility level</li>
 * <li>Command pre-processors</li>
 * </ol>
 * <p>
 * The JDBC compatibility level allows the API to behave slightly differently
 * depending on how JDBC like you need it to be, see {@link JdbcCompatibility}
 * for more discussion on this.
 * </p>
 * <p>
 * Command pre-processors are an extension mechanism designed to allow Jena JDBC
 * connections to cope with the fact that the tools consuming the API may be
 * completely unaware that we speak SPARQL rather than SQL. They allow for
 * manipulation of incoming command text as well as manipulation of the parsed
 * SPARQL queries and updates as desired.
 * </p>
 */
public abstract class JenaConnection implements Connection {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenaConnection.class);

    /**
     * Constant for default cursor holdability for Jena JDBC connections
     */
    public static final int DEFAULT_HOLDABILITY = ResultSet.CLOSE_CURSORS_AT_COMMIT;
    /**
     * Constant for default auto-commit for Jena JDBC connections
     */
    public final static boolean DEFAULT_AUTO_COMMIT = true;
    /**
     * Constant for default transaction isolation level for Jena JDBC
     * connections
     */
    public final static int DEFAULT_ISOLATION_LEVEL = TRANSACTION_NONE;

    private Properties clientInfo = new Properties();
    private int holdability = DEFAULT_HOLDABILITY;
    private SQLWarning warnings = null;
    private boolean autoCommit = DEFAULT_AUTO_COMMIT;
    private int isolationLevel = DEFAULT_ISOLATION_LEVEL;
    private int compatibilityLevel = JdbcCompatibility.DEFAULT;
    private List<Statement> statements = new ArrayList<Statement>();
    private List<CommandPreProcessor> preProcessors = new ArrayList<CommandPreProcessor>();
    private List<ResultsPostProcessor> postProcessors = new ArrayList<ResultsPostProcessor>();

    /**
     * Creates a new connection
     * 
     * @param holdability
     *            Cursor holdability
     * @param autoCommit
     *            Sets auto-commit behaviour for the connection
     * @param transactionLevel
     *            Sets transaction isolation level for the connection
     * @param compatibilityLevel
     *            Sets JDBC compatibility level for the connection, see
     *            {@link JdbcCompatibility}
     * @throws SQLException
     *             Thrown if the arguments are invalid
     */
    public JenaConnection(int holdability, boolean autoCommit, int transactionLevel, int compatibilityLevel) throws SQLException {
        this.checkHoldability(holdability);
        this.holdability = holdability;
        this.setAutoCommit(autoCommit);
        this.setTransactionIsolation(transactionLevel);
        this.compatibilityLevel = JdbcCompatibility.normalizeLevel(compatibilityLevel);
    }

    /**
     * Gets the JDBC compatibility level that is in use, see
     * {@link JdbcCompatibility} for explanations
     * 
     * @return Compatibility level
     */
    public int getJdbcCompatibilityLevel() {
        return this.compatibilityLevel;
    }

    /**
     * Sets the JDBC compatibility level that is in use, see
     * {@link JdbcCompatibility} for explanations.
     * <p>
     * Changing the level may not effect existing open objects, behaviour in
     * this case will be implementation specific.
     * </p>
     * 
     * @param level
     *            Compatibility level
     */
    public void setJdbcCompatibilityLevel(int level) {
        this.compatibilityLevel = JdbcCompatibility.normalizeLevel(level);
    }

    /**
     * Adds a command pre-processor to the connection
     * 
     * @param preProcessor
     *            Pre-processor to add
     */
    public final void addPreProcessor(CommandPreProcessor preProcessor) {
        if (preProcessor == null)
            return;
        this.preProcessors.add(preProcessor);
    }

    /**
     * Adds a results post-processor to the connection
     * 
     * @param postProcessor
     *            Post-processor to add
     */
    public final void addPostProcessor(ResultsPostProcessor postProcessor) {
        if (postProcessor == null)
            return;
        this.postProcessors.add(postProcessor);
    }

    /**
     * Inserts a command pre-processor for the connection
     * 
     * @param index
     *            Index to insert at
     * @param preProcessor
     *            Pre-processor
     */
    public final void insertPreProcessor(int index, CommandPreProcessor preProcessor) {
        if (preProcessor == null)
            return;
        this.preProcessors.add(index, preProcessor);
    }

    /**
     * Inserts a results post-processor for the connection
     * 
     * @param index
     *            Index to insert at
     * @param postProcessor
     *            Post-processor
     */
    public final void insertPostProcessor(int index, ResultsPostProcessor postProcessor) {
        if (postProcessor == null)
            return;
        this.postProcessors.add(index, postProcessor);
    }

    /**
     * Removes a command pre-processor from the connection
     * 
     * @param preProcessor
     *            Pre-processor to remove
     */
    public final void removePreProcessor(CommandPreProcessor preProcessor) {
        if (preProcessor == null)
            return;
        this.preProcessors.remove(preProcessor);
    }

    /**
     * Removes a results post-processor from the connection
     * 
     * @param postProcessor
     *            Post-processor to remove
     */
    public final void removePostProcessor(ResultsPostProcessor postProcessor) {
        if (postProcessor == null)
            return;
        this.postProcessors.remove(postProcessor);
    }

    /**
     * Removes a command pre-processor from the connection
     * 
     * @param index
     *            Index to remove at
     */
    public final void removePreProcessor(int index) {
        this.preProcessors.remove(index);
    }

    /**
     * Removes a results post-processor from the connection
     * 
     * @param index
     *            Index to remove at
     */
    public final void removePostProcessor(int index) {
        this.postProcessors.remove(index);
    }

    /**
     * Clears all command pre-processor from the connection
     */
    public final void clearPreProcessors() {
        this.preProcessors.clear();
    }

    /**
     * Clears all command post-processors from the connection
     */
    public final void clearPostProcessors() {
        this.postProcessors.clear();
    }

    /**
     * Gets the currently registered pre-processors for the connection
     * 
     * @return Iterator of pre-processors
     */
    public final Iterator<CommandPreProcessor> getPreProcessors() {
        return this.preProcessors.iterator();
    }

    /**
     * Gets the currently registered post-processors for the connection
     * 
     * @return Iterator of post-processors
     */
    public final Iterator<ResultsPostProcessor> getPostProcessors() {
        return this.postProcessors.iterator();
    }

    /**
     * Apply registered pre-processors to the given command text
     * 
     * @param text
     *            Command Text
     * @return Command Text after processing by registered pre-processors
     * @throws SQLException
     */
    public final String applyPreProcessors(String text) throws SQLException {
        for (CommandPreProcessor preProcessor : this.preProcessors) {
            if (preProcessor == null)
                continue;
            text = preProcessor.preProcessCommandText(text);
        }
        return text;
    }

    /**
     * Applies registered pre-processors to the given query
     * 
     * @param q
     *            Query
     * @return Query after processing by registered pre-processors
     * @throws SQLException
     */
    public final Query applyPreProcessors(Query q) throws SQLException {
        for (CommandPreProcessor preProcessor : this.preProcessors) {
            if (preProcessor == null)
                continue;
            q = preProcessor.preProcessQuery(q);
        }
        return q;
    }

    /**
     * Applies registered pre-processors to the given update
     * 
     * @param u
     *            Update
     * @return Update after processing by registered pre-processors
     * @throws SQLException
     */
    public final UpdateRequest applyPreProcessors(UpdateRequest u) throws SQLException {
        for (CommandPreProcessor preProcessor : this.preProcessors) {
            if (preProcessor == null)
                continue;
            u = preProcessor.preProcessUpdate(u);
        }
        return u;
    }

    /**
     * Applies registered post-processors to the given results
     * 
     * @param results
     *            Results
     * @return Results after processing by registered post-processors
     * @throws SQLException
     */
    public final com.hp.hpl.jena.query.ResultSet applyPostProcessors(com.hp.hpl.jena.query.ResultSet results) throws SQLException {
        for (ResultsPostProcessor postProcessor : this.postProcessors) {
            if (postProcessor == null)
                continue;
            results = postProcessor.postProcessResults(results);
        }
        return results;
    }

    /**
     * Applies registered post-processors to the given results
     * 
     * @param triples
     *            Results
     * @return Results after processing by registered post-processors
     * @throws SQLException
     */
    public final Iterator<Triple> applyPostProcessors(Iterator<Triple> triples) throws SQLException {
        for (ResultsPostProcessor postProcessor : this.postProcessors) {
            if (postProcessor == null)
                continue;
            triples = postProcessor.postProcessResults(triples);
        }
        return triples;
    }

    /**
     * Applies registered post-processors to the given results
     * 
     * @param result
     *            Result
     * @return Result after processing by registered post-processors
     * @throws SQLException
     */
    public final boolean applyPostProcessors(boolean result) throws SQLException {
        for (ResultsPostProcessor postProcessor : this.postProcessors) {
            if (postProcessor == null)
                continue;
            result = postProcessor.postProcessResults(result);
        }
        return result;
    }

    /**
     * Applies registered post-processors to the given results metadata
     * 
     * @param metadata
     *            Results metadata
     * @return Results metadata after processing by registered post-processors
     * @throws SQLException
     */
    public final SelectResultsMetadata applyPostProcessors(SelectResultsMetadata metadata) throws SQLException {
        for (ResultsPostProcessor postProcessor : this.postProcessors) {
            if (postProcessor == null)
                continue;
            metadata = postProcessor.postProcessResultsMetadata(metadata);
        }
        return metadata;
    }

    /**
     * Applies registered post-processors to the given results metadata
     * 
     * @param metadata
     *            Results metadata
     * @return Results metadata after processing by registered post-processors
     * @throws SQLException
     */
    public final TripleResultsMetadata applyPostProcessors(TripleResultsMetadata metadata) throws SQLException {
        for (ResultsPostProcessor postProcessor : this.postProcessors) {
            if (postProcessor == null)
                continue;
            metadata = postProcessor.postProcessResultsMetadata(metadata);
        }
        return metadata;
    }

    /**
     * Applies registered post-processors to the given results metadata
     * 
     * @param metadata
     *            Results metadata
     * @return Results metadata after processing by registered post-processors
     * @throws SQLException
     */
    public final AskResultsMetadata applyPostProcessors(AskResultsMetadata metadata) throws SQLException {
        for (ResultsPostProcessor postProcessor : this.postProcessors) {
            if (postProcessor == null)
                continue;
            metadata = postProcessor.postProcessResultsMetadata(metadata);
        }
        return metadata;
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.warnings = null;
    }

    @Override
    public final void close() throws SQLException {
        try {
            LOGGER.info("Closing connection...");
            // Close any open statements
            this.closeStatements();
        } finally {
            this.closeInternal();
            LOGGER.info("Connection was closed");
        }
    }

    private void closeStatements() throws SQLException {
        synchronized (this.statements) {
            if (this.statements.size() > 0) {
                LOGGER.info("Attempting to close " + this.statements.size() + " open statements");
                for (Statement stmt : this.statements) {
                    stmt.close();
                }
                LOGGER.info("All open statements were closed");
                this.statements.clear();
            }
        }
    }

    protected abstract void closeInternal() throws SQLException;

    @Override
    public void commit() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot commit on a closed connection");
        try {
            LOGGER.info("Attempting to commit a transaction...");

            // Get the implementation to do the actual commit
            this.commitInternal();

            LOGGER.info("Transaction was committed");

            // If applicable close cursors
            if (this.holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
                LOGGER.info("Holdability set to CLOSE_CURSORS_AT_COMMIT so closing open statements");
                this.closeStatements();
            }
        } catch (SQLException e) {
            // Throw as-is
            throw e;
        } catch (Exception e) {
            // Wrap as SQLException
            LOGGER.error("Unexpected error in transaction commit", e);
            throw new SQLException("Unexpected error committing transaction", e);
        }
    }

    protected abstract void commitInternal() throws SQLException;

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public final Statement createStatement() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        return this.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    public final Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        return this.createStatement(resultSetType, resultSetConcurrency, this.getHoldability());
    }

    @Override
    public final Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        JenaStatement stmt = this.createStatementInternal(resultSetType, resultSetConcurrency, resultSetHoldability);
        synchronized (this.statements) {
            this.statements.add(stmt);
        }
        return stmt;
    }

    protected abstract JenaStatement createStatementInternal(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException;

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return this.autoCommit;
    }

    @Override
    public String getCatalog() throws SQLException {
        return JenaMetadata.DEFAULT_CATALOG;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return this.clientInfo;
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return this.clientInfo.getProperty(name);
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.holdability;
    }

    @Override
    public abstract DatabaseMetaData getMetaData() throws SQLException;

    @Override
    public int getTransactionIsolation() throws SQLException {
        return this.isolationLevel;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.warnings;
    }

    @Override
    public abstract boolean isClosed() throws SQLException;

    @Override
    public abstract boolean isReadOnly() throws SQLException;

    @Override
    public abstract boolean isValid(int timeout) throws SQLException;

    @Override
    public String nativeSQL(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        return this.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        return this.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        return this.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        return this.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        return this.prepareStatement(sql, resultSetType, resultSetConcurrency, this.holdability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        return this.createPreparedStatementInternal(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Helper method which derived implementations must implement to provide an
     * actual prepared statement implementation
     * 
     * @param sql
     *            SPARQL command
     * @param resultSetType
     *            Desired result set type
     * @param resultSetConcurrency
     *            Result set concurrency
     * @param resultSetHoldability
     *            Result set holdability
     * @return Prepared statement
     * @throws SQLException
     */
    protected abstract JenaPreparedStatement createPreparedStatementInternal(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability) throws SQLException;

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void rollback() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot rollback on a closed connection");
        try {
            LOGGER.info("Attempting to rollback a transaction...");

            // Get the implementation to do the actual rollback
            this.rollbackInternal();

            LOGGER.info("Transaction was rolled back");

            // Close any open statements if applicable
            if (this.holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
                LOGGER.info("Holdability is set to CLOSE_CURSORS_AT_COMMIT so closing open statements");
                this.closeStatements();
            }
        } catch (SQLException e) {
            // Throw as-is
            throw e;
        } catch (Exception e) {
            throw new SQLException("Unexpected error rolling back transaction", e);
        }
    }

    protected abstract void rollbackInternal() throws SQLException;

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        this.clientInfo = properties;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        this.clientInfo.put(name, value);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        this.checkHoldability(holdability);
        this.holdability = holdability;
    }

    /**
     * Helper method that checks whether the given holdability setting is valid,
     * throws an error if it is not
     * 
     * @param h
     *            Holdability Setting
     * @throws SQLException
     *             Thrown if the setting is not valid
     */
    protected void checkHoldability(int h) throws SQLException {
        switch (h) {
        case ResultSet.CLOSE_CURSORS_AT_COMMIT:
        case ResultSet.HOLD_CURSORS_OVER_COMMIT:
            return;
        default:
            throw new SQLRecoverableException(String.format("%d is not a valid holdability setting", h));
        }
    }

    @Override
    public abstract void setReadOnly(boolean readOnly) throws SQLException;

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        this.checkTransactionIsolation(level);
        this.isolationLevel = level;
    }

    /**
     * Helper method which checks that a transaction isolation level is valid,
     * should throw an exception if the given level is not valid for the
     * connection
     * 
     * @param level
     *            Isolation Level
     * @throws SQLException
     *             Thrown if the isolation level is not valid
     */
    protected abstract void checkTransactionIsolation(int level) throws SQLException;

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
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

	//--- Java6/7 compatibility.
	public void setSchema(String schema) throws SQLException {
		throw new SQLFeatureNotSupportedException();	
	}

	public String getSchema() throws SQLException {
		throw new SQLFeatureNotSupportedException();	
	}

	public void abort(java.util.concurrent.Executor executor) throws SQLException {
		throw new SQLFeatureNotSupportedException();	
	}

	public int getNetworkTimeout() throws SQLException {
		throw new SQLFeatureNotSupportedException();	
	}

	public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException {
		throw new SQLFeatureNotSupportedException();	
	}
}
