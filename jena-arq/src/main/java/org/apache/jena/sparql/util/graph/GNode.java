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

package org.apache.jena.sparql.util.graph;

import java.util.Collection;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern ;


/**
 * A {@code GNode} pair of (graph, node in graph) with an abstracted "findable"
 * operation so it work for graphs and collections of triples.
 * <p>
 * See {@link GraphList}.
 */
public class GNode
{
    public static GNode create(Graph graph, Node node) {
        return new GNode(graph, node);
    }

    public static GNode subject(Graph graph, Triple triple) {
        return triple == null ? null : create(graph, triple.getSubject());
    }

    public static GNode predicate(Graph graph, Triple triple) {
        return triple == null ? null : create(graph, triple.getPredicate());
    }

    public static GNode object(Graph graph, Triple triple) {
        return triple == null ? null : create(graph, triple.getObject());
    }
    
    public final Findable findable ;
    public final Node node ;
    
    public GNode(Graph graph, Node node)
    { this.findable = new FindableGraph(graph) ; this.node = node ; }
    
    public GNode(BasicPattern triples, Node node)
    { this.findable = new FindableCollection(triples.getList()) ; this.node = node ; }
    
    public GNode(Collection<Triple> triples, Node node)
    { this.findable = new FindableCollection(triples) ; this.node = node ; }

    public GNode(GNode other, Node node)
    { this.findable = other.findable ; this.node = node ; }

    @Override
    public String toString() { return "gnode:"+node ; }
}
