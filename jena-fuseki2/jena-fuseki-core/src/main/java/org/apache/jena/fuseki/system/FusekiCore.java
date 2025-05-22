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

package org.apache.jena.fuseki.system;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb1.transaction.TransactionManager;

/**
 * Fuseki core initialization.
 * <p>
 * Initialize an instance of the Fuseki server core code.
 * This is not done via Jena's initialization mechanism
 * but done explicitly to give control.
 * <p>
 * It is done after Jena initializes.
 * <p>
 * {@code InitFusekiMain} is the code for Jena's initialization mechanism.
 * It calls this class.
 */
public class FusekiCore {
    // This is not triggered by a services file.

    private static boolean initialized = false;

    @SuppressWarnings("removal")
    public synchronized static void init() {
        if ( initialized )
            return;

        // Avoid re-entrancy.
        initialized = true;

        JenaSystem.init();
        // Touch the Fuseki class to make sure statics get initialized.
        Fuseki.initConsts();
        MappingRegistry.addPrefixMapping("fuseki", Fuseki.FusekiSymbolIRI);

        // TDB1
        TDB1.setOptimizerWarningFlag(false);
        // Don't use TDB1 batch commits.
        // This can be slower, but it less memory hungry and more predictable.
        TransactionManager.QueueBatchSize = 0;
    }
}
