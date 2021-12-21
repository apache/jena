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

import java.net.http.HttpClient;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection;
import org.apache.jena.jdbc.statements.JenaPreparedStatement;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.apache.jena.update.UpdateProcessor ;
import org.apache.jena.update.UpdateRequest ;

/**
 * A Jena JDBC statement against a remote endpoint
 *
 */
public class RemoteEndpointPreparedStatement extends JenaPreparedStatement {

    private RemoteEndpointConnection remoteConn;
    private HttpClient client;

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
     * @param client
     *            HTTP client
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
    public RemoteEndpointPreparedStatement(String sparql, RemoteEndpointConnection connection, HttpClient client,
            int type, int fetchDir, int fetchSize, int holdability) throws SQLException {
        super(sparql, connection, type, fetchDir, fetchSize, holdability, false, Connection.TRANSACTION_NONE);
        this.remoteConn = connection;
        this.client = client;
    }

    @Override
    protected QueryExecution createQueryExecution(Query q) throws SQLException {
        if (this.remoteConn.getQueryEndpoint() == null)
            throw new SQLException("This statement is backed by a write-only connection, read operations are not supported");

        // Create basic execution
        QueryExecutionHTTPBuilder exec = QueryExecutionHTTP.service(this.remoteConn.getQueryEndpoint()).query(q);

        // Apply HTTP settings
        if (this.client != null) {
            exec.httpClient(client);
        }

        // Apply default and named graphs if appropriate
        if (this.remoteConn.getDefaultGraphURIs() != null) {
            this.remoteConn.getDefaultGraphURIs().forEach(exec::addDefaultGraphURI);
        }
        if (this.remoteConn.getNamedGraphURIs() != null) {
            this.remoteConn.getNamedGraphURIs().forEach(exec::addNamedGraphURI);
        }

        // Set result types
        if (this.remoteConn.getSelectResultsType() != null) {
            exec.acceptHeader(this.remoteConn.getSelectResultsType());
        }
        if (this.remoteConn.getModelResultsType() != null) {
            exec.acceptHeader(this.remoteConn.getModelResultsType());
        }

        // Return execution
        return exec.build();
    }

    @Override
    protected UpdateProcessor createUpdateProcessor(UpdateRequest u) {
        UpdateExecutionHTTPBuilder proc = UpdateExecutionHTTP.service(this.remoteConn.getUpdateEndpoint()).update(u);

        // Apply HTTP settings
        if (this.client != null) {
            proc.httpClient(this.client);
        }

        // Apply default and named graphs if appropriate
        if (this.remoteConn.getUsingGraphURIs() != null) {
            this.remoteConn.getUsingGraphURIs().forEach(proc::addUsingGraphURI);
        }
        if (this.remoteConn.getNamedGraphURIs() != null) {
            this.remoteConn.getNamedGraphURIs().forEach(proc::addUsingNamedGraphURI);
        }

        return proc.build();
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
    protected boolean hasActiveTransaction() {
        // Remote endpoints don't support transactions so can't ever have an
        // active transaction
        return false;
    }
}
