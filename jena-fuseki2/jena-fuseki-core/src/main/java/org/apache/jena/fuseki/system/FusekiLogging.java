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

import java.io.File ;
import java.net.URL ;
import java.nio.file.Path;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.riot.SysRIOT ;
import org.apache.log4j.PropertyConfigurator ;
import org.apache.log4j.helpers.Loader ;

public class FusekiLogging
{
    // This class must not have static constants, or otherwise not "Fuseki.*"
    // or any class else where that might kick off logging.  Otherwise, the 
    // setLogging is pointless (it's already set).
    // PlanB - reinitialize logging regardless on first call. 
    
    // Set logging.
    // 1/ Use log4j.configuration if defined.
    // 2/ Use file:log4j.properties if exists
    // 3/ Use log4j.properties on the classpath.
    // 4/ Use built-in org/apache/jena/fuseki/log4j.properties on the classpath.
    // 5/ Use Built in string

    /** Places for the log4j properties file at (3) */ 
    private static final String[] resourcesForLog4jProperties = {
        // Hmm - 
        "log4j.properties",
        "org/apache/jena/fuseki/log4j.properties"
    } ;
    
    private static final boolean LogLogging     = false ;
    private static boolean loggingInitialized   = false ;
    private static boolean allowLoggingReset    = true ;
    
    /**
     * Switch off logging setting. 
     * Used by the embedded server so that the application's
     * logging setup is not overwritten.
     */
    public static synchronized void allowLoggingReset(boolean value) {
        allowLoggingReset = value ;
    }
    
    /** Set up logging - standalone and war packaging */
    public static synchronized void setLogging() {
        setLogging(null);
        
    }
    
    /** Set up logging. Allow an extra location (string directory name without trailing "/"). This may be null 
     * 
     * @param extraDir
     */
    public static synchronized void setLogging(Path extraDir) {
        if ( ! allowLoggingReset )
            return ;
        if ( loggingInitialized )
            return ;
        loggingInitialized = true ;
        
        logLogging("Fuseki logging") ;
        // No loggers have been created but configuration may have been set up.
        String x = System.getProperty("log4j.configuration", null) ;
        logLogging("log4j.configuration = %s", x) ;

        if ( x != null ) { 
            // log4j will initialize in the usual way. This includes a value of
            // "set", which indicates that logging was set before by some other Jena code.
            if ( x.equals("set") )
                Fuseki.serverLog.warn("Fuseki logging: Unexpected: Log4j was setup by some other part of Jena") ;
            return ;
        }
        logLogging("Fuseki logging - setup") ;
        // Look for a log4j.properties in the current working directory
        // and a plane (e.g. FUSEKI_BASE in the webapp/full server) for easy customization.
        String fn1 = "log4j.properties" ;
        String fn2 = null ;
        
        if ( extraDir != null ) 
            fn2 = extraDir.resolve("log4j.properties").toString() ;
        if ( attempt(fn1) ) return ; 
        if ( attempt(fn2) ) return ;
        
        // Try classpath
        for ( String resourceName : resourcesForLog4jProperties ) {
            // The log4j general initialization is done in a class static
            // in LogManager so it can't be called again in any sensible manner.
            // Instead, we include the same basic mechanism ...
            logLogging("Fuseki logging - classpath %s", resourceName) ;
            URL url = Loader.getResource(resourceName) ;
            if ( url != null ) {
                // Problem - test classes can be on the classpath (development mainly).
                if ( url.toString().contains("-tests.jar") || url.toString().contains("test-classes") )
                    url = null ;
            }
            
            if ( url != null ) {
                PropertyConfigurator.configure(url) ;
                logLogging("Fuseki logging - found via classpath %s", url) ;
                System.setProperty("log4j.configuration", url.toString()) ;
                return ;
            }
        }
        // Use builtin.
        logLogging("Fuseki logging - Fallback log4j.properties string") ;
        String dftLog4j = log4JsetupFallback() ;
        LogCtl.resetLogging(dftLog4j);
        // Stop anything attempting to do it again.
        System.setProperty("log4j.configuration", "set") ;
    }

    private static boolean attempt(String fn) {
        try {
            File f = new File(fn) ;
            if ( f.exists() ) {
                logLogging("Fuseki logging - found file:log4j.properties") ;
                PropertyConfigurator.configure(fn) ;
                System.setProperty("log4j.configuration", "file:" + fn) ;
                return true ;
            }
        }
        catch (Throwable th) {}
        return false ;
    }

    private static void logLogging(String fmt, Object ... args) {
        if ( LogLogging ) {
            System.out.printf(fmt, args) ; 
            System.out.println() ;
        }
    }

    private static String log4JsetupFallback() {
        return StrUtils.strjoinNL
            // Preferred: classes/log4j.properties, from src/main/resources/log4j.properties
            // Keep these in-step.  Different usages cause different logging initalizations;
            // if the jar is rebundled, it may loose the associated log4.properties file.
            ("## Plain output to stdout",
             "log4j.appender.jena.plainstdout=org.apache.log4j.ConsoleAppender",
             "log4j.appender.jena.plainstdout.target=System.out",
             "log4j.appender.jena.plainstdout.layout=org.apache.log4j.PatternLayout",
             "log4j.appender.jena.plainstdout.layout.ConversionPattern=[%d{yyyy-MM-dd HH:mm:ss}] %-10c{1} %-5p %m%n",
             //"log4j.appender.jena.plainstdout.layout.ConversionPattern=%d{HH:mm:ss} %-10c{1} %-5p %m%n",

             "# Unadorned, for the requests log.",
             "log4j.appender.fuseki.plain=org.apache.log4j.ConsoleAppender",
             "log4j.appender.fuseki.plain.target=System.out",
             "log4j.appender.fuseki.plain.layout=org.apache.log4j.PatternLayout",
             "log4j.appender.fuseki.plain.layout.ConversionPattern=%m%n",
             
             "## Most things", 
             "log4j.rootLogger=INFO, jena.plainstdout",
             "log4j.logger.com.hp.hpl.jena=WARN",
             "log4j.logger.org.apache.jena=WARN",

             "# Fuseki System logs.",
             "log4j.logger." + Fuseki.serverLogName     + "=INFO",
             "log4j.logger." + Fuseki.actionLogName     + "=INFO",
             "log4j.logger." + Fuseki.adminLogName      + "=INFO",
             "log4j.logger." + Fuseki.validationLogName + "=INFO",
             "log4j.logger." + Fuseki.configLogName     + "=INFO",

             "log4j.logger.org.apache.jena.tdb.loader=INFO",
             "log4j.logger.org.eclipse.jetty=WARN" ,
             "log4j.logger.org.apache.shiro=WARN",

             "# NCSA Request Access log",
             "log4j.additivity."+Fuseki.requestLogName   + "=false",
             "log4j.logger."+Fuseki.requestLogName       + "=OFF, fuseki.plain",

             "## Parser output", 
             "log4j.additivity" + SysRIOT.riotLoggerName + "=false",
             "log4j.logger." + SysRIOT.riotLoggerName + "=INFO, plainstdout"
                ) ;
    }
}

