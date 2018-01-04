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

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.InputStream;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.*;
import org.apache.jena.riot.web.HttpCaptureResponse;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpResponseLib;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.web.HttpSC;

/** 
 * Implementation of the {@link RDFConnection} interface using remote SPARQL operations.  
 */
public class RDFConnectionRemote implements RDFConnection {
    private static final String fusekiDftSrvQuery   = "sparql";
    private static final String fusekiDftSrvUpdate  = "update";
    private static final String fusekiDftSrvGSP     = "data";
    
    private boolean isOpen = true; 
    private final String destination;
    private final String svcQuery;
    private final String svcUpdate;
    private final String svcGraphStore;
    private HttpClient httpClient;
    private HttpContext httpContext = null;
    
    /** Create connection that will use the {@link HttpClient} using URL of the dataset and default service names */
    public RDFConnectionRemote(HttpClient httpClient, String destination) {
        this(httpClient,
             requireNonNull(destination),
             fusekiDftSrvQuery, 
             fusekiDftSrvUpdate,
             fusekiDftSrvGSP);
    }

    /** Create connection, using URL of the dataset and default service names */
    public RDFConnectionRemote(String destination) {
        this(requireNonNull(destination),
             fusekiDftSrvQuery, 
             fusekiDftSrvUpdate,
             fusekiDftSrvGSP);
    }

    /** Create connection, using full URLs for services. Pass a null for "no service endpoint". */
    public RDFConnectionRemote(String sQuery, String sUpdate, String sGSP) {
        this(null, sQuery, sUpdate, sGSP);
    }
    
    /** Create connection, using URL of the dataset and short names for the services */
    public RDFConnectionRemote(String destination, String sQuery, String sUpdate, String sGSP) {
        this(null, destination, sQuery, sUpdate, sGSP);
    }
    
