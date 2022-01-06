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

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Node ;

/**
 * A DatasetGraph base class for triples+quads storage.
 */
public abstract class DatasetGraphTriplesQuads extends DatasetGraphBaseFind
{
    @Override
    final public void add(Quad quad) {
        add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    @Override
    final public void delete(Quad quad) {
        delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    @Override
    final public void add(Node g, Node s, Node p, Node o) {
        if ( Quad.isDefaultGraph(g) )
            addToDftGraph(s, p, o) ;
        else
            addToNamedGraph(g, s, p, o) ;
    }

    @Override
    final public void delete(Node g, Node s, Node p, Node o) {
        if ( Quad.isDefaultGraph(g) )
            deleteFromDftGraph(s, p, o) ;
        else
            deleteFromNamedGraph(g, s, p, o) ;
    }

    protected abstract void addToDftGraph(Node s, Node p, Node o) ;
    protected abstract void addToNamedGraph(Node g, Node s, Node p, Node o) ;
    protected abstract void deleteFromDftGraph(Node s, Node p, Node o) ;
    protected abstract void deleteFromNamedGraph(Node g, Node s, Node p, Node o) ;

//    // Ensure we loop back here
//    @Override
//    public Graph getDefaultGraph() {
//        return GraphView.createDefaultGraph(this) ;
//    }
//
//    @Override
//    public Graph getGraph(Node graphNode) {
//        return GraphView.createNamedGraph(this, graphNode) ;
//    }

    // Default implementations - copy based.

    @Override
    public void setDefaultGraph(Graph graph) {
        GraphUtil.addInto(getDefaultGraph(), graph) ;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        GraphUtil.addInto(getGraph(graphName), graph) ;
    }

    @Override
    public void removeGraph(Node graphName) {
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY) ;
    }
}
