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

package org.apache.jena.riot.checker;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.graph.Node ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.Violation ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.IRIResolver ;

/**
 * Check IRIs.
 * <p>
 * This uses jena-iri for additional errors and warnings over and above the parsers..
 */
public class CheckerIRI implements NodeChecker
{
    private boolean allowRelativeIRIs = false ;

    private ErrorHandler handler ;
    private IRIFactory iriFactory ;

    public CheckerIRI() {
        this(ErrorHandlerFactory.getDefaultErrorHandler()) ;
    }

    public CheckerIRI(ErrorHandler handler) {
        this(handler, IRIResolver.iriCheckerFactory()) ;
    }

    public CheckerIRI(ErrorHandler handler, IRIFactory iriFactory) {
        this.handler = handler ;
        this.iriFactory = iriFactory ;
    }

    @Override
    public boolean check(Node node, long line, long col)
    { return node.isURI() && checkURI(node, line, col) ; }

    // An LRU cache is slower.
    // An unbounded cache is fastest but does not scale.
    private final Cache<Node, IRI> cache = CacheFactory.createSimpleCache(5000) ;

    // abstract
    public final boolean checkURI(Node node, long line, long col) {
        if ( cache != null && cache.containsKey(node) )
            return true ;

        IRI iri = iriFactory.create(node.getURI()) ; // always works - no exceptions.
        boolean b = checkIRI(iri, line, col) ;
        // If OK, put in cache.
        if ( cache != null && b )
            cache.put(node, iri) ;
        return b ;
    }

    final public boolean checkIRI(IRI iri, long line, long col) {
        iriViolations(iri, handler, allowRelativeIRIs, true, line, col) ;
        return !iri.hasViolation(true) ;
    }

    /**
     * Process violations on an IRI
     * Calls the errorHandler on all errors and warnings (as warning).
     * Assumes error handler throws exceptions on errors if need be
     */
    private static void iriViolations(IRI iri, ErrorHandler errorHandler,
                                      boolean allowRelativeIRIs,
                                      boolean includeIRIwarnings,
                                      long line, long col) {
        if ( !allowRelativeIRIs && iri.isRelative() )
            errorHandler.error("Relative IRI: " + iri, line, col) ;

        if ( iri.hasViolation(includeIRIwarnings) ) {
            Iterator<Violation> iter = iri.violations(includeIRIwarnings) ;

            boolean errorSeen = false ;
            boolean warningSeen = false ;

            // What to finally report.
            Violation vError = null ;
            Violation vWarning = null ;
            Violation xvSub = null ;

            for ( ; iter.hasNext() ; ) {
                Violation v = iter.next() ;
                int code = v.getViolationCode() ;
                boolean isError = v.isError() ;

                // IRI parsing ignored these.
                // When checking we output a higher level of warnings.
                // Ignore these. They seem to present even if switched off.
//                if ( code == Violation.LOWERCASE_PREFERRED
//                     || code == Violation.PERCENT_ENCODING_SHOULD_BE_UPPERCASE
//                     || code == Violation.SCHEME_PATTERN_MATCH_FAILED )
//                    continue ;

                // Anything we want to reprioritise?
                // [nothing at present]

                // Remember first error and first warning in case we want to order findings
                // (as use to happen when used with parsing).
                if ( isError ) {
                    errorSeen = true ;
                    if ( vError == null )
                        // Remember first error
                        vError = v ;
                } else {
                    warningSeen = true ;
                    if ( vWarning == null )
                        vWarning = v ;
                }

                String msg = v.getShortMessage() ;
                String iriStr = iri.toString() ;

                if ( isError )
                    // IRI errors are warnings - they got through syntax checks.
                    errorHandler.warning("Bad IRI: "+msg, line, col);
                else
                    errorHandler.warning("Not advised IRI: "+msg, line, col);
            }
        }
    }
}
