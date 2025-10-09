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

import java.net.BindException;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.server.FusekiServerRunner;

/**
 * Functions for building and runner a {@link FusekiServer} configured from command line arguments.
 *
 * See {@link FusekiServerRunner} for similar functionality except it is also initializes {@link FusekiModules} as well.
 */
public class FusekiMainRunner {

    /**
     * Run a plain {@link FusekiServer}.
     * @param args line arguments.
     * @return Return the running server.
     */
    public static FusekiServer runAsync(String... args) {
        FusekiServer server = construct(args);
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
     * Run a plain {@link FusekiServer}.
     * This function does not return.
     */
    public static void run(String... args) {
        FusekiServer server = runAsync(args);
        server.join();
    }

    /**
     * Build but do not start, a {@link FusekiServer}.
     */
    public static FusekiServer construct(String... args) {
        FusekiServer.Builder builder = builder(args);
        return builder.build();
    }

    /**
     * Create a {@link FusekiServer.Builder}
     * initialized according to the command line arguments processed.
     */
    public static FusekiServer.Builder builder(String... args) {
        return FusekiMain.builder(args);
    }

}
