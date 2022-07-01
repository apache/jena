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

import org.apache.jena.fuseki.main.FusekiServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * AbstractTestServiceDatasetAuth with a configuration file.
 */
public class TestServiceDataAuthBuild extends AbstractTestServiceDatasetAuth {

    static FusekiServer server;

    @BeforeClass public static void beforeClass () {
        server = FusekiServer.create()
            //.verbose(true)
            .port(port)
            .parseConfigFile("testing/Access/config-auth.ttl")
            .build();
        server.start();
    }

    @AfterClass public static void afterClass () {
        server.stop();
    }

    @Override
    protected FusekiServer server() {
        return server;
    }
}
