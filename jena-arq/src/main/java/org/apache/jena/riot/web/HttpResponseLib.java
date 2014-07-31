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

package org.apache.jena.riot.web;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.util.EntityUtils ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;

/** A collection of handlers for response handling.
 * @see HttpOp
 */
public class HttpResponseLib
{
    public static HttpCaptureResponse<Graph> graphHandler() { return new GraphReader() ; }
    static class GraphReader implements HttpCaptureResponse<Graph>
    {
        private Graph graph = null ;
        @Override
        final public void handle(String baseIRI, HttpResponse response)
        {
            try {
                Graph g = GraphFactory.createDefaultGraph() ;
                HttpEntity entity = response.getEntity() ;
                // org.apache.http.entity.ContentType ;
                String ct = contentType(response) ;
                Lang lang = RDFLanguages.contentTypeToLang(ct) ;
                StreamRDF dest = StreamRDFLib.graph(g) ; 
                try(InputStream in = entity.getContent()) {
                    RDFDataMgr.parse(dest, in, baseIRI, lang) ;
                }
                this.graph = g ; 
            } catch (IOException ex) { IO.exception(ex) ; }
        }
    
        @Override
        public Graph get() { return graph ; }
    }

    public static HttpResponseHandler httpDumpResponse = new HttpResponseHandler()
    {
        @Override
        public void handle(String baseIRI , HttpResponse response )
        {
            try {
                HttpEntity entity = response.getEntity() ;
                org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.get(entity) ;
                System.out.println("Content-type: "+ct) ;
                System.out.println() ;
                try (InputStream in = entity.getContent()) {
                    int l ;
                    byte buffer[] = new byte[1024] ;
                    while ((l = in.read(buffer)) != -1) {
                        System.out.print(new String(buffer, 0, l, "UTF-8")) ;
                    }
                }
            } catch (IOException ex)
            {
                ex.printStackTrace(System.err) ;
            }
        }
    } ;
    
    public static HttpResponseHandler nullResponse = new HttpResponseHandler() {
        @Override
        public void handle(String baseIRI , HttpResponse response ) {
            EntityUtils.consumeQuietly(response.getEntity()) ;
        }
    } ;
    
    public static ResultsFormat contentTypeToResultSet(String contentType) { return mapContentTypeToResultSet.get(contentType) ; }
    private static final Map<String, ResultsFormat> mapContentTypeToResultSet = new HashMap<>() ;
    static
    {
        mapContentTypeToResultSet.put(WebContent.contentTypeResultsXML, ResultsFormat.FMT_RS_XML) ;
        mapContentTypeToResultSet.put(WebContent.contentTypeResultsJSON, ResultsFormat.FMT_RS_JSON) ;
        mapContentTypeToResultSet.put(WebContent.contentTypeTextTSV, ResultsFormat.FMT_RS_TSV) ;
    }

    /** Response handling for SPARQL result sets. */
    public static class HttpCaptureResponseResultSet implements HttpCaptureResponse<ResultSet>
    {    
        ResultSet rs = null ;
        @Override
        public void handle(String baseIRI , HttpResponse response ) throws IOException
        {
            String ct = contentType(response) ;
            ResultsFormat fmt = mapContentTypeToResultSet.get(ct) ;
            InputStream in = response.getEntity().getContent() ;
            rs = ResultSetFactory.load(in, fmt) ;
            // Force reading
            rs = ResultSetFactory.copyResults(rs) ;
        }

        @Override
        public ResultSet get()
        {
            return rs ;
        }
    }
    
    private static String contentType(HttpResponse response) {
        HttpEntity entity = response.getEntity() ;
        //org.apache.http.entity.ContentType ;
        org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.get(entity) ;
        return ct.getMimeType() ;
    }
}
