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

import jena.cmd.CmdException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.fuseki.system.spot.TDBOps;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;

/*package*/ class DSGSetup {

    /**
     * Given a path name and a preference of TDB1/TDB2 for new databases, return
     * details of the setup to use.
     */
    /*package*/ static void setupTDB(String directory, boolean useTDB2, ServerConfig serverConfig) {
        if ( ! IO.exists(directory) )
            throw new CmdException("Does not exist: " + directory);
        if ( ! IO.isDirectory(directory) )
            throw new CmdException("Not a directory: " + directory);

        if ( IO.isEmptyDirectory(directory) ) {
            if ( useTDB2 )
                setupTDB2(directory, serverConfig);
            else
                setupTDB1(directory, serverConfig);
            return;
        }

        // Exists, not empty or does not exist
        if ( TDBOps.isTDB1(directory) ) {
            setupTDB1(directory, serverConfig);
            return;
        } else if ( TDBOps.isTDB2(directory) ) {
            setupTDB2(directory, serverConfig);
            return;
        } else
            throw new CmdException("Directory not a database: " + directory);
    }

    private static void setupTDB1(String directory, ServerConfig serverConfig) {
        serverConfig.datasetDescription = "TDB1 dataset: location="+directory;
        serverConfig.dsg = TDBFactory.createDatasetGraph(directory);
    }

    private static void setupTDB2(String directory, ServerConfig serverConfig) {
        serverConfig.datasetDescription = "TDB2 dataset: location="+directory;
        serverConfig.dsg = DatabaseMgr.connectDatasetGraph(directory);
    }
}
