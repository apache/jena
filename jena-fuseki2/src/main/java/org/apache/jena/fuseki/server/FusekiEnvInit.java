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

import java.nio.file.Path ;
import java.nio.file.Paths ;

/** 
 * Separate initialization for FUSEKI_HOME and FUSEKI_BASE so that 
 * Fusekilogging can use these values.
 * This code must not touch Jena.  
 * 
 * @See FusekiServer 
 */ 
public class FusekiEnvInit {
    /** Root of the Fuseki installation for fixed files. 
     *  This may be null (e.g. running inside a web application container) */ 
    public static Path ENV_FUSEKI_HOME = null ;
    
    /** Root of the varying files in this deployment. Often $FUSEKI_HOME/run.
     * This is not null - it may be /etc/fuseki, which must be writable.
     */ 
    public static Path ENV_FUSEKI_BASE = null ;
    
    static final boolean isWindows = determineIfWindows() ;
    
    // Copied from SystemTDB to avoid dependency.
    // This code must not touch Jena.  
    private static boolean determineIfWindows() {
        String s = System.getProperty("os.name") ;
        if ( s == null )
            return false ;
        return s.startsWith("Windows ") ;
    }
 
    /** Unused */
    // public static final String DFT_FUSEKI_HOME = isWindows 
    //        ? /*What's correct here?*/ "/usr/share/fuseki"
    //        : "/usr/share/fuseki" ;
    static final String  DFT_FUSEKI_BASE = isWindows ? /* What's correct here? */"/etc/fuseki" : "/etc/fuseki" ;

    public static final String   ENV_runArea     = "run" ;

    private static boolean       initialized     = false ;
    private static final boolean LogInit         = false ;
    
    public static synchronized void setEnvironment() {
        if ( initialized )
            return ;
        initialized = true ;
        logInit("FusekiInitEnv") ;
        logInit("Start: ENV_FUSEKI_HOME = %s : ENV_FUSEKI_BASE = %s", ENV_FUSEKI_HOME, ENV_FUSEKI_BASE) ;
        
        if ( ENV_FUSEKI_HOME == null ) {
            // Make absolute
            String x1 = getenv("FUSEKI_HOME") ;
            if ( x1 != null )
                ENV_FUSEKI_HOME = Paths.get(x1) ;
        }

        if ( ENV_FUSEKI_BASE == null ) {
            String x2 = getenv("FUSEKI_BASE") ;
            if ( x2 != null )
                ENV_FUSEKI_BASE = Paths.get(x2) ;
            else {
                if ( ENV_FUSEKI_HOME != null )
                    ENV_FUSEKI_BASE = ENV_FUSEKI_HOME.resolve(ENV_runArea) ;
                else
                    // Neither FUSEKI_HOME nor FUSEKI_BASE set.
                    ENV_FUSEKI_BASE = Paths.get(DFT_FUSEKI_BASE) ;
            }
        }

        if ( ENV_FUSEKI_HOME != null )
            ENV_FUSEKI_HOME = ENV_FUSEKI_HOME.toAbsolutePath() ;

        ENV_FUSEKI_BASE = ENV_FUSEKI_BASE.toAbsolutePath() ;

        logInit("Finish: ENV_FUSEKI_HOME = %s : ENV_FUSEKI_BASE = %s", ENV_FUSEKI_HOME, ENV_FUSEKI_BASE) ;
    }
    
    private static void logInit(String fmt, Object ... args) {
        if ( LogInit ) {
            System.out.printf(fmt, args) ; 
            System.out.println() ;
        }
    }
    
    /** Get environment variable value (maybe in system properties) */
    public static String getenv(String name) {
        String x = System.getenv(name) ;
        if ( x == null )
            x = System.getProperty(name) ;
        return x ;
    }

}

