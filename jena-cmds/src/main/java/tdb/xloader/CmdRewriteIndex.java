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

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.store.xloader.ProcRewriteIndex;
import org.apache.jena.tdb1.sys.Names;

/** Rewrite one index */
public class CmdRewriteIndex
{
    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    public static void main(String...argv)
    {
        // Usage: srcLocation dstLocation indexName
        if ( argv.length != 3 ) {
            System.err.println("Usage: " + Lib.classShortName(CmdRewriteIndex.class) + " SrcLocation DstLocation IndexName");
            System.exit(1);
        }

        Location srcLoc = Location.create(argv[0]);
        Location dstLoc = Location.create(argv[1]);
        String indexName = argv[2];

        if ( !FileOps.exists(argv[1]) ) {
            System.err.println("Destination directory does not exist");
            System.exit(1);
        }

        if ( FileOps.exists(dstLoc.getPath(indexName, Names.bptExtTree)) ) {
            System.err.println("Destination contains an index of that name");
            System.exit(1);
        }

        ProcRewriteIndex.exec(srcLoc, dstLoc, indexName);
    }
}
