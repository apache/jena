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

import java.util.Iterator;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIComponents;
import org.apache.jena.iri.Violation;
import org.apache.jena.irix.IRIProviderJenaIRI;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.SetupJenaIRI;
import org.apache.jena.irix.SystemIRIx;

/**
 * Copy of the Jena 5.1.0 Checker code (jena-iri related)
 * called from ParserProfieStd.
 *
 * */
class CheckerJenaIRI {
    /** See also {@link IRIs#reference} */
    static boolean checkIRI(String iriStr, ErrorHandler errorHandler, long line, long col) {
        IRI iri = SetupJenaIRI.iriCheckerFactory().create(iriStr);
        boolean b = iriViolations(iri, errorHandler, line, col);
        return b;
    }

    /**
     * Process violations on an IRI Calls the {@link ErrorHandler} on all errors and
     * warnings (as warnings).
     */
    static boolean iriViolations(IRI iri, ErrorHandler errorHandler, long line, long col) {
        return iriViolations(iri, errorHandler, false, true, line, col);
    }

    /**
     * Process violations on an IRI Calls the errorHandler on all errors and warnings
     * (as warning). (If checking for relative IRIs, these are sent out as errors.)
     * Assumes error handler throws exceptions on errors if need be
     */
    static boolean iriViolations(IRI iri, ErrorHandler errorHandler,
                                        boolean allowRelativeIRIs, boolean includeIRIwarnings,
                                        long line, long col) {

        if ( !allowRelativeIRIs && iri.isRelative() )
            // Relative IRIs.
            iriViolationMessage(iri.toString(), true, "Relative IRI: " + iri, line, col, errorHandler);

        boolean isOK = true;

        if ( iri.hasViolation(includeIRIwarnings) ) {
            Iterator<Violation> iter = iri.violations(includeIRIwarnings);

            for ( ; iter.hasNext() ; ) {
                Violation v = iter.next();
                int code = v.getViolationCode();
                boolean isError = v.isError();

                // --- Tune warnings.
                // IRIProviderJena filters ERRORs and throws an exception on error.
                // It can't add warnings or remove them at that point.
                // Do WARN filtering here.
                if ( code == Violation.LOWERCASE_PREFERRED && v.getComponent() != IRIComponents.SCHEME ) {
                    // Issue warning about the scheme part only. Not e.g. DNS names.
                    continue;
                }

                isOK = false;
                String msg = v.getShortMessage();
                String iriStr = iri.toString();
                //System.out.println("Warning: "+msg);
                iriViolationMessage(iriStr, isError, msg, line, col, errorHandler);
            }
        }
        return isOK;
    }

    /**
     * Common handling messages about IRIs during parsing whether a violation or an
     * IRIException. Prints a warning, with different messages for IRI error or warning.
     */
    static void iriViolationMessage(String iriStr, boolean isError, String msg, long line, long col, ErrorHandler errorHandler) {
        try {
            if ( ! ( SystemIRIx.getProvider() instanceof IRIProviderJenaIRI ) )
                msg = "<" + iriStr + "> : " + msg;

            if ( isError ) {
                // ?? Treat as error, catch exceptions?
                errorHandler(errorHandler).warning("Bad IRI: " + msg, line, col);
            } else
                errorHandler(errorHandler).warning("Not advised IRI: " + msg, line, col);
        } catch (org.apache.jena.iri.IRIException0 | org.apache.jena.irix.IRIException ex) {}
    }

    private static ErrorHandler errorHandler(ErrorHandler handler) {
        return handler != null ? handler : ErrorHandlerFactory.errorHandlerStd;
    }

    // Does nothing. Used in "check(node)" operations where the boolean result is key.
    private static ErrorHandler nullErrorHandler  = new ErrorHandler() {
        @Override
        public void warning(String message, long line, long col) {}

        @Override
        public void error(String message, long line, long col) {}

        @Override
        public void fatal(String message, long line, long col) {}
    };
}
