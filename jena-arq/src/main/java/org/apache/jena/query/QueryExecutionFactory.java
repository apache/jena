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

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecutionCompat;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.Context;

/**
 * {@code QueryExecutionFactory} provides some convenience operations for making {@link QueryExecution} objects.
 * It is not comprehensive and only covers common, simple cases.
 * <p>
 * For more control of building a local or remote {@link QueryExecution} object see the builder pattern:
 * <ul>
 * <li>{@code QueryExecution.create(). ... .build()} for querying local data.</li>
 * <li>{@code QueryExecutionHTTP.service(url). ... .build()} for querying a remote store using HTTP.</li>
 * </ul>
 * <p>
 * See also {@code RDFConnection} for working with SPARQL Query, SPARQL Update and SPARQL Graph Store Protocol together.
 *
 * @see QueryExecutionBuilder
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
    static public QueryExecution create(Query query) {
        checkArg(query);
        return make(query);
    }

    /**
     * Create a QueryExecution
     *
     * @param queryStr Query string
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr) {
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
    static public QueryExecution create(String queryStr, Syntax syntax) {
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
    static public QueryExecution create(Query query, Dataset dataset) {
        // checkArg(dataset); // Allow null
        return make(query, dataset, null);
    }

    /**
     * Create a QueryExecution to execute over the {@link DatasetGraph}.
     *
     * @param query Query
     * @param datasetGraph Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, DatasetGraph datasetGraph) {
        requireNonNull(query, "Query is null");
        requireNonNull(datasetGraph, "DatasetGraph is null");
        return make(query, datasetGraph, null);
    }

    /** Create a QueryExecution to execute over the Dataset.
     *
     * @param queryStr     Query string
     * @param dataset      Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Dataset dataset) {
        checkArg(queryStr);
        // checkArg(dataset); // Allow null
        return make(makeQuery(queryStr), dataset, null);
    }

    /** Create a QueryExecution to execute over the Dataset.
     *
     * @param queryStr     Query string
     * @param syntax       Query language
     * @param dataset      Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Syntax syntax, Dataset dataset) {
        checkArg(queryStr);
        // checkArg(dataset); // Allow null
        return make(makeQuery(queryStr, syntax), dataset, null);
    }

    // ---------------- Query + Model

    /** Create a QueryExecution to execute over the Model.
     *
     * @param query     Query
     * @param model     Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, Model model) {
        checkArg(query);
        checkArg(model);
        return make(query, model);
    }

    /** Create a QueryExecution to execute over the Model.
     *
     * @param queryStr     Query string
     * @param model     Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Model model) {
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
    static public QueryExecution create(String queryStr, Syntax lang, Model model) {
        checkArg(queryStr);
        checkArg(model);
        return create(makeQuery(queryStr, lang), model);
    }

    /** Create a QueryExecution to execute over the Model.
     *
     * @param query         Query string
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     * @deprecate Use {QueryExecution.create()....build()}.
     */
    @Deprecated
    static public QueryExecution create(Query query, QuerySolution initialBinding) {
        checkArg(query);
        QueryExecution qe = make(query, null, initialBinding);
        return qe;
    }

    /** Create a QueryExecution given some initial values of variables.
     *
     * @param queryStr          QueryString
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     * @deprecate Use {QueryExecution.create()....build()}.
     */
    @Deprecated
    static public QueryExecution create(String queryStr, QuerySolution initialBinding) {
        checkArg(queryStr);
        return create(makeQuery(queryStr), initialBinding);
    }

    /** Create a QueryExecution given some initial values of variables.
     *
     * @param queryStr          QueryString
     * @param syntax            Query language syntax
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Syntax syntax, QuerySolution initialBinding) {
        checkArg(queryStr);
        return create(makeQuery(queryStr, syntax), initialBinding);
    }

    /** Create a QueryExecution to execute over the Model,
     * given some initial values of variables.
     *
     * @param query            Query
     * @param model            Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     * @deprecate Use {QueryExecution.create()....build()}.
     */
    @Deprecated
    static public QueryExecution create(Query query, Model model, QuerySolution initialBinding) {
        checkArg(model);
        return create(query, DatasetFactory.wrap(model), initialBinding);
    }

    /** Create a QueryExecution to execute over the Model,
     * given some initial values of variables.
     *
     * @param queryStr         Query string
     * @param model            Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Model model, QuerySolution initialBinding) {
        checkArg(queryStr);
        checkArg(model);
        return create(makeQuery(queryStr), model, initialBinding);
    }

    /** Create a QueryExecution to execute over the Model,
     * given some initial values of variables.
     *
     * @param queryStr         Query string
     * @param syntax           Query language
     * @param model            Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     * @deprecate Use {QueryExecution.create()....build()}.
     */
    @Deprecated
    static public QueryExecution create(String queryStr, Syntax syntax, Model model, QuerySolution initialBinding) {
        checkArg(queryStr);
        return create(makeQuery(queryStr, syntax), model, initialBinding);
    }

    /** Create a QueryExecution over a Dataset given some initial values of variables.
     *
     * @param query            Query
     * @param dataset          Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, Dataset dataset, QuerySolution initialBinding) {
        checkArg(query);
        return make(query, dataset, initialBinding);
    }

    /** Create a QueryExecution over a Dataset given some initial values of variables.
     *
     * @param queryStr         Query string
     * @param dataset          Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     * @deprecate Use {QueryExecution.create()....build()}.
     */
    @Deprecated
    static public QueryExecution create(String queryStr, Dataset dataset, QuerySolution initialBinding) {
        checkArg(queryStr);
        return create(makeQuery(queryStr), dataset, initialBinding);
    }

    /** Create a QueryExecution over a Dataset given some initial values of variables.
     *
     * @param queryStr         Query string
     * @param dataset          Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     * @deprecate Use {QueryExecution.create()....build()}.
     */
    @Deprecated
    static public QueryExecution create(String queryStr, Syntax syntax, Dataset dataset, QuerySolution initialBinding) {
        QueryExecution.create();
        checkArg(queryStr);
        return create(makeQuery(queryStr, syntax), dataset, initialBinding);
    }

    // ---------------- Remote query execution

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service
     * @param query     Query string to execute
     * @return QueryExecution
     */
    static public QueryExecutionHTTP sparqlService(String service, Query query) {
        return sparqlService(service, query, null, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service
     * @param query     Query string to execute
     * @return QueryExecution
     */
    static public QueryExecutionHTTP sparqlService(String service, String query) {
        return sparqlService(service, query, null, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service
     * @param query         Query string to execute
     * @param defaultGraph  URI of the default graph
     * @return QueryExecution
     * @deprecate Use {QueryExecutionHTTP.create()....build()}.
     */
    @Deprecated
    static public QueryExecution sparqlService(String service, String query, String defaultGraph) {
        return sparqlService(service, query, List.of(defaultGraph), null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service
     * @param query             Query string to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @return QueryExecution
     * @deprecate Use {QueryExecutionHTTP.create()....build()}.
     */
    @Deprecated
    static public QueryExecutionHTTP sparqlService(String service, Query query, List<String> defaultGraphURIs, List<String> namedGraphURIs) {
        return sparqlService(service, query.toString(), defaultGraphURIs, namedGraphURIs);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service
     * @param query             Query string to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @return QueryExecution
     * @deprecate Use {QueryExecutionHTTP.create()....build()}.
     */
    @Deprecated
    static public QueryExecutionHTTP sparqlService(String service, String query, List<String> defaultGraphURIs, List<String> namedGraphURIs) {
        QueryExecutionHTTPBuilder builder = createExecutionHTTP(service, query);
        if ( defaultGraphURIs != null )
            defaultGraphURIs.forEach(builder::addDefaultGraphURI);
        if ( namedGraphURIs != null )
            namedGraphURIs.forEach(builder::addNamedGraphURI);
        return builder.build();
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service
     * @param query         Query to execute
     * @param defaultGraph  URI of the default graph
     * @return QueryExecution
     * @deprecate Use {QueryExecutionHTTP.create()....build()}.
     */
    @Deprecated
    static public QueryExecutionHTTP sparqlService(String service, Query query, String defaultGraph) {
        return sparqlService(service, query.toString(), List.of(defaultGraph), null);
    }

    /** Create a service request for remote execution over HTTP.
     * allows various HTTP specific parameters to be set.
     * @param service Endpoint URL
     * @param query Query
     * @return QueryExecutionHTTP
     * @deprecated Use the builder directly {@code QueryExecutionHTTP.create()....build()}
     */
    @Deprecated
    static public QueryExecutionHTTPBuilder createServiceRequest(String service, Query query) {
        return createExecutionHTTP(service, query.toString());
    }

    // All createServiceRequest calls
    static private QueryExecutionHTTPBuilder createExecutionHTTP(String serviceURL, String queryStr) {
        return QueryExecutionHTTP.create().endpoint(serviceURL).queryString(queryStr);
    }

    // -----------------

    static public Plan createPlan(Query query, DatasetGraph dataset, Binding input, Context context) {
        return makePlan(query, dataset, input, context);
    }

    public static Plan createPlan(Query query, DatasetGraph dataset, Binding input) {
        return makePlan(query, dataset, input, null);
    }

    private static Query toQuery(Element pattern) {
        Query query = QueryFactory.make();
        query.setQueryPattern(pattern);
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        return query;
    }

    private static Plan makePlan(Query query, DatasetGraph dataset, Binding input, Context context)
    {
        if ( context == null )
            context = new Context(ARQ.getContext());
        if ( input == null )
            input = BindingRoot.create();
        QueryEngineFactory f = QueryEngineRegistry.get().find(query, dataset, context);
        if ( f == null )
            return null;
        return f.create(query, dataset, input, context);
    }
    // ---------------- Internal routines

    static private Query makeQuery(String queryStr) {
        return QueryFactory.create(queryStr);
    }

    static private Query makeQuery(String queryStr, Syntax syntax) {
        return QueryFactory.create(queryStr, syntax);
    }

    static protected QueryExecution make(Query query) {
        return QueryExecution.create().query(query).build();
    }

    protected static QueryExecution make(Query query, Model model) {
        Graph graph = model.getGraph();
        DatasetGraph dataset = DatasetGraphFactory.wrap(graph);
        Graph g = unwrap(graph);
        if ( g instanceof GraphView ) {
            GraphView gv = (GraphView)g;
            // Copy context of the storage dataset to the wrapper dataset.
            dataset.getContext().putAll(gv.getDataset().getContext());
        }
        return make(query, dataset, null);
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

    protected static QueryExecution make(Query query, Dataset dataset, QuerySolution initialBinding) {
        QueryExecutionBuilder builder = QueryExecution.create().query(query);
        if ( dataset != null )
            builder.dataset(dataset);
        if ( initialBinding != null )
            builder.initialBinding(initialBinding);
        return builder.build();
    }

    protected static QueryExecution make(Query query, DatasetGraph dataset, Binding initialBinding) {
        QueryExecBuilder builder = QueryExec.newBuilder().query(query);
        if ( dataset != null )
            builder.dataset(dataset);
        if ( initialBinding != null )
            builder.initialBinding(initialBinding);
        return QueryExecutionCompat.compatibility(builder);
    }

    static private void checkArg(Model model)
    { requireNonNull(model, "Model is a null pointer"); }

//    static private void checkArg(Dataset dataset)
//    { requireNonNull(dataset, "Dataset is a null pointer"); }

    static private void checkArg(String queryStr)
    { requireNonNull(queryStr, "Query string is null"); }

    static private void checkArg(Query query)
    { requireNonNull(query, "Query is null"); }
}
