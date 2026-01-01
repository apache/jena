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

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiCoreInfo;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.main.sys.PlatformInfo;
import org.slf4j.Logger;

/** Code fragments used to log Fuseki start-stop */
public class FusekiRunnerLogging {

    public static void logCode(Logger log) {
        FusekiCoreInfo.logCode(log);
    }

    public static void logServerSetup(Logger log, FusekiServer server) {
        boolean verbose = Fuseki.getVerbose(server.getServletContext());
        FusekiCoreInfo.logDataAccessPointRegistry(log, server.getDataAccessPointRegistry(), verbose);
        if ( verbose )
            logModules(log, server);
        FmtLog.info(log, "System");
        if ( verbose )
            PlatformInfo.logSystemDetailsLong(log);
        else
            PlatformInfo.logSystemDetailsShort(log);
    }

    public static void logServerStarted(Logger log, FusekiServer server) {
        if ( ! server.getJettyServer().isStarted() )
            throw new FusekiException("FusekiServer not ready");

        int httpPort = server.getHttpPort();
        int httpsPort = server.getHttpsPort();

        if ( httpsPort > 0 )
            PlatformInfo.logInfo(log, "Port:", "https=%s", httpsPort);

        if ( httpPort > 0 )
            PlatformInfo.logInfo(log, "Port:", "http=%s", httpPort);

        log.info("Start Fuseki");
    }


    private static void logModules(Logger log, FusekiServer server) {
        FmtLog.info(log, "Modules");
        FusekiModules modules = server.getModules();
        if ( modules == null ) {
            PlatformInfo.logInfo(log, "No modules", "");
            return;
        }
        if ( modules.isEmpty() ) {
            PlatformInfo.logInfo(log, "No modules", "");
            return;
        }

        modules.forEach(fmod->{
            String name = fmod.name();
            if ( name == null )
                name = fmod.getClass().getSimpleName();
            PlatformInfo.logInfo(log, "Module:", "%s", name );
        });
    }

    public static void logServerStop(Logger log, FusekiServer server) {
        log.info("Stopping Fuseki");
    }
}
