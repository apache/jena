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

package org.apache.jena.tdb2.sys;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.base.file.ChannelManager;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.base.file.ProcessFileLock;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.TDB2StorageBuilder;

/** A StoreConnection is the reference to the underlying storage.
 *  There is JVM-wide cache of backing datasets.
 */
public class StoreConnection
{
    private static Map<Location, StoreConnection> cache = new ConcurrentHashMap<>();

    /**
     * Get the {@code StoreConnection} to a location,
     * creating the storage structures with default settings
     * if it does not exist.
     */
    public synchronized static StoreConnection connectCreate(Location location) {
        return connectCreate(location, null, null);
    }

    /** Get the {@code StoreConnection} to a location,
     *  creating the storage structures if it does not exist.
     *  Use the provided {@link StoreParams} and {@link ReorderTransformation} - any persistent settings
     *  already at the location takes precedence.
     */
    public synchronized static StoreConnection connectCreate(Location location, StoreParams params, ReorderTransformation reorderTransform) {
        return make(location, params, reorderTransform);
    }

    /** Get the {@code StoreConnection} for a location, but do not create it.
     *  Returns null for "not setup".
     */
    public synchronized static StoreConnection connectExisting(Location location) {
        StoreConnection sConn = cache.get(location);
        return sConn;
    }

    public synchronized static boolean isSetup(Location location) {
        return cache.containsKey(location);
    }

    /**
     * Return a {@code StoreConnection} for a particular location,
     * creating it if it does not exist in storage.
     */
    private synchronized static StoreConnection make(Location location, StoreParams params, ReorderTransformation reorderTransform) {
        StoreConnection sConn = cache.get(location);
        if ( sConn == null ) {
            ProcessFileLock lock = null;
            // This is not duplicating DatabaseConnection.build.
            // This is a tdb.lock file in the storage database, not the switchable.
            if (SystemTDB.DiskLocationMultiJvmUsagePrevention && ! location.isMem() ) {
                lock = lockForLocation(location);
                // Take the lock.  This is atomic and non-reentrant.
                lock.lockEx();
            }
            // Recovery happens when TransactionCoordinator.start is called
            // during the building of the DatasetGraphTDB
            DatasetGraphTDB dsg = TDB2StorageBuilder.build(location, params, reorderTransform);
            sConn = new StoreConnection(dsg, lock);
            if (!location.isMemUnique())
                cache.put(location, sConn);
        }
        return sConn;
    }

    /**
     * Stop managing all locations.
     * Use with extreme care.
     * This is intended to support internal testing.
     */
    public static synchronized void internalReset() {
        // Copy to avoid potential CME.
        Set<Location> x = Set.copyOf(cache.keySet());
        for (Location loc : x)
            internalExpel(loc, true);
        cache.clear();
        ChannelManager.reset();
    }

    /** Stop managing a location. There should be no transactions running. */
    public static synchronized void release(Location location) {
        internalExpel(location, false);
    }

    /** Use via {@link TDBInternal#expel} wherever possible.
     * <p>
     * Stop managing a location.<br/>
     * Use with great care (testing only).
     */
    public static synchronized void internalExpel(Location location, boolean force) {
        StoreConnection sConn = cache.get(location);
        if (sConn == null) return;

        TransactionCoordinator txnCoord = sConn.getDatasetGraphTDB().getTxnSystem().getTxnMgr();
        if (!force && txnCoord.countActive() > 0 )
            throw new TransactionException("Can't expel: Active transactions for location: " + location);

        // No transactions at this point
        // (or we don't care and are clearing up forcefully.)

        sConn.getDatasetGraphTDB().shutdown();
        // Done by DatasetGraphTDB()
        //txnCoord.shutdown();

        sConn.isValid = false;
        cache.remove(location);

        // Release the lock after the cache is emptied.
        if (SystemTDB.DiskLocationMultiJvmUsagePrevention && ! location.isMem() ) {
            if ( ! sConn.lock.isLockedHere() )
                SystemTDB.errlog.warn("Location " + location.getDirectoryPath() + " was not locked by this process.");
            sConn.lock.unlock();
            ProcessFileLock.release(sConn.lock);
        }
    }

    /** Create or fetch a {@link ProcessFileLock} for a Location */
    public static ProcessFileLock lockForLocation(Location location) {
        FileOps.ensureDir(location.getDirectoryPath());
        String lockFilename = location.getPath(Names.TDB_LOCK_FILE);
        Path path = Path.of(lockFilename);
        try {
            path.toFile().createNewFile();
        } catch(IOException ex) { IO.exception(ex); return null; }
        return ProcessFileLock.create(lockFilename);
    }

    private final DatasetGraphTDB   datasetGraph;
    // This is the location of the database itself, not the TDB2 container directory.
    private final Location          location;
    private final ProcessFileLock   lock;
    private boolean                 isValid = true;
    private volatile boolean        haveUsedInTransaction = false;

    private StoreConnection(DatasetGraphTDB dsg, ProcessFileLock fileLock)
    {
        this.datasetGraph = dsg;
        this.location = dsg.getLocation();
        this.lock = fileLock;
    }

    public DatasetGraph getDatasetGraph() {
        return datasetGraph;
    }

    public DatasetGraphTDB getDatasetGraphTDB() {
        return datasetGraph;
    }

    public Location getLocation() {
        return location;
    }

    public ProcessFileLock getLock() {
        return lock;
    }

}

