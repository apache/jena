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

package org.apache.jena.fuseki.build;

import java.util.Map;

import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.server.OperationRegistry;
import org.apache.jena.fuseki.servlets.ActionService;

/**
 * Operations to extend Fuseki with external code.
 */
public class FusekiExt {

    // Additional operations provided by extension, added via Jena init / typically ServiceLoader
    // These are used in FuskeiConfig.
    // Left in case in the future we want to have customized defautl setup.
    // Currently, Jena5, the preferred way is to have control via config.ttl or FusekiServer.Builder.
    /*package*/ static Map<String, Operation> extraOperationServicesRead = null;
    /*package*/ static Map<String, Operation> extraOperationServicesWrite = null;

    /** Make a new operation available. */
    public static void registerOperation(Operation op, ActionService handler) {
        // No Content-type registration
        OperationRegistry.get().register(op, null, handler);
    }

    /** Remove an operation. */
    public static void unregisterOperation(Operation op, ActionService handler) {
        OperationRegistry.get().unregister(op);
    }
}
