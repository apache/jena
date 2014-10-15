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

package org.apache.jena.jdbc.mem.metadata;

import java.sql.SQLException;

import org.apache.jena.jdbc.JenaJDBC;
import org.apache.jena.jdbc.connections.DatasetConnection;
import org.apache.jena.jdbc.metadata.DatasetMetadata;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.util.Version;

/**
 * Connection metadata for in-memory datasets
 *
 */
public class MemDatasetMetadata extends DatasetMetadata {
    
    private Version arq;
    private Version jdbc;

    /**
     * Creates new metadata
     * @param connection Connection
     * @throws SQLException
     */
    public MemDatasetMetadata(DatasetConnection connection) throws SQLException {
        super(connection);
        arq = new Version();
        arq.addClass(ARQ.class);
        jdbc = new Version();
        jdbc.addClass(JenaJDBC.class);
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return 2;
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return 10;
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return "Apache Jena - ARQ - In-Memory";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return arq.toString();
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
        return "Apache Jena - JDBC - In-Memory Driver";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return jdbc.toString();
    }

    @Override
    public String getURL() throws SQLException {
        return "http://jena.apache.org";
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        // In-Memory database doesn't use files
        return false;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        // In-Memory database doesn't use files
        return false;
    }

}
