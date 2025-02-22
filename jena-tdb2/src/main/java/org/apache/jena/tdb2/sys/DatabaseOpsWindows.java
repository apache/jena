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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.IO_DB;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Old DatabaseOps code for compaction (up to Jena 5.0.0-rc1).
 * Needed for MS Windows:
 * (1) does not allow directories with memory -mapped file to be renamed/moved
 * (2) does not release memory-mapped files until the JVM exits
 */
class DatabaseOpsWindows {
    private static Logger LOG = LoggerFactory.getLogger(DatabaseOpsWindows.class);
    /*public*/ static final String dbPrefix     = "Data";
    /*public*/ static final String SEP          = "-";
    /*public*/ static final String startCount   = "0001";

    // JVM-wide :-(
    private static Object compactionLock = new Object();

    // Windows specific compaction.
    /*public*/ static void compact_win(DatasetGraphSwitchable container, boolean shouldDeleteOld) {
        checkSupportsAdmin(container);
        synchronized(compactionLock) {
            Path base = container.getContainerPath();
            Path db1 = findLocation(base, dbPrefix);
            if ( db1 == null )
                throw new TDBException("No location: ("+base+", "+dbPrefix+")");
            Location loc1 = IO_DB.asLocation(db1);

            // -- Checks
            Location loc1a = ((DatasetGraphTDB)container.get()).getLocation();
            if ( ! loc1a.exists() )
                throw new TDBException("No such location: "+loc1a);

            // Is this the same database location?
            if ( ! loc1.equals(loc1a) )
                throw new TDBException("Inconsistent (not latest?) : "+loc1a+" : "+loc1);

            // Check version
            int v = extractIndexX(db1.getFileName().toString(), dbPrefix, SEP);
            String next = FilenameUtils.filename(dbPrefix, SEP, v+1);
            Path db2 = db1.getParent().resolve(next);

            // Write note about location to be used into the container
            Path inProgress = base.resolve(DatabaseOps.incompleteWIP);
            try {
                Files.writeString(inProgress, db2.toAbsolutePath().toString()+"\n");
            } catch(IOException ex) {
                // Try clean-up
                try { Files.delete(inProgress); } catch(IOException ex2) { }
                throw IOX.exception(ex);
            }

            IOX.createDirectory(db2);
            Location loc2 = IO_DB.asLocation(db2);
            LOG.debug(String.format("Compact %s -> %s\n", db1.getFileName(), db2.getFileName()));

            try {
                compaction_win(container, loc1, loc2);
                // Container now using the new location.
                // The original database is not in use.
            } catch (RuntimeIOException ex) {
                // Clear up - disk problems.
                try { IO.deleteAll(db2); } catch (Throwable th) { /* Continue with original error. */ }
                throw ex;
            } catch (Throwable th) {
                // Jena and Java errors
                try { IO.deleteAll(db2); } catch (Throwable th2) { /* Continue with original error. */ }
                throw th;
            }

            try {
                // Remove note about location.
                Files.delete(inProgress);
            } catch(IOException ex) {
                throw IOX.exception(ex);
            }

            if ( shouldDeleteOld ) {
                // Compact put each of the databases into exclusive mode to do the switchover.
                // There are no previous transactions on the old database at this point.
                // MS Windows. Does not take effect until the JVM exits.
                Path loc1Path = IO_DB.asPath(loc1);
                LOG.warn("Database will not be fully deleted until after a server restart: (old db path='" + loc1Path + "')");
                deleteDatabase(loc1Path);
            }
        }
    }

    /** Copy the latest version from one location to another. */
    private static void compaction_win(DatasetGraphSwitchable container, Location loc1, Location loc2) {
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
                    Log.warn(DatabaseOpsWindows.class, "Inconsistent: old datasetgraph not as expected");
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
        // MS Windows does not fully release memory mapped until the JVM exits.
        StoreConnection.release(dsgBase.getLocation());
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
        List<Path> maybe = scanForDirByPatternX(directory, namebase, SEP);
        return Util.getLastOrNull(maybe);
    }

    private static void checkSupportsAdmin(DatasetGraphSwitchable container) {
        if ( ! container.hasContainerPath() )
            throw new TDBException("Dataset does not support admin operations");
    }

    /** Find the files in this directory that have namebase as a prefix and
     *  are then numbered.
     *  <p>
     *  Returns a sorted list from, low to high index.
     */
    private static List<Path> scanForDirByPatternX(Path directory, String namebase, String nameSep) {
        Pattern pattern = Pattern.compile(Pattern.quote(namebase)+
                                          Pattern.quote(nameSep)+
                                          "[\\d]+");
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, namebase + "*")) {
            for ( Path entry : stream ) {
                if ( !pattern.matcher(entry.getFileName().toString()).matches() ) {
                    throw new DBOpEnvException("Invalid filename for matching: "+entry.getFileName());
                    // Alternative: Skip bad trailing parts but more likely there is a naming problem.
                    //   LOG.warn("Invalid filename for matching: {} skipped", entry.getFileName());
                    //   continue;
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
            int num1 = extractIndexX(f1.getFileName().toString(), namebase, nameSep);
            int num2 = extractIndexX(f2.getFileName().toString(), namebase, nameSep);
            return Integer.compare(num1, num2);
        };
        paths.sort(comp);
        //indexes.sort(Long::compareTo);
        return paths;
    }

    private static Pattern numberPattern = Pattern.compile("[\\d]+");
    /** Given a filename in "base-NNNN(-text)" format, return the value of NNNN */
    private static int extractIndexX(String name, String namebase, String nameSep) {
        Matcher matcher = numberPattern.matcher(name);
        if ( matcher.find() ) {
            var numStr = matcher.group();
            int num = Integer.parseInt(numStr);
            return num;
        } else {
            throw new InternalErrorException("Expected to find a number in '"+name+"'");
        }
    }
}
