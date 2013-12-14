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

    // This could do the initialization. 
    private final SPARQLServer sparqlServer ;
    
    // Embedded version.
    public FusekiServletContextListener(SPARQLServer sparqlServer) {
        this.sparqlServer = sparqlServer ;
    }

    // web.xml version.
    public FusekiServletContextListener() { sparqlServer = null ; }
    
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
        }
    }
}

