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

package org.apache.jena.tdb2.sys;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.ConfigTest;
import org.junit.Test;

public class TestDatabaseConnection {

    @Test
    public void testStoreConnectionTxn1() {
        TDBInternal.reset();
        // Only do disk things for test that need them (disk takes time!).
        String DIRx = ConfigTest.getCleanDir();
        Location DIR = Location.create(DIRx);
        FileOps.clearDirectory(DIRx);
        try {
            DatasetGraph dg1 = DatabaseConnection.connectCreate(DIR).getDatasetGraph();
            DatasetGraph dg2 = DatabaseConnection.connectCreate(DIR).getDatasetGraph();
            assertSame(dg1, dg2);
        }
        finally {
            FileOps.clearDirectory(DIRx);
        }
    }

    @Test
    public void testStoreConnectionTxn2() {
        // Named memory locations
        TDBInternal.reset();
        DatasetGraph dg1 = DatabaseConnection.connectCreate(Location.mem("FOO")).getDatasetGraph();
        DatasetGraph dg2 = DatabaseConnection.connectCreate(Location.mem("FOO")).getDatasetGraph();
        assertSame(dg1, dg2);
    }

    @Test
    public void testStoreConnectionTxn3() {
        // Un-named memory locations
        TDBInternal.reset();
        DatasetGraph dg1 = DatabaseConnection.connectCreate(Location.mem()).getDatasetGraph();
        DatasetGraph dg2 = DatabaseConnection.connectCreate(Location.mem()).getDatasetGraph();
        assertNotSame(dg1, dg2);
    }


}
