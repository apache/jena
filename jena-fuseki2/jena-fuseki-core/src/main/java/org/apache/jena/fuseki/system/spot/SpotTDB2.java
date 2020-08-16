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

package org.apache.jena.fuseki.system.spot;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsCodec;
import org.apache.jena.tdb2.sys.DatabaseOps;
import org.apache.jena.tdb2.sys.IOX;
import org.apache.jena.tdb2.sys.Util;

class SpotTDB2 {

    /* TDB2 layout
     * top:
     *
     * Data-0001/  tdb.lock
     *
     * Data-0001:
     * GOSP.bpt  GPU.dat       nodes.dat       OSPG.dat  POSG.idn           SPO.bpt
     * GOSP.dat  GPU.idn       nodes-data.bdf  OSPG.idn  POS.idn            SPO.dat
     * GOSP.idn  GSPO.bpt      nodes-data.obj  OSP.idn   prefixes.bpt       SPOG.bpt
     * GPOS.bpt  GSPO.dat      nodes.idn       POS.bpt   prefixes.dat       SPOG.dat
     * GPOS.dat  GSPO.idn      OSP.bpt         POS.dat   prefixes-data.bdf  SPOG.idn
     * GPOS.idn  journal.jrnl  OSP.dat         POSG.bpt  prefixes-data.obj  SPO.idn
     * GPU.bpt   nodes.bpt     OSPG.bpt        POSG.dat  prefixes.idn       tdb.lock
     */
//
//    // TEMP
//    // These are in DatabaseOps
//    private static final String dbPrefix     = "Data";
//    private static final String SEP          = "-";

    public static boolean isTDB2(String pathname) {
        return isTDB2(Location.create(pathname));
    }

    /**
     * Test to see is a location is either empty (and a fresh TDB2 database can be
     * created there) or has looks like it is an existing TDB2 database.
     * See {@link #checkTDB2(Location)} for a test that the location is a valid, existing
     * database.
     */
    public static boolean isTDB2(Location location) {
        if ( location.isMem() )
            return true;
        if ( ! location.exists() )
            return false;
        if ( isEmpty(location) )
            return true;
        // Look for Data-*
        Path db = storageDir(location);
        if ( db == null )
            // Uninitialized?
            // Or TDB1?
            return ! SpotTDB1.isTDB1(location.getDirectoryPath());
        // Validate storage.
        Location storageLocation = IOX.asLocation(db);
        return isTDB2_Storage(storageLocation);
    }

    /** Quick check for TDB2. Does not check for validity. */
    private static boolean isTDB2_Storage(Location location) {
        // Journal check.
        //return exists(location, Names.journalFileBase, Names.extJournal);
        // Test for triples primary index
        StoreParams params = getStoreParams(location);
        return exists(location, params.getPrimaryIndexTriples(),
            Names.extBptTree, Names.extBptRecords, Names.extBptState);
    }

    public static void checkTDB2(String pathname) {
        checkTDB2(Location.create(pathname));
    }

    public static void checkTDB2(Location location) {
        if ( location.isMem() )
            return;
        if ( isEmpty(location) )
            return;
        Path db = storageDir(location);
        if ( db == null )
            // Uninitialized
            return ;
        // Storage. Easier to work in "Location".
        Location locStorage = Location.create(db);
        checkStorageArea(locStorage);
    }

    /** Return the current active database area within a database directory. */
    private static Path storageDir(Location location) {
        // Database directory
        Path path = IOX.asPath(location);
        // Storage directory in database directory.
        Path db = findLocation(path, DatabaseOps.dbPrefix);
        return db;
    }

    private static Path findLocation(Path directory, String namebase) {
        if ( ! Files.exists(directory) )
            return null;
        // In-order, low to high.
        List<Path> maybe = IOX.scanForDirByPattern(directory, namebase, DatabaseOps.SEP);
        return Util.getLastOrNull(maybe);
    }

    // Places for StoreParams: location or default
    private static StoreParams getStoreParams(Location location) {
        StoreParams params = StoreParamsCodec.read(location);
        if ( params == null )
            params = StoreParams.getDftStoreParams();
        return params;
    }

    /**
     * Check all files exist for a TDB2 database, or the area is empty (and so a new
     * database can be created in the location). Throw {@link TDBException} is a file
     * is missing.
     */
    private static void checkStorageArea(Location location) {

        if ( location.isMem() )
            return;

        if ( isEmpty(location) )
            return;

        // Journal, fixed name.
        validate(location, Names.journalFileBase, Names.extJournal);

        // Places for StoreParams: location or default
        StoreParams params = getStoreParams(location);

        // Check for indexes
        containsIndex(params.getPrimaryIndexTriples(), params.getTripleIndexes());
        validateBPT(location, params.getTripleIndexes());

        containsIndex(params.getPrimaryIndexQuads(), params.getTripleIndexes());
        validateBPT(location, params.getQuadIndexes());

        // prefixes. GPU
        containsIndex(params.getPrimaryIndexPrefix(), params.getPrefixIndexes());
        // GPU is not in files "GPU"
        // Filename of GPU.
        validateBPT(location, params.getPrefixIndexes());

        //---- Node tables.
        /*
         * nodes.bpt
         * nodes.dat
         * nodes-data.bdf
         * nodes-data.obj
         */
        validateBPT(location, params.getNodeTableBaseName());
        validateDAT(location, params.getNodeTableBaseName()+"-data");

        /*
         * prefixes...
         */
        // XXX validateBPT(location, params.getPrefixTableBaseName());
        validateBPT(location, params.getPrefixTableBaseName());
        validateDAT(location, params.getPrefixTableBaseName()+"-data");

    }

    private static void validateDAT(Location location, String file) {
        validate(location, file, Names.extObjNodeData, Names.extBdfState);
    }

    private static void containsIndex(String primaryIdx, String[] tripleIndexes) {
        if ( Arrays.stream(tripleIndexes).findFirst().isPresent() )
            return;
        List<String> list = Arrays.asList(tripleIndexes);
        throw new TDBException("Missing primary in index list: "+primaryIdx+" "+list);
    }

    private static void validateBPT(Location location, String[] indexes) {
        for ( String idx : indexes )
            validateBPT(location, idx);
    }

    private static void validateBPT(Location location, String index) {
        validate(location, index, Names.extBptTree, Names.extBptRecords, Names.extBptState);
    }

    private static void validate(Location location, String basename, String ... exts) {
        if ( exts.length == 0 ) {
            String fn = location.absolute(basename);
            if ( location.exists(basename) )
                good(fn);
            else
                throw missingFile(fn);
            return;
        }

        for ( String ext : exts ) {
            String fn = location.absolute(basename, ext);
            if ( location.exists(basename, ext) )
                good(location, basename, ext);
            else
                throw missingFile(fn);
        }
    }

    private static boolean isEmpty(Location location) {
        Path path = Paths.get(location.getDirectoryPath());
        try(DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
            return ! entries.iterator().hasNext();
        } catch(IOException ex) {
            IO.exception(ex);
            return false;
        }
    }

    private static boolean exists(Location location, String baseName, String ... exts) {
        if ( exts.length == 0 )
            return location.exists(baseName);

        for ( String ext : exts ) {
            if ( ! location.exists(baseName, ext) ) return false;
        }
        return true;
    }

    private static RuntimeException missingFile(String filename) {
        return new TDBException("No such file: "+filename);
    }

    private static void good(Location location, String basename, String ext) {}
    private static void good(String filename) {}
}

