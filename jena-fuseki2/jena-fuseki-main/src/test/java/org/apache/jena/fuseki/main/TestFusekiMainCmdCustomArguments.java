/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.runner.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

public class TestFusekiMainCmdCustomArguments {

    static { FusekiLogging.setLogging(); }

    private static String confFixedStr = """
            PREFIX :        <http://example/base/>
            PREFIX fuseki:  <http://jena.apache.org/fuseki#>
            PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>

            :service rdf:type fuseki:Service ;
                fuseki:name "dataset" ;
                fuseki:endpoint [ fuseki:operation fuseki:query ; ] ;
                fuseki:dataset [ rdf:type ja:MemoryDataset ]
                .
                """;
    private static Model confFixed = RDFParser.fromString(confFixedStr,  Lang.TTL).toModel();

    private static String level = null;

    @BeforeAll public static void beforeClass() {
        FusekiLogging.setLogging();
        level = LogCtl.getLevel(Fuseki.serverLog);
        LogCtl.setLevel(Fuseki.serverLog, "WARN");
    }

    @AfterAll public static void afterClass() {
        if ( level != null )
            LogCtl.setLevel(Fuseki.serverLog, level);
        // Clear up!
    }

    @AfterEach public void after() {
        // No servers started here.
    }

    @Test
    public void test_custom_no_custom_args() {
        String[] arguments = {"--port=0", "--mem","/ds"};
        test(new ArgDecl(false, "special"), arguments, false, null);
    }

    @Test
    public void test_custom_no_custom_args_decl() {
        String[] arguments = {"--port=0", "--special", "--mem","/ds"};
        assertThrows(CmdException.class, ()->FusekiMain.construct(arguments));
    }

    @Test
    public void test_custom_allowNoArgs() {
        String[] arguments1 = {"--port=0"};
        FusekiModule customizer = new TestArgsAllowNoSetup();
        FusekiServer server1 = test(customizer, arguments1, serv->{});
        // Dataset arguments are allowed.
        String[] arguments2 = {"--port=0","--mem", "/ds"};
        FusekiServer server2 = test(customizer, arguments2, serv->{});

    }

    @Test
    public void test_custom_flag() {
        String[] arguments = {"--port=0", "--mem", "--custom-flag", "/ds"};
        test(new ArgDecl(false, "custom-flag"), arguments, true, null);
    }

    @Test
    public void test_custom_arg() {
        String[] arguments = {"--port=0", "--mem", "--custom-arg", "test", "/ds"};
        test(new ArgDecl(true, "custom-arg"), arguments, true, "test");
    }

    @Test
    public void test_custom_confModel_noFlag() {
        String[] arguments = {"--port=0", "--mem", "/ds"};
        TestArgsCustomModelAltArg customizer = new TestArgsCustomModelAltArg(new ArgDecl(false, "fixed"), confFixed);
        test(customizer, arguments, server->{
            assertSame(null, customizer.notedServerConfigModel);
            DataAccessPoint dap1 = server.getDataAccessPointRegistry().get("/ds");
            assertNotNull(dap1);
            DataAccessPoint dap2 = server.getDataAccessPointRegistry().get("/dataset");
            assertNull(dap2);
        });
    }

    @Test
    public void test_custom_confModel_replace() {
        // Ignores command line.
        String[] arguments = {"--port=0", "--conf=somefile.ttl", "--fixed"};
        FusekiModule customizer = new TestArgsCustomModelAltArg(new ArgDecl(false, "fixed"), confFixed);
        FusekiServer server = test(customizer, arguments, serv->{
            DataAccessPoint dap1 = serv.getDataAccessPointRegistry().get("/ds");
            assertNull(dap1);
            DataAccessPoint dap2 = serv.getDataAccessPointRegistry().get("/dataset");
            assertNotNull(dap2);
        });
        assertTrue(server.getDataAccessPointRegistry().isRegistered("/dataset"));
        assertFalse(server.getDataAccessPointRegistry().isRegistered("/ds"));
    }

