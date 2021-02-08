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

import java.io.PrintStream;
import java.util.Iterator;

import org.apache.jena.iri.*;
import org.apache.jena.iri.impl.PatternCompiler;

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
    static class IRIxJena extends IRIx {
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
            IRIxJena iriOther = (IRIxJena)other;
            IRI iri2 = jenaIRI.relativize(iriOther.jenaIRI, relFlags);
            if ( iri2.equals(iriOther.jenaIRI))
                return null;
            IRIProviderJenaIRI.exceptions(iri2);
            return new IRIxJena(iri2.toString(), iri2);
        }
    }

    @Override
    public IRIx create(String iriStr) throws IRIException {
        // "create" - does not throw exceptions
        IRI iriObj = iriFactory().create(iriStr);
        // errors and warnings.
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
            default:
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    private static IRI baseIRI = null;

    // -----------------------------

    static IRIFactory iriFactory() {
        return iriFactoryInst;
    }

    private static boolean STRICT_URN  = true;
    private static boolean STRICT_FILE = true;
    private static final boolean showExceptions = true;

    private static IRI exceptions(IRI iri) {
        if (!showExceptions)
            return iri;
        if (!iri.hasViolation(false))
            return iri;
        // Exclude certain errors which don't seem to be able to be switched off.
        Iterator<Violation> vIter = iri.violations(false);
        while(vIter.hasNext()) {
            Violation v = vIter.next();
            int code = v.getViolationCode() ;
            // Filter codes.
            if ( code == Violation.PERCENT_ENCODING_SHOULD_BE_UPPERCASE)
                continue;

            if ( code == Violation.SCHEME_PATTERN_MATCH_FAILED && isURN(iri) && ! STRICT_URN )
                continue;

            if ( code == Violation.REQUIRED_COMPONENT_MISSING && isFILE(iri) )
                // jena-iri implements the earlier RFCs, not RFC8089 which adds "file:local"
                continue;
            //break to retain errors
            switch(code) {
                //case Violation.LOWERCASE_PREFERRED:
                case Violation.PERCENT_ENCODING_SHOULD_BE_UPPERCASE:
                    continue;

                case Violation.SCHEME_PATTERN_MATCH_FAILED:
                    if ( isURN(iri) && ! STRICT_URN )
                        continue;
                    break;
                case Violation.REQUIRED_COMPONENT_MISSING:
                    if ( isFILE(iri) && ! STRICT_FILE )
                        continue;
                default:
            }
            String msg = iri.violations(false).next().getShortMessage();
            throw new IRIException(msg);
        }
        return iri;
    }

    private static boolean isURN(IRI iri)  { return "urn".equalsIgnoreCase(iri.getScheme()); }
    private static boolean isFILE(IRI iri) { return "file".equalsIgnoreCase(iri.getScheme()); }

    private static final boolean   ShowResolverSetup = false;

    private static final IRIFactory iriFactoryInst = new IRIFactory();
    static {
        iriFactoryInst.useSpecificationIRI(true);
        iriFactoryInst.useSchemeSpecificRules("*", true);

        // Allow relative references for file: URLs.
        iriFactoryInst.setSameSchemeRelativeReferences("file");

        // Convert "SHOULD" to warning (default is "error").
        // iriFactory.shouldViolation(false,true);

        if ( ShowResolverSetup ) {
            System.out.println("---- Default settings ----");
            printSetting(iriFactoryInst);
        }

        // Accept any scheme.
        setErrorWarning(iriFactoryInst, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.NON_INITIAL_DOT_SEGMENT, false, false);

        setErrorWarning(iriFactoryInst, ViolationCodes.LOWERCASE_PREFERRED, false, true);
        setErrorWarning(iriFactoryInst, ViolationCodes.REQUIRED_COMPONENT_MISSING, true, true);

        // Choices: setting here does not seem to have any effect. See CheckerIRI and IRIProviderJena for filtering.
//      //setErrorWarning(iriFactoryInst, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE, false, true);
//      //setErrorWarning(iriFactoryInst, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED, false, true);

        // NFC tests are not well understood by general developers and these cause confusion.
        // See JENA-864
        // NFC is in RDF 1.1 so do test for that.
        // https://www.w3.org/TR/rdf11-concepts/#section-IRIs
        // Leave switched on as a warning.
        //setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFC,  false, false);

        // NFKC is not mentioned in RDF 1.1. Switch off.
        setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFKC, false, false);

        // ** Applies to various unicode blocks.
        // The set of legal characters depends on the Java version.
        // If not set, this causes test failures in Turtle and Trig eval tests.
        // "Any" unicode codepoint.
        setErrorWarning(iriFactoryInst, ViolationCodes.COMPATIBILITY_CHARACTER, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER, false, false);

        if ( ShowResolverSetup ) {
            System.out.println("---- After initialization ----");
            printSetting(iriFactoryInst);
        }
    }

    /** Set the error/warning state of a violation code.
     * @param factory   IRIFactory
     * @param code      ViolationCodes constant
     * @param isError   Whether it is to be treated an error.
     * @param isWarning Whether it is to be treated a warning.
     */
    private static void setErrorWarning(IRIFactory factory, int code, boolean isError, boolean isWarning) {
        factory.setIsWarning(code, isWarning);
        factory.setIsError(code, isError);
    }

    private static void printSetting(IRIFactory factory) {
        PrintStream ps = System.out;
        printErrorWarning(ps, factory, ViolationCodes.UNREGISTERED_IANA_SCHEME);
        printErrorWarning(ps, factory, ViolationCodes.NON_INITIAL_DOT_SEGMENT);
        printErrorWarning(ps, factory, ViolationCodes.NOT_NFC);
        printErrorWarning(ps, factory, ViolationCodes.NOT_NFKC);
        printErrorWarning(ps, factory, ViolationCodes.UNWISE_CHARACTER);
        printErrorWarning(ps, factory, ViolationCodes.UNDEFINED_UNICODE_CHARACTER);
        printErrorWarning(ps, factory, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER);
        printErrorWarning(ps, factory, ViolationCodes.COMPATIBILITY_CHARACTER);
        printErrorWarning(ps, factory, ViolationCodes.LOWERCASE_PREFERRED);
        printErrorWarning(ps, factory, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE);
        printErrorWarning(ps, factory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED);
        ps.println();
    }

    private static void printErrorWarning(PrintStream ps, IRIFactory factory, int code) {
        String x = PatternCompiler.errorCodeName(code);
        ps.printf("%-40s : E:%-5s W:%-5s\n", x, factory.isError(code), factory.isWarning(code));
    }

