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

package org.openjena.atlas.logging;

import java.io.ByteArrayInputStream ;
import java.io.File ;
import java.io.FileInputStream ;
import java.io.InputStream ;

import org.apache.log4j.PropertyConfigurator ;
import org.apache.log4j.xml.DOMConfigurator ;
import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

// Simple wrapper for convenient, non-time critical logging.
public class Log
{
    private Log() {}
    
    // -- Delayed argument formatting.
    static public void info(Logger log, String fmt, Object...args)
    {
        if ( log.isInfoEnabled() )
        {
            String x = String.format(fmt, args) ;
            log.info(x) ;
        }
    }
    
    static public void debug(Logger log, String fmt, Object...args)
    {
        if ( log.isDebugEnabled() )
        {
            String x = String.format(fmt, args) ;
            log.debug(x) ;
        }
    }

    static public void trace(Logger log, String fmt, Object...args)
    {
        if ( log.isTraceEnabled() )
        {
            String x = String.format(fmt, args) ;
            log.trace(x) ;
        }
    }
    
    static public void warn(Logger log, String fmt, Object...args)
    {
        if ( log.isWarnEnabled() )
        {
            String x = String.format(fmt, args) ;
            log.warn(x) ;
        }
    }
    
    static public void error(Logger log, String fmt, Object...args)
    {
        if ( log.isErrorEnabled() )
        {
            String x = String.format(fmt, args) ;
            log.error(x) ;
        }
    }
    
    // ----
    
    static public void info(String caller, String msg)
    {
        log(caller).info(msg) ;
    }
    
    static public void info(Object caller, String msg)
    {
        log(caller.getClass()).info(msg) ;
    }
    
    static public void info(Class<?> cls, String msg)
    {
        log(cls).info(msg) ;
    }
    
    static public void info(Object caller, String msg, Throwable th)
    {
        log(caller.getClass()).info(msg, th) ;
    }
    
    static public void info(Class<?> cls, String msg, Throwable th)
    {
        log(cls).info(msg, th) ;
    }
    
    static public void debug(String caller, String msg)
    {
        log(caller).info(msg) ;
    }
    
    static public void debug(Object caller, String msg)
    {
        log(caller.getClass()).info(msg) ;
    }
    
    static public void debug(Class<?> cls, String msg)
    {
        log(cls).info(msg) ;
    }
    
    static public void debug(Object caller, String msg, Throwable th)
    {
        log(caller.getClass()).info(msg, th) ;
    }
    
    static public void debug(Class<?> cls, String msg, Throwable th)
    {
        log(cls).info(msg, th) ;
    }
    
    static public void warn(String caller, String msg)
    {
        log(caller).warn(msg) ;
    }
    
    static public void warn(Object caller, String msg)
    {
        warn(caller.getClass(), msg) ;
    }

    static public void warn(Class<?> cls, String msg)
    {
        log(cls).warn(msg) ;
    }

    static public void warn(Object caller, String msg, Throwable th)
    {
        warn(caller.getClass(), msg, th) ;
    }

    static public void warn(Class<?> cls, String msg, Throwable th)
    {
        log(cls).warn(msg, th) ;
    }

    static public void fatal(Object caller, String msg)
    {
        fatal(caller.getClass(), msg) ;
    }

    static public void fatal(Class<?> cls, String msg)
    {
        log(cls).error(msg) ;
    }

    static public void fatal(Object caller, String msg, Throwable th)
    {
        fatal(caller.getClass(), msg, th) ;
    }

    static public void fatal(Class<?> cls, String msg, Throwable th)
    {
        log(cls).error(msg, th) ;
    }

    static public void fatal(String caller, String msg)
    {
        log(caller).error(msg) ;
    }
    
    static public Logger log(Class<?> cls)
    {
        return LoggerFactory.getLogger(cls) ;
    }
    
    static public Logger log(String loggerName)
    {
        return LoggerFactory.getLogger(loggerName) ;
    }
    
