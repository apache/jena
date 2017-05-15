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
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.riot.RDFDataMgr.read;
import static org.apache.jena.sparql.core.assembler.AssemblerUtils.setContext;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphName;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;
import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue;
import static org.apache.jena.sparql.util.graph.GraphUtils.multiValueResource;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;

/**
 * An {@link Assembler} that creates transactional in-memory {@link Dataset}s.
 */
public abstract class TransactionalInMemDatasetAssembler extends AssemblerBase {

    public abstract Resource getType();

    public abstract Dataset createDataset();

    @Override
    public Dataset open(final Assembler assembler, final Resource root, final Mode mode) {
        checkType(root, getType());
        final Dataset dataset = createDataset();
        setContext(root, dataset.getContext());

        dataset.begin(WRITE);
        try {
            loadDefaultGraph(root, dataset);
            loadNamedGraphs(root, dataset);
            dataset.commit();
        } finally {
            dataset.end();
        }
        return dataset;
    }

    protected void loadNamedGraphs(final Resource root, final Dataset dataset) {
        multiValueResource(root, pNamedGraph).forEach(namedGraphResource -> {
            final String graphName = getAsStringValue(namedGraphResource, pGraphName);
            if (namedGraphResource.hasProperty(data))
                multiValueResource(namedGraphResource, data)
                        .forEach(namedGraphData -> read(dataset.getNamedModel(graphName), namedGraphData.getURI()));
        });
    }

    protected void loadDefaultGraph(final Resource root, final Dataset dataset) {
        if (root.hasProperty(data))
            multiValueResource(root, data)
                .forEach(defaultGraphDocument -> read(dataset, defaultGraphDocument.getURI()));
    }
}