//  // Jena 3.17.0
//  // Accept any scheme.
//  setErrorWarning(iriFactoryInst, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);
//
//  // These are a warning from jena-iri motivated by problems in RDF/XML and also internal processing by IRI
//  // (IRI.relativize).
//  // The IRI is valid and does correct resolve when relative.
//  setErrorWarning(iriFactoryInst, ViolationCodes.NON_INITIAL_DOT_SEGMENT, false, false);
//
//  // Turn off?? (ignored in CheckerIRI.iriViolations anyway).
//  // setErrorWarning(iriFactory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED, false, false);
//
//  // Choices
//  setErrorWarning(iriFactoryInst, ViolationCodes.LOWERCASE_PREFERRED, false, true);
//  //setErrorWarning(iriFactoryInst, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE, false, true);
//
//  // NFC tests are not well understood by general developers and these cause confusion.
//  // See JENA-864
//  // NFC is in RDF 1.1 so do test for that.
//  // https://www.w3.org/TR/rdf11-concepts/#section-IRIs
//  // Leave switched on as a warning.
//  //setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFC,  false, false);
//
//  // NFKC is not mentioned in RDF 1.1. Switch off.
//  setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFKC, false, false);
//
//  // ** Applies to various unicode blocks.
//  setErrorWarning(iriFactoryInst, ViolationCodes.COMPATIBILITY_CHARACTER, false, false);
//  setErrorWarning(iriFactoryInst, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, false);
//  // The set of legal characters depends on the Java version.
//  // If not set, this causes test failures in Turtle and Trig eval tests.
//  setErrorWarning(iriFactoryInst, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER, false, false);


