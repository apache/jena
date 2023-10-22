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

import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.logging.Log;

/**
 * System setup and configuration.
 * This class is not public API.
 */
public class SystemIRIx {

    // -- Providers
    private static IRIProvider makeProviderJenaIRI() {
        IRIProvider newProviderJenaIRI = new IRIProviderJenaIRI();
        newProviderJenaIRI.strictMode("urn",  false);
        newProviderJenaIRI.strictMode("http", false);
        newProviderJenaIRI.strictMode("file", false);
        return newProviderJenaIRI;
    }
    private static IRIProvider providerJenaIRI = makeProviderJenaIRI();

    // ** Do not use IRIProviderJDK in production. **
    private static IRIProvider makeProviderJDK() { return new IRIProviderJDK(); }

//    private static IRIProvider makeProviderIRI3986() {
//        IRIProvider newProviderIRI3986 = new IRIProvider3986();
//        newProviderIRI3986.strictMode("urn", true);
//        newProviderIRI3986.strictMode("http", true);
//        newProviderIRI3986.strictMode("file", true);
//    }
//    private static IRIProvider providerIRI3986 = makeProviderIRI3986();

    // -- System-wide provider.

    public static IRIProvider makeFreshSystemProvider() {
        // Choice point.
        return makeProviderJenaIRI();
    }

    private static IRIProvider provider = makeFreshSystemProvider();

    public static void init() {}
    public static void reset() {
        provider = makeFreshSystemProvider();
    }

    public static void setProvider(IRIProvider aProvider) {
        provider = aProvider;
        // Reset provider of the system base.
        IRIx iri = setupBase(systemBase.str());
        setSystemBase(iri);
    }

    public static IRIProvider getProvider() {
        return provider;
    }

    /**
     * Run in strict mode - the exact definition of "strict" depends on the provider.
     * When strict a provider should implement to the letter of the specifications,
     * including URI-scheme rules. This strictness should be documented.
     */
    public static void strictMode(String scheme, boolean runStrict) {
        getProvider().strictMode(scheme, runStrict);
    }

    /*
     * Return the state of strict mode for the given scheme.
     */
    public static boolean isStrictMode(String scheme) {
        return getProvider().isStrictMode(scheme);
    }

    // -- System base
    private static final IRIx cwdURI = establishBaseURI();
    private static IRIx systemBase = cwdURI;
    /* Used only if setting a base fails in some way. */
    private static String fallbackBaseURI = "urn:jena:base";

    /*package*/ static IRIx getSystemBase() {
        return systemBase;
    }

    private static IRIx establishBaseURI() {
        try {
            String baseStr = IRILib.filenameToIRI("./");
            if ( ! baseStr.endsWith("/") )
                baseStr = baseStr+"/";
            return setupBase(baseStr);
        } catch (Throwable ex) {
            ex.printStackTrace();
            // e.g. No filesystem.
            return IRIx.create(fallbackBaseURI);
        }
    }

    /**
     * Create an {@link IRIx} suitable for a system base.
     * This operation always returns an {@link IRIx}
     * This operation does not set the system base.
     * @param baseStr
     * @return
     */
    private static IRIx setupBase(String baseStr) {
        if ( baseStr == null )
            return IRIx.create(fallbackBaseURI);
        try {
            IRIx base = IRIx.create(baseStr);
            if ( ! base.isReference() )
                Log.error(IRIs.class, "System base URI is not a reference URI: must have scheme, host and path");
            return base;
        } catch (IRIException ex) {
            Log.error(IRIs.class, "Failed to create IRI from '"+baseStr+"'", ex);
            return IRIx.create(fallbackBaseURI);
        }
    }

    /**
     * Change the system default base for IRI operations.
     * It is recommended to only do this during start-up and not during normal operation.
     * It is better to have {@link IRIx} argument to operate with a different base URI.
     */
    private static void setSystemBase(IRIx iri) {
        systemBase = iri;
    }
}
