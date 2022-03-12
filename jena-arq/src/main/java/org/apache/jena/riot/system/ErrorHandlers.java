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

import java.util.function.Supplier;

public class ErrorHandlers {

    /** Relay an error handler event to an error handler w.r.t. a severity level */
    public static void relay(ErrorHandler errorHandler, Severity severity, ErrorHandlerEvent evt) {
        switch (severity) {
        case IGNORE: break;
        case WARNING:
            errorHandler.warning(evt.getMessage(), evt.getLine(), evt.getCol());
            break;
        case ERROR:
            errorHandler.error(evt.getMessage(), evt.getLine(), evt.getCol());
            break;
        case FATAL:
            errorHandler.fatal(evt.getMessage(), evt.getLine(), evt.getCol());
            break;
        }
    }

    /** Relay an error handler event to an error handler w.r.t. a severity level.
     * Lambda version that does not build messages if serverity level is IGNORE  */
    public static void relay(ErrorHandler errorHandler, Severity severity, Supplier<ErrorHandlerEvent> evtSupplier) {
        ErrorHandlerEvent evt;
        switch (severity) {
        case IGNORE: break;
        case WARNING:
            evt = evtSupplier.get();
            errorHandler.warning(evt.getMessage(), evt.getLine(), evt.getCol());
            break;
        case ERROR:
            evt = evtSupplier.get();
            errorHandler.error(evt.getMessage(), evt.getLine(), evt.getCol());
            break;
        case FATAL:
            evt = evtSupplier.get();
            errorHandler.fatal(evt.getMessage(), evt.getLine(), evt.getCol());
            break;
        }
    }
}
