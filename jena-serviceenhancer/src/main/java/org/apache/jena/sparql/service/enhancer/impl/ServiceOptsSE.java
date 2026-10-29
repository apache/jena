/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.Set;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;

/** Domain adaption for the ServiceEnhancer executor. */
public class ServiceOptsSE {

    public static final String SO_BULK = "bulk";
    public static final String SO_CACHE = "cache";

    /** Reserved; currently not implemented */
    public static final String SO_LATERAL = "lateral";

    public static final String SO_LOOP = "loop";

    /**
     * Modifies loop to substitute only in-scope variables on the rhs.
     * Original behavior is to substitute variables regardless of scope. Usage: SERVICE &gt;loop+scoped&lt; {}.
     */
    public static final String SO_LOOP_MODE_SCOPED = "scoped";

    // public static final String SO_CONCURRENT = "concurrent";
    public static final String SO_OPTIMIZE = "optimize";

    private static Set<String> knownOptions = Set.of(
        SO_BULK,
        SO_CACHE,
        SO_LATERAL,
        SO_LOOP,
        // SO_CONCURRENT,
        SO_OPTIMIZE);

    public Set<String> getKnownOptions() {
        return knownOptions;
    }

    private static boolean isKnownOption(String key) {
        return knownOptions.contains(key);
    }

    public static ServiceOpts getEffectiveService(OpService opService) {
        return ServiceOpts.getEffectiveService(opService, ServiceEnhancerConstants.SELF.getURI(), ServiceOptsSE::isKnownOption);
    }
}
