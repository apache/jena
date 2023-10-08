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

package org.apache.jena.tdb;


import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.setup.StoreParams;

/** Public factory for creating objects datasets backed by TDB1 storage.
 * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory}
 *
 */
@Deprecated
public class TDBFactory {
    static {
        JenaSystem.init();
    }

    private TDBFactory() {}

    /**
     * Read the file and assembler a dataset.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#assembleDataset}
     */
    @Deprecated
    public static Dataset assembleDataset(String assemblerFile) {
        return TDB1Factory.assembleDataset(assemblerFile);
    }

    /**
     * Create or connect to a TDB-backed dataset.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#createDataset}
     */
    @Deprecated
    public static Dataset createDataset(String dir) {
        return TDB1Factory.createDataset(dir);
    }

    /**
     * Create or connect to a TDB-backed dataset.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#createDataset(Location)}
     */
    @Deprecated
    public static Dataset createDataset(Location location) {
        return TDB1Factory.createDataset(location);
    }

    /**
     * Create or connect to a TDB dataset backed by an in-memory block manager. For
     * testing.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#createDataset()}
     */
    @Deprecated
    public static Dataset createDataset() {
        return TDB1Factory.createDataset();
    }

    /**
     * Create or connect to a TDB-backed dataset (graph-level).
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#createDatasetGraph(String)}
     */
    @Deprecated
    public static DatasetGraph createDatasetGraph(String directory) {
        return TDB1Factory.createDatasetGraph(directory);
    }

    /**
     * Create or connect to a TDB-backed dataset (graph-level).
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#createDatasetGraph(Location)}
     */
    @Deprecated
    public static DatasetGraph createDatasetGraph(Location location) {
        return TDB1Factory.createDatasetGraph(location);
    }

    /**
     * Create a TDB-backed dataset (graph-level) in memory (for testing).
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#createDatasetGraph()}
     */
    @Deprecated
    public static DatasetGraph createDatasetGraph() {
        return TDB1Factory.createDatasetGraph();
    }

    /**
     * Release from the JVM. All caching is lost.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#release(Dataset)}
     */
    @Deprecated
    public static void release(Dataset dataset) {
        TDB1Factory.release(dataset);
    }

    /**
     * Release from the JVM. All caching is lost.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#release(DatasetGraph)}
     */
    @Deprecated
    public static void release(DatasetGraph dataset) {
        TDB1Factory.release(dataset);
    }

    /**
     * Test whether a dataset is backed by TDB.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#isTDB1(Dataset)}
     */
    @Deprecated
    public static boolean isTDB1(Dataset dataset) {
        return TDB1Factory.isTDB1(dataset);
    }

    /**
     * Test whether a dataset is backed by TDB.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#isTDB1(DatasetGraph)}
     */
    @Deprecated
    public static boolean isTDB1(DatasetGraph datasetGraph) {
        return TDB1Factory.isTDB1(datasetGraph);
    }

    /**
     * Return the location of a dataset if it is backed by TDB, else null.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#location(Dataset)}
     */
    @Deprecated
    public static Location location(Dataset dataset) {
        return TDB1Factory.location(dataset);
    }

    /**
     * Return the location of a DatasetGraph if it is backed by TDB, else null.
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#location(DatasetGraph)}
     */
    @Deprecated
    public static Location location(DatasetGraph datasetGraph) {
        return TDB1Factory.location(datasetGraph);
    }

    /**
     * Test whether a location already has a TDB database or whether a call to
     * TDBFactory will cause a new, fresh TDB database to be created (pragmatic
     * tests). The directory may be empty, or not exist. Existing databases return
     * "true".
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#inUseLocation(String)}
     */
    @Deprecated
    public static boolean inUseLocation(String directory) {
        return TDB1Factory.inUseLocation(directory);
    }

    /**
     * Test whether a location already has a TDB database or whether a call to
     * TDBFactory will cause a new, fresh TDB database to be created (pragmatic
     * tests). The directory may be empty, or not exist. Existing databases return
     * "true".
     *
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#inUseLocation(Location)}
     */
    @Deprecated
    public static boolean inUseLocation(Location location) {
        return TDB1Factory.inUseLocation(location);
    }

    /**
     * Set the {@link StoreParams} for specific Location. This call must only be
     * called before a dataset from Location is created. This operation should be
     * used with care; bad choices of {@link StoreParams} can reduce performance.
     * <a href="http://jena.apache.org/documentation/tdb/store-parameters.html">See
     * documentation</a>.
     *
     * @param location The persistent storage location
     * @param params StoreParams to use
     * @throws IllegalStateException If the dataset has already been setup.
     * @deprecated Use {@link org.apache.jena.tdb1.TDB1Factory#setup}
     */
    @Deprecated
    public static void setup(Location location, StoreParams params) {
        TDB1Factory.setup(location, params);
    }
}
