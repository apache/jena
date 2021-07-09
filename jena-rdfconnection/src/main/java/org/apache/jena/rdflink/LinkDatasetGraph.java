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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;

/**
 * SPARQL Graph Store Protocol and whole dataset access.
 * This adds the write operations. The read operations are defined by {@link LinkDatasetGraphAccess}.
 *
 * @see RDFLink
 */
public interface LinkDatasetGraph extends LinkDatasetGraphAccess, Transactional, AutoCloseable
{
    /** Load (add, append) RDF into a named graph in a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param graphName Graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @param file File of the data.
     */
    public void load(Node graphName, String file);

    /** Load (add, append) RDF into the default graph of a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param file File of the data.
     */
    public void load(String file);

    /** Load (add, append) RDF into a named graph in a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param graphName Graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @param graph Data.
     */
    public void load(Node graphName, Graph graph);

    /** Load (add, append) RDF into the default graph of a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param graph Data.
     */
    public void load(Graph graph);

    /** Set the contents of a named graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param graphName Graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @param file File of the data.
     */
    public void put(Node graphName, String file);

    /** Set the contents of the default graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param file File of the data.
     */
    public void put(String file);

    /** Set the contents of a named graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param graphName Graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @param graph Data.
     */
    public void put(Node graphName, Graph graph);

    /** Set the contents of the default graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param graph Data.
     */
    public void put(Graph graph);

    /**
     * Delete a graph from the dataset.
     * Null or {@link Quad#defaultGraphIRI} means the default graph, which is cleared, not removed.
     *
     * @param graphName
     */
    public void delete(Node graphName);

    /**
     * Remove all data from the default graph.
     */
    public void delete();

    /* Load (add, append) RDF triple or quad data into a dataset. Triples wil go into the default graph.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP POST equivalent to the dataset.
     */
    public void loadDataset(String file);

    /* Load (add, append) RDF triple or quad data into a dataset. Triples wil go into the default graph.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP POST equivalent to the dataset.
     */
    public void loadDataset(DatasetGraph dataset);

    /* Set RDF triple or quad data as the dataset contents.
     * Triples will go into the default graph, quads in named graphs.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP PUT equivalent to the dataset.
     */
    public void putDataset(String file);

    /* Set RDF triple or quad data as the dataset contents.
     * Triples will go into the default graph, quads in named graphs.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP PUT equivalent to the dataset.
     */
    public void putDataset(DatasetGraph dataset);

    //    /** Clear the dataset - remove all named graphs, clear the default graph. */
//    public void clearDataset();

    /** Test whether this connection is closed or not */
    @Override
    public boolean isClosed();

    /** Close this connection.  Use with try-resource. */
    @Override
    public void close();
}

