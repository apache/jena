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
import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.tdb.TDBException;
import org.apache.jena.tdb.base.file.Location;
import org.apache.jena.tdb.setup.StoreParams;
import org.apache.jena.tdb.setup.StoreParamsCodec;
import org.apache.jena.tdb.sys.Names;

class SpotTDB1 {
    /* TDB1 layout
     *
     * GOSP.dat  GSPO.dat      node2id.idn  OSPG.idn  POSG.idn       prefixes.dat   SPOG.dat
     * GOSP.idn  GSPO.idn      nodes.dat    OSP.idn   POS.idn        prefixIdx.dat  SPOG.idn
     * GPOS.dat  journal.jrnl  OSP.dat      POS.dat   prefix2id.dat  prefixIdx.idn  SPO.idn
     * GPOS.idn  node2id.dat   OSPG.dat     POSG.dat  prefix2id.idn  SPO.dat
     */

    /* StoreParams
     * primaryIndexTriples    dft:SPO
     * tripleIndexes          dft:[SPO, POS, OSP]
     *
     * primaryIndexQuads      dft:GSPO
     * quadIndexes            dft:[GSPO, GPOS, GOSP, POSG, OSPG, SPOG]
     *
     * primaryIndexPrefix     dft:GPU
     * prefixIndexes          dft:[GPU]
     * -- Actual name of GPU index.
     * indexPrefix            dft:prefixIdx BPT
     *
     * -- NodeTable
     * indexNode2Id           dft:node2id
     * indexId2Node           dft:nodes
     *
     * -- NodeTable
     * prefixNode2Id          dft:prefix2id BPT *
     * prefixId2Node          dft:prefixes DAT *
     */

    /**
     * Test to see is a location is either empty (and a fresh TDB1 database can be
     * created there) or has looks like it is an existing TDB1 database. See
     * {@link #checkTDB1(Location)} for a test that the location is a valid, existing
     * database.
     */
    public static boolean isTDB1(String pathname) {
        return isTDB1(Location.create(pathname));
    }

    /**
     * Test to see is a location is either empty (and a fresh TDB1 database can be
     * created there) or has looks like it is an existing TDB1 database. See
     * {@link #checkTDB1(Location)} for a test that the location is a valid, existing
     * database.
     */
    public static boolean isTDB1(Location location) {
        if ( location.isMem() )
            return true;

        if ( ! location.exists() )
            return false;
        if ( isEmpty(location) )
            return true;

        return isTDB1_Storage(location);
    }

    /** Quick check for TDB1. Does not check for validity. */
    private static boolean isTDB1_Storage(Location location) {
        // Very occasionally, deleting the journal makes sense.
        //return exists(location, Names.journalFileBase, Names.extJournal);
        // Test for triples primary index
        StoreParams params = getStoreParams(location);
        return exists(location, params.getPrimaryIndexTriples(), Names.bptExtTree, Names.bptExtRecords);
    }

    /**
     * Check all files exist for a TDB1 database, or the area is empty (and so a new
     * database can be created in the location). Throw an exception if a file
     * is missing.
     */
    public static void checkTDB1(String pathname) {
        checkTDB1(Location.create(pathname));
    }

    /**
     * Check all files exist for a TDB1 database, or the area is empty (and so a new
     * database can be created in the location). Throw an exception if a file
     * is missing.
     */
    public static void checkTDB1(Location location) {
        if ( location.isMem() )
            return;

        if ( isEmpty(location) )
            return;

        if ( ! isTDB1(location) )
            throw new TDBException("Not a TDB1 location: "+location);

        // Places for StoreParams: location or default
        StoreParams params = getStoreParams(location);

        validate(location, Names.journalFileBase, Names.extJournal);

        // Check for indexes
        containsIndex(params.getPrimaryIndexTriples(), params.getTripleIndexes());
        validateBPT(location, params.getTripleIndexes());

        containsIndex(params.getPrimaryIndexQuads(), params.getTripleIndexes());
        validateBPT(location, params.getQuadIndexes());

        // prefixes. GPU
        containsIndex(params.getPrimaryIndexPrefix(), params.getPrefixIndexes());
        // GPOU is not in files "GPU" -- validateBPT(location, params.getPrefixIndexes());
        // Filename of GPU.
        validateBPT(location, params.getIndexPrefix());

        //---- Node tables.
        validateBPT(location, params.getIndexNode2Id());
        validateDAT(location, params.getIndexId2Node());

        validateBPT(location, params.getPrefixNode2Id());
        validateDAT(location, params.getPrefixId2Node());
    }

    // Places for StoreParams: location or default
    private static StoreParams getStoreParams(Location location) {
        StoreParams params = StoreParamsCodec.read(location);
        if ( params == null )
            params = StoreParams.getDftStoreParams();
        return params;
    }

    private static void validateDAT(Location location, String file) {
        validate(location, file, Names.extNodeData);
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
        validate(location, index, Names.bptExtTree, Names.bptExtRecords);
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
        Path path = Path.of(location.getDirectoryPath());
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
            if ( ! location.exists(baseName, ext) )
                return false;
        }
        return true;
    }

    private static RuntimeException missingFile(String filename) {
        return new TDBException("No such file: "+filename);
    }

    private static void good(Location location, String basename, String ext) {}
    private static void good(String filename) {}
}

