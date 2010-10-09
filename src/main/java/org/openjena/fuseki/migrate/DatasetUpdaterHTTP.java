/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.migrate;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.InputStream ;

import org.apache.http.Header ;
import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.HttpVersion ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.methods.HttpDelete ;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.client.methods.HttpPost ;
import org.apache.http.client.methods.HttpPut ;
import org.apache.http.client.methods.HttpUriRequest ;
import org.apache.http.entity.InputStreamEntity ;
import org.apache.http.impl.client.DefaultHttpClient ;
import org.apache.http.params.BasicHttpParams ;
import org.apache.http.params.HttpConnectionParams ;
import org.apache.http.params.HttpParams ;
import org.apache.http.params.HttpProtocolParams ;
import org.apache.http.protocol.HTTP ;
import org.apache.http.util.VersionInfo ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.fuseki.FusekiException ;
import org.openjena.fuseki.FusekiLib ;
import org.openjena.fuseki.FusekiRequestException ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.conneg.TypedStream ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.WebContent ;
import org.openjena.riot.lang.LangRDFXML ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkTriplesToGraph ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

public class DatasetUpdaterHTTP //implements DatasetUpdater
{
    private final String remote ;
    // Library
    static final String paramGraph = "graph" ; 
    static final String paramDefault = "default" ; 

    /** Create a updater for the remote URL */
    public DatasetUpdaterHTTP(String remote)
    {
        this.remote = remote ;
    }
    
    public Graph doGet()                            { return doGet(targetDefault()) ; }

    public Graph doGet(Node graphName)              { return doGet(target(graphName.getURI())) ; }
    
    private Graph doGet(String url)
    {
        HttpUriRequest httpGet = new HttpGet(url) ;
        return exec(url, null, httpGet) ;
    }
    
    public void doPut(Graph data)                   { doPut(targetDefault(), data) ; }

    public void doPut(Node graphName, Graph data)   { doPut(target(graphName.getURI()), data) ; }

    private void doPut(String url, Graph data)
    {
        HttpUriRequest httpPut = new HttpPut(url) ;
        exec(url, data, httpPut) ;
    }
    
    public void doDelete()                          { doDelete(targetDefault()) ; }

    public void doDelete(Node graphName)            { doDelete(target(graphName.getURI())) ; }

    private void doDelete(String url)
    {
        HttpUriRequest httpDelete = new HttpDelete(url) ;
        exec(url, null, httpDelete) ;
    }
    
    public void doPost(Graph data)                  { doPost(targetDefault(), data) ; }

    public void doPost(Node graphName, Graph data)  { doPost(target(graphName.getURI()), data) ; }

    private void doPost(String url, Graph data)
    {
        HttpUriRequest httpPost = new HttpPost(url) ;
        exec(url, data, httpPost) ;
    }

    public void doPatch(Graph data)                 { throw new UnsupportedOperationException() ; }

    public void doPatch(Node graphName, Graph data) { throw new UnsupportedOperationException() ; }

    private String targetDefault()
    {
        return remote+"?"+paramDefault+"=" ;
    }

    private String target(String name)
    {
        return remote+"?"+paramGraph+"="+name ;
    }

    static private HttpParams httpParams = createHttpParams() ;
    
    static private HttpParams createHttpParams()
    {
        HttpParams httpParams = new BasicHttpParams() ;
        // See DefaultHttpClient.createHttpParams
        HttpProtocolParams.setVersion(httpParams,               HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams,        HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(httpParams,     true);
        HttpConnectionParams.setTcpNoDelay(httpParams,          true);
        HttpConnectionParams.setSocketBufferSize(httpParams,    32*1024);

        // determine the release version from packaged version info
        final VersionInfo vi = VersionInfo.loadVersionInfo("org.apache.http.client", DatasetUpdaterHTTP.class.getClassLoader());
        final String release = (vi != null) ? vi.getRelease() : VersionInfo.UNAVAILABLE;
        HttpProtocolParams.setUserAgent(httpParams,             "Apache-HttpClient/" + release + " (java 1.5)");

        return httpParams;
    }
    
    private static String getHeader(HttpResponse response, String headerName)
    {
        Header h = response.getLastHeader(headerName) ;
        if ( h == null )
            return null ;
        return h.getValue() ;
    }

    private Graph exec(String targetStr, Graph graph, HttpUriRequest httpRequest)
    {
        HttpClient httpclient = new DefaultHttpClient(httpParams) ;
        
        if ( graph != null )
        {
            // ???
            // Impedence mismatch - is there a better way?
            ByteArrayOutputStream out = new ByteArrayOutputStream() ;
            Model model = ModelFactory.createModelForGraph(graph) ;
            model.write(out, "RDF/XML") ;
            byte[] bytes = out.toByteArray() ;
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
            InputStreamEntity reqEntity = new InputStreamEntity(in, bytes.length) ;
            reqEntity.setContentType(WebContent.contentTypeRDFXML) ;
            reqEntity.setContentEncoding(HTTP.UTF_8) ;
            HttpEntity entity = reqEntity ;
            ((HttpEntityEnclosingRequestBase)httpRequest).setEntity(entity) ;
        }
        
        // httpclient.getParams().setXXX
        try {
            HttpResponse response = httpclient.execute(httpRequest) ;

            int responseCode = response.getStatusLine().getStatusCode() ;
            String responseMessage = response.getStatusLine().getReasonPhrase() ;

            if (300 <= responseCode && responseCode < 400) throw new FusekiRequestException(responseCode, responseMessage) ;

            // Other 400 and 500 - errors

            if (responseCode >= 400) throw new FusekiRequestException(responseCode, responseMessage) ;

            String contentType = getHeader(response, HttpNames.hContentType) ;
            String charset = getHeader(response, HttpNames.hContentType) ;

            // Get hold of the response entity
            HttpEntity entity = response.getEntity() ;

            TypedStream ts = null ;
            if (entity != null)
            {
                InputStream instream = entity.getContent() ;
                ts = new TypedStream(instream, contentType, charset) ;
            }
            return readGraph(ts, null) ;
        } catch (IOException ex)
        {
            httpRequest.abort() ;
            return null ;
        }
    }

    private Graph readGraph(TypedStream ts, String base)
    {
        Lang lang = FusekiLib.langFromContentType(ts.getMediaType()) ;
        if ( lang == null )
            throw new FusekiException("Unknown lang for "+ts.getMediaType()) ;
        Graph graph = GraphFactory.createGraphMem() ;
        Sink<Triple> sink = new SinkTriplesToGraph(graph) ;
        LangRIOT parser ;
        
        if ( lang.equals(Lang.RDFXML) )
            parser = LangRDFXML.create(ts.getInput(), base, base, null, sink) ;
        else
            parser = RiotReader.createParserTriples(ts.getInput(), lang, base, sink) ;
        parser.parse() ;
        IO.close(ts.getInput()) ;
        return graph ;
    }    
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
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