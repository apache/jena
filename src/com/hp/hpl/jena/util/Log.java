/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            27 July 2001
 * Filename           $RCSfile: Log.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Released $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:20:59 $
 *               by   $Author: bwm $
 *
 * (c) Copyright Hewlett-Packard Company 2000, 2001
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
 *
 *****************************************************************************/


// Package
///////////////
package com.hp.hpl.jena.util;


// Imports
///////////////
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;


/**
 * <p>
 * Simple logging abstraction for use within the Jena core.  We have a general need to
 * log debug and diagnostic information, but we don't want to introduce another
 * dependency for Jena users by building on, say, Log4J (good though that package is).
 * Furthermore, the upcoming JDK 1.4 release will contain a standard logging API
 * <code>java.util.logging</code>.  We won't standardise on this API, since that would
 * require all Jena users to upgrade to a new JDK release. However, this class is
 * designed so that users who want to can interface this simple logger to more
 * sophisticated mechanisms, including the new standard.
 * </p>
 * <p>
 * Following common practice among logging packages, a number of levels of
 * severity are defined. Each log call is at one of these levels, and the logger has
 * a state variable that indicates the lowest level it will accept. Log entries at
 * this level or higher will be logged, those below this level will be silently
 * ignored. The levels are taken from the new <code>java.util.logging</code> API:
 * <ul>
 *    <li> SEVERE (highest value) </li>
 *    <li> WARNING </li>
 *    <li> INFO </li>
 *    <li> CONFIG </li>
 *    <li> FINE </li>
 *    <li> FINER </li>
 *    <li> FINEST (lowest value) </li>
 *    <li> OFF (turns off logging altoghether) </li>
 * </ul>
 * In addition, for compatability with other packages, DEBUG is defined as an alias
 * for FINE.
 * </p>
 * <p>
 * While the JDK 1.4 logger contains a great deal of flexibility for directing output
 * to different destinations, formatting, and localising, this class has deliberately
 * much simpler capabilities.  A log handler interface is defined (see
 * {@link com.hp.hpl.jena.util.LogHandler LogHandler}), which can publish a log message
 * as it sees fit.  This logger supports the use of a single log handler at a time, which
 * will consume all log messages. Two standard handlers are defined, one for console
 * output via <code>System.err</code>, and one for outputting to a file.  However, another
 * log handler, with arbitrary formatting capability, can be defined via {@link #setHandler}.
 * </p>
 * <p>
 * There is one global logger, implemented via a singleton pattern.  Convenience static
 * methods are provided for programmers to have simple access to logging capability.
 * </p>
 * <p>
 * <b>Basic usage patterns:</b><br>
 * Log a warning to the default output (console):
 * <code><pre>
 *     Log.warning( "Frimble count below threshold: " + nFrimbles );
 * </pre></code>
 * Direct log output to a file, and log a severe level exception (with stacktrace):
 * <code><pre>
 *     Log.getInstance().setDefaultFileHandler();
 *     Log.severe( "IOException while frimbling: " + frimEx, frimEx );
 * </pre></code>
 * Debug output, without timestamp:
 * <code><pre>
 *     Log.getInstance().setShowDate( false );
 *     Log.getInstance().setLevel( Log.FINE );
 *     Log.debug( "Frimble count: " + nFrimbles,
 *                Frimbler.class.getName(), "countFrimbles" );
 * </pre></code>
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: Log.java,v 1.1.1.1 2002-12-19 19:20:59 bwm Exp $
 */
public class Log
{
    // Constants
    //////////////////////////////////

    /** Message level, representing the highest level (most important) messages */
    public static final int SEVERE = 7;

    /** Message level, representing the level below SEVERE */
    public static final int WARNING = 6;

    /** Message level, representing the level below WARNING */
    public static final int INFO = 5;

    /** Message level, representing the level below INFO */
    public static final int CONFIG = 4;

    /** Message level, representing the level below CONFIG */
    public static final int FINE = 3;

    /** Message level, representing the level below FINE */
    public static final int FINER = 2;

    /** Message level, representing the lowest level (least important) messages */
    public static final int FINEST = 1;

    /** Message level used to suppress all messages. */
    public static final int OFF = 0;

    /** Convenience alias for FINE */
    public static final int DEBUG = FINE;

