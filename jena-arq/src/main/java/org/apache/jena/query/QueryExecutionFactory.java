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
import static java.util.Objects.requireNonNull;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.exec.QueryExecDataset;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.exec.QueryExecutionCompat;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.sparql.syntax.Element;

/**
 * {@code QueryExecutionFactory} provides some convenience operations for making
 * {@link QueryExecution} objects. It is not comprehensive and only covers common,
 * simple cases.
 * <p>
 * For more control of building a local or remote {@link QueryExecution} object see
 * the builder patterns such as:
 * <ul>
 * <li>{@code QueryExecution.create(). ... .build()} for querying local data.</li>
 * <li>{@code QueryExecution.service(url). ... .build()} for querying a remote store using HTTP.</li>
 * </ul>
 * <p>
 * See also {@code RDFConnection} for working with SPARQL Query, SPARQL Update and SPARQL Graph Store Protocol together.
 *
 * @see QueryExecutionDatasetBuilder
 * @see QueryExecutionHTTPBuilder
 */

public class QueryExecutionFactory
{
    protected QueryExecutionFactory() {}

    // ---------------- Query

    /**
     * Create a QueryExecution
     *
     * @param query Query
     * @return QueryExecution
     */
    public static QueryExecution create(Query query) {
        checkArg(query);
        return makeExecution(query);
    }

    /**
     * Create a QueryExecution
     *
     * @param queryStr Query string
     * @return QueryExecution
     */
    public static QueryExecution create(String queryStr) {
        checkArg(queryStr);
        return create(makeQuery(queryStr));
    }

    /**
     * Create a QueryExecution
     *
     * @param queryStr Query string
     * @param syntax Query syntax
     * @return QueryExecution
     */
    public static QueryExecution create(String queryStr, Syntax syntax) {
        checkArg(queryStr);
        return create(makeQuery(queryStr, syntax));
    }

    // ---------------- Query + Dataset

    /**
     * Create a QueryExecution to execute over the Dataset.
     *
     * @param query Query
     * @param dataset Target of the query
     * @return QueryExecution
     */
    public static QueryExecution create(Query query, Dataset dataset) {
        return make(query, dataset, null);
    }

    /**
     * Create a QueryExecution to execute over the {@link DatasetGraph}.
     *
     * @param query Query
     * @param datasetGraph Target of the query
     * @return QueryExecution
     */
    public static QueryExecution create(Query query, DatasetGraph datasetGraph) {
        requireNonNull(query, "Query is null");
        requireNonNull(datasetGraph, "DatasetGraph is null");
        return make(query, null, datasetGraph);
    }

    /** Create a QueryExecution to execute over the Dataset.
     *
     * @param queryStr     Query string
     * @param dataset      Target of the query
     * @return QueryExecution
     */
    public static QueryExecution create(String queryStr, Dataset dataset) {
        checkArg(queryStr);
        return make(makeQuery(queryStr), dataset, null);
    }

    /** Create a QueryExecution to execute over the Dataset.
     *
     * @param queryStr     Query string
     * @param syntax       Query language
     * @param dataset      Target of the query
     * @return QueryExecution
     */
    public static QueryExecution create(String queryStr, Syntax syntax, Dataset dataset) {
        checkArg(queryStr);
        return make(makeQuery(queryStr, syntax), dataset, null);
    }

    // ---------------- Query + Model

    /** Create a QueryExecution to execute over the Model.
     *
     * @param query     Query
     * @param model     Target of the query
     * @return QueryExecution
     */
    public static QueryExecution create(Query query, Model model) {
        checkArg(query);
        checkArg(model);
        return makeExecution(query, model);
    }

    /** Create a QueryExecution to execute over the Model.
     *
     * @param queryStr     Query string
     * @param model        Target of the query
     * @return QueryExecution
     */
    public static QueryExecution create(String queryStr, Model model) {
        checkArg(queryStr);
        checkArg(model);
        return create(makeQuery(queryStr), model);
    }

    /** Create a QueryExecution to execute over the Model.
     *
     * @param queryStr     Query string
     * @param lang         Query language
     * @param model        Target of the query
     * @return QueryExecution
     */
    public static QueryExecution create(String queryStr, Syntax lang, Model model) {
        checkArg(queryStr);
        checkArg(model);
        return create(makeQuery(queryStr, lang), model);
    }

    // ---------------- Internal routines

    private static Query toQuery(Element pattern) {
        Query query = QueryFactory.make();
        query.setQueryPattern(pattern);
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        return query;
    }

    static private Query makeQuery(String queryStr) {
        return QueryFactory.create(queryStr);
    }

    static private Query makeQuery(String queryStr, Syntax syntax) {
        return QueryFactory.create(queryStr, syntax);
    }

    static protected QueryExecution makeExecution(Query query) {
        return QueryExecution.create().query(query).build();
    }

    protected static QueryExecution makeExecution(Query query, Model model) {
        Graph graph = model.getGraph();
        DatasetGraph dataset = DatasetGraphFactory.wrap(graph);
        Graph g = unwrap(graph);
        if ( g instanceof GraphView ) {
            GraphView gv = (GraphView)g;
            // Copy context of the storage dataset to the wrapper dataset.
            dataset.getContext().putAll(gv.getDataset().getContext());
        }
        return make(query, null, dataset);
    }

    private static Graph unwrap(Graph graph) {
        for(;;) {
            if ( graph instanceof GraphWrapper )
                graph = ((GraphWrapper)graph).get();
            else if ( graph instanceof WrappedGraph )
                graph = ((WrappedGraph)graph).getWrapped();
            else return graph;
        }
    }

    // Preferred base of all QueryExecution creation in QueryExecutionFactory.
    // dataset and datasetGraph can't both be set.
    // Null for both of them is allowed and assumes the query has a dataset description.
    private static QueryExecution make(Query query, Dataset dataset, DatasetGraph datasetGraph) {
        QueryExecDatasetBuilder builder = QueryExecDataset.newBuilder().query(query);
        if ( dataset == null && datasetGraph == null )
            return QueryExecutionCompat.compatibility(builder, null, query, builder.getQueryString());
        if ( dataset == null ) {
            builder.dataset(datasetGraph);
            dataset = DatasetFactory.wrap(datasetGraph);
       } else {
            builder.dataset(dataset.asDatasetGraph());
        }
        return QueryExecutionCompat.compatibility(builder, dataset, query, builder.getQueryString());
    }

    static private void checkArg(Model model)
    { requireNonNull(model, "Model is a null pointer"); }

    static private void checkArg(String queryStr)
    { requireNonNull(queryStr, "Query string is null"); }

    static private void checkArg(Query query)
    { requireNonNull(query, "Query is null"); }
}
