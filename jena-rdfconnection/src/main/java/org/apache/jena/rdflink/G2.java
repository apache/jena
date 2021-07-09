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

package org.apache.jena.rdflink;

import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

/** Graph operations */
/*package*/ class G2 {
    /**
     * Add src to dst - assumes transaction.
     * src and dst must not overlap.
     * Copies "left to right" -- {@code src into dst}
     * @param src
     * @param dst
     */
    public static void copyGraphSrcToDst(Graph src, Graph dst) {
        apply(src, dst::add);
    }

    /**
     * Clear graph.
     */
    public static void clear(Graph graph) {
        graph.clear();
    }

    /**
     * Apply an action to every triple of a graph. The action must not attempt to
     * modify the graph but it can read it.
     *
     * @param src
     * @param action
     */
    public static void apply(Graph src, Consumer<Triple> action) {
        ExtendedIterator<Triple> iter = src.find();
        apply(iter, action);
    }

    /**
     * Apply an action to every triple of an iterator.
     * If the iterator is attracted to a graph, the action must not attempt to
     * modify the graph but it can read it.
     *
     * @param iter
     * @param action
     */    public static void apply(ExtendedIterator<Triple> iter, Consumer<Triple> action) {
        try {
            while(iter.hasNext()) {
                Triple t = iter.next();
                action.accept(t);
            }
        } finally { iter.close(); }
    }
}

