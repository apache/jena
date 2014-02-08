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
import org.slf4j.Logger ;

public class FusekiServletContextListener implements ServletContextListener {
    private static Logger confLog = Fuseki.configLog ; 

    public FusekiServletContextListener() { 
        confLog.info("FusekiServletContextListener") ;
    }
    
    public static ServerInitialConfig initialSetup = null ;

    // --- later: Play the "hunt the config files" game 
    // Default.
    static public final String rootDirectory     = "/usr/share/fuseki" ;
    static public final String configurationFile = rootDirectory + "/config-fuseki.ttl" ;
    // ----
    static public final String configDir = /* dir root + */ Fuseki.configDirName ;
    
    private volatile Boolean initialized = false ;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        confLog.info("contextInitialized");
        ServletContext cxt = sce.getServletContext() ;
        init() ;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}

    public void init() {
        if ( initialized )
            return ;
        synchronized(initialized)
        {
            if ( initialized )
                return ;
            initialized = true ;
            Fuseki.init() ;

            if ( initialSetup == null ) {
                initialSetup = new ServerInitialConfig() ;
                initialSetup.fusekiConfigFile = "config.ttl" ;
            }
            
            if ( initialSetup != null ) {
                FusekiServer.init(initialSetup, configDir) ;
            } else {
                Fuseki.serverLog.error("No configuration") ;
                System.exit(0) ;
            }
                
        }
    }
}

