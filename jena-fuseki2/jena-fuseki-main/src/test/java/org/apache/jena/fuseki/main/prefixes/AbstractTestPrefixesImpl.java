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

package org.apache.jena.fuseki.main.prefixes;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.servlets.prefixes.ActionProcPrefixes;
import org.apache.jena.fuseki.servlets.prefixes.PrefixesAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tests setup to test {@link PrefixesAccess} as standalone implementations.
 */
public abstract class AbstractTestPrefixesImpl extends AbstractTestPrefixes {

    private final PrefixesAccess prefixes;

    public AbstractTestPrefixesImpl(PrefixesAccess storage) {
        this.prefixes = storage;
    }

    private FusekiServer server = null;
    private String serviceURL = null;
    private static final String serviceToFormatURL = "http://localhost:%d/prefixes";

    @BeforeEach
    public void before() {
        server = FusekiServer.create()
                .port(0)
                .addProcessor("/prefixes", new ActionProcPrefixes(prefixes))
                .build();
        server.start();
        serviceURL = String.format(serviceToFormatURL, server.getHttpPort());
    }

    @AfterEach
    public void afterSuite() {
        if ( server != null )
            server.stop();
    }

    @Override
    protected String testReadURL() {
        return serviceURL;
    }

    @Override
    protected String testWriteURL() {
        return serviceURL;
    }

}
