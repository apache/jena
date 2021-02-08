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

import java.io.PrintStream;

import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.ViolationCodes;
import org.apache.jena.iri.impl.PatternCompiler;

/** IRI handling */
public abstract class IRIResolver
{
    // ---- System-wide IRI Factory.

    private static boolean         showExceptions    = true;

    private static final boolean   ShowResolverSetup = false;

    /**
     * The IRI checker setup, focused on parsing and languages.
     * This is a clean version of jena-iri {@link IRIFactory#iriImplementation()}
     * modified to allow unregistered schemes and allow {@code <file:relative>} IRIs.
     */
    public static IRIFactory iriFactory() {
        return iriFactoryInst;
    }


    /**
     * An IRIFactory with more detailed warnings.
     */
    public static IRIFactory iriCheckerFactory() {
        return iriCheckerInst;
    }

    private static final IRIFactory iriCheckerInst = new IRIFactory();
    static {
        // These two are from IRIFactory.iriImplementation() ...
        iriCheckerInst.useSpecificationIRI(true);
        iriCheckerInst.useSchemeSpecificRules("*", true);
        // Allow relative references for file: URLs.
        iriCheckerInst.setSameSchemeRelativeReferences("file");

        setErrorWarning(iriCheckerInst, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);
        setErrorWarning(iriCheckerInst, ViolationCodes.NON_INITIAL_DOT_SEGMENT, false, false);

        // Choices: setting here does not seem to have any effect. See CheckerIRI and IRIProviderJena.
        setErrorWarning(iriCheckerInst, ViolationCodes.LOWERCASE_PREFERRED, false, true);
        setErrorWarning(iriCheckerInst, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE, false, true);
        setErrorWarning(iriCheckerInst, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED, false, true);

        //setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFC,  false, false);
        // NFKC is not mentioned in RDF 1.1. Switch off.
        setErrorWarning(iriCheckerInst, ViolationCodes.NOT_NFKC, false, false);

        // ** Applies to various unicode blocks.
        setErrorWarning(iriCheckerInst, ViolationCodes.COMPATIBILITY_CHARACTER, false, true);
        setErrorWarning(iriCheckerInst, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, true);
        // The set of legal characters depends on the Java version.
        // If not set, this causes test failures in Turtle and Trig eval tests.
        setErrorWarning(iriCheckerInst, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER, false, false);

        if ( ShowResolverSetup ) {
            System.out.println("---- After initialization (checker) ----");
            printSetting(iriCheckerInst);
        }

    }

    // Previous IRI processing (Jena3).
    // To be removed eventually.

    private static final IRIFactory iriFactoryInst = new IRIFactory();
    static {
        // These two are from IRIFactory.iriImplementation() ...
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

        // Jena 3.17.0 setup.
        // Accept any scheme.
        setErrorWarning(iriFactoryInst, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);

        // These are a warning from jena-iri motivated by problems in RDF/XML and also internal processing by IRI
        // (IRI.relativize).
        // The IRI is valid and does correct resolve when relative.
        setErrorWarning(iriFactoryInst, ViolationCodes.NON_INITIAL_DOT_SEGMENT, false, false);

        // Choices: setting here does not seem to have any effect. See CheckerIRI and IRIProviderJena.
        //setErrorWarning(iriFactoryInst, ViolationCodes.LOWERCASE_PREFERRED, false, true);
        //setErrorWarning(iriFactoryInst, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE, false, true);
        //setErrorWarning(iriFactoryInst, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED, false, true);

        // NFC tests are not well understood by general developers and these cause confusion.
        // See JENA-864
        // NFC is in RDF 1.1 so do test for that.
        // https://www.w3.org/TR/rdf11-concepts/#section-IRIs
        // Leave switched on as a warning.
        //setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFC,  false, false);

        // NFKC is not mentioned in RDF 1.1. Switch off.
        setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFKC, false, false);

