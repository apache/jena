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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.base.Sys;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.IO_DB;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.sse.SSE_ParseException;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsBuilder;
import org.apache.jena.tdb2.params.StoreParamsCodec;
import org.apache.jena.tdb2.params.StoreParamsFactory;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operations related to TDB2 databases.
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
    // Composition of a database storage area directory name.
    public static final String dbNameBase       = "Data";
    public static final String SEP              = "-";
    public static final String dbSuffixPattern  = "[\\d]+";

    public static final String startCount       = "0001";

    // Additional suffix used during compact
    private static final String dbTmpSuffix      = "-tmp";
    private static final String dbTmpPattern     = "[\\d]+"+dbTmpSuffix;

    private static final String BACKUPS_DIR      = "Backups";
    // Basename of the backup file. "backup_{DateTime}.nq.gz
    private static final String BACKUPS_FN       = "backup";

    // A file name for a list of files to remove in cleanDatabaseDirectory()
    // while initializing the area for TDB2 usage in this JVM.
    /*package*/ static final String incompleteWIP    = "jena-tdb-temp-files";

    private enum ScanAccept { EXACT, SKIP }

    /**
     * Create a fresh database - called by {@code DatabaseMgr}.
     * It is important to go via {@code DatabaseConnection} to avoid
     * duplicate {@code DatasetGraphSwitchable}s for the same location.
     */
    /*package*/ static DatasetGraph create(Location location, StoreParams params, ReorderTransformation reorderTransform) {
        // Hide implementation class.
        return createSwitchable(location, params, reorderTransform);
    }

    private static boolean warnAboutOptimizer = true;

    private static DatasetGraphSwitchable createSwitchable(Location containerLocation, StoreParams appParams, ReorderTransformation appReorderTransform) {
        if ( containerLocation.isMem() ) {
            // A memory store is create in the container directly - compact does not apply.
            Location storageLocation = containerLocation;
            StoreParams params = appParams != null ? appParams : StoreParams.getDftMemStoreParams();
            ReorderTransformation reorderTransform = (appReorderTransform != null) ? appReorderTransform : ReorderLib.fixed();
            DatasetGraph dsg = StoreConnection.connectCreate(storageLocation, params, reorderTransform).getDatasetGraph();
            return new DatasetGraphSwitchable(null, containerLocation, dsg);
        }
        // Exists?
        if ( ! containerLocation.exists() )
            throw new TDBException("No such location: "+containerLocation);
        Path path = IO_DB.asPath(containerLocation);

        // Clean any temporary files and directories that there might be.
        cleanDatabaseDirectory(path);

        // Scan for DBs
        Path existingStorage = findStorageLocation(path);
        boolean isNewArea = (existingStorage == null);

        Path db = existingStorage;
        if ( db == null ) {
            db = path.resolve(dbNameBase+SEP+startCount);
            IOX.createDirectory(db);
        }

        Location storageLocation = IO_DB.asLocation(db);

        // ---- Find the params (if any).
        StoreParams switchableParams = StoreParamsCodec.read(containerLocation);
        StoreParams storageParams    = StoreParamsCodec.read(storageLocation);
        StoreParams dftParams        = containerLocation.isMem() ? StoreParams.getDftMemStoreParams() : StoreParams.getDftStoreParams();

        StoreParams params = StoreParamsFactory.decideStoreParams(null, isNewArea, appParams, switchableParams, storageParams, dftParams);

        // If new and some form of custom setup (appParams) passed in by code: write it to the container location
        if ( isNewArea && /* !containerLocation.isMem() &&*/ switchableParams == null && storageParams == null && ! params.equals(dftParams) ) {
            StoreParamsCodec.write(containerLocation, params);
        }

        // ---- Reorder
        ReorderTransformation reorderTransform = appReorderTransform;
        reorderTransform = maybeTransform(reorderTransform, storageLocation);
        reorderTransform = maybeTransform(reorderTransform, containerLocation);
        if ( reorderTransform == null )
            reorderTransform = SystemTDB.getDefaultReorderTransform();

        if ( reorderTransform == null && warnAboutOptimizer )
            ARQ.getExecLogger().warn("No BGP optimizer");

        DatasetGraphTDB dsg = StoreConnection.connectCreate(storageLocation, params, reorderTransform).getDatasetGraphTDB();
        DatasetGraphSwitchable appDSG = new DatasetGraphSwitchable(path, containerLocation, dsg);
        return appDSG;
    }

    /**
     * Clear out any partial compactions.
     */
    private static void cleanDatabaseDirectory(Path containerDirectory) {
        // Remove "-tmp" directories.
        List<Path> tmpDirs = scanForDirByPattern(containerDirectory, dbNameBase, SEP, dbTmpPattern, ScanAccept.SKIP);
        for ( Path dir : tmpDirs ) {
            FmtLog.info(LOG, "Remove incomplete compaction temporary directory: "+dir);
            IO.deleteAll(dir);
        }
        // Remove anything listed in "jena-tdb-temp-files" (used by Windows compaction)
        try {
            Path workfileList = containerDirectory.resolve(incompleteWIP);
            if ( Files.exists(workfileList) ) {
                List<String> filenames = Files.readAllLines(workfileList);
                for ( String fn : filenames) {
                    FmtLog.info(LOG, "Remove incomplete work-in-progress: "+fn);
                    Path path = Path.of(fn);
                    if ( Files.exists(path) ) {
                        try {
                            IO.deleteAll(path);
                        } catch (Throwable ex) {
                            FmtLog.error(LOG, "Exception while deleting "+fn+ " : manual clean-up required");
                        }
                    }
                }
                // Remove the list of clean-ups.
                Files.delete(workfileList);
            }
        } catch (IOException e) {
            throw IOX.exception(e);
        }
    }

    private static ReorderTransformation maybeTransform(ReorderTransformation reorderTransform, Location location) {
        if ( reorderTransform != null )
            return reorderTransform;
        return chooseReorderTransformation(location);
    }

    private static StoreParams buildStoreParams(boolean isNewArea, Location paramsLocation, StoreParams switchableParams, StoreParams storageParams, StoreParams appParams, StoreParams dftParams) {
        StoreParams params = null;
        params = buildParamsHelper(params, storageParams);
        params = buildParamsHelper(params, switchableParams);
        params = buildParamsHelper(params, appParams);

        if ( isNewArea && params != null && ! params.equals(dftParams) ) {
            StoreParamsCodec.write(paramsLocation, params);
        }

        if ( storageParams != null )
            params = storageParams;
        if ( switchableParams != null ) {
            if ( params != null  )
                params = StoreParamsBuilder.modify(params, switchableParams);
            else
                params = switchableParams;
        }
        if ( appParams != null ) {
            if ( params != null  )
                params = StoreParamsBuilder.modify(params, appParams);
            else
                params = appParams;
        }
        return params;
    }

    private static StoreParams buildParamsHelper(StoreParams baseParams, StoreParams additionalParams) {
        if ( baseParams == null )
            return additionalParams;
        if ( additionalParams == null )
            return null;
        return StoreParamsBuilder.modify(baseParams, additionalParams);
    }

    public static String backup(DatasetGraphSwitchable container) {
        checkSupportsAdmin(container);
        Path dbPath = container.getContainerPath();
        Path backupDir = dbPath.resolve(BACKUPS_DIR);
        if ( ! Files.exists(backupDir) )
            IOX.createDirectory(backupDir);

        DatasetGraph dsg = container;

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
        if ( Sys.isWindows) {
            // Windows does not support Files.move when the directory contains memory mapped files.
            // MS Windows: 2024-03-08 https://github.com/apache/jena/issues/2315
            // Moving the temporary directory does not work.
            DatabaseOpsWindows.compact_win(container, shouldDeleteOld);
            return;
        }

        checkSupportsAdmin(container);
        synchronized(compactionLock) {
            Path containerPath = container.getContainerPath();
            Path db1 = findStorageLocation(containerPath);
            if ( db1 == null )
                throw new TDBException("No location: ("+containerPath+", "+dbNameBase+")");
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
            int v = extractIndex(db1.getFileName().toString(), dbNameBase, SEP);
            String next = FilenameUtils.filename(dbNameBase, SEP, v+1);

            Path db2 = db1.getParent().resolve(next);
            LOG.debug(String.format("Compact %s -> %s\n", db1.getFileName(), db2.getFileName()));
            if ( Files.exists(db2) )
                throw new TDBException("Inconsistent : "+db2+" already exists");
            // End checks

            // Location of the storage area for the compacted database.
            // This is a temporary directory that is atomically moved into place when complete.
            // This is not supported by MS Windows.

            Path tmpDir = makeTempDirName(db2);
            if ( Files.exists(tmpDir) )
                throw new TDBException("Inconsistent : tmpdir"+tmpDir+" already exists");
            IOX.createDirectory(tmpDir);
            Location loc2tmp = Location.create(tmpDir);

            try {
                compaction(container, loc1, loc2tmp, db2);
                // Container now using the new location.
                // The original database is not in use.
            } catch (RuntimeIOException ex) {
                // Clear up - disk problems.
                try { IO.deleteAll(tmpDir); } catch (Throwable th) { /* Continue with original error. */ }
                throw ex;
            } catch (Throwable th) {
                // Jena and Java errors
                try { IO.deleteAll(tmpDir); } catch (Throwable th2) { /* Continue with original error. */ }
                throw th;
            }

            if ( shouldDeleteOld ) {
                // Compact put each of the databases into exclusive mode to do the switchover.
                // There are no previous transactions on the old database at this point.
                Path loc1Path = IO_DB.asPath(loc1);
                LOG.debug("Deleting old database after successful compaction (old db path='" + loc1Path + "')...");
                IO.deleteAll(loc1Path);
            }
        }
    }

    private static Path makeTempDirName(Path path) {
        String dirname = path.toString();
        if ( dirname.endsWith("/"))
            dirname = StrUtils.chop(dirname);
        return Path.of(dirname+dbTmpSuffix);
    }

    /** Copy the latest version from one location to another. */
    private static void compaction(DatasetGraphSwitchable container, Location loc1, Location loc2tmp, Path path2final) {
        if ( loc1.isMem() || loc2tmp.isMem() )
            throw new TDBException("Compact involves a memory location: "+loc1+" : "+loc2tmp);

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
            copyConfigFiles(loc1, loc2tmp);

            // -- Copy to temporary area so that if the copy is interrupted
            // (e.g. the server exits mid-copy) the restart does choose the
            // directory as the data storage.

            DatasetGraphTDB dsgTmpCompact = StoreConnection.connectCreate(loc2tmp).getDatasetGraphTDB();
            CopyDSG.copy(dsgBase, dsgTmpCompact);
            StoreConnection.internalExpel(loc2tmp, true);
            // Now on-disk in tmp location.

            moveDirectory(loc2tmp, path2final);
            Location loc2final = Location.create(path2final);   // Location must exist.

            // Next generation storage datasetGraph.
            DatasetGraphTDB dsgCompact = StoreConnection.connectCreate(loc2final).getDatasetGraphTDB();

            // -- Switch
            // Update TransactionCoordinator and switch over.
            TransactionCoordinator txnMgr2 = dsgCompact.getTxnSystem().getTxnMgr();
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
        // Includes dsgBase.shutdown() which closes files.
        StoreConnection.release(dsgBase.getLocation());
    }

    private static void moveDirectory(Location locTmp, Path pathDst) {
        Path pathSrc = IO_DB.asPath(locTmp);
        try {
            // MS Windows: Fails when the directory has memory mapped files in it.
            Files.move(pathSrc, pathDst);
        } catch (IOException ex) { throw IOX.exception(ex); }
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

    /**
     * Find the files in this directory that have namebase as a prefix and
     * are then numbered.
     *  <p>
     * Returns a sorted list from, low to high index.
     * @param directory Path to the data base directory
     * @param namebase Initial common component of the name
     * @param nameSep  Separator
     * @param trailerPattern Pattern for the part of the name after the namebase.
     * @return List<Path> List sorted low to high
     */
    private static List<Path> scanForDirByPattern(Path directory, String namebase, String nameSep, String trailerPattern, ScanAccept skipOthers) {
        Pattern pattern = Pattern.compile(Pattern.quote(namebase)+
                                          Pattern.quote(nameSep)+
                                          trailerPattern);
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, namebase + "*")) {
            for ( Path entry : stream ) {
                String filename = entry.getFileName().toString();
                if ( !pattern.matcher(filename).matches() ) {
                    switch ( skipOthers ) {
                        case EXACT: throw new DBOpEnvException("Invalid filename for matching: "+entry.getFileName());
                        case SKIP:  continue;
                    }
                }
                // Follows symbolic links.
                if ( !Files.isDirectory(entry) )
                    throw new DBOpEnvException("Not a directory: "+entry);
                paths.add(entry);
            }
        }
        catch (IOException ex) {
            FmtLog.warn(IO_DB.class, "Can't inspect directory: (%s, %s)", directory, namebase);
            throw new DBOpEnvException(ex);
        }
        Comparator<Path> comp = (f1, f2) -> {
            int num1 = extractIndex(f1.getFileName().toString(), namebase, nameSep);
            int num2 = extractIndex(f2.getFileName().toString(), namebase, nameSep);
            return Integer.compare(num1, num2);
        };
        paths.sort(comp);
        //indexes.sort(Long::compareTo);
        return paths;
    }

    private static Pattern numberPattern = Pattern.compile("[\\d]+");
    /** Given a filename in "base-NNNN(-text)" format, return the value of NNNN */
    private static int extractIndex(String name, String namebase, String nameSep) {
        Matcher matcher = numberPattern.matcher(name);
        if ( matcher.find() ) {
            var numStr = matcher.group();
            int num = Integer.parseInt(numStr);
            return num;
        } else {
            throw new InternalErrorException("Expected to find a number in '"+name+"'");
        }
    }

    /**
     * Find the active working storage area for a TDB2 database.
     * Return null if none.
     */
    public static Path findStorageLocation(Location directory) {
        Path dirPath = IO_DB.asPath(directory);
        return findStorageLocation(dirPath);
    }

    /**
     * Find the active working storage area for a TDB2 database.
     * Return null if none.
     */
    public static Path findStorageLocation(Path directory) {
        if ( ! Files.exists(directory) )
            return null;
        // In-order, low to high.
        List<Path> maybe = scanForDirByPattern(directory, dbNameBase, SEP, dbSuffixPattern, ScanAccept.EXACT);
        return Util.getLastOrNull(maybe);
    }

    // Find an optimizer settings file at a locations.
    public static ReorderTransformation chooseReorderTransformation(Location location) {
        if ( location == null )
            return ReorderLib.identity();

        ReorderTransformation reorder = null;
        if ( location.exists(Names.optStats) ) {
            try {
                reorder = ReorderLib.weighted(location.getPath(Names.optStats));
                LOG.debug("Statistics-based BGP optimizer");
            }
            catch (SSE_ParseException ex) {
                LOG.warn("Error in stats file: " + ex.getMessage());
                reorder = null;
            }
        }

        if ( location.exists(Names.optFixed) ) {
            reorder = ReorderLib.fixed();
            LOG.debug("Fixed pattern BGP optimizer");
        }

        if ( location.exists(Names.optNone) ) {
            reorder = ReorderLib.identity();
            LOG.debug("Optimizer explicitly turned off");
        }

        return reorder;
    }
}
