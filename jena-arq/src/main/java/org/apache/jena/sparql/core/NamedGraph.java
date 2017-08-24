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
import org.apache.jena.graph.Node ;

/**
 * A graph with an associated name.
 * <p>
 * Sometimes there is an associated name with a graph, where it lives on the web,
 * or the name in a dataset.
 * <p>
 * This interface is for graph that have one associated name.
 * What "associated" means is left open.  
 * 
 * @see GraphView
 * @see NamedGraphWrapper
 */

public interface NamedGraph extends Graph {
    /**
     * Return the graph name for this graph.
     * Blank nodes can be used.
     * <p>
     * A named graph of "null" is discouraged - use {@link Quad#defaultGraphIRI} 
     * for a default graph in the context of use.
     */
    public Node getGraphName();
}
