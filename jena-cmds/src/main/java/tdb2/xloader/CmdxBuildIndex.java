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
import org.apache.jena.cmd.CmdException;
import org.apache.jena.tdb2.xloader.ProcBuildIndexX;

public class CmdxBuildIndex extends AbstractCmdxLoad {

    public static void main(String... args) {
        new CmdxBuildIndex(args).mainRun();
    }

    protected CmdxBuildIndex(String[] argv) {
        super("Index", argv);
    }

    @Override
    protected void setCmdArgs() {
        super.add(argLocation,      "--loc=", "Database location");
        super.add(argTmpdir,        "--tmpdir=", "Temporary directory (defaults to --loc)");
        super.add(argIndex,         "--index=", "Index name");
        super.add(argSortThreads,   "--threads=", "Number of threads; passed as an argument to sort(1)");
        //super.add(argSortIndexArgs, "--sortIndexArgs=", "Specialised argument for the sort for the indexes");
    }

    @Override
    protected String getSummary() {
        return getCommandName()+" "+getArgsSummary();
    }

    @Override
    protected String getArgsSummary() {
        return super.getArgsSummary()+" --index=NAME";
    }

    @Override
    protected void subCheckArgs() {
        if ( location == null )
            throw new CmdException("Required : --loc");
        if ( indexName == null )
            throw new CmdException("Required : --index");
    }

    @Override
    protected String getCommandName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    protected void exec() {

        FileOps.ensureDir(location);

//        FmtLog.info(LOG, "  Database = %s", location);
//        FmtLog.info(LOG, "  TMPDIR   = %s", tmpdir==null?"unset":tmpdir);
//        FmtLog.info(LOG, "  DATAFILE = %s", filenames);
//        FmtLog.info(LOG, "Triples = %s", triplesFile);
//        FmtLog.info(LOG, "Quads   = %s", quadsFile);

        if ( tmpdir == null )
            tmpdir = location;
        ProcBuildIndexX.exec(location, indexName, sortThreads, sortIndexArgs, loaderFiles);
    }
}
