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

package org.apache.jena.sparql.system;

import org.apache.jena.sparql.engine.dispatch.SparqlDispatcherRegistry;
import org.apache.jena.sparql.exec.tracker.ChainingQueryDispatcherExecTracker;
import org.apache.jena.sparql.exec.tracker.ChainingUpdateDispatcherExecTracker;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitExecTracking implements JenaSubsystemLifecycle {
    @Override
    public void start() {
        init();
    }

    public static void init() {
        SparqlDispatcherRegistry.addDispatcher(new ChainingUpdateDispatcherExecTracker());
        SparqlDispatcherRegistry.addDispatcher(new ChainingQueryDispatcherExecTracker());
    }

    @Override
    public void stop() {}

    /** Initialize very late so that custom dispatchers are registered before execution tracking. */
    @Override
    public int level() {
        return 999_999_999 ;
    }
}
