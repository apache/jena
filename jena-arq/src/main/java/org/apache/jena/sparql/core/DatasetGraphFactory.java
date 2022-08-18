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

import java.util.Iterator ;
import java.util.Objects ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sys.JenaSystem;

public class DatasetGraphFactory
{
    static { JenaSystem.init(); }

    /** Create an in-memory {@link Dataset}.
     * <p>
     * See also {@link #createTxnMem()} for a transactional dataset.
     * <p>
     * This implementation copies models when {@link Dataset#addNamedModel(String, Model)} is called.
     * <p>
     * This implementation provides "best effort" transactions; it only provides MRSW locking.
     * Use {@link #createTxnMem} for a proper in-memory transactional {@code DatasetGraph}.
     *
     * @see #createTxnMem
     */
    public static DatasetGraph create() {
        return new DatasetGraphMap() ;
    }

    /**
     * Create an in-memory, transactional {@link Dataset}.
     * <p>
     * This fully supports transactions, including abort to roll-back changes.
     * It provides "autocommit" if operations are performed
     * outside a transaction but with a performance impact
     * (the implementation adds a begin/commit around each add or delete
     * so overheads can accumulate).
     *
     * @return a transactional, in-memory, modifiable DatasetGraph
     */
    public static DatasetGraph createTxnMem() { return new DatasetGraphInMemory(); }

    /**
     * Create a general-purpose  {@link Dataset}.<br/>
     * Any graphs needed are in-memory unless explicitly added with {@link Dataset#addNamedModel}.
     * <p>
     * This dataset type can contain graphs from any source when added via {@link Dataset#addNamedModel}.
     * These are held as links to the supplied graph and not copied.
     * <p>
     * <em>This dataset does not support the graph indexing feature of jena-text.</em>
     * <p>
     * This dataset does not support serialized transactions (it only provides MRSW locking).
     *
     * @see #createTxnMem
     * @return a general-purpose Dataset
     */
    public static DatasetGraph createGeneral() {
        return new DatasetGraphMapLink(graphMakerMem.create(null), graphMakerMem) ;
    }

    /**
     * Clone the structure of a {@link DatasetGraph}.
     */
    public static DatasetGraph cloneStructure(DatasetGraph dsg) {
        Objects.requireNonNull(dsg, "DatasetGraph must be provided") ;
        DatasetGraphMapLink dsg2 = new DatasetGraphMapLink(dsg.getDefaultGraph()) ;
        for ( Iterator<Node> names = dsg.listGraphNodes() ; names.hasNext() ; ) {
            Node gn = names.next() ;
            dsg2.addGraph(gn, dsg.getGraph(gn)) ;
        }
        return dsg2 ;
    }

    /**
     * Create a DatasetGraph starting with a single graph.
     * New graphs must be explicitly added.
     */
    public static DatasetGraph create(Graph dftGraph) {
        return new DatasetGraphMapLink(dftGraph);
    }

    /**
     * Create a DatasetGraph which only ever has a single default graph.
     */
    public static DatasetGraph wrap(Graph graph) { return DatasetGraphOne.create(graph) ; }

    /**
     * An always empty {@link DatasetGraph}.
     * It has one graph (the default graph) with zero triples.
     * No changes allowed - this is not a sink.
     */
    public static DatasetGraph empty() { return DatasetGraphZero.create(); }


    /** Interface for making graphs when a dataset needs to add a new graph.
     *  Return null for no graph created.
     */
    public interface GraphMaker { public Graph create(Node name) ; }

    /** A graph maker that doesn't make graphs. */
    public static GraphMaker graphMakerNull = (name) -> null ;

    /** A graph maker that creates unnamed Jena default graphs */
    public static GraphMaker graphMakerMem = (name) -> GraphFactory.createDefaultGraph() ;

    /** A graph maker that creates {@link NamedGraph}s around a Jena default graphs */
    public static GraphMaker graphMakerNamedGraphMem = (name) -> {
        Graph g = GraphFactory.createDefaultGraph() ;
        return new NamedGraphWrapper(name, g);
    };
}
