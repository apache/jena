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

import static java.lang.String.format ;

import java.io.IOException ;
import java.nio.file.DirectoryStream ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;

/** 
 * Separate initialization for FUSEKI_HOME and FUSEKI_BASE so that 
 * Fusekilogging can use these values.
 * This code must not touch Jena.  
 * 
 * @see FusekiServer 
 */ 
public class FusekiEnv {
    // Initialization logging happens via stdout/stderr directly.
    // Fuseki logging is not initialized to avoid going in circles.
    
    private static final boolean LogInit         = false ;
    
    /** Unused */
    // public static final String DFT_FUSEKI_HOME = isWindows 
    //        ? /*What's correct here?*/ "/usr/share/fuseki"
    //        : "/usr/share/fuseki" ;
    static final boolean isWindows = determineIfWindows() ;
    static final String  DFT_FUSEKI_BASE = isWindows ? /* What's correct here? */"/etc/fuseki" : "/etc/fuseki" ;
    
    /** Initialization mode, depending on the way Fuseki is started:
        <ul>
        <li>{@code WAR} - Running as a WAR file.</li>
        <li>{@code EMBEDDED}</li>
        <li>{@code STANDALONE} - Running as the standalone server in Jetty</li>
        <li>{@code TEST} - Running inside maven/JUnit and as the standalone server</li>
        <li>{@code UNSET} - Initial state.</li>
        </ul>
        <p> 
        If at server initialization, the MODE is UNSET, then assume WAR setup.
        A WAR file does not have the opportunity to set the mode.
        <p>
        TEST:  (better to set FUSEKI_HOME, FUSEKI_BASE from the test environment</li>
    */
    public enum INIT {
        // Default values of FUSEKI_HOME, and FUSEKI_BASE. 
        WAR         (null, "/etc/fuseki") , 
        EMBEDDED    (".", "run") ,
        STANDALONE  (".", "run") ,
        TEST        ("src/main/webapp", "target/run") ,
        UNSET       (null, null) ;
        
        final String dftFusekiHome ;
        final String dftFusekiBase ;
        
        INIT(String home, String base) {
            this.dftFusekiHome = home ;
            this.dftFusekiBase = base ;
        }
    }
    
    public static INIT mode = INIT.UNSET ;
    
    /** Root of the Fuseki installation for fixed files. 
     *  This may be null (e.g. running inside a web application container) */ 
    public static Path FUSEKI_HOME = null ;
    
    /** Root of the varying files in this deployment. Often $FUSEKI_HOME/run.
     * This is not null - it may be /etc/fuseki, which must be writable.
     */ 
    public static Path FUSEKI_BASE = null ;
    
    // Copied from SystemTDB to avoid dependency.
    // This code must not touch Jena.  
    private static boolean determineIfWindows() {
        String s = System.getProperty("os.name") ;
        if ( s == null )
            return false ;
        return s.startsWith("Windows ") ;
    }
 
    public static final String   ENV_runArea     = "run" ;
    private static boolean       initialized     = false ;
    
    /** Initialize the server */
    public static synchronized void setEnvironment() {
        if ( initialized )
            return ;
        initialized = true ;
        
        logInit("FusekiEnv:Start: ENV_FUSEKI_HOME = %s : ENV_FUSEKI_BASE = %s : MODE = %s", FUSEKI_HOME, FUSEKI_BASE, mode) ;
        
        if ( mode == null || mode == INIT.UNSET )
            mode = INIT.WAR ;

        if ( FUSEKI_HOME == null ) {
            // Make absolute
            String x1 = getenv("FUSEKI_HOME") ;
            if ( x1 == null )
                x1 = mode.dftFusekiHome ;
            if ( x1 != null )
                FUSEKI_HOME = Paths.get(x1) ;
        }

        if ( FUSEKI_BASE == null ) {
            String x2 = getenv("FUSEKI_BASE") ;
            if ( x2 == null )
                x2 = mode.dftFusekiBase ;
            if ( x2 != null )
                FUSEKI_BASE = Paths.get(x2) ;
            else {
                if ( FUSEKI_HOME != null )
                    FUSEKI_BASE = FUSEKI_HOME.resolve(ENV_runArea) ;
                else {
                    // This is bad - there should have been a default by now.
                    logInitError("Can't find a setting for FUSEKI_BASE - guessing wildy") ;
                    // Neither FUSEKI_HOME nor FUSEKI_BASE set.
                    FUSEKI_BASE = Paths.get(DFT_FUSEKI_BASE) ;
                }
            }
        }

        if ( FUSEKI_HOME != null )
            FUSEKI_HOME = FUSEKI_HOME.toAbsolutePath() ;

        FUSEKI_BASE = FUSEKI_BASE.toAbsolutePath() ;

        logInit("FusekiEnv:Finish: ENV_FUSEKI_HOME = %s : ENV_FUSEKI_BASE = %s", FUSEKI_HOME, FUSEKI_BASE) ;
    }
    
    private static void logInit(String fmt, Object ... args) {
        if ( LogInit ) {
            System.out.printf(fmt, args) ; 
            System.out.println() ;
        }
    }
    
    private static void logInitError(String fmt, Object ... args) {
        System.err.printf(fmt, args) ; 
        System.err.println() ;
    }

    /** Get environment variable value (maybe in system properties) */
    public static String getenv(String name) {
        String x = System.getenv(name) ;
        if ( x == null )
            x = System.getProperty(name) ;
        return x ;
    }

    /** Dataset set name to configuration file name. */
    public static String datasetNameToConfigurationFile(HttpAction action, String dsName) {
        List<String> existing = existingConfigurationFile(dsName) ;
        if ( ! existing.isEmpty() ) {
            if ( existing.size() > 1 ) {
                action.log.warn(format("[%d] Multiple existing configuration files for %s : %s",
                                       action.id, dsName, existing));
                ServletOps.errorBadRequest("Multiple existing configuration files for "+dsName);
                return null ;
            }
            return existing.get(0) ;
        }
        
        return generateConfigurationFilename(dsName) ;
    }

    /** Choose a configuration file name - existign one or ".ttl" form if new */
    public static String generateConfigurationFilename(String dsName) {
        String filename = dsName ;
        // Without "/"
        if ( filename.startsWith("/"))
            filename = filename.substring(1) ;
        filename = FusekiServer.dirConfiguration.resolve(filename).toString()+".ttl" ;
        return filename ;
    }

    /** Return the filenames of all matching files in the configuration directory */  
    public static List<String> existingConfigurationFile(String baseFilename) {
        try { 
            List<String> paths = new ArrayList<>() ;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(FusekiServer.dirConfiguration, baseFilename+"*") ) {
                stream.forEach((p)-> paths.add(p.getFileName().toString())) ;
            }
            return paths ;
        } catch (IOException ex) {
            throw new InternalErrorException("Failed to read configuration directory "+FusekiServer.dirConfiguration) ;
        }
    }

}

