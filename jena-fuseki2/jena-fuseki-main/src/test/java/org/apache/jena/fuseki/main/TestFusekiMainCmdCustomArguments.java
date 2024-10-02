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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.cmds.CustomisedFusekiMain;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.junit.*;

import java.util.List;

import static org.junit.Assert.*;

/**
 * NOTE: we will randomise the port (--port=0) on all happy paths in order to avoid conflict with existing runs.
 */
public class TestFusekiMainCmdCustomArguments {

    private static String level = null;

    @BeforeClass public static void beforeClass() {
        // This is not reset by each running server.
        FusekiLogging.setLogging();
        level = LogCtl.getLevel(Fuseki.serverLog);
        LogCtl.setLevel(Fuseki.serverLog, "WARN");
    }

    @AfterClass public static void afterClass() {
        if ( level != null )
            LogCtl.setLevel(Fuseki.serverLog, level);
    }

    private FusekiServer server = null;
    @After public void after() {
        if ( server != null )
            server.stop();
    }

    @Test
    public void test_custom_argument() {
        // given
        List<String> arguments = List.of("--port=0", "--mem", "--custom-flag", "--custom-arg", "test", "/ds");
        // when
        buildServer(buildCmdLineArguments(arguments));
        // then
        assertNotNull(server);
        Assert.assertNotNull(server.getServletContext().getAttribute("flag"));
        Assert.assertNotNull(server.getServletContext().getAttribute("arg"));
    }

    private void testForCmdException(List<String> arguments, String expectedMessage) {
        // when
        Throwable actual = null;
        try {
            buildServer(buildCmdLineArguments(arguments));
        } catch (Exception e) {
            actual = e;
        }
        // then
        assertNotNull(actual);
        assertTrue("Expecting correct exception", (actual instanceof CmdException));
        assertEquals("Expecting correct message", expectedMessage, actual.getMessage());
    }


    private static String[] buildCmdLineArguments(List<String> listArgs) {
        return listArgs.toArray(new String[0]);
    }

    // Build and set the server
    private void buildServer(String... cmdline) {
        if ( server != null )
            fail("Bad test - a server has already been created");
        server = CustomisedFusekiMain.buildCustom(cmdline);
        server.start();
    }
}
