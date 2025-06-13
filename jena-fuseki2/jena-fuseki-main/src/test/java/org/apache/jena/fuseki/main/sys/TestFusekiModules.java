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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sys.JenaSystem;

/** Same package for access */
public class TestFusekiModules {

    @BeforeAll
    public static void beforeClass() { JenaSystem.init(); }

    @Test public void modules_0() {
        ModuleForTest module = new ModuleForTest();
        assertEquals(0, module.countPrepared.get());
        assertEquals(0, module.countConfiguration.get());
        // Created, not loaded
    }

    @Test public void lifecycle_1() {
        ModuleForTest module = new ModuleForTest();
        FusekiModules fmods = FusekiModules.create(module);
        // Mock default set.
        FusekiModules.setSystemDefault(fmods);
        try {
            FusekiServer.Builder builder = FusekiServer.create().port(0);
            lifecycle(builder, module);
        } finally {
            FusekiModules.setSystemDefault(null);
        }
    }

    @Test public void lifecycle_2() {
        ModuleByServiceLoader.reset();
        FusekiModules.resetSystemDefault();

        ModuleForTest module = new ModuleForTest();
        FusekiModules fmods = FusekiModules.create(module);
        // Explicit FusekiModules
        FusekiServer.Builder builder = FusekiServer.create().fusekiModules(fmods).port(0);
        lifecycle(builder, module);
    }

    private void lifecycle(FusekiServer.Builder builder, ModuleForTest module) {
        assertEquals(0, module.countPrepared.get(), "prepare:");
        assertEquals(0, module.countConfiguration.get(), "configured:");
        assertEquals(0, module.countServer.get(), "server:");
        assertEquals(0, module.countServerBeforeStarting.get(), "serverBefore:");
        assertEquals(0, module.countServerAfterStarting.get(), "serverAfter:");

        FusekiServer server = builder.build();
        assertFalse(server.getModules().asList().isEmpty());

        assertEquals(1, module.countPrepared.get(), "prepare:");
        assertEquals(1, module.countConfiguration.get(), "configured:");
        assertEquals(1, module.countServer.get(), "server:");
        assertEquals(0, module.countServerBeforeStarting.get(), "serverBefore:");
        assertEquals(0, module.countServerAfterStarting.get(), "serverAfter:");

        server.start();

        assertEquals(1, module.countPrepared.get(), "prepare:");
        assertEquals(1, module.countConfiguration.get(), "configured:");
        assertEquals(1, module.countServer.get(), "server:");
        assertEquals(1, module.countServerBeforeStarting.get(), "serverBefore:");
        assertEquals(1, module.countServerAfterStarting.get(), "serverAfter:");

        server.stop();
    }

    @Test public void autoload_1() {
        FusekiModules systemModules = FusekiModules.getSystemModules();
        ModuleByServiceLoader.reset();
        try  {
            FusekiModules.resetSystemDefault();
            FusekiModules loadedModules = FusekiAutoModules.get();
            FusekiModules.setSystemDefault(loadedModules);

            // Reloaded by FusekiModules.resetSystemDefault
            assertEquals(1, ModuleByServiceLoader.countLoads.get(),"countLoads:");
            assertEquals(1, ModuleByServiceLoader.countStart.get(), "countStart:");

            // Default : loaded FusekiModules
            FusekiServer.Builder builder = FusekiServer.create().port(0);
            ModuleForTest module = ModuleByServiceLoader.lastLoaded();
            lifecycle(builder, module);
        } finally {
            ModuleByServiceLoader.reset();
            FusekiModules.setSystemDefault(systemModules);
        }
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
        FusekiServer server = FusekiServer.create().port(0).fusekiModules(mods).build();
        assertTrue(called1.get());
        assertFalse(called2.get());
        try {
            server.start();
            assertTrue(called2.get());
        } finally { server.stop(); }
    }
}
