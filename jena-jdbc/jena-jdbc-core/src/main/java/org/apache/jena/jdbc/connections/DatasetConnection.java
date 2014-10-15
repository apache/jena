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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.statements.DatasetPreparedStatement;
import org.apache.jena.jdbc.statements.DatasetStatement;
import org.apache.jena.jdbc.statements.JenaPreparedStatement;
import org.apache.jena.jdbc.statements.JenaStatement;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;

/**
 * Represents a connection to a {@link Dataset} instance
 * 
 */
public abstract class DatasetConnection extends JenaConnection {

    protected Dataset ds;
    private boolean readonly = false;

    private ThreadLocal<ReadWrite> transactionType = new ThreadLocal<ReadWrite>();
    private ThreadLocal<Integer> transactionParticipants = new ThreadLocal<Integer>();

    /**
     * Creates a new dataset connection
     * 
     * @param ds
     *            Dataset
     * @param holdability
     *            Cursor holdability
     * @param autoCommit
     *            Sets auto-commit behavior for the connection
     * @param transactionLevel
     *            Sets transaction isolation level for the connection
     * @param compatibilityLevel
     *            Sets JDBC compatibility level for the connection, see
     *            {@link JdbcCompatibility}
     * @throws SQLException
     */
    public DatasetConnection(Dataset ds, int holdability, boolean autoCommit, int transactionLevel, int compatibilityLevel)
            throws SQLException {
        super(holdability, autoCommit, transactionLevel, compatibilityLevel);
        this.ds = ds;
    }

    /**
     * Gets the dataset to which this connection pertains
     * 
     * @return Dataset
     */
    public final Dataset getJenaDataset() {
        return this.ds;
    }

    @Override
    protected void closeInternal() throws SQLException {
        try {
            if (this.ds != null) {
                ds.close();
                ds = null;
            }
        } catch (Exception e) {
            throw new SQLException("Unexpected error closing a dataset backed connection", e);
        } finally {
            this.ds = null;
        }
    }

    @Override
    protected JenaStatement createStatementInternal(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
            throw new SQLFeatureNotSupportedException("Dataset backed connections do not support scroll sensitive result sets");
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY)
            throw new SQLFeatureNotSupportedException("Dataset backed connections only supports read-only result sets");
        return new DatasetStatement(this, resultSetType, ResultSet.FETCH_FORWARD, 0, resultSetHoldability, this.getAutoCommit(),
                this.getTransactionIsolation());
    }

    @Override
    protected JenaPreparedStatement createPreparedStatementInternal(String sparql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
            throw new SQLFeatureNotSupportedException("Dataset backed connections do not support scroll sensitive result sets");
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY)
            throw new SQLFeatureNotSupportedException("Dataset backed connections only supports read-only result sets");
        return new DatasetPreparedStatement(sparql, this, resultSetType, ResultSet.FETCH_FORWARD, 0, resultSetHoldability,
                this.getAutoCommit(), this.getTransactionIsolation());
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.ds == null;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.readonly;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return !this.isClosed();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot set read-only mode on a closed connection");
        this.readonly = readOnly;
    }

    @Override
    protected void checkTransactionIsolation(int level) throws SQLException {
        switch (level) {
        case TRANSACTION_NONE:
            return;
        case TRANSACTION_SERIALIZABLE:
            // Serializable is supported if the dataset supports transactions
            if (this.ds != null)
                if (this.ds.supportsTransactions())
                    return;
            // Otherwise we'll drop through and throw the error
        default:
            throw new SQLException(String.format("The Transaction level %d is not supported by this connection", level));
        }
    }

    /**
     * Begins a new transaction
     * <p>
     * Transactions are typically thread scoped and are shared by each thread so
     * if there is an existing read transaction and another thread tries to
     * start a read transaction it will join the existing read transaction.
     * Trying to join a transaction not of the same type will produce an error.
     * </p>
     * 
     * @param type
     * @throws SQLException
     */
    public synchronized void begin(ReadWrite type) throws SQLException {
        try {
            if (this.isClosed())
                throw new SQLException("Cannot start a transaction on a closed connection");
            if (this.getTransactionIsolation() == Connection.TRANSACTION_NONE)
                throw new SQLException("Cannot start a transaction when transaction isolation is set to NONE");
            if (ds.supportsTransactions()) {
                if (ds.isInTransaction()) {
                    // Additional participant in existing transaction
                    ReadWrite currType = this.transactionType.get();
                    if (currType.equals(type)) {
                        this.transactionParticipants.set(this.transactionParticipants.get() + 1);
                    } else {
                        throw new SQLException(
                                "Unable to start a transaction of a different type on the same thread as an existing transaction, please retry your operation on a different thread");
                    }
                } else {
                    // Starting a new transaction
                    this.transactionType.set(type);
                    this.transactionParticipants.set(1);
                    this.ds.begin(type);
                }
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Unexpected error starting a transaction", e);
        }
    }

    @Override
    protected synchronized void commitInternal() throws SQLException {
        try {
            if (ds.supportsTransactions()) {
                if (ds.isInTransaction()) {
                    // How many participants are there in this transaction?
                    int participants = this.transactionParticipants.get();
                    if (participants > 1) {
                        // Transaction should remain active
                        this.transactionParticipants.set(participants - 1);
                    } else {
                        // Now safe to commit
                        ds.commit();
                        ds.end();
                        this.transactionParticipants.remove();
                        this.transactionType.remove();
                    }
                } else {
                    throw new SQLException("Attempted to commit a transaction when there was no active transaction");
                }
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Unexpected error committing the transaction", e);
        }
    }

    @Override
    protected synchronized void rollbackInternal() throws SQLException {
        try {
            if (ds.supportsTransactions()) {
                if (ds.isInTransaction()) {
                    // Regardless of participants a rollback is always immediate
                    ds.abort();
                    ds.end();
                    this.transactionType.remove();
                    this.transactionParticipants.remove();
                } else {
                    throw new SQLException("Attempted to rollback a transaction when there was no active transaction");
                }
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Unexpected error rolling back the transaction", e);
        }
    }
}
