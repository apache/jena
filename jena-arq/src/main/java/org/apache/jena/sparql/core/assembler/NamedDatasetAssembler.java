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

package org.apache.jena.sparql.core.assembler ;

import static org.apache.jena.sparql.util.graph.GraphUtils.getStringValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Dataset assembler supporting shared resources by {@code ja:name}.
 */
public abstract class NamedDatasetAssembler extends DatasetAssembler {

    public static final Map<String, DatasetGraph> sharedDatasetPool = new ConcurrentHashMap<>();

    protected NamedDatasetAssembler() {}

    @Override
    public final DatasetGraph createNamedDataset(Assembler a, Resource root) {
        String name = getStringValue(root, DatasetAssemblerVocab.pDatasetName);
        var pool = pool();
        if ( name != null && pool != null ) {
            DatasetGraph dsg = pool().computeIfAbsent(name, n->createDataset(a,root));
            if ( dsg != null )
                return dsg;
        }
        // No name or no pool.
        return createDataset(a, root);
    }

    /** Shared {@link DatasetGraph} objects */
    public abstract Map<String, DatasetGraph> pool();
}
