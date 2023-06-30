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

import java.util.Iterator;

import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.mem2.GraphMem2Fast;
import org.apache.jena.mem2.GraphMem2Legacy;
import org.apache.jena.mem2.GraphMem2Roaring;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;

/**
 * A factory class for creating memory Graphs.
 * <p>
 * Apache Jena is migrating to term semantics graph for consistency across all in-memory and persistent storage graphs
 *
 */
public class GraphMemFactory
{
    // Default for sameTerm/sameValue
    private static boolean defaultSameTerm = false;
    static {
        // Initial setting.
        String x = System.getProperty("jena:graphSameTerm");
        if ( x != null && x.equalsIgnoreCase("true") )
            defaultSameTerm = true;
    }

    /**
     * Set the default mode for in-memory graphs : same term (true) or same value
     * (false).
     * <p>
     * This is initially set with system property "jena:graphSameTerm"
     * with the system default is same value (Jena4).
     * <p>
     * This affects {@link #createDefaultGraph}.
     */
    public static void setDftGraphSameTerm(boolean value) {
        defaultSameTerm = value;
    }

    /**
     * Get the default mode for in-memory for graphs : same term (true) or same value
     * (false).
     * <p>
     * This is used by {@link #createDefaultGraph}.
     */
    public static boolean dftGraphSameTerm() {
        return defaultSameTerm;
    }

    private GraphMemFactory() {}

    /**
     * Answer a memory-based graph.
     * This is the system default.
     */
    public static Graph createDefaultGraph() {
        return dftGraphSameTerm()
                ? createDefaultGraphSameTerm()
                : createDefaultGraphSameValue();
    }

    /**
     * This function will track the preferred general purpose graph.
     * It will switch from "same value" to "same term"
     */
    @SuppressWarnings("deprecation")
    public static Graph createGraphMem()
    { return new org.apache.jena.mem.GraphMem(); }

    /**
     * Answer a memory-based graph with "same value" semantics
     * used in Jena2, Jena3 and Jena4 for in-memory graphs.
     * Jena5 may change to "same term" semantics.
     * This method will continue to provide a "same value" graph.
     */
    @SuppressWarnings("deprecation")
    public static Graph createDefaultGraphSameValue()
    { return new org.apache.jena.mem.GraphMem(); }

    /**
     * Answer a memory-based graph with "same term" semantics
     * This method will continue to provide the preferred general purpose "same term" graph.
     */
    public static Graph createDefaultGraphSameTerm()
    { return createGraphMem2(); }

    /**
     * A graph that stores triples in memory. This class is not thread-safe.
     * <p>
     * <ul>
     * <li>This graph provides term equality.</li>
     * <li>Iterator over this graph does not provide Iterator.remove</li>
     * </ul>
     * <p>
     * It has improved performance compared to {@link org.apache.jena.mem.GraphMem}
     * with a simpler implementation, primarily due to not providing support for {@link Iterator#remove}.
     * <p>
     * See {@link GraphMem2Legacy} for details.
     */
    public static Graph createGraphMem2Basic()
    { return new GraphMem2Legacy(); }

    /**
     * A graph that stores triples in memory. This class is not thread-safe.
     * <p>
     * <ul>
     * <li>This graph provides term equality.</li>
     * <li>Iterator over this graph does not provide Iterator.remove</li>
     * </ul>
     * <p>
     * This graph implementation provides improved performance with a minor increase in memory usage.
     * <p>
     * See {@link GraphMem2Fast} for details.
     */
    public static Graph createGraphMem2()
    { return new GraphMem2Fast(); }

    /**
     * A graph that stores triples in memory. This class is not thread-safe.
     * <p>
     * <ul>
     * <li>This graph provides term equality.</li>
     * <li>Iterator over this graph does not provide Iterator.remove</li>
     * </ul>
     * <p>
     * {@link GraphMem2Roaring} is focused on handling large in-memory graphs.
     * It uses <a href="https://roaringbitmap.org/">Roaring bitmaps</a> for indexing.
     * <p>
     * See {@link GraphMem2Roaring} for details.
     */
    public static Graph createGraphMem2Roaring()
    { return new GraphMem2Roaring(); }

    private final static Graph emptyGraph = new GraphBase() {
        @Override
        protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
            return NullIterator.instance();
        }
    };

    /** Immutable graph with no triples */
    public static Graph empty() { return emptyGraph ; }
}
