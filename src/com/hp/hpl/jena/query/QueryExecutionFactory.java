/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query;
import java.util.List ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DataSourceImpl ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.DatasetImpl ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.FileManager ;


/** Place to make QueryExecution objects from Query objects or a string. */
 
public class QueryExecutionFactory
{
    protected QueryExecutionFactory() {}
    
    // ---------------- Query
    
    /** Create a QueryExecution
     * 
     * @param query Query
     * @return QueryExecution
     */
    static public QueryExecution create(Query query)
    {
        checkArg(query) ;
        return make(query) ;
    }

    /** Create a QueryExecution
     * 
     * @param queryStr Query string
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr)) ;
    }

    /** Create a QueryExecution
     * 
     * @param queryStr Query string
     * @param syntax   Query syntax
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Syntax syntax)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr, syntax)) ;
    }
    
    // ---------------- Query + Dataset
    
    /** Create a QueryExecution to execute over the Dataset.
     * 
     * @param query     Query
     * @param dataset   Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, Dataset dataset)
    {
        //checkArg(dataset) ; // Allow null
        return make(query, dataset) ;
    }

    /** Create a QueryExecution to execute over the Dataset.
     * 
     * @param queryStr     Query string
     * @param dataset      Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Dataset dataset)
    {
        checkArg(queryStr) ;
        //checkArg(dataset) ; // Allow null
        return make(makeQuery(queryStr), dataset) ;
    }

    /** Create a QueryExecution to execute over the Dataset.
     * 
     * @param queryStr     Query string
     * @param syntax       Query language
     * @param dataset      Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Syntax syntax, Dataset dataset)
    {
        checkArg(queryStr) ;
        //checkArg(dataset) ; // Allow null
        return make(makeQuery(queryStr, syntax), dataset) ;
    }

    /** Create a QueryExecution : the file manager will be used to load
     *  URIs in the query decription. 
     * 
     * @param query Query
     * @param fm    FileManager 
     * @return QueryExecution
     */

    static public QueryExecution create(Query query, FileManager fm)
    {
        checkArg(query) ;
        QueryExecution qe = make(query) ;
        if ( fm != null )
            qe.setFileManager(fm) ;
        return qe ;
    }

    /** Create a QueryExecution : the file manager will be used to load
     *  URIs in the query decription. 
     * 
     * @param queryStr Query string
     * @param fm       FileManager 
     * @return QueryExecution
     */

