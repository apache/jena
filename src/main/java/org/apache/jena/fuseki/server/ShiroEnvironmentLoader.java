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

import java.io.IOException ;
import java.io.InputStream ;
import java.net.URL ;

import javax.servlet.ServletContextEvent ;
import javax.servlet.ServletContextListener ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.shiro.io.ResourceUtils ;
import org.apache.shiro.web.env.EnvironmentLoader ;
import org.apache.shiro.web.env.ResourceBasedWebEnvironment ;
import org.apache.shiro.web.env.WebEnvironment ;
import org.apache.shiro.web.util.WebUtils ;
import org.slf4j.Logger ;

/** A place to perform Fuseki-specific initialization of Apache Shiro.
 *  This means finding shiro.ini in multiple possible places, based on
 *  different deployment setups.
 */
public class ShiroEnvironmentLoader extends EnvironmentLoader implements ServletContextListener {
    private static Logger confLog = Fuseki.configLog ;
    private ServletContextEvent sce ; 
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.sce = sce ;
        initEnvironment(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        destroyEnvironment(sce.getServletContext());
    }

    /** 
     * Normal Shiro initialization only supports one location for an INI file.
     *  
     * When given multiple multiple locations for the shiro.ini file, and 
     * if a {@linkplain ResourceBasedWebEnvironment}, check the list of configuration
     * locations, testing whether the name identified an existing resource.  
     * For the first resource name found to exist, reset the {@linkplain ResourceBasedWebEnvironment}
     * to name that resource alone so the normal Shiro initialization  
     */
    @Override
    protected void customizeEnvironment(WebEnvironment environment) {
        if ( environment instanceof ResourceBasedWebEnvironment ) {
            ResourceBasedWebEnvironment env = (ResourceBasedWebEnvironment)environment ;
            String[] locations = env.getConfigLocations() ;
            if ( locations.length > 1 ) {
                for ( String loc : locations ) {
                    if ( resourceExists(loc) ) {
                        locations = new String[] {loc} ;
                        env.setConfigLocations(locations);
                        return ;
                    }
                }
            }
        }
    }

    /** 
     * Test whether a name identified an existing resource
     * @param resource    A String in Shiro-resource name format (e.g. URL scheme names) 
     * @return True/false as to whether the resource can be found or not. 
     */
    
    private boolean resourceExists(String resource) {
        try {
            // See IniWebEnvironment.convertPathToIni
            if (!ResourceUtils.hasResourcePrefix(resource)) {
                //Sort out "path" and open as a webapp resource.
                resource = WebUtils.normalize(resource);
                //is = getServletContextResourceStream(path);
                URL url = sce.getServletContext().getResource(resource) ;
                return ( url == null ) ;
            } else {
                InputStream is = ResourceUtils.getInputStreamForPath(resource);
                boolean exists = (is != null ) ;
                is.close() ;
                return exists ;
            }
        } catch (IOException e) { return false ; }
    }
}
