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

package org.apache.jena.fuseki.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.main.sys.FusekiModulesLoaded;
import org.apache.jena.fuseki.main.sys.FusekiModulesSystem;
import org.apache.jena.rdf.model.Model;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFusekiModules {

    // Module loading.
    // file :: src/test/resources/META-INF/services/org.apache.jena.fuseki.main.sys.FusekiModule
    // Module: ModuleForTest

    private static FusekiModules system = null;

    @BeforeClass public static void beforeClass() {
        system = FusekiModulesSystem.get();
        FusekiModulesLoaded.resetSystem();
    }

    @Before public void beforeTest() { }

    @AfterClass public static void afterClass() {
        FusekiModulesSystem.set(system);
    }

    @Test public void modules_0() {
        ModuleForTest module = findModule();
        module.clearLifecycle();
        assertEquals(1, module.countStart.get());
        assertEquals(0, module.countConfiguration.get());
    }

    @Test public void modules_1() {
        boolean bIsEmpty1 = FusekiModulesLoaded.loaded().asList().isEmpty();
        assertFalse(bIsEmpty1);

        FusekiModulesLoaded.enable(false);

        boolean bIsEmpty2 =
                FusekiModulesLoaded.loaded().asList().isEmpty();
        assertTrue(bIsEmpty2);

        FusekiModulesLoaded.enable(true);

        boolean bIsEmpty3 = FusekiModulesLoaded.loaded().asList().isEmpty();
        assertFalse(bIsEmpty3);
    }

    @Test public void modules_2() {
        boolean bIsEmpty1 = FusekiModulesLoaded.loaded().asList().isEmpty();
        assertFalse(bIsEmpty1);
        FusekiModulesLoaded.enable(false);
        boolean bIsEmpty2 =
                FusekiModulesLoaded.loaded().asList().isEmpty();
        assertTrue(bIsEmpty2);
    }



    @Test public void lifecycle_1() {
        FusekiModulesLoaded.resetSystem();

        ModuleForTest module = findModule();

        assertFalse(FusekiModulesSystem.get().asList().isEmpty());

        FusekiServer.Builder builder = FusekiServer.create().setModules(FusekiModules.create(module)).port(0);

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

        assertFalse(FusekiModulesLoaded.loaded().asList().contains(oneOff));

        FusekiModules mods = FusekiModules.create(oneOff);
        assertFalse(called1.get());
        assertFalse(called2.get());
        FusekiServer server = FusekiServer.create().port(0).setModules(mods).build();
        assertTrue(called1.get());
        assertFalse(called2.get());
        try {
            server.start();
            assertTrue(called2.get());
        } finally { server.stop(); }
    }

    private ModuleForTest findModule() {
        ModuleForTest mod = new ModuleForTest();
        mod.start();
        return mod;
    }
}
