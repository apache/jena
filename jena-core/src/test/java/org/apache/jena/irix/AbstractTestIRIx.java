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

/** Test suite driver for IRIx */
public class AbstractTestIRIx {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        SystemIRIx.init();
        List<Object[]> data = new ArrayList<>();
        //data.add(new Object[]{"IRI3986", new IRIProvider3986()});
        data.add(new Object[]{"JenaIRI", new IRIProviderJenaIRI()});
        // Does not pass the test suite.
        //data.add(new Object[]{"JDK.URI", new IRIProviderJDK()});
        return data;
    }

    protected static void setProvider(IRIProvider provider) {
        provider.strictMode("urn", true);
        provider.strictMode("file", true);
        SystemIRIx.setProvider(provider);
    }

    protected static IRIProvider getProvider() {
        return SystemIRIx.getProvider();
    }

    protected void notStrict(String scheme, Runnable action) {
        provider.strictMode(scheme, false);
        try { action.run(); }
        finally { provider.strictMode(scheme, true); }
    }

    private final IRIProvider provider;
    private static IRIProvider systemProvider;

    @BeforeClass static public void beforeClass() {
        systemProvider = getProvider();
    }

    @AfterClass static public void afterClass() {
        setProvider(systemProvider);
    }

    @Before public void beforeTest() {
        systemProvider = getProvider();
        setProvider(provider);
    }

    @After public void afterTest() {
        setProvider(systemProvider);
    }

    protected AbstractTestIRIx(String name, IRIProvider provider) {
        this.provider = provider;
    }
}
