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

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.server.FusekiCoreInfo;
import org.slf4j.Logger;

public class FusekiMainInfo {

    /** Details of the code version. */
    public static void logCode(Logger log) {
        FusekiCoreInfo.logCode(log);
    }

    /** Log server details. */
    public static void logServer(Logger log, FusekiServer server, boolean verbose) {
        FusekiMainInfo.logServerConnections(log, server);
        FusekiMainInfo.logServerDatasets(log, server, verbose);
        if ( server.getStaticContentDir() != null )
            FmtLog.info(log,  "Static files: %s", server.getStaticContentDir());
    }

    /** Log details about the code version */
    public static void logServerCode(Logger log) {
        FusekiCoreInfo.logCode(log);
    }

    /** The the server connection setup */
    public static void logServerConnections(Logger log, FusekiServer server) {
        int httpsPort = server.getHttpsPort();
        int httpPort = server.getHttpPort();
        if ( httpsPort > 0 && httpPort > 0 )
            log.info("Ports: http="+httpPort+" https="+httpsPort);
        else if ( httpsPort <= 0 )
            log.info("Port: http="+httpPort);
        else if ( httpPort <= 0 )
            log.info("Port: https="+httpsPort);
    }

    /** Log information about datasets in this server */
    public static void logServerDatasets(Logger log, FusekiServer server, boolean longForm) {
        FusekiCoreInfo.logDataAccessPointRegistry(log, server.getDataAccessPointRegistry(), longForm);
    }
}
