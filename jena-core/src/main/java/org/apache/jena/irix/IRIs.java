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
 * Operations in support of {@link IRIx}.
 */
public class IRIs {
    /**
     * Operation to take a string and make an {@link IRIx}.
     * This operation does not resolve the string against a base.
     * <p>
     * Use this function if the application has been given an URI
     * and wants to be sure it is valid for use in RDF.
     * <p>
     * Use {@link IRIs#resolve} to resolve a potentially relative URI against the current system base.
     */
    static public IRIx reference(String iriStr) {
        Objects.requireNonNull(iriStr);
        IRIx iri = IRIx.create(iriStr);
        if ( ! iri.isReference() )
            throw new IRIException("Not an RDF IRI: "+iriStr);
        return iri;
    }

    /** Check a string is valid syntax for an IRI (absolute or relative) */
    static public boolean check(String iriStr) {
        Objects.requireNonNull(iriStr);
        try {
            checkEx(iriStr);
            return true;
        } catch(Exception ex) { return false; }
    }

    /**
     * Check a string is valid syntax for an IRI,
     * is an absolute IRI (resolve if necessary)
     * and normalize (e.g. remove "./.." and "/../").
     */
    static public String checkEx(String iriStr) {
        Objects.requireNonNull(iriStr);
        return reference(iriStr).str();
    }

    public static IRIx getSystemBase() {
        return SystemIRIx.getSystemBase();
    }

    /** Return a general purpose resolver, with the current system base as its base IRI. */
    public static IRIxResolver stdResolver() {
        return resolver(getSystemBase());
    }

    /** Return a general purpose resolver, with the current system base as its base IRI. */
    public static IRIxResolver resolver(String base) {
        return IRIxResolver.create(base).resolve(true).allowRelative(false).build();
    }

    /** Return a general purpose resolver, with the supplied IRI its base. */
    public static IRIxResolver resolver(IRIx base) {
        return IRIxResolver.create(base).resolve(true).allowRelative(false).build();
    }

    /** Return a resolver that does not resolve, and have a base and does not allow relative URIs. */
    public static IRIxResolver absoluteResolver() {
        return IRIxResolver.create().noBase().resolve(false).allowRelative(false).build();
    }

    /** Return a resolver that does not resolve relative URIs, with the current system base as its base IRI */
    public static IRIxResolver relativeResolver() {
        return IRIxResolver.create().noBase().resolve(false).allowRelative(true).build();
    }

    /** Resolve a URI against the system base. */
    public static String resolve(String iriStr) {
        if ( iriStr == null )
            return getSystemBase().str();
        return resolve(getSystemBase(), iriStr);
    }

    /** Resolve a URI against the system base. */
    public static IRIx resolveIRI(String iriStr) {
        if ( iriStr == null )
            return getSystemBase();
        return getSystemBase().resolve(iriStr);
    }

    /** Resolve a URI against a base. */
    public static String resolve(IRIx base, String iriStr) {
        return base.resolve(iriStr).str();
    }

    /** Resolve a URI against a base. */
    public static String resolve(IRIx base, IRIx iri) {
        return base.resolve(iri).str();
    }

    /** Resolve a URI against a base. The base must be an absolute IRI. */
    public static String resolve(String baseStr, String iriStr) {
        IRIx base = IRIx.create(baseStr);
        if ( ! base.isReference() )
            throw new IRIException("Not suitable as a base URI: '"+baseStr+"'");
        return resolve(base, iriStr);
    }
}
