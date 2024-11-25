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

import static org.apache.jena.rfc3986.LibParseIRI.caseInsensitivePrefix;

/**
 * URI scheme
 * <ul>
 * <li><a href="https://www.iana.org/assignments/uri-schemes/uri-schemes.xhtml">URI Registrations</a>
 * <li><a href="https://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">URN Registrations</a>
 * </ul>
 * This also include URN namespaces.
 */
public enum URIScheme {

    // Violations of general URI forms, not syntax errors
    GENERAL("RFC3986"),
    // "scheme" is resolved for URI general parse conditions.
    HTTP("http"),
    HTTPS("https"),
    URN("urn"),
    // Pseudo scheme
    URN_UUID("urn", "uuid"),
    // It's not officially registered but may be found in the wild.
    UUID("uuid"),
    FILE("file"),
    DID("did"),
    URN_OID("urn", "oid"),
    // https://www.rfc-editor.org/rfc/rfc6963
    URN_EXAMPLE("urn", "example"),

    // It's not officially registered but may be found in the wild.
    OID("oid"),
    // RFC 7595 and registered.
    // https://www.rfc-editor.org/rfc/rfc7595.html#section-8
    EXAMPLE("example"),
    ;

    private final String name;
    private final String schemeName;
    private final String schemeNameColon;
    private final String urnNamespace;
    private final String prefix;

    public static URIScheme get(String name) {
        if ( name == null )
            return null;
        if ( name.endsWith(":") )
            name = name.substring(0, name.length()-1);
        for ( URIScheme scheme : URIScheme.values() ) {
            if ( scheme.getName().equalsIgnoreCase(name) )
                return scheme;
        }
        return null;
    }

    private URIScheme(String schemeName) {
        this.name = schemeName;
        this.schemeNameColon = schemeName+":";
        this.schemeName = schemeName;
        this.urnNamespace = null;
        this.prefix =name+":";
    }

    private URIScheme(String schemeName, String urnNS) {
        this.name = schemeName+":"+urnNS;
        this.schemeName = schemeName;
        this.schemeNameColon = schemeName+":";
        this.urnNamespace = urnNS;
        this.prefix = schemeName+":"+urnNS+":";
    }

    /** Match schema name, not including any urn namespace, case sensitively */
    public static boolean matchesExact(String iriStr, URIScheme scheme) {
        return iriStr.startsWith(scheme.schemeNameColon);
    }

    /** Match case insensitively */
    public static boolean matchesIgnoreCase(String iriStr, URIScheme scheme) {
        return caseInsensitivePrefix(iriStr, scheme.schemeNameColon);
    }

    /** Match case insensitively */
    public static boolean fromScheme(String iriStr, URIScheme scheme) {
        return caseInsensitivePrefix(iriStr, scheme.prefix);
    }

    public final boolean isURN() {
        return switch(this) {
            case URN, URN_EXAMPLE, URN_OID, URN_UUID -> true;
            default -> false;
        };
    }

    /** Scheme name; no ':' */
    public String getSchemeName() {
        return schemeName;
    }

    /** URN namespace name, or null, if not a URN. */
    public String getURNNamespace() {
        return urnNamespace;
    }

    /** Scheme name,including any URN namespace. */
    public String getName() {
        return name;
    }

    /**
     * Initial part of the URI scheme that identifies this scheme. In practice this
     * is the schema name in lower case followed by ':'; for URNs, it includes the
     * URN namespace. if that is one that is covered by the scheme-specific
     * validation rule.
     */
    public String getPrefix() {
        return prefix;
    }
}
