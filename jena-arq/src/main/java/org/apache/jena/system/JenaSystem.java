/**
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

package org.apache.jena.system;

/**
 * This is a temporary adapter for implementations to transition to [JENA-1524]
 */
@Deprecated
public class JenaSystem {

    /**
     * Initialize Jena.
     * @deprecated Use {@link org.apache.jena.sys.JenaSystem#init()}
     */
    @Deprecated
    public static void init() {
        org.apache.jena.sys.JenaSystem.init();
    }

    /** Shutdown subsystems
     * @deprecated Use {@link org.apache.jena.sys.JenaSystem#shutdown()}
     */
    @Deprecated
    public static void shutdown() {
        org.apache.jena.sys.JenaSystem.shutdown();
    }
}
