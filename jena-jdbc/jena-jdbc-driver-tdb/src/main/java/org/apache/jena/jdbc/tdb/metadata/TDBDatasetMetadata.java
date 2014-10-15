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

package org.apache.jena.jdbc.tdb.metadata;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.jena.jdbc.JenaJDBC;
import org.apache.jena.jdbc.connections.DatasetConnection;
import org.apache.jena.jdbc.metadata.DatasetMetadata;

import com.hp.hpl.jena.sparql.util.Version;
import com.hp.hpl.jena.tdb.TDB;

/**
 * Connection metadata for TDB datasets
 *
 */
public class TDBDatasetMetadata extends DatasetMetadata {
    
    private Version tdb;
    private Version jdbc;

    /**
     * Creates new metadata
     * @param connection Connection
     * @throws SQLException
     */
    public TDBDatasetMetadata(DatasetConnection connection) throws SQLException {
        super(connection);
        tdb = new Version();
        tdb.addClass(TDB.class);
        jdbc = new Version();
        jdbc.addClass(JenaJDBC.class);
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return 10;
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return "Apache Jena - TDB";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return tdb.toString(true);
    }
    
    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_SERIALIZABLE;
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
    public String getDriverName() throws SQLException {
        return "Apache Jena - JDBC - TDB Driver";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return jdbc.toString(true);
    }

    @Override
    public String getURL() throws SQLException {
        return "http://jena.apache.org";
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        // TDB database doesn't use files per table
        return false;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        // TDB database does use files
        return true;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        // TDB supports Serializable transactions
        switch (level) {
        case Connection.TRANSACTION_SERIALIZABLE:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        // TDB supports transactions
        return true;
    }

}
