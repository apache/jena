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

package org.apache.jena.fuseki.main.access;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.jena.fuseki.main.FusekiServer;

/**
 * AbstractTestServiceDatasetAuth with a configuration file.
 */
public class TestServiceDataAuthBuild extends AbstractTestServiceDatasetAuth {

    private FusekiServer server;

    @BeforeEach public void before() {
        server = FusekiServer.create()
            //.verbose(true)
            .port(port)
            .parseConfigFile("testing/Access/config-auth.ttl")
            .build();
        server.start();
    }

    @AfterEach public void after () {
        server.stop();
    }

    @Override
    protected FusekiServer server() {
        return server;
    }
}
