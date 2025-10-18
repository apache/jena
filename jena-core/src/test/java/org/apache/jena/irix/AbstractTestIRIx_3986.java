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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

import org.apache.jena.iri3986.provider.IRIProvider3986;
import org.apache.jena.iri3986.provider.JenaSeveritySettings;
import org.apache.jena.rfc3986.Violations;

/**
 * Test suite driver for IRIx.
 * The test execution environment is set to be "strict".
 * Tests can change this; it is reset after each test.
 */
public class AbstractTestIRIx_3986 {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"IRI3986", new IRIProvider3986()});

        // Does not pass the test suite.
        //data.add(new Object[]{"JDK.URI", new IRIProviderJDK()});
        // Wire up IRIProvider3986 error/warning controls.
        Violations.setSystemSeverityMap(JenaSeveritySettings.jenaSystemSettings());
        return data;
    }

    protected IRIProvider getProvider() {
        return provider;
    }

    protected void notStrict(String scheme, Runnable action) {
        strictMode(scheme, false, action);
    }

    protected void strict(String scheme, Runnable action) {
        strictMode(scheme, true, action);
    }

    protected void strictMode(String scheme, boolean strictMode, Runnable action) {
        boolean b = provider.isStrictMode(scheme);
        provider.strictMode(scheme, strictMode);
        try { action.run(); }
        finally { provider.strictMode(scheme, b); }
    }

    private final IRIProvider provider;
    private static IRIProvider systemProvider;

    // Strictness is managed statically by providers.
    private static boolean StrictHTTP;
    private static boolean StrictURN;
    private static boolean StrictFILE;
    private static boolean StrictDID;

    @BeforeClass static public void beforeClass_StoreSystemProvider() {
        systemProvider = SystemIRIx.getProvider();
        StrictHTTP = systemProvider.isStrictMode("http");
        StrictURN  = systemProvider.isStrictMode("urn");
        StrictFILE = systemProvider.isStrictMode("file");
        StrictDID  = systemProvider.isStrictMode("did");
    }

    @AfterClass static public void afterClass_RestoreSystemProvider() {
        systemProvider.strictMode("http", StrictHTTP);
        systemProvider.strictMode("urn",  StrictURN);
        systemProvider.strictMode("file", StrictFILE);
        systemProvider.strictMode("did",  StrictDID);
    }

    @Before public void beforeTest_setStrict() {
        provider.strictMode("http", true);
        provider.strictMode("urn",  true);
        provider.strictMode("file", true);
        provider.strictMode("did",  true);
    }

    @After public void afterTest_restoreSystemProvider() {
        restore();
    }

    private static void restore() {
        systemProvider.strictMode("http", StrictHTTP);
        systemProvider.strictMode("urn",  StrictURN);
        systemProvider.strictMode("file", StrictFILE);
        systemProvider.strictMode("did",  StrictDID);
    }

    protected AbstractTestIRIx_3986(String name, IRIProvider provider) {
        this.provider = provider;
    }

    /** Create an IRIx using the test' provider. */
    protected IRIx test_create(String iriStr) {
        return provider.create(iriStr);
    }
}
