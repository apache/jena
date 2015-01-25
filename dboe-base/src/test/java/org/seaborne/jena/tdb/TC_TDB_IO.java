/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.tdb;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.seaborne.jena.tdb.base.block.FileMode ;
import org.seaborne.jena.tdb.base.block.TS_Block ;
import org.seaborne.jena.tdb.base.file.TS_File ;
import org.seaborne.jena.tdb.base.objectfile.TS_ObjectFile ;
import org.seaborne.jena.tdb.base.record.TS_Record ;
import org.seaborne.jena.tdb.base.recordfile.TS_RecordFile ;
import org.seaborne.jena.tdb.sys.SystemLz ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TS_Block.class
    , TS_File.class
    , TS_Record.class
    , TS_RecordFile.class
    , TS_ObjectFile.class
} )

public class TC_TDB_IO {
    static {
        if ( false )
            SystemLz.setFileMode(FileMode.direct) ;
    }
    
    @BeforeClass static public void beforeClass()   
    {
        //org.apache.log4j.LogManager.resetConfiguration() ;
        //org.apache.log4j.PropertyConfigurator.configure("log4j.properties") ;
        Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(Level.WARN) ;
        //Logger.getLogger("com.hp.hpl.jena.tdb.exec").setLevel(Level.WARN) ;
    }

    @AfterClass static public void afterClass() {
    }
}

