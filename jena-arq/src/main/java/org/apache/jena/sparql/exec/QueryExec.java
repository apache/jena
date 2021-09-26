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

package org.apache.jena.sparql.exec;

import java.util.Iterator;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.http.QueryExecHTTPBuilder;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;

/**
 * Query execution interface working at the Graph-Node-Triple level.
 *
 * @see QueryExecution
 */
public interface QueryExec extends AutoCloseable {

    /**
     * Create a {@link QueryExecBuilder} for a dataset.
     * For local dataset specific configuration, use {@link #newBuilder}().dataset(dataset)
     * to get a {@link QueryExecDatasetBuilder}.
     */
    public static QueryExecBuilder dataset(DatasetGraph dataset) {
        return QueryExecDatasetBuilder.create().dataset(dataset);
    }

    /** Create a {@link QueryExecBuilder} for a graph. */
    public static QueryExecBuilder dataset(Graph graph) {
        return QueryExecDatasetBuilder.create().graph(graph);
    }

    /** Create a {@link QueryExecBuilder} for a remote endpoint. */
    public static QueryExecBuilder endpoint(String serviceURL) {
        return QueryExecHTTPBuilder.create().endpoint(serviceURL);
    }

    /** Create an uninitialized {@link QueryExecDatasetBuilder}. */
    public static QueryExecDatasetBuilder newBuilder() {
        return QueryExecDatasetBuilder.create();
    }

    /**
     * The dataset against which the query will execute. May be null - the dataset
     * may be remote or the query itself has a dataset description.
     */
    public DatasetGraph getDataset();

    /**
     * The properties associated with a query execution - implementation specific
     * parameters This includes Java objects (so it is not an RDF graph). Keys should
     * be URIs as strings. May be null (this implementation does not provide any
     * configuration).
     */
    public Context getContext();

    /**
     * The query associated with a query execution. May be null (QueryExec may
     * have been created by other means)
     */
    public Query getQuery();

    /**
     * The query as a string.
     * This may be null (QueryExec may have been created by other means).
     * This may contain non-Jena extensions and can not be parsed by Jena.
     * If {@code getQuery()} is not null, this is a corresponding string that parses to the same query.
     */
    public String getQueryString();

    /**
     * Execute a SELECT query
     * <p>
     * <strong>Important:</strong> The name of this method is somewhat of a misnomer
     * in that depending on the underlying implementation this typically does not
     * execute the SELECT query but rather answers a wrapper over an internal data
     * structure that can be used to answer the query. In essence calling this method
     * only returns a plan for executing this query which only gets evaluated when
     * you actually start iterating over the results.
     * </p>
     */
    public RowSet select();

    /** Execute a CONSTRUCT query */
    public default Graph construct() {
        Graph graph = GraphFactory.createDefaultGraph();
        return construct(graph);
    }

    /**
     * Execute a CONSTRUCT query, putting the statements into a graph.
     *
     * @return Graph The graph argument for cascaded code.
     */
    public Graph construct(Graph graph);

    /**
     * Execute a CONSTRUCT query, returning the results as an iterator of
     * {@link Triple}.
     * <p>
     * <b>Caution:</b> This method may return duplicate Triples. This method may be
     * useful if you only need the results for stream processing, as it can avoid
     * having to place the results in a Model.
     * </p>
     * <p>
     * <strong>Important:</strong> The name of this method is somewhat of a misnomer
     * in that depending on the underlying implementation this typically does not
     * execute the CONSTRUCT query but rather answers a wrapper over an internal data
     * structure that can be used to answer the query. In essence calling this method
     * only returns a plan for executing this query which only gets evaluated when
     * you actually start iterating over the results.
     * </p>
     *
     * @return An iterator of Triple objects (possibly containing duplicates)
     *     generated by applying the CONSTRUCT template of the query to the bindings
     *     in the WHERE clause.
     */
    public Iterator<Triple> constructTriples();

    /**
     * Execute a CONSTRUCT query, returning the results as an iterator of
     * {@link Quad}.
     * <p>
     * <b>Caution:</b> This method may return duplicate Quads. This method may be
     * useful if you only need the results for stream processing, as it can avoid
     * having to place the results in a Model.
     * </p>
     *
     * @return An iterator of Quad objects (possibly containing duplicates) generated
     *     by applying the CONSTRUCT template of the query to the bindings in the
     *     WHERE clause.
     *     </p>
     *     <p>
     *     See {@link #constructTriples} for usage and features.
     */
    public Iterator<Quad> constructQuads();

    /**
     * Execute a CONSTRUCT query, putting the statements into 'dataset'. This maybe
     * an extended syntax query (if supported).
     */
    public default DatasetGraph constructDataset() {
        return constructDataset(DatasetGraphFactory.create());
    }

    /**
     * Execute a CONSTRUCT query, putting the statements into 'dataset'. This may be
     * an extended syntax query (if supported).
     */
    public DatasetGraph constructDataset(DatasetGraph dataset);

    /** Execute a DESCRIBE query */
    public default Graph describe() {
        Graph graph = GraphFactory.createDefaultGraph();
        return describe(graph);
    }

    /**
     * Execute a DESCRIBE query, putting the statements into a graph.
     *
     * @return Graph The model argument for cascaded code.
     */
    public Graph describe(Graph graph);

    /**
     * Execute a DESCRIBE query, returning the results as an iterator of
     * {@link Triple}.
     * <p>
     * <b>Caution:</b> This method may return duplicate Triples. This method may be
     * useful if you only need the results for stream processing, as it can avoid
     * having to place the results in a Model.
     * </p>
     * <p>
     * <strong>Important:</strong> The name of this method is somewhat of a misnomer
     * in that depending on the underlying implementation this typically does not
     * execute the DESCRIBE query but rather answers a wrapper over an internal data
     * structure that can be used to answer the query. In essence calling this method
     * only returns a plan for executing this query which only gets evaluated when
     * you actually start iterating over the results.
     * </p>
     *
     * @return An iterator of Triple objects (possibly containing duplicates)
     *     generated as the output of the DESCRIBE query.
     */
    public Iterator<Triple> describeTriples();

    /** Execute an ASK query */
    public boolean ask();

    /** Execute a JSON query and return a json array */
    public JsonArray execJson();

    /** Execute a JSON query and return an iterator */
    public Iterator<JsonObject> execJsonItems();

    /**
     * Stop in mid execution. This method can be called in parallel with other
     * methods on the QueryExecution object. There is no guarantee that the concrete
     * implementation actual will stop or that it will do so immediately. No
     * operations on the query execution or any associated result set are permitted
     * after this call and may cause exceptions to be thrown.
     */
    public void abort();

    /**
     * Close the query execution and stop query evaluation as soon as convenient.
     * QExec objects, and a {@link RowSet} from {@link #select}, can not be used once
     * the QExec is closed. Model results from {@link #construct} and
     * {@link #describe} are still valid.
     * <p>
     * It is important to close query execution objects in order to release resources
     * such as working memory and to stop the query execution. Some storage
     * subsystems require explicit ends of operations and this operation will cause
     * those to be called where necessary. No operations on the query execution or
     * any associated result set are permitted after this call.
     */
    @Override
    public void close();

    /**
     * Answer whether this QueryExecution object has been closed or not.
     *
     * @return boolean
     */
    public boolean isClosed();

    static QueryExec adapt(QueryExecution qExec) {
        return QueryExecAdapter.adapt(qExec);
    }
}
