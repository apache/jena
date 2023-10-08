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

package org.apache.jena.jdbc.tdb.results;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static org.apache.jena.jdbc.results.metadata.AskResultsMetadata.COLUMN_LABEL_ASK;
import static org.apache.jena.tdb1.TDB1Factory.createDataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.jena.jdbc.tdb.connections.DebugTdbConnection;
import org.apache.jena.query.Dataset;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for result sets using a disk backed TDB dataset, to check that there are no problems with filenames.
 */
public class TestTdbDiskResultSets extends Assert {

    /**
     * Temporary directory rule used to guarantee a unique temporary folder for each test method
     */
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    /**
     * Test ASK results with a true result
     * 
     * @throws SQLException
     */
    @Test
    public void results_ask_true() throws SQLException {
        Dataset ds = createDataset(tempDir.getRoot().getAbsolutePath());
        try (DebugTdbConnection connection = new DebugTdbConnection(ds)) {
            Statement stmt = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
            @SuppressWarnings("resource")
            ResultSet rset = stmt.executeQuery("ASK { }");
            assertNotNull(rset);
            assertFalse(rset.isClosed());
            assertTrue(rset.isBeforeFirst());

            // Try to move to the result row
            assertTrue(rset.next());

            // Check the boolean return value
            assertTrue(rset.getBoolean(COLUMN_LABEL_ASK));

            // Check no further rows
            assertFalse(rset.next());
            assertTrue(rset.isAfterLast());

            // Close and clean up
            rset.close();
            assertTrue(rset.isClosed());
        }
    }

    /**
     * Test ASK results with a false result
     * 
     * @throws SQLException
     */
    @Test
    public void results_ask_false() throws SQLException {
        Dataset ds = createDataset(tempDir.getRoot().getAbsolutePath());
        try (DebugTdbConnection connection = new DebugTdbConnection(ds)) {
            Statement stmt = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
            @SuppressWarnings("resource")
            ResultSet rset = stmt.executeQuery("ASK { FILTER(false) }");
            assertNotNull(rset);
            assertFalse(rset.isClosed());
            assertTrue(rset.isBeforeFirst());

            // Try to move to the result row
            assertTrue(rset.next());

            // Check the boolean return value
            assertFalse(rset.getBoolean(COLUMN_LABEL_ASK));

            // Check no further rows
            assertFalse(rset.next());
            assertTrue(rset.isAfterLast());

            // Close and clean up
            rset.close();
            assertTrue(rset.isClosed());
        }
    }
}
