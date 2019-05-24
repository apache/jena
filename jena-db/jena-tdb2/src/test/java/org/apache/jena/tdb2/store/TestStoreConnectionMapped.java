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

package org.apache.jena.tdb2.store;

import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TestOps;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/** Slow tests - complete cleaning of disk areas each time */
public class TestStoreConnectionMapped extends AbstractTestStoreConnectionBasics
{
    static FileMode mode;

    @BeforeClass
    public static void beforeClassFileMode()
    {
        mode = SystemTDB.fileMode();
        TestOps.setFileMode(FileMode.mapped);
    }

    @AfterClass
    public static void afterClassFileMode()
    {
        TestOps.setFileMode(mode);
    }

    @Override
    protected Location getLocation() {
        return Location.create(ConfigTest.getCleanDir());
    }
}
