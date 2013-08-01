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

package org.apache.jena.jdbc.tdb.connections;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.DatasetConnection;
import org.apache.jena.jdbc.tdb.metadata.TDBDatasetMetadata;

import com.hp.hpl.jena.query.Dataset;

/**
 * Represents a dataset connection backed by a TDB dataset
 *
 */
public class TDBConnection extends DatasetConnection {

    private DatabaseMetaData metadata;
    
    /**
     * Creates a new connection
     * @param ds Dataset
     * @param holdability Result Set holdability
     * @param autoCommit Auto-commit mode
     * @param compatibilityLevel JDBC compatability level, see {@link JdbcCompatibility}
     * @throws SQLException
     */
    public TDBConnection(Dataset ds, int holdability, boolean autoCommit, int compatibilityLevel) throws SQLException {
        super(ds, holdability, autoCommit, Connection.TRANSACTION_SERIALIZABLE, compatibilityLevel);
        this.metadata = new TDBDatasetMetadata(this);
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.metadata;
    }


    @Override
    protected void checkTransactionIsolation(int level) throws SQLException {
        switch (level) {
        case TRANSACTION_SERIALIZABLE:
            // TDB supports only serializable as the transaction level
            return;
        default:
            throw new SQLException(String.format("The Transaction level %d is not supported by TDB backed connections, only Serializable (%d) may be used", level, TRANSACTION_SERIALIZABLE));
        }
    }
}
