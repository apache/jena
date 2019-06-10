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

package org.apache.jena.fuseki.servlets;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

/** Target of GSP operations */
final class GSPTarget {
    final boolean      isDefault;
    final DatasetGraph dsg;
    private Graph      _graph;
    final String       name;
    final Node         graphName;

    static GSPTarget createNamed(DatasetGraph dsg, String name, Node graphName) {
        return new GSPTarget(false, dsg, name, graphName);
    }

    static GSPTarget createDefault(DatasetGraph dsg) {
        return new GSPTarget(true, dsg, null, null);
    }

    /**
     * Create a new Target which is like the original but aimed at a different
     * DatasetGraph
     */
    static GSPTarget retarget(GSPTarget target, DatasetGraph dsg) {
        GSPTarget target2 = new GSPTarget(target, dsg);
        target2._graph = null;
        return target2;
    }

    private GSPTarget(boolean isDefault, DatasetGraph dsg, String name, Node graphName) {
        this.isDefault = isDefault;
        this.dsg = dsg;
        this._graph = null;
        this.name = name;
        this.graphName = graphName;

        if ( isDefault ) {
            if ( name != null || graphName != null )
                throw new IllegalArgumentException("Inconsistent: default and a graph name/node");
        } else {
            if ( name == null || graphName == null )
                throw new IllegalArgumentException("Inconsistent: not default and/or no graph name/node");
        }
    }

    private GSPTarget(GSPTarget other, DatasetGraph dsg) {
        this.isDefault = other.isDefault;
        this.dsg = dsg; // other.dsg;
        this._graph = other._graph;
        this.name = other.name;
        this.graphName = other.graphName;
    }

    /**
     * Get a graph for the action - this may create a graph in the dataset - this is
     * not a test for graph existence
     */
    public Graph graph() {
        if ( !isGraphSet() ) {
            if ( isDefault )
                _graph = dsg.getDefaultGraph();
            else
                _graph = dsg.getGraph(graphName);
        }
        return _graph;
    }

    public boolean exists() {
        if ( isDefault )
            return true;
        return dsg.containsGraph(graphName);
    }

    public boolean isGraphSet() {
        return _graph != null;
    }

    @Override
    public String toString() {
        if ( isDefault )
            return "default";
        return name;
    }
}