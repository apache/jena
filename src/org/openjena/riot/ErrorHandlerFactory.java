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
