/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.jena.sparql.core.assembler;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.apache.jena.assembler.JA.data;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.riot.RDFDataMgr.read;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphName;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;
import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue;
import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue;
import static org.apache.jena.sparql.util.graph.GraphUtils.multiValueResource;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraphPerGraphLocking;

/**
 * An {@link Assembler} that creates writer-per-graph in-memory {@link Dataset}s.
 */
public class WriterPerGraphDatasetAssembler extends TransactionalInMemDatasetAssembler {

    @Override
    public Resource getType() {
        return DatasetAssemblerVocab.tDatasetPGraphWriter;
    }

    @Override
    public Dataset createDataset() {
        return DatasetFactory.wrap(new DatasetGraphPerGraphLocking());
    }

    @Override
    protected void loadNamedGraphs(Resource root, Dataset dataset) {
        List<Resource> namedGraphs = multiValueResource(root, pNamedGraph);
        Resource parallelizeValue = getResourceValue(root, DatasetAssemblerVocab.pParallelize);
        // defaults to false
        boolean parallelize = parallelizeValue != null ? parallelizeValue.asLiteral().getBoolean() : false;
        if (parallelize) {
            // take advantage of writer-per-graph to load in parallel, one thread per named graph
            ExecutorService loaderThreadPool = newFixedThreadPool(namedGraphs.size());
            try {
                loaderThreadPool.submit(() -> namedGraphs.parallelStream().forEach(loadIntoDataset(dataset))).get();
            } catch (InterruptedException | ExecutionException e) {
                loaderThreadPool.shutdownNow();
                throw new JenaException(e);
            }
            loaderThreadPool.shutdown();
        } else 
            // load using only this thread, default mode
            namedGraphs.forEach(namedGraphResource -> loadNamedGraph(dataset, namedGraphResource));
    }
    
    private static Consumer<Resource> loadIntoDataset(Dataset ds) {
        return namedGraphResource -> {
            ds.begin(WRITE);
            try {
                loadNamedGraph(ds, namedGraphResource);
                ds.commit();
            } finally {
                ds.end();
            }
        };
    }

    private static void loadNamedGraph(Dataset dataset, Resource namedGraphResource) {
        final String graphName = getAsStringValue(namedGraphResource, pGraphName);
        if (namedGraphResource.hasProperty(data)) multiValueResource(namedGraphResource, data)
                .forEach(namedGraphData -> read(dataset.getNamedModel(graphName), namedGraphData.getURI()));
    }
}
