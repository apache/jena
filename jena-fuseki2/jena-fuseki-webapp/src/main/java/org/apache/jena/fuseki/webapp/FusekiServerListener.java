/**
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

package org.apache.jena.fuseki.webapp;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.cmd.FusekiArgs;
import org.apache.jena.fuseki.metrics.MetricsProviderRegistry;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.FusekiCoreInfo;
import org.apache.jena.fuseki.server.OperationRegistry;
import org.slf4j.Logger;

/** Setup configuration.
 * The order is controlled by {@code web.xml}:
 * <ul>
 * <li>{@link FusekiServerEnvironmentInit}
 * <li>{@link ShiroEnvironmentLoader}
 * <li>{@link FusekiServerListener}, the main configuration
 * </ul>
 */

public class FusekiServerListener implements ServletContextListener {

    public FusekiServerListener() { }

    public static FusekiArgs initialSetup = null;

    private boolean initialized = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        String x = servletContext.getContextPath();
        if ( ! x.isEmpty() )
            Fuseki.configLog.info("Context path = "+x);
        serverInitialization(servletContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        org.apache.jena.tdb.sys.TDBInternal.reset();
        org.apache.jena.tdb2.sys.TDBInternal.reset();
    }

    private synchronized void serverInitialization(ServletContext servletContext) {
        if ( initialized )
            return;
        initialized = true;

        OperationRegistry operationRegistry = OperationRegistry.createStd();
        OperationRegistry.set(servletContext, operationRegistry);
        DataAccessPointRegistry dataAccessPointRegistry = new DataAccessPointRegistry();
        DataAccessPointRegistry.set(servletContext, dataAccessPointRegistry);

        try {
            FusekiWebapp.formatBaseArea();
            if ( ! FusekiWebapp.serverInitialized ) {
                Fuseki.serverLog.error("Failed to initialize : Server not running");
                return;
            }

            // The command line code sets initialSetup.
            // In a non-command line startup, initialSetup is null.
            if ( initialSetup == null ) {
                initialSetup = new FusekiArgs();
                String cfg = FusekiEnv.FUSEKI_BASE.resolve(FusekiWebapp.DFT_CONFIG).toAbsolutePath().toString();
                initialSetup.fusekiServerConfigFile = cfg;
            }

            if ( initialSetup == null ) {
                Fuseki.serverLog.error("No configuration");
                throw new FusekiException("No configuration");
            }
            Fuseki.setVerbose(servletContext, initialSetup.verbose);
            FusekiWebapp.initializeDataAccessPoints(dataAccessPointRegistry,
                                                    initialSetup,
                                                    FusekiWebapp.dirConfiguration.toString());
            dataAccessPointRegistry.forEach((name, dap)->{
                dap.getDataService().setEndpointProcessors(operationRegistry);
                dap.getDataService().goActive();
                //Fuseki.configLog.info("Register: "+dap.getName());
            });

            MetricsProviderRegistry.bindPrometheus(dataAccessPointRegistry);

        } catch (Throwable th) {
            Fuseki.serverLog.error("Exception in initialization: {}", th.getMessage());
            throw th;
        }

        if ( initialSetup.quiet )
            return;

        info(initialSetup.datasetPath,
             initialSetup.datasetDescription,
             initialSetup.fusekiServerConfigFile,
             dataAccessPointRegistry);
    }

    /** Print command line setup */
    private static void info(String datasetPath,
                            String datasetDescription,
                            String serverConfigFile,
                            DataAccessPointRegistry dapRegistry) {
        Logger log = Fuseki.serverLog;
        FusekiCoreInfo.logServerCmdSetup(log, initialSetup.verbose,
                                         dapRegistry,
                                         datasetPath, datasetDescription, serverConfigFile, null);
    }
}