    static public QueryExecution create(String queryStr, FileManager fm)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr), fm) ;
    }

    /** Create a QueryExecution : the file manager will be used to load
     *  URIs in the query decription. 
     * 
     * @param queryStr Query string
     * @param syntax   Syntax
     * @param fm       FileManager 
     * @return QueryExecution
     */

    static public QueryExecution create(String queryStr, Syntax syntax, FileManager fm)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr, syntax), fm) ;
    }

    // ---------------- Query + Model
    
    /** Create a QueryExecution to execute over the Model.
     * 
     * @param query     Query
     * @param model     Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, Model model)
    {
        checkArg(query) ;
        checkArg(model) ;
        return make(query, new DatasetImpl(model)) ;
    }

    /** Create a QueryExecution to execute over the Model.
     * 
     * @param queryStr     Query string
     * @param model     Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Model model)
    {
        checkArg(queryStr) ;
        checkArg(model) ;
        return create(makeQuery(queryStr), model) ;
    }

    /** Create a QueryExecution to execute over the Model.
     * 
     * @param queryStr     Query string
     * @param lang         Query language
     * @param model        Target of the query
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Syntax lang, Model model)
    {
        checkArg(queryStr) ;
        checkArg(model) ;
        return create(makeQuery(queryStr, lang), model) ;
    }

    /** Create a QueryExecution to execute over the Model.
     * 
     * @param query         Query string
     * @param initialBinding    Any initial binding of variables      
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, QuerySolution initialBinding)
    {
        checkArg(query) ;
        QueryExecution qe = make(query) ;
        if ( initialBinding != null )
            qe.setInitialBinding(initialBinding) ;
        return qe ;
    }

    /** Create a QueryExecution given some initial values of variables.
     * 
     * @param queryStr          QueryString
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, QuerySolution initialBinding)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr), initialBinding) ; 
    }

    /** Create a QueryExecution given some initial values of variables.
     * 
     * @param queryStr          QueryString
     * @param syntax            Query language syntax
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Syntax syntax, QuerySolution initialBinding)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr, syntax), initialBinding) ; 
    }


    /** Create a QueryExecution to execute over the Model, 
     * given some initial values of variables.
     * 
     * @param query            Query
     * @param model            Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, Model model, QuerySolution initialBinding)
    {
        checkArg(model) ;
        return create(query, new DataSourceImpl(model), initialBinding) ;
    }
    
    /** Create a QueryExecution to execute over the Model, 
     * given some initial values of variables.
     * 
     * @param queryStr         Query string
     * @param model            Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Model model, QuerySolution initialBinding)
    {
        checkArg(queryStr) ;
        checkArg(model) ;
        return create(makeQuery(queryStr), model, initialBinding) ;
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
    static public QueryExecution create(String queryStr, Syntax syntax, Model model, QuerySolution initialBinding)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr, syntax), model, initialBinding) ;
    }
    
    /** Create a QueryExecution over a Dataset given some initial values of variables.
     * 
     * @param query            Query
     * @param dataset          Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(Query query, Dataset dataset, QuerySolution initialBinding)
    {
        checkArg(query) ;
        QueryExecution qe = make(query, dataset) ;
        if ( initialBinding != null )
            qe.setInitialBinding(initialBinding) ;
        return qe ;
    }

    /** Create a QueryExecution over a Dataset given some initial values of variables.
     * 
     * @param queryStr         Query string
     * @param dataset          Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Dataset dataset, QuerySolution initialBinding)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr), dataset, initialBinding) ;
    }

    /** Create a QueryExecution over a Dataset given some initial values of variables.
     * 
     * @param queryStr         Query string
     * @param dataset          Target of the query
     * @param initialBinding    Any initial binding of variables
     * @return QueryExecution
     */
    static public QueryExecution create(String queryStr, Syntax syntax, Dataset dataset, QuerySolution initialBinding)
    {
        checkArg(queryStr) ;
        return create(makeQuery(queryStr, syntax), dataset, initialBinding) ;
    }

    // ---------------- Remote query execution
    
    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service 
     * @param query     Query string to execute 
     * @return QueryExecution
     */ 
     
    static public QueryExecution sparqlService(String service, String query)
    {
        checkNotNull(service, "URL for service is null") ;
        checkArg(query) ;
        return sparqlService(service, QueryFactory.create(query)) ;
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service 
     * @param query         Query string to execute
     * @param defaultGraph  URI of the default graph
     * @return QueryExecution
     */ 
     
    static public QueryExecution sparqlService(String service, String query, String defaultGraph)
    {
        checkNotNull(service, "URL for service is null") ;
        //checkNotNull(defaultGraph, "IRI for default graph is null") ;
        checkArg(query) ;
        return sparqlService(service, QueryFactory.create(query), defaultGraph) ;
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service 
     * @param query             Query string to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @return QueryExecution
     */ 
    static public QueryExecution sparqlService(String service, String query, List<String> defaultGraphURIs, List<String> namedGraphURIs)
    {
        checkNotNull(service, "URL for service is null") ;
        //checkNotNull(defaultGraphURIs, "List of default graph URIs is null") ;
        //checkNotNull(namedGraphURIs, "List of named graph URIs is null") ;
        checkArg(query) ;
        return sparqlService(service, QueryFactory.create(query), defaultGraphURIs, namedGraphURIs) ;
    }
    
    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service   URL of the remote service 
     * @param query     Query to execute 
     * @return QueryExecution
     */ 
     
    static public QueryExecution sparqlService(String service, Query query)
    {
        checkNotNull(service, "URL for service is null") ;
        checkArg(query) ;
        return createServiceRequest(service, query) ;
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service           URL of the remote service 
     * @param query             Query to execute
     * @param defaultGraphURIs  List of URIs to make up the default graph
     * @param namedGraphURIs    List of URIs to make up the named graphs
     * @return QueryExecution
     */ 
    static public QueryExecution sparqlService(String service, Query query, List<String> defaultGraphURIs, List<String> namedGraphURIs)
    {
        checkNotNull(service, "URL for service is null") ;
        //checkNotNull(defaultGraphURIs, "List of default graph URIs is null") ;
        //checkNotNull(namedGraphURIs, "List of named graph URIs is null") ;
        checkArg(query) ;
        QueryEngineHTTP qe = createServiceRequest(service, query) ;
        if ( defaultGraphURIs != null )
            qe.setDefaultGraphURIs(defaultGraphURIs) ;
        if ( namedGraphURIs != null )
            qe.setNamedGraphURIs(namedGraphURIs) ;
        return qe ;
    }

    /** Create a QueryExecution that will access a SPARQL service over HTTP
     * @param service       URL of the remote service 
     * @param query         Query to execute
     * @param defaultGraph  URI of the default graph
     * @return QueryExecution
     */ 
     
    static public QueryExecution sparqlService(String service, Query query, String defaultGraph)
    {
        checkNotNull(service, "URL for service is null") ;
        //checkNotNull(defaultGraph, "IRI for default graph is null") ;
        checkArg(query) ;
        QueryEngineHTTP qe = createServiceRequest(service, query) ;
        qe.addDefaultGraph(defaultGraph) ;
        return qe ;
    }

    /** Create a service request for remote execution over HTTP.  The returned class,
     * {@link QueryEngineHTTP},
     * allows various HTTP specific paramters to be set. 
     */
    static public QueryEngineHTTP createServiceRequest(String service, Query query)
    {
        QueryEngineHTTP qe = new QueryEngineHTTP(service, query) ;
        return qe ;
    }

    // -----------------
    
    static public Plan createPlan(Query query, DatasetGraph dataset, Binding input, Context context)
    {
        return makePlan(query, dataset, input, context) ;
    }
    
    public static Plan createPlan(String queryStr, Graph graph)
    {
        return createPlan(QueryFactory.create(queryStr), graph) ; 
    }
    
    public static Plan createPlan(String queryStr, DatasetGraph dataset)
    {
        return createPlan(QueryFactory.create(queryStr), dataset) ; 
    }
     
    public static Plan createPlan(Query query, Graph graph)
    {
        return makePlan(query, DatasetGraphFactory.createOneGraph(graph), null, null) ; 
    }
    
    public static Plan createPlan(Query query, DatasetGraph dataset)
    {
        return makePlan(query, dataset, null, null) ;
    }
    
    public static Plan createPlan(Query query, DatasetGraph dataset, Binding input)
    {
        return makePlan(query, dataset, input, null) ;
    }

    public static Plan createPlan(Element pattern, Graph graph)
    {
        return createPlan(toQuery(pattern), graph) ; 
    }
    
    public static Plan createPlan(Element pattern, DatasetGraph dataset)
    {
        return createPlan(toQuery(pattern), dataset, null) ;
    }
    
    public static Plan createPlan(Element pattern, DatasetGraph dataset, Binding input)
    {
        return createPlan(toQuery(pattern), dataset, input) ;
    }
    
    private static Query toQuery(Element pattern)
    {
        Query query = QueryFactory.make() ;
        query.setQueryPattern(pattern) ;
        query.setQuerySelectType() ;
        query.setQueryResultStar(true) ;
        return query ;
    }

    private static Plan makePlan(Query query, DatasetGraph dataset, Binding input, Context context)
    {
        if ( context == null )
            context = new Context(ARQ.getContext()) ;
        if ( input == null )
            input = BindingRoot.create() ; 
        QueryEngineFactory f = findFactory(query, dataset, context) ;
        if ( f == null )
            return null ;
        return f.create(query, dataset, input, context) ;
    }
    // ---------------- Internal routines
    
    // Make query
    
    static private Query makeQuery(String queryStr)
    {
        return QueryFactory.create(queryStr) ;
    }


    static private Query makeQuery(String queryStr, Syntax syntax)
    {
        return QueryFactory.create(queryStr, syntax);
    }
    
    // ---- Make executions
    
    static protected QueryExecution make(Query query)
    {
        return make(query, null) ;
    }

    protected  static QueryExecution make(Query query, Dataset dataset)
    { return make(query, dataset, null) ; }

    
    protected static QueryExecution make(Query query, Dataset dataset, Context context)
    {
        query.setResultVars() ;
        if ( context == null )
            context = ARQ.getContext();  // .copy done in QueryExecutionBase -> Context.setupContext. 
        DatasetGraph dsg = null ;
        if ( dataset != null )
            dsg = dataset.asDatasetGraph() ;
        QueryEngineFactory f = findFactory(query, dsg, context);
        if ( f == null )
        {
            Log.warn(QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: "+query) ;
            return null ;
        }
        return new QueryExecutionBase(query, dataset, context, f) ;
    }
    
    static private QueryEngineFactory findFactory(Query query, DatasetGraph dataset, Context context)
    {
        return QueryEngineRegistry.get().find(query, dataset, context);
    }
    
    
    static private void checkNotNull(Object obj, String msg)
    {
        if ( obj == null )
            throw new IllegalArgumentException(msg) ;
    }
    
    static private void checkArg(Model model)
    { checkNotNull(model, "Model is a null pointer") ; }

//    static private void checkArg(Dataset dataset)
//    { checkNotNull(dataset, "Dataset is a null pointer") ; }

    static private void checkArg(String queryStr)
    { checkNotNull(queryStr, "Query string is null") ; }

    static private void checkArg(Query query)
    { checkNotNull(query, "Query is null") ; }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
