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

package org.apache.jena.enhanced;

import org.apache.jena.graph.* ;
import org.apache.jena.rdf.model.RDFNode ;

/**
 *  {@code EnhGraph} wraps a {@link Graph plain graph}
 *  and contains {@link EnhNode enhanced nodes} that wrap the
 *  plain nodes of the plain graph. All the enhanced nodes in
 *  the enhanced graph share the same polymorphic personality.
 */

public class EnhGraph
{
    // Instance variables
    /** The graph that this enhanced graph is wrapping */
    protected Graph graph;

    /** The unique personality that is bound to this polymorphic instance */
    private Personality<RDFNode> personality;

    // Constructors
    /**
     * Construct an enhanced graph from the given underlying graph, and
     * a factory for generating enhanced nodes.
     *
     * @param g The underlying plain graph, may be null to defer binding to a given
     *      graph until later.
     * @param p The personality factory, that maps types to realizations
     */
    public EnhGraph( Graph g, Personality<RDFNode> p ) {
        super();
        graph = g;
        personality = p;
    }

    /**
     * Answer the normal graph that this enhanced graph is wrapping.
     * @return A graph
     */
    public Graph asGraph() {
        return graph;
    }

    /**
     * Hashcode for an enhanced graph is delegated to the underlying graph.
     * @return The hashcode as an int
     */
    @Override final public int hashCode() {
     	return graph.hashCode();
    }

    /**
     * An enhanced graph is equal to another graph g iff the underlying graphs
     * are equal.
     * This  is deemed to be a complete and correct interpretation of enhanced
     * graph equality, which is why this method has been marked final.
     * <p> Note that this equality test does not look for correspondence between
     * the structures in the two graphs.  To test whether another graph has the
     * same nodes and edges as this one, use {@link #isIsomorphicWith}.
     * </p>
     * @param o An object to test for equality with this node
     * @return True if o is equal to this node.
     * @see #isIsomorphicWith
     */
    @Override final public boolean equals(Object o) {
        return
            this == o
            || o instanceof EnhGraph && graph.equals(((EnhGraph) o).asGraph());
    }

    /**
     * Answer true if the given enhanced graph contains the same nodes and
     * edges as this graph.  The default implementation delegates this to the
     * underlying graph objects.
     *
     * @param eg A graph to test
     * @return True if eg is a graph with the same structure as this.
     */
    final public boolean isIsomorphicWith(EnhGraph eg) {
        return graph.isIsomorphicWith(eg.graph);
    }

    /**
     * Answer an enhanced node that wraps the given node and conforms to the given
     * interface type.
     *
     * @param n A node (assumed to be in this graph)
     * @param interf A type denoting the enhanced facet desired
     * @return An enhanced node
     */
    public <X extends RDFNode> X getNodeAs( Node n, Class<X> interf ) {
        X constructed = personality.newInstance(interf, n, this) ;
        return constructed ;
    }

    /**
     * Answer the personality object bound to this polymorphic instance
     *
     * @return The personality object
     */
    protected Personality<RDFNode> getPersonality() {
        return personality;
    }
}
