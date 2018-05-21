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

package tdb.bulkloader2;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb.store.bulkloader2.ProcIndexCopy ;

public class CmdIndexCopy 
{
    static {
        LogCtl.setLog4j();
        JenaSystem.init();
    }

    public static void main(String... argv) {
        if ( argv.length != 4 ) {
            System.err.println("Usage: Location1 Index1 Location2 Index2");
            System.exit(1);
        }

        String locationStr1 = argv[0] ;
        String indexName1 = argv[1] ;
        String locationStr2 = argv[2] ;
        String indexName2 = argv[3] ;
        ProcIndexCopy.exec(locationStr1, indexName1, locationStr2, indexName2);
    }
}

