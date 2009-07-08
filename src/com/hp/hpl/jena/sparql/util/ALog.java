/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.io.File;

import org.slf4j.LoggerFactory;

/** Convenience logging when it's just the odd log message.  Classes don't need to import
 *  the logging system, or have excessive pirvate static loggers for non-performance
 *  critical messages.
 */

public class ALog
{
    // See also atlas.lib.Log....
    static public void info(Object caller, String msg)
    {
        info(caller.getClass(), msg) ;
    }

    static public void info(Class<?> cls, String msg)
    {
        LoggerFactory.getLogger(cls).info(msg) ;
    }

    static public void info(Object caller, String msg, Throwable th)
    {
        info(caller.getClass(), msg, th) ;
    }

    static public void info(Class<?> cls, String msg, Throwable th)
    {
        LoggerFactory.getLogger(cls).warn(msg, th) ;
    }

    static public void warn(Object caller, String msg)
    {
        warn(caller.getClass(), msg) ;
    }

    static public void warn(Class<?> cls, String msg)
    {
        LoggerFactory.getLogger(cls).warn(msg) ;
    }

    static public void warn(Object caller, String msg, Throwable th)
    {
        warn(caller.getClass(), msg, th) ;
    }

    static public void warn(Class<?> cls, String msg, Throwable th)
    {
        LoggerFactory.getLogger(cls).warn(msg, th) ;
    }

    static public void fatal(Object caller, String msg)
    {
        fatal(caller.getClass(), msg) ;
    }

    static public void fatal(Class<?> cls, String msg)
    {
        LoggerFactory.getLogger(cls).error(msg) ;
    }

    static public void fatal(Object caller, String msg, Throwable th)
    {
        fatal(caller.getClass(), msg, th) ;
    }

    static public void fatal(Class<?> cls, String msg, Throwable th)
    {
        LoggerFactory.getLogger(cls).error(msg, th) ;
    }
    
    /** Turn on a logger (all levels). 
     *  Works for Log4j and Java logging as the logging provider to Apache common logging or slf4j.
     */
    static public void enable(String logger)
    {
        org.apache.log4j.LogManager.getLogger(logger).setLevel(org.apache.log4j.Level.ALL) ;
        java.util.logging.Logger.getLogger(logger).setLevel(java.util.logging.Level.ALL) ;
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
    
    /** Set log4j - try to find a file "log4j.properties" if nothing already set */
    public static void setLog4j()
    {
        if ( System.getProperty("log4j.configuration") == null )
        {
            String fn = "log4j.properties" ;
            File f = new File(fn) ;
            if ( f.exists() ) 
                System.setProperty("log4j.configuration", "file:"+fn) ;
        }
    }

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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