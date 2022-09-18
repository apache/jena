/*
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

package org.apache.jena.tdb2.store;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.AbstractDatasetGraphTests;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.sys.StoreConnection;
import org.junit.After;
import org.junit.Before;

public class TestStorageDatasetGraphTests extends AbstractDatasetGraphTests {

    private Location location = Location.mem();
    private DatasetGraph currentDSG = null;

    @Override
    protected DatasetGraph emptyDataset() {
        return currentDSG;

    }

    @Before public void before() {
        currentDSG = TDB2StorageBuilder.build(location);
        currentDSG.begin(ReadWrite.WRITE);
    }

    @After public void after() {
        currentDSG.abort();
        currentDSG.end();
        StoreConnection.internalExpel(location, true);
    }
}
