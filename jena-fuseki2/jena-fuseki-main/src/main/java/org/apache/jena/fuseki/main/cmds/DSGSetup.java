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

package org.apache.jena.fuseki.main.cmds;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import arq.cmdline.ModDatasetAssembler;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.fuseki.system.spot.TDBOps;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.slf4j.Logger;

/**
 * Various ways to build a dataset from command line arguments.
 *
 * @implNote
 * This code is extracted so as to keep {@link FusekiMain} more manageable.
 */

/*package*/ class DSGSetup {
    // Each setup* should set ensure
    //   serverArgs.datasetDescription
    //   serverArgs.dataset
    // are set on exit.

    /**
     * Given a path name and a preference of TDB1/TDB2 for new databases, return
     * details of the setup to use.
     */
    /*package*/ static void setupTDB(Logger log, String directory, boolean useTDB2, ServerArgs serverArgs) {
        File dir = Path.of(directory).toFile();
        if ( ! dir.exists() )
            throw new CmdException("Directory does not exist: " + directory);
        if ( ! dir.isDirectory() )
            throw new CmdException("Not a directory: " + directory);
        if ( ! dir.canRead() )
            throw new CmdException("Directory not readable: "+directory) ;
        if ( ! dir.canWrite() )
            throw new CmdException("Directory not writeable: "+directory) ;

        if ( IO.isEmptyDirectory(directory) ) {
            if ( useTDB2 )
                setupTDB2(log, directory, serverArgs);
            else
                setupTDB1(log, directory, serverArgs);
            return;
        }

        // Exists, not empty or does not exist
        if ( TDBOps.isTDB1(directory) ) {
            setupTDB1(log, directory, serverArgs);
            return;
        }
        if ( TDBOps.isTDB2(directory) ) {
            setupTDB2(log, directory, serverArgs);
            return;
        }
        throw new CmdException("Directory not a database: " + directory);
    }

    @SuppressWarnings("removal")
    private static void setupTDB1(Logger log, String directory, ServerArgs serverArgs) {
        serverArgs.datasetDescription = "TDB1 dataset: location="+directory;
        serverArgs.dataset = TDB1Factory.createDatasetGraph(directory);
    }

    private static void setupTDB2(Logger log, String directory, ServerArgs serverArgs) {
        serverArgs.datasetDescription = "TDB2 dataset: location="+directory;
        serverArgs.dataset = DatabaseMgr.connectDatasetGraph(directory);
    }

    /*package*/ @SuppressWarnings("removal")
    static void setupMemTDB(Logger log, boolean useTDB2, ServerArgs serverArgs) {
        String tag = useTDB2 ? "TDB2" : "TDB1";
        serverArgs.datasetDescription = tag+" dataset in-memory";
        serverArgs.dataset = useTDB2
            ? DatabaseMgr.createDatasetGraph()
            : TDB1Factory.createDatasetGraph();
        serverArgs.allowUpdate = true;
    }

    /*package*/ static void setupMem(Logger log, ServerArgs serverArgs) {
        serverArgs.datasetDescription = "in-memory";
        serverArgs.dataset = DatasetGraphFactory.createTxnMem();
        serverArgs.allowUpdate = true;
    }

    /*package*/ static void setupFile(Logger log, List<String> filenames, ServerArgs serverArgs) {
        serverArgs.datasetDescription = "in-memory, with files loaded";
        serverArgs.dataset = DatasetGraphFactory.createTxnMem();

        for ( String filename : filenames ) {
            String pathname = filename;
            if ( filename.startsWith("file:") )
                pathname = filename.substring("file:".length());
            if ( !FileOps.exists(pathname) )
                throw new CmdException("File not found: " + filename);

            // INITIAL DATA.
            Lang language = RDFLanguages.filenameToLang(filename);
            if ( language == null )
                throw new CmdException("Cannot guess language for file: " + filename);
            Txn.executeWrite(serverArgs.dataset,  ()-> {
                try {
                    log.info("Dataset: in-memory: load file: " + filename);
                    RDFDataMgr.read(serverArgs.dataset, filename);
                } catch (RiotException ex) {
                    throw new CmdException("Failed to load file: " + filename);
                }
            });
        }
    }

    public static void setupAssembler(Logger log, ModDatasetAssembler modDataset, ServerArgs serverArgs) {
        serverArgs.datasetDescription = "Assembler: "+ modDataset.getAssemblerFile();
        Dataset ds = modDataset.createDataset();
        serverArgs.dataset = ds.asDatasetGraph();
    }

    public static void setupRDFS(Logger serverlog, Graph rdfsSchemaGraph, ServerArgs serverArgs) {
        serverArgs.datasetDescription = (serverArgs.datasetDescription == null)
                ? "RDFS"
                : serverArgs.datasetDescription+ " (with RDFS)";
        serverArgs.dataset = RDFSFactory.datasetRDFS(serverArgs.dataset, rdfsSchemaGraph);
    }
}
