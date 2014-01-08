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

import java.util.ArrayList ;
import java.util.List ;

import javax.servlet.ServletContext ;
import javax.servlet.ServletContextEvent ;
import javax.servlet.ServletContextListener ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.FileOps ;
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
    
    private Boolean initialized = false ;

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
                //FileOps.ensureDir(Fuseki.systemDatabaseName) ; 
                // XXX Make relative.
                FileOps.ensureDir(Fuseki.configDirName) ;
                FileOps.ensureDir(Fuseki.systemFileArea) ;
                
                // Places to look:
                // 1 - Fuseki v1 config file (deprecated)
                // 2 - Directory of assemblers files "assemblers"
                // 3 - The system database
                 
                List<DatasetRef> configFileDBs = findDatasets(initialSetup) ;
                List<DatasetRef> directoryDBs = FusekiConfig.readConfigurationDirectory(configDir) ;
                List<DatasetRef> systemDBs = FusekiConfig.readSystemDatabase(SystemState.getDataset()) ;
                
                List<DatasetRef> datasets = new ArrayList<DatasetRef>() ;
                datasets.addAll(configFileDBs) ;
                datasets.addAll(directoryDBs) ;
                datasets.addAll(systemDBs) ;
                
                // Having found them, set them all running.
                FusekiConfig.configureDatasets(datasets);
            }
        }
    }
    
    private static List<DatasetRef> findDatasets(ServerInitialConfig params) {  
        // Has a side effect of global context setting.

        List<DatasetRef> datasets = DS.list() ;

        if ( params.fusekiConfigFile != null ) {
            Fuseki.configLog.info("Configuration file: " + params.fusekiConfigFile) ;
            List<DatasetRef> cmdLineDatasets = FusekiConfig.readConfigFile(params.fusekiConfigFile) ;
            datasets.addAll(cmdLineDatasets) ;
        } else {
            List<DatasetRef> cmdLineDatasets = FusekiConfig.defaultConfiguration(params) ;
            datasets.addAll(cmdLineDatasets) ;
        }
        return datasets ;
    }
}