    /** Turn on a logger (all levels). 
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void enable(String logger)
    {
        enable(logger, "all") ;
    }
    
    static public void enable(String logger, String level)
    {
        org.apache.log4j.Level level1 = org.apache.log4j.Level.ALL ;
        java.util.logging.Level level2 = java.util.logging.Level.ALL ;
        if ( level.equalsIgnoreCase("info"))
        {
            level1 = org.apache.log4j.Level.INFO ;
            level2 = java.util.logging.Level.INFO ;
        }

        if ( level.equalsIgnoreCase("debug"))
        {
            level1 = org.apache.log4j.Level.DEBUG ;
            level2 = java.util.logging.Level.FINE ;
        }
        
        if ( level.equalsIgnoreCase("warn"))
        {
            level1 = org.apache.log4j.Level.WARN ;
            level2 = java.util.logging.Level.WARNING ;
        }
        logLevel(logger, level1, level2) ;
    }
    
    static public void logLevel(String logger, org.apache.log4j.Level level1, java.util.logging.Level level2 )
    {
        if ( level1 != null )
            org.apache.log4j.LogManager.getLogger(logger).setLevel(level1) ;
        if ( level2 != null )
            java.util.logging.Logger.getLogger(logger).setLevel(level2) ;
    }
    
    /** Turn on a logger (all levels). 
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void enable(Class<?> logger)
    {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.ALL) ;
        java.util.logging.Logger.getLogger(logger.getName()).setLevel(java.util.logging.Level.ALL) ;
    }
    
    /** Turn on a logger (all levels). 
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void disable(String logger)
    {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.OFF) ;
        java.util.logging.Logger.getLogger(logger).setLevel(java.util.logging.Level.OFF) ;
    }
    /** Turn on a logger (all levels). 
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void disable(Class<?> logger)
    {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.OFF) ;
        java.util.logging.Logger.getLogger(logger.getName()).setLevel(java.util.logging.Level.OFF) ;
    }

    /** Set to warning level. 
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void setWarn(String logger)
    {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.WARN) ;
        java.util.logging.Logger.getLogger(logger).setLevel(java.util.logging.Level.WARNING) ;
    }
    
    /** Set to warning level. 
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void setWarn(Class<?> logger)
    {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.WARN) ;
        java.util.logging.Logger.getLogger(logger.getName()).setLevel(java.util.logging.Level.WARNING) ;
    }
    
    /**  Set to infomation level.
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void setInfo(String logger)
    {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.INFO) ;
        java.util.logging.Logger.getLogger(logger).setLevel(java.util.logging.Level.INFO) ;
    }

    /**  Set to infomation level.
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void setInfo(Class<?> logger)
    {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.INFO) ;
        java.util.logging.Logger.getLogger(logger.getName()).setLevel(java.util.logging.Level.INFO) ;
    }
    
    /** Set log4j - try to find a file "log4j.properties" if nothing already set.  Return true if we think Log4J is not initialized. */
    public static boolean setLog4j()
    {
        if ( System.getProperty("log4j.configuration") == null )
        {
            String fn = "log4j.properties" ;
            File f = new File(fn) ;
            if ( f.exists() ) 
                System.setProperty("log4j.configuration", "file:"+fn) ;
        }
        
        return (System.getProperty("log4j.configuration") != null ) ;
    }
    
    /** Set log4j properties (XML or properties file) */
    public static void setLog4j(String filename)
    {
        if ( filename.toLowerCase().endsWith(".xml")) 
            DOMConfigurator.configure(filename) ;
        else
            PropertyConfigurator.configure(filename) ;
    }
    
    //---- java.util.logging - because that's always present.
    static String defaultProperties = 
        StrUtils.strjoinNL(
                           // Handlers - output
                           // All (comma separated)
                           //"handlers=java.util.logging.ConsoleHandler,atlas.logging.java.ConsoleHandlerStdout",

                           // Atlas.
                           //"handlers=atlas.logging.java.ConsoleHandlerStdout" ,

                           // Provided by the JRE
                           "handlers=java.util.logging.ConsoleHandler" ,

                           // Formatting and levels
                           //"atlas.logging.java.ConsoleHandlerStdout.level=ALL",
                           //"atlas.logging.java.ConsoleHandlerStdout.formatter=atlas.logging.java.TextFormatter",

                           "java.util.logging.ConsoleHandler.level=INFO"
                           //, "java.util.logging.ConsoleHandler.formatter=atlas.logging.java.TextFormatter"
        ) ;   
        
    
    public static void setJavaLogging()
    {
        setJavaLogging("logging.properties") ;
    }
    
    
    public static void setJavaLogging(String file)
    {
        try
        {
            InputStream details = new FileInputStream(file) ;
            java.util.logging.LogManager.getLogManager().readConfiguration(details) ;
        } catch (Exception ex) { throw new AtlasException(ex) ; } 
    }
    
    static void setJavaLoggingDft()
    {
        try
        {
            InputStream details = new ByteArrayInputStream(defaultProperties.getBytes("UTF-8")) ;
            java.util.logging.LogManager.getLogManager().readConfiguration(details) ;
            
        } catch (Exception ex) { throw new AtlasException(ex) ; } 
    }
    
    /** Set for command line tools so that (e.g. log4j.properties) isn't assumed
     * Avoids need for baked in log4j.properties which can be problematic
     * if there are multiple ones. 
     */
    private static void setLoggingForCommandLine()
    {
            // See CmdTDB.setLogging.
    }
}
