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

package org.apache.jena.iri3986.provider;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIProvider;
import org.apache.jena.irix.IRIx;
import org.apache.jena.rfc3986.*;

public class IRIProvider3986 implements IRIProvider {

    /**
     * IRIProvider3986
     */
    public IRIProvider3986() { }

    // Adjust severities (fatal/error/warning/ignore) JenaSeveritySettings

    public static class IRIx3986 extends IRIx {
        private final IRI3986 iri;
        private IRIx3986(String iriStr, IRI3986 iri) {
            super(iri.toString());
            this.iri = iri;
        }

        @Override
        public boolean isAbsolute() {
            return iri.isAbsolute();
        }

        @Override
        public boolean isRelative() {
            return iri.isRelative();
        }

        @Override
        public boolean isReference() {
            if ( iri.isRootless() )
                return true;
            // isHierarchical.
            // There is always a path even if it's ""
            return iri.hasScheme();
        }

        @Override
        public boolean hasScheme(String scheme) {
            String iriScheme = scheme();
            if ( iriScheme == null )
                return false;
            return iriScheme.equalsIgnoreCase(scheme);
        }

        @Override
        public String scheme() {
            return iri.scheme();
        }

        @Override
        public IRIx resolve(String other) {
            // create3986() - checks syntax, and errors if in strict mode.
            IRI3986 iriOther = create3986(other);
            IRI3986 iri2 = this.iri.resolve(iriOther);
            return newIRIx(iri2);
        }

        @Override
        public IRIx resolve(IRIx other) {
            IRIx3986 iriOther = (IRIx3986)other;
            IRI3986 iri2 = this.iri.resolve(iriOther.iri);
            return newIRIx(iri2);
        }

        @Override
        public IRIx normalize() {
            IRI3986 iri3986 = RFC3986.normalize(iri);
            return newIRIx(iri3986);
        }

        @Override
        public IRIx relativize(IRIx other) {
            IRIx3986 iriOther = (IRIx3986)other;
            IRI3986 iri2 = this.iri.relativize(iriOther.iri);
            //violations(iri2);
            return ( iri2 == null ) ? null : newIRIx(iri2);
        }

        @Override
        public IRI3986 getImpl() {
            return iri;
        }

        /**
         * Return true if there are no "ignore items".
         */
        @Override
        public boolean hasViolations() {
            return iri.hasViolations(Severity.IGNORE);
        }

        @Override
        public void handleViolations(BiConsumer<Boolean, String> handler) {
            iri.forEachViolation(v->{
                // Caution: this uses the system-wide severity map.
                Severity severity = Violations.getSeverity(v.issue());
                String msg = v.message();
                switch(severity) {
                    case INVALID -> throw new IRIException(msg);
                    case ERROR  ->  handler.accept(TRUE, msg);
                    case WARNING -> handler.accept(FALSE, msg);
                    case IGNORE ->  {}
                    default ->      {}
                }
            });
        }

        @Override
        public int hashCode() {
            return Objects.hash(iri);
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            IRIx3986 other = (IRIx3986)obj;
            return Objects.equals(iri, other.iri);
        }
    }

    /** Create a new IRIx from an {@link IRI3986}. */
    private static IRIx newIRIx(IRI3986 iri) {
        return newIRIx(iri.toString(), iri);
    }

    /** Create a new IRIx from an {@link IRI3986}. */
    private static IRIx newIRIx(String iriStr, IRI3986 iri) {
        return new IRIProvider3986.IRIx3986(iriStr, iri);
    }

    @Override
    public IRIx create(String iriStr) throws IRIException {
        IRI3986 iri = create3986(iriStr);
        return newIRIx(iriStr, iri);
    }

    @Override
    public void check(String iriStr) throws IRIException {
        IRI3986 iri = create3986(iriStr);
    }

    /**
     * All creation of IRI3986 objects should go via this function.
     * {@link IRI3986#createAny} parses and records violations,
     * including syntax errors, but does not throws exceptions.
     * Specifically, it does not throw non-Jena exception
     * {@link org.apache.jena.rfc3986.IRIParseException}.
     * This code then decides how to treat violations.
     */
    private static IRI3986 create3986(String iriStr) {
        // This is not "IRIx.createAny" which is only a
        // placeholder for its string argument.
        IRI3986 iri = IRI3986.createAny(iriStr);
        violationsOnCreation(iri);
        return iri;
    }

    /** Execute policies IRI3986 for Jena. */
    private static void violationsOnCreation(IRI3986 iri) {
        if ( iri.hasViolations() )
            violationsOnCreation(iri, Violations.severities());
    }

    /** Execute policies IRI3986 for Jena. */
    private static void violationsOnCreation(IRI3986 iri, SeverityMap severityMap) {
        iri.forEachViolation(v->{
            Issue issue = v.issue();
            boolean isStrict = Issues.isStrict(issue);
            // Any special cases.
//            switch(issue) {
//                case urn_nid, urn_nss, urn_uuid_bad_pattern, uuid_bad_pattern:
//                    ...
//                case file_bad_form:
//                    break;
//                case ParseError:
//                    throw new IRIException(v.message());
//                default :
//                    break;
//            }
            Severity severity = severityMap.get(issue);
            if ( isStrict ) {
                switch( severity ) {
                    case INVALID, ERROR -> throw new IRIException(v.message());
                    case WARNING, IGNORE -> {}
                }
            } else {
                switch( severity ) {
                    case INVALID -> throw new IRIException(v.message());
                    case ERROR, WARNING, IGNORE -> {}
                }
            }
        });
    }

    @Override
    public void strictMode(String schemeName, boolean strictness) {
        Objects.requireNonNull(schemeName);
        IssueGroup issueGroup = Issues.getScheme(schemeName);
        if ( issueGroup == null )
            // No such scheme.
            throw new IllegalArgumentException("Scheme name '"+schemeName+"' not recognized as an issues group");
        Issues.setStrictness(issueGroup, strictness);
    }

    @Override
    public boolean isStrictMode(String schemeName) {
        IssueGroup issueGroup = Issues.getScheme(schemeName);
        if ( issueGroup == null )
            // No such scheme.
            return true;
        return Issues.isStrict(issueGroup);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
