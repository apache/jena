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

package org.apache.jena.atlas.logging;

import java.io.* ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.Properties ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.log4j.PropertyConfigurator ;
import org.apache.log4j.xml.DOMConfigurator ;
import org.slf4j.Logger ;

/** Setup and control of logging - needs access to log4j binaries */ 
public class LogCtl {

    static public void set(Logger logger, String level) {
        setLevel(logger.getName(), level) ;
    }

    /** @deprecated Use {@link #setLevel(String, String)} */
    @Deprecated
    static public void set(Class<? > logger, String level) {
        setLevel(logger.getName(), level) ;
    }

    /** @deprecated Use {@link #setLevel(String, String)} */
    @Deprecated
    static public void set(String logger, String level) {
        setLevel(logger,level) ;
    }

    static public void setLevel(Class<? > logger, String level) {
        setLevel(logger.getName(), level) ;
    }
    
    static public void setLevel(String logger, String level) {
        setLevelLog4j(logger,level) ;
        setLevelJUL(logger,level) ;
    }

    private static void setLevelJUL(String logger, String levelName) {
        java.util.logging.Level level = java.util.logging.Level.ALL ;
        if ( levelName.equalsIgnoreCase("info") )
            level = java.util.logging.Level.INFO ;
        else if ( levelName.equalsIgnoreCase("debug") )
            level = java.util.logging.Level.FINE ;
        else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") ) 
            level = java.util.logging.Level.WARNING ;
        else if ( levelName.equalsIgnoreCase("error") )
            level = java.util.logging.Level.SEVERE ;
        else if ( levelName.equalsIgnoreCase("OFF") )
            level = java.util.logging.Level.OFF ;
        if ( level != null )
            java.util.logging.Logger.getLogger(logger).setLevel(level) ;
    }

    private static void setLevelLog4j(String logger, String levelName) {
        try {
            org.apache.log4j.Level level = org.apache.log4j.Level.ALL ;
            if ( levelName.equalsIgnoreCase("info") )
                level = org.apache.log4j.Level.INFO ;
            else if ( levelName.equalsIgnoreCase("debug") )
                level = org.apache.log4j.Level.DEBUG ;
            else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
                level = org.apache.log4j.Level.WARN ;
            else if ( levelName.equalsIgnoreCase("error") )
                level = org.apache.log4j.Level.ERROR ;
            else if ( levelName.equalsIgnoreCase("OFF") )
                level = org.apache.log4j.Level.OFF ;
            if ( level != null )   
                org.apache.log4j.LogManager.getLogger(logger).setLevel(level) ;            
        } catch (NoClassDefFoundError ex) {
            // For when it is not on the class path 
        }
    }

    /** @deprecated Do not use - to be removed - use {@link #setLevel(String, String)} */
    @Deprecated
    static public void logLevel(String logger, org.apache.log4j.Level level1, java.util.logging.Level level2) {
        if ( level1 != null )
            org.apache.log4j.LogManager.getLogger(logger).setLevel(level1) ;
        if ( level2 != null )
            java.util.logging.Logger.getLogger(logger).setLevel(level2) ;
    }

    /**
     * Turn on a logger (all levels). Works for Log4j and Java logging as the
     * logging provider to Apache common logging or slf4j.
     */
    
    static public void enable(Logger logger) {
        enable(logger.getName()) ;
    }

    static public void enable(String logger) {
        setLevel(logger, "all") ;
    }

