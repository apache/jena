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
import java.util.function.BiConsumer;
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
            String iriScheme = scheme();
            if ( iriScheme == null )
                return false;
            return iriScheme.equalsIgnoreCase(scheme);
        }

        @Override
        public String scheme() {
            return jenaIRI.getScheme();
        }

        @Override
        public IRIx resolve(String other) {
            IRI iri2 = jenaIRI.resolve(other);
            return newIRIxJena(iri2);
        }

        @Override
        public IRIx resolve(IRIx other) {
            IRIxJena iriOther = (IRIxJena)other;
            IRI iri2 = jenaIRI.resolve(iriOther.jenaIRI);
            return newIRIxJena(iri2);
        }

        @Override
        public IRIx normalize() {
            IRI irin = jenaIRI.normalize(false);
            return new IRIxJena(irin.toString(), irin);
        }

        // The default setting in previous Jena.
        static private int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.ABSOLUTE | IRIRelativize.CHILD | IRIRelativize.PARENT;
        @Override
        public IRIx relativize(IRIx other) {
            if ( jenaIRI.getRawQuery() != null )
                return null;
            IRIxJena iriOther = (IRIxJena)other;
            IRI iri2 = jenaIRI.relativize(iriOther.jenaIRI, relFlags);
            if ( iri2.equals(iriOther.jenaIRI))
                return null;
            return newIRIxJena(iri2);
        }

        @Override
        public IRI getImpl() {
            return jenaIRI;
        }

        @Override
        public boolean hasViolations() {
            return jenaIRI.hasViolation(false);
        }

        @Override
        public void handleViolations(BiConsumer<Boolean, String> handler) {
            jenaIRI.violations(false)
                   .forEachRemaining(v->handler.accept(v.isError(), v.getShortMessage()));
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

    private static IRIxJena newIRIxJena(IRI iri2) {
        String iriStr2 = iri2.toString();
        return newIRIxJena(iri2, iriStr2);
    }

    private static IRIxJena newIRIxJena(IRI iri2, String iriStr2) {
        IRIProviderJenaIRI.exceptions(iri2, iriStr2);
        return new IRIxJena(iriStr2, iri2);
    }

    @Override
    public IRIx create(String iriStr) throws IRIException {
        // "create" - does not throw exceptions
        IRI iriObj = iriFactory().create(iriStr);
        return newIRIxJena(iriObj, iriStr);
    }

    @Override
    public void check(String iriStr) throws IRIException {
        IRI iri = iriFactory().create(iriStr);
        exceptions(iri, iriStr);
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

    private static IRI exceptions(IRI iri, String iriStr) {
        if ( iriStr == null )
            iriStr = iri.toString();

        // Additional checks

        // errors and warnings.
        if ( STRICT_FILE && isFILE(iri) ) {
            if ( iriStr.startsWith("file://" ) && ! iriStr.startsWith("file:///") )
                throw new IRIException("file: URLs should start file:///: <"+iriStr+">");
        } else if ( isUUID(iri, iriStr) ) {
            checkUUID(iri, iriStr);
        } else if ( isURNUUID(iri, iriStr) ) {
            checkURNUUID(iri, iriStr);
        }

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
            // See also Checker.iriViolations for WARN filtering.
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
            // Signal first error.
            String msg = v.getShortMessage();
            throw new IRIException(msg);
        }
        return iri;
    }

    // HTTP and HTTPS
    private static boolean isHTTP(IRI iri) {
        return "http".equalsIgnoreCase(iri.getScheme())
            || "https".equalsIgnoreCase(iri.getScheme());
    }

    private static boolean isURN(IRI iri)  { return "urn".equalsIgnoreCase(iri.getScheme()); }
    private static boolean isFILE(IRI iri) { return "file".equalsIgnoreCase(iri.getScheme()); }

    private static boolean isUUID(IRI iri, String iriStr) {
        // Ignore case
        return iriStr.regionMatches(true, 0, "uuid:", 0, "uuid:".length());
    }

    private static boolean isURNUUID(IRI iri, String iriStr) {
        // Ignore case
        return iriStr.regionMatches(true, 0, "urn:uuid:", 0, "urn:uuid:".length());
    }

    // ---- uuid:
    // UUID match, no anchors or URI scheme.
    private static String UUID_BASE = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static String UUID_REGEXP = "^uuid:"+UUID_BASE+"$";
    private static Pattern UUID_PATTERN = Pattern.compile(UUID_REGEXP, Pattern.CASE_INSENSITIVE);

    private static void checkUUID(IRI iriObj, String original) {
        if ( iriObj.hasViolation(true) )
            // Already has problems.
            return;
        // jena-iri does not have UUID checks.
        // Unfortunately, these tests are check/no-check sensitive.
        if ( iriObj.getRawFragment() != null )
            throw new IRIException("Fragment used with uuid:");
        if ( iriObj.getRawQuery() != null )
            throw new IRIException("Query used with uuid:");
        boolean matches = UUID_PATTERN.matcher(original).matches();
        if ( !matches )
            throw new IRIException("Not a valid UUID string: "+original);
    }


    // ---- urn:uuid:
    // RFC 8141 added the possibility for r-component, q-component (combined
    // into the URI query string) and f-component (restricted fragment). This
    // regexp has a weak test for r/q/f. It does not check the character
    // limitations to ASCII on r/q/f

    //private static String A2Z = "[0-9a-z]";

    // Non-strict regexp: Any order r- and q-compoments, UCSchars.
    private static String URN_UUID_REGEXP_LAX = "^urn:uuid:"+UUID_BASE+"(?:(?:\\?\\+.|\\?=.|#).*)?$";

    // Strict regex for urn:uuid
    //    Only ASCII.
    //  pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
    //  pct-encoded   = "%" HEXDIG HEXDIG
    //  unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
    //  iunreserved   = ALPHA / DIGIT / "-" / "." / "_" / "~" / ucschar
    //  reserved      = gen-delims / sub-delims
    //  gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
    //  sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
    //                / "*" / "+" / "," / ";" / "="
    // Not:
    //  ipchar        = iunreserved / pct-encoded / sub-delims / ":" / "@"
    //                = ipchar / ucschar

//    ucschar        = %xA0-D7FF / %xF900-FDCF / %xFDF0-FFEF
//            / %x10000-1FFFD / %x20000-2FFFD / %x30000-3FFFD
//            / %x40000-4FFFD / %x50000-5FFFD / %x60000-6FFFD
//            / %x70000-7FFFD / %x80000-8FFFD / %x90000-9FFFD
//            / %xA0000-AFFFD / %xB0000-BFFFD / %xC0000-CFFFD
//            / %xD0000-DFFFD / %xE1000-EFFFD

    // "(?:  )" is a non-binding group.
    private static String PCT = "(?:%[a-f][a-f])";

    // As contents of "[]" used in PCHAR
    private static String UNRESERVED = "-0-9a-z._~";
    // Or use \p{IsAlphabetic}
    private static String UCSCHAR = "\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";
            /*
            / %x10000-1FFFD / %x20000-2FFFD / %x30000-3FFFD
            / %x40000-4FFFD / %x50000-5FFFD / %x60000-6FFFD
            / %x70000-7FFFD / %x80000-8FFFD / %x90000-9FFFD
            / %xA0000-AFFFD / %xB0000-BFFFD / %xC0000-CFFFD
            / %xD0000-DFFFD / %xE1000-EFFFD
           */
    // private       = %xE000-F8FF / %xF0000-FFFFD / %x100000-10FFFD
    //private static String IPRIVATE
    private static String IUNRESERVED = UNRESERVED+UCSCHAR;

    //private static String GEN_DELIMS = ":/\\?#\\[\\]@";
    private static String SUB_DELIMS = "!\\$&'\\(\\)\\*\\+,;=";
    // Switch IUNRESERVED / UNRESERVED
    private static String PCHARS1 = UNRESERVED+SUB_DELIMS+":"+"@";
    private static String PCHAR = "(?:(?:["+PCHARS1+"]|"+PCT+"))";

    private static String URN_COMP_X = "/\\?";
    private static String URN_RQ_COMP_CHAR = PCHAR+URN_COMP_X;
    private static String URN_R_COMP = "(?:\\?\\+["+URN_RQ_COMP_CHAR+"]+)?";
    private static String URN_Q_COMP = "(?:\\?=["+URN_RQ_COMP_CHAR+"]+)?";
    private static String URN_F_COMP = "(?:#["+PCHAR+"]*)?";
    private static String URN_UUID_REGEXP = "^urn:uuid:"+UUID_BASE+URN_R_COMP+URN_Q_COMP+URN_F_COMP+"$";

    private static Pattern URN_UUID_PATTERN = Pattern.compile(URN_UUID_REGEXP, Pattern.CASE_INSENSITIVE);

    private static void checkURNUUID(IRI iriObj, String original) {
        if ( iriObj.hasViolation(true) )
            // Already has problems.
            return;
        boolean matches = URN_UUID_PATTERN.matcher(original).matches();
        if ( !matches )
            throw new IRIException("Not a valid UUID string: "+original);
    }
}