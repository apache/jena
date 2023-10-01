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

import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.GraphPlain;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sys.JenaSystem;

/** Ways to make graphs and models */
public class GraphFactory {
    static {
        JenaSystem.init();
    }

    private static boolean defaultSameTerm = true;
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
     * Create a graph that is a Jena memory graph
     *
     * @see #createDefaultGraph
     */
    public static Graph createGraphMem() {
        return GraphMemFactory.createGraphMem();
    }

    /**
     * Create an in-memory, transactional graph.
     * <p>
     * This fully supports transactions, including abort to roll-back changes. It
     * provides "autocommit" if operations are performed outside a transaction. The
     * implementation adds a begin/commit around each add or delete so overheads can
     * accumulate).
     */
    public static GraphTxn createTxnGraph() {
        return new GraphTxn();
    }

    /** Create a graph - ARQ-wide default type */
    public static Graph createDefaultGraph() {
        // Normal usage is SystemARQ.UsePlainGraph = false and use
        // createJenaDefaultGraph
        return SystemARQ.UsePlainGraph ? createPlainGraph() : createJenaDefaultGraph();
    }

    /** Create a graph - always the Jena default graph type */
    public static Graph createJenaDefaultGraph() {
        return GraphMemFactory.createDefaultGraph();
    }

    /** Graph that uses same-term for find() and contains(). */
    public static Graph createPlainGraph() {
        return GraphPlain.plain();
    }

    public static Graph sinkGraph() {
        return new GraphSink();
    }

    /** Guaranteed call-through to Jena's ModelFactory operation */
    public static Model makeJenaDefaultModel() {
        return ModelFactory.createDefaultModel();
    }

    /** Create a model over a default graph (ARQ-wide for default graph type) */
    public static Model makeDefaultModel() {
        return ModelFactory.createModelForGraph(createDefaultGraph());
    }

    /** Create a model over a plain graph (small-scale use only) */
    public static Model makePlainModel() {
        return ModelFactory.createModelForGraph(createPlainGraph());
    }
}
