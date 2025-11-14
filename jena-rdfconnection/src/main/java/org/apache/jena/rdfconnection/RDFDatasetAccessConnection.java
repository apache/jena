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

package org.apache.jena.rdfconnection;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Transactional;

/**
 * SPARQL Graph Store Protocol (read operations) and whole dataset access.
 * {@link RDFDatasetConnection} adds the write operations.
 *
 * @see RDFDatasetConnection
 * @see RDFConnection
 */
public interface RDFDatasetAccessConnection extends Transactional, AutoCloseable
{
    /** Fetch a named graph.
     * This is SPARQL Graph Store Protocol HTTP GET or equivalent.
     *
     * @param graphName URI string for the graph name (null or "default" for the default graph)
     * @return Model
     */
    public Model fetch(String graphName);

    /** Fetch the default graph.
     * This is SPARQL Graph Store Protocol HTTP GET or equivalent.
     * @return Model
     */
    public Model fetch();

    /** Fetch the contents of the dataset */
    public Dataset fetchDataset();

    /** Test whether this connection is closed or not */
    public boolean isClosed();

    /** Close this connection.  Use with try-resource. */
    @Override public void close();
}
