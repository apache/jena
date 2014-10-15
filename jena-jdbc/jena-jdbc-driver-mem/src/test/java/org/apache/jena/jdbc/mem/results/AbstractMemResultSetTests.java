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

package org.apache.jena.jdbc.mem.results;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.mem.connections.DebugMemConnection;
import org.apache.jena.jdbc.results.AbstractResultSetTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hp.hpl.jena.query.Dataset;

/**
 * Abstract
 *
 */
public abstract class AbstractMemResultSetTests extends AbstractResultSetTests {

    protected static DebugMemConnection connection;

    /**
     * Sets up the tests by creating a fake connection for test use
     * @throws SQLException
     */
    @BeforeClass
    public static void setup() throws SQLException {
        connection = new DebugMemConnection();
        connection.setJdbcCompatibilityLevel(JdbcCompatibility.HIGH);
    }

    /**
     * Cleans up after the tests by closing the fake connection
     * @throws SQLException
     */
    @AfterClass
    public static void teardown() throws SQLException {
        connection.close();
    }

    /**
     * Method which derived test classes must implement which they can use to turn
     * the provided dataset (which will be a memory dataset) into the actual
     * dataset they want to test against
     * @param ds Dataset
     * @return Prepared Dataset
     * @throws SQLException Thrown if the dataset cannot be prepared
     */
    protected abstract Dataset prepareDataset(Dataset ds) throws SQLException;
    
    @Override
    protected final ResultSet createResults(Dataset ds, String query) throws SQLException {
        return createResults(ds, query, ResultSet.TYPE_FORWARD_ONLY);
    }
    
    @Override
    protected final ResultSet createResults(Dataset ds, String query, int resultSetType) throws SQLException {
        connection.setJenaDataset(this.prepareDataset(ds));
        Statement stmt = connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
        return stmt.executeQuery(query);
    }
}