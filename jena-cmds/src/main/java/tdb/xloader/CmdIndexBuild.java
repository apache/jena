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

package tdb.xloader;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb1.store.xloader.ProcIndexBuild;

public class CmdIndexBuild {
    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    public static void main(String... argv) {
        // DATA IN S/P/O columns but sorted by index order.

        if ( argv.length != 3 ) {
            System.err.println("Usage: Location Index dataFile");
            System.exit(1);
        }

        String locationStr = argv[0];
        String indexName = argv[1];

//        if ( ! Arrays.asList(Names.tripleIndexes).contains(indexName) &&
//            ! Arrays.asList(Names.quadIndexes).contains(indexName) )
//        {
//            System.err.println("Index name not recognized: "+indexName) ;
//            System.exit(1) ;
//        }

        String dataFile = argv[2];
        ProcIndexBuild.exec(locationStr, indexName, dataFile);
    }
}

