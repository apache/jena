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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.cmds.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiServerArgsCustomiser;
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

    @BeforeClass public static void beforeClass() {
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
    public void test_custom_no_custom_args() {
        String[] arguments = {"--port=0", "--mem","/ds"};
        test(new ArgDecl(false, "special"), arguments, false, null);
    }

    @Test(expected = CmdException.class)
    public void test_custom_no_custom_args_decl() {
        String[] arguments = {"--port=0", "--special", "--mem","/ds"};
        FusekiServer server = FusekiMain.build(arguments);
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
        FusekiCliCustomModel customiser = new FusekiCliCustomModel(new ArgDecl(false, "fixed"), confFixed);
        test(customiser, arguments, server->{
            assertSame(null, customiser.notedServerConfigModel);
            DataAccessPoint dap1 = server.getDataAccessPointRegistry().get("/ds");
            assertNotNull(dap1);
            DataAccessPoint dap2 = server.getDataAccessPointRegistry().get("/dataset");
            assertNull(dap2);
        });
    }

    @Test
    public void test_custom_confModel_withFlag() {
        // Ignores command line.
        String[] arguments = {"--port=0", "--mem", "--fixed", "/ds"};
        FusekiCliCustomModel customiser = new FusekiCliCustomModel(new ArgDecl(false, "fixed"), confFixed);
        FusekiServer server = test(customiser, arguments, serv->{
            DataAccessPoint dap1 = serv.getDataAccessPointRegistry().get("/ds");
            assertNull(dap1);
            DataAccessPoint dap2 = serv.getDataAccessPointRegistry().get("/dataset");
            assertNotNull(dap2);
        });
        assertTrue(server.getDataAccessPointRegistry().isRegistered("/dataset"));
        assertFalse(server.getDataAccessPointRegistry().isRegistered("/ds"));
    }

    // ----

    private void test(ArgDecl argDecl, String[] arguments, boolean seen, String value) {
        FusekiCliCustomArg customiser = new FusekiCliCustomArg(argDecl);
        test(customiser, arguments, (server)->{
            assertEquals(seen, customiser.argSeen);
            assertEquals(value, customiser.argValue);
        });
    }

    private FusekiServer test(FusekiServerArgsCustomiser customiser, String[] arguments, Consumer<FusekiServer> checker) {
        FusekiMain.resetCustomisers();
        FusekiMain.addCustomiser(customiser);
        FusekiServer server = FusekiMain.build(arguments);
        if ( checker != null )
            checker.accept(server);
        return server;
    }

    // ---- Test FusekiCliCustomisers

    static class FusekiCliCustomArg implements FusekiServerArgsCustomiser {
        final ArgDecl argDecl;
        String argValue = null;
        boolean argSeen = false;

        FusekiCliCustomArg(ArgDecl argDecl) {
            this.argDecl = argDecl;
        }

        @Override
        public void serverArgsModify(CmdGeneral fusekiCmd) {
            fusekiCmd.add(argDecl);
        }

        @Override
        public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
            argSeen = fusekiCmd.contains(argDecl);
            argValue = fusekiCmd.getValue(argDecl);
        }
    };

    static class FusekiCliCustomModel implements FusekiServerArgsCustomiser {
        final ArgDecl argDecl;
        final Model fixedModel;
        Model notedServerConfigModel = null;
        boolean argSeen = false;

        FusekiCliCustomModel(ArgDecl argDecl, Model conf) {
            this.argDecl = argDecl;
            this.fixedModel = conf;
        }

        @Override
        public void serverArgsModify(CmdGeneral fusekiCmd) {
            fusekiCmd.add(argDecl);
        }

        @Override
        public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
            argSeen = fusekiCmd.contains(argDecl);
            if ( argSeen ) {
                serverArgs.serverConfigModel = fixedModel;
                notedServerConfigModel = fixedModel;
                serverArgs.dsg = null;
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

    static class FusekiCliCustomConfiguration implements FusekiServerArgsCustomiser {
        @Override public void serverArgsModify(CmdGeneral fusekiCmd) { }
        @Override public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
            if ( Objects.equals(serverArgs.serverConfigFile, "placeholder") ) {
                serverArgs.serverConfigModel = confFixed;
            }

            //serverArgs.serverConfigFile;
            //serverArgs.serverConfigModel;
        }

        @Override public void serverArgsBuilder(FusekiServer.Builder serverBuilder, Model configModel) {}
    }

}
