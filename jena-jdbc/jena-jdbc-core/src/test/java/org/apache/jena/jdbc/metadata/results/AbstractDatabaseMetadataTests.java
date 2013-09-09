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

package org.apache.jena.jdbc.metadata.results;

import java.sql.Connection ;
import java.sql.DatabaseMetaData ;
import java.sql.ResultSet ;
import java.sql.SQLException ;
import java.util.List ;

import org.apache.jena.jdbc.connections.JenaConnection ;
import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.query.ARQ ;

/**
 * Tests that inspect the information provided by {@link JenaConnection}
 * implementations in response to the {@link JenaConnection#getMetaData()}
 * method
 * 
 */
public abstract class AbstractDatabaseMetadataTests {

    protected abstract JenaConnection getConnection() throws SQLException;

    protected abstract List<Integer> getSupportedTransactionLevels();

    /**
     * JDBC transaction levels
     */
    private static int[] TRANSACTION_LEVELS = new int[] { Connection.TRANSACTION_NONE, Connection.TRANSACTION_READ_COMMITTED,
            Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_REPEATABLE_READ, Connection.TRANSACTION_SERIALIZABLE };

    static {
        ARQ.init();
    }

    /**
     * Get type info and check the nullable column (regression for an issue
     * encountered during debugging)
     * 
     * @throws SQLException
     */
    @Test
    public void metadata_type_info() throws SQLException {
        JenaConnection conn = this.getConnection();

        DatabaseMetaData metadata = conn.getMetaData();
        ResultSet typeInfo = metadata.getTypeInfo();

        while (typeInfo.next()) {
            typeInfo.getShort("NULLABLE");
        }

        Assert.assertTrue(typeInfo.isAfterLast());
        typeInfo.close();
        Assert.assertTrue(typeInfo.isClosed());
        conn.close();
        Assert.assertTrue(typeInfo.isClosed());
    }

    /**
     * Tests that reported transaction support is correct
     * 
     * @throws SQLException
     */
    @Test
    public void metadata_transaction_support() throws SQLException {
        JenaConnection conn = this.getConnection();
        DatabaseMetaData metadata = conn.getMetaData();

        List<Integer> supportedLevels = this.getSupportedTransactionLevels();

        // Expect transactions to report as supported if some supported levels
        // are returned and either:
        // 1 - There is more than one supported level
        // 2 - There is one supported level which is not TRANSACTION_NONE
        boolean expectedSupport = supportedLevels.size() > 0
                && (supportedLevels.size() != 1 || (supportedLevels.size() == 1 && !supportedLevels.get(0).equals(
                        Connection.TRANSACTION_NONE)));
        Assert.assertEquals(expectedSupport, metadata.supportsTransactions());

        for (int level : TRANSACTION_LEVELS) {
            Assert.assertEquals(supportedLevels.contains(level), metadata.supportsTransactionIsolationLevel(level));
        }
    }
}
