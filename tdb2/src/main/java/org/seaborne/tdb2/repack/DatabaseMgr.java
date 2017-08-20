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

package org.seaborne.tdb2.repack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.jena.sparql.core.DatasetGraph;
import org.seaborne.dboe.base.file.Location;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator;
import org.seaborne.dboe.transaction.txn.TransactionalSystem;
import org.seaborne.tdb2.TDBException;
import org.seaborne.tdb2.repack.sys.CopyDSG;
import org.seaborne.tdb2.repack.sys.FilenameUtils;
import org.seaborne.tdb2.repack.sys.IOX;
import org.seaborne.tdb2.repack.sys.Util;
import org.seaborne.tdb2.setup.StoreParams;
import org.seaborne.tdb2.store.DatasetGraphTDB;
import org.seaborne.tdb2.sys.StoreConnection;

/** Create TDB2 databases. 
 * 
 *  TDB2 uses a hierarchical structure to manage on disk.
 * 
 *  If in-memory (non-scalable, not-performant, perfect simulation for functionality using a RAM disk, for testing mainly),
 *  create a 
 *    
 * 
 * Directory and files:
 * <ul>
 * <li> {@code Data-NNNN/} -- databases by version. Compacting creates a new directory, leaving 
 * <li> store params, static (disk layout related - can not chnage once created) and dynamic (settings related to in-memory).
 * <li> {@code Backup-DATE/} -- backups.
 * <li> External indexes like {@code TextIndex/} -- the text index only applies to the current, latest database.
 * </ul>
 */
public class DatabaseMgr {
    private static final String dbPrefix   = "Data";
    private static final String SEP        = "-";
    private static final String startCount = "0001";
    
    // ***** Need to share DatasetGraphSwitchable -> push down into StoreConnection.
    
    public static DatasetGraph connectOrCreate(Location location) {
        if ( location.isMem() )
            return StoreConnection.connectCreate(location).getDatasetGraph();
        // Exists?
       if ( ! location.exists() )
           throw new TDBException("No such location: "+location);
       Path path = IOX.asPath(location);
       // Scan for DBs
       Path db = findLocation(path, dbPrefix);
       // XXX [StoreParams]
       StoreParams params;
       if ( db == null ) {
           db = path.resolve(dbPrefix+SEP+startCount);
           IOX.createDirectory(db);
       }
       Location loc2 = IOX.asLocation(db);
       DatasetGraphTDB dsg = StoreConnection.connectCreate(loc2).getDatasetGraphTDB();
       DatasetGraph appDSG = new DatasetGraphSwitchable(path, dsg);
       return appDSG;
    }
    
    // JVM-wide :-(
    private static Object compactionLock = new Object();
    
    public static void compact(DatasetGraphSwitchable container) {
        synchronized(compactionLock) {
            
            Path base = container.getContainerPath();
            
            Location loc1a = ((DatasetGraphTDB)container.get()).getLocation();
            
            if ( loc1a.isMem() ) {}
            if ( ! loc1a.exists() )
                throw new TDBException("No such location: "+loc1a);
            
            // Checks
            Path db1 = findLocation(base, dbPrefix);
            Location loc1 = IOX.asLocation(db1);
            // Is this the same database location?
            if ( ! loc1.equals(loc1a) )
                throw new TDBException("Inconsistent (not latested?) : "+loc1a+" : "+loc1);
            
            // Version
            int v = IOX.extractIndex(db1.getFileName().toString(), dbPrefix, SEP);
            String next = FilenameUtils.filename(dbPrefix, SEP, v+1);
            
            Path db2 = db1.getParent().resolve(next);
            IOX.createDirectory(db2);
            Location loc2 = IOX.asLocation(db2);
            System.out.printf("Compact %s -> %s\n", db1.getFileName(), db2.getFileName());
            
            compact(container, loc1, loc2);
        }
    }
    
    // XXX Later - switch in a recording dataset, not block writers, and reply after
    // switch over before releasing the new dataset to the container.
    // Maybe copy indexes and switch the DSG over (drop switchable).
    
    /** Copy the latest version from one location to another. */
    private static void compact(DatasetGraphSwitchable container, Location loc1, Location loc2) {
        if ( loc1.isMem() || loc2.isMem() )
            throw new TDBException("Compact involves a memory location: "+loc1+" : "+loc2);
        StoreConnection srcConn = StoreConnection.connectExisting(loc1);
        if ( srcConn == null ) {
            throw new TDBException("No database at location : "+loc1); 
        }
        
        DatasetGraphTDB dsgBase0 = (DatasetGraphTDB)container.get();
        if ( ! dsgBase0.getLocation().equals(loc1) )
            throw new TDBException("Inconsistent locations for base : "+dsgBase0.getLocation()+" , "+dsgBase0.getLocation());
        
        DatasetGraphTDB dsgBase = srcConn.getDatasetGraphTDB();
        if ( dsgBase != dsgBase0 )
            throw new TDBException("Inconsistent datasets : "+dsgBase0.getLocation()+" , "+dsgBase.getLocation());
        
        TransactionalSystem txnSystem = dsgBase.getTxnSystem();
        TransactionCoordinator txnMgr = dsgBase.getTxnSystem().getTxnMgr();
        
        // Stop update.  On exit there are no writers and none will start until switched over.
        txnMgr.blockWriters();
        
        // Copy the latest generation.
        DatasetGraphTDB dsgCompact = StoreConnection.connectCreate(loc2).getDatasetGraphTDB();
        CopyDSG.copy(dsgBase, dsgCompact);   

        TransactionCoordinator txnMgr2 = dsgCompact.getTxnSystem().getTxnMgr();
        txnMgr2.startExclusiveMode();

        txnMgr.startExclusiveMode();

        // No transactions on either database.
        // Switch.
        container.set(dsgCompact);
        txnMgr2.finishExclusiveMode();
        // New database running.

        // Clean-up.
        // txnMgr.finishExclusiveMode();
        // Don't call : txnMgr.startWriters();
        StoreConnection.release(dsgBase.getLocation());
    }
    
    private static Path findLocation(Path directory, String namebase) {
        if ( ! Files.exists(directory) )
            return null;
        // In-order, low to high.
        List<Path> maybe = IOX.scanForDirByPattern(directory, namebase, SEP);
        return Util.getLastOrNull(maybe);
    }
}
