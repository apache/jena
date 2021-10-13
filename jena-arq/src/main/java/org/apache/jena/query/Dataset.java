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

package org.apache.jena.query;

import java.util.Iterator ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.Lock ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.util.Context ;

/** Query is over a Dataset, a collection of named graphs
 *  and a background graph (also called the default
 *  graph or unnamed graph). */

public interface Dataset extends Transactional
{
    /** Get the default graph as a Jena Model */
    public Model getDefaultModel() ;

    /** Get the graph which is the union of all named graphs as a Jena Model */
    public Model getUnionModel() ;

    /**
     * Set the default graph. Can be set to null for none.
     *
     * @param model the default graph to set
     * @return this {@code Dataset} for continued usage
     */
    public Dataset setDefaultModel(Model model);

    /** Get a graph by name as a Jena Model */
    public Model getNamedModel(String uri) ;

    /** Get a graph by resource as a Jena Model */
    public Model getNamedModel(Resource uri) ;

    /** Does the dataset contain a model with the name supplied? */
    public boolean containsNamedModel(String uri) ;

    /** Does the dataset contain a model named by the resource supplied? */
    public boolean containsNamedModel(Resource uri) ;

    /**
     * Add a named graph.
     *
     * @param uri the name of the graph to set
     * @param model the graph to set
     * @return this {@code Dataset} for continued usage
     */
    public Dataset addNamedModel(String uri, Model model);

    /**
     * Add a named graph.
     *
     * @param resource the name of the graph to set
     * @param model the graph to set
     * @return this {@code Dataset} for continued usage
     */
    public Dataset addNamedModel(Resource resource, Model model);

    /**
     * Remove a named graph.
     *
     * @param uri the name of the graph to remove
     * @return this {@code Dataset} for continued usage
     */
    public Dataset removeNamedModel(String uri);

    /**
     * Remove a named graph.
     *
     * @param resource the name of the graph to remove
     * @return this {@code Dataset} for continued usage
     */
    public Dataset removeNamedModel(Resource resource);

    /**
     * Change a named graph for another using the same name
     *
     * @param uri the name of the graph to replace
     * @param model the graph with which to replace it
     * @return this {@code Dataset} for continued usage
     */
    public Dataset replaceNamedModel(String uri, Model model);

    /**
     * Change a named graph for another using the same name
     *
     * @param resource the name of the graph to replace
     * @param model the graph with which to replace it
     * @return this {@code Dataset} for continued usage
     */
    public Dataset replaceNamedModel(Resource resource, Model model);

    /** List the names */
    public Iterator<String> listNames() ;

    /** List the names */
    public Iterator<Resource> listModelNames() ;

    /** Get the lock for this dataset */
    public Lock getLock() ;

    /** Get the context associated with this dataset */
    public Context getContext() ;

    /**
     * Does this dataset support transactions? Supporting transactions means that
     * the dataset implementation provides {@link #begin}, {@link #commit},
     * {@link #end} which otherwise may throw
     * {@link UnsupportedOperationException}.
     * <p>
     * See {@link #supportsTransactionAbort()} for {@link #abort}.
     * A {@code Dataset} that provides functionality across independent systems
     * can not provide all features strong guarantees. For example, they may use MRSW
     * locking and some isolation control. Specifically, they do not necessarily
     * provide {@link #abort}.
     * <p>
     * In addition, check details of a specific implementation.
     */
    public boolean supportsTransactions() ;

    /** Declare whether {@link #abort} is supported.
     *  This goes along with clearing up after exceptions inside application transaction code.
     */
    public boolean supportsTransactionAbort() ;

    /** Start either a READ or WRITE transaction */
    @Override
    public void begin(ReadWrite readWrite) ;

    /** Commit a transaction - finish the transaction and make any changes permanent (if a "write" transaction) */
    @Override
    public void commit() ;

    /** Abort a transaction - finish the transaction and undo any changes (if a "write" transaction) */
    @Override
    public void abort() ;

    /** Say whether a transaction is active */
    @Override
    public boolean isInTransaction() ;

    /** Finish the transaction - if a write transaction and commit() has not been called, then abort */
    @Override
    public void end() ;

    /** Get the dataset in graph form */
    public DatasetGraph asDatasetGraph() ;

    /** Get the {@link PrefixMapping} this dataset.
     * <p>
     * This is an optional operation.
     */
    public default PrefixMapping getPrefixMapping() {
        DatasetGraph dsg = asDatasetGraph();
        if ( dsg == null )
            throw new UnsupportedOperationException("Dataset.getPrefixMapping");
        return Prefixes.adapt(dsg.prefixes());
    }

    /** Close the dataset, potentially releasing any associated resources.
     *  The dataset can not be used for query after this call.
     */
    public void close() ;

    /**
     * @return Whether this {@code Dataset} is empty of triples, whether in the default graph or in any named graph.
     */
    boolean isEmpty();
}
