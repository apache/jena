/*
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

package org.apache.jena.atlas.logging;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.lib.StrUtils;
import org.slf4j.bridge.SLF4JBridgeHandler;

/** java.util.logging specific code. */
public class LogCtlJUL {
    // File or java resource name default.
    public static String JUL_LOGGING       = "logging.properties";

    // JUL will close existing logger if logging is reset.
    // This includes StreamHandler logging to stdout. Stdout is closed.
    // This property controls setJavaLogging() acting multiple times.
    private static String JUL_PROPERTY      = "java.util.logging.configuration";

    // @formatter:on
    static String defaultProperties = StrUtils.strjoinNL
        ("handlers=org.apache.jena.atlas.logging.java.ConsoleHandlerStream"
        // These are the defaults in o.a.jena.logging.java
        // ,"org.apache.jena.atlas.logging.java.ConsoleHandlerStream.level=INFO"
        // ,"org.apache.jena.atlas.logging.java.ConsoleHandlerStream.formatter=org.apache.jena.atlas.logging.java.TextFormatter"
        //,"org.apache.jena.atlas.logging.java.TextFormatter.format=%5$tT %3$-5s %2$-20s :: %6$s"
        );
    // @formatter:off

    private LogCtlJUL() {}

    /**
     * Reset java.util.logging - this overrides the previous configuration, if any.
     */
    static void resetJavaLogging() {
        Path p = Path.of(JUL_LOGGING);
        if ( Files.exists(p) ) {
            setJavaLogging(JUL_LOGGING);
            return;
        }
        if ( setJavaLoggingClasspath(JUL_LOGGING) )
            return;
        setJavaLoggingDft();
    }

    static void readJavaLoggingConfiguration(InputStream details) throws Exception {
        System.setProperty(JUL_PROPERTY, "set");
        java.util.logging.LogManager.getLogManager().readConfiguration(details);
    }

    private static boolean setJavaLoggingClasspath(String resourceName) {
        // Not "LogCtl.class.getResourceAsStream(resourceName)" which monkeys around
        // with the resourceName.
        InputStream in = LogCtl.class.getClassLoader().getResourceAsStream(resourceName);
        if ( in != null ) {
            try {
                readJavaLoggingConfiguration(in);
                return true;
            } catch (Exception ex) {
                throw new AtlasException(ex);
            }
        }
        return false;
    }

    public static void setJavaLogging(String file) {
        try {
            InputStream details = new FileInputStream(file);
            details = new BufferedInputStream(details);
            readJavaLoggingConfiguration(details);
        } catch (Exception ex) {
            throw new AtlasException(ex);
        }
    }

    public static void setJavaLoggingDft() {
        try {
            InputStream details = new ByteArrayInputStream(defaultProperties.getBytes("UTF-8"));
            readJavaLoggingConfiguration(details);
        } catch (Exception ex) {
            throw new AtlasException(ex);
        }
    }

    /**
     * Suppress messages on a specific logger for the duration of a {@code Runnable}.
     * (jul-to-slf4j does not catch loggers created before it is active).
     */
    public static void noOutputJUL(Runnable action, String loggerName) {
        try {
            Logger logger = Logger.getLogger(loggerName);
            Level level = logger.getLevel();
            logger.setLevel(Level.OFF);
            action.run();
            logger.setLevel(level);
        } catch (Throwable th) {}
    }

    /**
     * Route JUL to SLF4J.
     * Do not include org.slf4j:slf4j-jdk14.
     */
    public static void routeJULtoSLF4J() {
        try {
            if (!SLF4JBridgeHandler.isInstalled()) {
                SLF4JBridgeHandler.removeHandlersForRootLogger();
                SLF4JBridgeHandler.install();
            }
        }
        catch (Throwable th) {
        }
    }
}
