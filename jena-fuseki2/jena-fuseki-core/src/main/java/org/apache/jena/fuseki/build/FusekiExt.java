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

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.server.OperationRegistry;
import org.apache.jena.fuseki.servlets.ActionService;

/**
 * Operations to extend Fuseki with external code.
 * <p>
 */
public class FusekiExt {

    // Additional operations provided by extension, added via Jena init / typically ServiceLoader
    static Map<String, Operation> extraOperationServicesRead;
    static Map<String, Operation> extraOperationServicesWrite;

    /**
     * Add a new operation, which will be included in a default configuration.
     * The operation must have been added with {@link #registerOperation} first.
     * @deprecated Use {@code FusekiModules} with FusekiMain.
     */
    @Deprecated
    public static void addDefaultEndpoint(Operation op, String serviceName) {
        // Include in both sets.
        addDefaultEndpoint(op, serviceName, false);
        addDefaultEndpoint(op, serviceName, true);
    }

    /**
     * Add a new operation, which will be included in a default configuration,
     * depending on whether it is for general inclusion or for inclusion for update
     * only configuration.
     * The operation must have been added with {@link #registerOperation} first.
     * @deprecated Use {@code FusekiModules} with FusekiMain.
     */
    @Deprecated
    public static void addDefaultEndpoint(Operation op, String serviceName, boolean forUpdate) {
        // Due to class loading and ServiceLoader interaction, it is not reliable to
        // set extraOperationServices as it is declared.
        if ( extraOperationServicesRead == null )
            extraOperationServicesRead = new HashMap<>();
        if ( extraOperationServicesWrite == null )
            extraOperationServicesWrite = new HashMap<>();

        if ( forUpdate )
            extraOperationServicesWrite.put(serviceName, op);
        else
            extraOperationServicesRead.put(serviceName, op);
    }

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
