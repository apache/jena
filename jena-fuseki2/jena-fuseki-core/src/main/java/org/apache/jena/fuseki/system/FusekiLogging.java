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

package org.apache.jena.fuseki.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.logging.LogCtlLog4j2;
import org.apache.jena.fuseki.Fuseki;

/**
 * FusekiLogging.
 * <p>
 * This applies to Fuseki run from the command line and embedded.
 * <p>
 * This does not apply to Fuseki running in Tomcat where it uses the
 * servlet 3.0 mechanism described in
 * <a href="https://logging.apache.org/log4j/2.x/manual/webapp.html">Log4j2 manual
 * (webapp)</a>. See {@code FusekiServerEnvironmentInit}.
 */
public class FusekiLogging
{
    // This class must not have static constants, or otherwise not "Fuseki.*"
    // or any class else where that might kick off logging.  Otherwise, the
    // setLogging is pointless (it's already set).

    // Set logging.
    // 1/ Use system property log4j2.configurationFile if defined.
    // 2/ Use file:log4j2.properties if exists
    // 3/ Use log4j2.properties on the classpath.
    // 4/ Use org/apache/jena/fuseki/log4j2.properties on the classpath.
    // 5/ Use built in string

    /**
     * Places for the log4j properties file at (3).
     * This is not the standard, fixed classpath names used by log4j.
     * log4j2.properties, log4j2.yaml, log4j2.json, log4j2.xml
     */
    private static final String[] resourcesForLog4jProperties = {
        "log4j2.properties"
    };

    private static final boolean LogLogging =
            System.getenv("FUSEKI_LOGLOGGING") != null ||
            System.getProperty("fuseki.loglogging") != null;

    private static boolean loggingInitialized   = false;

    /**
     * Mark whether logging is considered "initialized". Some external factor (e.g.
     * log4j2 webapp context-param "log4jConfiguration") may mean logging will be
     * initialized some other way.
     * <p>
     * Call this with argument false if the code wants to re-initialize the logging
     * otherwise calls of {@code setLogging} will be no-ops.
     */
    public static synchronized void markInitialized(boolean isInitialized) {
        logLogging("markInitialized("+isInitialized+")");
        loggingInitialized = isInitialized;
    }

    /** Set up logging. */
    public static synchronized void setLogging() {
        setLogging(null);
    }

    public static final String log4j2_configurationFile = LogCtl.log4j2ConfigFileProperty;
    private static final String log4j2_configurationFileLegacy = LogCtl.log4j2ConfigFilePropertyLegacy;
    // Only used by the webapp.
    public static final String log4j2_web_configuration = "log4jConfiguration";

    public static synchronized boolean hasInitialized() {
        return loggingInitialized;
    }

    /**
     * Set up logging. Allow an extra location. This may be null.
     *
     * @param extraDir
     */
    public static synchronized void setLogging(Path extraDir) {
        // Cope with repeated calls so code can call this to ensure
        // logging setup has happened.
        if ( loggingInitialized )
            return;
        loggingInitialized = true;

        logLogging("Set logging");

        // Is there a log4j setup provided?
        if ( checkSystemProperties(log4j2_configurationFile) ||
             checkSystemProperties(log4j2_configurationFileLegacy) ||
             System.getenv("LOG4J_CONFIGURATION_FILE") != null )
        {
            logLogging("External log4j2 setup");
            return ;
        }

        logLogging("Setup");
        // Look for a log4j2.properties file in the current working directory
        // and a place (e.g. FUSEKI_BASE in the webapp/full server) for easy customization.
        String fn1 = "log4j2.properties";
        String fn2 = null;

        if ( extraDir != null )
            fn2 = extraDir.resolve("log4j2.properties").toString();
        if ( attempt(fn1) ) return;
        if ( attempt(fn2) ) return;

        // Try classpath
        for ( String resourceName : resourcesForLog4jProperties ) {
            // In log4j2, classpath initialization is fixed name :
            //   log4j2.properties, log4j2.yaml, log4j2.json, log4j2.xml
            // Instead, we manually load a resource.
            logLogging("Try classpath %s", resourceName);
            URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
//            if ( url != null ) {
//                // Problem - test classes can be on the classpath (development mainly).
//                if ( url.toString().contains("-tests.jar") || url.toString().contains("test-classes") )
//                    url = null;
//            }

            if ( url != null ) {
                try ( InputStream inputStream = url.openStream() ) {
                    String x = IO.readWholeFileAsUTF8(inputStream);
                } catch (IOException ex) { IO.exception(ex); }

                try ( InputStream inputStream = url.openStream() ) {
                    loadConfiguration(inputStream, resourceName);
                } catch (IOException ex) { IO.exception(ex); }
                logLogging("Found via classpath %s", url);
                System.setProperty(log4j2_configurationFile, url.toString());
                return;
            }
        }
        // Use built-in.
        logLogging("Fallback built-in log4j2setup");
        String dftLog4j = log4j2setupFallback();
        resetLogging(dftLog4j);
        // Stop anything attempting to do it again.
        System.setProperty(log4j2_configurationFile, "set");
    }

