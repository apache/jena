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
import java.util.List;
import java.util.Objects;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
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
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.Context;

/**
 * Place to make {@link QueryExecution} objects from {@link Query} objects or a string.
 *
 * For detailed creation of a {@link QueryExecution}, see {@link QueryExecutionBuilder}.
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
        return make(query, dataset);
    }

    /**
     * Create a QueryExecution to execute over the {@link DatasetGraph}.
     *
     * @param query Query
     * @param datasetGraph Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, DatasetGraph datasetGraph) {
        Objects.requireNonNull(query, "Query is null");
        Objects.requireNonNull(datasetGraph, "DatasetGraph is null");
        return make(query, datasetGraph);
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
        return make(makeQuery(queryStr), dataset);
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
        return make(makeQuery(queryStr, syntax), dataset);
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
     */
    static public QueryExecution create(Query query, QuerySolution initialBinding) {
        checkArg(query);
        QueryExecution qe = make(query);
        if ( initialBinding != null )
            qe.setInitialBinding(initialBinding);
        return qe;
    }

    /** Create a QueryExecution given some initial values of variables.
     *
     * @param queryStr          QueryString
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
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
     */
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
     */
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
        QueryExecution qe = make(query, dataset);
        if ( initialBinding != null )
            qe.setInitialBinding(initialBinding);
        return qe;
    }

    /** Create a QueryExecution over a Dataset given some initial values of variables.
     *
     * @param queryStr         Query string
     * @param dataset          Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
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
     */
    static public QueryExecution create(String queryStr, Syntax syntax, Dataset dataset, QuerySolution initialBinding) {
        checkArg(queryStr);
        return create(makeQuery(queryStr, syntax), dataset, initialBinding);
    }

    // ---------------- Remote query execution

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service
     * @param query     Query string to execute
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, String query) {
        return sparqlService(service, query, (HttpClient)null);
    }

    static public QueryExecution sparqlService(String service, String query, HttpClient client) {
        return sparqlService(service, query, client, null);
    }
        /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service
     * @param query     Query string to execute
     * @param client    HTTP client
     * @param httpContext HTTP Context
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, String query, HttpClient client, HttpContext httpContext) {
        checkNotNull(service, "URL for service is null");
        checkArg(query);
        return sparqlService(service, QueryFactory.create(query), client);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service
     * @param query         Query string to execute
     * @param defaultGraph  URI of the default graph
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, String query, String defaultGraph) {
        return sparqlService(service, query, defaultGraph, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service
     * @param query         Query string to execute
     * @param defaultGraph  URI of the default graph
     * @param client        HTTP client
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, String query, String defaultGraph, HttpClient client) {
        return sparqlService(service, query, defaultGraph, client, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service
     * @param query         Query string to execute
     * @param defaultGraph  URI of the default graph
     * @param client        HTTP client
     * @param httpContext   HTTP Context
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, String query, String defaultGraph, HttpClient client, HttpContext httpContext) {
        checkNotNull(service, "URL for service is null");
        // checkNotNull(defaultGraph, "IRI for default graph is null");
        checkArg(query);
        return sparqlService(service, QueryFactory.create(query), defaultGraph, client, httpContext);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service
     * @param query             Query string to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, String query, List<String> defaultGraphURIs, List<String> namedGraphURIs) {
        return sparqlService(service, query, defaultGraphURIs, namedGraphURIs, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service
     * @param query             Query string to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @param client            HTTP client
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, String query, List<String> defaultGraphURIs, List<String> namedGraphURIs,
                                               HttpClient client) {
        return sparqlService(service, query, defaultGraphURIs, namedGraphURIs, client, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service
     * @param query             Query string to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @param client            HTTP client
     * @param httpContext HTTP Context
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, String query, List<String> defaultGraphURIs, List<String> namedGraphURIs,
                                               HttpClient client, HttpContext httpContext) {
        checkNotNull(service, "URL for service is null");
        // checkNotNull(defaultGraphURIs, "List of default graph URIs is null");
        // checkNotNull(namedGraphURIs, "List of named graph URIs is null");
        checkArg(query);
        return sparqlService(service, QueryFactory.create(query), defaultGraphURIs, namedGraphURIs, client, httpContext);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service
     * @param query     Query to execute
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query) {
        return sparqlService(service, query, (HttpClient)null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service
     * @param query     Query to execute
     * @param client    HTTP client
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query, HttpClient client) {
        return sparqlService(service, query, null, null, client, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service
     * @param query     Query to execute
     * @param client    HTTP client
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query, HttpClient client, HttpContext httpContext) {
        checkNotNull(service, "URL for service is null");
        checkArg(query);
        return sparqlServiceWorker(service, query, null, null, client, httpContext);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service
     * @param query             Query to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query, List<String> defaultGraphURIs, List<String> namedGraphURIs) {
        return sparqlService(service, query, defaultGraphURIs, namedGraphURIs, null, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service
     * @param query             Query to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @param client            HTTP client
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query, List<String> defaultGraphURIs, List<String> namedGraphURIs, HttpClient client) {
        return sparqlService(service, query, defaultGraphURIs, namedGraphURIs, client, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service
     * @param query             Query to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @param client            HTTP client
     * @param httpContext       HTTP Context
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query,
                                               List<String> defaultGraphURIs, List<String> namedGraphURIs,
                                               HttpClient client, HttpContext httpContext) {
        checkNotNull(service, "URL for service is null");
        // checkNotNull(defaultGraphURIs, "List of default graph URIs is null");
        // checkNotNull(namedGraphURIs, "List of named graph URIs is null");
        checkArg(query);
        return sparqlServiceWorker(service, query, defaultGraphURIs, namedGraphURIs, client, httpContext);
    }

    // String defaultGraph

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service
     * @param query         Query to execute
     * @param defaultGraph  URI of the default graph
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query, String defaultGraph) {
        return sparqlService(service, query, defaultGraph, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service
     * @param query         Query to execute
     * @param defaultGraph  URI of the default graph
     * @param client        HTTP client
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query, String defaultGraph, HttpClient client) {
       return sparqlService(service, query, defaultGraph, client, null);
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service
     * @param query         Query to execute
     * @param defaultGraph  URI of the default graph
     * @param client        HTTP client
     * @return QueryExecution
     */
    static public QueryExecution sparqlService(String service, Query query, String defaultGraph, HttpClient client, HttpContext httpContext) {
        return sparqlServiceWorker(service, query, List.of(defaultGraph), null, client, httpContext);
    }

    // All sparqlService calls
    static private QueryExecution sparqlServiceWorker(String service, Query query, List<String> defaultGraphURIs, List<String> namedGraphURIs, HttpClient client, HttpContext httpContext) {
        checkNotNull(service, "URL for service is null");
        // checkNotNull(defaultGraph, "IRI for default graph is null");
        checkArg(query);
        QueryEngineHTTP qe = createQueryEngineHTTP(service, query, client, httpContext);
        if ( defaultGraphURIs != null )
            qe.setDefaultGraphURIs(defaultGraphURIs);
        if ( namedGraphURIs != null )
            qe.setNamedGraphURIs(namedGraphURIs);
        return qe;
    }

    /** Create a service request for remote execution over HTTP.  The returned class,
     * {@link QueryEngineHTTP},
     * allows various HTTP specific parameters to be set.
     * @param service Endpoint URL
     * @param query Query
     * @return Remote Query Engine
     */
    static public QueryEngineHTTP createServiceRequest(String service, Query query) {
        return createServiceRequest(service, query, null);
    }

    /** Create a service request for remote execution over HTTP.  The returned class,
     * {@link QueryEngineHTTP},
     * allows various HTTP specific parameters to be set.
     * @param service Endpoint URL
     * @param query Query
     * @param client HTTP client
     * @return Remote Query Engine
     */
    static public QueryEngineHTTP createServiceRequest(String service, Query query, HttpClient client) {
        return createServiceRequest(service, query, client, null);
    }

    /** Create a service request for remote execution over HTTP.  The returned class,
     * {@link QueryEngineHTTP},
     * allows various HTTP specific parameters to be set.
     * @param service Endpoint URL
     * @param query Query
     * @param client HTTP client
     * @param httpContext HTTP Context
     * @return Remote Query Engine
     */
    static public QueryEngineHTTP createServiceRequest(String service, Query query, HttpClient client, HttpContext httpContext) {
        return createQueryEngineHTTP(service, query, client, httpContext);
    }

    // All createServiceRequest calls
    static private QueryEngineHTTP createQueryEngineHTTP(String service, Query query, HttpClient client, HttpContext httpContext) {
        QueryEngineHTTP qe = new QueryEngineHTTP(service, query, client, httpContext);
        return qe;
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

    protected  static QueryExecution make(Query query, Model model) {
        Graph graph = model.getGraph();
        DatasetGraph dataset = DatasetGraphFactory.wrap(graph);
        Graph g = unwrap(graph);
        if ( g instanceof GraphView ) {
            GraphView gv = (GraphView)g;
            // Copy context of the storage dataset to the wrapper dataset.
            dataset.getContext().putAll(gv.getDataset().getContext());
        }
        return make(query, dataset);
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

    protected static QueryExecution make(Query query, Dataset dataset) {
        DatasetGraph dsg = dataset==null ? null : dataset.asDatasetGraph();
        return make(query, dsg);
    }

    @SuppressWarnings("deprecation")
    protected static QueryExecution make(Query query, DatasetGraph datasetGraph)
    { return QueryExecution.create().query(query).dataset(datasetGraph).build(); }

    static private <X> void checkNotNull(X obj, String msg) {
        Objects.requireNonNull(obj, msg);
    }

    static private void checkArg(Model model)
    { checkNotNull(model, "Model is a null pointer"); }

//    static private void checkArg(Dataset dataset)
//    { checkNotNull(dataset, "Dataset is a null pointer"); }

    static private void checkArg(String queryStr)
    { checkNotNull(queryStr, "Query string is null"); }

    static private void checkArg(Query query)
    { checkNotNull(query, "Query is null"); }
}
