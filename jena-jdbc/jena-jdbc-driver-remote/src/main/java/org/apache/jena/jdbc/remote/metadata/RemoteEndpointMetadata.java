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

package org.apache.jena.jdbc.remote.metadata;

import java.sql.Connection ;
import java.sql.SQLException ;

import org.apache.jena.atlas.lib.Version ;
import org.apache.jena.jdbc.JenaJDBC ;
import org.apache.jena.jdbc.metadata.JenaMetadata ;
import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection ;

/**
 * Represents metadata about connections to remote endpoints
 *
 */
public class RemoteEndpointMetadata extends JenaMetadata {

    private RemoteEndpointConnection remoteConn;

    /**
     * Creates new metadata
     *
     * @param connection
     *            Remote Endpoint connection
     * @throws SQLException
     */
    public RemoteEndpointMetadata(RemoteEndpointConnection connection) throws SQLException {
        super(connection);
        this.remoteConn = connection;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int isolationLevel) {
        // No transactions supported for remote endpoints
        switch (isolationLevel) {
        case Connection.TRANSACTION_NONE:
            return true;
        default:
            return false;
        }
    }

    @Override
    public int getDatabaseMajorVersion() {
        // Underlying database is unknown
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() {
        // Underlying database is unknown
        return 0;
    }

    @Override
    public String getDatabaseProductName() {
        return "";
    }

    @Override
    public String getDatabaseProductVersion() {
        // Underlying database is unknown
        return "";
    }

    @Override
    public int getDriverMajorVersion() {
        return 0;
    }

    @Override
    public int getDriverMinorVersion() {
        return 1;
    }

    @Override
    public String getDriverName() {
        return "Apache Jena - JDBC - Remote Endpoint Driver";
    }

    @Override
    public String getDriverVersion() {
        return Version.versionForClass(JenaJDBC.class).orElse("<development>");
    }

    @Override
    public String getURL() {
        // Underlying database is unknown
        return null;
    }

    @Override
    public String getUserName() {
        // Even though we may be using a HTTP authenticator that may not be
        // using a user name based login method and regardless for security
        // reasons the authenticator APIs don't expose the underlying
        // credentials in any way
        return null;
    }

    @Override
    public boolean usesLocalFilePerTable() {
        // Remote endpoints don't use local files
        return false;
    }

    @Override
    public boolean usesLocalFiles() {
        // Remote endpoints don't use local files
        return false;
    }
}