    private static boolean checkSystemProperties(String... properties) {
        String x = null;
        for ( String propertyName : properties ) {
            x = System.getProperty(propertyName, null);
            if ( x != null ) {
                if ( "set".equals(x) ) {
                    Fuseki.serverLog.warn("Fuseki logging: Unexpected: Log4j2 was setup by some other part of Jena");
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    private static void loadConfiguration(InputStream inputStream, String resourceName) throws IOException {
        LogCtlLog4j2.resetLogging(inputStream, resourceName);
    }

    private static boolean attempt(String fn) {
        if ( fn == null )
            return false;
        try {
            logLogging("Try file:"+fn);
            File f = new File(fn);
            if ( f.exists() ) {
                logLogging("Found via file "+fn);
                try (InputStream input = new FileInputStream(fn) ) {
                    loadConfiguration(input, fn);
                } catch (IOException ex) { IO.exception(ex); }
                System.setProperty(log4j2_configurationFile, "file:" + fn);
                return true;
            }
        }
        catch (Throwable th) {}
        return false;
    }

    private static void logLogging(String fmt, Object ... args) {
        if ( LogLogging ) {
            System.err.print("Fuseki Logging: ");
            System.err.printf(fmt, args);
            System.err.println();
        }
    }

    private static String log4j2setupFallback() {
        // The logging file for Fuseki in Tomcat webapp is in "log4j2.properties" in the webapp root directory.
        // This is used by command line Fuseki (full and main)
        // @formatter:off
        return StrUtils.strjoinNL
            ("## Plain output to stdout"
            , "status = error"
            , "name = FusekiLogging"
//            , ""
//            , "filters = threshold"
//            , "filter.threshold.type = ThresholdFilter"
//            , "filter.threshold.level = ALL"
//            , ""
            , "appender.console.type = Console"
            , "appender.console.name = OUT"
            , "appender.console.target = SYSTEM_OUT"
            , "appender.console.layout.type = PatternLayout"
            , "appender.console.layout.pattern = [%d{yyyy-MM-dd HH:mm:ss}] %-10c{1} %-5p %m%n"
            , ""
            , "rootLogger.level                  = INFO"
            , "rootLogger.appenderRef.stdout.ref = OUT"
            , ""
            , "logger.jena.name  = org.apache.jena"
            , "logger.jena.level = INFO"
            , ""
            , "logger.arq-exec.name  = org.apache.jena.arq.exec"
            , "logger.arq-exec.level = INFO"
            , ""
            , "logger.riot.name  = org.apache.jena.riot"
            , "logger.riot.level = INFO"
            , ""
            , "logger.fuseki.name  = org.apache.jena.fuseki"
            , "logger.fuseki.level = INFO"
            , ""
            , "logger.fuseki-fuseki.name  = org.apache.jena.fuseki.Fuseki"
            , "logger.fuseki-fuseki.level = INFO"
            ,""
            , "logger.fuseki-server.name  = org.apache.jena.fuseki.Server"
            , "logger.fuseki-server.level = INFO"
            ,""
            , "logger.fuseki-config.name  = org.apache.jena.fuseki.Config"
            , "logger.fuseki-config.level = INFO"
            ,""
            , "logger.fuseki-admin.name  = org.apache.jena.fuseki.Admin"
            , "logger.fuseki-admin.level = INFO"
            ,""
            , "logger.jetty.name  = org.eclipse.jetty"
            , "logger.jetty.level = WARN"
            , ""
            , "logger.apache-http.name   = org.apache.http"
            , "logger.apache-http.level  = WARN"
            , "logger.shiro.name = org.apache.shiro"
            , "logger.shiro.level = WARN"
            , ""
            , "# Hide bug in Shiro 1.5.0"
            , "logger.shiro-realm.name = org.apache.shiro.realm.text.IniRealm"
            , "logger.shiro-realm.level = ERROR"
            , ""
            , "# This goes out in NCSA format"
            , "appender.plain.type = Console"
            , "appender.plain.name = PLAIN"
            , "appender.plain.layout.type = PatternLayout"
            , "appender.plain.layout.pattern = %m%n"
            , ""
            , "logger.fuseki-request.name                   = org.apache.jena.fuseki.Request"
            , "logger.fuseki-request.additivity             = false"
            , "logger.fuseki-request.level                  = OFF"
            , "logger.fuseki-request.appenderRef.plain.ref  = PLAIN"
                );
        // @formatter:on
    }

    public static void resetLogging(String configString) {
        LogCtlLog4j2.resetLogging(configString);
    }
}

