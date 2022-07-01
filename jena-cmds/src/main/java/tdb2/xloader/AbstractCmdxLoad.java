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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.xloader.XLoaderFiles;

/**
 * Base class for TDB xloaders commands for java steps in the load process.
 * All steps accept all the same arguments, even if they are not applicable to the stage.
 */
abstract class AbstractCmdxLoad extends CmdGeneral {
    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
    }
    // All possible arguments for xloader commands.
    // The commands themselves check whether they have the necessary arguments.
    protected static ArgDecl argLocation     = new ArgDecl(true, "location", "loc");
    protected static ArgDecl argTmpdir       = new ArgDecl(true, "tmpdir", "tmp");
    protected static ArgDecl argIndex        = new ArgDecl(true, "index");
    protected static ArgDecl argSortThreads  = new ArgDecl(true, "threads",  "thread", "sortThreads", "sortthreads");

//    // If this is put back, note there are two different sorts - one for the node table and several for the indexes.
//    protected static ArgDecl argSortNodeTableArgs   = new ArgDecl(true, "sortNodeTableArgs");
//    protected static ArgDecl argSortIndexArgs   = new ArgDecl(true, "sortIndexArgs");

    protected String location = null;
    protected String tmpdir = null;
    protected String indexName = null;

    protected int sortThreads = -1;

    // If we add support for arguments to sort(1)
    protected String sortNodeTableArgs = null;
    protected String sortIndexArgs = null;

    protected List<String> filenames = null;

    protected XLoaderFiles loaderFiles = null;

    protected AbstractCmdxLoad(String stageName, String[] argv) {
        super(argv);
        setCmdArgs();

//        super.add(argLocation,     "--loc=", "Database location");
//        super.add(argTmpdir,       "--tmpdir=", "Temporary directory (defaults to --loc)");
//        super.add(argIndex,        "--index=", "Index name");
//        super.add(argSortThreads,  "--threads=", "Number of threads; passed as an argument to sort(1)");
//        super.add(argSortArgs,     "--sortArgs=", "Arguments to sort(1)");
    }

    protected abstract void setCmdArgs();

    @Override
    protected abstract String getSummary();

    protected String getArgsSummary() {
        return "--loc=DIR --tmpdir=DIR";
    }

    @Override
    protected void processModulesAndArgs() {
        if ( ! super.hasArg(argLocation) )
            throw new CmdException("Required: --loc=");

        location = super.getValue(argLocation);
        tmpdir = super.getValue(argTmpdir);
        indexName = super.getValue(argIndex);

//        sortNodeTableArgs = super.getValue(argSortNodeTableArgs);
//        sortIndexArgs = super.getValue(argSortIndexArgs);

        if ( location != null )
            checkDirectory(location);
        if ( tmpdir != null )
            checkDirectory(tmpdir);
        else
            tmpdir = location;
        filenames = new ArrayList<>(super.getPositional());

        if ( super.contains(argSortThreads) ) {
            String str = super.getValue(argSortThreads);
            try {
                sortThreads = Integer.parseInt(str);
            } catch (NumberFormatException ex) {
                throw new CmdException("--threads :: Failed to parse '"+str+"' as an integer");
            }
        }

        subCheckArgs();

        loaderFiles = new XLoaderFiles(tmpdir);
    }

    private void checkDirectory(String dirname) {
        try {
            Path path = Paths.get(dirname);
            if ( Files.exists(path) )
                path = path.toRealPath();
            if ( Files.exists(path) ) {
                if ( !Files.isDirectory(path) || !Files.isWritable(path) ) {
                    throw new CmdException("Path name '" + dirname + "' exists but is not a writable directory");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void subCheckArgs();
}
