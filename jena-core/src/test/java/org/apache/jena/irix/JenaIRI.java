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

import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.ViolationCodes;

// The Jena IRI settings using in ARQ/RIOT.
class JenaIRI {

    public static IRIFactory iriFactory() {
        return iriFactoryInst;
    }

    private static final IRIFactory iriFactoryInst = new IRIFactory();
    static {
        iriFactoryInst.useSpecificationIRI(true);
        iriFactoryInst.useSchemeSpecificRules("*", true);

        // Allow relative references for file: URLs.
        iriFactoryInst.setSameSchemeRelativeReferences("file");

        // Convert "SHOULD" to warning (default is "error").
        // iriFactory.shouldViolation(false,true);

        // Accept any scheme.
        setErrorWarning(iriFactoryInst, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.UNREGISTERED_NONIETF_SCHEME_TREE, false, false);

        setErrorWarning(iriFactoryInst, ViolationCodes.NON_INITIAL_DOT_SEGMENT, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.LOWERCASE_PREFERRED, false, true);
        setErrorWarning(iriFactoryInst, ViolationCodes.REQUIRED_COMPONENT_MISSING, true, true);

        setErrorWarning(iriFactoryInst, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE, false, false);

        // jena-iri has not been updated for percent in DNS name (RFC 3986)
        setErrorWarning(iriFactoryInst, ViolationCodes.NOT_DNS_NAME, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.USE_PUNYCODE_NOT_PERCENTS, false, false);

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
    }

    private static void setErrorWarning(IRIFactory factory, int code, boolean isError, boolean isWarning) {
        factory.setIsError(code, isError);
        factory.setIsWarning(code, isWarning);
    }
}
