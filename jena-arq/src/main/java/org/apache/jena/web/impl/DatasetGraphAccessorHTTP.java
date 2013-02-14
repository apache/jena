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

package org.apache.jena.web.impl;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.InputStream ;

import org.apache.http.Header ;
import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.HttpVersion ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.methods.* ;
import org.apache.http.entity.InputStreamEntity ;
import org.apache.http.impl.client.SystemDefaultHttpClient ;
import org.apache.http.params.BasicHttpParams ;
import org.apache.http.params.HttpConnectionParams ;
import org.apache.http.params.HttpParams ;
import org.apache.http.params.HttpProtocolParams ;
import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.web.JenaHttpException ;
import org.apache.jena.web.JenaHttpNotFoundException ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.graph.UnmodifiableGraph ;

public class DatasetGraphAccessorHTTP implements DatasetGraphAccessor
{
    private final String remote ;

    /** Create a DatasetUpdater for the remote URL */
    public DatasetGraphAccessorHTTP(String remote)
    {
        this.remote = remote ;
    }
    
    @Override
    public Graph httpGet()                            { return doGet(targetDefault()) ; }

    @Override
    public Graph httpGet(Node graphName)              { return doGet(target(graphName)) ; }
    
    private Graph doGet(String url)
    {
        HttpUriRequest httpGet = new HttpGet(url) ;
        try {
            return exec(url, null, httpGet, true) ;
        } catch (JenaHttpNotFoundException ex)
        {
            return null ;  
        }
    }
    
    @Override
    public boolean httpHead()
    {
        return doHead(targetDefault()) ;
    }
    
    @Override
    public boolean httpHead(Node graphName)
    {
        return doHead(target(graphName)) ;
    }

