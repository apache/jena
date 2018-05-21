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

package org.apache.jena.tdb2;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.DatabaseConnection;
import org.apache.jena.tdb2.sys.DatabaseOps;
import org.apache.jena.tdb2.sys.TDBInternal;

/** Operations for TDBS DatasetGraph, including admin operations 
 * See {@link TDB2Factory} for creating API-level {@link Dataset Datasets}.
 * 
 * @see TDB2Factory
 */
public class DatabaseMgr {

    // All creation of DatasetGraph for TDB2 goes through this method.
    private static DatasetGraph DB_ConnectCreate(Location location) {
        return DatabaseConnection.connectCreate(location).getDatasetGraph();
    }

    /** Create or connect to a TDB2-backed dataset */
    public static DatasetGraph connectDatasetGraph(Location location) {
        return DB_ConnectCreate(location); 
    }

    /** Create or connect to a TDB2-backed dataset */
    public static DatasetGraph connectDatasetGraph(String location) {
        return connectDatasetGraph(Location.create(location)) ;
    }
    
    /**
     * Compact a datasets which must be a switchable TDB database.
     * This is the normal dataset type for on-disk TDB2 databases.
     *  
     * @param container
     */
    public static void compact(DatasetGraph container) {
        DatasetGraphSwitchable dsg = requireSwitchable(container);
        DatabaseOps.compact(dsg);
    }

    /**
     * Create a backup for a switchable TDB database. This is the normal dataset type for
     * on-disk TDB2 databases.
     * <p>
     * The backup is created in the databases folder, under "Backups".
     * <p>
     * Backup creates a consistent copy og the database. It is performed as a read-transaction
     * and does not lock out other use of the dataset.
     * 
     * @param container
     * @return File name of the backup.
     */
    public static String backup(DatasetGraph container) {
        DatasetGraphSwitchable dsg = requireSwitchable(container);
        return DatabaseOps.backup(dsg);
    }

    /** Create an in-memory TDB2-backed dataset (for testing) */
    public static DatasetGraph createDatasetGraph() {
        return connectDatasetGraph(Location.mem()) ;
    }

    /** Return the location of a dataset if it is backed by TDB, else null */ 
    public static boolean isBackedByTDB(DatasetGraph datasetGraph) {
        return TDBInternal.isBackedByTDB(datasetGraph);
    }

    /** Return the location of a DatasetGraph if it is backed by TDB, else null. */
    public static Location location(DatasetGraph datasetGraph) {
        DatasetGraphSwitchable dsg = requireSwitchable(datasetGraph);
        if ( dsg == null )
            return null ;
        return dsg.getLocation();
    }

    private static DatasetGraphSwitchable requireSwitchable(DatasetGraph datasetGraph) {
        if ( datasetGraph instanceof DatasetGraphSwitchable )
            return (DatasetGraphSwitchable)datasetGraph;
        throw new TDBException("Not a switchable TDB database");
    }

    static DatasetGraphTDB requireDirect(DatasetGraph datasetGraph) {
        DatasetGraphTDB dsg = TDBInternal.getDatasetGraphTDB(datasetGraph);
        if ( dsg == null )
            throw new TDBException("Not a TDB database (argument is neither a switchable nor direct TDB DatasetGraph)");
        return dsg;
    }
}
