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

import java.io.BufferedOutputStream ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.nio.file.* ;
import java.util.List;
import java.util.zip.GZIPOutputStream ;

import org.apache.jena.atlas.RuntimeIOException ;
import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.system.Txn;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.setup.StoreParams;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Operations on and about TDB2 databases. 
 * <p>
 * TDB2 uses a hierarchical structure to manage on disk.
 * <p>
 * If in-memory (non-scalable, not-performant, perfect simulation for functionality using a RAM disk, for testing mainly),
 * then the switchable layer is just a covenience. DatasetGrapOps such as {@link #compact}
 * and {@link #backup} do not apply.
 * <p>
 * Directory and files on disk:
 * <ul>
 * <li> {@code Data-NNNN/} -- databases by version. Compacting creates a new directory, leaving 
 * <li> store params, static (disk layout related - can not change once created) and dynamic (settings related to in-memory).
 * <li> {@code Backups/} -- backups.
 * <li> External indexes like {@code TextIndex/} -- the text index only applies to the current, latest database.
 * </ul>
 */
public class DatabaseOps {
    private static Logger LOG = LoggerFactory.getLogger(DatabaseOps.class); 
    private static final String dbPrefix     = "Data";
    private static final String SEP          = "-";
    private static final String startCount   = "0001";

    private static final String BACKUPS_DIR  = "Backups";
    // Basename of the backup file. "backup_{DateTime}.nq.gz
    private static final String BACKUPS_FN   = "backup";
    
    /** Create a fresh database - called by {@code DatabaseMgr}.
     * It is important to go via {@code DatabaseConnection} to avoid
     * duplicate {@code DatasetGraphSwitchable}s for the same location.
     */
    /*package*/ static DatasetGraph create(Location location, StoreParams params) {
        // Hide implementation class.
        return createSwitchable(location, params);
    }
    
    private static DatasetGraphSwitchable createSwitchable(Location location, StoreParams params) {
        if ( location.isMem() ) {
            DatasetGraph dsg = StoreConnection.connectCreate(location).getDatasetGraph();
            return new DatasetGraphSwitchable(null, location, dsg);
        }
        // Exists?
       if ( ! location.exists() )
           throw new TDBException("No such location: "+location);
       Path path = IOX.asPath(location);
       // Scan for DBs
       Path db = findLocation(path, dbPrefix);
       if ( db == null ) {
           db = path.resolve(dbPrefix+SEP+startCount);
           IOX.createDirectory(db);
       }
       Location loc2 = IOX.asLocation(db);
       DatasetGraphTDB dsg = StoreConnection.connectCreate(loc2, params).getDatasetGraphTDB();
       DatasetGraphSwitchable appDSG = new DatasetGraphSwitchable(path, location, dsg);
       return appDSG;
    }
    
    public static String backup(DatasetGraphSwitchable container) {
        checkSupportsAdmin(container);
        Path dbPath = container.getContainerPath();
        Path backupDir = dbPath.resolve(BACKUPS_DIR);
        if ( ! Files.exists(backupDir) )
            IOX.createDirectory(backupDir);
      
        DatasetGraph dsg = container;
        
//  // Per backup source lock. 
//  synchronized(activeBackups) {
//      // Atomically check-and-set
//      if ( activeBackups.contains(dsg) )
//          Log.warn(Fuseki.serverLog, "Backup already in progress") ;
//      activeBackups.add(dsg) ;
//  }

        Pair<OutputStream, Path> x = openUniqueFileForWriting(backupDir, BACKUPS_FN, "nq.gz");
        try (OutputStream out2 = x.getLeft();
             OutputStream out1 = new GZIPOutputStream(out2, 8 * 1024) ;
             OutputStream out = new BufferedOutputStream(out1)) {
            Txn.executeRead(dsg, ()->RDFDataMgr.write(out, dsg, Lang.NQUADS));
        } catch (IOException e) {
            throw IOX.exception(e) ;
        }
        return x.getRight().toString();
    }
    
    private static void checkSupportsAdmin(DatasetGraphSwitchable container) {
        if ( ! container.hasContainerPath() )
            throw new TDBException("Dataset does not support admin operations");
    }

    //private static void logWarn(String msg) {}
    
    
    // --> IOX
    private static Pair<OutputStream, Path> openUniqueFileForWriting(Path dirPath, String basename, String ext) {
        if ( ! Files.isDirectory(dirPath) )
            throw new IllegalArgumentException("Not a directory: "+dirPath);
        if ( basename.contains("/") || basename.contains("\\") )
            throw new IllegalArgumentException("Basename must not contain a file path separator (\"/\" or \"\\\")");
        
        String timestamp = DateTimeUtils.nowAsString("yyyy-MM-dd_HHmmss") ;
        String filename = basename + "_" + timestamp ;
        Path p = dirPath.resolve(filename+"."+ext);
        int x = 0 ;
        for(;;) {
            try {
                OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE_NEW); 
                return Pair.create(out, p);
            } catch (AccessDeniedException ex)  { 
                throw IOX.exception("Access denied", ex);
            } catch (FileAlreadyExistsException ex) {
                // Drop through and try again.
            } catch (IOException ex) {
                throw IOX.exception(ex) ;
            }
            // Try again.
            x++;
            if ( x >= 5 )
                throw new RuntimeIOException("Can't create the unique name: number of attempts exceeded");
            p = dirPath.resolve(filename+"_"+x+"."+ext);
        }
    }

    
    // JVM-wide :-(
    private static Object compactionLock = new Object();
    
    public static void compact(DatasetGraphSwitchable container) {
        checkSupportsAdmin(container);
        synchronized(compactionLock) {
            Path base = container.getContainerPath();
            Path db1 = findLocation(base, dbPrefix);
            Location loc1 = IOX.asLocation(db1);

            // -- Checks
            Location loc1a = ((DatasetGraphTDB)container.get()).getLocation();
            if ( loc1a.isMem() ) {}
            if ( ! loc1a.exists() )
                throw new TDBException("No such location: "+loc1a);
            
            // Is this the same database location?
            if ( ! loc1.equals(loc1a) )
                throw new TDBException("Inconsistent (not latested?) : "+loc1a+" : "+loc1);
            // -- Checks
            
            // Version
            int v = IOX.extractIndex(db1.getFileName().toString(), dbPrefix, SEP);
            String next = FilenameUtils.filename(dbPrefix, SEP, v+1);
            
            Path db2 = db1.getParent().resolve(next);
            IOX.createDirectory(db2);
            Location loc2 = IOX.asLocation(db2);
            LOG.debug(String.format("Compact %s -> %s\n", db1.getFileName(), db2.getFileName()));
            
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
        
        DatasetGraphTDB dsgCurrent = (DatasetGraphTDB)container.get();
        if ( ! dsgCurrent.getLocation().equals(loc1) )
            throw new TDBException("Inconsistent locations for base : "+dsgCurrent.getLocation()+" , "+dsgCurrent.getLocation());
        
        DatasetGraphTDB dsgBase = srcConn.getDatasetGraphTDB();
        if ( dsgBase != dsgCurrent )
            throw new TDBException("Inconsistent datasets : "+dsgCurrent.getLocation()+" , "+dsgBase.getLocation());
        
        TransactionalSystem txnSystem = dsgBase.getTxnSystem();
        TransactionCoordinator txnMgr = dsgBase.getTxnSystem().getTxnMgr();
        
        // Stop update.  On exit there are no writers and none will start until switched over.
        txnMgr.tryBlockWriters();
        // txnMgr.begin(WRITE, false) will now bounce.
        
        // Copy the latest generation.
        DatasetGraphTDB dsgCompact = StoreConnection.connectCreate(loc2).getDatasetGraphTDB();
        CopyDSG.copy(dsgBase, dsgCompact);   

        TransactionCoordinator txnMgr2 = dsgCompact.getTxnSystem().getTxnMgr();
        txnMgr2.startExclusiveMode();

        txnMgr.startExclusiveMode();

        // No transactions on either database.
        // Switch.
        if ( ! container.change(dsgCurrent, dsgCompact) ) {
            Log.warn(DatabaseOps.class, "Inconistent: old datasetgraph not as expected");
            container.set(dsgCompact);
        }
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
