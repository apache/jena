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

package org.seaborne.tdb2.sys ;

import java.util.HashSet ;
import java.util.Map ;
import java.util.Set ;
import java.util.concurrent.ConcurrentHashMap ;

import org.apache.jena.sparql.core.DatasetGraph ;
import org.seaborne.dboe.base.file.ChannelManager ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.file.LocationLock ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.setup.StoreParams ;
import org.seaborne.tdb2.setup.TDBBuilder ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;


/** A StoreConnection is the reference to the underlying storage.
 *  There is JVM-wide cache of backing datasets.
 */
public class StoreConnection
{
    private static Map<Location, StoreConnection> cache = new ConcurrentHashMap<>() ;
    
    public synchronized static StoreConnection connectCreate(Location location) {
        return make(location, StoreParams.getDftStoreParams()) ;
    }

    public synchronized static boolean isSetup(Location location) {
        return cache.containsKey(location) ;
    }

    public synchronized static StoreConnection connectExisting(Location location) {
        StoreConnection sConn = cache.get(location) ;
        return sConn ;
    }

    /**
     * Return a StoreConnection for a particular connection.
     */
    public synchronized static StoreConnection make(Location location, StoreParams params) {
        StoreConnection sConn = cache.get(location) ;
        if ( sConn == null ) {
            // Recovery happens when TransactionCoordinator.start is called
            // during the building of the DatasetGraphTxn.
            DatasetGraphTDB dsg = (DatasetGraphTDB)TDBBuilder.build(location, params) ;
            if (SystemTDB.DiskLocationMultiJvmUsagePrevention)
            {
                // Obtain the lock ASAP but we do need an initialized database,
                // so an empty directory is actually empty. Hence, after building.
                LocationLock lock = location.getLock();
                if (lock.canLock()) {
                    if (!lock.canObtain()) 
                        throw new TDBException("Can't open database at location " + location.getDirectoryPath() + " as it is already locked by the process with PID " + lock.getOwner() + ".  TDB databases do not permit concurrent usage across JVMs so in order to prevent possible data corruption you cannot open this location from the JVM that does not own the lock for the dataset");

                    lock.obtain();
                    // There's an interesting race condition here that two JVMs might write out the lock file one after another without
                    // colliding and causing an IO error in either.  The best way to check for this is simply to check we now own the lock
                    // and if not error
                    if (!lock.isOwned()) {
                        throw new TDBException("Can't open database at location " + location.getDirectoryPath() + " as it is alread locked by the process with PID " + lock.getOwner() + ".  TDB databases do not permit concurrent usage across JVMs so in order to prevent possible data corruption you cannot open this location from the JVM that does not own the lock for the dataset");
                    }
                }
            }
            sConn = new StoreConnection(dsg) ;
            if (!location.isMemUnique())
                cache.put(location, sConn) ;
        }
        return sConn ;
    }
    
    /** Make a StoreConnection based on any StoreParams at the location or the system defaults. */
    public static StoreConnection make(Location location) {
        return make(location, null) ;
    }

    /** Stop managing all locations. Use with great care. */
    public static synchronized void reset()
    {
        // Copy to avoid potential CME.
        Set<Location> x = new HashSet<>(cache.keySet()) ;
        for (Location loc : x)
            expel(loc, true) ;
        cache.clear() ;
        ChannelManager.reset(); 
    }

    /** Stop managing a location. There should be no transactions running. */
    public static synchronized void release(Location location)
    {
        expel(location, false) ;
    }

    /** Stop managing a location. Use with great care (testing only). */
    public static synchronized void expel(Location location, boolean force)
    {
        StoreConnection sConn = cache.get(location) ;
        if (sConn == null) return ;

        //    if (!force && sConn.transactionManager.activeTransactions()) 
        //        throw new TDBTransactionException("Can't expel: Active transactions for location: " + location) ;

        // No transactions at this point 
        // (or we don't care and are clearing up forcefully.)
//        try { sConn.getDatasetGraph().getCoordinator().shutdown(); } catch (Throwable th ) {} 
//        try { sConn.getDatasetGraphTDB().close() ; } catch (Throwable th ) {}
        
        sConn.getDatasetGraphTDB().getTxnSystem().getTxnMgr().shutdown();
        sConn.getDatasetGraphTDB().shutdown() ;
        
        sConn.isValid = false ;
        cache.remove(location) ;

        // Release the lock
        if (SystemTDB.DiskLocationMultiJvmUsagePrevention) {
            if (location.getLock().isOwned()) {
                location.getLock().release();
            } else if (location.getLock().canLock()) {
                SystemTDB.errlog.warn("Location " + location.getDirectoryPath() + " was not locked, if another JVM accessed this location simultaneously data corruption may have occurred");
            }
        }
    }

    private final DatasetGraphTDB   datasetGraph ;
    private final Location          location ;
    private boolean                 isValid = true ;
    private volatile boolean        haveUsedInTransaction = false ;

    private StoreConnection(DatasetGraphTDB dsg)
    {
        datasetGraph = dsg ;
        location = dsg.getLocation() ;
    }

    public DatasetGraph getDatasetGraph() {
        return datasetGraph;
    }

    public DatasetGraphTDB getDatasetGraphTDB() {
        return datasetGraph;
    }

    
    public Location getLocation() {
        return location ;
    }
}

