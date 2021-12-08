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
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.tdb2.xloader.BulkLoaderX;
import org.apache.jena.tdb2.xloader.ProcNodeTableBuilderX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdxBuildNodeTable extends AbstractCmdxLoad {

    public static void main(String... args) {
        new CmdxBuildNodeTable(args).mainRun();
    }

    protected CmdxBuildNodeTable(String[] argv) {
        super("Nodes", argv);
    }

    @Override
    protected void setCmdArgs() {
        super.add(argLocation,  "--loc=", "Database location");
        super.add(argTmpdir,    "--tmpdir=", "Temporary directory (defaults to --loc)");
        //super.add(argSortNodeTableArgs, "--sortNodeTableArgs=", "Specialised argument for the sort for the node table");
    }

    @Override
    protected String getSummary() {
        return getCommandName()+" "+getArgsSummary();
    }

    @Override
    protected void subCheckArgs() {
        if ( location == null )
            throw new CmdException("Required : --loc");
        if ( filenames.isEmpty() )
            throw new CmdException("No files to load");
    }

    @Override
    protected String getCommandName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    protected void exec() {
        FileOps.ensureDir(location);
        // Deletes any existing database!
        FileOps.clearAll(location);

        Timer timer = new Timer();
        timer.startTimer();
        FmtLog.info(LOG, "Build node table");
        FmtLog.info(LOG, "  Database   = %s", location);
        FmtLog.info(LOG, "  TMPDIR     = %s", tmpdir==null?"unset":tmpdir);
        FmtLog.info(LOG, "  Data files = %s", StrUtils.strjoin(filenames, " "));

        if ( tmpdir == null )
            tmpdir = location;

        Logger LOG1 = LOG;
        Logger LOG2 = LoggerFactory.getLogger("Terms");

        Pair<Long/*triples or quads*/, Long/*indexed nodes*/> buildCounts = ProcNodeTableBuilderX.exec(LOG1, LOG2, location, loaderFiles, filenames, sortNodeTableArgs);

        long timeMillis = timer.endTimer();

        long items = buildCounts.getLeft();
        double xSec = timeMillis/1000.0;
        double rate = items/xSec;
        String elapsedStr = BulkLoaderX.milliToHMS(timeMillis);
        String rateStr = BulkLoaderX.rateStr(items, timeMillis);

        FmtLog.info(LOG, "NodeTable - %s seconds - %s at %s terms per second", Timer.timeStr(timeMillis), elapsedStr, rateStr);
    }
}
