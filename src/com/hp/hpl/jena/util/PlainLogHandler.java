/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */


package com.hp.hpl.jena.util ;

import java.io.* ;

/** A simple, unadorned output stream.
 *  Does not add anything to the messages
 */

public class PlainLogHandler implements LogHandler {

    PrintWriter out = null ;
    static final String FIELD_SEP = " " ;

    /** Construct PlainLogHandler that goes to System.out
     */
    public PlainLogHandler()
    {
        this(System.out) ;
    }


    /** Construct PlainLogHandler that goes to an existing java.io.OutputStream
     */
    public PlainLogHandler(OutputStream outStream)
    {
        out = new PrintWriter(outStream) ;
    }

    /** Construct PlainLogHandler that goes to an existing java.io.PrintWriter
    */
    public PlainLogHandler(PrintWriter pw)
    {
        out = pw ;
    }


    /**
     * Publish the given log information.
     *
     * @param level The log level that this message is published at (ignored in PlainLogHandler)
     * @param msg The log message to be published.
     * @param cls Optional name of the class that originated the log request.
     * @param method Optional name of the method that originated the log request.
     * @param ex Optional throwable that triggered the log request.
     */

    public void publish( int level, String msg, String cls, String method, Throwable ex )
    {
        if (out != null)
        {
            if (cls != null && cls.length() > 0 )
            {
                out.print( cls );
                if ( method != null && method.length() > 0 )
                {
                    out.print(".") ;
                    out.print( method );
                }
                out.print(":") ;
                out.print( FIELD_SEP );
            }

            // print the message itself
            out.println( msg );

            // and show the exception backtrace if defined
            if (ex != null)
               ex.printStackTrace( out );

            // ensure that this record is flushed
            out.flush();
        }
    }

    /**
     * Close the log handler and free any associated resources.
     * Does not close the underlying stream in case that in turn closes System.out
     */
    public void close()
    {
        out.flush() ;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
 *  All rights reserved.
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
