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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mod.admin.FMod_Admin;
import org.apache.jena.fuseki.mod.prometheus.FMod_Prometheus;
import org.apache.jena.fuseki.mod.shiro.FMod_Shiro;
import org.apache.jena.fuseki.mod.ui.FMod_UI;

public class FusekiModServer {

    public static void main(String... args) {
        runAsync(args).join();
    }

    public static FusekiServer runAsync(String... args) {
        return construct(args).start();
    }

    public static FusekiServer construct(String... args) {
        // Order: FMod_Admin before FMod_Shiro
        // These modules may have state that is carried across the build steps.
        FusekiModule fmodShiro = FMod_Shiro.create();
        FusekiModule fmodAdmin = FMod_Admin.create();

        FusekiModules serverModules = FusekiModules.create( fmodAdmin
                                                          , FMod_UI.get()
                                                          , fmodShiro
                                                          , FMod_Prometheus.get() );
        serverModules.forEach(FusekiMain::addCustomiser);

        System.setProperty("FUSEKI_BASE", "run");
        FileOps.ensureDir("run");

        // Adjust args.
        List<String> argList = Arrays.asList(args);
        // Ensure "--empty", "--modules=true"
        // Better?: moded startup - i.e. setting defaults.

        if ( args.length == 0 ) {
            String [] defaultArgs = { "--port=3030", "--empty" };
            args = defaultArgs;
        } else {
            List<String> argsList = new ArrayList<String>(Arrays.asList(args));
            if ( ! containsArg(argList, "--?empty") )
                argsList.add(0, "--empty"); // addFirst in java21
            if ( ! containsArg(argList, "--?modules") )
                argsList.add(0, "--modules=true");
            args = argsList.toArray(args);
        }

        FusekiModules modules = serverModules;
        FusekiModules.setSystemDefault(modules);
        FusekiServer server = FusekiServer.construct(args);
        return server;
    }

    private static boolean containsArg(List<String> argList, String argRegex) {
        //Pattern pattern = Pattern.compile(argRegex);

        return argList.stream().anyMatch(arg->{
            return arg.matches(argRegex);
        });
    }
}
