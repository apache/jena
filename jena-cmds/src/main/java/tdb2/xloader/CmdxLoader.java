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

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.tdb.store.bulkloader.BulkLoader;
import org.apache.jena.tdb2.xloader.BulkLoaderX;


/**
 * A version of xloader/TDB2 that runs in a single JVM.
 * <p>
 * This still requires an external sort programme.
 * Normally, xload is run by script which uses one JVM per operation.
 * Exiting the JVM and startig a new one
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
           super.add(argLocation,  "--loc=", "Database location");
           super.add(argTmpdir,    "--tmpdir=", "Temporary directory (defaults to --loc)");
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

       @Override
       protected void exec() {
           // Java code to do all the steps.
           String TMPDIR = super.tmpdir;
           String DIR = super.location;
           String datafile = super.filenames.get(0);

           FileOps.ensureDir(TMPDIR);
           FileOps.clearAll(TMPDIR);

           if ( !TMPDIR.equals(DIR) ) {
               FileOps.ensureDir(DIR);
               FileOps.clearAll(DIR);
           }

           BulkLoaderX.DataTick = 100_000;
           BulkLoader.DataTickPoint = BulkLoaderX.DataTick;

           long maxMemory = Runtime.getRuntime().maxMemory();
           System.out.printf("RAM = %,d\n", maxMemory);

           System.out.println("STEP 1 - load node table");
           CmdxBuildNodeTable.main("--loc=" + DIR, datafile);

           System.out.println("STEP 2 - ingest triples and quads");
           CmdxIngestData.main("--loc=" + DIR, datafile);

           System.out.println("STEP 3 - build indexes");
           CmdxBuildIndex.main("--loc=" + DIR, "--index=SPO");
           CmdxBuildIndex.main("--loc=" + DIR, "--index=POS");
           CmdxBuildIndex.main("--loc=" + DIR, "--index=OSP");

           CmdxBuildIndex.main("--loc=" + DIR, "--index=GSPO");
           CmdxBuildIndex.main("--loc=" + DIR, "--index=GPOS");
           CmdxBuildIndex.main("--loc=" + DIR, "--index=GOSP");

           CmdxBuildIndex.main("--loc=" + DIR, "--index=SPOG");
           CmdxBuildIndex.main("--loc=" + DIR, "--index=POSG");
           CmdxBuildIndex.main("--loc=" + DIR, "--index=OSPG");
       }
   }