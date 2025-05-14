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

package org.apache.jena.delta.server.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.AssemblerFactory;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.delta.DeltaVocab;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;

/**
 * RDF Delta assembler vocabulary and initialization.
 */
public class DeltaAssemblerVocab {
    
    private static boolean initialized = false;
    
    /**
     * Initialize the RDF Delta assemblers.
     * This is normally called during system initialization.
     */
    public static void init() {
        if (initialized)
            return;
        initialized = true;
        
        // Register the assemblers
        AssemblerUtils.init();
        registerWith(Assembler.general);
    }
    
    /**
     * Register the RDF Delta assemblers with an assembler group.
     */
    public static void registerWith(AssemblerGroup group) {
        // Register the ReplicatedDataset assembler
        AssemblerFactory af = new ReplicatedDatasetAssembler.Factory();
        group.implementWith(DeltaVocab.ReplicatedDataset, af);
    }
}