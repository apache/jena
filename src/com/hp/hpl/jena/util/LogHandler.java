/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            27 July 2001
 * Filename           $RCSfile: LogHandler.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Released $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:21:01 $
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


/**
 * Interface that encapsulates bare-bones log handling capability.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: LogHandler.java,v 1.1.1.1 2002-12-19 19:21:01 bwm Exp $
 */
public interface LogHandler
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * Publish the given log information.
     *
     * @param level The log level that this message is published at
     * @param msg The log message to be published.
     * @param cls Optional name of the class that originated the log request.
     * @param method Optional name of the method that originated the log request.
     * @param ex Optional throwable that triggered the log request.
     */
    public void publish( int level, String msg, String cls, String method, Throwable ex );


    /**
     * Close the log handler and free any associated resources.
     */
    public void close();

}
