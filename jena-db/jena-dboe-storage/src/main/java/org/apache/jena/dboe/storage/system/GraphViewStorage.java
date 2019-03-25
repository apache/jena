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

package org.apache.jena.dboe.storage.system;

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.dboe.storage.prefixes.PrefixMapI;
import org.apache.jena.dboe.storage.prefixes.PrefixesFactory;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixMap;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixesView;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;

/**
 * General operations for graphs over datasets where the datasets are {@link StorageRDF} based.
 */
// Can't merge into GraphView because of StoragePrefixes
public class GraphViewStorage extends GraphView {
    private final StoragePrefixMap  prefixes;

    public static GraphView createDefaultGraphStorage(DatasetGraph dsg, StoragePrefixes prefixes)
    { return new GraphViewStorage(dsg, Quad.defaultGraphNodeGenerated, prefixes); }

    public static GraphView createNamedGraphStorage(DatasetGraph dsg, Node graphIRI, StoragePrefixes prefixes)
    { return new GraphViewStorage(dsg, graphIRI, prefixes); }

    public static GraphView createUnionGraphStorage(DatasetGraph dsg, StoragePrefixes prefixes)
    { return new GraphViewStorage(dsg, Quad.unionGraph, prefixes); }

    protected GraphViewStorage(DatasetGraph dataset, Node graphName, StoragePrefixes prefixes) {
        super(dataset, graphName);
        this.prefixes = StoragePrefixesView.viewGraph(prefixes, graphName);
    }

    @Override
    protected PrefixMapping createPrefixMapping() {
        PrefixMapI x = PrefixesFactory.newPrefixMap(prefixes);
        PrefixMapping pm = PrefixesFactory.newPrefixMappingOverPrefixMapI(x);
        return pm;
    }
    
    @Override
    public void clear() {
        // GraphView uses null. 
        Node gn = getGraphName();
        if ( gn == null )
            gn = Quad.defaultGraphNodeGenerated;
        getDataset().deleteAny(gn, Node.ANY, Node.ANY, Node.ANY);
        getEventManager().notifyEvent(this, GraphEvents.removeAll);
    }
}
