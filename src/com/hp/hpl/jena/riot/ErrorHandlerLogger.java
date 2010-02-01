/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import org.slf4j.Logger;

/** An error handler that logs for errors and warnings. */ 
public class ErrorHandlerLogger implements ErrorHandler
{
    private Logger log ;

    public static String fmtMessage(String message, long line, long col)
    {
        if ( col == -1 && line == -1 )
                return message ;
        if ( col == -1 && line != -1 )
            return String.format("[line: %d] %s", line, message) ;
        if ( col != -1 && line == -1 )
            return String.format("[col: %d] %s", col, message) ;
        
        return String.format("[line: %d, col: %d] %s", line, col, message) ;
    }
    
    public ErrorHandlerLogger(Logger log)
    {
        this.log = log ;
    }

    /** report a warning */
    public void warning(String message, long line, long col)     { log.warn(fmtMessage(message, line, col)) ; }
    
    /** report an error */
    public void error(String message, long line, long col)       { log.error(fmtMessage(message, line, col)) ; }
    
    /** report a catastrophic error.  Must not return. */    
    public void fatal(String message, long line, long col)  { throw new RiotException(fmtMessage(message, line, col)) ; }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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