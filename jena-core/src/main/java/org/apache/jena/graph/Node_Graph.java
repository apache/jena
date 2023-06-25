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

package org.apache.jena.graph;

import org.apache.jena.shared.PrefixMapping;

/** RDF Graphs as RDF terms.
 * <p>
 * Beware that equality and hashCode are defined by
 * object identity, not graph same-triples nor isomorphism
 * due to the costs.
 * <p>
 * For experimentation.
 * <br/>
 * Otherwise, unsupported.
 */
public class Node_Graph extends Node {

    private final Graph graph;

    public Node_Graph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public boolean isNodeGraph() {
        return true;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public Object visitWith(NodeVisitor v) {
        return v.visitGraph(this, graph);
    }

    // Note - object identity equality!

    @Override
    public int hashCode() {
        return System.identityHashCode(getGraph())*31;
    }

    @Override
    public boolean equals(Object o) {
        if ( o == this )
            return true;
        if ( !(o instanceof Node_Graph) )
            return false;
        Node_Graph other = (Node_Graph)o;
        return this.getGraph() == other.getGraph();
    }

    @Override
    public boolean isConcrete() {
        // Safe answer!
        return false;
    }

    @Override
    public String toString( PrefixMapping pmap ) { return toString(); }

    @Override
    public String toString() {
        return "Node_Graph";
    }
}
