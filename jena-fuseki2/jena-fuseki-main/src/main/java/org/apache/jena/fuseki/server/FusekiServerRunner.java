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

package org.apache.jena.fuseki.server;

import java.net.BindException;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiMainRunner;
import org.apache.jena.fuseki.main.FusekiRunner;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.cmds.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.main.sys.FusekiServerArgsCustomiser;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.fuseki.mod.FusekiServerModules;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;

/**
 * Functions for building and runner a {@link FusekiServer} configured from command line arguments
 * and system {@link FusekiModules}.
 *
 * See {@link FusekiMainRunner} for similar functionality except without the configuring with {@link FusekiModules}.
 */
public class FusekiServerRunner {

    static { JenaSystem.init(); }

    /**
     * Run {@link FusekiServer} with {@link FusekiModules} as given by {@link FusekiServerModules#serverModules()}.
     * @param args Command line arguments.
     * @return Return the running server.
     */
    public static FusekiServer runAsync(String... args) {
        FusekiServer server = construct(args);
        startAsync(server);
        return server;
    }

    private static FusekiServer startAsync(FusekiServer server) {
        try {
            return server.start();
        } catch (FusekiException ex) {
            if ( ex.getCause() instanceof BindException ) {
                Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port in use");
                System.exit(1);
            }
            throw ex;
        } catch (Exception ex) {
            throw new FusekiException("Failed to start server: " + ex.getMessage(), ex);
        }
    }

    /**
     * Run {@link FusekiServer} with {@link FusekiModules} as given by {@link FusekiServerModules#serverModules()}.
     * This function does not return.
     */
    public static void run(String... args) {
        Logger log = Fuseki.fusekiLog;
        FusekiRunner.logCode(log);
        FusekiServer server = construct(args);
        FusekiRunner.logServerSetup(log, server);
        startAsync(server);
        FusekiRunner.logServerStart(log, server);
        server.join();
    }

    /**
     * Build but do not start, a {@link FusekiServer} with {@link FusekiModules} as given by {@link FusekiServerModules#serverModules()}.
     */
    public static FusekiServer construct(String... args) {
        FusekiServer.Builder builder = builder(args);
        return builder.build();
    }

    /**
     * Create a {@code FusekiServer.Builder} that has the FusekiServer with
     * server modules setup and the command line args processed.
     */
    public static FusekiServer.Builder builder(String... args) {
        prepareFusekiServerConstruct();
        try {
            return FusekiMain.builder(args);
        } finally {
            resetFusekiMain();
        }
    }

    private static void prepareFusekiServerConstruct() {
        String fusekiBase = Lib.getenv(FusekiServerCtl.envFusekiBase);
        if ( fusekiBase == null )
            fusekiBase = FusekiServerCtl.dftFusekiBase;
        FileOps.ensureDir(fusekiBase);

        FusekiModules serverModules = FusekiServerModules.serverModules();

        // Adjust the default settings of ServerArgs
        FusekiServerArgsCustomiser initializeServerArgs = new FusekiServerArgsCustomiser() {
            @Override
            public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
                serverArgs.allowEmpty = true;
                serverArgs.fusekiModules = serverModules;
            }
        };

        FusekiMain.resetCustomisers();
        FusekiMain.addCustomiser(initializeServerArgs);
        // They can also modify the argument processing.
        serverModules.forEach(FusekiMain::addCustomiser);
    }

    private static void resetFusekiMain() {
        FusekiMain.resetCustomisers();
    }
}
