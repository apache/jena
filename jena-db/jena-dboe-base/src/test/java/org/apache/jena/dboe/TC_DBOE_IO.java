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

package org.apache.jena.dboe;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.dboe.base.block.TS_Block;
import org.apache.jena.dboe.base.buffer.TS_Buffer;
import org.apache.jena.dboe.base.file.TS_File;
import org.apache.jena.dboe.base.record.TS_Record;
import org.apache.jena.dboe.base.recordfile.TS_RecordFile;
import org.apache.jena.dboe.sys.SystemIndex;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TS_Block.class
    , TS_File.class
    , TS_Buffer.class
    , TS_Record.class
    , TS_RecordFile.class
} )

public class TC_DBOE_IO {
    private static String level;

    static {
        if ( false )
            SystemIndex.setFileMode(FileMode.direct);
    }

    @BeforeClass static public void beforeClass() {
        level = LogCtl.getLevel("org.apache.jena.tdb.info");
        LogCtl.setLevel("org.apache.jena.tdb.info", "WARN");
    }

    @AfterClass static public void afterClass() {
        LogCtl.setLevel("org.apache.jena.tdb.info", level);
    }
}

