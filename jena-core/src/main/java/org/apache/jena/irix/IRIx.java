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

import java.util.Objects;

/**
 * Support for RFC3986 IRIs.
 * <p>
 * The class {@code IRIx} is an abstraction layer; a provider is needed to give an
 * implementation. A provider is selected at start-up and is not expected to change
 * while the system is running.
 * <p>
 * Use {@link IRIs#reference} to check a string is suitable for us in RDF.
 *
 * @see IRIxResolver for resolving relative IRIs against a base.
 * @see IRIs for functions related to IRIs
 */
public abstract class IRIx {

    private final String iriString;

    /**
     * Create an {@link IRIx} without resolving the iri. This operation may throw an
     * {@link IRIException}.
     * <p>
     * See {@link IRIs#resolve(String)} to create an absolute IRI, resolving
     * against the system base if necessary.
     * <p>
     * See {@link IRIs#check(String)} to check a string is an absolute URI and is suitable for use in RDF.
     * <p>
     * See {@link IRIs#reference(String)} when the string is an absolute URI and
     * should not be resolved against local system base (e.g. it was passed in from
     * outside) to create an {@link IRIx} that is suitable for use in RDF.
     */
    static public IRIx create(String iri) throws IRIException {
        Objects.requireNonNull(iri);
        return SystemIRIx.getProvider().create(iri);
    }

    /**
     * String must have been validated (e.g complies with the grammar of RFC3986).
     * This constructor does not perform any additional checking.
     */
    protected IRIx(String string) {
        this.iriString = string;
    }

    /**
     * An <a href="https://tools.ietf.org/html/rfc3986#section-4.3"><em>absolute URI</em></a>
     * is one with a URI scheme and without a fragment.
     * The other components, host (authority), path, and query, are optional.
     * <p>
     * absolute-URI  = scheme ":" hier-part [ "?" query ]
     * <p>
     * Beware of the meaning : {@code http:abc} is an absolute URI - it has only a schema and a path without a root.
     * <p>
     * Note that a URI can be both "not absolute" and "not relative", e.g. {@code http://example/path#fragment}.
     * <p>
     * See {@linkplain #isReference()} for testing whether a URI is suitable for use in RDF.
     */
    public abstract boolean isAbsolute();

    /**
     * A <a href="https://tools.ietf.org/html/rfc3986#section-4.2"><em>relative
     * URI</em></a> one without a scheme, and maybe without some of the other parts.
     * <p>
     * Often it is just the path part.
     * <p>
     * See {@linkplain #isReference()} for testing whether a URI is suitable for use in RDF.
     * <p>
     * Note that a URI can be both "not absolute" and "not relative", e.g. {@code http://example/path#fragment}.
     */
    public abstract boolean isRelative();

    /**
     * Test whether the IRI has the given scheme name.
     * <p>
     * The scheme name should be lowercase.
     */
    public abstract boolean hasScheme(String scheme);

    /**
     * An <em>RDF Reference</em> is an URI which has scheme.
     * If it is hierarchical, it should a non-empty host authority,
     * and may have a query component and may have a fragment component.
     * This not a term in
     * <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>
     * and it is not the same as "absolute URI".
     * <p>
     * In RDF data it is a
     * <a href="https://www.w3.org/TR/rdf11-concepts/#section-IRIs">useful concept</a>.
     * It is either an absolute URI, but if it is hierarchical, it must have a host.
     * <p>
     * Examples:
     * <ul>
     * <li>http://www.w3.org/
     * <li>http://www.w3.org/1999/02/22-rdf-syntax-ns#type
     * <li>urn:abc:def
     * <li>urn:abc:def#frag
     * </ul>
     * but not
     * <ul>
     * <li>http:abc -- no host authority; HTTP is a hierarchical URI scheme
     * <li>http:// -- the http(s) URI scheme requires the host to be not empty if there is an authority component.
     * </ul>
     * <p>
     * In practical terms:
     * <ul>
     * <li>It has a scheme name.
     * <li>It does not have user info ("user:password@")
     * <li>It can have a fragment.
     * <li>If it is an HTTP URI:
     *   <ul>
     *   <li>It has a host authority, that is, a "//" section
     *   <li>It should have a path (starting "/" after the host authority) but this is not required.
     *   </ul>
     * <li>If it is a <a href="https://tools.ietf.org/html/rfc8141">URN (RFC8141)</a>, which is a "rootless URI" with no "//" part:
     *    <ul>
     *    <li>Optionally, it can have a <a href="https://tools.ietf.org/html/rfc8141#section-2.3.1">r-component</a> (though this is not advised),
     *      a <a href="https://tools.ietf.org/html/rfc8141#section-2.3.2">q-component</a>,
     *     and a <a href="https://tools.ietf.org/html/rfc8141#section-2.3.3">f-component</a> (which is a URI fragment).
     *     </ul>
     * </ul>
     */
    public abstract boolean isReference();

    /**
     * Try to resolve a string against this IRI as base.
     * This call is "base.resolver(possibleRelativeIRI)".
     * Throw {@link IRIException} if the string does not conform to the IRI grammar.
     */
    public abstract IRIx resolve(String other);

    /**
     * Try to resolve a string against this IRI as base.
     * Throw {@link IRIException} if the string does not conform to the IRI grammar.
     */
    public abstract IRIx resolve(IRIx other);

    /**
     * <a href="https://tools.ietf.org/html/rfc3986#section-6.2.2">Syntax-based Normalization</a>
     * Normalize an {@link IRIx}.
     */
    public abstract IRIx normalize();


    /**
     * Return (if possible), an IRI that is relative to the base argument.
     * If this IRI is a relative path, this is returned unchanged.
     * <p>
     * The base ("this" object) must have a scheme, have no fragment and no query string.
     * Only the path name is made relative.
     * <p>
     * If no relative IRI can be found, return null.
     */
    public abstract IRIx relativize(IRIx other);

    /**
     * Return the URI as string. This has a stronger contract than "toString".
     * "Object.toString" is a user readable string (e.g. it might add enclosing "<>"
     * or show the parsed structure of the IRI) whereas {@code asString()} is by
     * contract the string that comprises the IRI. The string returned may be the
     * normalized form. It is guaranteed to be usable as string in other API calls
     * that expect a IRI in string form if the original input was a legal IRI by the
     * RFC grammar and any additional scheme-specific rules the IRI provider
     * enforces.
     */
    public String str() {
        return iriString;
    }

    /**
     * User readable form. Not guaranteed to be usable as a string
     * in other API calls.
     *
     * Use {@link #str()} to get a string form that represents the IRI in the RFC grammar.
     */
    @Override
    public String toString() {
        return iriString;
    }
}

