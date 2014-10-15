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

import java.sql.SQLException;

import org.apache.jena.jdbc.utils.TestUtils;
import org.junit.After;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.StoreConnection;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

/**
 * Tests for result sets using a in-memory TDB dataset
 *
 */
public class TestTdbMemResultSets extends AbstractTdbResultSetTests {

    private Dataset currDataset;
    
    /**
     * Cleans up after the tests by ensuring that the TDB dataset is closed
     */
    @After
    public void cleanupTest() {
        if (currDataset != null) {
            currDataset.close();
        }
        StoreConnection.expel(Location.mem(), true);
    }
    
    @Override
    protected Dataset prepareDataset(Dataset ds) throws SQLException {
        if (currDataset != null) {
            currDataset.close();
        }
        
        Dataset tdb = TDBFactory.createDataset();
        TestUtils.copyDataset(ds, tdb, true);
        currDataset = tdb;
        return tdb;
    }
}
