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

package org.apache.jena.fuseki.main;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.FusekiCoreInfo;
import org.slf4j.Logger;

public class FusekiRunner {

    // ---- Logging fragments

    public static void logCode(Logger log) {
        FusekiCoreInfo.logCode(log);
    }

    public static void logServerSetup(Logger log, FusekiServer server) {
        boolean verbose = Fuseki.getVerbose(server.getServletContext());
        FusekiCoreInfo.logDataAccessPointRegistry(log, server.getDataAccessPointRegistry(), verbose);
        FusekiCoreInfo.logSystemDetails(log);
    }

    public static void logServerStart(Logger log, FusekiServer server) {
        if ( ! server.getJettyServer().isStarted() )
            throw new FusekiException("FusekiServer not ready");
        int httpPort = server.getHttpPort();
        int httpsPort = server.getHttpsPort();
        if ( httpsPort > 0 && httpPort > 0 )
            Fuseki.serverLog.info("Start Fuseki (http="+httpPort+" https="+httpsPort+")");
        else if ( httpsPort > 0 )
            Fuseki.serverLog.info("Start Fuseki (https="+httpsPort+")");
        else if ( httpPort > 0 )
            Fuseki.serverLog.info("Start Fuseki (http="+httpPort+")");
        else
            Fuseki.serverLog.info("Start Fuseki");
    }

    public static void logServerStop(Logger log, FusekiServer server) {
        log.info("Stopping Fuseki");
    }
}
