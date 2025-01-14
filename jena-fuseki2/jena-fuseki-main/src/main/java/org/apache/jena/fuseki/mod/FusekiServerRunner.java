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

package org.apache.jena.fuseki.mod;

import java.net.BindException;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.cmds.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.main.sys.FusekiServerArgsCustomiser;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.fuseki.mod.admin.FMod_Admin;
import org.apache.jena.fuseki.mod.prometheus.FMod_Prometheus;
import org.apache.jena.fuseki.mod.shiro.FMod_Shiro;
import org.apache.jena.fuseki.mod.ui.FMod_UI;

public class FusekiServerRunner {

    public static void main(String... args) {
        //runAsync(args).join();
        prepareFusekiMain();
        FusekiMain.run(args);
    }

    public static FusekiServer runAsync(String... args) {
        FusekiServer server = construct(args);
        try {
            return server.start();
        } catch (FusekiException ex) {
            if ( ex.getCause() instanceof BindException ) {
//                if ( serverArgs.jettyConfigFile == null )
//                    Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port="+serverArgs.port);
//                else
//                    Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port in use");
                Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port in use");
                System.exit(1);
            }
            throw ex;
        } catch (Exception ex) {
            throw new FusekiException("Failed to start server: " + ex.getMessage(), ex);
        }
    }

    public static FusekiServer construct(String... args) {
        prepareFusekiMain();
        // Make server
        FusekiServer server = FusekiServer.construct(args);
        resetFusekiMain();
        return server;
    }

    private static void prepareFusekiMain() {
        String fusekiBase = Lib.getenv(FusekiServerCtl.envFusekiBase);
        if ( fusekiBase == null )
            fusekiBase = FusekiServerCtl.dftFusekiBase;
        FileOps.ensureDir(fusekiBase);

        FusekiModules serverModules = serverModules();

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

    /** A use-once {@link FusekiModules} for the full-featured Fuseki server. */
    public static FusekiModules serverModules() {
        // Modules may have state that is carried across the build steps or used for reload.
        FusekiModule fmodShiro = FMod_Shiro.create();
        FusekiModule fmodAdmin = FMod_Admin.create();
        FusekiModule fmodUI = FMod_UI.create();
        FusekiModule fmodPrometheus = FMod_Prometheus.create();

        FusekiModules serverModules = FusekiModules.create(fmodAdmin, fmodUI, fmodShiro, fmodPrometheus);
        return serverModules;
    }
}
