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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.base.Sys;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.IO_DB;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDatabaseOps
{
    private Location dir = null;

    static Quad quad1 = SSE.parseQuad("(_ <s> <p> 1)");
    static Quad quad2 = SSE.parseQuad("(_ _:a <p> 2)");
    static Triple triple1 = quad1.asTriple();
    static Triple triple2 = quad2.asTriple();
    static Triple triple3 = SSE.parseTriple("(<s> <q> 3)");

    @Before
    public void before() {
        String DIR = ConfigTest.getCleanDir();
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);
        dir = Location.create(DIR);
    }

    @After
    public void after() {
        TDBInternal.reset();
        FileUtils.deleteQuietly(IO_DB.asFile(dir));
    }

    @Test public void compact_dsg_1() {
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(dir);
        DatasetGraphSwitchable dsgs = (DatasetGraphSwitchable)dsg;
        DatasetGraph dsg1 = dsgs.get();
        Location loc1 = ((DatasetGraphTDB)dsg1).getLocation();

        Txn.executeWrite(dsg, ()-> {
            dsg.add(quad2);
            dsg.add(quad1);
        });
        DatabaseMgr.compact(dsg, false);

        assertFalse(StoreConnection.isSetup(loc1));

        DatasetGraph dsg2 = dsgs.get();
        Location loc2 = ((DatasetGraphTDB)dsg2).getLocation();

        assertNotEquals(dsg1, dsg2);
        assertNotEquals(loc1, loc2);

        Txn.executeRead(dsg, ()-> {
            assertTrue(dsg.contains(quad2));
            assertTrue(dsg.contains(quad1));
        });

        // dsg1 was closed and expelled. We must carefully reopen its storage only.
        DatasetGraph dsgOld = StoreConnection.connectCreate(loc1).getDatasetGraph();

        Txn.executeWrite(dsgOld, ()->dsgOld.delete(quad2));
        Txn.executeRead(dsg,     ()->assertTrue(dsg.contains(quad2)) );
        Txn.executeRead(dsg2,    ()->assertTrue(dsg2.contains(quad2)) );
    }

    @Test public void compact_graph_2() {
        // graphs across compaction.
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(dir);
        Graph g = dsg.getDefaultGraph();

        DatasetGraphSwitchable dsgs = (DatasetGraphSwitchable)dsg;
        DatasetGraph dsg1 = dsgs.get();
        Location loc1 = ((DatasetGraphTDB)dsg1).getLocation();

        Txn.executeWrite(dsg, ()-> {
            dsg.add(quad2);
            dsg.add(quad1);
        });
        DatabaseMgr.compact(dsg, false);
        Txn.executeRead(dsg, ()-> {
            assertEquals(2, g.size());
            assertTrue(g.contains(triple2));
        });

        // Check is not attached to the old graph.
        DatasetGraph dsgOld = StoreConnection.connectCreate(loc1).getDatasetGraph();

        Txn.executeWrite(dsgOld, ()->dsgOld.getDefaultGraph().delete(triple2));
        Txn.executeRead(dsg,     ()->assertTrue(g.contains(triple2)) );

        Txn.executeWrite(dsg,    ()->g.add(triple3));
        Txn.executeRead(dsgOld,  ()->assertFalse(dsgOld.getDefaultGraph().contains(triple3)) );
    }

    @Test public void compact_prefixes_3() {
        // 2020-04:
        // Case 1:
        // This test fails with an NPE with Java11+ on ASF Jenkins.
        // java.lang.NullPointerException
        //         at java.base/java.nio.file.Files.provider(Files.java:105)
        //         at java.base/java.nio.file.Files.isDirectory(Files.java:2308)
        //         at org.apache.jena.tdb2.sys.IOX.asLocation(IOX.java:151)
        //         at org.apache.jena.tdb2.sys.DatabaseOps.compact(DatabaseOps.java:176)
        //         at org.apache.jena.tdb2.DatabaseMgr.compact(DatabaseMgr.java:62)
        //         at org.apache.jena.tdb2.sys.TestDatabaseOps.compact_prefixes_3_test(TestDatabaseOps.java:157)
        //         at org.apache.jena.tdb2.sys.TestDatabaseOps.compact_prefixes_3(TestDatabaseOps.java:135)
        // The NPE is from java.nio.file.Files.provider.
        // It seems to be impossible for the data from TDB to be null intermittently.
        // It does not fail anywhere else except at ASF and it does not always fail.
        // This seems to be on specific, and maybe just on, an ASF-Jenkins build node.

        // 2021-08:
        // Case 2:
        // Another "can't happen" situation on ASF Jenkins only.
        // This is consisten with case 1 - it just shows up differently now.
        // The database got created - so it's directory must exist unless something external interferred.
        // No location: (/home/jenkins/workspace/Jena/Jena_Development_Deploy/jena-db/jena-tdb2/target/tdb-testing/DB,Data)
        //
        // Stacktrace
        //
        // org.apache.jena.tdb2.TDBException: No location: (/home/jenkins/workspace/Jena/Jena_Development_Deploy/jena-db/jena-tdb2/target/tdb-testing/DB,Data)
        //     at org.apache.jena.tdb2.sys.TestDatabaseOps.compact_prefixes_3_test(TestDatabaseOps.java:175)
        //     at org.apache.jena.tdb2.sys.TestDatabaseOps.compact_prefixes_3(TestDatabaseOps.java:145)

        try {
            compact_prefixes_3_test();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            //            StackTraceElement[] x = ex.getStackTrace();
            //            if ( x.length >= 0 && "java.nio.file.Files".equals(x[0].getClassName()) )
            //               return ;
            throw ex;
        }
        catch (TDBException ex) {
            if ( ex.getMessage().startsWith("No location:")) {
                System.err.println("'No location:' for known location. Msg-> " + ex.getMessage());
                return ;
            }
            throw ex;
        }
    }

    private void compact_prefixes_3_test() {
        // prefixes across compaction.
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(dir);
        Graph g = dsg.getDefaultGraph();
        Txn.executeWrite(dsg, ()-> g.getPrefixMapping().setNsPrefix("ex", "http://example/") );

        Txn.executeRead(dsg, ()-> {
            assertEquals("ex", g.getPrefixMapping().getNsURIPrefix("http://example/"));
            assertEquals("http://example/", g.getPrefixMapping().getNsPrefixURI("ex"));
        });

        DatasetGraphSwitchable dsgs = (DatasetGraphSwitchable)dsg;
        assertNotNull("DatasetGraphSwitchable created", dsgs.getLocation());
        DatasetGraph dsg1 = dsgs.get();
        Location loc1 = ((DatasetGraphTDB)dsg1).getLocation();

        // Before
        int x1 = Txn.calculateRead(dsg, ()->dsg.prefixes().size());
        assertTrue("Prefxies count", x1 > 0);

        DatabaseMgr.compact(dsgs, false); // HERE

        // After
        int x2 = Txn.calculateRead(dsg, ()->dsg.prefixes().size());

        assertEquals("Before and after prefix count", x1, x2);

        Graph g2 = dsgs.getDefaultGraph();

        Txn.executeRead(dsgs, ()-> {
            assertEquals("ex", g2.getPrefixMapping().getNsURIPrefix("http://example/"));
            assertEquals("http://example/", g2.getPrefixMapping().getNsPrefixURI("ex"));
        });

        // Check is not attached to the old graph.
        DatasetGraph dsgOld = StoreConnection.connectCreate(loc1).getDatasetGraph();

        Txn.executeWrite(dsgOld, ()->dsgOld.getDefaultGraph().getPrefixMapping().removeNsPrefix("ex"));
        Txn.executeRead(dsg,     ()->assertEquals("http://example/", g.getPrefixMapping().getNsPrefixURI("ex")));

        Txn.executeWrite(dsg,    ()->g.getPrefixMapping().setNsPrefix("ex2", "http://exampl2/") );
        Txn.executeRead(dsgOld,  ()->assertNull(dsgOld.getDefaultGraph().getPrefixMapping().getNsPrefixURI("ex")));
    }

    @Test public void compact_delete() {
        assumeFalse(Sys.isWindows);
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(dir);
        DatasetGraphSwitchable dsgs = (DatasetGraphSwitchable)dsg;
        DatasetGraph dsg1 = dsgs.get();
        Location loc1 = ((DatasetGraphTDB)dsg1).getLocation();

        Txn.executeWrite(dsg, ()-> {
            dsg.add(quad2);
            dsg.add(quad1);
        });
        DatabaseMgr.compact(dsg, true);

        // Memory mapped files do not go away on MS Windows until the JVM exits.
        // This is a long standing JDK issue.
        // https://bugs.openjdk.java.net/browse/JDK-4715154
        if ( ! Sys.isWindows )
            assertFalse(IO_DB.asFile(loc1).exists());

        DatasetGraph dsg2 = dsgs.get();
        Location loc2 = ((DatasetGraphTDB)dsg2).getLocation();

        assertNotEquals(dsg1, dsg2);
        assertNotEquals(loc1, loc2);

        Txn.executeRead(dsg, ()-> {
            assertTrue(dsg.contains(quad2));
            assertTrue(dsg.contains(quad1));
        });

        Txn.executeRead(dsg,  ()->assertTrue(dsg.contains(quad2)) );
        Txn.executeRead(dsg2, ()->assertTrue(dsg2.contains(quad2)) );
    }

    @Test public void backup_1() {
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
