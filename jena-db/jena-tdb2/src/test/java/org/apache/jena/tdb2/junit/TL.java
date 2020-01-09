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

package org.apache.jena.tdb2.junit;

import java.util.function.Consumer;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.sys.TDBInternal;

/** Support for tests to be less "transaction-y" */
public class TL {

    public static void exec(Consumer<Dataset> action) {
        Dataset dataset = createTestDataset();
        try {
            Txn.executeWrite(dataset, ()->action.accept(dataset));
            //TDBInternal.reset();

        } finally { expel(dataset); }
    }

    public static void execMem(Consumer<Dataset> action) {
        Dataset dataset = createTestDatasetMem();
        Txn.executeWrite(dataset, ()->action.accept(dataset));
        expel(dataset);
    }

    public static void expel(Dataset dataset) {
        expel(dataset.asDatasetGraph());
    }

    public static void expel(DatasetGraph dataset) {
        TDBInternal.expel(dataset);
    }

    // Or use these for @Before, @After style.

    public static Location cleanLocation() {
        // To avoid the problems on MS Windows where memory mapped files
        // can't be deleted from a running JVM, we use a different, cleaned
        // directory each time.
        String dirname = ConfigTest.getCleanDir();
        Location location = Location.create(dirname);
        return location;
    }

    private static void releaseDataset(Dataset dataset) {
        dataset.abort();
        expel(dataset);
    }

    private static Dataset createTestDataset() {
        Location location = cleanLocation();
        Dataset dataset = TDB2Factory.connectDataset(location);
        return dataset;
    }

    public static Dataset createTestDatasetMem() {
        Dataset dataset = TDB2Factory.createDataset();
        return dataset;
    }

    public static DatasetGraph createTestDatasetGraphMem() {
        DatasetGraph dataset = DatabaseMgr.createDatasetGraph();
        return dataset;
    }
}

