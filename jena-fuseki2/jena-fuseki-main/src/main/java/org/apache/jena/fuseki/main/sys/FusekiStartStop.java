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

package org.apache.jena.fuseki.main.sys;

import org.apache.jena.fuseki.main.FusekiServer;

/** Interface for server starting and stopping.
 * <p>
 * At server start-up:
 * <ul>
 * <li>{@linkplain #serverBeforeStarting(FusekiServer)} -- called before {@code server.start} happens.</li>
 * <li>{@linkplain #serverAfterStarting(FusekiServer)} -- called after {@code server.start} happens.</li>
 * <li>{@linkplain #serverStopped(FusekiServer)} -- call after {@code server.stop}, but only if a clean shutdown happens.
 *     Servers may simply exit without a shutdown phase.
 *     The JVM may exit or be killed without clean shutdown.
 *     Modules must not rely on a call to {@code serverStopped} happening.</li>
 * </ul>
 */
public interface FusekiStartStop {
    /**
     * Server starting - called just before server.start happens.
     */
    public default void serverBeforeStarting(FusekiServer server) { }

    /**
     * Server started - called just after server.start happens, and before server
     * .start() returns to the application.
     */
    public default void serverAfterStarting(FusekiServer server) { }

    /**
     * Server stopping.
     * Do not rely on this called; do not rely on this to clear up external resources.
     * Usually there is no stop phase and the JVM just exits or is killed externally.
     */
    public default void serverStopped(FusekiServer server) { }

}
