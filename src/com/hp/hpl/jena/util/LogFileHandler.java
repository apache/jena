/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            27 July 2001
 * Filename           $RCSfile: LogFileHandler.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Released $State: Exp $
 *
 * Last modified on   $Date: 2003-06-18 21:56:08 $
 *               by   $Author: ian_dickinson $
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Basic handler for writing log messages to a log file.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: LogFileHandler.java,v 1.2 2003-06-18 21:56:08 ian_dickinson Exp $
 */
public class LogFileHandler
    implements LogHandler
{
    // Constants
    //////////////////////////////////

    /** Character used to separate fields in the output. Default: tab */
    public static final char FIELD_SEP = '\t';


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** File writer that we are writing to */
    protected FileWriter m_file = null;

    /** Print writer providing print access to the file */
    protected PrintWriter m_print = null;

    // Constructors
    //////////////////////////////////

    /**
     * Construct a log file handler to write to the given log file.
     *
     * @param fileName The name of the log file to write to.
     * @param append If true, append to the end of an existing log file. If false,
     *               the existing file will be overwritten.
     * @exception IOException if the file cannot be opened for writing.
     */
    public LogFileHandler( String fileName, boolean append )
        throws IOException
    {
        m_file = new FileWriter( fileName, append );
        m_print = new PrintWriter( m_file );
    }


    /**
     * Construct a log file handler to write to the given log file, overwriting
     * any existing content in the log file.
     *
     * @param fileName The name of the log file to write to.
     * @exception IOException if the file cannot be opened for writing.
     */
    public LogFileHandler( String fileName )
        throws IOException
    {
        this( fileName, false );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Publish the given information to the log file opened by the constructor.
     *
     * @param level The log level that this message is published at
     * @param msg The log message to be published.
     * @param cls Optional name of the class that originated the log request.
     * @param method Optional name of the method that originated the log request.
     * @param ex Optional throwable that triggered the log request.
     */
    public void publish( int level, String msg, String cls, String method, Throwable ex ) {
        if (m_print != null) {
            // put the log level identifier at the start of the line
            m_print.print( Log.getLevelName( level ) );
            m_print.print( FIELD_SEP );

            // write the date at the beginning of the record
            m_print.print( Log.formatDate() );
            m_print.print( FIELD_SEP );

            // show the source if defined
            if (cls != null) {
               m_print.print( cls );
               m_print.print( FIELD_SEP );
               m_print.print( method );
               m_print.print( FIELD_SEP );
            }

            // print the message itself
            m_print.println( msg );

            // and show the exception backtrace if defined
            if (ex != null) {
               ex.printStackTrace( m_print );
            }

            // ensure that this record is flushed
            try {
                m_file.flush();
            }
            catch (IOException ignore) {}
        }
    }


    /**
     * Close the log file handler.
     */
    public void close() {
        try {
            m_file.close();
            m_file = null;
            m_print = null;
        }
        catch (IOException ignore) {}
    }



    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
