/*
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

package org.seaborne.tdb2.store;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.seaborne.dboe.base.block.FileMode ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.ConfigTest ;
import org.seaborne.tdb2.sys.SystemTDB ;
import org.seaborne.tdb2.sys.TestOps ;

/** Slow tests - complete cleaning of disk areas each time */
public class TestStoreConnectionMapped extends AbstractTestStoreConnectionBasics
{
    static FileMode mode ;   

    @BeforeClass
    public static void beforeClassFileMode()
    {
        mode = SystemTDB.fileMode() ;
        TestOps.setFileMode(FileMode.mapped) ;
    }

    @AfterClass
    public static void afterClassFileMode()
    {
        TestOps.setFileMode(mode) ;
    }

    @Override
    protected Location getLocation() {
        return Location.create(ConfigTest.getCleanDir()) ;
    }
}
