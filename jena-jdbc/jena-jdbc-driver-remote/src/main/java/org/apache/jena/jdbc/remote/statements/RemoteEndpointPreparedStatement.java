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

package org.apache.jena.jdbc.remote.statements;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection;
import org.apache.jena.jdbc.statements.JenaPreparedStatement;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.modify.UpdateProcessRemoteBase;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * A Jena JDBC statement against a remote endpoint
 * 
 */
public class RemoteEndpointPreparedStatement extends JenaPreparedStatement {

    private RemoteEndpointConnection remoteConn;
    private HttpAuthenticator authenticator;

    /**
     * Creates a new statement
     * 
     * @param sparql
     *            SPARQL command
     * @param connection
     *            Connection
     * @throws SQLException
     *             Thrown if there is an error with the statement parameters
     */
    public RemoteEndpointPreparedStatement(String sparql, RemoteEndpointConnection connection) throws SQLException {
        this(sparql, connection, null, DEFAULT_TYPE, DEFAULT_FETCH_DIRECTION, DEFAULT_FETCH_SIZE, DEFAULT_HOLDABILITY);
    }

    /**
     * Creates a new statement
     * 
     * @param sparql
     *            SPARQL command
     * @param connection
     *            Connection
     * @param authenticator
     *            HTTP Authenticator
     * @param type
     *            Result Set type for result sets produced by this statement
     * @param fetchDir
     *            Fetch Direction
     * @param fetchSize
     *            Fetch Size
     * @param holdability
     *            Result Set holdability
     * @throws SQLException
     *             Thrown if there is an error with the statement parameters
     * 
     */
    public RemoteEndpointPreparedStatement(String sparql, RemoteEndpointConnection connection, HttpAuthenticator authenticator,
            int type, int fetchDir, int fetchSize, int holdability) throws SQLException {
        super(sparql, connection, type, fetchDir, fetchSize, holdability, false, Connection.TRANSACTION_NONE);
        this.remoteConn = connection;
        this.authenticator = authenticator;
    }

    @Override
    protected QueryExecution createQueryExecution(Query q) throws SQLException {
        if (this.remoteConn.getQueryEndpoint() == null)
            throw new SQLException("This statement is backed by a write-only connection, read operations are not supported");

        // Create basic execution
        QueryEngineHTTP exec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(this.remoteConn.getQueryEndpoint(), q);

        // Apply authentication settings
        if (this.authenticator != null) {
            exec.setAuthenticator(this.authenticator);
        }

        // Apply default and named graphs if appropriate
        if (this.remoteConn.getDefaultGraphURIs() != null) {
            exec.setDefaultGraphURIs(this.remoteConn.getDefaultGraphURIs());
        }
        if (this.remoteConn.getNamedGraphURIs() != null) {
            exec.setNamedGraphURIs(this.remoteConn.getNamedGraphURIs());
        }

        // Set result types
        if (this.remoteConn.getSelectResultsType() != null) {
            exec.setSelectContentType(this.remoteConn.getSelectResultsType());
        }
        if (this.remoteConn.getModelResultsType() != null) {
            exec.setModelContentType(this.remoteConn.getModelResultsType());
        }

        // Return execution
        return exec;
    }

    @Override
    protected UpdateProcessor createUpdateProcessor(UpdateRequest u) {
        UpdateProcessRemoteBase proc = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(u,
                this.remoteConn.getUpdateEndpoint());

        // Apply authentication settings
        if (this.authenticator != null) {
            proc.setAuthenticator(this.authenticator);
        }

        // Apply default and named graphs if appropriate
        if (this.remoteConn.getUsingGraphURIs() != null) {
            proc.setDefaultGraphs(this.remoteConn.getUsingGraphURIs());
        }
        if (this.remoteConn.getNamedGraphURIs() != null) {
            proc.setNamedGraphs(this.remoteConn.getUsingNamedGraphURIs());
        }

        return proc;
    }

    @Override
    protected void beginTransaction(ReadWrite type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Transactions against remote endpoint backed connections are not supported");
    }

    @Override
    protected void commitTransaction() throws SQLException {
        throw new SQLFeatureNotSupportedException("Transactions against remote endpoint backed connections are not supported");
    }

    @Override
    protected void rollbackTransaction() throws SQLException {
        throw new SQLFeatureNotSupportedException("Transactions against remote endpoint backed connections are not supported");
    }

    @Override
    protected boolean hasActiveTransaction() throws SQLException {
        // Remote endpoints don't support transactions so can't ever have an
        // active transaction
        return false;
    }
}
