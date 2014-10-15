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

import java.sql.SQLException;

import org.apache.jena.jdbc.connections.DatasetConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * A Jena JDBC statement over a {@link Dataset}
 * 
 */
public class DatasetStatement extends JenaStatement {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetStatement.class);

    private DatasetConnection dsConn;

    /**
     * Creates a new statement
     * 
     * @param connection
     *            Connection
     * @throws SQLException
     *             Thrown if there is an error with the statement parameters
     */
    public DatasetStatement(DatasetConnection connection) throws SQLException {
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
     *            Auto commit behavior
     * @param transactionLevel
     *            Transaction level
     * @throws SQLException
     *             Thrown if there is an error with the statement parameters
     * 
     */
    public DatasetStatement(DatasetConnection connection, int type, int fetchDir, int fetchSize, int holdability, boolean autoCommit,
            int transactionLevel) throws SQLException {
        super(connection, type, fetchDir, fetchSize, holdability, autoCommit, transactionLevel);
        this.dsConn = connection;
    }

    /**
     * Creates a query execution over the dataset
     */
    @Override
    protected QueryExecution createQueryExecution(Query q) {
        return QueryExecutionFactory.create(q, this.dsConn.getJenaDataset());
    }

    /**
     * Creates an update execution over the dataset
     */
    @Override
    protected UpdateProcessor createUpdateProcessor(UpdateRequest u) {
        return UpdateExecutionFactory.create(u, GraphStoreFactory.create(this.dsConn.getJenaDataset()));
    }

    @Override
    protected void beginTransaction(ReadWrite type) throws SQLException {
        try {
            this.dsConn.begin(type);
        } catch (SQLException e) {
            // Throw as-is
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error starting a transaction", e);
            throw new SQLException("Unexpected error starting a transaction", e);
        }
    }

    @Override
    protected void commitTransaction() throws SQLException {
        try {
            this.dsConn.commit();
        } catch (SQLException e) {
            // Throw as-is
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error committing a transaction", e);
            throw new SQLException("Unexpected error committing a transaction", e);
        }
    }

    @Override
    protected void rollbackTransaction() throws SQLException {
        try {
        this.dsConn.rollback();
        } catch (SQLException e) {
            // Throw as-is
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error rolling back a transaction", e);
            throw new SQLException("Unexpected error rolling back a transaction", e);
        }
    }

    @Override
    protected boolean hasActiveTransaction() throws SQLException {
        return this.dsConn.getJenaDataset().isInTransaction();
    }

}
