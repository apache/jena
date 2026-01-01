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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main.runner;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.main.sys.ModuleForTest;
import org.apache.jena.sys.JenaSystem;

/** Test FusekiArgs setup processing. */
public class TestFusekiSetupInternal {

    static { JenaSystem.init(); }

    private static String DIR = "testing/Config/";

    @Test public void core_setup() {
        ModuleForTest fmod = new ModuleForTest();

        FusekiModules fusekiModules = FusekiModules.create(fmod);
        String[] args = { "--port=0", "--conf="+DIR+"std-empty.ttl"};

        FusekiArgs fusekiArgs = FusekiArgs.args(fusekiModules, args);

        assertSame(fusekiModules, fusekiArgs.serverArgs.fusekiModules);

        assertEquals(0, fmod.countServer.get());

        // Process command line args according to the arguments specified.
        fusekiArgs.process();
        assertEquals(1, fmod.countArgsPrepare.get());
        assertEquals(1, fmod.countArgsModify.get());
        assertSame(fusekiModules, fusekiArgs.serverArgs.fusekiModules);

        FusekiServer.Builder builder = FusekiServer.create();
        fusekiArgs.applySetup(builder);

        assertEquals(0, fmod.countConfigured.get());
        assertEquals(0, fmod.countServer.get());

        FusekiServer fusekiServer = builder.build();
        assertNotNull(fusekiServer);
        try {
            assertEquals(1, fmod.countConfigured.get());
            assertEquals(1, fmod.countServer.get());
            assertEquals(0, fusekiServer.getPort());
            fusekiServer.start();
            // and we have a port.
            assertNotEquals(0, fusekiServer.getPort());
        } finally {
            fusekiServer.stop();
        }
    }

    @Test public void default_module_setup() {
        // Includes ModuleForTest
        FusekiModules fusekiSystemModules = FusekiModules.getSystemModules();
        test_modules(fusekiSystemModules);
    }

    @Test public void empty_module_setup() {
        FusekiModules fusekiSystemModules = FusekiModules.empty();
        test_modules(fusekiSystemModules);
    }

    private void test_modules(FusekiModules fusekiModulesStart) {
        String[] args = { "--port=0", "--conf="+DIR+"std-empty.ttl"};

        // All calls to FusekiArgs should decide the modules, not leave it to "null".
        assertThrows(NullPointerException.class, ()->new FusekiArgs(null, args));

        // Pass in null; null means use SystemModules
        FusekiArgs fusekiArgs = new FusekiArgs(fusekiModulesStart, args);
        assertNotNull(fusekiArgs.serverArgs.fusekiModules);

        fusekiArgs.process();

        FusekiServer.Builder builder = fusekiArgs.setup();
        assertNotNull(builder.fusekiModules());

        assertSame(fusekiModulesStart, builder.fusekiModules());

        FusekiModules serverModules = builder.build().getModules();
        assertNotNull(serverModules);
        assertSame(serverModules, fusekiModulesStart);
    }
}
