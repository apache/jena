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

import static org.apache.jena.assembler.JA.data;
import static org.apache.jena.riot.RDFDataMgr.read;
import static org.apache.jena.sparql.core.assembler.AssemblerUtils.mergeContext;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphName;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;
import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue;
import static org.apache.jena.sparql.util.graph.GraphUtils.multiValueAsString;
import static org.apache.jena.sparql.util.graph.GraphUtils.multiValueResource;

import java.util.Map;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.system.Txn;
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

        Txn.executeWrite(dataset, ()->{
            // Load data into the default graph
            // This also loads quads into the dataset.
            multiValueAsString(root, data)
                .forEach(dataURI -> read(dataset, dataURI));

            // load data into named graphs
            multiValueResource(root, pNamedGraph).forEach(namedGraphResource -> {
                final String graphName = getAsStringValue(namedGraphResource, pGraphName);
                if (namedGraphResource.hasProperty(data)) {
                    multiValueAsString(namedGraphResource, data)
                            .forEach(namedGraphData -> {
                                Node gn = NodeFactory.createURI(graphName);
                                read(dataset.getGraph(gn), namedGraphData);
                            });
                }
            });
        });
        mergeContext(root, dataset.getContext());
        return dataset;
    }
}
