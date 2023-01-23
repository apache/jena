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

package tdb2.xloader;

import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb.store.bulkloader.BulkLoader;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.apache.jena.tdb2.xloader.BulkLoaderX;

/**
 * A version of xloader/TDB2 that runs in a single JVM.
 * <p>
 * This still requires an external sort programme.
 * Normally, xloader is run by script which uses one JVM per operation.
 * Exiting the JVM and starting a new one clears the process state which is beneficial.
 * <p>
 * This program does not need much RAM. Do not set the heap size large.
 * 4Gbytes is enough, usually 2Gbytes is sufficient.
 * More heap is not faster (in fact, it is often slower).
 */
public class CmdxLoader extends AbstractCmdxLoad {

    public static void main(String... args) {
        new CmdxLoader("AIO", args).mainRun();
    }

    protected CmdxLoader(String stageName, String[] argv) {
        super(stageName, argv);
    }

    @Override
    protected void setCmdArgs() {
        super.add(argLocation,      "--loc=", "Database location");
        super.add(argTmpdir,        "--tmpdir=", "Temporary directory (defaults to --loc)");
        super.add(argSortThreads,   "--threads=", "Number of threads; passed as an argument to sort(1)");
    }

    @Override
    protected String getSummary() {
        return getCommandName()+" "+getArgsSummary();
    }

    @Override
    protected void subCheckArgs() {}

    @Override
    protected String getCommandName() {
        return "cmd-xloader";
    }

    private String TMPDIR;
    private String DIR;
    private String datafile;

    @Override
    protected void exec() {
        TMPDIR = super.tmpdir;
        DIR = super.location;
        datafile = super.filenames.get(0);

        FileOps.ensureDir(TMPDIR);
        FileOps.clearAll(TMPDIR);

        if ( !TMPDIR.equals(DIR) ) {
            FileOps.ensureDir(DIR);
            FileOps.clearAll(DIR);
        }

        BulkLoaderX.DataTick = 100_000;
        BulkLoader.DataTickPoint = BulkLoaderX.DataTick;

        long maxMemory = Runtime.getRuntime().maxMemory();
        // Java code to do all the steps.
        System.out.printf("RAM = %,d\n", maxMemory);

        System.out.println("STEP 1 - load node table");
        step(() -> CmdxBuildNodeTable.main("--loc=" + DIR, "--threads=" + super.sortThreads, datafile));

        System.out.println("STEP 2 - ingest triples and quads");
        step(() -> CmdxIngestData.main("--loc=" + DIR, datafile));

        System.out.println("STEP 3 - build indexes");

        if ( !isEmptyFile(loaderFiles.triplesFile) ) {
            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=SPO"));
            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=POS"));
            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=OSP"));
        }

        if ( !isEmptyFile(loaderFiles.quadsFile) ) {
            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=GSPO"));
            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=GPOS"));
            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=GOSP"));

            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=SPOG"));
            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=POSG"));
            step(() -> CmdxBuildIndex.main("--loc=" + DIR, "--threads=" + super.sortThreads, "--index=OSPG"));
        }
        expel();
    }

    // Compression - empty file != zero size
    private boolean isEmptyFile(String filename) {
        try (InputStream in = IO.openFile(filename)) {
            int b = in.read();
            return (b == -1);
        } catch (IOException ex) {
            throw IOX.exception(ex);
        }
    }

    private void step(Runnable action) {
        // expel();
        action.run();
    }

    private void expel() {
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(DIR);
        TDBInternal.expel(dsg);
    }
}
