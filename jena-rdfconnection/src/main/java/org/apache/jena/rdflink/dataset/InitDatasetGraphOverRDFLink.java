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

package org.apache.jena.rdflink.dataset;

import org.apache.jena.rdflink.dataset.assembler.VocabAssemblerHTTP;
import org.apache.jena.sparql.engine.dispatch.SparqlDispatcherRegistry;
import org.apache.jena.sparql.system.InitARQ;
import org.apache.jena.sparql.system.InitExecTracking;
import org.apache.jena.sys.JenaSubsystemLifecycle;

/**
 * Initialize SPARQL dispatcher for {@link DatasetGraphOverRDFLink}.
 */
public class InitDatasetGraphOverRDFLink implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {}

    /** Initialize after {@link InitARQ} and before {@link InitExecTracking}. */
    @Override
    public int level() {
        return 40 ;
    }

    private static boolean initialized = false;

    public synchronized static void init() {
        if (!initialized) {
            initialized = true;

            SparqlDispatcherRegistry.addDispatcher(new ChainingQueryDispatcherForDatasetGraphOverRDFLink());
            SparqlDispatcherRegistry.addDispatcher(new ChainingUpdateDispatcherForDatasetGraphOverRDFLink());

            VocabAssemblerHTTP.init();
        }
    }
}
