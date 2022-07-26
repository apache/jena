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

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.IO_DB;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Operations on and about TDB2 databases.
 * <p>
 * TDB2 uses a hierarchical structure to manage on disk.
 * <p>
 * If in-memory (non-scalable, not-performant, perfect simulation for functionality using a RAM disk, for testing mainly),
 * then the switchable layer is just a convenience. DatasetGrapOps such as {@link #compact}
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
    public static final String dbPrefix     = "Data";
    public static final String SEP          = "-";
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
        Path path = IO_DB.asPath(location);
        // Scan for DBs
        Path db = findLocation(path, dbPrefix);
        if ( db == null ) {
            db = path.resolve(dbPrefix+SEP+startCount);
            IOX.createDirectory(db);
        }
        Location loc2 = IO_DB.asLocation(db);
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
        //          Log.warn(Fuseki.serverLog, "Backup already in progress");
        //      activeBackups.add(dsg);
        //  }

        Pair<OutputStream, Path> x = openUniqueFileForWriting(backupDir, BACKUPS_FN, "nq.gz");
        try (OutputStream out2 = x.getLeft();
             OutputStream out1 = new GZIPOutputStream(out2, 8 * 1024);
             OutputStream out = new BufferedOutputStream(out1)) {
            Txn.executeRead(dsg, ()->RDFDataMgr.write(out, dsg, Lang.NQUADS));
        } catch (IOException e) {
            throw IOX.exception(e);
        }
        return x.getRight().toString();
    }

    private static void checkSupportsAdmin(DatasetGraphSwitchable container) {
        if ( ! container.hasContainerPath() )
            throw new TDBException("Dataset does not support admin operations");
    }

    // --> IOX
    private static Pair<OutputStream, Path> openUniqueFileForWriting(Path dirPath, String basename, String ext) {
        if ( ! Files.isDirectory(dirPath) )
            throw new IllegalArgumentException("Not a directory: "+dirPath);
        if ( basename.contains("/") || basename.contains("\\") )
            throw new IllegalArgumentException("Basename must not contain a file path separator (\"/\" or \"\\\")");

        String timestamp = DateTimeUtils.nowAsString("yyyy-MM-dd_HHmmss");
        String filename = basename + "_" + timestamp;
        Path p = dirPath.resolve(filename+"."+ext);
        int x = 0;
        for(;;) {
            try {
                OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE_NEW);
                return Pair.create(out, p);
            } catch (AccessDeniedException ex)  {
                throw IOX.exception("Access denied", ex);
            } catch (FileAlreadyExistsException ex) {
                // Drop through and try again.
            } catch (IOException ex) {
                throw IOX.exception(ex);
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

    /**
     * Equivalent to {@code compact(container, false)}.
     */
    public static void compact(DatasetGraphSwitchable container) {
        compact(container, false);
    }

    public static void compact(DatasetGraphSwitchable container, boolean shouldDeleteOld) {
        checkSupportsAdmin(container);
        synchronized(compactionLock) {
            Path base = container.getContainerPath();
            Path db1 = findLocation(base, dbPrefix);
            if ( db1 == null )
                throw new TDBException("No location: ("+base+", "+dbPrefix+")");
            Location loc1 = IO_DB.asLocation(db1);

            // -- Checks
            Location loc1a = ((DatasetGraphTDB)container.get()).getLocation();
            if ( loc1a.isMem() ) {}
            if ( ! loc1a.exists() )
                throw new TDBException("No such location: "+loc1a);

            // Is this the same database location?
            if ( ! loc1.equals(loc1a) )
                throw new TDBException("Inconsistent (not latest?) : "+loc1a+" : "+loc1);

            // Check version
            int v = IO_DB.extractIndex(db1.getFileName().toString(), dbPrefix, SEP);
            String next = FilenameUtils.filename(dbPrefix, SEP, v+1);

            Path db2 = db1.getParent().resolve(next);
            IOX.createDirectory(db2);
            Location loc2 = IO_DB.asLocation(db2);
            LOG.debug(String.format("Compact %s -> %s\n", db1.getFileName(), db2.getFileName()));

            compact(container, loc1, loc2);

            if ( shouldDeleteOld ) {
                // Compact put each of the databases into exclusive mode to do the switchover.
                // There are no previous transactions on the old database at this point.
                Path loc1Path = IO_DB.asPath(loc1);
                LOG.debug("Deleting old database after successful compaction (old db path='" + loc1Path + "')...");
                deleteDatabase(loc1Path);
            }
        }
    }

    private static void deleteDatabase(Path locationPath) {
        try (Stream<Path> walk = Files.walk(locationPath)){
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException ex) {
            throw IOX.exception(ex);
        }
    }

    /** Copy the latest version from one location to another. */
    private static void compact(DatasetGraphSwitchable container, Location loc1, Location loc2) {
        if ( loc1.isMem() || loc2.isMem() )
            throw new TDBException("Compact involves a memory location: "+loc1+" : "+loc2);

        StoreConnection srcConn = StoreConnection.connectExisting(loc1);

        if ( srcConn == null )
            throw new TDBException("No database at location : "+loc1);
        if ( ! ( container.get() instanceof DatasetGraphTDB ) )
            throw new TDBException("Not a TDB2 database in DatasetGraphSwitchable");

        DatasetGraphTDB dsgCurrent = (DatasetGraphTDB)container.get();
        if ( ! dsgCurrent.getLocation().equals(loc1) )
            throw new TDBException("Inconsistent locations for base : "+dsgCurrent.getLocation()+" , "+dsgCurrent.getLocation());

        DatasetGraphTDB dsgBase = srcConn.getDatasetGraphTDB();
        if ( dsgBase != dsgCurrent )
            throw new TDBException("Inconsistent datasets : "+dsgCurrent.getLocation()+" , "+dsgBase.getLocation());

        TransactionCoordinator txnMgr1 = dsgBase.getTxnSystem().getTxnMgr();

        // -- Stop updates.
        // On exit there are no writers and none will start until switched over.
        // Readers can start on the old database.

        // Block writers on the container (DatasetGraphSwitchable)
        // while we copy the database to the new location.
        // These writer wait output the TransactionCoordinator (old and new)
        // until the switchover has happened.

        container.execReadOnlyDatabase(()->{
            // No active writers or promote transactions on the current database.
            // These are held up on a lock in the switchable container.

            // -- Copy the current state to the new area.
            copyConfigFiles(loc1, loc2);
            DatasetGraphTDB dsgCompact = StoreConnection.connectCreate(loc2).getDatasetGraphTDB();
            CopyDSG.copy(dsgBase, dsgCompact);

            if ( false ) {
                // DEVELOMENT. FAke a long copy time in state copy.
                System.err.println("-- Inside compact 1");
                Lib.sleep(3_000);
                System.err.println("-- Inside compact 2");
            }

            TransactionCoordinator txnMgr2 = dsgCompact.getTxnSystem().getTxnMgr();
            // Update TransactionCoordinator and switch over.
            txnMgr2.execExclusive(()->{
                // No active transactions in either database.
                txnMgr2.takeOverFrom(txnMgr1);

                // Copy over external transaction components.
                txnMgr2.modifyConfigDirect(()-> {
                    txnMgr1.listExternals().forEach(txnMgr2::addExternal);
                    // External listeners?
                    // (the NodeTableCache listener is not external)
                });

                // No transactions on new database 2 (not exposed yet).
                // No writers or promote transactions on database 1.
                // Maybe old readers on database 1.
                // -- Switch.
                if ( ! container.change(dsgCurrent, dsgCompact) ) {
                    Log.warn(DatabaseOps.class, "Inconsistent: old datasetgraph not as expected");
                    container.set(dsgCompact);
                }
                // The compacted database is now active
            });
            // New database running.
            // New transactions go to this database.
            // Old readers continue on db1.

        });


        // This switches off the source database.
        // It waits until all transactions (readers) have finished.
        // This call is not undone.
        // Database1 is no longer in use.
        txnMgr1.startExclusiveMode();
        // Clean-up.
        StoreConnection.release(dsgBase.getLocation());
    }

    /** Copy certain configuration files from {@code loc1} to {@code loc2}. */
    private static void copyConfigFiles(Location loc1, Location loc2) {
        FileFilter copyFiles  = (pathname)->{
            String fn = pathname.getName();
            if ( fn.equals(Names.TDB_CONFIG_FILE) )
                return true;
            if ( fn.endsWith(".opt") )
                return true;
            return false;
        };
        File d = new File(loc1.getDirectoryPath());
        File[] files = d.listFiles(copyFiles);
        copyFiles(loc1, loc2, files);
    }

    /** Copy a number of files from one location to another location. */
    private static void copyFiles(Location loc1, Location loc2, File[] files) {
        if ( files == null || files.length == 0 )
            return;
        for ( File f : files ) {
            String fn = f.getName();
            IOX.copy(loc1.getPath(fn), loc2.getPath(fn));
        }
    }

    private static Path findLocation(Path directory, String namebase) {
        if ( ! Files.exists(directory) )
            return null;
        // In-order, low to high.
        List<Path> maybe = IO_DB.scanForDirByPattern(directory, namebase, SEP);
        return Util.getLastOrNull(maybe);
    }
}
