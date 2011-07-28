/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */