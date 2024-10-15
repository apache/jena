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

package org.apache.jena.rfc3986;

/** Setup and configuration of the IRI3986 parser package. */
public class SystemIRI3986 {

    /** System default : throw exception on errors, silent about warnings. */
    private static final ErrorHandler errorHandlerSystemDefault =
            ErrorHandler.create(s -> { throw new IRIParseException(s); }, null);

    /**
     * System error handler.
     * The initial setting is one that throws errors, and ignore warnings.
     */
    private static ErrorHandler errorHandler = errorHandlerSystemDefault;

    public static void setErrorHandler(ErrorHandler errHandler) {
        errorHandler = errHandler;
    }

    public static ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Send any violations to an {@link ErrorHandler}.
     * <p>
     * The error handler may throw an exception to give an operation that is "good
     * IRI or exception".
     * <p>
     * This call uses the system default {@link SeverityMap} from
     * {@link Violations#severities()}.
     */
    public static void toHandler(IRI3986 iri, ErrorHandler errorHandler) {
        toHandler(iri, Violations.severities(), errorHandler);
    }

    /**
     * Determine the severity of each violation and send to an {@link ErrorHandler}.
     * The severity is determined from the severity function, which should not return
     * null.
     * <p>
     * The error handler may throw an exception to give an operation that is "good
     * IRI or exception".
     */
    public static void toHandler(IRI3986 iri, SeverityMap severityMap, ErrorHandler errorHandler) {
        iri.forEachViolation((Violation report) -> {
            Severity severity = severityMap.getOrDefault(report.issue(), Severity.INVALID);
            switch (severity) {
                case WARNING ->        errorHandler.warning(report.message());
                case ERROR, INVALID -> errorHandler.error(report.message());
                case IGNORE -> {}
            }
        });
    }

    /*package*/ static String formatMsg(CharSequence source, int posn, String s) {
        StringBuilder sb = new StringBuilder(s.length()+20);
        if ( source != null ) {
            sb.append("<");
            sb.append(source);
            sb.append("> : ");
        }
        if ( posn >= 0 ) {
            sb.append("[Posn "+posn+"] ");
        }
        sb.append(s);
        return sb.toString();
    }
}
