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
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;

/**
 * A factory class for creating memory Graphs.
 * <p>
 * Apache Jena is migrating to term semantics graph for consistency across all in-memory and persistent storage graphs.
 * <p>
 * All the graphs that this factory creates are <strong>not thread-safe</strong>.
 * Note that if the memory Graph is structurally modified at any time after
 * the iterator has been created by any of the {@code find*} or {@code stream*} methods, the iterator may throw
 * a {@link java.util.ConcurrentModificationException ConcurrentModificationException}
 * if continued with it after this modification.
 * This may happen even if the queried data does not relate directly to the modified data
 * (i.e. when triple search pattern does not match added or deleted triple).
 * <p>
 * The good practice is to explicitly close any {@link ExtendedIterator} immediately after a read operation.
 * For GraphMem implementations {@code ExtendedIterator}'s materializing methods (such as {@link ExtendedIterator#toList()})
 * could be used safely without explicit close. The same is true for {@link java.util.stream.Stream Java Stream}'s
 * terminal operations.
 */
public class GraphMemFactory
{
    static { JenaSystem.init(); }

    private GraphMemFactory() {}

    /**
     * Answer a memory-based graph.
     * This is the system default.
     */
    public static Graph createDefaultGraph() {
        return createDefaultGraphSameTerm();
    }

    /**
     * This function will track the preferred general purpose graph for the Model
     * API. It is value-based, to align with the Java-object mapping, rather than
     * RDF-term based (Integers +001 and 1 are the same value).
     */
    public static Graph createGraphMemForModel() {
        @SuppressWarnings("deprecation")
        Graph g = new org.apache.jena.mem.GraphMemValue();
        return g;
    }

    /**
     * Answer a memory-based graph with "same value" semantics
     * used in Jena2, Jena3 and Jena4 for in-memory graphs.
     * Jena5 changed to "same term" semantics.
     * This method will continue to provide a "same value" graph.
     * This is used for the Model API.
     */
    public static Graph createDefaultGraphSameValue() {
        @SuppressWarnings("deprecation")
        Graph g = new org.apache.jena.mem.GraphMemValue();
        return g;
    }

    /**
     * Answer a memory-based graph with "same term" semantics
     * This method will continue to provide the preferred
     * general purpose "same term" graph.
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
     * It has improved performance compared to {@link org.apache.jena.mem.GraphMemValue}
     * with a simpler implementation, primarily due to not providing support for {@link Iterator#remove}.
     * <p>
     * See {@link GraphMem2Legacy} for details.
     */
    public static Graph createGraphMemBasic()
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
    public static Graph createGraphMem2() {
        return new GraphMem2Fast();
    }

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
    public static Graph createGraphMemRoaring()
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
