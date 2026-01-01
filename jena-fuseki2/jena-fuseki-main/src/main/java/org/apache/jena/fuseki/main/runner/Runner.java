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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main.runner;

import org.apache.jena.fuseki.main.FusekiServer;

/**
 * Operations for running Fuseki.
 * <p>
 * The server always has multiple threads.
 * <p>
 * A server can be run asynchronously by calling {@link #runAsync}.
 * The server is started and then the server object is returned to the caller.
 * <p>
 * Alternatively, the server can be run by calling {@link #exec}
 * which is blocking. It is equivalent to {@code runAsync(args).join()}.
 * This function does not return unless an exception is thrown.
 * <p>
 * @see FusekiRunner for some provided configurations of Fuseki.
 */
public interface Runner {
    /**
     * Setup a {@link FusekiServer} from the command line options and run it asynchronously.
     * @param args line arguments.
     * @return Return the running server.
     */
    public FusekiServer runAsync(String...args);

    /**
     * Setup a {@link FusekiServer} from the command line options and run it.
     * This function does not return.
     * @param args line arguments.
     */
    public void exec(String... args);

    /**
     * Setup a {@link FusekiServer} from the command line options.
     * The returned server has not been started.
     * @param args line arguments.
     */
    public FusekiServer construct(String... args);
}
