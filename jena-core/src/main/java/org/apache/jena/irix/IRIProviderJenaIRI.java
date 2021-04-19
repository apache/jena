/*
T * Licensed to the Apache Software Foundation (ASF) under one
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

package org.apache.jena.irix;

import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.jena.iri.*;

/**
 * Provider for {@link IRIx} using the {@code jena-iri} module.
 */
public class IRIProviderJenaIRI implements IRIProvider {

    // Notes:
    // jena-iri:IRI.create is silent.
    // jena-iri:IRI.construct throws errors.
    // jena-iri:IRI.resolve is the same as create

    public IRIProviderJenaIRI() { }

    /** {@link IRIx} implementation for the jena-iri provider. */
    public static class IRIxJena extends IRIx {
        private final IRI jenaIRI;

        private IRIxJena(String iriStr, IRI iri) {
            super(iri.toString());
            this.jenaIRI = iri;
        }

        @Override
        public boolean isAbsolute() {
            return jenaIRI.isAbsolute();
        }

        @Override
        public boolean isRelative() {
            return jenaIRI.isRelative();
        }

        @Override
        public boolean isReference() {
            if ( jenaIRI.isRootless() )
                return true;

            // isHierarchical.
            return jenaIRI.getScheme() != null;
                    // Unnecessary There is always a path even if it's "".
                    /* && iri.getRawPath() != null*/
        }

        @Override
        public boolean hasScheme(String scheme) {
            if ( jenaIRI.getScheme() == null )
                return false;
            return jenaIRI.getScheme().startsWith(scheme);
        }

        @Override
        public IRIx resolve(String other) {
            IRI iri2 = jenaIRI.resolve(other);
            IRIProviderJenaIRI.exceptions(iri2);
            return new IRIxJena(iri2.toString(), iri2);
        }

        @Override
        public IRIx resolve(IRIx other) {
            IRIxJena iriOther = (IRIxJena)other;
            IRI iri2 = jenaIRI.resolve(iriOther.jenaIRI);
            IRIProviderJenaIRI.exceptions(iri2);
            return new IRIxJena(iri2.toString(), iri2);
        }

        @Override
        public IRIx normalize() {
            IRI irin = jenaIRI.normalize(false);
            return new IRIxJena(irin.toString(), irin);
        }

        static private int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;
        @Override
        public IRIx relativize(IRIx other) {
            // Align of IRI3986 algorithm.
            if (jenaIRI.getRawQuery() != null )
                return null;
            IRIxJena iriOther = (IRIxJena)other;
            IRI iri2 = jenaIRI.relativize(iriOther.jenaIRI, relFlags);
            if ( iri2.equals(iriOther.jenaIRI))
                return null;
            IRIProviderJenaIRI.exceptions(iri2);
            return new IRIxJena(iri2.toString(), iri2);
        }

        @Override
        public IRI getImpl() {
            return jenaIRI;
        }

        @Override
        public int hashCode() {
            return Objects.hash(jenaIRI);
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            IRIxJena other = (IRIxJena)obj;
            return Objects.equals(jenaIRI, other.jenaIRI);
        }
    }

    @Override
    public IRIx create(String iriStr) throws IRIException {
        // "create" - does not throw exceptions
        IRI iriObj = iriFactory().create(iriStr);
        // errors and warnings.
        if ( STRICT_FILE && isFILE(iriObj) ) {
            if ( iriStr.startsWith("file://" ) && ! iriStr.startsWith("file:///") )
                throw new IRIException("file: URLs should start file:///");
        }
        if ( isUUID(iriObj) )
            checkUUID(iriObj, iriStr);
        exceptions(iriObj);
        return new IRIProviderJenaIRI.IRIxJena(iriStr, iriObj);
    }

    @Override
    public void check(String iriStr) throws IRIException {
        IRI iri = iriFactory().create(iriStr);
        exceptions(iri);
    }

    @Override
    public void strictMode(String scheme, boolean runStrict) {
        switch(scheme) {
            case "urn":
                STRICT_URN = runStrict;
                break;
            case "file":
                STRICT_FILE = runStrict;
                break;
            case "http":
                STRICT_HTTP = runStrict;
            default:
        }
    }

    @Override
    public boolean isStrictMode(String scheme) {
        switch(scheme) {
            case "urn":
                return STRICT_URN;
            case "file":
                return STRICT_FILE;
            case "http":
                return STRICT_HTTP;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    private static IRI baseIRI = null;

    // -----------------------------

    private static IRIFactory iriFactory() {
        return iriFactoryInst;
    }

    // Parser settings.
    private static final IRIFactory iriFactoryInst = SetupJenaIRI.setupCheckerIRIFactory();

    private static boolean STRICT_HTTP = true;
    private static boolean STRICT_URN  = true;
    private static boolean STRICT_FILE = true;

    private static final boolean showExceptions = true;
    // Should be "false" in a release - this is an assist for development checking.
    private static final boolean includeWarnings = false;

    private static IRI exceptions(IRI iri) {
        if (!showExceptions)
            return iri;
        if (!iri.hasViolation(includeWarnings))
            return iri;
        // Some error/warnings are scheme dependent.
        Iterator<Violation> vIter = iri.violations(includeWarnings);
        while(vIter.hasNext()) {
            Violation v = vIter.next();

            int code = v.getViolationCode() ;
            // Filter codes.
            // Global settings below; this section is for conditional filtering.
            switch(code) {
                case Violation.PROHIBITED_COMPONENT_PRESENT:
                    // Allow "u:p@" when non-strict.
                    // Jena3 compatibility.
                    if ( isHTTP(iri) && ! STRICT_HTTP && v.getComponent() == IRIComponents.USER )
                        continue;
                    break;
                case Violation.SCHEME_PATTERN_MATCH_FAILED:
                    if ( isURN(iri) && ! STRICT_URN )
                        continue;
                    if ( isFILE(iri) )
                        continue;
                    break;
                case Violation.REQUIRED_COMPONENT_MISSING:
                    // jena-iri handling of "file:" URIs is only for (an interpretation of) RFC 1738.
                    // RFC8089 allows relative file URIs and a wider use of characters.
                    if ( isFILE(iri) )
                        continue;
            }
            // First error.
            String msg = v.getShortMessage();
            throw new IRIException(msg);
        }
        return iri;
    }

    // HTTP and HTTPS
    private static boolean isHTTP(IRI iri) {
        return "http".equalsIgnoreCase(iri.getScheme()) || "https".equalsIgnoreCase(iri.getScheme());
    }

    private static boolean isURN(IRI iri)  { return "urn".equalsIgnoreCase(iri.getScheme()); }
    private static boolean isFILE(IRI iri) { return "file".equalsIgnoreCase(iri.getScheme()); }

    // Checks trailing part of URI.
    // Works on "urn:" and "urn:uuid:".
    private static Pattern UUID_PATTERN = Pattern.compile(":[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private boolean isUUID(IRI iri) {
        if ( "uuid".equalsIgnoreCase(iri.getScheme()) )
                return true;
        return iri.getRawPath().startsWith("uuid:");
    }

    private void checkUUID(IRI iriObj, String original) {
        if ( iriObj.getRawFragment() != null )
            throw new IRIException("Fragment used with UUID");
        if ( iriObj.getRawQuery() != null )
            throw new IRIException("Query used with UUID");
        boolean matches = UUID_PATTERN.matcher(original).matches();
        if ( !matches )
            throw new IRIException("Not a valid UUID string: "+original);
    }
}