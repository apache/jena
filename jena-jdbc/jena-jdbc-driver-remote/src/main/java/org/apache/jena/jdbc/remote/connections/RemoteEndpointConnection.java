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

package org.apache.jena.jdbc.remote.connections;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.remote.metadata.RemoteEndpointMetadata;
import org.apache.jena.jdbc.remote.statements.RemoteEndpointPreparedStatement;
import org.apache.jena.jdbc.remote.statements.RemoteEndpointStatement;
import org.apache.jena.jdbc.statements.JenaPreparedStatement;
import org.apache.jena.jdbc.statements.JenaStatement;

/**
 * Represents a connection to a remote endpoint
 * 
 */
public class RemoteEndpointConnection extends JenaConnection {

    private String queryService, updateService;
    private boolean closed = false;
    private boolean readonly = false;
    private List<String> defaultGraphUris;
    private List<String> namedGraphUris;
    private List<String> usingGraphUris;
    private List<String> usingNamedGraphUris;
    private HttpAuthenticator authenticator;
    private DatabaseMetaData metadata;
    private String selectResultsType, modelResultsType;

    /**
     * Creates a new remote connection
     * 
     * @param queryEndpoint
     *            SPARQL Query Endpoint
     * @param updateEndpoint
     *            SPARQL Update Endpoint
     * @param holdability
     *            Result Set holdability
     * @param compatibilityLevel
     *            JDBC compatibility level, see {@link JdbcCompatibility}
     * @throws SQLException
     *             Thrown if there is a problem creating the connection
     */
    public RemoteEndpointConnection(String queryEndpoint, String updateEndpoint, int holdability, int compatibilityLevel)
            throws SQLException {
        this(queryEndpoint, updateEndpoint, null, null, null, null, null, holdability, compatibilityLevel, null, null);
    }

    /**
     * Creates a new remote connection
     * 
     * @param queryEndpoint
     *            SPARQL Query Endpoint
     * @param updateEndpoint
     *            SPARQL Update Endpoint
     * @param defaultGraphUris
     *            Default Graph URIs for SPARQL queries
     * @param namedGraphUris
     *            Named Graph URIs for SPARQL queries
     * @param usingGraphUris
     *            Default Graph URIs for SPARQL updates
     * @param usingNamedGraphUris
     *            Named Graph URIs for SPARQL updates
     * @param authenticator
     *            HTTP Authenticator
     * @param holdability
     *            Result Set holdability
     * @param compatibilityLevel
     *            JDBC compatibility level, see {@link JdbcCompatibility}
     * @param selectResultsType
     *            Results Type to request for {@code SELECT} queries against the
     *            remote endpoint
     * @param modelResultsType
     *            Results Type to request for {@code CONSTRUCT} and
     *            {@code DESCRIBE} queries against the remote endpoint
     * @throws SQLException
     *             Thrown if there is a problem creating the connection
     */
    public RemoteEndpointConnection(String queryEndpoint, String updateEndpoint, List<String> defaultGraphUris,
            List<String> namedGraphUris, List<String> usingGraphUris, List<String> usingNamedGraphUris,
            HttpAuthenticator authenticator, int holdability, int compatibilityLevel, String selectResultsType,
            String modelResultsType) throws SQLException {
        super(holdability, true, Connection.TRANSACTION_NONE, compatibilityLevel);
        if (queryEndpoint == null && updateEndpoint == null)
            throw new SQLException("Must specify one/both of a query endpoint and update endpoint");
        this.queryService = queryEndpoint;
        this.updateService = updateEndpoint;
        this.readonly = this.updateService == null;
        this.defaultGraphUris = defaultGraphUris;
        this.namedGraphUris = namedGraphUris;
        this.usingGraphUris = usingGraphUris;
        this.usingNamedGraphUris = usingNamedGraphUris;
        this.authenticator = authenticator;
        this.metadata = new RemoteEndpointMetadata(this);
        this.selectResultsType = selectResultsType;
        this.modelResultsType = modelResultsType;
    }

