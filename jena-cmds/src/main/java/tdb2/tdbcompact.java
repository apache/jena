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

package tdb2;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.sys.DatabaseOps;
import tdb2.cmdline.CmdTDB;

public class tdbcompact extends CmdTDB {
    private static final ArgDecl argDeleteOld = new ArgDecl(ArgDecl.NoValue, "deleteOld");

    private boolean shouldDeleteOld = false;


    static public void main(String... argv) {
        CmdTDB.init() ;
        new tdbcompact(argv).mainRun() ;
    }

    protected tdbcompact(String[] argv) {
        super(argv);

        super.add(argDeleteOld, "--deleteOld", "Delete old database after compaction");
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();

        shouldDeleteOld = contains(argDeleteOld);
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " : Compact a TDB2 dataset" ;
    }

    @Override
    protected void exec() {
        DatasetGraphSwitchable dsg = getDatabaseContainer() ;
        long start = System.currentTimeMillis();
        DatabaseOps.compact(dsg, shouldDeleteOld) ;
        long finish = System.currentTimeMillis();
        System.out.printf("Compacted in %.3fs\n", (finish-start)/1000.0);
    }
}
