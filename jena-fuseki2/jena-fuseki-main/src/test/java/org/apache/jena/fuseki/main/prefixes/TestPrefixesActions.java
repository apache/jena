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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test the prefixes service when used as Fuseki operations
 * on a Fuseki service with a database.
 */
public class TestPrefixesActions extends AbstractTestPrefixes {

    private FusekiServer server = null;
    private String serviceR = null;
    private String serviceRW = null;

    @BeforeEach
    public void beforeEach() {
        String DATASET = "dataset";
        server = FusekiServer.create()
                .port(0)
                // Empty dataset every test start.
                .parseConfigFile("src/test/files/config-prefixes-empty.ttl")
                .start();

        int port = server.getHttpPort();
        serviceR = "http://localhost:"+port+"/"+DATASET+"/prefixes";
        serviceRW = "http://localhost:"+port+"/"+DATASET+"/prefixes-rw";
    }

    @AfterEach
    public void afterEach() {
        if ( server != null )
            server.stop();
    }

    @Override
    protected String testReadURL() {
        return serviceR;
    }

    @Override
    protected String testWriteURL() {
        return serviceRW;
    }
}
