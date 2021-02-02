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

package org.apache.jena.sparql.graph;

import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;

/** Some operations on graphs */ 
public class GraphOps {
    /** 
     * Check whether a dataset contains a named graph of the given name.
     * Graph with special names (union and default) return true.
     */
    public static boolean containsGraph(DatasetGraph dsg, Node gn) {
        if ( Quad.isDefaultGraph(gn) || Quad.isUnionGraph(gn)  )
            return true ;
        return dsg.containsGraph(gn) ;
    }
    
    /** Get a graph from the dataset - the graph name may be special
     * - the union graph (which is immutable) or a special name for
     * the default graph. 
     * <p>
     * A graph name of "null" is interpreted as the default graph.
     */
    public static Graph getGraph(DatasetGraph dsg, Node gn) {
        if ( gn == null )
            return dsg.getDefaultGraph() ;
        if ( Quad.isDefaultGraph(gn) )
            // Explicit or generated.
            return dsg.getDefaultGraph() ;
        if ( Quad.isUnionGraph(gn) )
            return unionGraph(dsg) ;
        return dsg.getGraph(gn) ;
    }

    /** Create an immutable union graph comprised of a set of named graphs. */
    public static Graph unionGraph(DatasetGraph dsg, Set<Node> graphNames) {
        return new GraphUnionRead(dsg, graphNames) ;
    }

    /** Create an immutable union graph of all the named graphs in the dataset.
     * Future changes to the set of graphs in the dataset will be seen.
     */   
    public static Graph unionGraph(DatasetGraph dsg) {
        return new GraphUnionRead(dsg) ;
    }

    public static void addAll(Graph g, Iterator<Triple> iter) {
        while (iter.hasNext())
            g.add(iter.next()) ;
        Iter.close(iter) ;
    }

    public static void addAll(Graph g, Iterable<Triple> iter) {
        addAll(g, iter.iterator()) ;
    }

    public static void deleteAll(Graph g, Iterator<Triple> iter) {
        while (iter.hasNext())
            g.delete(iter.next()) ;
        Iter.close(iter) ;
    }

    public static void deleteAll(Graph g, Iterable<Triple> iter) {
        deleteAll(g, iter.iterator()) ;
    }

    /** Remove all layers of graph wrapping. Returns the original graph is not wrapped at all.*/
    public static Graph unwrapAll(Graph graph) {
        Graph graph1 = graph;
        for (;;) {
            Graph graph2 = unwrapOne(graph1);
            if ( graph2 == graph1 )
                return graph1;
            graph1 = graph2;
        }
    }

    /** Remove one layer of graph wrapping. */
    public static boolean isWrapped(Graph graph) {
        if ( graph instanceof WrappedGraph ) return true;
        if ( graph instanceof GraphWrapper ) return true;
        return false;
    }
        
    /** Remove one layer of graph wrapping. Returns the orinalk graph is not wrapped at all. */
    public static Graph unwrapOne(Graph graph) {
        if ( graph instanceof WrappedGraph )
            // WrappedGraph is a GraphWithPerform
            return ((WrappedGraph)graph).getWrapped();
        if ( graph instanceof GraphWrapper )
            // GraphWrapper is a pure wrapper.
            return ((GraphWrapper)graph).get();
        return graph;
    }
}
