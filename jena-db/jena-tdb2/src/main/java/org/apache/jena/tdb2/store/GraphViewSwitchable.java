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
import org.apache.jena.dboe.storage.prefixes.*;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A GraphView that is sensitive to {@link DatasetGraphSwitchable} switching.
 * This ensures that a graph object remains valid as the {@link DatasetGraphSwitchable} switchs.
 */
public class GraphViewSwitchable extends GraphView {
    public static GraphViewSwitchable createDefaultGraphSwitchable(DatasetGraphSwitchable dsg)
    { return new GraphViewSwitchable(dsg, Quad.defaultGraphNodeGenerated); }

    public static GraphView createNamedGraphSwitchable(DatasetGraphSwitchable dsg, Node graphIRI)
    { return new GraphViewSwitchable(dsg, graphIRI); }

    public static GraphViewSwitchable createUnionGraphSwitchable(DatasetGraphSwitchable dsg)
    { return new GraphViewSwitchable(dsg, Quad.unionGraph); }

    private final DatasetGraphSwitchable dsgx;
    protected DatasetGraphSwitchable getx() { return dsgx; }

    protected GraphViewSwitchable(DatasetGraphSwitchable dsg, Node gn) {
        super(dsg, gn);
        this.dsgx = dsg;
    }

    // Some operations need to be caught and switched here, some don't 
    // Add/delete get switched because the DSG of the super class GraphView switches.
    // But we need a switching PrefixMapping.
    // We need a TransactionHandler.
    
    @Override
    protected PrefixMapping createPrefixMapping() {
        Node gn = super.getGraphName();
        if ( gn == Quad.defaultGraphNodeGenerated )
            gn = null;
        if ( Quad.isUnionGraph(gn) ) {
            // [TDBX] Union
            // Read-only wrapper would be better than a copy.
            PrefixMapping pmap = new PrefixMappingImpl();
            pmap.setNsPrefixes(prefixMapping(null));
            return pmap;
        }
        return prefixMapping(gn);
    }

    /** Return the {@link DatasetGraphSwitchable} we are viewing. */
    @Override
    public DatasetGraphSwitchable getDataset() {
        return getx();
    }

    /** 
     *  Return the {@code Graph} from the underlying DatasetGraph
     *  Do not hold onto this reference across switches.
     */
    public Graph getBaseGraph() {
        // Switchable.
        if ( getGraphName() == null )
            return getDSG().getDefaultGraph();
        else
            return getDSG().getGraph(getGraphName());
    }

    @Override
    protected int graphBaseSize() {
        return getBaseGraph().size();
    }

    @Override
    public void clear() {
        getBaseGraph().clear();
    }
    
    // Operations that GraphView provides but where the underlying switchable graph may be do better.
    // As the underlying graph is not a subclass, it can not override by inheritance.
    
    @Override
    public void sync() { } 
   
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Node s, Node p, Node o) {
        // This breaks the cycle because super.find will call here again.
        return getBaseGraph().find(s, p, o); 
    }

    // Not needed here because the union graph is a graph(unionGraph) so that redirects on use.
    // graphUnionFind(Node s, Node p, Node o) : see GraphTDB
    
    private DatasetGraphTDB getDSG() {
        return ((DatasetGraphTDB)(getx().get()));
    }

    private PrefixMapping prefixMapping(Node graphName) {
        PrefixMapI pmap = new PrefixMapTDB2(graphName);
        return PrefixesFactory.newPrefixMappingOverPrefixMapI(pmap);
    }

    class PrefixMapTDB2 extends PrefixMapIOverStorage {
        
        private final Node graphName;
    
        PrefixMapTDB2(Node graphName) {
            super(null);
            graphName = PrefixLib.canonicalGraphName(graphName);
            this.graphName = graphName;
        }
    
        @Override
        protected StoragePrefixMap spm() {
            StoragePrefixes prefixes = getDSG().getPrefixes();
            StoragePrefixMap view = PrefixLib.isNodeDefaultGraph(graphName)
                ? StoragePrefixesView.viewDefaultGraph(prefixes)
                : StoragePrefixesView.viewGraph(prefixes, graphName);
            return view;
        }
    }
}
