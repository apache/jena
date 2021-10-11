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

import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.apache.jena.sys.JenaSystem;

/**
 * Jena initialization.
 * Level 101 - called during Jena system initialization
 * and after Jena itself has initialized.
 */
public class InitFuseki implements JenaSubsystemLifecycle {

    private static volatile boolean initialized = false;

    @Override
    public void start() { init(); }

    @Override
    public void stop() {}

    @Override
    public int level() { return 101; }

    /**
     * Load modules, call "start()".
     * Each {@link FusekiModule} will be called during the server build process.
     */
    public static void init() {
        if ( initialized ) {
            return;
        }
        synchronized (InitFuseki.class) {
            if ( initialized ) {
                JenaSystem.logLifecycle("Fuseki.init - skip");
                return;
            }
            initialized = true;
            JenaSystem.logLifecycle("Fuseki.init - start");
            FusekiModules.load();
            JenaSystem.logLifecycle("Fuseki.init - finish");
        }
    }
}