        // ** Applies to various unicode blocks.
        setErrorWarning(iriFactoryInst, ViolationCodes.COMPATIBILITY_CHARACTER, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, false);
        // The set of legal characters depends on the Java version.
        // If not set, this causes test failures in Turtle and Trig eval tests.
        setErrorWarning(iriFactoryInst, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER, false, false);

        setErrorWarning(iriFactoryInst, ViolationCodes.SUPERFLUOUS_NON_ASCII_PERCENT_ENCODING, false, true);
        setErrorWarning(iriFactoryInst, ViolationCodes.SUPERFLUOUS_ASCII_PERCENT_ENCODING, false, true);


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

    // For reference: Violations and their default settings.
//  0 ILLEGAL_CHARACTER                        : E:true  W:false
//  1 PERCENT_ENCODING_SHOULD_BE_UPPERCASE     : E:true  W:false
//  2 SUPERFLUOUS_NON_ASCII_PERCENT_ENCODING   : E:true  W:false
//  3 SUPERFLUOUS_ASCII_PERCENT_ENCODING       : E:true  W:false
//  4 UNWISE_CHARACTER                         : E:true  W:false
//  5 CONTROL_CHARACTER                        : E:true  W:false
//  8 NON_INITIAL_DOT_SEGMENT                  : E:true  W:false
//  9 EMPTY_SCHEME                             : E:true  W:false
// 10 SCHEME_MUST_START_WITH_LETTER            : E:true  W:false
// 11 LOWERCASE_PREFERRED                      : E:true  W:false
// 12 PORT_SHOULD_NOT_BE_EMPTY                 : E:true  W:false
// 13 DEFAULT_PORT_SHOULD_BE_OMITTED           : E:true  W:false
// 14 PORT_SHOULD_NOT_BE_WELL_KNOWN            : E:true  W:false
// 15 PORT_SHOULD_NOT_START_IN_ZERO            : E:true  W:false
// 16 BIDI_FORMATTING_CHARACTER                : E:true  W:false
// 17 WHITESPACE                               : E:true  W:false
// 18 DOUBLE_WHITESPACE                        : E:true  W:false
// 19 NOT_XML_SCHEMA_WHITESPACE                : E:true  W:false
// 25 IP_V6_OR_FUTURE_ADDRESS_SYNTAX           : E:true  W:false
// 26 IPv6ADDRESS_SHOULD_BE_LOWERCASE          : E:true  W:false
// 27 IP_V4_OCTET_RANGE                        : E:true  W:false
// 28 NOT_DNS_NAME                             : E:true  W:false
// 29 USE_PUNYCODE_NOT_PERCENTS                : E:true  W:false
// 30 ILLEGAL_PERCENT_ENCODING                 : E:true  W:false
// 33 DNS_LABEL_DASH_START_OR_END              : E:true  W:false
// 34 BAD_IDN_UNASSIGNED_CHARS                 : E:true  W:false
// 35 BAD_IDN                                  : E:true  W:false
// 36 HAS_PASSWORD                             : E:true  W:false
// 37 DISCOURAGED_IRI_CHARACTER                : E:true  W:false
// 38 BAD_BIDI_SUBCOMPONENT                    : E:true  W:false
// 44 UNREGISTERED_IANA_SCHEME                 : E:true  W:false
// 45 UNREGISTERED_NONIETF_SCHEME_TREE         : E:true  W:false
// 46 NOT_NFC                                  : E:true  W:false
// 47 NOT_NFKC                                 : E:true  W:false
// 48 DEPRECATED_UNICODE_CHARACTER             : E:true  W:false
// 49 UNDEFINED_UNICODE_CHARACTER              : E:true  W:false
// 50 PRIVATE_USE_CHARACTER                    : E:true  W:false
// 51 UNICODE_CONTROL_CHARACTER                : E:true  W:false
// 52 UNASSIGNED_UNICODE_CHARACTER             : E:true  W:false
// 55 UNICODE_WHITESPACE                       : E:true  W:false
// 56 COMPATIBILITY_CHARACTER                  : E:true  W:false

}
