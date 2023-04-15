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

package org.apache.jena.sys;

import org.apache.jena.base.module.Subsystem;
import org.apache.jena.base.module.SubsystemRegistry;
import org.apache.jena.base.module.SubsystemRegistryServiceLoader;

/** Jena "system" - simple controls for ensuring components are loaded and initialized.
 * <p>
 * All initialization should be concurrent and thread-safe.  In particular,
 * some subsystems need initialization in some sort of order (e.g. ARQ before TDB).
 * <p>
 * This is achieved by "levels": levels less than 500 are considered "Jena system levels"
 * and are reserved.
 * <ul>
 * <li>0 - reserved
 * <li>10 - jena-core
 * <li>20 - RIOT
 * <li>30 - ARQ
 * <li>40 - TDB
 * <li>50-100 - Other Jena system modules.
 * <li>101 - Fuseki
 * <li>102-9998 - Application
 * <li>9999 - other
 * </ul>
 * See also the <a href="http://jena.apache.org/documentation/notes/system-initialization.html">notes on Jena initialization</a>.
 */
public class JenaSystem {

    private static Subsystem<JenaSubsystemLifecycle> singleton = null;

    // Don't rely on class initialization.
    private static void setup() {
        // Called inside synchronized
        if ( singleton == null ) {
            singleton = new Subsystem<>(JenaSubsystemLifecycle.class);
            SubsystemRegistry<JenaSubsystemLifecycle> reg =
                    new SubsystemRegistryServiceLoader<>(JenaSubsystemLifecycle.class);
            singleton.setSubsystemRegistry(reg);
            reg.add(new JenaInitLevel0());
        }
    }

    public JenaSystem() { }

    /**
     * Development support - flag to enable output during
     * initialization. Output to {@code System.err}, not a logger
     * to avoid the risk of recursive initialization.
     */
    public static boolean DEBUG_INIT = false ;
    private static volatile boolean initialized = false ;

    /** Output a debugging message if DEBUG_INIT is set */
    public static void logLifecycle(String fmt, Object ...args) {
        if ( ! DEBUG_INIT )
            return ;
        System.err.printf(fmt, args) ;
        System.err.println() ;
    }

    public static void init() {
        if ( initialized )
            return ;
        synchronized(JenaSystem.class) {
            if ( initialized )
                return ;
            initialized = true;
            setup();
            if ( DEBUG_INIT )
                singleton.debug(DEBUG_INIT);
            singleton.initialize();
            singleton.debug(false);
        }
    }

    public static void shutdown() { singleton.shutdown(); }

    /** The level 0 subsystem - inserted without using the Registry load function.
     *  There should be only one such level 0 handler.
     */
    private static class JenaInitLevel0 implements JenaSubsystemLifecycle {
        @Override
        public void start() {
            logLifecycle("Jena initialization");
        }

        @Override
        public void stop() {
            logLifecycle("Jena shutdown");
        }

        @Override
        public int level() {
            return 0;
        }
    }
}
