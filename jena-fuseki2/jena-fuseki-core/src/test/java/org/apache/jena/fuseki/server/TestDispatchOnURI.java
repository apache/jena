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

package org.apache.jena.fuseki.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.jena.fuseki.servlets.ActionLib;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Test the request URI part of dispatch.
 * This covers finding the DataAccessPoint.
 * A request may still fail due to no endpoint or suitable processor;
 * this isn't covered in these unit tests.
 */
public class TestDispatchOnURI {

    private static DataAccessPointRegistry registryNoRoot;
    private static DataAccessPointRegistry registryWithRoot;

    @BeforeClass public static void beforeClass() {
        registryNoRoot = new DataAccessPointRegistry();
        DataService dSrv1 = DataService.newBuilder()
                .addEndpoint(Operation.Query)
                .addEndpoint(Operation.Query, "spook")
                .build();
        registryNoRoot.register(new DataAccessPoint("ds", dSrv1));
        DataService dSrv2 = DataService.newBuilder()
                .addEndpoint(Operation.Query)
                .build();
        registryNoRoot.register(new DataAccessPoint("/path/dataset", dSrv2));
        registryNoRoot.register(new DataAccessPoint("/path1/path2/dataset", dSrv2));

        registryWithRoot = new DataAccessPointRegistry(registryNoRoot);
        registryWithRoot.register(new DataAccessPoint("/", dSrv1));
    }

    @Test public void dispatch_1() {
        testDispatch("/ds", registryWithRoot, "/ds", "");
    }

    @Test public void dispatch_2() {
        // Request URI dispatch does not consider existence of a suitable endpoint.
        testDispatch("/ds/does-not-exist", registryWithRoot, "/ds", "does-not-exist");
    }

    @Test public void dispatch_3() {
        testDispatch("/ds/spook", registryWithRoot, "/ds", "spook");
    }

    @Test public void dispatch_root_1() {
        testDispatch("/", registryWithRoot, "/", "");
    }

    @Test public void dispatch_root_2() {
        testDispatch("/sparql", registryWithRoot, "/", "sparql");
    }

    // endpoint names can only be path components.
    @Test public void no_dispatch_1() {
        testNoDispatch("/ds/abc/def", registryWithRoot);
    }

    @Test public void no_dispatch_2() {
        testNoDispatch("/x404", registryNoRoot);
    }

    @Test public void no_dispatch_3() {
        testNoDispatch("/", registryNoRoot);
    }

    @Test public void no_dispatch_4() {
        testNoDispatch("/x404/sparql", registryWithRoot);
    }

    @Test public void no_dispatch_5() {
        testNoDispatch("/anotherPath/dataset", registryWithRoot);
    }

    @Test public void dispatch_path_1() {
        testDispatch("/path/dataset", registryWithRoot, "/path/dataset", "");
    }

    @Test public void dispatch_path_2() {
        testDispatch("/path/dataset/sparql", registryWithRoot, "/path/dataset", "sparql");
    }

    @Test public void dispatch_path_3() {
        testDispatch("/path/dataset/does-not-exist", registryWithRoot, "/path/dataset", "does-not-exist");
    }

    @Test public void dispatch_path_4() {
        testDispatch("/path1/path2/dataset", registryWithRoot, "/path1/path2/dataset", "");
    }

    @Test public void dispatch_path_5() {
        testDispatch("/path1/path2/dataset/sparql", registryWithRoot, "/path1/path2/dataset", "sparql");
    }

    private void testNoDispatch(String requestURI, DataAccessPointRegistry registry) {
        DataAccessPoint dap = Dispatcher.locateDataAccessPoint(requestURI, registry);
        assertNull("Expect no dispatch for "+requestURI, dap);
    }

    private void testDispatch(String requestURI, DataAccessPointRegistry registry, String expectedDataset, String expectedEndpoint) {
        DataAccessPoint dap = Dispatcher.locateDataAccessPoint(requestURI, registry);
        if ( dap == null ) {
            if ( expectedDataset != null )
                fail("No DataAccessPoint: expected to find a match: "+requestURI+" -> ("+expectedDataset+", "+expectedEndpoint+")");
            return;
        }
        // The request URI part of dispatch choice in Dispatcher.chooseProcessor(HttpAction action)
        String ep = ActionLib.mapRequestToEndpointName(requestURI, dap);
        assertNotNull(ep);
        assertEquals("Endpoint", expectedEndpoint, ep);
    }
}