    /**
     * Gets the SPARQL query endpoint that is in use
     * 
     * @return Endpoint URI or null for write only connections
     */
    public String getQueryEndpoint() {
        return this.queryService;
    }

    /**
     * Gets the SPARQL update endpoint that is in use
     * 
     * @return Endpoint URI or null for read-only connections
     */
    public String getUpdateEndpoint() {
        return this.updateService;
    }

    /**
     * Gets the default graphs for SPARQL queries (may be null if none were set)
     * 
     * @return Default graphs
     */
    public List<String> getDefaultGraphURIs() {
        return this.defaultGraphUris;
    }

    /**
     * Gets the named graphs for SPARQL queries (may be null if none were set)
     * 
     * @return Named graphs
     */
    public List<String> getNamedGraphURIs() {
        return this.namedGraphUris;
    }

    /**
     * Gets the default graphs for SPARQL updates (may be null if none were set)
     * 
     * @return Default graphs
     */
    public List<String> getUsingGraphURIs() {
        return this.usingGraphUris;
    }

    /**
     * Gets the named graphs for SPARQL updates (may be null if none were set)
     * 
     * @return Named graphs
     */
    public List<String> getUsingNamedGraphURIs() {
        return this.usingNamedGraphUris;
    }

    /**
     * Gets the results type that will be requested from the remote endpoint for
     * {@code SELECT} queries
     * 
     * @return Select results type if set, otherwise null which indicates that
     *         the ARQ default will be used
     */
    public String getSelectResultsType() {
        return this.selectResultsType;
    }

    /**
     * Gets the results type that will be requested from the remote endpoint for
     * {@code CONSTRUCT} and {@code DESCRIBE} queries.
     * 
     * @return Model results type if set, otherwise null which indicates that
     *         the ARQ default will be used
     */
    public String getModelResultsType() {
        return this.modelResultsType;
    }

    @Override
    protected void closeInternal() throws SQLException {
        this.closed = true;
    }

    @Override
    protected JenaStatement createStatementInternal(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
            throw new SQLFeatureNotSupportedException(
                    "Remote endpoint backed connection do not support scroll sensitive result sets");
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY)
            throw new SQLFeatureNotSupportedException("Remote endpoint backed connections only support read-only result sets");
        return new RemoteEndpointStatement(this, this.authenticator, resultSetType, ResultSet.FETCH_FORWARD, 0,
                resultSetHoldability);
    }

    @Override
    protected JenaPreparedStatement createPreparedStatementInternal(String sparql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Cannot create a statement after the connection was closed");
        if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
            throw new SQLFeatureNotSupportedException(
                    "Remote endpoint backed connection do not support scroll sensitive result sets");
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY)
            throw new SQLFeatureNotSupportedException("Remote endpoint backed connections only support read-only result sets");
        return new RemoteEndpointPreparedStatement(sparql, this, this.authenticator, resultSetType, ResultSet.FETCH_FORWARD, 0,
                resultSetHoldability);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.closed;
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
        if (readOnly) {
            this.readonly = readOnly;
        } else {
            if (this.updateService == null)
                throw new SQLException("Cannot set connection to read/write as it was created without an update endpoint");
            this.readonly = readOnly;
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.metadata;
    }

    @Override
    protected void commitInternal() throws SQLException {
        // No-op
    }

    @Override
    protected void rollbackInternal() throws SQLException {
        // No-op
    }

    @Override
    protected void checkTransactionIsolation(int level) throws SQLException {
        switch (level) {
        case Connection.TRANSACTION_NONE:
            return;
        default:
            throw new SQLFeatureNotSupportedException("Transactions are not supported for remote endpoint backed connections");
        }
    }

    /**
     * Gets whether any HTTP authenticator has been provided. Note that the
     * provision of an authenticator does not guarantee authentication since
     * that will be down to the configuration of the given authenticator.
     * 
     * @return True if an authenticator is provided, false otherwise
     */
    public boolean isUsingAuthentication() {
        return this.authenticator != null;
    }
}
