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

package org.apache.jena.fuseki.main.runner;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiMain;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiServer.Builder;
import org.apache.jena.fuseki.main.cmds.FusekiMainCmd;
import org.apache.jena.fuseki.main.cmds.FusekiServerUICmd;
import org.apache.jena.fuseki.main.sys.FusekiAutoModule;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mod.admin.FMod_Admin;
import org.apache.jena.fuseki.mod.prometheus.FMod_Prometheus;
import org.apache.jena.fuseki.mod.shiro.FMod_Shiro;
import org.apache.jena.fuseki.mod.system.FMod_Ping;
import org.apache.jena.fuseki.mod.ui.FMod_UI;
import org.apache.jena.sys.JenaSystem;

/**
 * {@code FusekiRunner} provides a number of forms of Fuseki for
 * running as a server using the command line as configuration.
 * <p>
 * Each is form is a selection of Fuseki modules.
 * All forms include any discovered {@link FusekiAutoModule FusekiAutoModules}
 * </p><p>
 * The commands {@link FusekiServerUICmd}, {@link FusekiMainCmd} call the forms here.
 * To take greater control of the configuration, use
 * {@link org.apache.jena.fuseki.main.FusekiServer.Builder FusekiServer.Builder}
 * </p>
 *
 * <ul>
 * <li>basic &ndash; no additional Fuseki modules. Only the command line
 * <li>main &ndash; a basic server with Prometheus statistics. This is the target of {@link FusekiMainCmd} </li>
 * <li>serverPlain &ndash; a server with all functionality, with Prometheus, no persistent admin area,
 *     Shiro configuration by comman dline or environment variable {@code FUISEKI_SHIRO}.</li>
 * <li>serverUI &ndash; a server with admin area and UI</li>
 * </ul>
 *
 * @see FusekiMain
 * @see FusekiServer
 */
public class FusekiRunner {

    static { JenaSystem.init(); }

    /*
     * Basic - minimal server
     * Main - server, except no admin and no UI => no server managed on-disk area.
     * PlainServer -  server, all functionality, except no and no UI; shiro only by command line argument
     * Server -  server, admin and UI => server managed on-disk area. This name will be preserved.
     */

    enum FusekiForm {
        BASIC("basic"), MAIN("main"), SERVER_PLAIN("server/plain"), SERVER("server/ui");
        final String fusekiFormName;
        FusekiForm(String string) { this.fusekiFormName = string; }
    }

    /** Minimal server - e.g. embedded use and testing. */
    public static void execBasic(String...args) {
        announcementExec(FusekiForm.BASIC);
        runnerBasic().exec(args);
    }

    /** Basic server */
    public static void execMain(String...args) {
        announcementExec(FusekiForm.MAIN);
        runnerMain().exec(args);
    }

    /** General server, with server-side admin functionality, no UI */
    public static void execServerPlain(String...args) {
        announcementExec(FusekiForm.SERVER_PLAIN);
        runnerServerPlain().exec(args);
    }

    /** General server, with UI */
    public static void execServerUI(String...args) {
        announcementExec(FusekiForm.SERVER);
        runnerServerUI().exec(args);
    }

    /** Minimal server - e.g. embedded use and testing */
    public static FusekiServer runAsyncBasic(String...args) {
        announcementAsync(FusekiForm.BASIC);
        return runnerBasic().runAsync(args);
    }

    /** Basic server */
    public static FusekiServer runAsyncMain(String...args) {
        announcementAsync(FusekiForm.MAIN);
        return runnerMain().runAsync(args);
    }

    /** All available server functionality, except admin functionality; no UI */
    public static FusekiServer runAsyncServerPlain(String...args) {
        announcementAsync(FusekiForm.SERVER_PLAIN);
        return runnerServerPlain().runAsync(args);
    }

    /** General server, with UI */
    public static FusekiServer runAsyncServerUI(String...args) {
        announcementAsync(FusekiForm.SERVER);
        return runnerServerUI().runAsync(args);
    }

    // @formatter:off
    /** Minimal server - e.g. embedded use and testing */
    public static Runner basic()       { return runnerBasic(); }
    /** Basic server */
    public static Runner main()        { return runnerMain(); }
    /** All available server functionality, except admin functionality; no UI; Shiro configuration by command line or environment variable. */
    public static Runner serverPlain() { return runnerServerPlain(); }
    /** General server, with UI */
    public static Runner serverUI()    { return runnerServerUI(); }
    // @formatter:on

    // Banners
    private static void announcementExec(FusekiForm form) {
        switch(form) {
            default -> FusekiRunnerLogging.logCode(Fuseki.fusekiLog);
        }
    }

    private static void announcementAsync(FusekiForm form) {
        switch(form) {
            default -> FusekiRunnerLogging.logCode(Fuseki.fusekiLog);
        }
    }

    // Modules. Fuseki Modules may have internal state in which case they should not
    // be used more than once; otherwise they are small objects.
    // We need to have fresh setups every time a server is created.

    // @formatter:off
    /** Minimal server - e.g. embedded use and testing */
    private static Runner runnerBasic() { return setup(fmodsBasic()); }
    /** Basic server */
    private static Runner runnerMain() { return setup(fmodsMain()); }
    /** All available server functionality, except admin functionality; no UI */
    private static Runner runnerServerPlain() { return setup(fmodsServerPlain()); }
    /** General server, with UI */
    private static Runner runnerServerUI() { return setup(fmodsServerUI()); }
    // @formatter:on

    // Setups.

    // Auto loaded modules and the given ones.
    private static FusekiModules fmods(FusekiModule...fmods) {
        return FusekiModules.add( FusekiModules.getSystemModules(), fmods );
    }

    // No auto loaded modules, only the given ones.
    private static FusekiModules fmods0(FusekiModule...fmods) {
        return FusekiModules.create(fmods);
    }

    // Embedded, No on-disk. No logging(?)
    public static FusekiModules fmodsBasic() {
        return FusekiModules.empty();
    }

    // RDF publishing. No on-disk. No auto loaded modules.
    public static FusekiModules fmodsMain() {
        return fmods0(FMod_Ping.create(),
                      FMod_Shiro.create(),
                      FMod_Prometheus.create());
    }

    public static FusekiModules fmodsServerPlain() {
        return fmods(FMod_Ping.create(),
                     FMod_Shiro.create(),
                     FMod_Prometheus.create()
                     );
    }

    // Fuseki Server. Includes admin - needs on-disk server space.
    public static FusekiModules fmodsServerUI() {
        return fmods(
                     // Enables /$/ping, /$/compact, /$/tasks
                     FMod_Admin.create(),
                     // Enables /$/stats
                     FMod_UI.create(),
                     // After FMod_Admin
                     FMod_Shiro.create(),
                     FMod_Prometheus.create()
                );
    }

    private static FusekiServer.Builder builder(FusekiModules fmods, String...args) {
        return FusekiMain.builder(fmods, args);
    }

    static class RunnerMods extends AbstractRunner {
        private final FusekiModules modules;

        RunnerMods(FusekiModules modules) {
            this.modules = modules;
        }

        @Override
        protected Builder builder(String...args) {
            return FusekiRunner.builder(modules, args);
        }
    }
    private static Runner setup(FusekiModules fmods) { return new RunnerMods(fmods); }
}
