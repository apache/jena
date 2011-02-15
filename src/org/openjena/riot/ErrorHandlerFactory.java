/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import static org.openjena.riot.SysRIOT.fmtMessage ;
import org.slf4j.Logger ;

public class ErrorHandlerFactory
{
    static public final Logger stdLogger = SysRIOT.getLogger() ;
    static public final Logger noLogger = null ;
    
    /** Standard error handler - logs to stdLogger */
    static public ErrorHandler errorHandlerStd          = errorHandlerStd(stdLogger) ;

    /** Strict error handler - logs to stdLogger - exceptions for warnings */
    static public ErrorHandler errorHandlerStrict       = errorHandlerStrict(stdLogger) ;
    
    /** Warning error handler - logs to stdLogger - mesages for warnings and some errors */
    static public ErrorHandler errorHandlerWarn         = errorHandlerWarning(stdLogger) ;
    
    /** Silent error handler */
    static public ErrorHandler errorHandlerNoLogging    = errorHandlerSimple() ;
    
    static public void setTestLogging(boolean visible)
    {
        // Reset
        if ( visible )
            errorHandlerStd = new ErrorHandlerStd(stdLogger) ;
        else
            errorHandlerStd = new ErrorHandlerSimple() ;
    }

    public static ErrorHandler errorHandlerStrict(Logger log)   { return new ErrorHandlerStrict(log) ; }
    public static ErrorHandler errorHandlerStd(Logger log)      { return new ErrorHandlerStd(log) ; }
    public static ErrorHandler errorHandlerWarning(Logger log)  { return new ErrorHandlerWarning(log) ; }
    public static ErrorHandler errorHandlerSimple()             { return new ErrorHandlerSimple() ; }
    
    /** Messages to a logger. This is not an ErrorHandler */ 
    private static class ErrorLogger
    {
        protected final Logger log ;

        public ErrorLogger(Logger log)
        {
            this.log = log ;
        }

        /** report a warning */
        public void logWarning(String message, long line, long col)
        {
            log.warn(fmtMessage(message, line, col)) ;
        }
        
        /** report an error */
        public void logError(String message, long line, long col)
        {
            log.error(fmtMessage(message, line, col)) ;
        }

        /** report a catastrophic error */    
        public void logFatal(String message, long line, long col)
        { 
            logError(message, line, col) ;
        }
    }
    
    /** Ignores warnings, throws exceptions for errors */ 
    private static class ErrorHandlerSimple implements ErrorHandler
    {
        public void warning(String message, long line, long col)
        {}
        public void error(String message, long line, long col)
        { throw new RiotException(fmtMessage(message, line, col)) ; }

        public void fatal(String message, long line, long col)
        { throw new RiotException(fmtMessage(message, line, col)) ; }
    }
    
    /** An error handler that logs message then throws exceptions for errors but not warnings */ 
    private static class ErrorHandlerStd extends ErrorLogger implements ErrorHandler
    {
        public ErrorHandlerStd(Logger log)
        {
            super(log) ;
        }
        
        /** report a warning */
        //@Override
        public void warning(String message, long line, long col)
        { logWarning(message, line, col) ; }
        
        /** report an error */
        //@Override
        public void error(String message, long line, long col)
        { 
            logError(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        /** report a fatal error - does not return */
        //@Override
        public void fatal(String message, long line, long col)
        {
            logFatal(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
    }
    
    /** An error handler that logs message for errors and warnings and throw exceptions on either */ 
    private static class ErrorHandlerStrict extends ErrorLogger implements ErrorHandler
    {
        public ErrorHandlerStrict(Logger log)
        {
            super(log) ;
        }
        
        /** report a warning  - do not carry on */
        //@Override
        public void warning(String message, long line, long col)
        { 
            logWarning(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
        
        /** report an error - do not carry on */
        //@Override
        public void error(String message, long line, long col)
        { 
            logError(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        public void fatal(String message, long line, long col)
        {
            logFatal(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
    }
    
    /** An error handler that logs messages for errors and warnings and attempt to carry on */ 
    private static class ErrorHandlerWarning extends ErrorLogger implements ErrorHandler
    {
        public ErrorHandlerWarning(Logger log)
        { super(log) ; }
        
        //@Override
        public void warning(String message, long line, long col)
        { logWarning(message, line, col) ; }
        
        /** report an error but continue */
        //@Override
        public void error(String message, long line, long col)
        { logError(message, line, col) ; }

        //@Override
        public void fatal(String message, long line, long col)
        { 
            logFatal(message, line, col) ;
            throw new RiotException(SysRIOT.fmtMessage(message, line, col)) ; 
        }
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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