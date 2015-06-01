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

import org.apache.jena.jdbc.utils.TestUtils;
import org.apache.jena.query.Dataset ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.base.file.Location ;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for result sets using a disk backed TDB dataset
 * 
 */
public class TestTdbDiskResultSets extends AbstractTdbResultSetTests {

    /**
     * Temporary directory rule used to guarantee a unique temporary folder for
     * each test method
     */
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private Dataset currDataset;

    /**
     * Cleans up after the tests by ensuring that the TDB dataset is closed
     */
    @After
    public void cleanupTest() {
        if (currDataset != null) {
            currDataset.close();
        }
        StoreConnection.expel(Location.create(tempDir.getRoot().getAbsolutePath()), true);
    }

    @Override
    protected Dataset prepareDataset(Dataset ds) {
        if (this.currDataset != null) {
            this.currDataset.close();
        }

        Dataset tdb = TDBFactory.createDataset(tempDir.getRoot().getAbsolutePath());
        TestUtils.copyDataset(ds, tdb, true);
        this.currDataset = tdb;
        return tdb;
    }
}
