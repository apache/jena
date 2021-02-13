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
    private static IRIProvider providerJenaIRI = new IRIProviderJenaIRI();
    //private static IRIProvider providerJDK     = new IRIProviderJDK();
    //private static IRIProvider providerIRI3986 = new IRIProvider3986();

    // ** Do not use IRIProviderJDK in production. **

    // -- System-wide provider.

    private static IRIProvider provider;
    static {
        provider = providerJenaIRI;
        provider.strictMode("urn", false);
    }

    public static void init() {}

    public static void setProvider(IRIProvider aProvider) {
        provider = aProvider;
        // Reset
        systemBase = establishBaseURI();
    }

    public static IRIProvider getProvider() {
        return provider;
    }

    // -- System base
    private static IRIx cwdURI = establishBaseURI();
    private static IRIx systemBase = cwdURI;

    /*package*/ static IRIx getSystemBase() {
        return systemBase;
    }

    private static IRIx establishBaseURI() {
        try {
            String baseStr = IRILib.filenameToIRI("./");
            if ( ! baseStr.endsWith("/") )
                baseStr = baseStr+"/";
            return setSystemBase(baseStr);
        } catch (Throwable ex) {
            ex.printStackTrace();
            // e.g. No filesystem.
            return IRIx.create("urn:base:");
        }
    }

    private static IRIx setSystemBase(String baseStr) {
        try {
            if ( !baseStr.endsWith("/") )
                baseStr = baseStr+"/";
            IRIx base = IRIx.create(baseStr);
            if ( ! base.isReference() )
                Log.error(IRIs.class, "System base URI is not a reference URI: must have scheme, host and path");
            return base;
        } catch (IRIException ex) {
            Log.error(IRIs.class, "Failed to create IRI from '"+baseStr+"'", ex);
            return IRIx.create("urn:base:");
        }
    }

    /**
     * Change the system default base for IRI operations.
     * It is recommended to only do this during start-up and not during normal operation.
     * It is better to have {@link IRIx} argument to operate with a different base URI.
     */
    /*package*/ static void setSystemBase(IRIx iri) {
        systemBase = iri;
    }
}
