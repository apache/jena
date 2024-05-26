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

package riotcmd;

import static org.apache.jena.riot.SysRIOT.fmtMessage;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ErrorHandler;
import org.slf4j.Logger;

/**
 * Error handler for "riot" command line parsing.
 */
class ErrorHandlerCLI implements ErrorHandler {

    /** Logs warnings and errors while tracking the counts of each and optionally throwing exceptions when errors and/or warnings are encounted */
    static ErrorHandlerCLI errorHandlerTracking(Logger log, boolean silentWarnings, boolean failOnError, boolean failOnWarning)
    { return new ErrorHandlerCLI(log, silentWarnings, failOnError, failOnWarning); }

    private final Logger log ;
    private final boolean silentWarnings;
    private final boolean failOnError;
    private final boolean failOnWarning;
    private long errorCount, warningCount;

    public ErrorHandlerCLI(Logger log, boolean silentWarnings, boolean failOnError, boolean failOnWarning) {
        this.log = log ;
        this.silentWarnings = silentWarnings;
        this.failOnError = failOnError;
        this.failOnWarning = failOnWarning;
    }

    /** report a warning  */
    @Override
    public void warning(String message, long line, long col) {
        if ( ! silentWarnings )
            logWarning(message, line, col) ;
        this.warningCount++;
        if (! silentWarnings && this.failOnWarning)
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

    long getErrorCount() {
        return this.errorCount;
    }

    long getWarningCount() {
        return this.warningCount;
    }

    boolean hadErrors() {
        return this.errorCount > 0;
    }

    boolean hadWarnings() {
        return this.warningCount > 0;
    }

    boolean hadIssues() {
        return hadErrors() || hadWarnings();
    }

    /** report a warning */
    private void logWarning(String message, long line, long col) {
        if ( log != null )
            log.warn(fmtMessage(message, line, col)) ;
    }

    /** report an error */
    private void logError(String message, long line, long col) {
        if ( log != null )
            log.error(fmtMessage(message, line, col)) ;
    }

    /** report a catastrophic error */
    private void logFatal(String message, long line, long col) {
        if ( log != null )
            logError(message, line, col) ;
    }
}