    private boolean doHead(String url)
    {
        HttpUriRequest httpHead = new HttpHead(url) ;
        try {
            exec(url, null, httpHead, false) ;
            return true ;
        } catch (JenaHttpException ex)
        {
            if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 )
                return false ;
            throw ex ;
        }
    }

    @Override
    public void httpPut(Graph data)                   { doPut(targetDefault(), data) ; }

    @Override
    public void httpPut(Node graphName, Graph data)   { doPut(target(graphName), data) ; }

    private void doPut(String url, Graph data)
    {
        HttpUriRequest httpPut = new HttpPut(url) ;
        exec(url, data, httpPut, false) ;
    }
    
    @Override
    public void httpDelete()                          { doDelete(targetDefault()) ; }

    @Override
    public void httpDelete(Node graphName)            { doDelete(target(graphName)) ; }

    private boolean doDelete(String url)
    {
        try {
            HttpUriRequest httpDelete = new HttpDelete(url) ;
            exec(url, null, httpDelete, false) ;
            return true ;
        } catch (JenaHttpNotFoundException ex) { return false ; }
    }
    
    @Override
    public void httpPost(Graph data)                  { doPost(targetDefault(), data) ; }

    @Override
    public void httpPost(Node graphName, Graph data)  { doPost(target(graphName), data) ; }

    private void doPost(String url, Graph data)
    {
        HttpUriRequest httpPost = new HttpPost(url) ;
        exec(url, data, httpPost, false) ;
    }

    @Override
    public void httpPatch(Graph data)                 { throw new UnsupportedOperationException() ; }

    @Override
    public void httpPatch(Node graphName, Graph data) { throw new UnsupportedOperationException() ; }

    private String targetDefault()
    {
        return remote+"?"+HttpNames.paramGraphDefault+"=" ;
    }

    private String target(Node name)
    {
        if ( ! name.isURI() )
            throw new JenaException("Not a URI: "+name) ;
        String guri = name.getURI() ;
        // Encode
        guri = IRILib.encodeUriComponent(guri) ;
        return remote+"?"+HttpNames.paramGraph+"="+guri ;
    }

    static private HttpParams httpParams = createHttpParams() ;
    
    static private HttpParams createHttpParams()
    {
        HttpParams httpParams$ = new BasicHttpParams() ;
        // See DefaultHttpClient.createHttpParams
        HttpProtocolParams.setVersion(httpParams$,               HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams$,        WebContent.charsetUTF8);
        HttpProtocolParams.setUseExpectContinue(httpParams$,     true);
        HttpConnectionParams.setTcpNoDelay(httpParams$,          true);
        HttpConnectionParams.setSocketBufferSize(httpParams$,    32*1024);
        HttpProtocolParams.setUserAgent(httpParams$,             Jena.NAME+"/"+Jena.VERSION);
        return httpParams$;
    }
    
    private static String getHeader(HttpResponse response, String headerName)
    {
        Header h = response.getLastHeader(headerName) ;
        if ( h == null )
            return null ;
        return h.getValue() ;
    }

    private Graph exec(String targetStr, Graph graphToSend, HttpUriRequest httpRequest, boolean processBody)
    {
        HttpClient httpclient = new SystemDefaultHttpClient(httpParams) ;
        
        if ( graphToSend != null )
        {
            // ??? httpRequest isa Post
            // Impedence mismatch - is there a better way?
            ByteArrayOutputStream out = new ByteArrayOutputStream() ;
            Model model = ModelFactory.createModelForGraph(graphToSend) ;
            model.write(out, "RDF/XML") ;
            byte[] bytes = out.toByteArray() ;
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
            InputStreamEntity reqEntity = new InputStreamEntity(in, bytes.length) ;
            reqEntity.setContentType(WebContent.contentTypeRDFXML) ;
            reqEntity.setContentEncoding(WebContent.charsetUTF8) ;
            HttpEntity entity = reqEntity ;
            ((HttpEntityEnclosingRequestBase)httpRequest).setEntity(entity) ;
        }
        TypedInputStream ts = null ;
        // httpclient.getParams().setXXX
        try {
            HttpResponse response = httpclient.execute(httpRequest) ;

            int responseCode = response.getStatusLine().getStatusCode() ;
            String responseMessage = response.getStatusLine().getReasonPhrase() ;
            
            if ( HttpSC.isRedirection(responseCode) )
                // Not implemented yet.
                throw JenaHttpException.create(responseCode, responseMessage) ;

            // Other 400 and 500 - errors

            if ( HttpSC.isClientError(responseCode) || HttpSC.isServerError(responseCode) )
                throw JenaHttpException.create(responseCode, responseMessage) ;

            if ( responseCode == HttpSC.NO_CONTENT_204) return null ;
            if ( responseCode == HttpSC.CREATED_201 ) return null ;
            
            if ( responseCode != HttpSC.OK_200 )
            {
                Log.warn(this, "Unexpected status code") ;
                throw JenaHttpException.create(responseCode, responseMessage) ;
            }
            
            // May not have a body.
            String ct = getHeader(response, HttpNames.hContentType) ;
            if ( ct == null )
            {
                HttpEntity entity = response.getEntity() ;
                
                if (entity != null)
                {
                    InputStream instream = entity.getContent() ;
                    // Read to completion?
                    instream.close() ;
                }
                return null ;
            }
            
            // Tidy. See ConNeg / MediaType.
            String x = getHeader(response, HttpNames.hContentType) ;
            String y[] = x.split(";") ;
            String contentType = null ;
            if ( y[0] != null )
                contentType = y[0].trim();
            String charset = null ;
            if ( y.length > 1 && y[1] != null )
                charset = y[1].trim();

            // Get hold of the response entity
            HttpEntity entity = response.getEntity() ;

            if (entity != null)
            {
                InputStream instream = entity.getContent() ;
//                String mimeType = ConNeg.chooseContentType(request, rdfOffer, ConNeg.acceptRDFXML).getAcceptType() ;
//                String charset = ConNeg.chooseCharset(request, charsetOffer, ConNeg.charsetUTF8).getAcceptType() ;
                ts = new TypedInputStream(instream, contentType, charset, null) ;
            }
            Graph graph = GraphFactory.createGraphMem() ;
            if ( processBody )
                readGraph(graph, ts, null) ;
            if ( ts != null )
                ts.close() ;
            Graph graph2 = new UnmodifiableGraph(graph) ;
            return graph2 ;
        } catch (IOException ex)
        {
            httpRequest.abort() ;
            return null ;
        }
    }

    private void readGraph(Graph graph, TypedInputStream ts, String base)
    {
        // Yes - we ignore the charset.
        // Either it's XML and so the XML parser deals with it, or the 
        // language determines the charset and the parsers offer InputStreams.   
       
        Lang lang = WebContent.contentTypeToLang(ts.getContentType()) ;
        if ( lang == null )
            throw new RiotException("Unknown lang for "+ts.getMediaType()) ;
        RDFDataMgr.read(graph, ts, lang) ; 
    }        
}
