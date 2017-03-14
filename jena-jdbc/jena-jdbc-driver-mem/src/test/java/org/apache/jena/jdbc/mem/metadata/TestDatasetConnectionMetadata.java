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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.mem.connections.MemConnection;
import org.apache.jena.jdbc.metadata.results.AbstractDatabaseMetadataTests;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;

/**
 * Database metadata tests for {@link MemConnection} implementation
 *
 */
public class TestDatasetConnectionMetadata extends AbstractDatabaseMetadataTests {

    // Some datasets in memory don't provide perfect transactions but do report
    // "supportsTransactions".
    // createTxnMem is fully transactional. 
    // "false" does not pass the tests because "supportsTransactions" is still true.
    
    boolean supportsSerializable = true ;
    
    @Override
    protected JenaConnection getConnection() throws SQLException {
        Dataset ds = supportsSerializable ? DatasetFactory.createTxnMem() : DatasetFactory.create() ;
        int transactionLevelSupported = supportsSerializable ?  JenaConnection.TRANSACTION_SERIALIZABLE :  JenaConnection.TRANSACTION_NONE ;
        return new MemConnection(ds, JenaConnection.DEFAULT_HOLDABILITY,
                JenaConnection.DEFAULT_AUTO_COMMIT, transactionLevelSupported, JdbcCompatibility.DEFAULT);
    }

    @Override
    protected List<Integer> getSupportedTransactionLevels() {
        List<Integer> levels = new ArrayList<>();
        levels.add(Connection.TRANSACTION_NONE);
        if ( supportsSerializable )
            levels.add(Connection.TRANSACTION_SERIALIZABLE);
        return levels;
    }

}
