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

package org.apache.jena.jdbc.mem.connections;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.DatasetConnection;
import org.apache.jena.jdbc.mem.metadata.MemDatasetMetadata;
import org.apache.jena.query.Dataset ;

/**
 * Represents a dataset connection backed by an in-memory dataset
 * 
 */
public class MemConnection extends DatasetConnection {

    private DatabaseMetaData metadata;
    
    /**
     * Creates a new connection
     * 
     * @param ds
     *            Dataset
     * @param holdability
     *            Result Set holdability
     * @param autoCommit
     *            Sets auto-commit behavior for the connection
     * @param transactionLevel
     *            Sets transaction isolation level for the connection
     * @param compatibilityLevel
     *            Sets JDBC compatibility level for the connection, see
     *            {@link JdbcCompatibility}
     * @throws SQLException
     */
    public MemConnection(Dataset ds, int holdability, boolean autoCommit, int transactionLevel, int compatibilityLevel)
            throws SQLException {
        super(ds, holdability, autoCommit, TRANSACTION_NONE, compatibilityLevel);
        // DatasetConnection extends JenaConnection
        // JenaConnection.super calls setTransactionIsolation
        // ... which calls DatasetConnection.checkTransactionIsolation
        // ... which relies on DatasetConnection.ds being set if the transactionLevel isn't TRANSACTION_NONE
        // ... which it isn't yet becaue during  JenaConnection.super, the rest of DatasetConnection.super has not run.
        // So set to TRANSACTION_NONE in super(), then set properly.
        setTransactionIsolation(transactionLevel);
        this.metadata = new MemDatasetMetadata(this);
    }

    @Override
    public DatabaseMetaData getMetaData() {
        return this.metadata;
    }
}
