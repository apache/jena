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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test DatabaseOp - the compaction tests are in {@link TestDatabaseCompact}. */
public class TestDatabaseOps
{
    static Quad quad1 = SSE.parseQuad("(_ <s> <p> 1)");
    static Quad quad2 = SSE.parseQuad("(_ _:a <p> 2)");
    static Triple triple1 = quad1.asTriple();
    static Triple triple2 = quad2.asTriple();
    static Triple triple3 = SSE.parseTriple("(<s> <q> 3)");

    private static final String testingDirBackup = "target/tdb-testing/Backup";
    private static final File testingDirBackupFile = new File(testingDirBackup);
    static {
        FileOps.ensureDir("target");
        FileOps.ensureDir("target/tdb-testing");
        FileOps.ensureDir(testingDirBackup);
    }

    @After
    public void after() {
        TDBInternal.reset();
        FileUtils.deleteQuietly(testingDirBackupFile);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder(testingDirBackupFile);

    @Test public void backup_1() {
        Location dir = Location.create(folder.getRoot().getPath());
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(dir);
        Txn.executeWrite(dsg, ()-> {
            dsg.add(quad2);
            dsg.add(quad1);
        });
        String file1 = DatabaseMgr.backup(dsg);
        DatasetGraph dsg2 = RDFDataMgr.loadDatasetGraph(file1);
        Txn.executeRead(dsg, ()-> {
            assertTrue(dsg.contains(quad1));
            assertEquals(2, dsg.getDefaultGraph().size());
            assertTrue(dsg2.getDefaultGraph().isIsomorphicWith(dsg.getDefaultGraph()));
        });
        String file2 = DatabaseMgr.backup(dsg);
        assertNotEquals(file1, file2);
    }
}