    /** Create connection, using URL of the dataset and short names for the services */
    public RDFConnectionRemote(HttpClient httpClient, String destination, String sQuery, String sUpdate, String sGSP) {
        this.destination = destination;
        this.svcQuery = RDFConn.formServiceURL(destination, sQuery);
        this.svcUpdate = RDFConn.formServiceURL(destination, sUpdate);
        this.svcGraphStore = RDFConn.formServiceURL(destination, sGSP);
        this.httpClient = httpClient;
    }
    
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }

    public void setHttpContext(HttpContext httpContext) {
        this.httpContext = httpContext;
    }

    @Override
    public QueryExecution query(Query query) {
        checkQuery();
        return exec(()->QueryExecutionFactory.sparqlService(svcQuery, query, this.httpClient, this.httpContext));
    }

    @Override
    public void update(UpdateRequest update) {
        checkUpdate();
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(update, svcUpdate, this.httpClient, this.httpContext);
        exec(()->proc.execute());
    }
    
    @Override
    public Model fetch(String graphName) {
        checkGSP();
        String url = RDFConn.urlForGraph(svcGraphStore, graphName);
        Graph graph = fetch$(url);
        return ModelFactory.createModelForGraph(graph);
    }
    
    @Override
    public Model fetch() {
        checkGSP();
        return fetch(null);
    }
    
    private Graph fetch$(String url) {
        HttpCaptureResponse<Graph> graph = HttpResponseLib.graphHandler();
        exec(()->HttpOp.execHttpGet(url, WebContent.defaultGraphAcceptHeader, graph, this.httpClient, this.httpContext));
        return graph.get();
    }

    @Override
    public void load(String graph, String file) {
        checkGSP();
        upload(graph, file, false);
    }
    
    @Override
    public void load(String file) {
        checkGSP();
        upload(null, file, false);
    }
    
    @Override
    public void load(Model model) {
        doPutPost(model, null, false);
    }
    
    @Override
    public void load(String graphName, Model model) {
        doPutPost(model, graphName, false);
    }
    
    @Override
    public void put(String graph, String file) {
        checkGSP();
        upload(graph, file, true);
    }
    
    @Override
    public void put(String file) { 
        checkGSP();
        upload(null, file, true); 
    }
    
    @Override
    public void put(String graphName, Model model) {
        checkGSP();
        doPutPost(model, graphName, true);
    }

    @Override
    public void put(Model model) {
        checkGSP();
        doPutPost(model, null, true);
    }
    
    /** Send a file to named graph (or "default" or null for the default graph).
     * <p>
     * The Content-Type is inferred from the file extension.
     * <p>
     * "Replace" means overwrite existing data, othewise the date is added to the target.
     */
    protected void upload(String graph, String file, boolean replace) {
        // if triples
        Lang lang = RDFLanguages.filenameToLang(file);
        if ( RDFLanguages.isQuads(lang) )
            throw new ARQException("Can't load quads into a graph");
        if ( ! RDFLanguages.isTriples(lang) )
            throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")");
        String url = RDFConn.urlForGraph(svcGraphStore, graph);
        doPutPost(url, file, lang, replace);
    }

    /** Send a file to named graph (or "default" or null for the default graph).
     * <p>
     * The Content-Type is taken from the given {@code Lang}.
     * <p>
     * "Replace" means overwrite existing data, othewise the date is added to the target.
     */
    protected void doPutPost(String url, String file, Lang lang, boolean replace) {
        File f = new File(file);
        long length = f.length(); 
        InputStream source = IO.openFile(file);
        // Charset.
        exec(()->{
            if ( replace )
                HttpOp.execHttpPut(url, lang.getContentType().getContentType(), source, length, httpClient, this.httpContext);
            else    
                HttpOp.execHttpPost(url, lang.getContentType().getContentType(), source, length, null, null, httpClient, this.httpContext);
        });
    }

    /** Send a model to named graph (or "default" or null for the default graph).
     * <p>
     * The Content-Type is taken from the given {@code Lang}.
     * <p>
     * "Replace" means overwrite existing data, othewise the date is added to the target.
     */
    protected void doPutPost(Model model, String name, boolean replace) {
        String url = RDFConn.urlForGraph(svcGraphStore, name);
        exec(()->{
            Graph graph = model.getGraph();
            if ( replace )
                HttpOp.execHttpPut(url, graphToHttpEntity(graph), httpClient, this.httpContext);
            else    
                HttpOp.execHttpPost(url, graphToHttpEntity(graph), null, null, httpClient, this.httpContext);
        });
    }

    @Override
    public void delete(String graph) {
        checkGSP();
        String url = RDFConn.urlForGraph(svcGraphStore, graph);
        exec(()->HttpOp.execHttpDelete(url));
    }

    @Override
    public void delete() {
        checkGSP();
        delete(null);
    }
    
    @Override
    public Dataset fetchDataset() {
        checkDataset();
        DatasetGraph dsg = fetchDataset$();
        return DatasetFactory.wrap(dsg);
    }

    private DatasetGraph fetchDataset$() {
        HttpCaptureResponse<DatasetGraph> dsg = HttpResponseLib.datasetHandler();
        exec(()->HttpOp.execHttpGet(destination, WebContent.defaultDatasetAcceptHeader, dsg, this.httpClient, this.httpContext));
        return dsg.get();
    }
    
    @Override
    public void loadDataset(String file) { 
        checkDataset();
        doPutPostDataset(file, false); 
    }
    
    @Override
    public void loadDataset(Dataset dataset) {
        checkDataset();
        doPutPostDataset(dataset, false); 
    }

    @Override
    public void putDataset(String file) {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URl provided"); 
        doPutPostDataset(file, true);
    }
    
    @Override
    public void putDataset(Dataset dataset) {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URl provided"); 
        doPutPostDataset(dataset, true); 
    }

    /** Do a PUT or POST to a dataset, sending the contents of the file.
     * <p>
     * The Content-Type is inferred from the file extension.
     * <p>
     * "Replace" implies PUT, otherwise a POST is used.
     */
    protected void doPutPostDataset(String file, boolean replace) {
        Lang lang = RDFLanguages.filenameToLang(file);
        File f = new File(file);
        long length = f.length();
        exec(()->{
            InputStream source = IO.openFile(file);
            if ( replace )
                HttpOp.execHttpPut(destination, lang.getContentType().getContentType(), source, length, httpClient, httpContext);
            else    
                HttpOp.execHttpPost(destination, lang.getContentType().getContentType(), source, length, null, null, httpClient, httpContext);
        });
    }

    /** Do a PUT or POST to a dataset, sending the contents of a datasets.
     * The Content-Type is {@code application/n-quads}.
     * <p>
     * "Replace" implies PUT, otherwise a POST is used.
     */
    protected void doPutPostDataset(Dataset dataset, boolean replace) {
        exec(()->{
            DatasetGraph dsg = dataset.asDatasetGraph();
            if ( replace )
                HttpOp.execHttpPut(destination, datasetToHttpEntity(dsg), httpClient, null);
            else    
                HttpOp.execHttpPost(destination, datasetToHttpEntity(dsg), httpClient, null);
        });
    }

    private void checkQuery() {
        checkOpen();
        if ( svcQuery == null )
            throw new ARQException("No query service defined for this RDFConnection");
    }
    
    private void checkUpdate() {
        checkOpen();
        if ( svcUpdate == null )
            throw new ARQException("No update service defined for this RDFConnection");
    }
    
    private void checkGSP() {
        checkOpen();
        if ( svcGraphStore == null )
            throw new ARQException("No SPARQL Graph Store service defined for this RDFConnection");
    }
    
    private void checkDataset() {
        checkOpen();
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided"); 
    }

    private void checkOpen() {
        if ( ! isOpen )
            throw new ARQException("closed");
    }

    @Override
    public void close() {
        isOpen = false;
    }

    @Override
    public boolean isClosed() {
        return ! isOpen;
    }

    /** Create an HttpEntity for the graph */  
    protected HttpEntity graphToHttpEntity(Graph graph) {
        return graphToHttpEntity(graph, RDFFormat.NTRIPLES);
    }
    
    /** Create an HttpEntity for the graph */
    protected HttpEntity graphToHttpEntity(Graph graph, RDFFormat syntax) {
        EntityTemplate entity = new EntityTemplate((out)->RDFDataMgr.write(out, graph, syntax));
        String ct = syntax.getLang().getContentType().getContentType();
        entity.setContentType(ct);
        return entity;
    }

    /** Create an HttpEntity for the dataset */  
    protected HttpEntity datasetToHttpEntity(DatasetGraph dataset) {
        return datasetToHttpEntity(dataset, RDFFormat.NQUADS);
    }
    
    /** Create an HttpEntity for the dataset */  
    protected HttpEntity datasetToHttpEntity(DatasetGraph dataset, RDFFormat syntax) {
        EntityTemplate entity = new EntityTemplate((out)->RDFDataMgr.write(out, dataset, syntax));
        String ct = syntax.getLang().getContentType().getContentType();
        entity.setContentType(ct);
        return entity;
    }

    /** Convert HTTP status codes to exceptions */ 
    static void exec(Runnable action)  {
        try { action.run(); }
        catch (HttpException ex) { handleHttpException(ex, false); }
    }

    /** Convert HTTP status codes to exceptions */ 
    static <X> X exec(Supplier<X> action)  {
        try { return action.get(); }
        catch (HttpException ex) { handleHttpException(ex, true); return null;}
    }

    private static void handleHttpException(HttpException ex, boolean ignore404) {
        if ( ex.getResponseCode() == HttpSC.NOT_FOUND_404 && ignore404 )
            return ;
        throw ex;
    }

    private final Transactional txn = TransactionalLock.createMRPlusSW();
    
    @Override public void begin()                       { txn.begin(); }
    @Override public void begin(TxnType txnType)        { txn.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn.begin(mode); }
    @Override public boolean promote()                  { return txn.promote(); }
    @Override public void commit()                      { txn.commit(); }
    @Override public void abort()                       { txn.abort(); }
    @Override public boolean isInTransaction()          { return txn.isInTransaction(); }
    @Override public void end()                         { txn.end(); }
    @Override public ReadWrite transactionMode()        { return txn.transactionMode(); }
    @Override public TxnType transactionType()          { return txn.transactionType(); }
}

