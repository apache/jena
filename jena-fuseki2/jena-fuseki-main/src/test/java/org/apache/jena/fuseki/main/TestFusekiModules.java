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

import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFusekiModules {

    // Module loading.
    // file :: src/test/resources/META-INF/services/org.apache.jena.fuseki.main.sys.FusekiModule
    // Module: ModuleForTest

    @BeforeClass public static void beforeClass() {
        FusekiModules.load();
    }

    @Before public void beforeTest() {
        ModuleForTest.module.clearLifecycle();
    }

    @AfterClass public static void afterTest() {
        FusekiModules.remove(ModuleForTest.module);
    }

    @Test public void modules_0() {
        ModuleForTest module = findModule();
        module.clearLifecycle();
        assertEquals(1, module.countStart.get());
        assertEquals(0, module.countConfiguration.get());
    }

    @Test public void lifecycle_1() {
        ModuleForTest module = findModule();

        FusekiServer.Builder builder = FusekiServer.create().port(0);

        assertEquals(1, module.countStart.get());
        assertEquals(0, module.countConfiguration.get());
        assertEquals(0, module.countServer.get());
        assertEquals(0, module.countServerStarting.get());

        FusekiServer server = builder.build();

        assertEquals(1, module.countStart.get());
        assertEquals(1, module.countConfiguration.get());
        assertEquals(1, module.countServer.get());
        assertEquals(0, module.countServerStarting.get());

        server.start();

        assertEquals(1, module.countStart.get());
        assertEquals(1, module.countConfiguration.get());
        assertEquals(1, module.countServer.get());
        assertEquals(1, module.countServerStarting.get());

        server.stop();
    }

    private ModuleForTest findModule() {
        return ModuleForTest.module;
    }
}