    /** The default file name for the Jena log file */
    public static final String DEFAULT_LOG_FILE_NAME = "jena.log";

    /** The default message level for the logger (CONFIG) */
    public static final int DEFAULT_LEVEL = CONFIG;

    /** Set of names corresponding to the well-known levels */
    public static final String[] LEVEL_NAMES = new String[] {"OFF", "FINEST", "FINER", "FINE", "CONFIG",
                                                             "INFO", "WARNING", "SEVERE"};



    // Static variables
    //////////////////////////////////

    /** The singleton instance */
    private static Log s_instance = new Log();

    /** Simple date formatter for showing the current date and time */
    private static SimpleDateFormat s_dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z " );

    /** Flag to control whether the date is shown during console output */
    private static boolean s_showDate = true;



    // Instance variables
    //////////////////////////////////

    /** The current (unique) handler for all messages */
    private LogHandler m_handler = null;

    /** The current log level we are operating at */
    private int m_logLevel = DEFAULT_LEVEL;



    // Constructors
    //////////////////////////////////

    /**
     * Constructor is private to enforce singleton postcondition. Set default
     * handler to console.
     */
    private Log() {
        setConsoleHandler();
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Answer a reference to the singleton (global) instance of the logger.
     *
     * @return the single global log object.
     */
    public static final Log getInstance() {
        return s_instance;
    }


    /**
     * Set the current loggin level to the given value.
     *
     * @param level The new logging level
     */
    public void setLevel( int level ) {
        m_logLevel = level;
    }


    /**
     * Answer the current logging level.
     *
     * @return the log level as an integer.
     */
    public int getLevel() {
        return m_logLevel;
    }


    /**
     * Answer a readable string for the given level identifier
     *
     * @param level The level number
     * @return the level name
     */
    public static String getLevelName( int level ) {
        if (level >= OFF  &&  level <= SEVERE) {
            return LEVEL_NAMES[level];
        }
        else {
            return "UNKNOWN";
        }
    }


    /**
     * Set the current output handler to be the given object.  The old
     * handler will be closed first.
     *
     * @param handler The new output handler.
     */
    public void setHandler( LogHandler handler ) {
        if (m_handler != null) {
            m_handler.close();
        }

        m_handler = handler;
    }


    /**
     * Set the current output handler to one that writes to the default
     * log file (see {@link #DEFAULT_LOG_FILE_NAME}), overwriting any
     * existing contents.
     *
     * @exception Throws IOException if the default log file cannot be opened for writing.
     */
    public void setDefaultFileHandler()
        throws IOException
    {
        setFileHandler( DEFAULT_LOG_FILE_NAME );
    }


    /**
     * Set the current output handler to one that writes to the default
     * log file (see {@link #DEFAULT_LOG_FILE_NAME}), optionally appending
     * to any existing log file contents.
     *
     * @param append If true, append to the end of the existing log file; otherwise
     *               overwrite it.
     * @exception Throws IOException if the default log file cannot be opened for writing.
     */
    public void setDefaultFileHandler( boolean append )
        throws IOException
    {
        setFileHandler( DEFAULT_LOG_FILE_NAME, append );
    }


    /**
     * Set the current output handler to one that writes to the given
     * file, overwriting the current contents, if any.
     *
     * @param fileName The name of the file to write to.
     * @exception IOException if the file cannot be opened for writing.
     */
    public void setFileHandler( String fileName )
        throws IOException
    {
        setFileHandler( fileName, false );
    }


    /**
     * Set the current output handler to one that writes to the given
     * file, optionally appending to the existing contents of the logfile.
     *
     * @param fileName The name of the file to write to.
     * @param append If true, append to the end of the file; otherwise overwrite it.
     * @exception IOException if the file cannot be opened for writing.
     */
    public void setFileHandler( String fileName, boolean append )
        throws IOException
    {
        // try to build the new handler first, in case any IO exception is thrown
        LogHandler h = new LogFileHandler( fileName, append );

        // now switch over
        setHandler( h );
    }


    /**
     * Set the current output handler to one that writes to System.err
     */
    public void setConsoleHandler() {
        // build a simple handler that prints to System.err
        LogHandler h = new LogHandler() {
                           /* Format message for display on console */
                           public void publish( int level, String msg, String cls, String method, Throwable ex ) {
                               System.err.print( Log.getLevelName( level ) );
                               System.err.print( ' ' );
                               System.err.print( formatDate() );
                               if (cls != null) {
                                   System.err.print( "In " + cls + "." + method + "(): " );
                               }
                               System.err.println( msg );
                               if (ex != null) {
                                   ex.printStackTrace( System.err );
                               }
                           }

                           /* Don't close System.err */
                           public void close() {}
                       };

        // now switch over
        setHandler( h );
    }


    /**
     * Set a flag to control whether the date is shown in the console log.
     * Default true.
     *
     * @param showDate If true, the date and time will be prepended to console log output.
     */
    public void setShowDate( boolean showDate ) {
        s_showDate = showDate;
    }


    /**
     * Answer whether the date is shown in console output.
     *
     * @return true if the date and time are prepended to console output (default true).
     */
    public boolean getShowDate() {
        return s_showDate;
    }


    /**
     * Answer a string representing the date and time, formatted for
     * output to the console.
     *
     * @return a string encoding the date and time.
     */
    public static String formatDate() {
        if (s_showDate) {
            return s_dateFormat.format( new Date() );
        }
        else {
            return "";
        }
    }


    /* --------------- Message handlers --------------- */

    /**
     * Log the given message at log level {@link #SEVERE}.
     *
     * @param msg The message text
     */
    public static void severe( String msg ) {
        getInstance().logMessage( msg, SEVERE, null, null, null );
    }

    /**
     * Log the given message at log level {@link #SEVERE}.
     *
     * @param msg The message text.
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void severe( String msg, Throwable ex ) {
        getInstance().logMessage( msg, SEVERE, null, null, ex );
    }

    /**
     * Log the given message at log level {@link #SEVERE}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     */
    public static void severe( String msg, String cls, String method ) {
        getInstance().logMessage( msg, SEVERE, cls, method, null );
    }

    /**
     * Log the given message at log level {@link #SEVERE}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void severe( String msg, String cls, String method, Throwable ex ) {
        getInstance().logMessage( msg, SEVERE, cls, method, ex );
    }


    /**
     * Log the given message at log level {@link #WARNING}.
     *
     * @param msg The message text
     */
    public static void warning( String msg ) {
        getInstance().logMessage( msg, WARNING, null, null, null );
    }

    /**
     * Log the given message at log level {@link #WARNING}.
     *
     * @param msg The message text.
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void warning( String msg, Throwable ex ) {
        getInstance().logMessage( msg, WARNING, null, null, ex );
    }

    /**
     * Log the given message at log level {@link #WARNING}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     */
    public static void warning( String msg, String cls, String method ) {
        getInstance().logMessage( msg, WARNING, cls, method, null );
    }

    /**
     * Log the given message at log level {@link #WARNING}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void warning( String msg, String cls, String method, Throwable ex ) {
        getInstance().logMessage( msg, WARNING, cls, method, ex );
    }


    /**
     * Log the given message at log level {@link #INFO}.
     *
     * @param msg The message text
     */
    public static void info( String msg ) {
        getInstance().logMessage( msg, INFO, null, null, null );
    }

    /**
     * Log the given message at log level {@link #INFO}.
     *
     * @param msg The message text.
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void info( String msg, Throwable ex ) {
        getInstance().logMessage( msg, INFO, null, null, ex );
    }

    /**
     * Log the given message at log level {@link #INFO}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     */
    public static void info( String msg, String cls, String method ) {
        getInstance().logMessage( msg, INFO, cls, method, null );
    }

    /**
     * Log the given message at log level {@link #INFO}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void info( String msg, String cls, String method, Throwable ex ) {
        getInstance().logMessage( msg, INFO, cls, method, ex );
    }


    /**
     * Log the given message at log level {@link #CONFIG}.
     *
     * @param msg The message text
     */
    public static void config( String msg ) {
        getInstance().logMessage( msg, CONFIG, null, null, null );
    }

    /**
     * Log the given message at log level {@link #CONFIG}.
     *
     * @param msg The message text.
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void config( String msg, Throwable ex ) {
        getInstance().logMessage( msg, CONFIG, null, null, ex );
    }

    /**
     * Log the given message at log level {@link #CONFIG}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     */
    public static void config( String msg, String cls, String method ) {
        getInstance().logMessage( msg, CONFIG, cls, method, null );
    }

    /**
     * Log the given message at log level {@link #CONFIG}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void config( String msg, String cls, String method, Throwable ex ) {
        getInstance().logMessage( msg, CONFIG, cls, method, ex );
    }


    /**
     * Log the given message at log level {@link #FINE}.
     *
     * @param msg The message text
     */
    public static void fine( String msg ) {
        getInstance().logMessage( msg, FINE, null, null, null );
    }

    /**
     * Log the given message at log level {@link #FINE}.
     *
     * @param msg The message text.
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void fine( String msg, Throwable ex ) {
        getInstance().logMessage( msg, FINE, null, null, ex );
    }

    /**
     * Log the given message at log level {@link #FINE}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     */
    public static void fine( String msg, String cls, String method ) {
        getInstance().logMessage( msg, FINE, cls, method, null );
    }

    /**
     * Log the given message at log level {@link #FINE}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void fine( String msg, String cls, String method, Throwable ex ) {
        getInstance().logMessage( msg, FINE, cls, method, ex );
    }


    /**
     * Log the given message at log level {@link #FINER}.
     *
     * @param msg The message text
     */
    public static void finer( String msg ) {
        getInstance().logMessage( msg, FINER, null, null, null );
    }

    /**
     * Log the given message at log level {@link #FINER}.
     *
     * @param msg The message text.
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void finer( String msg, Throwable ex ) {
        getInstance().logMessage( msg, FINER, null, null, ex );
    }

    /**
     * Log the given message at log level {@link #FINER}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     */
    public static void finer( String msg, String cls, String method ) {
        getInstance().logMessage( msg, FINER, cls, method, null );
    }

    /**
     * Log the given message at log level {@link #FINER}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void finer( String msg, String cls, String method, Throwable ex ) {
        getInstance().logMessage( msg, FINER, cls, method, ex );
    }


    /**
     * Log the given message at log level {@link #FINEST}.
     *
     * @param msg The message text
     */
    public static void finest( String msg ) {
        getInstance().logMessage( msg, FINEST, null, null, null );
    }

    /**
     * Log the given message at log level {@link #FINEST}.
     *
     * @param msg The message text.
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void finest( String msg, Throwable ex ) {
        getInstance().logMessage( msg, FINEST, null, null, ex );
    }

    /**
     * Log the given message at log level {@link #FINEST}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     */
    public static void finest( String msg, String cls, String method ) {
        getInstance().logMessage( msg, FINEST, cls, method, null );
    }

    /**
     * Log the given message at log level {@link #FINEST}.
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void finest( String msg, String cls, String method, Throwable ex ) {
        getInstance().logMessage( msg, FINEST, cls, method, ex );
    }


    /**
     * Log the given message at log level {@link #DEBUG} (alias FINE).
     *
     * @param msg The message text
     */
    public static void debug( String msg ) {
        getInstance().logMessage( msg, DEBUG, null, null, null );
    }

    /**
     * Log the given message at log level {@link #DEBUG} (alias FINE).
     *
     * @param msg The message text.
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void debug( String msg, Throwable ex ) {
        getInstance().logMessage( msg, DEBUG, null, null, ex );
    }

    /**
     * Log the given message at log level {@link #DEBUG} (alias FINE).
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     */
    public static void debug( String msg, String cls, String method ) {
        getInstance().logMessage( msg, DEBUG, cls, method, null );
    }

    /**
     * Log the given message at log level {@link #DEBUG} (alias FINE).
     *
     * @param msg The message text.
     * @param cls The class name of the originator of the log event
     * @param method The method name of the originator of the log event
     * @param ex A throwable (exception or error) that triggered the log event.
     */
    public static void debug( String msg, String cls, String method, Throwable ex ) {
        getInstance().logMessage( msg, DEBUG, cls, method, ex );
    }



    // Internal implementation methods
    //////////////////////////////////

    /**
     * Log the given message to the current handler.
     *
     * @param msg The message to be logged
     * @param level The log level of the message
     * @param cls The name of the originating class, or null
     * @param method The name of the originating method, or null
     * @param ex A throwable that triggered the log message, or null
     */
    private synchronized void logMessage( String msg, int level, String cls, String method, Throwable ex ) {
        if (level >= m_logLevel) {
            m_handler.publish( level, msg, cls, method, ex );
        }
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