    /**
     * Turn on a logger (all levels). Works for Log4j and Java logging as the
     * logging provider to Apache common logging or slf4j.
     */
    static public void enable(Class<? > logger) {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.ALL) ;
        java.util.logging.Logger.getLogger(logger.getName()).setLevel(java.util.logging.Level.ALL) ;
    }

    /**
     * Turn on a logger (all levels). Works for Log4j and Java logging as the
     * logging provider to Apache common logging or slf4j.
     */
    static public void disable(Logger logger) {
        setLevel(logger.getName(), "OFF") ;
    }

    /**
     * Turn on a logger (all levels). Works for Log4j and Java logging as the
     * logging provider to Apache common logging or slf4j.
     */
    static public void disable(String logger) {
        setLevel(logger, "OFF") ;
    }

    /**
     * Turn on a logger (all levels). Works for Log4j and Java logging as the
     * logging provider to Apache common logging or slf4j.
     */
    static public void disable(Class<? > logger) {
        setLevel(logger.getName(), "OFF") ;
    }

    /**
     * Set to info level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setInfo(String logger) {
        setLevel(logger, "info") ;
    }

    /**
     * Set to info level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setInfo(Class<? > logger) {
        setLevel(logger.getName(), "info") ;
    }

    /**
     * Set to warning level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setWarn(String logger) {
        setLevel(logger, "warn") ;
    }

    /**
     * Set to warning level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setWarn(Class<? > logger) {
        setLevel(logger.getName(), "warn") ;
    }

    /**
     * Set to error level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setError(String logger) {
        setLevel(logger, "error") ;
    }

    /**
     * Set to error level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setError(Class<? > logger) {
        setLevel(logger.getName(), "error") ;
    }

    private static String log4Jsetup = StrUtils.strjoinNL
        ( "## Command default log4j setup"
         
          ,"## Plain output with level, to stderr"
          ,"log4j.appender.jena.plainlevel=org.apache.log4j.ConsoleAppender"
          ,"log4j.appender.jena.plainlevel.target=System.err"
          ,"log4j.appender.jena.plainlevel.layout=org.apache.log4j.PatternLayout"
          ,"log4j.appender.jena.plainlevel.layout.ConversionPattern=%-5p %m%n"

//          , "## Plain output to stdout, unadorned output format"
//          ,"log4j.appender.jena.plain=org.apache.log4j.ConsoleAppender"
//          ,"log4j.appender.jena.plain.target=System.out"
//          ,"log4j.appender.jena.plain.layout=org.apache.log4j.PatternLayout"
//          ,"log4j.appender.jena.plain.layout.ConversionPattern=%m%n"

          ,"## Everything"
          ,"log4j.rootLogger=INFO, jena.plainlevel"
          ,"log4j.logger.com.hp.hpl.jena=WARN"
          ,"log4j.logger.org.apache.jena=WARN"
          ,"log4j.logger.org.apache.jena.tdb.loader=INFO"

          , "## Parser output"
          , "log4j.additivity.org.apache.jena.riot=false"
          , "log4j.logger.org.apache.jena.riot=INFO, jena.plainlevel"
         ) ;
    /**
     * Set logging
     * <ol>
     * <li>Check for -Dlog4j.configuration.</li>
     * <li>Looks for log4j.properties file in current directory.</li>
     * </ol>
     * Return true if we think Log4J is not initialized.
     */
    
    public static void setLog4j() {
        if ( System.getProperty("log4j.configuration") == null ) {
            String fn = "log4j.properties" ;
            File f = new File(fn) ;
            if ( f.exists() )
                System.setProperty("log4j.configuration", "file:" + fn) ;
        }
    }

    /** Set log4j properties (XML or properties file) */
    public static void setLog4j(String filename) {
        if ( filename.toLowerCase().endsWith(".xml") )
            DOMConfigurator.configure(filename) ;
        else
            PropertyConfigurator.configure(filename) ;
    }

    /**
     * Set logging, suitable for a command line application.
     * If "log4j.configuration" not set, then use the built-in default, 
     * else just leave to log4j startup.
     */
    public static void setCmdLogging() {
        setCmdLogging(log4Jsetup) ;
    }

    /**
     * Set logging, suitable for a command line application.
     * If "log4j.configuration" not set, then use the provided default 
     * (log4j properties syntax) else just leave to log4j startup.
     */
    public static void setCmdLogging(String defaultConfig) {
        if (System.getProperty("log4j.configuration") == null )
            resetLogging(defaultConfig) ;
    }

    /**
     * Reset logging (log4j).
     */
    public static void resetLogging(String config) {
        Properties p = new Properties() ;
        InputStream in = new ByteArrayInputStream(StrUtils.asUTF8bytes(config)) ;
        try {
            p.load(in) ;
        } catch (IOException ex) {}
        PropertyConfigurator.configure(p) ;
        System.setProperty("log4j.configuration", "set") ;
    }

    // ---- java.util.logging - because that's always present.
    // Need:  org.slf4j:slf4j-jdk14
    private static String defaultProperties = StrUtils.strjoinNL
        ("handlers=org.apache.jena.atlas.logging.java.ConsoleHandlerStream"
        // These are the defaults.
        //,"org.apache.jena.atlas.logging.java.ConsoleHandlerStream.level=INFO"
        //,"org.apache.jena.atlas.logging.java.ConsoleHandlerStream.formatter=org.apache.jena.atlas.logging.java.TextFormatter"
        //,"org.apache.jena.atlas.logging.java.TextFormatter.format=%5$tT %3$-5s %2$-20s :: %6$s"
        ) ;
    // File or java resource name default.
    private static String JUL_LOGGING = "logging.properties";
    
    // JUL will close existing logger if logging is reset.
    // This includes StreamHandler logging to stdout.  Stdout is closed.
    // This property controls setJavaLogging() acting multiple times.
    private static String JUL_PROPERTY = "java.util.logging.configuration";

    /** Setup java.util.logging if it has not been set before; otherwise do nothing. */
    public static void setJavaLogging() {
        if ( System.getProperty(JUL_PROPERTY) != null )
            return;
        resetJavaLogging();
    }
    
    /** Reset java.util.logging - this overrided the previous configuration, if any. */  
    public static void resetJavaLogging() {
        Path p = Paths.get(JUL_LOGGING) ;
        if ( Files.exists(p) ) {
            setJavaLogging(JUL_LOGGING) ;
            return ;
        }
        if ( setJavaLoggingClasspath(JUL_LOGGING) )
            return ;
        setJavaLoggingDft();
    }

    private static void readConfiguration(InputStream details) throws Exception {
        System.setProperty(JUL_PROPERTY, "set");
        java.util.logging.LogManager.getLogManager().readConfiguration(details) ;
    }
    
    private static boolean setJavaLoggingClasspath(String resourceName) {
        // Not "LogCtl.class.getResourceAsStream(resourceName)" which monkeys around with the resourceName.
        InputStream in = LogCtl.class.getClassLoader().getResourceAsStream(resourceName);
        if ( in != null ) {
            try {
                readConfiguration(in) ;
                return true; 
            } catch (Exception ex) {
                throw new AtlasException(ex) ;
            }
        }
        return false;
    }

    public static void setJavaLogging(String file) {
        try {
            InputStream details = new FileInputStream(file) ;
            details = new BufferedInputStream(details) ;
            readConfiguration(details) ;
        } catch (Exception ex) {
            throw new AtlasException(ex) ;
        }
    }

    public static void setJavaLoggingDft() {
        try {
            InputStream details = new ByteArrayInputStream(defaultProperties.getBytes("UTF-8")) ;
            readConfiguration(details) ;
        } catch (Exception ex) {
            throw new AtlasException(ex) ;
        }
    }
}
