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

package org.apache.jena.fuseki.server;

import javax.servlet.ServletContext ;
import javax.servlet.ServletContextEvent ;
import javax.servlet.ServletContextListener ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.tdb.StoreConnection ;

/** Setup configurtation.
 * The order is controlled by {@code web.xml}:
 * <ul>
 * <li>{@link FusekiServerEnvironmentInit}
 * <li>{@link ShiroEnvironmentLoader}
 * <li>{@link FusekiServerListener}, the main configuration
 * </ul>
 */

public class FusekiServerListener implements ServletContextListener {

    public FusekiServerListener() { }
    
    public static ServerInitialConfig initialSetup = null ;

    private boolean initialized = false ;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext() ;
        String x = servletContext.getContextPath() ;
        if ( ! x.isEmpty() ) 
            Fuseki.configLog.info("Context path = "+x) ;
        serverInitialization(servletContext) ;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
//        DataAccessPointRegistry.get().forEach((key, dap) -> {
//            ??
//        }) ;
        // But in flight-transactions?
        StoreConnection.reset();
    }

    private synchronized void serverInitialization(ServletContext servletContext) {
        if ( initialized )
            return ;
        initialized = true ;

        DataAccessPointRegistry registry = new DataAccessPointRegistry() ;
        DataAccessPointRegistry.set(servletContext, registry);
        
        try {
            FusekiServer.formatBaseArea() ; 
            if ( ! FusekiServer.serverInitialized ) {
                Fuseki.serverLog.error("Failed to initialize : Server not running") ;
                return ;
            }
            
            // The command line code sets initialSetup.
            // In a non-commandline startup, initialSetup is null. 
            if ( initialSetup == null ) {
                initialSetup = new ServerInitialConfig() ;
                String cfg = FusekiEnv.FUSEKI_BASE.resolve(FusekiServer.DFT_CONFIG).toAbsolutePath().toString() ;
                initialSetup.fusekiServerConfigFile = cfg ;
            }

            if ( initialSetup != null ) {
                FusekiServer.initializeDataAccessPoints(registry,
                                                        initialSetup, FusekiServer.dirConfiguration.toString()) ;
            } else {
                Fuseki.serverLog.error("No configuration") ;
                throw new FusekiException("No configuration") ;
            }
        } catch (Throwable th) { 
            Fuseki.serverLog.error("Exception in initialization: {}", th.getMessage()) ;
            throw th ;
        }
    }
}

