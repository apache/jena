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

package org.apache.jena.tdb.store;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapWrapper;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb.sys.TDBInternal;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction;

/**
 * Alternative GraphTxnTDB that has its own prefixes.
 * These prefixes are "switchable", that is, valid across transaction boundaries.
 */
public class GraphTxnTDB_Prefixes extends GraphTxnTDB {
    private final DatasetGraphTransaction dsgx;
    private final Node graphName;

    // Default graph..
    public GraphTxnTDB_Prefixes(DatasetGraphTransaction dataset) {
        this(dataset, null);
    }

    // Named graph.
    public GraphTxnTDB_Prefixes(DatasetGraphTransaction dataset, Node graphName) {
        super(dataset, graphName);
        this.dsgx = dataset;
        this.graphName = graphName;
    }

    @Override
    protected PrefixMapping createPrefixMapping() {
        PrefixMap pmap = new PrefixMapSwitchableTDB1(dsgx, graphName);
        return Prefixes.adapt(pmap);
    }

    /**
     * "switchable" prefix mapping backed by storage in DatasetPrefixesTDB
     */
    public static class PrefixMapSwitchableTDB1 extends PrefixMapWrapper {

        private final DatasetGraphTransaction dsgx;
        private final Node graphName;
        private final boolean isDefault;

        public PrefixMapSwitchableTDB1(DatasetGraphTransaction dsgx, Node graphName) {
            super(null);
            this.dsgx = dsgx;
            this.graphName = graphName;
            this.isDefault = (graphName==null)?true: Quad.isDefaultGraph(graphName);
        }

        private PrefixMap getPrefixMap() {
            DatasetPrefixesTDB pm = TDBInternal.getDatasetGraphTDB(dsgx).getStoragePrefixes();
            if ( isDefault )
                // NB Uses a speific URI for the default graph. "" is the dataset.
                return pm.getPrefixMap(Quad.defaultGraphNodeGenerated.getURI());
            return pm.getPrefixMap(graphName.getURI());
        }

        @Override
        protected PrefixMap getR() {
            return getPrefixMap();
        }

        @Override
        protected PrefixMap getW() {
            dsgx.requireWrite();
            return getPrefixMap();
        }
    }
}
