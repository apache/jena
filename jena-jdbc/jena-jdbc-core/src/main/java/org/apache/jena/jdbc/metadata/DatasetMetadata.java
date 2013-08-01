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

package org.apache.jena.jdbc.metadata;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.jena.jdbc.connections.DatasetConnection;

/**
 * Abstract implementation of metadata for dataset connections
 *
 */
public abstract class DatasetMetadata extends JenaMetadata {

    /**
     * Creates new metadata
     * @param connection Connection
     * @throws SQLException
     */
    public DatasetMetadata(DatasetConnection connection) throws SQLException {
        super(connection);
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        // Dataset connections can support None or Serializable transactions
        switch (level) {
        case Connection.TRANSACTION_NONE:
            return true;
        case Connection.TRANSACTION_SERIALIZABLE:
            return this.supportsTransactions();
        default:
            return false;
        }
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        // Transactions are only supported if the underlying dataset supports them
        return ((DatasetConnection)this.getConnection()).getJenaDataset().supportsTransactions();
    }
}
