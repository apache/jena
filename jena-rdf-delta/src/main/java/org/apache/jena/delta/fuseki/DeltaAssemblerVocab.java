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

package org.apache.jena.delta.fuseki;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.delta.DeltaVocab;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register assemblers for Delta-related vocabulary.
 */
public class DeltaAssemblerVocab {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaAssemblerVocab.class);
    
    private static boolean initialized = false;
    
    /**
     * Initialize the Delta assembler vocabulary.
     */
    public static void init() {
        if (initialized) {
            return;
        }
        
        JenaSystem.init();
        registerAssemblers();
        initialized = true;
        
        LOG.debug("DeltaAssemblerVocab initialized");
    }
    
    /**
     * Register the Delta assemblers with the Jena assembler system.
     */
    private static void registerAssemblers() {
        // Register the Delta dataset assembler
        Assembler.general.implementWith(DeltaVocab.ReplicatedDataset, new DeltaDatasetAssembler());
    }
}