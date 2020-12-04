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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A GraphView that is sensitive to {@link DatasetGraphSwitchable} switching.
 * This ensures that a graph object remains valid as the {@link DatasetGraphSwitchable} switches.
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

    private DatasetGraphTDB getDSG() {
        return ((DatasetGraphTDB)(getx().get()));
    }
}
