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
        
        confLog.info("FusekiServletContextListener created") ;
    }
    
    public static ServerInitialConfig initialSetup = null ;

    private volatile Boolean initialized = false ;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        confLog.info("Fuseki contextInitialized");
        ServletContext servletContext = sce.getServletContext() ;
//        String x = System.getProperty("user.dir") ;
//        Path currentRelativePath = Paths.get("");
//        String s = currentRelativePath.toAbsolutePath().toString();
//        confLog.info("dir1 = "+x+" : dir2 = "+s) ;
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
            
            Fuseki.init() ;         // Java wiring
            FusekiServer.init() ;   // Server and the filesystem 
            
            if ( initialSetup == null ) {
                initialSetup = new ServerInitialConfig() ;
                initialSetup.fusekiConfigFile = "config.ttl" ;
            }
            
            if ( initialSetup != null ) {
                FusekiServer.initializeDataAccessPoints(initialSetup, FusekiServer.dirConfiguration.toString()) ;
            } else {
                Fuseki.serverLog.error("No configuration") ;
                System.exit(0) ;
            }
        }
    }
    
    public static String chooseFusekiDirectory() {
        return "" ;
    }
}