    @Test
    public void test_custom_confModel_different() {
        // Ignores command line --conf setting.
        String[] arguments = {"--port=0", "--conf=somefile.ttl"};
        FusekiModule customizer = new TestArgsMyConfModel();
        FusekiServer server = test(customizer, arguments, serv->{
            DataAccessPoint dap1 = serv.getDataAccessPointRegistry().get("/ds");
            assertNull(dap1);
            DataAccessPoint dap2 = serv.getDataAccessPointRegistry().get("/dataset");
            assertNotNull(dap2);
        });
        assertTrue(server.getDataAccessPointRegistry().isRegistered("/dataset"));
        assertFalse(server.getDataAccessPointRegistry().isRegistered("/ds"));
    }

    @Test
    public void test_custom_confModel_different_ignore() {
        // --conf not used. Does not reset.
        String[] arguments = {"--port=0", "--mem", "/ds"};
        FusekiModule customizer = new TestArgsMyConfModel();
        FusekiServer server = test(customizer, arguments, serv->{
            DataAccessPoint dap1 = serv.getDataAccessPointRegistry().get("/ds");
            assertNotNull(dap1);
            DataAccessPoint dap2 = serv.getDataAccessPointRegistry().get("/dataset");
            assertNull(dap2);
        });
        assertFalse(server.getDataAccessPointRegistry().isRegistered("/dataset"));
        assertTrue(server.getDataAccessPointRegistry().isRegistered("/ds"));
    }

    // ----

    private void test(ArgDecl argDecl, String[] arguments, boolean seen, String value) {
        TestArgsCustomArg customizer = new TestArgsCustomArg(argDecl);
        test(customizer, arguments, (server)->{
            assertEquals(seen, customizer.argSeen);
            assertEquals(value, customizer.argValue);
        });
    }

    // Does not start the server
    private FusekiServer test(FusekiModule customizer, String[] arguments, Consumer<FusekiServer> checker) {
        FusekiModules fmods = (customizer == null)
                ? FusekiModules.empty()
                : FusekiModules.create(customizer);
        FusekiServer server = FusekiMain.construct(fmods, arguments);
        if ( checker != null )
            checker.accept(server);
        return server;
    }

    // ---- Arg customizers.

    // Allow no dataset or configuration.
    static class TestArgsAllowNoSetup implements FusekiModule {
        @Override
        public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
            serverArgs.allowEmpty = true;
        }
    }

    // Custom argument.
    static class TestArgsCustomArg implements FusekiModule {
        final ArgDecl argDecl;
        String argValue = null;
        boolean argSeen = false;

        TestArgsCustomArg(ArgDecl argDecl) {
            this.argDecl = argDecl;
        }

        @Override
        public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverConfig) {
            fusekiCmd.add(argDecl);
        }

        @Override
        public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
            argSeen = fusekiCmd.contains(argDecl);
            argValue = fusekiCmd.getValue(argDecl);
        }
    };

    // --fixed triggers replacing the configuration model.
    static class TestArgsCustomModelAltArg implements FusekiModule {
        final ArgDecl argDecl;
        final Model fixedModel;
        Model notedServerConfigModel = null;
        boolean argSeen = false;

        TestArgsCustomModelAltArg(ArgDecl argDecl, Model conf) {
            this.argDecl = argDecl;
            this.fixedModel = conf;
        }

        @Override
        public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
            fusekiCmd.add(argDecl);
        }

        @Override
        public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
            argSeen = fusekiCmd.contains(argDecl);
            if ( argSeen ) {
                serverArgs.serverConfigModel = fixedModel;
                notedServerConfigModel = fixedModel;
                serverArgs.dataset = null;
            }
        }

        @Override
        public void serverArgsBuilder(FusekiServer.Builder serverBuilder, Model configModel) {
            if ( argSeen )
                assertSame(notedServerConfigModel, configModel);
            else
                assertNull(configModel);
        }
    };

    // Replace --conf setting with the model. Do nothing if no --conf.
    static class TestArgsMyConfModel implements FusekiModule {

        TestArgsMyConfModel() { }

        @Override
        public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {}

        @Override
        public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
            if ( serverArgs.serverConfigFile != null ) {
                serverArgs.serverConfigFile = null;
                serverArgs.serverConfigModel = confFixed;
            }
        }

        @Override
        public void serverArgsBuilder(FusekiServer.Builder serverBuilder, Model configModel) {}
    };
}
