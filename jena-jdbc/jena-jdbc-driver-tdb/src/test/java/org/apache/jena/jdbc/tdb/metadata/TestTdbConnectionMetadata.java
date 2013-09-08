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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.metadata.results.AbstractDatabaseMetadataTests;
import org.apache.jena.jdbc.tdb.connections.TDBConnection;
import org.junit.After;

import com.hp.hpl.jena.tdb.StoreConnection;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

/**
 * Tests database metadata for TDB connections
 */
public class TestTdbConnectionMetadata extends AbstractDatabaseMetadataTests {
    
    /**
     * Cleans up resources used by tests
     */
    @After
    public void cleanupTest() {
        StoreConnection.expel(Location.mem(), true);
    }

    @Override
    protected JenaConnection getConnection() throws SQLException {
        return new TDBConnection(TDBFactory.createDataset(), ResultSet.HOLD_CURSORS_OVER_COMMIT,
                JenaConnection.DEFAULT_AUTO_COMMIT, JdbcCompatibility.DEFAULT);
    }

    @Override
    protected List<Integer> getSupportedTransactionLevels() {
        List<Integer> levels = new ArrayList<Integer>();
        levels.add(Connection.TRANSACTION_SERIALIZABLE);
        return levels;
    }

}
