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

package org.apache.jena.ontology.models;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.WrappedGraph;

/**
 * GraphMemRefCount - a base class for GraphMemValue
 * <p>
 * GraphMemRefCount maintains a reference count, set to one when it is created, and
 * incremented by the method <code>openAgain()</code>. When the graph is closed, the
 * count is decremented, and when it reaches 0, the tables are trashed and
 * GraphBase.close() called. Thus in normal use one close is enough, but GraphMakers
 * using GraphMems can arrange to re-use the same named graph.
 */
public class GraphRefCount extends WrappedGraph {
    /**
     * The number-of-times-opened count.
     */
    protected int count;

    /**
     * initialise a GraphMemBase with its count set to 1.
     */
    protected GraphRefCount(Graph graph) {
        super(graph);
        count = 1;
    }

    /**
     * Note a re-opening of this graph by incrementing the count. Answer this Graph.
     */
    public GraphRefCount openAgain() {
        count++;
        return this;
    }

    /**
     * Sub-classes over-ride this method to release any resources they no longer need
     * once fully closed.
     */
    public void destroy() {}

    /**
     * Close this graph; if it is now fully closed, destroy its resources and run the
     * GraphBase close.
     */
    @Override
    public void close() {
        if ( --count == 0 ) {
            destroy();
            super.close();
        }
    }
}