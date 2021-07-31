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
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

/**
 * Query execution interface working at the Graph-Node-Triple level.
 *
 * @see QueryExecution
 */
public class QueryExecutionAdapter implements QueryExecution
{
    // Pure adapter. Does not support timeout or initial bindings.
    private final QueryExec qExec;
    private final Dataset dataset;

    public static QueryExecution adapt(QueryExec qExec) {
        if ( qExec instanceof QueryExecAdapter) {
            return ((QueryExecAdapter)qExec).get();
        }
        return new QueryExecutionAdapter(qExec);
    }

    protected QueryExecutionAdapter(QueryExec qExec) {
        this.qExec = qExec;
        if ( qExec == null )
            this.dataset = null;
        else
            this.dataset = (get().getDataset() != null) ? DatasetFactory.wrap(get().getDataset()) : null;
    }

    protected QueryExec get() { return qExec; }

    /** Set the initial association of variables and values.
     * May not be supported by all QueryExecution implementations.
     * @param binding
     */
    @Override
    public void setInitialBinding(Binding binding) {}

    /**
     * The dataset against which the query will execute.
     * May be null, implying it is expected that the query itself
     * has a dataset description.
     */
    @Override
    public Dataset getDataset() { return dataset; }

    /** The properties associated with a query execution -
     *  implementation specific parameters  This includes
     *  Java objects (so it is not an RDF graph).
     *  Keys should be URIs as strings.
     *  May be null (this implementation does not provide any configuration).
     */
    @Override
    public Context getContext() { return get().getContext(); }

    /** The query associated with a query execution.
     *  May be null (QueryExecution may have been created by other means)
     */
    @Override
    public Query getQuery() { return get().getQuery(); }

    /**
     *  Execute a SELECT query
     *  <p>
     *  <strong>Important:</strong> The name of this method is somewhat of a misnomer in that
     *  depending on the underlying implementation this typically does not execute the
     *  SELECT query but rather answers a wrapper over an internal data structure that can be
     *  used to answer the query.  In essence calling this method only returns a plan for
     *  executing this query which only gets evaluated when you actually start iterating
     *  over the results.
     *  </p>
     *  */
    @Override
    public ResultSet execSelect() {
        if ( getDataset() != null )
            return new ResultSetAdapter(get().select(), getDataset().getDefaultModel());
        else
            return ResultSet.adapt(get().select());
    }

    /** Execute a CONSTRUCT query */
    @Override
    public Model execConstruct() { return ModelFactory.createModelForGraph(get().construct()); }

    /** Execute a CONSTRUCT query, putting the statements into a graph.
     *  @return Graph The model argument for cascaded code.
     */
    @Override
    public Model execConstruct(Model model) {
        get().construct(model.getGraph());
        return model;
    }

    /**
     * Execute a CONSTRUCT query, returning the results as an iterator of {@link Triple}.
     * <p>
     * <b>Caution:</b> This method may return duplicate Triples.  This method may be useful if you only
     * need the results for stream processing, as it can avoid having to place the results in a Model.
     * </p>
     * <p>
     * <strong>Important:</strong> The name of this method is somewhat of a misnomer in that
     * depending on the underlying implementation this typically does not execute the
     * CONSTRUCT query but rather answers a wrapper over an internal data structure that can be
     * used to answer the query.  In essence calling this method only returns a plan for
     * executing this query which only gets evaluated when you actually start iterating
     * over the results.
     * </p>
     * @return An iterator of Triple objects (possibly containing duplicates) generated
     * by applying the CONSTRUCT template of the query to the bindings in the WHERE clause.
     */
    @Override
    public Iterator<Triple> execConstructTriples() { return get().constructTriples(); }

    /**
     * Execute a CONSTRUCT query, returning the results as an iterator of {@link Quad}.
     * <p>
     * <b>Caution:</b> This method may return duplicate Quads.  This method may be useful if you only
     * need the results for stream processing, as it can avoid having to place the results in a Model.
     * </p>
     * @return An iterator of Quad objects (possibly containing duplicates) generated
     * by applying the CONSTRUCT template of the query to the bindings in the WHERE clause.
     * </p>
     * <p>
     * See {@link #execConstructTriples} for usage and features.
     */
    @Override
    public Iterator<Quad> execConstructQuads() { return get().constructQuads(); }

    /** Execute a CONSTRUCT query, putting the statements into 'dataset'.
     *  This maybe an extended syntax query (if supported).
     */
    @Override
    public Dataset execConstructDataset() {
        return DatasetFactory.wrap(get().constructDataset());
    }

