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

package org.apache.jena.fuseki.main.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sys.JenaSystem;
import org.junit.BeforeClass;
import org.junit.Test;

/** Same packege for access */
public class TestFusekiModules {

    private static FusekiModules system = null;

    @BeforeClass public static void beforeClass() { JenaSystem.init(); }

    @Test public void modules_0() {
        ModuleForTest module = new ModuleForTest();
        assertEquals(0, module.countStart.get());
        assertEquals(0, module.countConfiguration.get());
        module.start();
        assertEquals(1, module.countStart.get());
    }

    private static void reset() {
        FusekiSystemModules.reset();
    }


    @Test public void lifecycle_1() {
        reset();

        ModuleForTest module = new ModuleForTest();
        module.start();
        FusekiModules fmods = FusekiModules.create(module);

        // Mock default set.
        FusekiModulesCtl.setSystemDefault(fmods);

        FusekiServer.Builder builder = FusekiServer.create().port(0);
        try {
            lifecycle(builder, module);
        } finally {
            FusekiModulesCtl.setSystemDefault(null);
        }
    }

    @Test public void lifecycle_2() {
        reset();

        ModuleForTest module = new ModuleForTest();
        module.start();
        FusekiModules fmods = FusekiModules.create(module);
        FusekiModulesCtl.setSystemDefault(null);
        // Explicit FusekiModules
        FusekiServer.Builder builder = FusekiServer.create().setFusekiModules(fmods).port(0);
        lifecycle(builder, module);
    }

    @Test public void lifecycle_3() {
        reset();
        FusekiSystemModules.get().load();

        ModuleForTest module = ModuleByServiceLoader.resetForTest();
        module.start();
        FusekiModules fmods = FusekiModules.create(module);
        FusekiModulesCtl.setSystemDefault(fmods);
        // Default FusekiModules
        FusekiServer.Builder builder = FusekiServer.create().port(0);
        lifecycle(builder, module);
    }


    private void lifecycle(FusekiServer.Builder builder, ModuleForTest module) {
        assertEquals("start:"  ,       1, module.countStart.get());
        assertEquals("prepare:",       0, module.countPrepared.get());
        assertEquals("configured:",    0, module.countConfiguration.get());
        assertEquals("server: ",       0, module.countServer.get());
        assertEquals("serverBefore: ", 0, module.countServerBeforeStarting.get());
        assertEquals("serverAfter: ",  0, module.countServerAfterStarting.get());

        FusekiServer server = builder.build();
        assertFalse(server.getModules().asList().isEmpty());

        assertEquals("start:"  ,       1, module.countStart.get());
        assertEquals("prepare:",       1, module.countPrepared.getPlain());
        assertEquals("configured:",    1, module.countConfiguration.get());
        assertEquals("server: ",       1, module.countServer.get());
        assertEquals("serverBefore: ", 0, module.countServerBeforeStarting.get());
        assertEquals("serverAfter: ",  0, module.countServerAfterStarting.get());

        server.start();

        assertEquals("start:"  ,       1, module.countStart.get());
        assertEquals("prepare:",       1, module.countPrepared.get());
        assertEquals("configured:",    1, module.countConfiguration.get());
        assertEquals("server: ",       1, module.countServer.get());
        assertEquals("serverBefore: ", 1, module.countServerBeforeStarting.get());
        assertEquals("serverAfter: ",  1, module.countServerAfterStarting.get());

        server.stop();
    }

    @Test public void server_module_1() {
        AtomicBoolean called1 = new AtomicBoolean(false);
        AtomicBoolean called2 = new AtomicBoolean(false);
        FusekiModule oneOff = new FusekiModule() {
            @Override public String name() { return "Local"; }
            @Override public void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) {
                called1.set(true);
            }
            @Override public void serverBeforeStarting(FusekiServer server) {
                called2.set(true);
            }
        };

        FusekiModules mods = FusekiModules.create(oneOff);
        assertFalse(called1.get());
        assertFalse(called2.get());
        FusekiServer server = FusekiServer.create().port(0).setFusekiModules(mods).build();
        assertTrue(called1.get());
        assertFalse(called2.get());
        try {
            server.start();
            assertTrue(called2.get());
        } finally { server.stop(); }
    }
}
