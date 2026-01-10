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

package org.apache.jena.fuseki.main;

import org.apache.jena.fuseki.main.runner.FusekiArgs;
import org.apache.jena.fuseki.main.runner.FusekiRunner;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;

/**
 * Programmatic ways to create a server builder using command line syntax.
 * <p>
 * This also has a convenience ways to run a server.
 * </p><p>
 * If used in testing, the following may be used:
 * <pre>
 *    FusekiServer server = FusekiMain.runAync("--port=0", args);
 *    String URL = server.serverURL();
 *    // or server.getPort()
 *    try {
 *        ...
 *    } finally { server.stop(); }
 * </pre>
 * Using port 0 causes the kernel to allocate a free port.
 * It is important for test to close servers promptly.
 *
 * @see FusekiRunner
 * @see FusekiServer
 */
public class FusekiMain {

    /**
     * Run a server asynchronously based on the command line arguments.
     * Use {@link FusekiServer#join} to block the caller.
     */
    public static FusekiServer run(String... args) {
        return construct(args).start();
    }

    /**
     * Build, but do not start, a server based on command line syntax.
     * @deprecated Use {@link #construct(String...)}
     */
    @Deprecated(forRemoval = true)
    public static FusekiServer build(String... args) {
        return FusekiArgs.applyArgs(FusekiServer.create(), args).build();
    }

    /**
     * Build, but do not start, a server based on command line syntax.
     */
    public static FusekiServer construct(String... args) {
        return FusekiArgs.applyArgs(FusekiServer.create(), args).build();
    }

    /**
     * Build, but do not start, a server based on modules and the command line syntax.
     */
    public static FusekiServer construct(FusekiModules fusekiModules, String... args) {
        return FusekiArgs.applyArgs(FusekiServer.create(), fusekiModules, args).build();
    }

    /**
     * Create a {@link org.apache.jena.fuseki.main.FusekiServer.Builder FusekiServer.Builder} which has
     * been setup according to the command line arguments.
     * The builder can be further modified.
     * @param args Command line syntax
     */
    public static FusekiServer.Builder builder(String... args) {
        return builder(null, args);
    }

    /**
     * Create a {@link org.apache.jena.fuseki.main.FusekiServer.Builder FusekiServer.Builder} which has
     * been setup according to the command line arguments.
     * The builder can be further modified.
     * @param fusekiModules  {@link FusekiModule Fuseki modules} to include
     * @param args Command line syntax
     */
    public static FusekiServer.Builder builder(FusekiModules fusekiModules, String... args) {
        FusekiServer.Builder builder = FusekiServer.create();
        FusekiArgs.applyArgs(builder, fusekiModules, args);
        return builder;
    }
}
