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

package org.apache.jena.sparql.core;

import java.util.Iterator ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node ;


/** A DatasetGraph base class for pure quad-centric storage.
 */
public abstract class DatasetGraphQuads extends DatasetGraphBase
{
    @Override
    public Iterator<Node> listGraphNodes() {
        Iter<Quad> iter = Iter.iter(find(Node.ANY, Node.ANY, Node.ANY, Node.ANY));
        return iter.map(Quad::getGraph).distinct();
    }

    @Override
    public void removeGraph(Node graphName) {
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY);
    }

    // See DatasetGraphViewGraphs
    // If that is merged to DatasetGraphBase, these are unnecessary.

    @Override
    public void addGraph(Node graphName, Graph graph) {
        graph.find().forEachRemaining(t -> add(Quad.create(graphName, t)));
    }

//    @Override
//    public Graph getDefaultGraph() {
//        return GraphView.createDefaultGraph(this);
//    }
//
//    @Override
//    public Graph getGraph(Node graphNode) {
//        return GraphView.createNamedGraph(this, graphNode);
//    }

    @Override
    public abstract Iterator<Quad> find(Node g, Node s, Node p, Node o);

    @Override
    public abstract Iterator<Quad> findNG(Node g, Node s, Node p, Node o);

    @Override
    public abstract void add(Quad quad);

    @Override
    public abstract void delete(Quad quad);
}
