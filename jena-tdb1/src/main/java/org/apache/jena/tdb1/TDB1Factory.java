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

package org.apache.jena.tdb1;

import java.io.File ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb1.assembler.VocabTDB;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.setup.StoreParams;
import org.apache.jena.tdb1.store.DatasetGraphTDB;
import org.apache.jena.tdb1.sys.StoreConnection;
import org.apache.jena.tdb1.sys.TDBInternal;
import org.apache.jena.tdb1.sys.TDBMaker;
import org.apache.jena.tdb1.transaction.DatasetGraphTransaction;

/** Public factory for creating objects datasets backed by TDB storage */
public class TDB1Factory
{
    static { JenaSystem.init(); }

    private TDB1Factory() {}

    /** Read the file and assembler a dataset */
    public static Dataset assembleDataset(String assemblerFile) {
        return (Dataset)AssemblerUtils.build(assemblerFile, VocabTDB.tDatasetTDB);
    }

    /** Create or connect to a TDB-backed dataset */
    public static Dataset createDataset(String dir) {
        return createDataset(Location.create(dir));
    }

    /** Create or connect to a TDB-backed dataset */
    public static Dataset createDataset(Location location) {
        return createDataset(createDatasetGraph(location));
    }

    /**
     * Create or connect to a TDB dataset backed by an in-memory block manager. For
     * testing.
     */
    public static Dataset createDataset() {
        return createDataset(createDatasetGraph());
    }

    /** Create a dataset around a DatasetGraphTDB */
    private static Dataset createDataset(DatasetGraph datasetGraph) {
        return DatasetFactory.wrap(datasetGraph);
    }

    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraph createDatasetGraph(String directory) {
        return createDatasetGraph(Location.create(directory));
    }

    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraph createDatasetGraph(Location location) {
        return _createDatasetGraph(location);
    }

    /** Create a TDB-backed dataset (graph-level) in memory (for testing) */
    public static DatasetGraph createDatasetGraph() {
        return _createDatasetGraph();
    }

    /** Release from the JVM. All caching is lost. */
    public static void release(Dataset dataset) {
        release(dataset.asDatasetGraph());
    }

    /** Release from the JVM. All caching is lost. */
    public static void release(DatasetGraph dataset) {
        _release(dataset);
    }

    private static void _release(DatasetGraph dataset) {
        TDBInternal.expel(dataset);
    }

    private static DatasetGraph _createDatasetGraph(Location location) {
        return TDBMaker.createDatasetGraphTransaction(location);
    }

    private static DatasetGraph _createDatasetGraph() {
        return TDBMaker.createDatasetGraphTransaction();
    }

    /** Test whether a dataset is backed by TDB. */
    public static boolean isTDB1(Dataset dataset) {
        DatasetGraph dsg = dataset.asDatasetGraph();
        return isTDB1(dsg);
    }

    /** Test whether a dataset is backed by TDB. */
    public static boolean isTDB1(DatasetGraph datasetGraph) {
        return TDBInternal.isTDB1(datasetGraph);
    }

    /** Return the location of a dataset if it is backed by TDB, else null */
    public static Location location(Dataset dataset) {
        DatasetGraph dsg = dataset.asDatasetGraph();
        return location(dsg);
    }

    /** Return the location of a DatasetGraph if it is backed by TDB, else null */
    public static Location location(DatasetGraph datasetGraph) {
        if ( datasetGraph instanceof DatasetGraphTDB dsgt )
            return dsgt.getLocation();
        if ( datasetGraph instanceof DatasetGraphTransaction dsgtxn )
            return dsgtxn.getLocation();
        return null;
    }

    /**
     * Test whether a location already has a TDB database or whether a call to
     * TDBFactory will cause a new, fresh TDB database to be created (pragmatic
     * tests). The directory may be empty, or not exist. Existing databases return
     * "true".
     */
    public static boolean inUseLocation(String directory) {
        return inUseLocation(Location.create(directory));
    }

    /**
     * Test whether a location already has a TDB database or whether a call to
     * TDBFactory will cause a new, fresh TDB database to be created (pragmatic
     * tests). The directory may be empty, or not exist. Existing databases return
     * "true".
     */
    public static boolean inUseLocation(Location location) {
        if ( location.isMemUnique() )
            return false;
        if ( location.isMem() )
            return StoreConnection.getExisting(location) != null;
        String dirname = location.getDirectoryPath();
        File d = new File(dirname);

        if ( !d.exists() )
            // TDB autocreates directories one level.
            return !FileOps.exists(d.getParent());
        return !TDBInternal.isNewDatabaseArea(location);
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
     */
    public static void setup(Location location, StoreParams params) {
        StoreConnection sConn = StoreConnection.getExisting(location);
        if ( sConn != null )
            throw new IllegalStateException("Location is already active");
        StoreConnection.make(location, params);
    }
}