// Jena3: IRIResolver.iriFactory settings:
//    0 ILLEGAL_CHARACTER                        : E:true  W:false
//    1 PERCENT_ENCODING_SHOULD_BE_UPPERCASE     : E:true  W:false
//    2 SUPERFLUOUS_NON_ASCII_PERCENT_ENCODING   : E:true  W:false
//    3 SUPERFLUOUS_ASCII_PERCENT_ENCODING       : E:true  W:false
//    4 UNWISE_CHARACTER                         : E:true  W:false
//    5 CONTROL_CHARACTER                        : E:true  W:false
//    6 NON_XML_CHARACTER                        : E:false W:false
//    7 DISCOURAGED_XML_CHARACTER                : E:false W:false
//    8 NON_INITIAL_DOT_SEGMENT                  : E:false W:false
//    9 EMPTY_SCHEME                             : E:true  W:false
//   10 SCHEME_MUST_START_WITH_LETTER            : E:true  W:false
//   11 LOWERCASE_PREFERRED                      : E:false W:true
//   12 PORT_SHOULD_NOT_BE_EMPTY                 : E:true  W:false
//   13 DEFAULT_PORT_SHOULD_BE_OMITTED           : E:true  W:false
//   14 PORT_SHOULD_NOT_BE_WELL_KNOWN            : E:true  W:false
//   15 PORT_SHOULD_NOT_START_IN_ZERO            : E:true  W:false
//   16 BIDI_FORMATTING_CHARACTER                : E:true  W:false
//   17 WHITESPACE                               : E:true  W:false
//   18 DOUBLE_WHITESPACE                        : E:true  W:false
//   19 NOT_XML_SCHEMA_WHITESPACE                : E:true  W:false
//   20 DOUBLE_DASH_IN_REG_NAME                  : E:false W:false
//   21 SCHEME_INCLUDES_DASH                     : E:false W:false
//   22 NON_URI_CHARACTER                        : E:false W:false
//   23 PERCENT_20                               : E:false W:false
//   24 PERCENT                                  : E:false W:false
//   25 IP_V6_OR_FUTURE_ADDRESS_SYNTAX           : E:true  W:false
//   26 IPv6ADDRESS_SHOULD_BE_LOWERCASE          : E:true  W:false
//   27 IP_V4_OCTET_RANGE                        : E:true  W:false
//   28 NOT_DNS_NAME                             : E:true  W:false
//   29 USE_PUNYCODE_NOT_PERCENTS                : E:true  W:false
//   30 ILLEGAL_PERCENT_ENCODING                 : E:true  W:false
//   31 ACE_PREFIX                               : E:false W:false
//   32 LONE_SURROGATE                           : E:false W:false
//   33 DNS_LABEL_DASH_START_OR_END              : E:true  W:false
//   34 BAD_IDN_UNASSIGNED_CHARS                 : E:true  W:false
//   35 BAD_IDN                                  : E:true  W:false
//   36 HAS_PASSWORD                             : E:true  W:false
//   37 DISCOURAGED_IRI_CHARACTER                : E:true  W:false
//   38 BAD_BIDI_SUBCOMPONENT                    : E:true  W:false
//   39 DNS_LENGTH_LIMIT                         : E:false W:false
//   40 DNS_LABEL_LENGTH_LIMIT                   : E:false W:false
//   41 NOT_UTF8_ESCAPE                          : E:false W:false
//   42 NOT_UTF8_ESCAPE_IN_HOST                  : E:false W:false
//   43 BAD_DOT_IN_IDN                           : E:false W:false
//   44 UNREGISTERED_IANA_SCHEME                 : E:false W:false
//   45 UNREGISTERED_NONIETF_SCHEME_TREE         : E:true  W:false
//   46 NOT_NFC                                  : E:true  W:false
//   47 NOT_NFKC                                 : E:false W:false
//   48 DEPRECATED_UNICODE_CHARACTER             : E:true  W:false
//   49 UNDEFINED_UNICODE_CHARACTER              : E:false W:false
//   50 PRIVATE_USE_CHARACTER                    : E:true  W:false
//   51 UNICODE_CONTROL_CHARACTER                : E:true  W:false
//   52 UNASSIGNED_UNICODE_CHARACTER             : E:false W:false
//   53 MAYBE_NOT_NFC                            : E:false W:false
//   54 MAYBE_NOT_NFKC                           : E:false W:false
//   55 UNICODE_WHITESPACE                       : E:true  W:false
//   56 COMPATIBILITY_CHARACTER                  : E:false W:false
//   57 REQUIRED_COMPONENT_MISSING               : E:false W:false
//   58 PROHIBITED_COMPONENT_PRESENT             : E:false W:false
//   59 SCHEME_REQUIRES_LOWERCASE                : E:false W:false
//   60 SCHEME_PREFERS_LOWERCASE                 : E:false W:false
//   61 SCHEME_PATTERN_MATCH_FAILED              : E:false W:false
//   62 QUERY_IN_LEGACY_SCHEME                   : E:false W:false
}
