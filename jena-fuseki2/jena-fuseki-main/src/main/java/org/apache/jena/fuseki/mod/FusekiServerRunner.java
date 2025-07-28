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

package org.apache.jena.fuseki.mod;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModules;

/**
 *  @deprecated Use from new locations
 */
@Deprecated(forRemoval = true)
public class FusekiServerRunner {

    /**
     * Run {@link FusekiServer} with {@link FusekiModules} as given by {@link #serverModules()}.
     *  @deprecated Use from new location {@link org.apache.jena.fuseki.server.FusekiServerRunner#runAsync}.
     */
    @Deprecated(forRemoval = true)
    public static FusekiServer runAsync(String... args) {
        return org.apache.jena.fuseki.server.FusekiServerRunner.runAsync(args);
    }

    /**
     * Build but do not start, a {@link FusekiServer} with {@link FusekiModules} as given by {@link #serverModules()}.
     *  @deprecated Use from new location {@link org.apache.jena.fuseki.server.FusekiServerRunner#construct}.
     */
    @Deprecated(forRemoval = true)
    public static FusekiServer construct(String... args) {
        return org.apache.jena.fuseki.server.FusekiServerRunner.construct(args);
    }

    /**
     * @deprecated Use from new location {@link FusekiServerModules#serverModules}.
     */
    @Deprecated(forRemoval = true)
    public static FusekiModules serverModules() {
        return org.apache.jena.fuseki.mod.FusekiServerModules.serverModules();
    }
}
