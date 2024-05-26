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

package org.apache.jena.tdb2.setup;

import static org.junit.Assert.assertEquals;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.base.Sys;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.*;

public class TestReorderSetup {

    private String DIR = ConfigTest.getCleanDir() ;
    // The TC_TDB2 collection runs with reorder=none
    private static ReorderTransformation envReorder = SystemTDB.getDefaultReorderTransform();

    @BeforeClass public static void beforeClass() {
        envReorder = SystemTDB.getDefaultReorderTransform();
        SystemTDB.setDefaultReorderTransform(ReorderLib.fixed());
    }

    @AfterClass public static void afterClass() {
        SystemTDB.setDefaultReorderTransform(envReorder);
    }

    @Before public void before() {
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);
        FileOps.ensureDir(DIR+"/Data-0001");
    }

    @After public void after() {
        FileOps.clearAll(DIR);
    }

    @Test public void reorder_setup_1() {
        Assume.assumeFalse(Sys.isWindows);
        test(()->{}, ReorderLib.fixed().getClass());
    }

    @Test public void reorder_setup_2() {
        Assume.assumeFalse(Sys.isWindows);
        test(()->touchFile(DIR+"/none.opt"), ReorderLib.identity().getClass());
    }

    @Test public void reorder_setup_3() {
        Assume.assumeFalse(Sys.isWindows);
        test(()-> touchFile(DIR+"/Data-0001/none.opt"), ReorderLib.identity().getClass());
    }

    @Test public void reorder_setup_4() {
        Assume.assumeFalse(Sys.isWindows);
        test(()-> {
            touchFile(DIR+"/Data-0001/none.opt");
            touchFile(DIR+"/fixed.opt");
        }, ReorderLib.identity().getClass());
    }

    @Test public void reorder_setup_5() {
        Assume.assumeFalse(Sys.isWindows);
        test(()-> {
            touchFile(DIR+"/Data-0001/fixed.opt");
            touchFile(DIR+"/none.opt");
        }, ReorderLib.fixed().getClass());
    }

    private static void touchFile(String filename) {
        try {
            try (OutputStream out = new FileOutputStream(filename)) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void test(Runnable before, Class<?> expected) {

        DatasetGraph dsg = null;
        before.run();

        test1(dsg, expected);
        // And again on existing database.
        test1(dsg, expected);
    }

    private void test1(DatasetGraph dsg, Class<? > expected) {
        try {
            dsg = DatabaseMgr.connectDatasetGraph(DIR);
            DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
            ReorderTransformation reorder = dsgtdb.getReorderTransform();
            assertEquals(expected, reorder.getClass());
        } finally {
            if ( dsg != null )
                TDBInternal.expel(dsg);
        }
    }
}
