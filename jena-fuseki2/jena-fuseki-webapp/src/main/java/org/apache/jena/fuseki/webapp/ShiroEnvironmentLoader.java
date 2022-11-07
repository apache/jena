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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.irix.IRIs;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.io.ResourceUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.ResourceBasedWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;

/** A place to perform Fuseki-specific initialization of Apache Shiro.
 *  Runs after listener {@link FusekiServerEnvironmentInit} and before {@link FusekiServerListener}.
 *  This means finding shiro.ini in multiple possible places, based on
 *  different deployment setups.
 */
public class ShiroEnvironmentLoader extends EnvironmentLoader implements ServletContextListener {
    private ServletContext servletContext;

    public ShiroEnvironmentLoader() {}

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        FusekiWebapp.formatBaseArea();
        this.servletContext = sce.getServletContext();
        try {
            // Shiro.
            initEnvironment(servletContext);
        } catch (ConfigurationException  ex) {
            Fuseki.configLog.error("Shiro initialization failed: "+ex.getMessage());
            // Exit?
            throw ex;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        destroyEnvironment(sce.getServletContext());
    }

    /**
     * Normal Shiro initialization only supports one location for an INI file.
     *
     * When given multiple locations for the shiro.ini file, and
     * if a {@link ResourceBasedWebEnvironment}, check the list of configuration
     * locations, testing whether the name identified an existing resource.
     * For the first resource name found to exist, reset the {@link ResourceBasedWebEnvironment}
     * to name that resource alone so the normal Shiro initialization
     */
    @Override
    protected void customizeEnvironment(WebEnvironment environment) {
        if ( environment instanceof ResourceBasedWebEnvironment ) {
            ResourceBasedWebEnvironment env = (ResourceBasedWebEnvironment)environment;
            String[] locations = env.getConfigLocations();
            String loc = huntForShiroIni(locations);
            Fuseki.configLog.info("Shiro file: "+loc);
            if (loc != null )
                locations = new String[] {loc};
            env.setConfigLocations(locations);
        }
    }

    private static final String FILE = "file";

    /** Look for a Shiro ini file, or return null */
    private static String huntForShiroIni(String[] locations) {
        FusekiEnv.setEnvironment();
        Fuseki.init();
        for ( String loc : locations ) {
            // If file:, look for that file.
            // If a relative name without scheme, look in FUSEKI_BASE, FUSEKI_HOME, webapp.
            String scheme = IRIs.scheme(loc);

            // Covers C:\\ as a "scheme name"
            if ( scheme != null ) {
                if ( scheme.equalsIgnoreCase(FILE)) {
                    // Test file: for exists
                    Path p = Path.of(loc.substring(FILE.length()+1));
                    if ( ! p.toFile().exists() )
                        continue;
                    // Fall through.
                }
                // Can't test - try
                return loc;
            }
            // No scheme .
            Path p = Path.of(loc);

            String fn = resolve(FusekiEnv.FUSEKI_BASE, p);
            if ( fn != null )
                return "file://"+fn;
            fn = resolve(FusekiEnv.FUSEKI_HOME, p);
            if ( fn != null )
                return "file://"+fn;

            // Try in webapp.

            try ( InputStream is = ResourceUtils.getInputStreamForPath(loc); ) {
                boolean exists = (is != null );
                return loc;
            } catch (IOException e) { }
        }
        return null;
    }

    /** Directory + name -> filename if it exists */
    private static String resolve(Path dir, Path file) {
        Path p = dir.resolve(file);
        if ( p.toFile().exists() )
            return p.normalize().toString();
        return null;
    }
}
