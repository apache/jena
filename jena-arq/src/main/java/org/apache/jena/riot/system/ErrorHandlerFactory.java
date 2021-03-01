/*
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

package org.apache.jena.riot.system;

import static org.apache.jena.riot.SysRIOT.fmtMessage ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.RiotParseException ;
import org.apache.jena.riot.SysRIOT ;
import org.slf4j.Logger ;

public class ErrorHandlerFactory
{
    static public final Logger stdLogger = SysRIOT.getLogger() ;
    static public final Logger noLogger = null ;

    /** Standard error handler - logs to stdLogger */
    static public final ErrorHandler errorHandlerStd          = errorHandlerStd(stdLogger) ;

    /** Error handler (no warnings) - logs to stdLogger */
    static public final ErrorHandler errorHandlerNoWarnings   = errorHandlerNoWarnings(stdLogger) ;

    /** Strict error handler - logs to stdLogger - exceptions for warnings */
    static public final ErrorHandler errorHandlerStrict       = errorHandlerStrict(stdLogger) ;

    /** Warning error handler - logs to stdLogger - messages for warnings and some errors */
    static public final ErrorHandler errorHandlerWarn         = errorHandlerWarning(stdLogger) ;

    /** Silent error handler : ignores warnings, throws exceptions for errors */
    static public final ErrorHandler errorHandlerNoLogging    = errorHandlerSimple() ;

    /** Silent, strict error handler */
    static public final ErrorHandler errorHandlerStrictNoLogging    = errorHandlerStrictSilent() ;

    /** Silent, strict error handler, no logging */
    public static ErrorHandler errorHandlerStrictSilent()           { return new ErrorHandlerStrict(null) ; }

    /** Strict error handler, with logging */
    public static ErrorHandler errorHandlerStrict(Logger log)       { return new ErrorHandlerStrict(log) ; }

    /** An error handler that logs messages, then throws exceptions for errors but not warnings */
    public static ErrorHandler errorHandlerStd(Logger log)          { return new ErrorHandlerStd(log) ; }

    /** An error handler that logs error and fatal messages, but not warnings */
    public static ErrorHandler errorHandlerNoWarnings(Logger log)   { return new ErrorHandlerNoWarnings(log) ; }

    /** An error handler that logs messages for errors and warnings and attempts to carry on */
    public static ErrorHandler errorHandlerWarning(Logger log)      { return new ErrorHandlerWarning(log) ; }

    /** Ignores warnings, throws exceptions for errors */
    public static ErrorHandler errorHandlerSimple()                 { return new ErrorHandlerSimple() ; }

    /** Logs warnings and errors while tracking the counts of each and optionally throwing exceptions when errors and/or warnings are encounted */
    public static ErrorHandlerTracking errorHandlerTracking(Logger log, boolean failOnError, boolean failOnWarning) { return new ErrorHandlerTracking(log, failOnError, failOnWarning); }

    /**
     * An error handler that throws a {@link RiotParseException}, hence it
     * exposes the details of errors.
     */
    public static ErrorHandler errorHandlerDetailed()           { return new ErrorHandlerRiotParseErrors() ; }

    /**
     * An error handler that throws exceptions in all cases.
     */
    public static ErrorHandler errorHandlerExceptions()        { return new ErrorHandlerRiotParseException() ; }

    private static ErrorHandler defaultErrorHandler = errorHandlerStd ;
    /** Get the current default error handler */
    public static ErrorHandler getDefaultErrorHandler() { return defaultErrorHandler ; }

    /** Set the current default error handler - use carefully, mainly for use in testing */
    public static void setDefaultErrorHandler(ErrorHandler errorHandler) { defaultErrorHandler = errorHandler ; }

    /** Messages to a logger. This is not an ErrorHandler */
    private static class ErrorLogger {
        protected final Logger log ;

        public ErrorLogger(Logger log) {
            this.log = log ;
        }

        /** report a warning */
        public void logWarning(String message, long line, long col) {
            if ( log != null )
                log.warn(fmtMessage(message, line, col)) ;
        }

        /** report an error */
        public void logError(String message, long line, long col) {
            if ( log != null )
                log.error(fmtMessage(message, line, col)) ;
        }

        /** report a catastrophic error */
        public void logFatal(String message, long line, long col) {
            if ( log != null )
                logError(message, line, col) ;
        }
    }

    /** Ignores warnings, throws exceptions for errors */
    private static class ErrorHandlerSimple implements ErrorHandler {
        @Override
        public void warning(String message, long line, long col)
        {}
        @Override
        public void error(String message, long line, long col)
        { throw new RiotException(fmtMessage(message, line, col)) ; }

        @Override
        public void fatal(String message, long line, long col)
        { throw new RiotException(fmtMessage(message, line, col)) ; }
    }

    /** An error handler that logs message then throws exceptions for errors but not warnings */
    private static class ErrorHandlerStd extends ErrorLogger implements ErrorHandler {
        public ErrorHandlerStd(Logger log) {
            super(log) ;
        }

        /** report a warning */
        @Override
        public void warning(String message, long line, long col) {
            logWarning(message, line, col);
        }

        /** report an error */
        @Override
        public void error(String message, long line, long col) {
            logError(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        /** report a fatal error - does not return */
        @Override
        public void fatal(String message, long line, long col) {
            logFatal(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
    }

    /** An error handler that logs message then throws exceptions for errors but not warnings */
    private static class ErrorHandlerNoWarnings extends ErrorLogger implements ErrorHandler {
        public ErrorHandlerNoWarnings(Logger log) {
            super(log) ;
        }

        /** report a warning */
        @Override
        public void warning(String message, long line, long col)
        { } //logWarning(message, line, col) ;

        /** report an error */
        @Override
        public void error(String message, long line, long col) {
            logError(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        /** report a fatal error - does not return */
        @Override
        public void fatal(String message, long line, long col) {
            logFatal(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
    }

    /** An error handler that logs message for errors and warnings and throw exceptions on either */
    private static class ErrorHandlerStrict extends ErrorLogger implements ErrorHandler {
        public ErrorHandlerStrict(Logger log) {
            super(log) ;
        }

        /** report a warning - do not carry on */
        @Override
        public void warning(String message, long line, long col) {
            logWarning(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        /** report an error - do not carry on */
        @Override
        public void error(String message, long line, long col) {
            logError(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        @Override
        public void fatal(String message, long line, long col) {
            logFatal(message, line, col) ;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
    }

    /**
     * An error handler that throw exceptions on warnings and errors but does
     * not log
     */
    private static class ErrorHandlerStrictSilent implements ErrorHandler {
        /** report a warning - do not carry on */
        @Override
        public void warning(String message, long line, long col) {
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        /** report an error - do not carry on */
        @Override
        public void error(String message, long line, long col) {
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        @Override
        public void fatal(String message, long line, long col) {
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
    }

    /** An error handler that logs message for errors and warnings and throw exceptions on either */
    public static class ErrorHandlerTracking extends ErrorLogger implements ErrorHandler {
        private final boolean failOnError, failOnWarning;
        private long errorCount, warningCount;

        public ErrorHandlerTracking(Logger log, boolean failOnError, boolean failOnWarning) {
            super(log) ;

            this.failOnError = failOnError;
            this.failOnWarning = failOnWarning;
        }

        /** report a warning  */
        @Override
        public void warning(String message, long line, long col) {
            logWarning(message, line, col) ;
            this.warningCount++;
            if (this.failOnWarning)
                throw new RiotException(fmtMessage(message, line, col)) ;
        }

        /** report an error */
        @Override
        public void error(String message, long line, long col) {
            logError(message, line, col) ;
            this.errorCount++;
            if (this.failOnError)
                throw new RiotException(fmtMessage(message, line, col)) ;
        }

        @Override
        public void fatal(String message, long line, long col) {
            logFatal(message, line, col) ;
            this.errorCount++;
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        public long getErrorCount() {
            return this.errorCount;
        }

        public long getWarningCount() {
            return this.warningCount;
        }

        public boolean hadErrors() {
            return this.errorCount > 0;
        }

        public boolean hadWarnings() {
            return this.warningCount > 0;
        }

        public boolean hadIssues() {
            return hadErrors() || hadWarnings();
        }
    }

    /** An error handler that logs messages for errors and warnings and attempt to carry on */
    private static class ErrorHandlerWarning extends ErrorLogger implements ErrorHandler {
        public ErrorHandlerWarning(Logger log)
        { super(log) ; }

        @Override
        public void warning(String message, long line, long col)
        { logWarning(message, line, col) ; }

        /** report an error but continue */
        @Override
        public void error(String message, long line, long col)
        { logError(message, line, col) ; }

        @Override
        public void fatal(String message, long line, long col) {
            logFatal(message, line, col) ;
            throw new RiotException(SysRIOT.fmtMessage(message, line, col)) ;
        }
    }

    /** An error handler that throws a RiotParseException, hence it exposes the details of errors. */
    private static class ErrorHandlerRiotParseErrors implements ErrorHandler {

        public ErrorHandlerRiotParseErrors() {}

        @Override public void warning(String message, long line, long col) { }

        @Override public void error(String message, long line, long col) {
            throw new RiotParseException(message, line, col);
        }

        @Override public void fatal(String message, long line, long col) {
            throw new RiotParseException(message, line, col);
        }
    }

    /** An error handler that throws a RiotParseException in all cases. */
    private static class ErrorHandlerRiotParseException implements ErrorHandler {

        public ErrorHandlerRiotParseException() {}

        @Override public void warning(String message, long line, long col) {
            throw new RiotParseException(message, line, col);
        }

        @Override public void error(String message, long line, long col) {
            throw new RiotParseException(message, line, col);
        }

        @Override public void fatal(String message, long line, long col) {
            throw new RiotParseException(message, line, col);
        }
    }
}
