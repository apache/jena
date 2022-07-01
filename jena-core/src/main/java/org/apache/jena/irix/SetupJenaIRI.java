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

package org.apache.jena.irix;

import java.io.PrintStream;

import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.ViolationCodes;
import org.apache.jena.iri.impl.PatternCompiler;

/** Setup of jena-iri package IRI Factory for parsing and for checking. */
public class SetupJenaIRI {

    // Currently, the same.
    // The difference is the treatment in IRIProviderJenaIRI and ParserProfileStd.internalMakeIRI
    // both of which can be scheme and component sensitive.

    private static final IRIFactory iriFactoryInst = setupIRIFactory();
    private static final IRIFactory iriCheckerInst = setupCheckerIRIFactory();

    /**
     * The IRI checker setup, focused on parsing and languages.
     * This is a clean version of jena-iri {@link IRIFactory#iriImplementation()}
     * modified to allow unregistered schemes and allow {@code <file:relative>} IRIs.
     */
    public static IRIFactory iriFactory() {
        return iriFactoryInst;
    }

    public static IRIFactory iriFactory_RDFXML() {
        // Used in ReaderRiotRDFXML
        return iriFactory();
    }

    /**
     * An IRIFactory with more detailed warnings.
     */
    public static IRIFactory iriCheckerFactory() {
        return iriCheckerInst;
    }

    // Currently the same factory.
    // The difference is the treatment in IRIProviderJenaIRI and ParserProfileStd.internalMakeIRI
    // both of which can be scheme and component sensitive.

    /*package*/ static final IRIFactory setupIRIFactory() {
        return setupCheckerIRIFactory();
    }

    /** IRI Factory with "checker" settings. */
    /*package*/ static final IRIFactory setupCheckerIRIFactory() {
        // See IRIProviderJenaIRI.exceptions for context specific tuning.
        // See Checker.iriViolations for filtering and output from parsers.

        IRIFactory iriCheckerFactory = new IRIFactory();

        //iriCheckerInst.shouldViolation(false,true);
        // These two are from IRIFactory.iriImplementation() ...
        iriCheckerFactory.useSpecificationIRI(true);
        iriCheckerFactory.useSchemeSpecificRules("*", true);
        // Allow relative references for file: URLs.
        iriCheckerFactory.setSameSchemeRelativeReferences("file");

        // See also Checker.iriViolations and IRProviderJenaIRI where this is restricted to the scheme component.
        setErrorWarning(iriCheckerFactory, ViolationCodes.LOWERCASE_PREFERRED, false, true);
        // Jena3 compatibility (false, false) for this one.
        setErrorWarning(iriCheckerFactory, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE, false, false);

        // -- Scheme specific rules.
        setErrorWarning(iriCheckerFactory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED, false, true);
        // jena-iri produces an error for PROHIBITED_COMPONENT_PRESENT regardless.
        // See Checker.iriViolations for handling this
        //setErrorWarning(iriCheckerFactory, ViolationCodes.PROHIBITED_COMPONENT_PRESENT, false, true);

        // == Scheme
        setErrorWarning(iriCheckerFactory, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);
        setErrorWarning(iriCheckerFactory, ViolationCodes.UNREGISTERED_NONIETF_SCHEME_TREE, false, false);

        // == DNS name.
        setErrorWarning(iriCheckerFactory, ViolationCodes.NOT_DNS_NAME, false, false);
        // RFC3986 allows present-encoded DNS names.
        setErrorWarning(iriCheckerFactory, ViolationCodes.USE_PUNYCODE_NOT_PERCENTS, false, false);

        // == Port related
        setErrorWarning(iriCheckerFactory, ViolationCodes.PORT_SHOULD_NOT_BE_EMPTY, false, true);
        setErrorWarning(iriCheckerFactory, ViolationCodes.PORT_SHOULD_NOT_START_IN_ZERO, false, true);
        setErrorWarning(iriCheckerFactory, ViolationCodes.DEFAULT_PORT_SHOULD_BE_OMITTED, false, true);
        // Warning in Jena3.  "Well known" is ports 0 to 1023.
        setErrorWarning(iriCheckerFactory, ViolationCodes.PORT_SHOULD_NOT_BE_WELL_KNOWN, false, false);

        // == Authority
        setErrorWarning(iriCheckerFactory, ViolationCodes.HAS_PASSWORD, false, true);
        setErrorWarning(iriCheckerFactory, ViolationCodes.PROHIBITED_COMPONENT_PRESENT, false, true);

        // == Path
        setErrorWarning(iriCheckerFactory, ViolationCodes.NON_INITIAL_DOT_SEGMENT, false, false);


        // == Character related.
        //setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFC,  false, false);
        // NFKC is not mentioned in RDF 1.1. Switch off.
        setErrorWarning(iriCheckerFactory, ViolationCodes.NOT_NFKC, false, false);

        // ** Applies to various unicode blocks.

        // Needed to be (false, false) for some Turtle tests (due to EricP!)
        setErrorWarning(iriCheckerFactory, ViolationCodes.COMPATIBILITY_CHARACTER, false, false);
        setErrorWarning(iriCheckerFactory, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, false);
        // Otherwise the set of legal characters depends on the Java version.
        // If not set, this causes test failures in Turtle and Trig eval tests.
        setErrorWarning(iriCheckerFactory, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER, false, false);

        // == Percent encoded.
        setErrorWarning(iriCheckerFactory, ViolationCodes.SUPERFLUOUS_NON_ASCII_PERCENT_ENCODING, false, true);
        setErrorWarning(iriCheckerFactory, ViolationCodes.SUPERFLUOUS_ASCII_PERCENT_ENCODING, false, true);

        return iriCheckerFactory;
    }

    /** Set the error/warning state of a violation code.
     * @param factory   IRIFactory
     * @param code      ViolationCodes constant
     * @param isError   Whether it is to be treated an error.
     * @param isWarning Whether it is to be treated a warning.
     */
    static void setErrorWarning(IRIFactory factory, int code, boolean isError, boolean isWarning) {
        factory.setIsWarning(code, isWarning);
        factory.setIsError(code, isError);
    }

    private static void printErrorWarning(PrintStream ps, IRIFactory factory, int code) {
        String x = PatternCompiler.errorCodeName(code);
        ps.printf("%-40s : E:%-5s W:%-5s\n", x, factory.isError(code), factory.isWarning(code));
    }

}
