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

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <a href="https://datatracker.ietf.org/doc/html/rfc3061">RFC 3061</a>
 * urn:NID:NSS
 * where NID = "oid" (case insensitive) and NSS is the oid grammar.
 * <pre>
 *   oid             = number *( DOT number )
 *   number          = DIGIT / ( LEADDIGIT 1*DIGIT )
 *   LEADDIGIT       = %x31-39          ; 1-9
 *   DIGIT           = %x30 / LEADDIGIT ; 0-9
 *   DOT             = %x2E             ; period
 * </pre>
 * Some uses of OIDs are stricter e.g.
 * <a href="https://www.hl7.org/fhir/datatypes.html#oid">HL7 FHIR Datatypes</a>
 * <pre>
 *   urn:oid:[0-2](\.(0|[1-9][0-9]*))+
 * </pre>
 */
public class ParseOID {
    // and some (often non-URN) uses of OID may restrict size and length:
    //   ^([1-9][0-9]{0,3}|0)(\.([1-9][0-9]{0,3}|0)){5,13}$

    private static final String NUM = "(0|[1-9][0-9]*)";
    // Checks both correct "urn:oid:" and incorrect "oid:".
    private static final String OID_URN = "^(?:urn:oid|oid):"+NUM+"(\\."+NUM+")*$";
    private static final Pattern OID_URN_RE = Pattern.compile(OID_URN, Pattern.CASE_INSENSITIVE);

    // Where * is {0,3}

    private static final int pathOffset = "urn:".length();

    public static void check(String string) {
        parse(string);
    }

    public static IRI3986 parse(String string) {
        Objects.requireNonNull(string);
        if ( ! OID_URN_RE.matcher(string).matches() )
            throw new OIDParseException(string, "Not a match");
        IRI3986 iri = IRI3986.build("urn", null,  string.substring(pathOffset), null, null);
        return iri;
    }

    static class OIDParseException extends IRIParseException {
        OIDParseException(String entity, String msg) { super(entity, msg); }
    }
}
