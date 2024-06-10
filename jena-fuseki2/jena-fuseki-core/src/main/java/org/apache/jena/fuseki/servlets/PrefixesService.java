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

package org.apache.jena.fuseki.servlets;

import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.server.OperationRegistry;

/** The PrefixesService class registers the PrefixesR and PrefixesRW operations
 * and actions for the Prefixes Service. The service allows for prefix management
 * on a given dataset provided in the config.ttl file. When making requests
 * to the API the url can have 3 parameters: prefix, uri, and prefixToRemove,
 * the combination of which defines the operation performed. The read operations
 * are fetching the prefix by URI, fetching the URI by prefix, and fetching all.
 * They are evoked via HTTP GET. The read-write operations are removing and updating
 * prefix-URI pairs. Those are POST requests. */

public class PrefixesService {
    public static final Operation operationPrefixesRW;
    public static final Operation operationPrefixesR;

    private static final ActionService procPrefixR;
    private static final ActionService procPrefixRW;

    static {
        operationPrefixesR = Operation.PREFIXES_R;
        operationPrefixesRW = Operation.PREFIXES_RW;

        procPrefixR = new ActionPrefixesR();
        procPrefixRW = new ActionPrefixesRW();

        OperationRegistry.get().register(operationPrefixesR, procPrefixR);
        OperationRegistry.get().register(operationPrefixesRW, procPrefixRW);
    }

    public static void init() {}
}