    /** Execute a CONSTRUCT query, putting the statements into 'dataset'.
     *  This may be an extended syntax query (if supported).
     */
    @Override
    public Dataset execConstructDataset(Dataset dataset) {
        get().constructDataset(dataset.asDatasetGraph());
        return dataset;
    }

    /** Execute a DESCRIBE query */
    @Override
    public Model execDescribe() { return ModelFactory.createModelForGraph(get().describe()); }

    /** Execute a DESCRIBE query, putting the statements into a graph.
     *  @return Graph The model argument for cascaded code.
     */
    @Override
    public Model execDescribe(Model model) {
        get().describe(model.getGraph());
        return model;
    }

    /**
     * Execute a DESCRIBE query, returning the results as an iterator of {@link Triple}.
     * <p>
     * <b>Caution:</b> This method may return duplicate Triples.  This method may be useful if you only
     * need the results for stream processing, as it can avoid having to place the results in a Model.
     * </p>
     * <p>
     * <strong>Important:</strong> The name of this method is somewhat of a misnomer in that
     * depending on the underlying implementation this typically does not execute the
     * DESCRIBE query but rather answers a wrapper over an internal data structure that can be
     * used to answer the query.  In essence calling this method only returns a plan for
     * executing this query which only gets evaluated when you actually start iterating
     * over the results.
     * </p>
     * @return An iterator of Triple objects (possibly containing duplicates) generated as the output of the DESCRIBE query.
     */
    @Override
    public Iterator<Triple> execDescribeTriples() { return get().describeTriples(); }

    /** Execute an ASK query */
    @Override
    public boolean execAsk() { return get().ask(); }

    /** Execute a JSON query and return a json array */
    @Override
    public JsonArray execJson() { return get().execJson(); }

    /** Execute a JSON query and return an iterator */
    @Override
    public Iterator<JsonObject> execJsonItems() { return get().execJsonItems(); }

    /** Stop in mid execution.
     *  This method can be called in parallel with other methods on the
     *  QueryExecution object.
     *  There is no guarantee that the concrete implementation actual
     *  will stop or that it will do so immediately.
     *  No operations on the query execution or any associated
     *  result set are permitted after this call and may cause exceptions to be thrown.
     */

    @Override
    public void abort() { get().abort(); }

    /** Close the query execution and stop query evaluation as soon as convenient.
     *  QueryExecution objects, and a {@link ResultSet} from {@link #execSelect},
     *  can not be used once the QueryExecution is closed.
     *  Model results from {@link #execConstruct} and {@link #execDescribe}
     *  are still valid.
     *  It is important to close query execution objects in order to release
     *  resources such as working memory and to stop the query execution.
     *  Some storage subsystems require explicit ends of operations and this
     *  operation will cause those to be called where necessary.
     *  No operations on the query execution or any associated
     *  result set are permitted after this call.
     */
    @Override
    public void close() { get().close(); }

    /**
     * Answer whether this QueryExecution object has been closed or not.
     * @return boolean
     */
    @Override
    public boolean isClosed() { return get().isClosed(); }

    /** Set a timeout on the query execution.
     * Processing will be aborted after the timeout (which starts when the appropriate exec call is made).
     * Not all query execution systems support timeouts.
     * A timeout of less than zero means no timeout.
     */
    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        throw new UnsupportedOperationException();
    }

    /** Set time, in milliseconds
     * @see #setTimeout(long, TimeUnit)
     */
    @Override
    public void setTimeout(long timeout) {
        throw new UnsupportedOperationException();
    }

    /** Set timeouts on the query execution; the first timeout refers to time to first result,
     * the second refers to overall query execution after the first result.
     * Processing will be aborted if a timeout expires.
     * Not all query execution systems support timeouts.
     * A timeout of less than zero means no timeout; this can be used for timeout1 or timeout2.
     */

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        throw new UnsupportedOperationException();
    }

    /** Set time, in milliseconds
     *  @see #setTimeout(long, TimeUnit, long, TimeUnit)
     */
    @Override
    public void setTimeout(long timeout1, long timeout2) {
        throw new UnsupportedOperationException();
    }

    /** Return the first timeout (time to first result), in milliseconds: negative if unset */
    @Override
    public long getTimeout1() { return -1L; }
    /** Return the second timeout (overall query execution after first result), in milliseconds: negative if unset */
    @Override
    public long getTimeout2() { return -1L; }

    @Override
    public void setInitialBinding(QuerySolution binding) {
        throw new UnsupportedOperationException();
    }
}
