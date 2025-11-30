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

package org.apache.jena.rdflink.dataset;

import org.apache.jena.sparql.exec.QueryExecBuilderRegistry;
import org.apache.jena.sparql.exec.UpdateExecBuilderRegistry;
import org.apache.jena.sparql.system.InitARQ;
import org.apache.jena.sys.JenaSubsystemLifecycle;

/**
 * Initialize adapters for {@link DatasetGraphOverRDFLink}.
 */
public class InitDatasetGraphOverRDFLink implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {}

    /**
     * Must initialize after {@link InitARQ} because otherwise ARQ would always take precedence.
     * Must also initialize before certain extensions such as execution tracking.
     */
    @Override
    public int level() {
        return 40 ;
    }

    private static boolean initialized = false;

    public synchronized static void init() {
        if (!initialized) {
            initialized = true;

            QueryExecBuilderRegistry.addFactory(QueryExecBuilderFactoryOverRDFLink.get());
            UpdateExecBuilderRegistry.addFactory(UpdateExecBuilderFactoryOverRDFLink.get());
        }
    }
}
