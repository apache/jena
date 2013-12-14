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

package webapp;

import javax.servlet.ServletContext ;
import javax.servlet.ServletContextEvent ;
import javax.servlet.ServletContextListener ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.server.FusekiConfig ;
import org.apache.jena.fuseki.server.ServerConfig ;
import org.slf4j.Logger ;

/** Manage server initialization */
public class ServerInit implements ServletContextListener {
   private static Logger confLog = Fuseki.configLog ; 
    
    // Only works with the uber servlet.

    // Default.
    static public String rootDirectory     = "/usr/share/fuseki" ;
//    static public String staticContentDir  = rootDirectory + "/pages" ;
    static public String configurationFile = rootDirectory + "/config-fuseki.ttl" ;

    static public ServerConfig serverConfig = null ;
    static Boolean initialized = false ;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //confLog.info("contextInitialized");
        ServletContext cxt = sce.getServletContext() ;
        confLog.info(cxt.getServletContextName()) ;
        init() ;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}

    public static void init() {
        if ( initialized )
            return ;
        synchronized(initialized)
        {
            if ( initialized )
                return ;
            Fuseki.init() ;
           confLog.error("Unconverted code - need to find configuration");
            
            // Find configuration file.
            if ( serverConfig != null )
                // already set - the standalone server may set this early
                // for command line use.
                return ;
            
            if ( configurationFile == null )
                confLog.error("No configuration file") ;
            serverConfig = FusekiConfig.configure(configurationFile) ;
            
            for ( DatasetRef dsDesc : serverConfig.datasets ) {
                String datasetPath = dsDesc.name ;
                if ( datasetPath.equals("/") )
                    datasetPath = "" ;
                else
                    if ( !datasetPath.startsWith("/") )
                        datasetPath = "/" + datasetPath ;

                if ( datasetPath.endsWith("/") )
                    datasetPath = datasetPath.substring(0, datasetPath.length() - 1) ;

                dsDesc.enable(); 
                // Register with "/ds"
                // Because config file says "ds" and command line + Uber usually says "/ds"
                // hence somewhere non-uber does this as well.  Check.
                // mapRequestToDatasetLongest$ vs ?
                dsDesc.name = datasetPath ;
                //confLog.info("Register: "+datasetPath+" : "+dsDesc.name);
                DatasetRegistry.get().put(datasetPath, dsDesc) ;
            }
            
            // Just the datasets

            //serverConfig.pages = staticContentDir ;
            //serverConfig.verboseLogging = false ;
            
            // Default, built-in.
            //serverConfig = FusekiConfig.defaultConfiguration(datasetPath, dsg, allowUpdate, listenLocal) ;
        
            // NA -- serverConfig.port = port ;
            // NA -- serverConfig.mgtPort = mgtPort ;
            // NA -- serverConfig.pagesPort = port ;
            // NA -- serverConfig.loopback = listenLocal ;
            // NA? -- serverConfig.enableCompression = enableCompression ;
            // NA -- serverConfig.jettyConfigFile = jettyConfigFile ;
            // NA -- serverConfig.authConfigFile = authConfigFile ; // Use web container.
            
        }
    }
}

