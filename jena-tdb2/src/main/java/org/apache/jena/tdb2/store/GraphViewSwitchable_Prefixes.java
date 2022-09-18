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

package org.apache.jena.tdb2.store;

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.prefixes.PrefixesDboeFactory;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixMap;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixesView;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;

/**
 * TDB2 Graph that has its own prefixes, separate from the datasets.
 * See {@code GraphTxnTDB_Prefixes} for TDB1 version.
 */

public class GraphViewSwitchable_Prefixes extends GraphViewSwitchable {
    // Same as GraphViewSwitchable except it returns a switching PrefixMapSwitchable
    // that picks from the StoragePrefixes using the graph name.

    public GraphViewSwitchable_Prefixes(DatasetGraphSwitchable dsg, Node gn) {
        super(dsg, gn);
    }

    public GraphViewSwitchable_Prefixes(DatasetGraphSwitchable dsg) {
        // Agrees with StoragePrefixesView
        super(dsg, Quad.defaultGraphNodeGenerated);
    }

    @Override
    protected PrefixMapping createPrefixMapping() {
        // Switchable via getBaseGraph.
        PrefixMap pm = new PrefixMapSwitchable(getx()) {
            @Override
            protected PrefixMap getR() { return createPrefixMappingX(); }

            @Override
            protected PrefixMap getW() { return createPrefixMappingX(); }
        };
        return Prefixes.adapt(pm);
    }

    // Create prefix map view.
    private PrefixMap createPrefixMappingX() {
        DatasetGraphTDB dsg = ((DatasetGraphTDB)(getx().get()));
        StoragePrefixes storagePrefixes = dsg.getStoragePrefixes();
        StoragePrefixMap spm = isDefaultGraph()
                ? StoragePrefixesView.internal_viewDefaultGraph(storagePrefixes)
                : StoragePrefixesView.internal_viewGraph(storagePrefixes, getGraphName());

        return PrefixesDboeFactory.newPrefixMap(spm);
    }
}
