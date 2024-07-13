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

package org.apache.jena.sparql.core.assembler;

import static org.apache.jena.sparql.core.assembler.AssemblerUtils.loadData;
import static org.apache.jena.sparql.core.assembler.AssemblerUtils.mergeContext;

import java.util.Map;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.vocabulary.RDF;

/**
 * An {@link Assembler} that creates in-memory {@link Dataset}s.
 * <p>
 * Dataset can be shared by using {@code ja:name}.
 */
public class InMemDatasetAssembler extends NamedDatasetAssembler {

    public InMemDatasetAssembler() {}

    @Override
    public Map<String, DatasetGraph> pool() {
        return sharedDatasetPool;
    }

    public static Resource getType() {
        return DatasetAssemblerVocab.tMemoryDataset ;
    }

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        // Old name
        if ( ! root.hasProperty( RDF.type, DatasetAssemblerVocab.tDatasetTxnMem ) )
            checkType(root, DatasetAssemblerVocab.tMemoryDataset);
        final DatasetGraph dataset = DatasetGraphFactory.createTxnMem();
        // Load from JA.data
        loadData(dataset, root);

        mergeContext(root, dataset.getContext());
        return dataset;
    }
}
