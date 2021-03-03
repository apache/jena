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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

/**
 * Separate initialization for FUSEKI_HOME and FUSEKI_BASE so that
 * FusekiLogging can use these values.
 * This code must not touch Jena.
 *
 * @see FusekiWebapp
 */
public class FusekiEnv {
    // Initialization logging happens via stdout/stderr directly.
    // Fuseki logging is not initialized to avoid going in circles.

    private static final boolean LogInit         = false;

    /** Unused */
    // public static final String DFT_FUSEKI_HOME = isWindows
    //        ? /*What's correct here?*/ "/usr/share/fuseki"
    //        : "/usr/share/fuseki";
    static final boolean isWindows = SystemUtils.IS_OS_WINDOWS;
    static final String  DFT_FUSEKI_BASE = isWindows ? /* What's correct here? */"/etc/fuseki" : "/etc/fuseki";

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
        EMBEDDED    (null, null) ,
        STANDALONE  (".", "run") ,
        TEST        ("src/main/webapp", "target/run") ,
        UNSET       (null, null);

        final String dftFusekiHome;
        final String dftFusekiBase;

        INIT(String home, String base) {
            this.dftFusekiHome = home;
            this.dftFusekiBase = base;
        }
    }

    public static INIT mode = INIT.UNSET;

    /** Root of the Fuseki installation for fixed files.
     *  This may be null (e.g. running inside a web application container) */
    public static Path FUSEKI_HOME = null;

    /** Root of the varying files in this deployment. Often $FUSEKI_HOME/run.
     * This is not null - it may be /etc/fuseki, which must be writable.
     */
    public static Path FUSEKI_BASE = null;

    public static final String   ENV_runArea     = "run";
    private static boolean       initialized     = false;

    /** Initialize the server : standalone and WAR versions : not embedded */
    public static synchronized void setEnvironment() {
        if ( initialized )
            return;
        resetEnvironment();
    }

    /** Reset environment - use with care and before server start up */
    public static synchronized void resetEnvironment() {
        initialized = true;
        logInit("FusekiEnv:Start: ENV_FUSEKI_HOME = %s : ENV_FUSEKI_BASE = %s : MODE = %s", FUSEKI_HOME, FUSEKI_BASE, mode);

        if ( mode == null || mode == INIT.UNSET )
            mode = INIT.WAR;

        if ( FUSEKI_HOME == null ) {
            // Make absolute
            String x1 = getenv("FUSEKI_HOME");
            if ( x1 == null )
                x1 = mode.dftFusekiHome;
            if ( x1 != null )
                FUSEKI_HOME = Paths.get(x1);
        }

        if ( FUSEKI_BASE == null ) {
            String x2 = getenv("FUSEKI_BASE");
            if ( x2 == null )
                x2 = mode.dftFusekiBase;
            if ( x2 != null )
                FUSEKI_BASE = Paths.get(x2);
            else {
                if ( FUSEKI_HOME != null )
                    FUSEKI_BASE = FUSEKI_HOME.resolve(ENV_runArea);
                else {
                    // This is bad - there should have been a default by now.
                    logInitError("Can't find a setting for FUSEKI_BASE - guessing wildy");
                    // Neither FUSEKI_HOME nor FUSEKI_BASE set.
                    FUSEKI_BASE = Paths.get(DFT_FUSEKI_BASE);
                }
            }
        }

        if ( FUSEKI_HOME != null )
            FUSEKI_HOME = FUSEKI_HOME.toAbsolutePath();

        FUSEKI_BASE = FUSEKI_BASE.toAbsolutePath();

        logInit("FusekiEnv:Finish: ENV_FUSEKI_HOME = %s : ENV_FUSEKI_BASE = %s", FUSEKI_HOME, FUSEKI_BASE);
    }

    private static void logInit(String fmt, Object ... args) {
        if ( LogInit ) {
            System.out.printf(fmt, args);
            System.out.println();
        }
    }

    private static void logInitError(String fmt, Object ... args) {
        System.err.printf(fmt, args);
        System.err.println();
    }

    /** Get environment variable value (maybe in system properties) */
    public static String getenv(String name) {
        String x = System.getenv(name);
        if ( x == null )
            x = System.getProperty(name);
        return x;
    }
}

